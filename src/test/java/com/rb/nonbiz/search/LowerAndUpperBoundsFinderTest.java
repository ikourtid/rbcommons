package com.rb.nonbiz.search;

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
  BigDecimal startingGuessForSearch = BigDecimal.ONE;
  UnaryOperator<BigDecimal> reduceLowerBound = l -> l.divide(BigDecimal.valueOf(2), DEFAULT_MATH_CONTEXT);
  UnaryOperator<BigDecimal> increaseUpperBound = u -> u.multiply(BigDecimal.valueOf(2), DEFAULT_MATH_CONTEXT);
  int maxIterations = 50;

  @Test
  public void happyPath_targetIsAboveStartingGuess_canFindValidBounds() {
    double targetAboveStartingGuess = 1.2;
    assertValidLowerAndUpperBoundsCanBeFound(targetAboveStartingGuess);
  }

  @Test
  public void happyPath_targetIsBelowStartingGuess_canFindValidBounds() {
    double targetBelowStartingGuess = 0.8;
    assertValidLowerAndUpperBoundsCanBeFound(targetBelowStartingGuess);
  }

  @Test
  public void happyPath_targetIsSameAsStartingGuess_canFindValidBounds() {
    assertValidLowerAndUpperBoundsCanBeFound(1.0);
  }

  @Test
  public void cannotFindBoundsWithinIterationsCap_throws() {
    double target = 999_999;
    int tooFewMaxIterationsToFindBounds = 2;
    assertIllegalArgumentException( () -> makeTestObject().findLowerAndUpperBounds(
        evaluateInput, startingGuessForSearch, target, reduceLowerBound, increaseUpperBound, tooFewMaxIterationsToFindBounds));
  }

  private void assertValidLowerAndUpperBoundsCanBeFound(double target) {
    Range<BigDecimal> lowerAndUpperBounds = makeTestObject().findLowerAndUpperBounds(
        evaluateInput, startingGuessForSearch, target, reduceLowerBound, increaseUpperBound, maxIterations);
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
