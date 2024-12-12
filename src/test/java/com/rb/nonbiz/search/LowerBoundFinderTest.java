package com.rb.nonbiz.search;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.bigDecimalMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
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

  private final BigDecimal STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF = BigDecimal.valueOf(0.5);

  @Test
  public void happyPath_targetIsAboveSingleStartingGuess_canFindValidBound() {
    double targetAboveStartingGuess = 1.2;
    assertValidLowerBoundCanBeFoundSingleGuess(targetAboveStartingGuess);
  }

  @Test
  public void happyPath_targetIsBelowSingleStartingGuess_canFindValidBound() {
    double targetBelowStartingGuess = 0.8;
    assertValidLowerBoundCanBeFoundSingleGuess(targetBelowStartingGuess);
  }

  @Test
  public void happyPath_targetIsSameAsSingleStartingGuess_canFindValidBound() {
    assertValidLowerBoundCanBeFoundSingleGuess(1.0);
  }

  @Test
  public void cannotFindBoundsWithinIterationsCap_returnsEmptyOptional() {
    int tooFewMaxIterationsToFindBounds = 2;
    // try a target that's too low (1e-7). The starting guess is 1. So if we keep halving it (0.5, 0.25, etc.)
    // we can't get to a valid bound that's below 1e-7.
    assertOptionalEmpty(
        makeTestObject().findPossiblyReducedLowerBound(
            EVALUATE_INPUT_TO_SQUARE,
            STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF,
            1e-7,
            REDUCE_LOWER_BOUND_BY_HALVING,
            tooFewMaxIterationsToFindBounds));
  }

  // for the case with a single starting guess value
  private void assertValidLowerBoundCanBeFoundSingleGuess(double target) {
    Optional<BigDecimal> lowerBound = makeTestObject().findPossiblyReducedLowerBound(
        EVALUATE_INPUT_TO_SQUARE,
        STARTING_SINGLE_GUESS_FOR_SEARCH,
        target,
        REDUCE_LOWER_BOUND_BY_HALVING,
        MAX_ITERATIONS);
    double valueAtLower = EVALUATE_INPUT_TO_SQUARE.apply(lowerBound.get());
    assertTrue(valueAtLower <= target);
  }

  @Test
  public void movingInWrongDirection_throws() {
    Function<UnaryOperator<BigDecimal>, Optional<BigDecimal>> maker = lowerBoundReducer ->
        makeTestObject().findPossiblyReducedLowerBound(
            EVALUATE_INPUT_TO_SQUARE,
            STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF,
            -12.34, // must be low
            lowerBoundReducer,
            MAX_ITERATIONS);

    Optional<BigDecimal> doesNotThrow = maker.apply(REDUCE_LOWER_BOUND_BY_HALVING);
    // This throws because it we should be loosening (lowering) the lower bound, not tightening (increasing) it.
    assertIllegalArgumentException( () -> maker.apply(l -> l.multiply(BigDecimal.valueOf(2))));
  }

  @Test
  public void happyPath_targetIsAboveGuessRange_canFindValidBound() {
    double targetAboveStartingGuessRange = 5.0;
    assertValidLowerBoundCanBeFoundWithGuessRange(targetAboveStartingGuessRange);
  }

  @Test
  public void happyPath_targetIsAboveGuessRange_bothGuessesSame_canFindValidBound() {
    double targetAboveStartingGuessRange = 5.0;
    assertValidLowerBoundCanBeFoundWithGuessRange(
        // use lower bound = upper bound
        targetAboveStartingGuessRange,
        STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF);

    double targetBelowStartingGuessRange = 0.2;
    assertValidLowerBoundCanBeFoundWithGuessRange(
        // use lower bound = upper bound
        targetBelowStartingGuessRange,
        STARTING_LOWER_BOUND_FOR_SEARCH_ONE_HALF);
  }

  @Test
  public void happyPath_targetIsBelowGuessRange_canFindValidBound() {
    double targetBelowStartingGuessRange = 0.2;
    assertValidLowerBoundCanBeFoundWithGuessRange(targetBelowStartingGuessRange);
  }

  @Test
  public void happyPath_targetIsInsideGuessRange_canFindValidBound() {
    double targetInsideStartingGuessRange = 1.0;
    assertValidLowerBoundCanBeFoundWithGuessRange(targetInsideStartingGuessRange);
  }

  @Test
  public void happyPath_targetIsOnEdgeOfGuessRange_canFindValidBound() {
    double targetLowerEdgeStartingGuessRange = 0.5;
    assertValidLowerBoundCanBeFoundWithGuessRange(targetLowerEdgeStartingGuessRange);

    double targetUpperEdgeStartingGuessRange = 2.0;
    assertValidLowerBoundCanBeFoundWithGuessRange(targetUpperEdgeStartingGuessRange);
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
