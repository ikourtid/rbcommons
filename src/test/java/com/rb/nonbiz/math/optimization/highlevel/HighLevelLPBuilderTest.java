package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.Range;
import com.rb.nonbiz.math.optimization.general.FlattenedRawVarExpressionMerger;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.math.optimization.general.ScalingInstructionsForImprovedLpAccuracy;
import com.rb.nonbiz.math.optimization.general.ScalingInstructionsForImprovedLpAccuracyTest;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Optional;

import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.math.optimization.general.AllArtificialObjectiveFunctionTerms.allArtificialObjectiveFunctionTerms;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.allRawVariablesInOrder;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.EQUAL_TO_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.GREATER_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.LESS_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.InitialFeasiblePointTest.initialFeasiblePointMatcher;
import static com.rb.nonbiz.math.optimization.general.InitialInfeasiblePointTest.initialInfeasiblePointMatcher;
import static com.rb.nonbiz.math.optimization.general.LPBuilder.lpBuilder;
import static com.rb.nonbiz.math.optimization.general.LinearObjectiveFunctionTest.linearObjectiveFunctionMatcher;
import static com.rb.nonbiz.math.optimization.general.LinearObjectiveFunctionWithArtificialTerms.linearObjectiveFunctionWithArtificialTerms;
import static com.rb.nonbiz.math.optimization.general.LinearOptimizationProgramTest.linearOptimizationProgramMatcher;
import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.general.SimpleLinearObjectiveFunction.simpleLinearObjectiveFunction;
import static com.rb.nonbiz.math.optimization.general.SingleSuperVarArtificialObjectiveFunctionTerms.singleSuperVarArtificialObjectiveFunctionTerms;
import static com.rb.nonbiz.math.optimization.general.WeightedRawVariables.weightedRawVariables;
import static com.rb.nonbiz.math.optimization.highlevel.DisjointVariablesSafeguard.CHECK_FOR_DISJOINT_RAW_VARIABLES;
import static com.rb.nonbiz.math.optimization.highlevel.FlattenedRawVarExpression.flattenedRawVarExpressionWithoutConstantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar.generalSuperVar;
import static com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar.generalSuperVarWithoutArtificialTerms;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelLPBuilder.highLevelLPBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarConstraint.minMaxConstraints;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarConstraintTest.highLevelVarConstraintMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.disjointHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.singleVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVariablesBuilderTest.highLevelVariablesBuilderMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.RBEitherMatchers.eitherMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertNullPointerException;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_LABEL;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_STRING;

public class HighLevelLPBuilderTest extends RBTestMatcher<HighLevelLPBuilder> {

  @Test
  public void noVariables_cannotBuild() {
    assertIllegalArgumentException( () -> makeRealHighLevelLPBuilder().build());
  }

  @Test
  public void noObjectiveFunction_cannotBuild() {
    HighLevelLPBuilder builder = makeRealHighLevelLPBuilder();
    builder.getHighLevelVariablesBuilder().addRawVariable(DUMMY_STRING);
    assertNullPointerException( () -> builder.build());
  }

