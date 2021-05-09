package com.rb.nonbiz.math.optimization.general;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.optimization.general.AllRawVariablesAndCoefficients.allRawVariablesAndCoefficients;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.allRawVariablesInOrder;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.GREATER_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.LinearConstraintImpl.equalToScalar;
import static com.rb.nonbiz.math.optimization.general.LinearConstraintImpl.greaterThanScalar;
import static com.rb.nonbiz.math.optimization.general.LinearConstraintImpl.lessThanScalar;
import static com.rb.nonbiz.math.optimization.general.LinearConstraintImpl.linearConstraintImpl;
import static com.rb.nonbiz.math.optimization.general.LinearConstraintTest.linearConstraintMatcher;
import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;

public class LinearConstraintImplTest extends RBTestMatcher<LinearConstraint> {

  @Test
  public void greaterThan_allCoefficientsAreZero_throws() {
    assertIllegalArgumentException( () -> greaterThanScalar(
        "test constraint",
        allRawVariablesAndCoefficients(
            allRawVariablesInOrder(
                rawVariable("first var", 0),
                rawVariable("second var", 1)), new double[] { 0.0, 0.0 }
        ),
        33.3));
  }

  @Test
  public void equality_allCoefficientsAreZero_throws() {
    assertIllegalArgumentException( () -> equalToScalar(
        "test constraint",
        allRawVariablesAndCoefficients(
            allRawVariablesInOrder(
                rawVariable("first var", 0),
                rawVariable("second var", 1)), new double[] { 0.0, 0.0 }
        ),
        33.3));
  }

  @Test
  public void lessThan_allCoefficientsAreZero_throws() {
    assertIllegalArgumentException( () -> lessThanScalar(
        "test constraint",
        allRawVariablesAndCoefficients(
            allRawVariablesInOrder(
                rawVariable("first var", 0),
                rawVariable("second var", 1)), new double[] { 0.0, 0.0 }
        ),
        33.3));
  }

  @Test
  public void nonVerbose_printsClearly() {
    AllRawVariablesInOrder allRawVariablesInOrder = allRawVariablesInOrder(
        rawVariable("first var", 0),
        rawVariable("second var", 1),
        rawVariable("third var", 2),
        rawVariable("fourth var", 3),
        rawVariable("fifth var", 4));
    AllRawVariablesAndCoefficients coefficients = allRawVariablesAndCoefficients(
        allRawVariablesInOrder, new double[] { 1.0, -1.0, 0, 44.4, -55.5 });
    assertEquals(
        "constraint XYZ :   33.3000000000 <  + x0 - x1 +   44.4000000000*x3 -   55.5000000000*x4",
        greaterThanScalar("constraint XYZ", coefficients, 33.3).toString());
    assertEquals(
        "constraint XYZ :   33.3000000000 ==  + x0 - x1 +   44.4000000000*x3 -   55.5000000000*x4",
        equalToScalar("constraint XYZ", coefficients, 33.3).toString());
    assertEquals(
        "constraint XYZ :   33.3000000000 >  + x0 - x1 +   44.4000000000*x3 -   55.5000000000*x4",
        lessThanScalar("constraint XYZ", coefficients, 33.3).toString());
  }

  @Test
  public void verbose_printsClearly() {
    AllRawVariablesInOrder allRawVariablesInOrder = allRawVariablesInOrder(
        rawVariable("first var", 0),
        rawVariable("second var", 1),
        rawVariable("third var", 2),
        rawVariable("fourth var", 3),
        rawVariable("fifth var", 4));
    AllRawVariablesAndCoefficients coefficients = allRawVariablesAndCoefficients(
        allRawVariablesInOrder, new double[] { 1.0, -1.0, 0, 44.4, -55.5 });
    assertEquals(
        "constraint XYZ :   33.3000000000 <  + [x0:first var] - [x1:second var] +   44.4000000000*[x3:fourth var] -   55.5000000000*[x4:fifth var]",
        greaterThanScalar("constraint XYZ", coefficients, 33.3).toVerboseString());
    assertEquals(
        "constraint XYZ :   33.3000000000 ==  + [x0:first var] - [x1:second var] +   44.4000000000*[x3:fourth var] -   55.5000000000*[x4:fifth var]",
        equalToScalar("constraint XYZ", coefficients, 33.3).toVerboseString());
    assertEquals(
        "constraint XYZ :   33.3000000000 >  + [x0:first var] - [x1:second var] +   44.4000000000*[x3:fourth var] -   55.5000000000*[x4:fifth var]",
        lessThanScalar("constraint XYZ", coefficients, 33.3).toVerboseString());
  }

  @Override
  public LinearConstraint makeTrivialObject() {
    return equalToScalar(
        "",
        // we can't use a 0 as a coefficient in the constraint; otherwise the constraint is 0 * a == 0
        // which is disallowed.
        allRawVariablesAndCoefficients(allRawVariablesInOrder(rawVariable("a", 0)), new double[] { 12.34 }),
        0.0);
  }

  @Override
  public LinearConstraint makeNontrivialObject() {
    return linearConstraintImpl(
        "abc", new AllRawVariablesAndCoefficientsTest().makeNontrivialObject(), GREATER_THAN_SCALAR, 7.7);
  }

  @Override
  public LinearConstraint makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return linearConstraintImpl(
        "abc", new AllRawVariablesAndCoefficientsTest().makeMatchingNontrivialObject(), GREATER_THAN_SCALAR, 7.7 + e);
  }

  @Override
  protected boolean willMatch(LinearConstraint expected, LinearConstraint actual) {
    return linearConstraintMatcher(expected).matches(actual);
  }

  // The matcher lives in LinearConstraintTest.java

}