package com.rb.nonbiz.math.optimization.general;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.optimization.general.AllRawVariablesAndCoefficients.allRawVariablesAndCoefficients;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.allRawVariablesInOrder;
import static com.rb.nonbiz.math.optimization.general.EvaluatedLinearConstraint.evaluatedLinearConstraint;
import static com.rb.nonbiz.math.optimization.general.LinearConstraintImpl.equalToScalar;
import static com.rb.nonbiz.math.optimization.general.LinearConstraintImpl.lessThanScalar;
import static com.rb.nonbiz.math.optimization.general.LinearConstraintTest.linearConstraintMatcher;
import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class EvaluatedLinearConstraintTest extends RBTestMatcher<EvaluatedLinearConstraint> {

  @Test
  public void equalityConstraintIsNeverBinding() {
    assertFalse(
        evaluatedLinearConstraint(
            equalToScalar(
                "5 * x == 88",
                allRawVariablesAndCoefficients(allRawVariablesInOrder(rawVariable("x", 0)), new double[] { 5 }),
                88),
            88)
            .isBindingConstraint(1e-8));
  }

  @Test
  public void testIsBindingConstraint_absDistanceFromBound() {
    EvaluatedLinearConstraint evaluatedLinearConstraint = evaluatedLinearConstraint(
        lessThanScalar(
            "5 * x < 88",
            allRawVariablesAndCoefficients(allRawVariablesInOrder(rawVariable("x", 0)), new double[] { 5 }),
            88),
        77);
    assertEquals(
        doubleExplained(11, 88 - 77),
        evaluatedLinearConstraint.absDistanceFromBound(),
        1e-8);
    assertFalse(
        "Since 5*x was evaluated to 77 (assumption) which is 11 away from 88, this constraint is not binding for an epsilon of just below 11",
        evaluatedLinearConstraint.isBindingConstraint(11 - 1e-9));
    assertTrue(
        "Since 5*x was evaluated to 77 (assumption) which is 11 away from 88, this constraint is binding for an epsilon of just above 11",
        evaluatedLinearConstraint.isBindingConstraint(11 + 1e-9));
  }

  @Override
  public EvaluatedLinearConstraint makeTrivialObject() {
    return evaluatedLinearConstraint(new LinearConstraintImplTest().makeTrivialObject(), 0.0);
  }

  @Override
  public EvaluatedLinearConstraint makeNontrivialObject() {
    return evaluatedLinearConstraint(new LinearConstraintImplTest().makeNontrivialObject(), -12.34);
  }

  @Override
  public EvaluatedLinearConstraint makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return evaluatedLinearConstraint(new LinearConstraintImplTest().makeMatchingNontrivialObject(), -12.34 + e);
  }

  @Override
  protected boolean willMatch(EvaluatedLinearConstraint expected, EvaluatedLinearConstraint actual) {
    return evaluatedLinearConstraintMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<EvaluatedLinearConstraint> evaluatedLinearConstraintMatcher(
      EvaluatedLinearConstraint expected) {
    return makeMatcher(expected,
        match(v -> v.getLinearConstraint(), f -> linearConstraintMatcher(f)),
        matchUsingDoubleAlmostEquals(v -> v.getValueOfTermsOnly(), 1e-8));
  }

}
