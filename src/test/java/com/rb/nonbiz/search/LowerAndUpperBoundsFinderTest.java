package com.rb.nonbiz.search;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;
import static org.junit.Assert.assertTrue;

public class LowerAndUpperBoundsFinderTest extends RBTest<LowerAndUpperBoundsFinder> {

  Function<BigDecimal, Double> evaluateInput = x -> x.multiply(x).doubleValue();
  BigDecimal startingSingleGuessForSearch = BigDecimal.ONE;
  UnaryOperator<BigDecimal> reduceLowerBound   = l -> l.divide( BigDecimal.valueOf(2),  DEFAULT_MATH_CONTEXT);
  UnaryOperator<BigDecimal> increaseUpperBound = u -> u.multiply(BigDecimal.valueOf(2), DEFAULT_MATH_CONTEXT);
  int maxIterations = 50;

  // the following are used for the override with 2 initial "guess" bounds
  BigDecimal startingLowerBoundForSearch = BigDecimal.valueOf(0.5);
  BigDecimal startingUpperBoundForSearch = BigDecimal.valueOf(2.0);

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
      assertIllegalArgumentException(() -> makeTestObject().findLowerAndUpperBounds(
          evaluateInput,
          startingSingleGuessForSearch,
          target,
          reduceLowerBound,
          increaseUpperBound,
          tooFewMaxIterationsToFindBounds));
    }
  }

  // for the case with a single starting guess value
  private void assertValidLowerAndUpperBoundsCanBeFoundSingleGuess(double target) {
    Range<BigDecimal> lowerAndUpperBounds = makeTestObject().findLowerAndUpperBounds(
        evaluateInput, startingSingleGuessForSearch, target, reduceLowerBound, increaseUpperBound, maxIterations);
    double valueAtLower = evaluateInput.apply(lowerAndUpperBounds.lowerEndpoint());
    double valueAtUpper = evaluateInput.apply(lowerAndUpperBounds.upperEndpoint());
    assertTrue(valueAtLower < target);
    assertTrue(valueAtUpper > target);
  }

  // for the case of a starting range of guesses [lowerBound, upperBound]
  @Test
  public void lowerBoundAboveUpperBound_throws() {
    assertIllegalArgumentException( () -> makeTestObject().findLowerAndUpperBounds(
        evaluateInput,
        BigDecimal.valueOf(2.2),  // lower bound
        BigDecimal.valueOf(1.1),  // invalid upper bound; below lower bound
        1.234,                    // target
        reduceLowerBound,
        increaseUpperBound,
        maxIterations) );
  }

  @Test
  public void decreasingFunction_throws() {
    Function<Function<BigDecimal, Double>, Range<BigDecimal>> maker = evaluator ->
        makeTestObject().findLowerAndUpperBounds(
            evaluator,
            startingLowerBoundForSearch,
            startingUpperBoundForSearch,
            1.234,
            reduceLowerBound,
            increaseUpperBound,
            maxIterations);

    Range<BigDecimal> doesNotThrow = maker.apply(x -> x.doubleValue());

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
        targetAboveStartingGuessRange, startingLowerBoundForSearch, startingLowerBoundForSearch);

    double targetBelowStartingGuessRange = 0.2;
    assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(
        // use lower bound = upper bound
        targetBelowStartingGuessRange, startingLowerBoundForSearch, startingLowerBoundForSearch);
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
    // try targets that are both too low and too high
    for (double target : ImmutableList.of(1e-7, 999_999.0)) {
      assertIllegalArgumentException(() -> makeTestObject().findLowerAndUpperBounds(
          evaluateInput,
          startingLowerBoundForSearch,
          startingUpperBoundForSearch,
          target,
          reduceLowerBound,
          increaseUpperBound,
          tooFewMaxIterationsToFindBounds));
    }
  }

  // for the case with a starting range guess [guessLower, guessUpper]
  private void assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(double target) {
    assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(
        target,
        startingLowerBoundForSearch,
        startingUpperBoundForSearch);
  }

  private void assertValidLowerAndUpperBoundsCanBeFoundWithGuessRange(
      double target,
      BigDecimal lowerBound,
      BigDecimal upperBound) {
    Range<BigDecimal> lowerAndUpperBounds = makeTestObject().findLowerAndUpperBounds(
        evaluateInput,
        lowerBound,
        upperBound,
        target,
        reduceLowerBound,
        increaseUpperBound,
        maxIterations);
    double valueAtLower = evaluateInput.apply(lowerAndUpperBounds.lowerEndpoint());
    double valueAtUpper = evaluateInput.apply(lowerAndUpperBounds.upperEndpoint());
    assertTrue(valueAtLower < target);
    assertTrue(valueAtUpper > target);
  }

  @Override
  protected LowerAndUpperBoundsFinder makeTestObject() {
    return makeRealObject(LowerAndUpperBoundsFinder.class);
  }

}
