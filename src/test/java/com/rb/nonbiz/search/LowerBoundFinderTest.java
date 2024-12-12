package com.rb.nonbiz.search;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.bigDecimalMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalNonEmpty;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.function.UnaryOperator.identity;
import static org.junit.Assert.assertTrue;

public class LowerBoundFinderTest extends RBTest<LowerBoundFinder> {

  private final Function<BigDecimal, Double> EVALUATE_INPUT_TO_SQUARE = x -> x.multiply(x).doubleValue();
  private final BigDecimal STARTING_SINGLE_GUESS_FOR_SEARCH = BigDecimal.ONE;
  private final UnaryOperator<BigDecimal> REDUCE_LOWER_BOUND_BY_HALVING =
      l -> l.divide(  BigDecimal.valueOf(2), DEFAULT_MATH_CONTEXT);
  private final int MAX_ITERATIONS = 50;

  // the following is used for the override of the initial "guess" lower bound
  private final BigDecimal STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF = BigDecimal.valueOf(0.5);

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
  public void cannotFindBoundsWithinIterationsCap_throws() {
    int tooFewMaxIterationsToFindBounds = 2;
    // try targets that are too low and too high
    for (double target : ImmutableList.of(1e-7, 999_999.0)) {
      assertIllegalArgumentException( () -> makeTestObject().findPossiblyReducedLowerBound(
          EVALUATE_INPUT_TO_SQUARE,
          STARTING_SINGLE_GUESS_FOR_SEARCH,
          target,
          REDUCE_LOWER_BOUND_BY_HALVING,
          INCREASE_UPPER_BOUND_BY_DOUBLING,
          tooFewMaxIterationsToFindBounds));
    }
  }

  // for the case with a single starting guess value
  private void assertValidLowerAndUpperBoundsCanBeFoundSingleGuess(double target) {
    Range<BigDecimal> lowerAndUpperBounds = makeTestObject().findLowerAndUpperBounds(
        EVALUATE_INPUT_TO_SQUARE,
        STARTING_SINGLE_GUESS_FOR_SEARCH,
        target,
        REDUCE_LOWER_BOUND_BY_HALVING,
        INCREASE_UPPER_BOUND_BY_DOUBLING,
        MAX_ITERATIONS);
    double valueAtLower = EVALUATE_INPUT_TO_SQUARE.apply(lowerAndUpperBounds.lowerEndpoint());
    double valueAtUpper = EVALUATE_INPUT_TO_SQUARE.apply(lowerAndUpperBounds.upperEndpoint());
    assertTrue(valueAtLower <= target);
    assertTrue(valueAtUpper >= target);
  }

  // for the case of a starting range of guesses [lowerBound, upperBound]
  @Test
  public void lowerBoundAboveUpperBound_throws() {
    for (double target : ImmutableList.of(1.0, 1.1, 1.2, 2.1, 2.2, 2.3)) {
      assertIllegalArgumentException(() -> makeTestObject().findLowerAndUpperBounds(
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
    Function<Function<BigDecimal, Double>, Range<BigDecimal>> maker = evaluator ->
        makeTestObject().findLowerAndUpperBounds(
            evaluator,
            STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF,
            STARTING_UPPER_BOUND_FOR_SEARCH_TWO,
            1.234,
            REDUCE_LOWER_BOUND_BY_HALVING,
            INCREASE_UPPER_BOUND_BY_DOUBLING,
            MAX_ITERATIONS);

    // use f(x) = x
    Range<BigDecimal> doesNotThrow;
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
    assertValidLowerBoundCanBeFoundWithGuessRange(targetAboveStartingGuessRange);
  }

  @Test
  public void happyPath_targetIsAboveGuessRange_bothGuessesSame_canFindValidBounds() {
    double targetAboveStartingGuessRange = 5.0;
    assertValidLowerBoundCanBeFoundWithGuessRange(
        // use lower bound = upper bound
        targetAboveStartingGuessRange,
        STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF,
        STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF);

    double targetBelowStartingGuessRange = 0.2;
    assertValidLowerBoundCanBeFoundWithGuessRange(
        // use lower bound = upper bound
        targetBelowStartingGuessRange,
        STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF,
        STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF);
  }

  @Test
  public void happyPath_targetIsBelowGuessRange_canFindValidBounds() {
    double targetBelowStartingGuessRange = 0.2;
    assertValidLowerBoundCanBeFoundWithGuessRange(targetBelowStartingGuessRange);
  }

  @Test
  public void happyPath_targetIsInsideGuessRange_canFindValidBounds() {
    double targetInsideStartingGuessRange = 1.0;
    assertValidLowerBoundCanBeFoundWithGuessRange(targetInsideStartingGuessRange);
  }

  @Test
  public void happyPath_targetIsOnEdgeOfGuessRange_canFindValidBounds() {
    double targetLowerEdgeStartingGuessRange = 0.5;
    assertValidLowerBoundCanBeFoundWithGuessRange(targetLowerEdgeStartingGuessRange);

    double targetUpperEdgeStartingGuessRange = 2.0;
    assertValidLowerBoundCanBeFoundWithGuessRange(targetUpperEdgeStartingGuessRange);
  }

  @Test
  public void cannotFindBoundsStartingGuessRange_withinIterationsCap_throws() {
    int tooFewMaxIterationsToFindBounds = 2;
    // try targets that are both too low and too high
// FIXME IAK Issue #1527    for (double target : ImmutableList.of(1e-7, 999_999.0)) {
    assertIllegalArgumentException( () -> makeTestObject().findPossiblyReducedLowerBound(
        EVALUATE_INPUT_TO_SQUARE,
        STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF,
        1e-7,
        REDUCE_LOWER_BOUND_BY_HALVING,
        tooFewMaxIterationsToFindBounds));
  }

  @Test
  public void cornerCase_exactlyOnTarget_returnsValue() {
    int maxIterations = 10;

    // The lower bound isn't on target
    assertOptionalNonEmpty(
        makeTestObject().findPossiblyReducedLowerBound(
            EVALUATE_INPUT_TO_SQUARE,
            STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF,
            1.0,
            REDUCE_LOWER_BOUND_BY_HALVING,
            maxIterations),
        bigDecimalMatcher(STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF, DEFAULT_EPSILON_1e_8));

    // The lower bound is already on target
    assertOptionalNonEmpty(
        makeTestObject().findPossiblyReducedLowerBound(
            EVALUATE_INPUT_TO_SQUARE,
            BigDecimal.ONE,
            1.0,
            identity(), // this says we can't increase the lower bound.
            maxIterations),
        bigDecimalMatcher(BigDecimal.ONE, DEFAULT_EPSILON_1e_8));
  }

  // for the case with a starting range guess [guessLower, guessUpper]
  private void assertValidLowerBoundCanBeFoundWithGuessRange(double target) {
    assertValidLowerBoundCanBeFoundWithGuessRange(
        target,
        STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF);
  }

  private void assertValidLowerBoundCanBeFoundWithGuessRange(
      double target,
      BigDecimal initialLowerBound) {
    Optional<BigDecimal> validLowerBound = makeTestObject().findPossiblyReducedLowerBound(
        EVALUATE_INPUT_TO_SQUARE,
        initialLowerBound,
        target,
        REDUCE_LOWER_BOUND_BY_HALVING,
        MAX_ITERATIONS);
    assertOptionalNonEmpty(validLowerBound);
    double valueAtLower = EVALUATE_INPUT_TO_SQUARE.apply(validLowerBound.get());
    assertTrue(valueAtLower < target);
  }

  @Override
  protected LowerBoundFinder makeTestObject() {
    return new LowerBoundFinder();
  }

}
