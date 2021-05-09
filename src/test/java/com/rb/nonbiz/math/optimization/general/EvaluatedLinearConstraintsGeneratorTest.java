package com.rb.nonbiz.math.optimization.general;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBIntegrationTest;
import org.junit.Test;

import static com.rb.nonbiz.math.optimization.general.AllRawVariablesAndCoefficients.allRawVariablesAndCoefficients;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues.allRawVariablesAndOptimalValues;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.allRawVariablesInOrder;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.GREATER_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.LESS_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.EvaluatedLinearConstraint.evaluatedLinearConstraint;
import static com.rb.nonbiz.math.optimization.general.EvaluatedLinearConstraints.evaluatedLinearConstraints;
import static com.rb.nonbiz.math.optimization.general.EvaluatedLinearConstraintsTest.evaluatedLinearConstraintsMatcher;
import static com.rb.nonbiz.math.optimization.general.LinearConstraintImpl.linearConstraintImpl;
import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static org.hamcrest.MatcherAssert.assertThat;

public class EvaluatedLinearConstraintsGeneratorTest extends RBIntegrationTest<EvaluatedLinearConstraintsGenerator> {

  @Test
  public void generalCase() {
    AllRawVariablesInOrder vars = allRawVariablesInOrder(
        rawVariable("a", 0),
        rawVariable("b", 1),
        rawVariable("c", 2));
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = allRawVariablesAndOptimalValues(vars, new double[] { 7, 8, 9 });

    LinearConstraint constraint1 = linearConstraintImpl(
        "11a + 111c < 1_111",   allRawVariablesAndCoefficients(vars, new double[] { 11, 0,  111 }), LESS_THAN_SCALAR,     1_111);
    LinearConstraint constraint2 = linearConstraintImpl(
        "22b - 222c > -2_222",  allRawVariablesAndCoefficients(vars, new double[] { 0, 22, -222 }), GREATER_THAN_SCALAR, -2_222);

    assertThat(
        makeRealObject().generate(
            ImmutableList.of(constraint1, constraint2),
            variablesAndOptimalValues),
        evaluatedLinearConstraintsMatcher(
            evaluatedLinearConstraints(
                ImmutableList.of(
                    evaluatedLinearConstraint(constraint1, doubleExplained( 1_076, 11 * 7 + 111 * 9)),
                    evaluatedLinearConstraint(constraint2, doubleExplained(-1_822, 22 * 8 - 222 * 9))))));
  }

  @Override
  protected Class<EvaluatedLinearConstraintsGenerator> getClassBeingTested() {
    return EvaluatedLinearConstraintsGenerator.class;
  }

}
