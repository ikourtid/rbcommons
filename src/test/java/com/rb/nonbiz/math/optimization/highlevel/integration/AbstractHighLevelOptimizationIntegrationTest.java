package com.rb.nonbiz.math.optimization.highlevel.integration;

import com.rb.nonbiz.collections.Pair;
import com.rb.nonbiz.math.optimization.general.FeasibleOptimizationResult;
import com.rb.nonbiz.math.optimization.general.LinearOptimizationProgram;
import com.rb.nonbiz.math.optimization.general.LinearOptimizer;
import com.rb.nonbiz.math.optimization.highlevel.AbsoluteValueSuperVarsGenerator;
import com.rb.nonbiz.math.optimization.lpsolve.LpSolveLinearOptimizer;
import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.math.optimization.general.OptimizationResultTest.assumeFeasible;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelLPBuilderTest.makeRealHighLevelLPBuilder;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_LABEL;
import static com.rb.nonbiz.testutils.RBIntegrationTest.makeRealObject;

public abstract class AbstractHighLevelOptimizationIntegrationTest {

  protected AbsoluteValueSuperVarsGenerator absoluteValueSuperVarsGenerator =
      makeRealObject(AbsoluteValueSuperVarsGenerator.class);
  protected HighLevelVarEvaluator evaluator = 
      makeRealObject(HighLevelVarEvaluator.class);
  protected LinearOptimizer optimizer = 
      makeRealObject(LpSolveLinearOptimizer.class);

  protected void assertResults(HighLevelLPBuilder builder, Pair<HighLevelVar, Double>...expectedResults) {
    LinearOptimizationProgram lp = builder.build();
    FeasibleOptimizationResult solution = assumeFeasible(optimizer.minimize(lp));
    for (int i = 0; i < expectedResults.length; i++) {
      Pair<HighLevelVar, Double> expectedResult = expectedResults[i];
      assertEquals(
          Strings.format("item %s: %s", i, expectedResult),
          expectedResult.getRight(),
          evaluator.evaluateHighLevelVar(expectedResult.getLeft(), solution.getAllRawVariablesAndOptimalValues()),
          1e-8);
    }
  }

  protected HighLevelLPBuilder makeBuilder() {
    return makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(DUMMY_LABEL); // we need some label to avoid a null pointer exception.
  }

}
