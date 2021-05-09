package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.testutils.RBIntegrationTest;
import org.junit.Test;

import static com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues.allRawVariablesAndOptimalValues;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.allRawVariablesInOrder;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.disjointHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVariablesBuilder.highLevelVariablesBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.RBPublicTestOnlySuperVarConstructors.testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static org.junit.Assert.assertEquals;

public class HighLevelVarEvaluatorTest extends RBIntegrationTest<HighLevelVarEvaluator> {

  private final HighLevelVariablesBuilder highLevelVariablesBuilder = highLevelVariablesBuilder();
  private final RawVariable v0 = highLevelVariablesBuilder.addRawVariable("v0");
  private final RawVariable v1 = highLevelVariablesBuilder.addRawVariable("v1");
  private final RawVariable v2 = highLevelVariablesBuilder.addRawVariable("v2");

  @Test
  public void rawVariable_evaluatesProperly() {
    assertResult(v0, new double[] { -12.34, DUMMY_DOUBLE, DUMMY_DOUBLE }, -12.34);
    assertResult(v0, new double[] {      0, DUMMY_DOUBLE, DUMMY_DOUBLE }, 0);
    assertResult(v0, new double[] {  12.34, DUMMY_DOUBLE, DUMMY_DOUBLE }, 12.34);
  }

  @Test
  public void shallowHighLevelExpression_evaluatesProperly() {
    assertResult(
        testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, disjointHighLevelVarExpression(2, v0, 3, v1, 1_000)),
        new double[] { -10, -20, DUMMY_DOUBLE },
        doubleExplained(920, 2 * -10 + 3 * -20 + 1_000));
  }

  @Test
  public void deepHighLevelExpression_evaluatesProperly() {
    assertResult(
        testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, disjointHighLevelVarExpression(
            2, v0,
            3, testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, disjointHighLevelVarExpression(4, v1, 5, v2, 200)),
            1_000)),
        new double[] { 10, 20, 30 },
        doubleExplained(2_310, 2 * 10 + 3 * (4 * 20 + 5 * 30 + 200) + 1_000));
  }

  private void assertResult(HighLevelVar highLevelVar, double[] values, double expectedResult) {
    assertEquals(
        makeRealObject().evaluateHighLevelVar(highLevelVar,
            allRawVariablesAndOptimalValues(allRawVariablesInOrder(v0, v1, v2), values)),
        expectedResult,
        1e-8);
  }

  @Override
  protected Class<HighLevelVarEvaluator> getClassBeingTested() {
    return HighLevelVarEvaluator.class;
  }

}
