//package com.rb.nonbiz.math.optimization.jopt;
//
//import com.rb.nonbiz.math.optimization.general.QuadraticObjectiveFunctionEvaluator;
//
//public class JOptQuadraticOptimizerTest {
//
//  public static JOptQuadraticOptimizer makeRealJOptQuadraticOptimizer() {
//    JOptQPInputConverterImpl jOptQPInputConverter = new JOptQPInputConverterImpl();
//    jOptQPInputConverter.jOptQPConstraintsInputConverter = new JOptQPConstraintsInputConverter();
//
//    JOptQuadraticOptimizer jOptQuadraticOptimizer = new JOptQuadraticOptimizer();
//    jOptQuadraticOptimizer.jOptQPInputConverter = jOptQPInputConverter;
//
//    JOptQPOutputConverterImpl jOptQPOutputConverter = new JOptQPOutputConverterImpl();
//    jOptQPOutputConverter.quadraticObjectiveFunctionEvaluator = new QuadraticObjectiveFunctionEvaluator();
//
//    jOptQuadraticOptimizer.jOptQPOutputConverter = jOptQPOutputConverter;
//    return jOptQuadraticOptimizer;
//  }
//
//}
