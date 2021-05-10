package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValuesTest.linearApproximationVarRangesAndValuesForQuadratic;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;

public class LinearApproximationEvaluatorTest extends RBTest<LinearApproximationEvaluator> {

  // This is 100x what we'd normally use in prod, but it makes the tests easier to follow.
  // Also, in practice, we don't often use a plain x^2, but instead we use the 'water slide function'
  // (as of May 2018). But squares are easier to understand in the context of this test.
  private LinearApproximationVarRangesAndValues SQUARE_APPROXIMATION =
      linearApproximationVarRangesAndValuesForQuadratic(0.0, 1.0, 3.0, 7.0, 15.0, 31.0, 50.0);

  @Test
  public void outsideRange_throws() {
    assertIllegalArgumentException( () -> makeTestObject().evaluate(-1e-8, SQUARE_APPROXIMATION));
    assertIllegalArgumentException( () -> makeTestObject().evaluate(50 + 1e-8, SQUARE_APPROXIMATION));
  }

  @Test
  public void lowerEndpointOfEntireRange_producesCorrectResult() {
    assertResult(0, doubleExplained(0, 0 * 0));
  }

  @Test
  public void upperEndpointOfEntireRange_producesCorrectResult() {
    assertResult(50, doubleExplained(2_500, 50 * 50));
  }

  @Test
  public void intermediatePoint_justAfterLowerEndpointOfEntireRange_returnsInterpolatedResult() {
    assertResult(0.9, doubleExplained(0.9, 0.9 * (1.0 - 0.0) / 1.0));
  }

  @Test
  public void intermediatePoint_middleEntireRange_returnsInterpolatedResult() {
    double slope = (15 * 15 - 7 * 7) / (15 - 7.0);
    // 115 > 10^2, but this is an approximation which always overestimates the value of x^2, so that's expected.
    assertResult(10, doubleExplained(115, 7 * 7 + (10 - 7) * slope));
  }

  @Test
  public void exactlyOnHingePoints_returnsPerfectSquareWithoutAnyApproximation() {
    for (double hingePoint : SQUARE_APPROXIMATION.getLinearApproximationVarRanges().getHingePoints()) {
      assertResult(hingePoint, hingePoint * hingePoint);
    }
  }

  @Test
  public void allResults() {
    TriFunction<Integer, Integer, Integer, Double> squaresInterpolator = (x, x0, x1) ->
        ( (double) x - x0) * ((double) x1 * x1 - x0 * x0) / ((double) x1 - x0);
    assertResult(0,  0 * 0);
    assertResult(0.9, doubleExplained(0.9, 0.9 * (1.0 - 0.0) / 1.0)); // vs 0.81
    assertResult(1,  1 * 1);
    assertResult(2,  doubleExplained(5, 1 * 1 + squaresInterpolator.apply(2, 1, 3))); // vs 4
    assertResult(3,  3 * 3);
    assertResult(4,  doubleExplained(19, 3 * 3 + squaresInterpolator.apply(4, 3, 7))); // vs 16 = 4^2
    assertResult(5,  doubleExplained(29, 3 * 3 + squaresInterpolator.apply(5, 3, 7))); // vs 25 = 5^2
    assertResult(6,  doubleExplained(39, 3 * 3 + squaresInterpolator.apply(6, 3, 7))); // vs 36 = 6^2
    assertResult(7,  7 * 7);
    assertResult(8,  doubleExplained(71, 7 * 7 + squaresInterpolator.apply(8, 7, 15))); // vs 64 = 8^2
    assertResult(9,  doubleExplained(93, 7 * 7 + squaresInterpolator.apply(9, 7, 15))); // vs 81 = 9^2
    assertResult(10, doubleExplained(115, 7 * 7 + squaresInterpolator.apply(10, 7, 15))); // vs 100 = 10^2
    assertResult(11, doubleExplained(137, 7 * 7 + squaresInterpolator.apply(11, 7, 15))); // vs 121 = 11^2
    assertResult(12, doubleExplained(159, 7 * 7 + squaresInterpolator.apply(12, 7, 15))); // vs 144 = 12^2
    assertResult(13, doubleExplained(181, 7 * 7 + squaresInterpolator.apply(13, 7, 15))); // vs 169 = 13^3
    assertResult(14, doubleExplained(203, 7 * 7 + squaresInterpolator.apply(14, 7, 15))); // vs 196 = 14^2
    assertResult(15, 15 * 15);
    // ...
    assertResult(31, 31 * 31);
    // ...
    assertResult(40, doubleExplained(1_690, 31 * 31 + squaresInterpolator.apply(40, 31, 50))); // vs 1600 = 40^2
    assertResult(49, doubleExplained(2_419, 31 * 31 + squaresInterpolator.apply(49, 31, 50))); // vs 2401 = 49^2
    assertResult(50, 50 * 50);
  }

  private void assertResult(double x, double expectedApproximatedXsquared) {
    assertEquals(
        expectedApproximatedXsquared,
        makeTestObject().evaluate(x, SQUARE_APPROXIMATION),
        1e-8);
  }

  @Override
  protected LinearApproximationEvaluator makeTestObject() {
    return new LinearApproximationEvaluator();
  }

}
