//package com.rb.nonbiz.math.optimization.jopt;
//
//import cern.colt.matrix.impl.DenseDoubleMatrix1D;
//import cern.colt.matrix.impl.DenseDoubleMatrix2D;
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
//import com.rb.nonbiz.math.optimization.general.RawVariable;
//import com.rb.nonbiz.testutils.RBTest;
//import org.junit.Test;
//
//import java.util.Optional;
//
//import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
//import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
//import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.AllRawVariablesInOrderBuilder.allRawVariablesInOrderBuilder;
//import static com.rb.nonbiz.math.optimization.general.QPBuilder.qpBuilder;
//import static com.rb.nonbiz.math.optimization.general.QuadraticObjectiveFunction.quadraticObjectiveFunction;
//import static com.rb.nonbiz.math.optimization.general.WeightedRawVariables.weightedRawVariables;
//import static com.rb.nonbiz.math.optimization.jopt.JOptQuadraticOptimizerTest.makeRealJOptQuadraticOptimizer;
//import static com.rb.nonbiz.testutils.Asserters.assertDoubleArraysAlmostEqual;
//import static junit.framework.TestCase.assertTrue;
//import static org.junit.Assert.assertEquals;
//
//public class JOptQPGeneralIntegrationTest extends RBTest<JOptQuadraticOptimizer> {
//
//  /**
//   * minimize {@code x ^ 2 + y ^ 2} , i.e. the (square of) Euclidean distance from origin
//   * subject to
//   * <pre>
//   * {@code y - x < -5}
//   * {@code y + x < -5}
//   * </pre>
//   * min should be at {@code x = 0}, jc
//   */
//  @Test
//  public void simpleDistanceFromOrigin() {
//    AllRawVariablesInOrderBuilder allRawVariablesInOrderBuilder = allRawVariablesInOrderBuilder();
//    RawVariable x = allRawVariablesInOrderBuilder.addRawVariable("x");
//    RawVariable y = allRawVariablesInOrderBuilder.addRawVariable("y");
//    AllRawVariablesInOrder allVariables = allRawVariablesInOrderBuilder.build();
//    QPBuilder qpBuilder = qpBuilder(allVariables);
//    qpBuilder.withQuadraticObjectiveFunctionToMinimize(quadraticObjectiveFunction(
//        allVariables,
//        new DenseDoubleMatrix2D(new double[][]{
//            { 1, 0 },
//            { 0, 1 }
//        }),
//        new DenseDoubleMatrix1D(new double[] { 0, 0 })));
//    qpBuilder.withVariableCombinationLessThanScalar("y - x < -5", weightedRawVariables(doubleMap(rbMapOf(x, -1.0, y, 1.0))), -5.0);
//    qpBuilder.withVariableCombinationLessThanScalar("y + x < -5", weightedRawVariables(doubleMap(rbMapOf(x,  1.0, y, 1.0))), -5.0);
//    // This is weird, but we need this extra constraint (even though the solution is not constrainted by it)
//    // b/c the problem is infeasible otherwise. This isn't just the case with the RB wrapper around joptimizer;
//    // even the raw usage of joptimizer (see simpleDistanceFromOrigin_raw) seems to require this.
//    qpBuilder.withVariableRange(y, Range.atLeast(-77.0));
//    Optional<FeasibleOptimizationResult> solution = makeTestObject().minimize(qpBuilder.build());
//    assertTrue(solution.isPresent());
//    assertEquals(0, solution.get().getAllRawVariablesAndOptimalValues().getAllRawVariablesAndDoubles().getValue(x), 1e-8);
//    assertEquals(-5, solution.get().getAllRawVariablesAndOptimalValues().getAllRawVariablesAndDoubles().getValue(y), 1e-8);
//  }
//
//  @Test
//  public void simpleDistanceFromOrigin_raw() throws Exception {
//    OptimizationRequest or = new OptimizationRequest();
//    // objective function
//    or.setF0(new PDQuadraticMultivariateRealFunction(new double[][] {{ 1, 0 }, { 0, 1 }}, new double[] { 0, 0 }, 0));
//    // inequalities
//    or.setFi(new ConvexMultivariateRealFunction[]{
//        new LinearMultivariateRealFunction(new double[]{ -1, 1 }, 5),  // y - x < -5
//        new LinearMultivariateRealFunction(new double[]{  1, 1 }, 5),  // y + x < -5
//        // This is weird, but we need this extra constraint (even though the solution is not constrainted by it)
//        // b/c the problem is infeasible otherwise.
//        new LinearMultivariateRealFunction(new double[]{ 0, -1 }, -77) // y > -77 <==> -y < 77
//    });
//    or.setToleranceFeas(1e-12);
//    or.setTolerance(1e-12);
//
//    JOptimizer opt = new JOptimizer();
//    opt.setOptimizationRequest(or);
//    int returnCode = opt.optimize();
//    assertDoubleArraysAlmostEqual(new double[] { 0, -5 }, opt.getOptimizationResponse().getSolution(), 1e-8);
//  }
//
//  @Test
//  public void simpleDistanceFromOrigin_1var_raw() throws Exception {
//    OptimizationRequest or = new OptimizationRequest();
//    // objective function
//    or.setF0(new PDQuadraticMultivariateRealFunction(new double[][] {{ 1.0 }}, new double[] { 0.0 }, 0.0)); // minimize x^2
//    // inequalities
//
//    or.setFi(new ConvexMultivariateRealFunction[]{
//        new LinearMultivariateRealFunction(new double[]{ 1.0 }, -7.0),  // x < 7 (<==> x - 7 < 0)
//        new LinearMultivariateRealFunction(new double[]{ -1 }, 5)    // x > 5 <==> -x + 5 < 0)
//    });
//    or.setInitialPoint(new double[] { 6 });
//    or.setToleranceFeas(1e-12);
//    or.setTolerance(1e-12);
//    //or.setA(new double[][]{{ 1 }});
//    //or.setB(new double[]{ 6 });
//
//    JOptimizer opt = new JOptimizer();
//    opt.setOptimizationRequest(or);
//    int returnCode = opt.optimize();
//    assertDoubleArraysAlmostEqual(new double[] { 5 }, opt.getOptimizationResponse().getSolution(), 1e-8);
//
//  }
//
//  @Test
//  public void exampleFromWebpage() throws Exception {
//    OptimizationRequest or = new OptimizationRequest();
//    // objective function
//    or.setF0(new PDQuadraticMultivariateRealFunction(new double[][] {{ 1, 0 }, { 0, 1 }}, new double[] { 0, 0 }, 0));
//    // inequalities
//    or.setFi(new ConvexMultivariateRealFunction[]{
//        new LinearMultivariateRealFunction(new double[]{ -1,  0 }, 0),
//        new LinearMultivariateRealFunction(new double[]{  0, -1 }, 0)
//    });
//    //equalities
//    or.setA(new double[][]{{1,1}});
//    or.setB(new double[]{1});
//    or.setToleranceFeas(1e-12);
//    or.setTolerance(1e-12);
//
//    JOptimizer opt = new JOptimizer();
//    opt.setOptimizationRequest(or);
//    int returnCode = opt.optimize();
//    assertDoubleArraysAlmostEqual(new double[] { 0.5, 0.5 }, opt.getOptimizationResponse().getSolution(), 1e-8);
//  }
//
//  @Override
//  protected JOptQuadraticOptimizer makeTestObject() {
//    return makeRealJOptQuadraticOptimizer();
//  }
//
//}
