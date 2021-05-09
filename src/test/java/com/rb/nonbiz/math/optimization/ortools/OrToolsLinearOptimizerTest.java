package com.rb.nonbiz.math.optimization.ortools;

/**
 * All the code below is commented out because it is not currently (Sept 2019) used,
 * but could be useful in the future.
 * @see OrToolsLinearOptimizer
 */
/*
import com.google.common.collect.Range;
import com.google.ortools.linearsolver.MPSolver;
import com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder;
import com.rb.nonbiz.math.optimization.general.OptimizationResult;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.testutils.RBIntegrationTest;
import org.hamcrest.TypeSafeMatcher;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.google.ortools.linearsolver.MPSolver.ResultStatus.OPTIMAL;
import static com.rb.biz.investing.strategy.optbased.rebal.lp.NormalizedObjectiveValue.normalizedObjectiveValue;
import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
import static com.rb.nonbiz.collections.RBMaps.rbMapOf;
import static com.rb.nonbiz.math.optimization.general.AllArtificialObjectiveFunctionTerms.allArtificialObjectiveFunctionTerms;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues.allRawVariablesAndOptimalValues;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.allRawVariablesInOrder;
import static com.rb.nonbiz.math.optimization.general.FeasibleOptimizationResult.feasibleOptimizationResult;
import static com.rb.nonbiz.math.optimization.general.LPBuilder.lpBuilder;
import static com.rb.nonbiz.math.optimization.general.LinearObjectiveFunctionWithArtificialTerms.linearObjectiveFunctionWithArtificialTerms;
import static com.rb.nonbiz.math.optimization.general.LinearObjectiveFunctionWithArtificialTermsTest.linearObjectiveFunctionWithNoArtificialTerms;
import static com.rb.nonbiz.math.optimization.general.OptimizationResultTest.optimizationResultMatcher;
import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.general.SimpleLinearObjectiveFunction.simpleLinearObjectiveFunction;
import static com.rb.nonbiz.math.optimization.general.WeightedRawVariables.singletonWeightedRawVariables;
import static com.rb.nonbiz.math.optimization.general.WeightedRawVariables.weightedRawVariables;
import static com.rb.nonbiz.math.optimization.highlevel.FlattenedRawVarExpression.flattenedExpressionOfSingleRawVar;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.disjointHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.singleVarExpression;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.types.Weighted.weighted;
import static org.hamcrest.MatcherAssert.assertThat;
*/

