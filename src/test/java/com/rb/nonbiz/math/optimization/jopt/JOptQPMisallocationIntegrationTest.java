//package com.rb.nonbiz.math.optimization.jopt;
//
//import com.google.common.collect.Range;
//import com.joptimizer.functions.ConvexMultivariateRealFunction;
//import com.joptimizer.functions.LinearMultivariateRealFunction;
//import com.joptimizer.functions.PDQuadraticMultivariateRealFunction;
//import com.joptimizer.optimizers.JOptimizer;
//import com.joptimizer.optimizers.OptimizationRequest;
//import com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder;
//import com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.AllRawVariablesInOrderBuilder;
//import com.rb.nonbiz.math.optimization.general.FeasibleOptimizationResult;
//import com.rb.nonbiz.math.optimization.general.QPBuilder;
//import com.rb.nonbiz.math.optimization.general.QuadraticObjectiveFunction;
//import com.rb.nonbiz.math.optimization.general.QuadraticOptimizationProgram;
//import com.rb.nonbiz.math.optimization.general.RawVariable;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.Optional;
//
//import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
//import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
//import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.AllRawVariablesInOrderBuilder.allRawVariablesInOrderBuilder;
//import static com.rb.nonbiz.math.optimization.general.QPBuilder.qpBuilder;
//import static com.rb.nonbiz.math.optimization.general.QuadraticObjectiveFunction.quadraticObjectiveFunction;
//import static com.rb.nonbiz.math.optimization.general.WeightedRawVariables.singletonWeightedRawVariables;
//import static com.rb.nonbiz.math.optimization.general.WeightedRawVariables.weightedRawVariables;
//import static com.rb.nonbiz.math.optimization.jopt.JOptQuadraticOptimizerTest.makeRealJOptQuadraticOptimizer;
//import static com.rb.nonbiz.testutils.Asserters.assertDoubleArraysAlmostEqual;
//import static junit.framework.TestCase.assertTrue;
//import static org.junit.Assert.fail;
//
///**
// * All the tests in this class try to solve the same problem, but using permutations of
// * 1) 2 formulations: QP supervars may represent the final weight, OR the deviation from the ideal weight.
// * 2) raw JOptimizer calls vs the RB wrappers that abstract that away
// * 3) the 'sum of weights is 1' may either be formulated as an epsilon-based pair of 2 inequalities,
// *    or as regular inequalities.
// *
// * PROBLEM STATEMENT
// *
// * A ideal target is 10%
// * B is 40%
// * C is 50%
// *
// * C is forced to be no less than 70%, so 20% overweight.
// *
// * EXPECTED OUTCOME
// * A + B must become underweight. They split this 'underweightness'
// * proportionately, so A gets 1/5 of it and becomes 10% - 1/5 * 20% = 6% and B becomes 40% - 4/5 * 20% = 24%
// *
// * FORMULATION 1:
// * let's say mA is the (signed) misallocation for A: e.g. if A is at 8% (vs 10% ideal target), then mA = -0.02
// *
// * {@code minimize mA ^ 2 + mB ^ 2 / 4 + mC ^ 2 / 5}
// * (square of Euclidean distance of misallocation - with each 1% of misallocation penalized
// * proportionally less for asset classes with a bigger target % - e.g. 10% going to 9% is like 40% going to 36%).
// *
// * subject to
// * <pre>
// * {@code mA + mB + mC = 0}
// * {@code -0.1 <= mA <= 0.9}
// * {@code -0.4 <= mB <= 0.6}
// * {@code  0.2 <= mC <= 0.5}
// * </pre>
// *
// * FORMULATION 2:
// * Final weights are wA, wB, wC
// * minimize
// * {@code (wA - 0.1) ^ 2 + (wB - 0.4) ^ 2 / 4 + (wB - 0.5) ^ 2 / 5}
// *
// * subject to
// * <pre>
// * {@code wA + wB + wC = 1}
// * {@code 0   <= wA <= 1}
// * {@code 0   <= wB <= 1}
// * {@code 0.7 <= wC <= 1}
// * </pre>
// */
//public class JOptQPMisallocationIntegrationTest {
//
//  RawVariable mA;
//  RawVariable mB;
//  RawVariable mC;
//  AllRawVariablesInOrder misallocationVariables;
//
//  RawVariable wA;
//  RawVariable wB;
//  RawVariable wC;
//  AllRawVariablesInOrder weightVariables;
//
//  @Before
//  public void setup() {
//    AllRawVariablesInOrderBuilder misallocationAllRawVariablesInOrderBuilder = allRawVariablesInOrderBuilder();
//    mA = misallocationAllRawVariablesInOrderBuilder.addRawVariable("mA");
//    mB = misallocationAllRawVariablesInOrderBuilder.addRawVariable("mB");
//    mC = misallocationAllRawVariablesInOrderBuilder.addRawVariable("mC");
//    misallocationVariables = misallocationAllRawVariablesInOrderBuilder.build();
//
//    AllRawVariablesInOrderBuilder weightAllRawVariablesInOrderBuilder = allRawVariablesInOrderBuilder();
//    wA = weightAllRawVariablesInOrderBuilder.addRawVariable("wA");
//    wB = weightAllRawVariablesInOrderBuilder.addRawVariable("wB");
//    wC = weightAllRawVariablesInOrderBuilder.addRawVariable("wC");
//    weightVariables = weightAllRawVariablesInOrderBuilder.build();
//  }
//
//  @Test
//  public void variablesRepresentMisallocations_usesSingleEquality_usesRawJOpt() {
//    OptimizationRequest or = new OptimizationRequest();
//    or.setF0(getRawObjectiveUsingMisallocation());
//    or.setFi(new ConvexMultivariateRealFunction[] {
//        new LinearMultivariateRealFunction(new double[] { -1,  0,  0 }, -0.1), // -0.1 <= mA
//        new LinearMultivariateRealFunction(new double[] {  1,  0,  0 }, -0.9), // mA <= 0.9
//        new LinearMultivariateRealFunction(new double[] {  0, -1,  0 }, -0.4), // -0.4 <= mB
//        new LinearMultivariateRealFunction(new double[] {  0,  1,  0 }, -0.6), // mB <= 0.9
//        new LinearMultivariateRealFunction(new double[] {  0,  0, -1 },  0.2), // 0.2 <= mC
//        new LinearMultivariateRealFunction(new double[] {  0,  0,  1 }, -0.5), // mC <= 0.5
//    });
//    or.setA(new double[][] { { 1.0, 1.0, 1.0 } }); // mA + mB + mC = 0
//    or.setB(new double[] { 0.0 });
//    assertRawOptimization(new double[] { -0.04, -0.16, 0.2 }, or);
//  }
//
//  @Test
//  public void variablesRepresentMisallocations_usesSingleEquality_usesWrapperWithoutVarRanges() {
//    QPBuilder qpBuilder = qpBuilder(misallocationVariables);
//    qpBuilder.withQuadraticObjectiveFunctionToMinimize(getObjectiveUsingMisallocation(misallocationVariables));
//    qpBuilder.withVariableCombinationEqualToScalar("mA + mB + mC = 0", weightedRawVariables(doubleMap(rbMapOf(mA, 1.0, mB, 1.0, mC, 1.0))), 0);
//    addInequalities_variablesRepresentMisallocations(qpBuilder);
//    assertOptimization(new double[] { -0.04, -0.16, 0.2 }, qpBuilder.build());
//  }
//
//  @Test
//  public void variablesRepresentMisallocations_usesSingleEquality_usesWrapperWithVarRanges() {
//    QPBuilder qpBuilder = qpBuilder(misallocationVariables);
//    qpBuilder.withQuadraticObjectiveFunctionToMinimize(getObjectiveUsingMisallocation(misallocationVariables));
//    qpBuilder.withVariableCombinationEqualToScalar("mA + mB + mC = 0", weightedRawVariables(doubleMap(rbMapOf(mA, 1.0, mB, 1.0, mC, 1.0))), 0);
//    addVariableRanges_variablesRepresentMisallocations(qpBuilder);
//    assertOptimization(new double[] { -0.04, -0.16, 0.2 }, qpBuilder.build());
//  }
//
//  @Test
//  public void variablesRepresentMisallocations_usesPairOfEpsilonInequalities_usesRawJOpt() {
//    OptimizationRequest or = new OptimizationRequest();
//    or.setF0(getRawObjectiveUsingMisallocation());
//    or.setFi(new ConvexMultivariateRealFunction[] {
//        new LinearMultivariateRealFunction(new double[] { -1,  0,  0 }, -0.1), // -0.1 <= mA
//        new LinearMultivariateRealFunction(new double[] {  1,  0,  0 }, -0.9), // mA <= 0.9
//        new LinearMultivariateRealFunction(new double[] {  0, -1,  0 }, -0.4), // -0.4 <= mB
//        new LinearMultivariateRealFunction(new double[] {  0,  1,  0 }, -0.6), // mB <= 0.9
//        new LinearMultivariateRealFunction(new double[] {  0,  0, -1 },  0.2), // 0.2 <= mC
//        new LinearMultivariateRealFunction(new double[] {  0,  0,  1 }, -0.5), // mC <= 0.5
//        new LinearMultivariateRealFunction(new double[] {  1,  1,  1 }, -1e-7), // mA + mB + mC < epsilon
//        new LinearMultivariateRealFunction(new double[] { -1, -1, -1 }, -1e-7), // mA + mB + mC > -epsilon
//    });
//    assertRawOptimization(new double[] { -0.04, -0.16, 0.2 }, or);
//  }
//
//  @Test
//  public void variablesRepresentMisallocations_usesPairOfEpsilonInequalities_usesWrapperWithoutVarRanges() {
//    QPBuilder qpBuilder = qpBuilder(misallocationVariables);
//    qpBuilder.withQuadraticObjectiveFunctionToMinimize(getObjectiveUsingMisallocation(misallocationVariables));
//    qpBuilder.withVariableCombinationAlmostEqualToScalar("mA + mB + mC = 0", weightedRawVariables(doubleMap(rbMapOf(mA, 1.0, mB, 1.0, mC, 1.0))), 0, 1e-7);
//    addInequalities_variablesRepresentMisallocations(qpBuilder);
//    assertOptimization(new double[] { -0.04, -0.16, 0.2 }, qpBuilder.build());
//  }
//
//  @Test
//  public void variablesRepresentMisallocations_usesPairOfEpsilonInequalities_usesWrapperWithVarRanges() {
//    QPBuilder qpBuilder = qpBuilder(misallocationVariables);
//    qpBuilder.withQuadraticObjectiveFunctionToMinimize(getObjectiveUsingMisallocation(misallocationVariables));
//    qpBuilder.withVariableCombinationAlmostEqualToScalar("mA + mB + mC = 0", weightedRawVariables(doubleMap(rbMapOf(mA, 1.0, mB, 1.0, mC, 1.0))), 0, 1e-7);
//    addVariableRanges_variablesRepresentMisallocations(qpBuilder);
//    assertOptimization(new double[] { -0.04, -0.16, 0.2 }, qpBuilder.build());
//  }
//
//  @Test
//  public void variablesRepresentWeights_usesSingleEquality_usesRawJOpt() {
//    OptimizationRequest or = new OptimizationRequest();
//    or.setF0(getRawObjectiveUsingWeights());
//    or.setFi(new ConvexMultivariateRealFunction[] {
//        new LinearMultivariateRealFunction(new double[] { -1,  0,  0 },  0), // 0 <= wA
//        new LinearMultivariateRealFunction(new double[] {  1,  0,  0 }, -1), // wA <= 1
//        new LinearMultivariateRealFunction(new double[] {  0, -1,  0 },  0), // 0 <= wB
//        new LinearMultivariateRealFunction(new double[] {  0,  1,  0 }, -1), // wB <= 1
//        new LinearMultivariateRealFunction(new double[] {  0,  0, -1 },  0.7), // 0.7 <= wC
//        new LinearMultivariateRealFunction(new double[] {  0,  0,  1 }, -1), // wC <= 1
//    });
//    or.setA(new double[][] { { 1.0, 1.0, 1.0 } }); // wA + wB + wC = 1
//    or.setB(new double[] { 1 });
//    assertRawOptimization(new double[] { 0.06, 0.24, 0.70 }, or);
//  }
//
//  @Test
//  public void variablesRepresentWeights_usesSingleEquality_usesWrapperWithoutVarRanges() {
//    QPBuilder qpBuilder = qpBuilder(weightVariables);
//    qpBuilder.withQuadraticObjectiveFunctionToMinimize(getObjectiveUsingWeights(weightVariables));
//    qpBuilder.withVariableCombinationEqualToScalar("wA + wB + wC = 1", weightedRawVariables(doubleMap(rbMapOf(wA, 1.0, wB, 1.0, wC, 1.0))), 1);
//    addInequalities_variablesRepresentWeights(qpBuilder);
//    assertOptimization(new double[] { 0.06, 0.24, 0.70 }, qpBuilder.build());
//  }
//
//  @Test
//  public void variablesRepresentWeights_usesSingleEquality_usesWrapperWithVarRanges() {
//    QPBuilder qpBuilder = qpBuilder(weightVariables);
//    qpBuilder.withQuadraticObjectiveFunctionToMinimize(getObjectiveUsingWeights(weightVariables));
//    qpBuilder.withVariableCombinationEqualToScalar("wA + wB + wC = 1", weightedRawVariables(doubleMap(rbMapOf(wA, 1.0, wB, 1.0, wC, 1.0))), 1);
//    addVariableRanges_variablesRepresentWeights(qpBuilder);
//    assertOptimization(new double[] { 0.06, 0.24, 0.70 }, qpBuilder.build());
//  }
//
//  @Test
//  public void variablesRepresentWeights_usesPairOfEpsilonInequalities_usesRawJOpt() {
//    OptimizationRequest or = new OptimizationRequest();
//    or.setF0(getRawObjectiveUsingWeights());
//    or.setFi(new ConvexMultivariateRealFunction[] {
//        new LinearMultivariateRealFunction(new double[] { -1,  0,  0 },  0), // 0 <= wA
//        new LinearMultivariateRealFunction(new double[] {  1,  0,  0 }, -1), // wA <= 1
//        new LinearMultivariateRealFunction(new double[] {  0, -1,  0 },  0), // 0 <= wB
//        new LinearMultivariateRealFunction(new double[] {  0,  1,  0 }, -1), // wB <= 1
//        new LinearMultivariateRealFunction(new double[] {  0,  0, -1 },  0.7), // 0.7 <= wC
//        new LinearMultivariateRealFunction(new double[] {  0,  0,  1 }, -1), // wC <= 1
//
//        new LinearMultivariateRealFunction(new double[] {  1,  1,  1 }, -1 - 1e-7), // wA + wB + wC < 1 + epsilon
//        new LinearMultivariateRealFunction(new double[] { -1, -1, -1 },  1 - 1e-7) // wA + wB + wC > 1 - epsilon
//    });
//    assertRawOptimization(new double[] { 0.06, 0.24, 0.70 }, or);
//  }
//
//  @Test
//  public void variablesRepresentWeights_usesPairOfEpsilonInequalities_usesWrapperWithoutVarRanges() {
//    QPBuilder qpBuilder = qpBuilder(weightVariables);
//    qpBuilder.withQuadraticObjectiveFunctionToMinimize(getObjectiveUsingWeights(weightVariables));
//    qpBuilder.withVariableCombinationAlmostEqualToScalar("wA + wB + wC = 1", weightedRawVariables(doubleMap(rbMapOf(wA, 1.0, wB, 1.0, wC, 1.0))), 1, 1e-7);
//    addInequalities_variablesRepresentWeights(qpBuilder);
//    assertOptimization(new double[] { 0.06, 0.24, 0.70 }, qpBuilder.build());
//  }
//
//  @Test
//  public void variablesRepresentWeights_usesPairOfEpsilonInequalities_usesWrapperWithVarRanges() {
//    QPBuilder qpBuilder = qpBuilder(weightVariables);
//    qpBuilder.withQuadraticObjectiveFunctionToMinimize(getObjectiveUsingWeights(weightVariables));
//    qpBuilder.withVariableCombinationAlmostEqualToScalar("wA + wB + wC = 1", weightedRawVariables(doubleMap(rbMapOf(wA, 1.0, wB, 1.0, wC, 1.0))), 1, 1e-7);
//    addVariableRanges_variablesRepresentWeights(qpBuilder);
//    assertOptimization(new double[] { 0.06, 0.24, 0.70 }, qpBuilder.build());
//  }
//
//  private void assertRawOptimization(double[] expected, OptimizationRequest optimizationRequest) {
//    optimizationRequest.setToleranceFeas(1e-12);
//    optimizationRequest.setTolerance(1e-12);
//    JOptimizer opt = new JOptimizer();
//    opt.setOptimizationRequest(optimizationRequest);
//    try {
//      int returnCode = opt.optimize();
//    } catch (Exception e) {
//      fail("caught exception " + e);
//    }
//    assertDoubleArraysAlmostEqual(expected, opt.getOptimizationResponse().getSolution(), 1e-7);
//  }
//
//  private void assertOptimization(double[] expected, QuadraticOptimizationProgram qp) {
//    Optional<FeasibleOptimizationResult> result = makeRealJOptQuadraticOptimizer().minimize(qp);
//    assertTrue("Problem must be feasible", result.isPresent());
//    assertDoubleArraysAlmostEqual(
//        expected,
//        result.get().getAllRawVariablesAndOptimalValues().getAllRawVariablesAndDoubles().getAllValuesInOrder(),
//        1e-7);
//  }
//
//  private PDQuadraticMultivariateRealFunction getRawObjectiveUsingMisallocation() {
//    return new PDQuadraticMultivariateRealFunction(new double[][] {
//        { 1, 0,       0 },
//        { 0, 1 / 4.0, 0 },
//        { 0, 0,       1 / 5.0 }},
//        new double[] { 0, 0, 0 },
//        0);
//  }
//
//  private QuadraticObjectiveFunction getObjectiveUsingMisallocation(AllRawVariablesInOrder variables) {
//    return quadraticObjectiveFunction(
//        variables,
//        getRawObjectiveUsingMisallocation().getP(),
//        getRawObjectiveUsingMisallocation().getQ());
//  }
//
//  private PDQuadraticMultivariateRealFunction getRawObjectiveUsingWeights() {
//    /**
//     * minimize (wA - 0.1) ^ 2 + (wB - 0.4) ^ 2 / 4 + (wB - 0.5) ^ 2 / 5 =
//     * wA ^ 2 + wB ^ 2 / 4 + wC ^ 2 / 5
//     * - 2 * 0.1 * wA - 2 * 0.4 * wB / 4 - 2 * 0.5 * wC / 5
//     * (dropping constant terms, as they don't affect how we minimize the objective function)
//     *
//     * JOptimizer's formulation (and I think it's the standard one) uses a 1/2 in front of the matrix.
//     * Since the objective function can be multiplied by a scalar and minimizing it will have the same result,
//     * we don't need the 2s in the linear terms.
//     */
//    return new PDQuadraticMultivariateRealFunction(new double[][] {
//        { 1, 0,       0 },
//        { 0, 1 / 4.0, 0 },
//        { 0, 0,       1 / 5.0 }},
//        new double[] { -0.1, -0.4 / 4.0, -0.5 / 5.0 },
//        0);
//  }
//
//  private QuadraticObjectiveFunction getObjectiveUsingWeights(AllRawVariablesInOrder variables) {
//    return quadraticObjectiveFunction(
//        variables,
//        getRawObjectiveUsingWeights().getP(),
//        getRawObjectiveUsingWeights().getQ());
//  }
//
//  private void addInequalities_variablesRepresentMisallocations(QPBuilder qpBuilder) {
//    qpBuilder.withVariableCombinationGreaterThanScalar("-0.1 <= mA", singletonWeightedRawVariables(mA, 1.0), -0.1);
//    qpBuilder.withVariableCombinationLessThanScalar("mA <= 0.9", singletonWeightedRawVariables(mA, 1.0), 0.9);
//    qpBuilder.withVariableCombinationGreaterThanScalar("-0.4 <= mB", singletonWeightedRawVariables(mB, 1.0), -0.4);
//    qpBuilder.withVariableCombinationLessThanScalar("mB <= 0.6", singletonWeightedRawVariables(mB, 1.0), 0.6);
//    qpBuilder.withVariableCombinationGreaterThanScalar("0.2 <= mC", singletonWeightedRawVariables(mC, 1.0), 0.2);
//    qpBuilder.withVariableCombinationLessThanScalar("mC <= 0.5", singletonWeightedRawVariables(mC, 1.0), 0.5);
//  }
//
//  private void addInequalities_variablesRepresentWeights(QPBuilder qpBuilder) {
//    qpBuilder.withVariableCombinationGreaterThanScalar("0 <= wA", singletonWeightedRawVariables(wA, 1.0), 0);
//    qpBuilder.withVariableCombinationLessThanScalar("wA <= 1", singletonWeightedRawVariables(wA, 1.0), 1);
//    qpBuilder.withVariableCombinationGreaterThanScalar("0 <= wB", singletonWeightedRawVariables(wB, 1.0), 0);
//    qpBuilder.withVariableCombinationLessThanScalar("wB <= 1", singletonWeightedRawVariables(wB, 1.0), 1);
//    qpBuilder.withVariableCombinationGreaterThanScalar("0.7 <= wC", singletonWeightedRawVariables(wC, 1.0), 0.7);
//    qpBuilder.withVariableCombinationLessThanScalar("wC <= 1", singletonWeightedRawVariables(wC, 1.0), 1);
//  }
//
//  private void addVariableRanges_variablesRepresentMisallocations(QPBuilder qpBuilder) {
//    qpBuilder.withVariableRange(mA, Range.closed(-0.1, 0.9));
//    qpBuilder.withVariableRange(mB, Range.closed(-0.4, 0.6));
//    qpBuilder.withVariableRange(mC, Range.closed( 0.2, 0.5));
//  }
//
//  private void addVariableRanges_variablesRepresentWeights(QPBuilder qpBuilder) {
//    qpBuilder.withVariableRange(wA, Range.closed(0.0, 1.0));
//    qpBuilder.withVariableRange(wB, Range.closed(0.0, 1.0));
//    qpBuilder.withVariableRange(wC, Range.closed(0.7, 1.0));
//  }
//
//}
