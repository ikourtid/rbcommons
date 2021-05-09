package com.rb.nonbiz.math.optimization.general;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressionTest.highLevelVarExpressionMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class LinearSubObjectiveFunctionTest {

  public static <T extends LinearSubObjectiveFunction> TypeSafeMatcher<T> linearSubObjectiveFunctionMatcher(T expected) {
    return makeMatcher(expected,
        match(v -> v.getHighLevelVarExpression(), f -> highLevelVarExpressionMatcher(f)));
  }

}