public class OrToolsLinearOptimizerTest { /* Issue #1003     extends RBIntegrationTest<OrToolsLinearOptimizer> {

  @BeforeClass
  public static void setup() {
    System.loadLibrary("jniortools");
  }

  @Test
  public void singleVariableLp_works() {
    // e.g. minimize 3x subject to 4 <= x <= 5
    assertSingleVarLp( 3, Range.closed( 4.0,  5.0),  4.0, doubleExplained( 12.0,  3.0 *  4.0));
    assertSingleVarLp( 3, Range.closed(-5.0, -4.0), -5.0, doubleExplained(-15.0,  3.0 * -5.0));
    assertSingleVarLp( 3, Range.closed(-5.0,  4.0), -5.0, doubleExplained(-15.0,  3.0 * -5.0));
    assertSingleVarLp(-3, Range.closed( 4.0,  5.0),  5.0, doubleExplained(-15.0, -3.0 *  5.0));
    assertSingleVarLp(-3, Range.closed(-5.0, -4.0), -4.0, doubleExplained( 12.0, -3.0 * -4.0));
    assertSingleVarLp(-3, Range.closed(-5.0,  4.0),  4.0, doubleExplained(-12.0, -3.0 *  4.0));

    // closed / open behaves the same in LpSolve
    assertSingleVarLp( 3, Range.openClosed( 4.0,  5.0),  4.0, doubleExplained( 12.0,  3.0 *  4.0));
    assertSingleVarLp( 3, Range.openClosed(-5.0, -4.0), -5.0, doubleExplained(-15.0,  3.0 * -5.0));
    assertSingleVarLp( 3, Range.openClosed(-5.0,  4.0), -5.0, doubleExplained(-15.0,  3.0 * -5.0));
    assertSingleVarLp(-3, Range.openClosed( 4.0,  5.0),  5.0, doubleExplained(-15.0, -3.0 *  5.0));
    assertSingleVarLp(-3, Range.openClosed(-5.0, -4.0), -4.0, doubleExplained( 12.0, -3.0 * -4.0));
    assertSingleVarLp(-3, Range.openClosed(-5.0,  4.0),  4.0, doubleExplained(-12.0, -3.0 *  4.0));

    assertSingleVarLp( 3, Range.closedOpen( 4.0,  5.0),  4.0, doubleExplained( 12.0,  3.0 *  4.0));
    assertSingleVarLp( 3, Range.closedOpen(-5.0, -4.0), -5.0, doubleExplained(-15.0,  3.0 * -5.0));
    assertSingleVarLp( 3, Range.closedOpen(-5.0,  4.0), -5.0, doubleExplained(-15.0,  3.0 * -5.0));
    assertSingleVarLp(-3, Range.closedOpen( 4.0,  5.0),  5.0, doubleExplained(-15.0, -3.0 *  5.0));
    assertSingleVarLp(-3, Range.closedOpen(-5.0, -4.0), -4.0, doubleExplained( 12.0, -3.0 * -4.0));
    assertSingleVarLp(-3, Range.closedOpen(-5.0,  4.0),  4.0, doubleExplained(-12.0, -3.0 *  4.0));

    assertSingleVarLp( 3, Range.open( 4.0,  5.0),  4.0, doubleExplained( 12.0,  3.0 *  4.0));
    assertSingleVarLp( 3, Range.open(-5.0, -4.0), -5.0, doubleExplained(-15.0,  3.0 * -5.0));
    assertSingleVarLp( 3, Range.open(-5.0,  4.0), -5.0, doubleExplained(-15.0,  3.0 * -5.0));
    assertSingleVarLp(-3, Range.open( 4.0,  5.0),  5.0, doubleExplained(-15.0, -3.0 *  5.0));
    assertSingleVarLp(-3, Range.open(-5.0, -4.0), -4.0, doubleExplained( 12.0, -3.0 * -4.0));
    assertSingleVarLp(-3, Range.open(-5.0,  4.0),  4.0, doubleExplained(-12.0, -3.0 *  4.0));
  }

  private void assertSingleVarLp(double objectiveCoeff, Range<Double> varRange, double optimalX, double optimalObjective) {
    RawVariable x = rawVariable("x", 0);
    AllRawVariablesInOrder vars = allRawVariablesInOrder(x);
    TypeSafeMatcher<OptimizationResult> expectedMatcher = optimizationResultMatcher(
        feasibleOptimizationResult(
            allRawVariablesAndOptimalValues(vars, new double[] { optimalX }),
            intExplained(0, OPTIMAL.swigValue()),
            normalizedObjectiveValue(optimalObjective)));
    assertThat(
        makeRealObject().minimize(lpBuilder(vars)
            .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithNoArtificialTerms(
                simpleLinearObjectiveFunction(singleVarExpression(objectiveCoeff, x))))
            .withVariableRange(x, varRange)
            .build()),
        expectedMatcher);
    // Also, try a formulation where there are general constraints, not the special-cased variable ranges
    assertThat(
        makeRealObject().minimize(lpBuilder(vars)
            .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithNoArtificialTerms(
                simpleLinearObjectiveFunction(singleVarExpression(objectiveCoeff, x))))
            .withVariableCombinationGreaterThanScalar("min <= x", singletonWeightedRawVariables(x, 1.0), varRange.lowerEndpoint())
            .withVariableCombinationLessThanScalar(   "x <= max", singletonWeightedRawVariables(x, 1.0), varRange.upperEndpoint())
            .build()),
        expectedMatcher);
  }

  @Test
  public void simpleLp_solvesCorrectly_stateOfLpIsCorrect() {
    // minimize 3x + 5y subject to 1 <= x <= 2 and 6 <= y <= 7
    RawVariable x = rawVariable("x", 0);
    RawVariable y = rawVariable("y", 1);
    // this uses the high-level vars infrastructure, which is too upstream of this, but I want to create a
    // realistic situation where there is an artificial term, so that the tests in this file are general
    // and don't only incorporate situations with no artificial objective function terms.
    RawVariable maxXY = rawVariable("maxXY", 2);
    AllRawVariablesInOrder vars = allRawVariablesInOrder(x, y, maxXY);
    assertThat(
        makeRealObject().minimize(lpBuilder(vars)
            .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithArtificialTerms(
                simpleLinearObjectiveFunction(disjointHighLevelVarExpression(3.0, x, 5.0, y)),
                allArtificialObjectiveFunctionTerms(flattenedExpressionOfSingleRawVar(weighted(maxXY, 1e-6)))))
            .withVariableCombinationGreaterThanScalar(
                "max(x,y) >= x", weightedRawVariables(doubleMap(rbMapOf(maxXY, 1.0, x, -1.0))), 0.0)
            .withVariableCombinationGreaterThanScalar(
                "max(x,y) >= y", weightedRawVariables(doubleMap(rbMapOf(maxXY, 1.0, y, -1.0))), 0.0)
            .withVariableRange(x, Range.closed(1.0, 2.0))
            .withVariableRange(y, Range.closed(6.0, 7.0))
            .build()),
        optimizationResultMatcher(
            feasibleOptimizationResult(
                allRawVariablesAndOptimalValues(vars, new double[] { 1.0, 6.0, 6.0 }), // x, y, max(x, y)
                intExplained(0, OPTIMAL.swigValue()),
                // should not care about the artificial term
                normalizedObjectiveValue(doubleExplained(33.0, 3.0 * 1.0 + 5.0 * 6.0)))));
  }

  @Test
  public void simpleLp_2_solvesCorrectly_stateOfLpIsCorrect() {
    // minimize 3x - 5y subject to 1 <= x <= 2 and -7 <= y <= -6
    RawVariable x = rawVariable("x", 0);
    RawVariable y = rawVariable("y", 1);
    AllRawVariablesInOrder vars = allRawVariablesInOrder(x, y);
    assertThat(
        makeRealObject().minimize(lpBuilder(vars)
            .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithNoArtificialTerms(
                simpleLinearObjectiveFunction(
                    disjointHighLevelVarExpression(3.0, x, -5.0, y))))
            .withVariableRange(x, Range.closed(1.0, 2.0))
            .withVariableRange(y, Range.closed(-7.0, -6.0))
            .build()),
        optimizationResultMatcher(
            feasibleOptimizationResult(
                allRawVariablesAndOptimalValues(vars, new double[] { 1.0, -6.0 }),
                intExplained(0, OPTIMAL.swigValue()),
                normalizedObjectiveValue(doubleExplained(33.0, 3.0 * 1.0 - 5.0 * (-6.0))))));
  }

  @Override
  protected Class<OrToolsLinearOptimizer> getClassBeingTested() {
    return OrToolsLinearOptimizer.class;
  }

  public static OrToolsLinearOptimizer makeRealOrToolsLinearOptimizer() {
    return makeRealObject(OrToolsLinearOptimizer.class);
  }
  */

}

