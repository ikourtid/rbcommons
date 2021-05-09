package com.rb.nonbiz.math.optimization.general;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.math.optimization.general.EqualityConstraint.variableCombinationEqualToScalar;
import static com.rb.nonbiz.math.optimization.general.EqualityConstraint.variableCombinationEqualToZero;
import static com.rb.nonbiz.math.optimization.general.EqualityConstraintTest.equalityConstraintMatcher;
import static com.rb.nonbiz.math.optimization.general.EqualityConstraints.equalityConstraints;
import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.general.WeightedRawVariables.singletonWeightedRawVariables;
import static com.rb.nonbiz.math.optimization.general.WeightedRawVariables.weightedRawVariables;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Collections.emptyList;

public class EqualityConstraintsTest extends RBTestMatcher<EqualityConstraints> {

  @Test
  public void emptyConstraintsListPassed_throws() {
    assertIllegalArgumentException( () -> equalityConstraints(emptyList()));
  }

  @Override
  public EqualityConstraints makeTrivialObject() {
    return equalityConstraints(variableCombinationEqualToZero("", singletonWeightedRawVariables(rawVariable("x", 0), 1.0)));
  }

  @Override
  public EqualityConstraints makeNontrivialObject() {
    return equalityConstraints(
        variableCombinationEqualToScalar(
            "abc",
            weightedRawVariables(doubleMap(rbMapOf(
                rawVariable("a", 10), 100.0,
                rawVariable("b", 20), 200.0,
                rawVariable("c", 30), 300.0))),
            12.34),
        variableCombinationEqualToScalar(
            "xyz",
            weightedRawVariables(doubleMap(rbMapOf(
                rawVariable("x", 40), 400.0,
                rawVariable("y", 50), 500.0,
                rawVariable("z", 60), 600.0))),
            45.67));
  }

  @Override
  public EqualityConstraints makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return equalityConstraints(
        variableCombinationEqualToScalar(
            "abc",
            weightedRawVariables(doubleMap(rbMapOf(
                rawVariable("a", 10), 100.0 + e,
                rawVariable("b", 20), 200.0 + e,
                rawVariable("c", 30), 300.0 + e))),
            12.34 + e),
        variableCombinationEqualToScalar(
            "xyz",
            weightedRawVariables(doubleMap(rbMapOf(
                rawVariable("x", 40), 400.0 + e,
                rawVariable("y", 50), 500.0 + e,
                rawVariable("z", 60), 600.0 + e))),
            45.67 + e));
  }

  @Override
  protected boolean willMatch(EqualityConstraints expected, EqualityConstraints actual) {
    return equalityConstraintsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<EqualityConstraints> equalityConstraintsMatcher(EqualityConstraints expected) {
    return makeMatcher(expected,
        matchList(v -> v.getListOfEqualityConstraints(), f -> equalityConstraintMatcher(f)));
  }

}
