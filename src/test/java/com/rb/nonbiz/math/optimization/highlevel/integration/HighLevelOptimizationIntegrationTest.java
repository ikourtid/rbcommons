package com.rb.nonbiz.math.optimization.highlevel.integration;

import com.google.common.collect.Range;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.math.optimization.highlevel.AbsoluteValueSuperVars;
import org.junit.Test;

import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.EQUAL_TO_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.GREATER_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.SimpleLinearObjectiveFunction.simpleLinearObjectiveFunction;
import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.constantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.constantHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.differenceOf2DisjointHighLevelVars;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.disjointHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.singleVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.sumOfDisjointHighLevelVars;
import static com.rb.nonbiz.math.optimization.highlevel.RBPublicTestOnlySuperVarConstructors.testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_LABEL;

/**
 * Demonstration of the high-level optimization API.
 */
public class HighLevelOptimizationIntegrationTest extends AbstractHighLevelOptimizationIntegrationTest {

  /**
   * minimize {@code abs(x - y) + 400}
   *
   * subject to
   *
   * <pre>
   *   {@code 30 < x < 50}
   *   {@code 80 < y < 110}
   *   {@code x - y > 40}
   *   {@code 2y - 3x > 20}
   * </pre>
   *
   * solution should be x = 50, y = 90
   * solution value should be (3 * 50 - 2 * 85) + 400 = 420
   */
  @Test
  public void sampleOptimization() {
    HighLevelLPBuilder builder = makeBuilder();
    // 1) We normally would use some specific SuperVar instead of a generic CustomSuperVar.
    // 2) It's better to place any constraints that refer only to a single supervar within that supervar.
    // This avoids polluting the space of 'top-level', i.e. cross-supervar constraints.
    RawVariable x = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("x", Range.closed(30.0, 50.0));
    RawVariable y = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("y", Range.closed(80.0, 110.0));
    AbsoluteValueSuperVars absDiff = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        builder.getHighLevelVariablesBuilder(), "abs(x-y)", differenceOf2DisjointHighLevelVars(y, x));
    // This could also have 50 and 90 as a solution, but the way we do the optimization with absolute values,
    // we'll always get the smallest combination of x and y that also gives us the same result.
    // Since y has a lower bound of 80, it can't be lower. But then, since -x + y > 40 <==> 80 - x > 40 <==> 40 > x,
    // and if x gets any lower than x, the abs diff x-y will get bigger.
    assertResults(
        builder
            .addConstraint("-3x + 2y > 20", disjointHighLevelVarExpression(-3, x, 2, y), GREATER_THAN_SCALAR, 20)
            .addConstraint(" -x +  y > 40", disjointHighLevelVarExpression(-1, x, 1, y), GREATER_THAN_SCALAR, 40)
            .setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(absDiff.getAbsoluteValue()))),
        pair(x, 40.0),
        pair(y, 80.0),
        pair(absDiff.getAbsoluteValue(), 40.0));
  }

  @Test
  public void unconstrainedAllocation_getsToTarget() {
    HighLevelLPBuilder builder = makeBuilder();
    HighLevelVariablesBuilder varBuilder = builder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
    RawVariable z = varBuilder.addConstrainedRawVariable("z", Range.closed(0.0, 1.0));
    SuperVar xTarget = testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, constantHighLevelVarExpression(constantTerm(0.5)));
    SuperVar yTarget = testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, constantHighLevelVarExpression(constantTerm(0.4)));
    SuperVar zTarget = testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, constantHighLevelVarExpression(constantTerm(0.1)));
    AbsoluteValueSuperVars xAbsMisallocation = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        varBuilder, "x misallocation", differenceOf2DisjointHighLevelVars(x, xTarget));
    AbsoluteValueSuperVars yAbsMisallocation = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        varBuilder, "y misallocation", differenceOf2DisjointHighLevelVars(y, yTarget));
    AbsoluteValueSuperVars zAbsMisallocation = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        varBuilder, "z misallocation", differenceOf2DisjointHighLevelVars(z, zTarget));
    assertResults(
        builder
            .addConstraint("Portfolio fractions must add to 1", sumOfDisjointHighLevelVars(x, y, z), EQUAL_TO_SCALAR, 1.0)
            .setObjectiveFunction(simpleLinearObjectiveFunction(disjointHighLevelVarExpression(
                    1 / 0.5, xAbsMisallocation.getAbsoluteValue(),
                    1 / 0.4, yAbsMisallocation.getAbsoluteValue(),
                    1 / 0.1, zAbsMisallocation.getAbsoluteValue()))),
        pair(x, 0.5),
        pair(y, 0.4),
        pair(z, 0.1),
        pair(xTarget, 0.5),
        pair(yTarget, 0.4),
        pair(zTarget, 0.1),
        pair(xAbsMisallocation.getAbsoluteValue(), 0.0),
        pair(yAbsMisallocation.getAbsoluteValue(), 0.0),
        pair(zAbsMisallocation.getAbsoluteValue(), 0.0));
  }

  @Test
  public void constrainedAllocation_getsAsCloseAsPossibleToTarget() {
    HighLevelLPBuilder builder = makeBuilder();
    HighLevelVariablesBuilder varBuilder = builder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 0.3));
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
    RawVariable z = varBuilder.addConstrainedRawVariable("z", Range.closed(0.0, 1.0));
    SuperVar xTarget = testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, constantHighLevelVarExpression(constantTerm(0.5)));
    SuperVar yTarget = testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, constantHighLevelVarExpression(constantTerm(0.4)));
    SuperVar zTarget = testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, constantHighLevelVarExpression(constantTerm(0.1)));
    AbsoluteValueSuperVars xAbsMisallocation = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        varBuilder, "x misallocation", differenceOf2DisjointHighLevelVars(x, xTarget));
    AbsoluteValueSuperVars yAbsMisallocation = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        varBuilder, "y misallocation", differenceOf2DisjointHighLevelVars(y, yTarget));
    AbsoluteValueSuperVars zAbsMisallocation = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        varBuilder, "z misallocation", differenceOf2DisjointHighLevelVars(z, zTarget));
    // x must be at 50% but is constrained to be < 30%. Therefore, that extra 20% should get distributed pro rata to
    // y (0.16) and z (0.04). However, I think the only way to achieve this is to use quadratic programming.
    // Since we use LP, the optimizer will allocate that extra 20% to y, which has the lowest coefficient,
    // i.e. it's more acceptable (relative to z) if y gets misallocated by the same amount.
    assertResults(
        builder
            .addConstraint("Portfolio fractions must add to 1", sumOfDisjointHighLevelVars(x, y, z), EQUAL_TO_SCALAR, 1.0)
            .setObjectiveFunction(simpleLinearObjectiveFunction(disjointHighLevelVarExpression(
                    1 / 0.5, xAbsMisallocation.getAbsoluteValue(),
                    1 / 0.4, yAbsMisallocation.getAbsoluteValue(),
                    1 / 0.1, zAbsMisallocation.getAbsoluteValue()))),
        pair(x, 0.3),
        pair(y, 0.4 + 0.2),
        pair(z, 0.1),
        pair(xTarget, 0.5),
        pair(yTarget, 0.4),
        pair(zTarget, 0.1),
        pair(xAbsMisallocation.getAbsoluteValue(), 0.2),
        pair(yAbsMisallocation.getAbsoluteValue(), 0.2),
        pair(zAbsMisallocation.getAbsoluteValue(), 0.0));
  }

  @Test
  public void veryConstrainedAllocation_getsAsCloseAsPossibleToTarget() {
    HighLevelLPBuilder builder = makeBuilder();
    HighLevelVariablesBuilder varBuilder = builder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 0.3));
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 0.55));
    RawVariable z = varBuilder.addConstrainedRawVariable("z", Range.closed(0.0, 1.0));
    SuperVar xTarget = testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, constantHighLevelVarExpression(constantTerm(0.5)));
    SuperVar yTarget = testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, constantHighLevelVarExpression(constantTerm(0.4)));
    SuperVar zTarget = testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, constantHighLevelVarExpression(constantTerm(0.1)));
    AbsoluteValueSuperVars xAbsMisallocation = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        varBuilder, "x misallocation", differenceOf2DisjointHighLevelVars(x, xTarget));
    AbsoluteValueSuperVars yAbsMisallocation = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        varBuilder, "y misallocation", differenceOf2DisjointHighLevelVars(y, yTarget));
    AbsoluteValueSuperVars zAbsMisallocation = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        varBuilder, "z misallocation", differenceOf2DisjointHighLevelVars(z, zTarget));
    // x must be at 50% but is constrained to be < 30%. Therefore, that extra 20% should get distributed pro rata to
    // y (0.16) and z (0.04). However, I think the only way to achieve this is to use quadratic programming.
    // Since we use LP, the optimizer would normally try to allocate that extra 20% to y, which has the lowest coefficient,
    // i.e. it's more acceptable (relative to z) if y gets misallocated by the same amount. However, y can't be more than
    // 55%, so it will get to its max (55%) and the other 5% will go to z.
    assertResults(
        builder
            .addConstraint("Portfolio fractions must add to 1", sumOfDisjointHighLevelVars(x, y, z), EQUAL_TO_SCALAR, 1.0)
            .setObjectiveFunction(simpleLinearObjectiveFunction(disjointHighLevelVarExpression(
                    1 / 0.5, xAbsMisallocation.getAbsoluteValue(),
                    1 / 0.4, yAbsMisallocation.getAbsoluteValue(),
                    1 / 0.1, zAbsMisallocation.getAbsoluteValue()))),
        pair(x, doubleExplained(0.3, 0.5 - 0.15 - 0.05)),
        pair(y, doubleExplained(0.55, 0.4 + 0.15)),
        pair(z, doubleExplained(0.15, 0.1 + 0.05)),
        pair(xTarget, 0.5),
        pair(yTarget, 0.4),
        pair(zTarget, 0.1),
        pair(xAbsMisallocation.getAbsoluteValue(), 0.2),
        pair(yAbsMisallocation.getAbsoluteValue(), 0.15),
        pair(zAbsMisallocation.getAbsoluteValue(), 0.05));
  }

}