  @Test
  public void happyPath() {
    ScalingInstructionsForImprovedLpAccuracy dummyScalingInstructions =
        new ScalingInstructionsForImprovedLpAccuracyTest().makeDummyObject();
    HighLevelLPBuilder highLevelLPBuilder = makeRealHighLevelLPBuilder()
        .resetScalingInstructionsForImprovedLpAccuracy(dummyScalingInstructions)
        .withHumanReadableLabel(DUMMY_LABEL);
    HighLevelVariablesBuilder varBuilder = highLevelLPBuilder.getHighLevelVariablesBuilder();

    RawVariable x = varBuilder.addRawVariable("x");
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.atLeast(4.4));
    RawVariable z = varBuilder.addConstrainedRawVariable("z", Range.closed(-5.5, 6.6));
    RawVariable v1 = varBuilder.addRawVariable("v1");
    RawVariable v2 = varBuilder.addRawVariable("v2");
    SuperVar w = varBuilder.addSuperVar(generalSuperVar(
        DUMMY_LABEL,
        disjointHighLevelVarExpression(100, x, 200, y, 300),
        minMaxConstraints(-400, 500, CHECK_FOR_DISJOINT_RAW_VARIABLES),
        singleSuperVarArtificialObjectiveFunctionTerms(flattenedRawVarExpressionWithoutConstantTerm(
            weightedRawVariables(doubleMap(rbMapOf(
                v1, 1.1,
                v2, 2.2)))))));
    assertThat(
        highLevelLPBuilder
            .addConstraint("constr_gt", disjointHighLevelVarExpression(12, x, 13, y), GREATER_THAN_SCALAR, 14)
            .addConstraint("constr_eq", disjointHighLevelVarExpression(15, y, 16, z), EQUAL_TO_SCALAR, 17)
            .addConstraint("constr_lt", disjointHighLevelVarExpression(18, z, 19, w), LESS_THAN_SCALAR, 20)
            .setObjectiveFunction(simpleLinearObjectiveFunction(disjointHighLevelVarExpression(40, x, 50, y, 60)))
            .build(),
        linearOptimizationProgramMatcher(
            lpBuilder(allRawVariablesInOrder(x, y, z, v1, v2))
                .withScalingInstructionsForImprovedLpAccuracy(dummyScalingInstructions)
                // minimize 30 * (40x + 50y + 60) + 70 * (80y + 90z + 100) + 110*(120z + 130w + 140)
                // = (1200x + 1500y + 1800) + (5600y + 6300z + 7000) + (13200z + 110*130(100x + 200y + 300) + 15400)
                // = (1200 + 130*110*100)x + (1500 + 5600 + 110*130*200)y + (6300 + 13200)z + (1800 + 7000 + 110*130*300 + 15400)
                // = 1431200x + 2867100y + 19500z + 4314200
                .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithArtificialTerms(
                    simpleLinearObjectiveFunction(disjointHighLevelVarExpression(40, x, 50, y, 60)),
                    allArtificialObjectiveFunctionTerms(flattenedRawVarExpressionWithoutConstantTerm(
                        weightedRawVariables(doubleMap(rbMapOf(
                            v1, 1.1,
                            v2, 2.2)))))))
                .withVariableRange(y, Range.atLeast(4.4))
                .withVariableRange(z, Range.closed(-5.5, 6.6))
                // -400 < 100x + 200y + 300 < 500 <==> -700 < 100x + 200y < 200
                .withVariableCombinationGreaterThanScalar(DUMMY_STRING, weightedRawVariables(doubleMap(rbMapOf(x, 100.0, y, 200.0))), -700)
                .withVariableCombinationLessThanScalar(DUMMY_STRING, weightedRawVariables(doubleMap(rbMapOf(x, 100.0, y, 200.0))), 200)
                .withVariableCombinationGreaterThanScalar("constr_gt", weightedRawVariables(doubleMap(rbMapOf(x, 12.0, y, 13.0))), 14)
                .withVariableCombinationEqualToScalar("constr_eq", weightedRawVariables(doubleMap(rbMapOf(y, 15.0, z, 16.0))), 17)
                // 18z + 19w < 20 <==> 18z + 19*(100x + 200y + 300) < 20 <==> 1900x + 3800y + 18z < -5680
                .withVariableCombinationLessThanScalar("constr_lt", weightedRawVariables(doubleMap(rbMapOf(x, 1_900.0, y, 3_800.0, z, 18.0))), -5_680)
                .build()));
  }

  // This test is a bit unusual, because the builder is like a data class (it has state)
  // but it also needs a verb class object.
  public static HighLevelLPBuilder makeRealHighLevelLPBuilder() {
    return highLevelLPBuilder(new HighLevelVarExpressionFlattener(), new FlattenedRawVarExpressionMerger());
  }

  @Override
  public HighLevelLPBuilder makeTrivialObject() {
    return makeRealHighLevelLPBuilder().setObjectiveFunction(
        simpleLinearObjectiveFunction(singleVarExpression(rawVariable("x", 0))));
  }

  @Override
  public HighLevelLPBuilder makeNontrivialObject() {
    HighLevelLPBuilder highLevelLPBuilder = makeRealHighLevelLPBuilder();
    HighLevelVariablesBuilder varBuilder = highLevelLPBuilder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addRawVariable("x");
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.atLeast(4.4));
    RawVariable z = varBuilder.addConstrainedRawVariable("z", Range.closed(-5.5, 6.6));
    SuperVar w = highLevelLPBuilder.getHighLevelVariablesBuilder().addSuperVar(generalSuperVarWithoutArtificialTerms(
        DUMMY_LABEL,
        disjointHighLevelVarExpression(100, x, 200, y, 300),
        minMaxConstraints(-400, 500, CHECK_FOR_DISJOINT_RAW_VARIABLES)));
    return highLevelLPBuilder
        .addConstraint("constr_gt", disjointHighLevelVarExpression(12, x, 13, y), GREATER_THAN_SCALAR, 14)
        .addConstraint("constr_eq", disjointHighLevelVarExpression(15, y, 16, z), EQUAL_TO_SCALAR, 17)
        .addConstraint("constr_lt", disjointHighLevelVarExpression(18, z, 19, w), LESS_THAN_SCALAR, 20)
        .setObjectiveFunction(simpleLinearObjectiveFunction(disjointHighLevelVarExpression(40, x, 50, y, 60)));
  }

  @Override
  public HighLevelLPBuilder makeMatchingNontrivialObject() {
    // I normally use 1e-9 for such tests, but I need to use a smaller number here
    // because the objective multiplies a bunch of numbers and gets to a large one,
    // so the difference ends up being more than 1e-9.
    double e = 1e-13;
    HighLevelLPBuilder highLevelLPBuilder = makeRealHighLevelLPBuilder();
    HighLevelVariablesBuilder varBuilder = highLevelLPBuilder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addRawVariable("x");
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.atLeast(4.4 + e));
    RawVariable z = varBuilder.addConstrainedRawVariable("z", Range.closed(-5.5 + e, 6.6 + e));
    SuperVar w = highLevelLPBuilder.getHighLevelVariablesBuilder().addSuperVar(generalSuperVarWithoutArtificialTerms(
        DUMMY_LABEL,
        disjointHighLevelVarExpression(100 + e, x, 200 + e, y, 300 + e),
        minMaxConstraints(-400 + e, 500 + e, CHECK_FOR_DISJOINT_RAW_VARIABLES)));
    return highLevelLPBuilder
        .addConstraint("constr_gt", disjointHighLevelVarExpression(12 + e, x, 13 + e, y), GREATER_THAN_SCALAR, 14 + e)
        .addConstraint("constr_eq", disjointHighLevelVarExpression(15 + e, y, 16 + e, z), EQUAL_TO_SCALAR, 17 + e)
        .addConstraint("constr_lt", disjointHighLevelVarExpression(18 + e, z, 19 + e, w), LESS_THAN_SCALAR, 20 + e)
        .setObjectiveFunction(simpleLinearObjectiveFunction(disjointHighLevelVarExpression(40 + e, x, 50 + e, y, 60 + e)));
  }

  @Override
  protected boolean willMatch(HighLevelLPBuilder expected, HighLevelLPBuilder actual) {
    return highLevelLPBuilderMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<HighLevelLPBuilder> highLevelLPBuilderMatcher(HighLevelLPBuilder expected) {
    return makeMatcher(expected,
        matchList(v -> v.getConstraints(),               f -> highLevelVarConstraintMatcher(f, Optional.empty())),
        match(v -> v.getHighLevelVariablesBuilder(), f -> highLevelVariablesBuilderMatcher(f)),
        match(v -> v.getLinearObjectiveFunction(),   f -> linearObjectiveFunctionMatcher(f)),
        matchOptional(v -> v.getInitialPoint(),      f -> eitherMatcher(f,
            v1 -> initialFeasiblePointMatcher(v1),
            v2 -> initialInfeasiblePointMatcher(v2))));
  }

}
