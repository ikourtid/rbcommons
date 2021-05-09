package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.List;

import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.general.WeightedRawVariables.singletonWeightedRawVariables;
import static com.rb.nonbiz.math.optimization.general.WeightedRawVariables.weightedRawVariables;
import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.constantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.FlattenedRawVarExpression.constantFlattenedRawVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.FlattenedRawVarExpression.flattenedRawVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.FlattenedRawVarExpressionTest.flattenedRawVarExpressionMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.constantHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.disjointHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.disjointHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.highLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.singleVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarWithWeight.highLevelVarWithWeight;
import static com.rb.nonbiz.math.optimization.highlevel.RBPublicTestOnlySuperVarConstructors.testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static org.hamcrest.MatcherAssert.assertThat;

public class HighLevelVarExpressionFlattenerTest extends RBTest<HighLevelVarExpressionFlattener> {

  @Test
  public void simplestCase_constant_returnsFlattenedExpressionWithConstantOnly() {
    assertThat(
        makeTestObject().flatten(constantHighLevelVarExpression(constantTerm(1.1))),
        flattenedRawVarExpressionMatcher(constantFlattenedRawVarExpression(1.1)));
  }

  @Test
  public void simpleVarPlusConstant() {
    RawVariable x = rawVariable("x", 0);
    assertThat(
        makeTestObject().flatten(
            highLevelVarExpression(7.7, x, 1.1)),
        flattenedRawVarExpressionMatcher(
            flattenedRawVarExpression(singletonWeightedRawVariables(x, 7.7), constantTerm(1.1))));
  }

  @Test
  public void simpleAXplusB() {
    RawVariable x = rawVariable("x", 0);
    assertThat(
        makeTestObject().flatten(disjointHighLevelVarExpression(
            ImmutableList.of(
                highLevelVarWithWeight(x, 10)),
            constantTerm(6))),
        flattenedRawVarExpressionMatcher(flattenedRawVarExpression(
            singletonWeightedRawVariables(x, 10.0),
            constantTerm(6))));
  }

  @Test
  public void generalCase() {
    RawVariable x = rawVariable("x", 0);
    RawVariable y = rawVariable("y", 1);
    RawVariable z = rawVariable("z", 2);
    assertThat(
        "[10 * x] + [20 * (2y + 3z + 5)] + 6 = 10x + 40y +60z + 106",
        makeTestObject().flatten(disjointHighLevelVarExpression(
            ImmutableList.of(
                highLevelVarWithWeight(x, 10),
                highLevelVarWithWeight(
                    testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(
                        DUMMY_LABEL,
                        disjointHighLevelVarExpression(2, y, 3, z, 5)),
                    20)),
            constantTerm(6))),
        flattenedRawVarExpressionMatcher(flattenedRawVarExpression(
            weightedRawVariables(doubleMap(rbMapOf(
                x, 10.0,
                y, doubleExplained(40.0, 2 * 20),
                z, doubleExplained(60.0, 3 * 20)))),
            constantTerm(doubleExplained(106, 5 * 20 + 6)))));
  }

  @Test
  public void flattenExpressionOfDisjointSuperVars_hasDepthOf2() {
    RawVariable x = rawVariable("x", 0);
    assertThat(
        "2 * (3 * (4 * x + 1)) = 24x + 6",
        makeTestObject().flatten(
            singleVarExpression(2, testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(
                DUMMY_LABEL,
                singleVarExpression(3, testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(
                    DUMMY_LABEL,
                    highLevelVarExpression(4, x, 1)))))),
        flattenedRawVarExpressionMatcher(flattenedRawVarExpression(
            singletonWeightedRawVariables(x, doubleExplained(24.0, 2 * 3 * 4)),
            constantTerm(doubleExplained(6, 2 * 3)))));
  }

  @Test
  public void hasNonDisjointExpressions_failsIfAsksForDisjoint_succeedsOtherwise() {
    RawVariable x = rawVariable("x", 0);
    RawVariable y = rawVariable("y", 1);
    List<HighLevelVarWithWeight> highLevelVarsWithWeights = ImmutableList.of(
        highLevelVarWithWeight(x, 10),
        highLevelVarWithWeight(
            testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(
                DUMMY_LABEL,
                disjointHighLevelVarExpression(2, x, 3, y, 5)),
            20));
    // This fails in the static constructor #disjointHighLevelVarExpression;
    // we don't even get to call makeTestObject().flatten to get to an exception.
    assertIllegalArgumentException( () ->
        disjointHighLevelVarExpression(highLevelVarsWithWeights, constantTerm(DUMMY_DOUBLE)));
    assertThat(
        "[10 * x] + [20 * (2x + 3y + 5)] + 6 = 50x + 60y + 6",
        makeTestObject().flatten(HighLevelVarExpression.highLevelVarExpression(
            highLevelVarsWithWeights,
            constantTerm(6))),
        flattenedRawVarExpressionMatcher(flattenedRawVarExpression(
            weightedRawVariables(doubleMap(rbMapOf(
                x, doubleExplained(50, 10 + 2 * 20),
                y, doubleExplained(60, 3 * 20)))),
            constantTerm(doubleExplained(106, 5 * 20 + 6)))));
  }

  @Override
  protected HighLevelVarExpressionFlattener makeTestObject() {
    return new HighLevelVarExpressionFlattener();
  }

}
