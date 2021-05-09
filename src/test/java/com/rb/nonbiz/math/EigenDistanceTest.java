package com.rb.nonbiz.math;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.investing.strategy.optbased.rebal.lp.EigenSubObjectiveInstructionsForCoefficientsTest.eigenSubObjectiveInstructionsForCoefficientsMatcher;
import static com.rb.nonbiz.math.EigenDistance.eigenDistanceInStandardDeviationSpace;
import static com.rb.nonbiz.math.EigenDistance.eigenDistanceInVarianceSpace;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class EigenDistanceTest extends RBTestMatcher<EigenDistance> {

  public static EigenDistance zeroEigenDistance() {
    return EigenDistance.eigenDistanceInVarianceSpace(0, 0);
  }

  @Test
  public void euclideanDistanceLessThanSumOfAbsDiffs_throws() {
    assertIllegalArgumentException( () -> eigenDistanceInVarianceSpace(1.1 - 1e-9, 1.1));
    assertIllegalArgumentException( () -> eigenDistanceInVarianceSpace(0, 1e-9));
  }

  @Test
  public void negativeValues_throws() {
    assertIllegalArgumentException( () -> eigenDistanceInVarianceSpace(-1.1 - 1e-9, -1.1));
    assertIllegalArgumentException( () -> eigenDistanceInVarianceSpace(-1.1, -1.1));
    assertIllegalArgumentException( () -> eigenDistanceInVarianceSpace(-1.1 + 1e-9, -1.1));
  }

  @Override
  public EigenDistance makeTrivialObject() {
    return eigenDistanceInVarianceSpace(0, 0);
  }

  @Override
  public EigenDistance makeNontrivialObject() {
    return eigenDistanceInStandardDeviationSpace(3.3, 1.1);
  }

  @Override
  public EigenDistance makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return eigenDistanceInStandardDeviationSpace(3.3 + e, 1.1 + e);
  }

  @Override
  protected boolean willMatch(EigenDistance expected, EigenDistance actual) {
    return eigenDistanceMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<EigenDistance> eigenDistanceMatcher(EigenDistance expected) {
    return makeMatcher(expected,
        match(
            v -> v.getEigenSubObjectiveInstructionsForCoefficients(),
            f -> eigenSubObjectiveInstructionsForCoefficientsMatcher(f)),
        matchUsingDoubleAlmostEquals(v -> v.getUsingL1Norm(), 1e-8),
        matchUsingDoubleAlmostEquals(v -> v.getUsingL2Norm(), 1e-8));
  }

}
