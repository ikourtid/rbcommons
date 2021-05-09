package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.biz.investing.strategy.optbased.rebal.lp.EigenSubObjectiveInstructionsForCoefficients.EigenSubObjectiveMinimizesStandardDeviation.eigenSubObjectiveMinimizesStandardDeviation;
import static com.rb.biz.investing.strategy.optbased.rebal.lp.EigenSubObjectiveInstructionsForCoefficients.EigenSubObjectiveMinimizesVariance.eigenSubObjectiveMinimizesVariance;
import static com.rb.biz.investing.strategy.optbased.rebal.lp.EigenSubObjectiveInstructionsForCoefficientsTest.eigenSubObjectiveInstructionsForCoefficientsMatcher;
import static com.rb.nonbiz.math.eigen.DecreasingPositiveDoublesTest.decreasingPositiveDoublesMatcher;
import static com.rb.nonbiz.math.eigen.EigenDistanceCalculationInstructions.eigenDistanceCalculationInstructions;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class EigenDistanceCalculationInstructionsTest extends RBTestMatcher<EigenDistanceCalculationInstructions> {

  @Override
  public EigenDistanceCalculationInstructions makeTrivialObject() {
    return eigenDistanceCalculationInstructions(
        eigenSubObjectiveMinimizesVariance(), new DecreasingPositiveDoublesTest().makeTrivialObject());
  }

  @Override
  public EigenDistanceCalculationInstructions makeNontrivialObject() {
    return eigenDistanceCalculationInstructions(
        eigenSubObjectiveMinimizesStandardDeviation(), new DecreasingPositiveDoublesTest().makeNontrivialObject());
  }

  @Override
  public EigenDistanceCalculationInstructions makeMatchingNontrivialObject() {
    return eigenDistanceCalculationInstructions(
        eigenSubObjectiveMinimizesStandardDeviation(), new DecreasingPositiveDoublesTest().makeMatchingNontrivialObject());
  }

  @Override
  protected boolean willMatch(EigenDistanceCalculationInstructions expected,
                              EigenDistanceCalculationInstructions actual) {
    return eigenDistanceCalculationInstructionsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<EigenDistanceCalculationInstructions> eigenDistanceCalculationInstructionsMatcher(
      EigenDistanceCalculationInstructions expected) {
    return makeMatcher(expected,
        match(v -> v.getEigenSubObjectiveInstructionsForCoefficients(), f -> eigenSubObjectiveInstructionsForCoefficientsMatcher(f)),
        match(v -> v.getDecreasingEigenvalues(),                        f -> decreasingPositiveDoublesMatcher(f)));
  }

}
