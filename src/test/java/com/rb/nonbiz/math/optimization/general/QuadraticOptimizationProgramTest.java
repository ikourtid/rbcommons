package com.rb.nonbiz.math.optimization.general;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.general.OptimizationProgramTest.optimizationProgramMatcher;
import static com.rb.nonbiz.math.optimization.general.QuadraticObjectiveFunctionTest.quadraticObjectiveFunctionMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class QuadraticOptimizationProgramTest {

  public static TypeSafeMatcher<QuadraticOptimizationProgram> quadraticOptimizationProgramMatcher(
      QuadraticOptimizationProgram expected) {
    return makeMatcher(expected, actual ->
        optimizationProgramMatcher(expected).matches(actual)
        && quadraticObjectiveFunctionMatcher(expected.getObjectiveToMinimize())
        .matches(actual.getObjectiveToMinimize()));
  }

}
