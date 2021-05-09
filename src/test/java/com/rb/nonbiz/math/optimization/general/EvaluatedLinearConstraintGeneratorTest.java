package com.rb.nonbiz.math.optimization.general;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesAndCoefficients.allRawVariablesAndCoefficients;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues.allRawVariablesAndOptimalValues;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.allRawVariablesInOrder;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.EQUAL_TO_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.GREATER_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.LESS_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.EvaluatedLinearConstraint.evaluatedLinearConstraint;
import static com.rb.nonbiz.math.optimization.general.EvaluatedLinearConstraintTest.evaluatedLinearConstraintMatcher;
import static com.rb.nonbiz.math.optimization.general.LinearConstraintImpl.linearConstraintImpl;
import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static org.hamcrest.MatcherAssert.assertThat;

public class EvaluatedLinearConstraintGeneratorTest extends RBTest<EvaluatedLinearConstraintGenerator> {

  @Test
  public void generalCase() {
    rbSetOf(LESS_THAN_SCALAR, EQUAL_TO_SCALAR, GREATER_THAN_SCALAR)
        .forEach(constraintDirection -> {
          LinearConstraint linearConstraint = linearConstraintImpl(
              "5a + 6c >=< 7",
              allRawVariablesAndCoefficients(
                  allRawVariablesInOrder(rawVariable("a", 0), rawVariable("b", 1), rawVariable("c", 2)),
                  new double[] { 5, 0, 6 }),
              constraintDirection,
              7);
          assertThat(
              makeTestObject().generate(
                  linearConstraint,
                  allRawVariablesAndOptimalValues(
                      allRawVariablesInOrder(rawVariable("a", 0), rawVariable("b", 1), rawVariable("c", 2)),
                      new double[] { 100, DUMMY_DOUBLE, 10 })),
              evaluatedLinearConstraintMatcher(
                  evaluatedLinearConstraint(
                      linearConstraint, doubleExplained(560, 5 * 100 + 6 * 10))));
        });
  }

  @Override
  protected EvaluatedLinearConstraintGenerator makeTestObject() {
    return new EvaluatedLinearConstraintGenerator();
  }

}
