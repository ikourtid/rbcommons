//package com.rb.nonbiz.math.optimization.jopt;
//
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.Range;
//import com.rb.biz.investing.strategy.optbased.GlobalObjective;
//import com.rb.biz.investing.strategy.optbased.NaiveSubObjectiveCoefficients;
//import com.rb.biz.investing.strategy.optbased.NaiveSubObjectiveCoefficientsTest;
//import com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder;
//import com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.AllRawVariablesInOrderBuilder;
//import com.rb.nonbiz.math.optimization.general.FeasibleOptimizationResult;
//import com.rb.nonbiz.math.optimization.general.LPBuilder;
//import com.rb.nonbiz.math.optimization.general.LinearOptimizationProgram;
//import com.rb.nonbiz.math.optimization.general.RawVariable;
//import com.rb.nonbiz.math.optimization.general.ScalingInstructionsForImprovedLpAccuracyTest;
//import com.rb.nonbiz.testutils.RBTest;
//import com.rb.nonbiz.text.Strings;
//import org.junit.Test;
//
//import java.util.stream.Collectors;
//
//import static com.rb.biz.investing.strategy.optbased.GlobalObjectiveTest.trackingOnlyGlobalObjective;
//import static com.rb.biz.investing.strategy.optbased.NaiveSubObjective.naiveSubObjective;
//import static com.rb.biz.investing.strategy.optbased.TrackingSubObjectiveTest.naiveOnlyTrackingSubObjective;
//import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.AllRawVariablesInOrderBuilder.allRawVariablesInOrderBuilder;
//import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.allRawVariablesInOrder;
//import static com.rb.nonbiz.math.optimization.general.LPBuilder.lpBuilder;
//import static com.rb.nonbiz.math.optimization.general.LinearObjectiveFunctionWithArtificialTermsTest.linearObjectiveFunctionWithNoArtificialTerms;
//import static com.rb.nonbiz.math.optimization.general.NormalizedLeafSubObjective.normalizedNaiveSubObjective;
//import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplierTest.unitNaiveSubObjectiveNormalization;
//import static com.rb.nonbiz.math.optimization.general.OptimizationResultTest.assumeFeasible;
//import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
//import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.disjointHighLevelVarExpression;
//import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.disjointHighLevelVarExpressionWithoutConstantTerm;
//import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.sumOfDisjointHighLevelVars;
//import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarWithWeight.highLevelVarWithWeight;
//import static com.rb.nonbiz.testutils.Asserters.assertDoubleArraysAlmostEqual;
//import static com.rb.nonbiz.testutils.RBIntegrationTest.makeRealObject;
//import static org.junit.Assert.assertEquals;
//
///**
// * This is best tested in an integration test
// */
//public class JOptLinearOptimizerTest extends RBTest<JOptLinearOptimizer> {
//
//  @Test
//  public void onlyVariableRanges_minimizeSum_returnsOptimal() {
//    RawVariable var0 = rawVariable("1st var", 0);
//    RawVariable var1 = rawVariable("2nd var", 1);
//    NaiveSubObjectiveCoefficients dummyCoefficients = new NaiveSubObjectiveCoefficientsTest().makeTrivialObject();
//    LinearOptimizationProgram lp =
//        lpBuilder(allRawVariablesInOrder(var0, var1))
//            .withScalingInstructionsForImprovedLpAccuracy(
//                new ScalingInstructionsForImprovedLpAccuracyTest().makeDummyObject()) // joptimizer does not use this yet (May 2020)
//            .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithNoArtificialTerms(
//                trackingOnlyGlobalObjective(
//                    naiveOnlyTrackingSubObjective(
//                        normalizedNaiveSubObjective(
//                            naiveSubObjective(sumOfDisjointHighLevelVars(var0, var1), dummyCoefficients),
//                            unitNaiveSubObjectiveNormalization())))))
//            .withVariableRange(var0, Range.closed(-2.0, 3.0))
//            .withVariableRange(var1, Range.closed(-4.0, 5.0))
//            .build();
//    FeasibleOptimizationResult solution = assumeFeasible(makeTestObject().minimize(lp));
//    assertEquals(
//        ImmutableList.of(var0, var1),
//        solution.getAllRawVariablesAndOptimalValues().getAllRawVariablesAndDoubles()
//            .getAllRawVariablesInOrder().getRawVariablesInOrder());
//    assertDoubleArraysAlmostEqual(
//        // If we want to minimize the sum, we need each var to go to its lowest possible value in the range
//        new double[] { -2.0, -4.0 },
//        solution.getAllRawVariablesAndOptimalValues().getAllRawVariablesAndDoubles().getAllValuesInOrder(),
//        1e-8);
//  }
//
//  @Test
//  public void onlyVariableRanges_maximizeSum_returnsOptimal() {
//    RawVariable var0 = rawVariable("1st var", 0);
//    RawVariable var1 = rawVariable("2nd var", 1);
//    AllRawVariablesInOrder allVars = allRawVariablesInOrder(var0, var1);
//    NaiveSubObjectiveCoefficients dummyCoefficients = new NaiveSubObjectiveCoefficientsTest().makeTrivialObject();
//    LinearOptimizationProgram lp =
//        lpBuilder(allVars)
//            .withScalingInstructionsForImprovedLpAccuracy(
//                new ScalingInstructionsForImprovedLpAccuracyTest().makeDummyObject()) // joptimizer does not use this yet (May 2020)
//            .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithNoArtificialTerms(
//                trackingOnlyGlobalObjective(
//                    naiveOnlyTrackingSubObjective(
//                        normalizedNaiveSubObjective(
//                            naiveSubObjective(disjointHighLevelVarExpression(-1, var0, -1, var1), dummyCoefficients),
//                            unitNaiveSubObjectiveNormalization())))))
//            .withVariableRange(var0, Range.closed(-2.0, 3.0))
//            .withVariableRange(var1, Range.closed(-4.0, 5.0))
//            .build();
//    FeasibleOptimizationResult solution = assumeFeasible(makeTestObject().minimize(lp));
//    assertEquals(
//        ImmutableList.of(var0, var1),
//        solution.getAllRawVariablesAndOptimalValues().getAllRawVariablesAndDoubles()
//            .getAllRawVariablesInOrder().getRawVariablesInOrder());
//    assertDoubleArraysAlmostEqual(
//        // If we want to minimize the sum, we need each var to go to its lowest possible value in the range
//        new double[] { 3.0, 5.0 },
//        solution.getAllRawVariablesAndOptimalValues().getAllRawVariablesAndDoubles().getAllValuesInOrder(),
//        1e-8);
//  }
//
//  @Test
//  public void onlyVariableRanges_minimizeSum_10000variables_returnsOptimal() {
//    int numVariables = 10_000;
//    AllRawVariablesInOrder allVars = generateVariables(numVariables);
//    LPBuilder lpBuilder = lpBuilder(allVars);
//    lpBuilder
//        .withScalingInstructionsForImprovedLpAccuracy(
//            new ScalingInstructionsForImprovedLpAccuracyTest().makeDummyObject()) // joptimizer does not use this yet (May 2020)
//        .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithNoArtificialTerms(
//            generateMinimizeSumObjective(allVars)));
//    for (int i = 0; i < numVariables; i++) {
//      lpBuilder.withVariableRange(i, Range.closed(-2.0, 3.0));
//    }
//    LinearOptimizationProgram lp = lpBuilder.build();
//    FeasibleOptimizationResult solution = assumeFeasible(makeTestObject().minimize(lp));
//    for (int i = 0; i < numVariables; i++) {
//      assertEquals(
//          allVars.get(i),
//          lp.getAllRawVariablesInOrder().get(i));
//      assertEquals(
//          solution.getAllRawVariablesAndOptimalValues().getAllRawVariablesAndDoubles().getAllRawVariablesInOrder().get(i),
//          lp.getAllRawVariablesInOrder().get(i));
//      assertEquals(
//          "If we want to minimize the sum, we need each var to go to its lowest possible value in the range (-2.0)",
//          -2.0,
//          solution.getAllRawVariablesAndOptimalValues().getAllRawVariablesAndDoubles().getValue(i),
//          1e-8);
//    }
//  }
//
//  @Test
//  public void constraintsAndRanges_minimizeSum_50variables_returnsOptimal() {
//    // On my Dell m6600 laptop, I tried these combinations when the ranges partly overlapped the space
//    // defined by the constraints:
//    //  100 supervars =  4 secs
//    //  500 supervars =  32 secs
//    // 1000 supervars = 135 secs
//    // However, making the ranges wide enough that they don't matter (meaning that the search space is solely
//    // what the constraints define) changes things
//    //  100 supervars =   1 secs
//    //  500 supervars =  15 secs
//    // 1000 supervars = 135 secs (100 warm)
//    constraintsAndRanges_minimizeSum_manyVariables_returnsOptimal_helper(50);
//  }
//
//  private void constraintsAndRanges_minimizeSum_manyVariables_returnsOptimal_helper(int numVariables) {
//    AllRawVariablesInOrder allVars = generateVariables(numVariables);
//    LPBuilder lpBuilder = lpBuilder(allVars);
//    lpBuilder
//        .withScalingInstructionsForImprovedLpAccuracy(
//            new ScalingInstructionsForImprovedLpAccuracyTest().makeDummyObject()) // joptimizer does not use this yet (May 2020)
//        .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithNoArtificialTerms(
//            generateMinimizeSumObjective(allVars)));
//    for (int i = 0; i < numVariables; i++) {
//      // The range isn't really a bottleneck in this optimization, as the constraints define a space
//      // that's a subset of the N-dimensional space defined by the range. But let's add them anyway.
//      lpBuilder.withVariableRange(i, Range.closed(-50.0, 50.0));
//    }
//    // We'll add the following n constraints.
//    // -5 < x1 + x2 < 5
//    // -5 < x2 + x3 < 5
//    // ...
//    // -5 < xn + x1 < 5
//    for (int i = 0; i < numVariables; i++) {
//      double[] coefficients = new double[numVariables];
//      int indexA = i;
//      int indexB = (i + 1) % numVariables;
//      coefficients[indexA] = 1.0;
//      coefficients[indexB] = 1.0;
//      lpBuilder.withRawVariableCombinationLessThanScalar(
//          Strings.format("%s + %s < 5", allVars.get(indexA), allVars.get(indexB)),
//          coefficients,
//          5.0);
//      lpBuilder.withRawVariableCombinationGreaterThanScalar(
//          Strings.format("-5 < %s + %s", allVars.get(indexA), allVars.get(indexB)),
//          coefficients,
//          -5.0);
//    }
//    LinearOptimizationProgram lp = lpBuilder.build();
//    FeasibleOptimizationResult solution = assumeFeasible(makeTestObject().minimize(lp));
//    for (int i = 0; i < numVariables; i++) {
//      assertEquals(-2.5, solution.getAllRawVariablesAndOptimalValues().getAllRawVariablesAndDoubles().getValue(i), 1e-7);
//    }
//  }
//
//  private AllRawVariablesInOrder generateVariables(int numVariables) {
//    AllRawVariablesInOrderBuilder allRawVariablesInOrderBuilder = allRawVariablesInOrderBuilder();
//    for (int i = 0; i < numVariables; i++) {
//      allRawVariablesInOrderBuilder.addRawVariable(Strings.format("var %s", i));
//    }
//    return allRawVariablesInOrderBuilder.build();
//  }
//
//  private GlobalObjective generateMinimizeSumObjective(AllRawVariablesInOrder allVars) {
//    NaiveSubObjectiveCoefficients dummyCoefficients = new NaiveSubObjectiveCoefficientsTest().makeTrivialObject();
//    return trackingOnlyGlobalObjective(
//        naiveOnlyTrackingSubObjective(normalizedNaiveSubObjective(
//            naiveSubObjective(
//                disjointHighLevelVarExpressionWithoutConstantTerm(
//                    allVars.getRawVariablesInOrder()
//                        .stream()
//                        .map(var -> highLevelVarWithWeight(var, 1.0))
//                        .collect(Collectors.toList())),
//                dummyCoefficients),
//            unitNaiveSubObjectiveNormalization())));
//  }
//
//  @Override
//  protected JOptLinearOptimizer makeTestObject() {
//    return makeRealJOptLinearOptimizer();
//  }
//
//  public static JOptLinearOptimizer makeRealJOptLinearOptimizer() {
//    return makeRealObject(JOptLinearOptimizer.class);
//  }
//
//}