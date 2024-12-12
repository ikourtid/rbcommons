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
import static com.rb.nonbiz.testutils.Asserters.assertOptionalNonEmpty;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.function.UnaryOperator.identity;
import static org.junit.Assert.assertTrue;

public class UpperBoundFinderTest extends RBTest<UpperBoundFinder> {

  private final Function<BigDecimal, Double> EVALUATE_INPUT_TO_SQUARE = x -> x.multiply(x).doubleValue();
  private final BigDecimal STARTING_SINGLE_GUESS_FOR_SEARCH = BigDecimal.ONE;
  private final UnaryOperator<BigDecimal> INCREASE_UPPER_BOUND_BY_DOUBLING =
      u -> u.multiply(BigDecimal.valueOf(2), DEFAULT_MATH_CONTEXT);
  private final int MAX_ITERATIONS = 50;

  private final BigDecimal STARTING_UPPER_BOUND_FOR_SEARCH_TWO      = BigDecimal.valueOf(2.0);

  @Test
  public void happyPath_targetIsAboveSingleStartingGuess_canFindValidBound() {
    double targetAboveStartingGuess = 1.2;
    assertValidUpperBoundCanBeFoundSingleGuess(targetAboveStartingGuess);
  }

  @Test
  public void happyPath_targetIsBelowSingleStartingGuess_canFindValidBound() {
    double targetBelowStartingGuess = 0.8;
    assertValidUpperBoundCanBeFoundSingleGuess(targetBelowStartingGuess);
  }

  @Test
  public void happyPath_targetIsSameAsSingleStartingGuess_canFindValidBound() {
    assertValidUpperBoundCanBeFoundSingleGuess(1.0);
  }

  // for the case with a single starting guess value
  private void assertValidUpperBoundCanBeFoundSingleGuess(double target) {
    Optional<BigDecimal> lowerBound = makeTestObject().findPossiblyIncreasedUpperBound(
        EVALUATE_INPUT_TO_SQUARE,
        STARTING_SINGLE_GUESS_FOR_SEARCH,
        target,
        INCREASE_UPPER_BOUND_BY_DOUBLING,
        MAX_ITERATIONS);
    double valueAtUpper = EVALUATE_INPUT_TO_SQUARE.apply(lowerBound.get());
    assertTrue(valueAtUpper >= target);
  }

  @Test
  public void movingInWrongDirection_throws() {
    Function<UnaryOperator<BigDecimal>, Optional<BigDecimal>> maker = upperBoundIncreaser ->
        makeTestObject().findPossiblyIncreasedUpperBound(
            EVALUATE_INPUT_TO_SQUARE,
            STARTING_UPPER_BOUND_FOR_SEARCH_TWO,
            12.34, // must be high so we won't just terminate without entering the loop
            upperBoundIncreaser,
            MAX_ITERATIONS);

    Optional<BigDecimal> doesNotThrow = maker.apply(INCREASE_UPPER_BOUND_BY_DOUBLING);
    // This throws because it we should be loosening (increasing) the upper bound, not tightening (decreasing) it.
    assertIllegalArgumentException( () -> maker.apply(l -> l.divide(BigDecimal.valueOf(2), DEFAULT_MATH_CONTEXT)));
  }

  @Test
  public void happyPath_targetIsAboveGuessRange_canFindValidBound() {
    double targetAboveStartingGuessRange = 5.0;
    assertValidUpperBoundsCanBeFoundWithGuessRange(targetAboveStartingGuessRange);
  }

  @Test
  public void happyPath_targetIsBelowGuessRange_canFindValidBound() {
    double targetBelowStartingGuessRange = 0.2;
    assertValidUpperBoundsCanBeFoundWithGuessRange(targetBelowStartingGuessRange);
  }

  @Test
  public void happyPath_targetIsInsideGuessRange_canFindValidBound() {
    double targetInsideStartingGuessRange = 1.0;
    assertValidUpperBoundsCanBeFoundWithGuessRange(targetInsideStartingGuessRange);
  }

  @Test
  public void happyPath_targetIsOnEdgeOfGuessRange_canFindValidBound() {
    double targetLowerEdgeStartingGuessRange = 0.5;
    assertValidUpperBoundsCanBeFoundWithGuessRange(targetLowerEdgeStartingGuessRange);

    double targetUpperEdgeStartingGuessRange = 2.0;
    assertValidUpperBoundsCanBeFoundWithGuessRange(targetUpperEdgeStartingGuessRange);
  }

  @Test
  public void cannotFindBoundsStartingGuessRange_withinIterationsCap_throws() {
    int tooFewMaxIterationsToFindBounds = 2;
    // try a target that's too low (999). The starting guess is 1. So if we keep doubling it (2, 4, etc.)
    // we can't get to a valid upper bound that's above 999.
    assertIllegalArgumentException( () -> makeTestObject().findPossiblyIncreasedUpperBound(
        EVALUATE_INPUT_TO_SQUARE,
        STARTING_UPPER_BOUND_FOR_SEARCH_TWO,
        999.0,
        INCREASE_UPPER_BOUND_BY_DOUBLING,
        tooFewMaxIterationsToFindBounds));
  }

  @Test
  public void cornerCase_exactlyOnTarget_returnsValue() {
    int maxIterations = 10;

    // The upper bound isn't already on target.
    assertOptionalNonEmpty(
        makeTestObject().findPossiblyIncreasedUpperBound(
            EVALUATE_INPUT_TO_SQUARE,
            STARTING_UPPER_BOUND_FOR_SEARCH_TWO,
            1.0,
            INCREASE_UPPER_BOUND_BY_DOUBLING,
            maxIterations),
        bigDecimalMatcher(STARTING_UPPER_BOUND_FOR_SEARCH_TWO, DEFAULT_EPSILON_1e_8));

    // The upper bound is already on target, but the lower bound isn't.
    assertOptionalNonEmpty(
        makeTestObject().findPossiblyIncreasedUpperBound(
            EVALUATE_INPUT_TO_SQUARE,
            BigDecimal.ONE,
            1.0,
            identity(), // this says we can't increase the upper bound.
            maxIterations),
        bigDecimalMatcher(BigDecimal.ONE, DEFAULT_EPSILON_1e_8));
  }

  // for the case with a starting range guess [guessLower, guessUpper]
  private void assertValidUpperBoundsCanBeFoundWithGuessRange(double target) {
    assertValidUpperBoundsCanBeFoundWithGuessRange(
        target,
        STARTING_UPPER_BOUND_FOR_SEARCH_TWO);
  }

  private void assertValidUpperBoundsCanBeFoundWithGuessRange(
      double target,
      BigDecimal initialUpperBound) {
    Optional<BigDecimal> validUpperBound = makeTestObject().findPossiblyIncreasedUpperBound(
        EVALUATE_INPUT_TO_SQUARE,
        initialUpperBound,
        target,
        INCREASE_UPPER_BOUND_BY_DOUBLING,
        MAX_ITERATIONS);
    assertOptionalNonEmpty(validUpperBound);
    double valueAtUpper = EVALUATE_INPUT_TO_SQUARE.apply(validUpperBound.get());
    assertTrue(valueAtUpper > target);
  }

  @Override
  protected UpperBoundFinder makeTestObject() {
    return new UpperBoundFinder();
  }

}
