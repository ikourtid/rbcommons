package com.rb.nonbiz.search;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.ClosedRange.singletonClosedRange;
import static com.rb.nonbiz.search.BinarySearchInitialXBoundsResult.binarySearchBoundsCanBracketTargetY;
import static com.rb.nonbiz.search.BinarySearchInitialXBoundsResult.onlyHasValidLowerBoundForX;
import static com.rb.nonbiz.search.BinarySearchInitialXBoundsResult.onlyHasValidUpperBoundForX;
import static com.rb.nonbiz.search.BinarySearchInitialXBoundsResultTest.binarySearchInitialXBoundsResultMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.bigDecimalMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.function.UnaryOperator.identity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class LowerAndUpperBoundsFinderTest extends RBCommonsIntegrationTest<LowerAndUpperBoundsFinder> {

  private final Function<BigDecimal, Double> EVALUATE_INPUT_TO_SQUARE = x -> x.multiply(x).doubleValue();
  private final BigDecimal STARTING_SINGLE_GUESS_FOR_SEARCH = BigDecimal.ONE;
  private final UnaryOperator<BigDecimal> REDUCE_LOWER_BOUND_BY_HALVING =
      l -> l.divide(  BigDecimal.valueOf(2), DEFAULT_MATH_CONTEXT);
  private final UnaryOperator<BigDecimal> INCREASE_UPPER_BOUND_BY_DOUBLING =
      u -> u.multiply(BigDecimal.valueOf(2), DEFAULT_MATH_CONTEXT);
  private final int MAX_ITERATIONS = 50;

  // the following are used for the override with 2 initial "guess" bounds
  private final BigDecimal STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF = BigDecimal.valueOf(0.5);
  private final BigDecimal STARTING_UPPER_BOUND_FOR_SEARCH_TWO      = BigDecimal.valueOf(2.0);

  @Test
  public void happyPath_targetIsAboveSingleStartingGuess_canFindValidBounds() {
    double targetAboveStartingGuess = 1.2;
    assertValidLowerAndUpperBoundsCanBeFoundSingleGuess(targetAboveStartingGuess);
  }

  @Test
  public void happyPath_targetIsBelowSingleStartingGuess_canFindValidBounds() {
    double targetBelowStartingGuess = 0.8;
    assertValidLowerAndUpperBoundsCanBeFoundSingleGuess(targetBelowStartingGuess);
  }

  @Test
  public void happyPath_targetIsSameAsSingleStartingGuess_canFindValidBounds() {
    assertValidLowerAndUpperBoundsCanBeFoundSingleGuess(1.0);
  }

  @Test
  public void cannotFindBoundsWithinIterationsCap_returnsOnlyOneLimit() {
    int tooFewMaxIterationsToFindBounds = 2;

    BiConsumer<Double, BinarySearchInitialXBoundsResult<BigDecimal>> asserter = (target, expectedResult) ->
        assertThat(
            makeRealObject().findLowerAndUpperBounds(
                EVALUATE_INPUT_TO_SQUARE,
                STARTING_SINGLE_GUESS_FOR_SEARCH,
                target,
                REDUCE_LOWER_BOUND_BY_HALVING,
                INCREASE_UPPER_BOUND_BY_DOUBLING,
                tooFewMaxIterationsToFindBounds),
            binarySearchInitialXBoundsResultMatcher(expectedResult, v -> bigDecimalMatcher(v, DEFAULT_EPSILON_1e_8)));

    // Target is too low (high), and so we can't get to a valid lower (upper) bound (< target) in a few iterations
    asserter.accept(1e-7, onlyHasValidUpperBoundForX(STARTING_SINGLE_GUESS_FOR_SEARCH));
    asserter.accept(1e+7, onlyHasValidLowerBoundForX(STARTING_SINGLE_GUESS_FOR_SEARCH));
  }

  // for the case with a single starting guess value
  private void assertValidLowerAndUpperBoundsCanBeFoundSingleGuess(double target) {
    ClosedRange<BigDecimal> lowerAndUpperBounds = makeRealObject()
        .findLowerAndUpperBounds(
            EVALUATE_INPUT_TO_SQUARE,
            STARTING_SINGLE_GUESS_FOR_SEARCH,
            target,
            REDUCE_LOWER_BOUND_BY_HALVING,
            INCREASE_UPPER_BOUND_BY_DOUBLING,
            MAX_ITERATIONS)
        .getLowerAndUpperBoundOrThrow();

    double valueAtLower = EVALUATE_INPUT_TO_SQUARE.apply(lowerAndUpperBounds.lowerEndpoint());
    double valueAtUpper = EVALUATE_INPUT_TO_SQUARE.apply(lowerAndUpperBounds.upperEndpoint());
    assertTrue(valueAtLower <= target);
    assertTrue(valueAtUpper >= target);
  }

  // for the case of a starting range of guesses [lowerBound, upperBound]
  @Test
  public void lowerBoundAboveUpperBound_throws() {
    for (double target : ImmutableList.of(1.0, 1.1, 1.2, 2.1, 2.2, 2.3)) {
      assertIllegalArgumentException(() -> makeRealObject().findLowerAndUpperBounds(
          EVALUATE_INPUT_TO_SQUARE,
          BigDecimal.valueOf(2.2),  // lower bound
          BigDecimal.valueOf(1.1),  // invalid upper bound; below lower bound
          target,                   // the target doesn't matter; the bounds are invalid
          REDUCE_LOWER_BOUND_BY_HALVING,
          INCREASE_UPPER_BOUND_BY_DOUBLING,
          MAX_ITERATIONS));
    }
  }

  @Test
  public void decreasingFunction_throws() {
    Function<Function<BigDecimal, Double>, BinarySearchInitialXBoundsResult<BigDecimal>> maker = evaluator ->
        makeRealObject().findLowerAndUpperBounds(
            evaluator,
            STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF,
            STARTING_UPPER_BOUND_FOR_SEARCH_TWO,
            1.234,
            REDUCE_LOWER_BOUND_BY_HALVING,
            INCREASE_UPPER_BOUND_BY_DOUBLING,
            MAX_ITERATIONS);

    // use f(x) = x
    BinarySearchInitialXBoundsResult<BigDecimal> doesNotThrow;
    doesNotThrow = maker.apply(x -> x.doubleValue());

    // use f(x) = 1e-9 * x
    doesNotThrow = maker.apply(x -> 1e-9 * x.doubleValue());

    // use f(x) = -1e-9 * x
    assertIllegalArgumentException( () -> maker.apply(
        x -> -1e-9 * x.doubleValue()));

    // use f(x) = -x
    assertIllegalArgumentException( () -> maker.apply(
        x -> x.negate().doubleValue()));

    // use f(x) = 1/x
    assertIllegalArgumentException( () -> maker.apply(
        x -> BigDecimal.ONE.divide(x, DEFAULT_MATH_CONTEXT).doubleValue()));
  }

  @Test
  public void happyPath_targetIsAboveGuessRange_canFindValidBounds() {
    double targetAboveStartingGuessRange = 5.0;
    assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(targetAboveStartingGuessRange);
  }

  @Test
  public void happyPath_targetIsAboveGuessRange_bothGuessesSame_canFindValidBounds() {
    double targetAboveStartingGuessRange = 5.0;
    assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(
        // use lower bound = upper bound
        targetAboveStartingGuessRange,
        STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF,
        STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF);

    double targetBelowStartingGuessRange = 0.2;
    assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(
        // use lower bound = upper bound
        targetBelowStartingGuessRange,
        STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF,
        STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF);
  }

  @Test
  public void happyPath_targetIsBelowGuessRange_canFindValidBounds() {
    double targetBelowStartingGuessRange = 0.2;
    assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(targetBelowStartingGuessRange);
  }

  @Test
  public void happyPath_targetIsInsideGuessRange_canFindValidBounds() {
    double targetInsideStartingGuessRange = 1.0;
    assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(targetInsideStartingGuessRange);
  }

  @Test
  public void happyPath_targetIsOnEdgeOfGuessRange_canFindValidBounds() {
    double targetLowerEdgeStartingGuessRange = 0.5;
    assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(targetLowerEdgeStartingGuessRange);

    double targetUpperEdgeStartingGuessRange = 2.0;
    assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(targetUpperEdgeStartingGuessRange);
  }

  @Test
  public void cannotFindBoundsStartingGuessRange_withinIterationsCap_throws() {
    int tooFewMaxIterationsToFindBounds = 2;

    BiConsumer<Double, BinarySearchInitialXBoundsResult<BigDecimal>> asserter = (target, expectedResult) ->
        assertThat(
            makeRealObject().findLowerAndUpperBounds(
                EVALUATE_INPUT_TO_SQUARE,
                STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF,
                STARTING_UPPER_BOUND_FOR_SEARCH_TWO,
                target,
                REDUCE_LOWER_BOUND_BY_HALVING,
                INCREASE_UPPER_BOUND_BY_DOUBLING,
                tooFewMaxIterationsToFindBounds),
            binarySearchInitialXBoundsResultMatcher(expectedResult, v -> bigDecimalMatcher(v, DEFAULT_EPSILON_1e_8)));

    // Target is too low (high), and so we can't get to a valid lower (upper) bound (< target) in a few iterations
    asserter.accept(1e-7, onlyHasValidUpperBoundForX(STARTING_UPPER_BOUND_FOR_SEARCH_TWO));
    asserter.accept(1e+7, onlyHasValidLowerBoundForX(STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF));
  }

  @Test
  public void cornerCase_exactlyOnTarget_returnsValue() {
    int maxIterations = 10;

    // The lower bound is already on target, but the upper bound isn't.
    assertThat(
        makeRealObject().findLowerAndUpperBounds(
            EVALUATE_INPUT_TO_SQUARE,
            BigDecimal.ONE,
            STARTING_UPPER_BOUND_FOR_SEARCH_TWO,
            1.0,
            identity(), // this says we can't decrease the lower bound.
            INCREASE_UPPER_BOUND_BY_DOUBLING,
            maxIterations),
        binarySearchInitialXBoundsResultMatcher(
            binarySearchBoundsCanBracketTargetY(closedRange(BigDecimal.ONE, STARTING_UPPER_BOUND_FOR_SEARCH_TWO)),
            f -> bigDecimalMatcher(f, DEFAULT_EPSILON_1e_8)));

    // The upper bound is already on target, but the lower bound isn't.
    assertThat(
        makeRealObject().findLowerAndUpperBounds(
            EVALUATE_INPUT_TO_SQUARE,
            STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF,
            BigDecimal.ONE,
            1.0,
            REDUCE_LOWER_BOUND_BY_HALVING,
            identity(), // this says we can't increase the upper bound.
            maxIterations),
        binarySearchInitialXBoundsResultMatcher(
            binarySearchBoundsCanBracketTargetY(closedRange(STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF, BigDecimal.ONE)),
            f -> bigDecimalMatcher(f, DEFAULT_EPSILON_1e_8)));

    // and now let's try both
    assertThat(
        makeRealObject().findLowerAndUpperBounds(
            EVALUATE_INPUT_TO_SQUARE,
            BigDecimal.ONE,
            BigDecimal.ONE,
            1.0,
            identity(), // this says we can't increase the lower bound.
            identity(), // this says we can't increase the upper bound.
            maxIterations),
        binarySearchInitialXBoundsResultMatcher(
            binarySearchBoundsCanBracketTargetY(singletonClosedRange(BigDecimal.ONE)),
            f -> bigDecimalMatcher(f, DEFAULT_EPSILON_1e_8)));
  }

  // for the case with a starting range guess [guessLower, guessUpper]
  private void assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(double target) {
    assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(
        target,
        STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF,
        STARTING_UPPER_BOUND_FOR_SEARCH_TWO);
  }

  private void assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(
      double target,
      BigDecimal lowerBound,
      BigDecimal upperBound) {
    ClosedRange<BigDecimal> lowerAndUpperBounds = makeRealObject()
        .findLowerAndUpperBounds(
            EVALUATE_INPUT_TO_SQUARE,
            lowerBound,
            upperBound,
            target,
            REDUCE_LOWER_BOUND_BY_HALVING,
            INCREASE_UPPER_BOUND_BY_DOUBLING,
            MAX_ITERATIONS)
        .getLowerAndUpperBoundOrThrow();

    double valueAtLower = EVALUATE_INPUT_TO_SQUARE.apply(lowerAndUpperBounds.lowerEndpoint());
    double valueAtUpper = EVALUATE_INPUT_TO_SQUARE.apply(lowerAndUpperBounds.upperEndpoint());
    assertTrue(valueAtLower < target);
    assertTrue(valueAtUpper > target);
  }

  @Override
  protected Class<LowerAndUpperBoundsFinder> getClassBeingTested() {
    return LowerAndUpperBoundsFinder.class;
  }

}
