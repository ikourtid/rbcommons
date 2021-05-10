package com.rb.nonbiz.math.optimization.highlevel.integration;

import com.google.common.collect.Range;
import com.rb.biz.investing.modeling.RBCommonsConstants;
import com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues;
import com.rb.nonbiz.math.optimization.general.LinearOptimizationProgram;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.testutils.RBIntegrationTest;
import org.junit.Test;

import java.util.function.ToDoubleFunction;

import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.EQUAL_TO_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.GREATER_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.LESS_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.OptimizationResultTest.assumeFeasible;
import static com.rb.nonbiz.math.optimization.general.SimpleLinearObjectiveFunction.simpleLinearObjectiveFunction;
import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMinUsingDefaultWeight.createArtificialTermForMinUsingDefaultWeight;
import static com.rb.nonbiz.math.optimization.highlevel.DoNotCreateArtificialTermForMin.doNotCreateArtificialTermForMin;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelLPBuilderTest.makeRealHighLevelLPBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.highLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.singleVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.sumOfDisjointHighLevelVars;
import static com.rb.nonbiz.math.optimization.lpsolve.LpSolveLinearOptimizerTest.makeRealLpSolveLinearOptimizer;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static org.junit.Assert.assertEquals;

public class MinSuperVarIntegrationTest extends RBIntegrationTest<MinSuperVarGenerator> {

  private HighLevelVarEvaluator evaluator = makeRealObject(HighLevelVarEvaluator.class);

  @Test
  public void simplestTestUsingMin() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("maximize min(x, y) subject to 0 <= x, y <= 1 and x + y = 1"));
    HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
    // We do not create artificial objective function terms for the minXY supervar,
    // because the objective function already tries to minimize minXY, so we don't need an additional
    // artificial term to make that happen.
    SuperVar minXY = makeRealObject().generateMin(
        lpBuilder.getHighLevelVariablesBuilder(),
        "min(x,y)", singleVarExpression(x), singleVarExpression(y), doNotCreateArtificialTermForMin());
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(-1, minXY)))
        .addConstraint("x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1.0)
        .build());
    ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
    assertEquals(0.5, evaluate.applyAsDouble(x), 1e-8);
    assertEquals(0.5, evaluate.applyAsDouble(y), 1e-8);
    assertEquals(0.5, evaluate.applyAsDouble(minXY), 1e-8);
  }

  @Test
  public void simpleTestUsingMin() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("maximize min(3x, y) subject to 0 <= x, y <= 1 and x + y = 1"));
    HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
    // We do not create artificial objective function terms for the min3XandY supervar,
    // because the objective function already tries to minimize min3XandY, so we don't need an additional
    // artificial term to make that happen.
    SuperVar min3XandY = makeRealObject().generateMin(
        lpBuilder.getHighLevelVariablesBuilder(),
        "min(3x,y)", singleVarExpression(3, x), singleVarExpression(y), doNotCreateArtificialTermForMin());
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(-1, min3XandY)))
        .addConstraint("x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1.0)
        .build());
    ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
    assertEquals(0.25, evaluate.applyAsDouble(x), 1e-8);
    assertEquals(0.75, evaluate.applyAsDouble(y), 1e-8);
    assertEquals(0.75, evaluate.applyAsDouble(min3XandY), 1e-8);
  }

  @Test
  public void simpleTestUsingMin_expressionsHaveSomeConstants_minIsRightExpression() {
    simpleTestUsingMin_expressionsHaveSomeConstants_helper(false);
  }

  @Test
  public void simpleTestUsingMin_expressionsHaveSomeConstants_minIsLeftExpression() {
    simpleTestUsingMin_expressionsHaveSomeConstants_helper(true);
  }

  private void simpleTestUsingMin_expressionsHaveSomeConstants_helper(boolean threeXplus7comesFirst) {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("maximize min(3x+7, y+8) subject to 0 <= x, y <= 1 and x + y = 1"));
    HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
    HighLevelVarExpression threeXplus7 = highLevelVarExpression(3, x, 7);
    HighLevelVarExpression yPlus8 = highLevelVarExpression(1, y, 8);
    SuperVar minXY = makeRealObject().generateMin(
        lpBuilder.getHighLevelVariablesBuilder(),
        "min(x,y)",
        threeXplus7comesFirst ? threeXplus7 : yPlus8,
        threeXplus7comesFirst ? yPlus8 : threeXplus7,
        doNotCreateArtificialTermForMin());
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(-1, minXY)))
        .addConstraint(
            "x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1.0)
        .build());
    ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
    assertEquals(0.5, evaluate.applyAsDouble(x), 1e-8);
    assertEquals(0.5, evaluate.applyAsDouble(y), 1e-8);
    assertEquals(doubleExplained(8.5, Math.min(3 * 0.5 + 7, 0.5 + 8)), evaluate.applyAsDouble(minXY), 1e-8);
  }

  @Test
  public void simpleMin_minDoesNotParticipateInObjective() {
    // This test has 2 scenarios so we can juxtapose the results.
    // The curly braces create new scopes so we can avoid any variable overlaps.

    // Scenario 1: no min of any sort. Indicates that this minimization can get to its perfect answer
    // where x = 0.6, y = 0.4
    {
      HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
          .withHumanReadableLabel(label("minimize abs(x - 0.6) + abs(y - 0.4) subject to 0 <= x, y <= 1 and x + y = 1"));
      HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
      RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
      RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
      AbsoluteValueSuperVarsGenerator absGenerator = RBIntegrationTest.makeRealObject(AbsoluteValueSuperVarsGenerator.class);
      AbsoluteValueSuperVars absDiffX = absGenerator.generateSmartAbsoluteValueSuperVars(
          lpBuilder.getHighLevelVariablesBuilder(), "abs(x - 0.6)", highLevelVarExpression(1.0, x, -0.6), Range.closed(0.0, 1.0));
      AbsoluteValueSuperVars absDiffY = absGenerator.generateSmartAbsoluteValueSuperVars(
          lpBuilder.getHighLevelVariablesBuilder(), "abs(y - 0.4)", highLevelVarExpression(1.0, y, -0.4), Range.closed(0.0, 1.0));
      AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
          .setObjectiveFunction(
              simpleLinearObjectiveFunction(
                  sumOfDisjointHighLevelVars(absDiffX.getAbsoluteValue(), absDiffY.getAbsoluteValue())))
          .addConstraint(
              "x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1)
          .build());
      ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
      assertEquals(0.6, evaluate.applyAsDouble(x), 1e-8);
      assertEquals(0.4, evaluate.applyAsDouble(y), 1e-8);
      assertEquals(0.0, evaluate.applyAsDouble(absDiffX.getAbsoluteValue()), 1e-8);
      assertEquals(0.0, evaluate.applyAsDouble(absDiffY.getAbsoluteValue()), 1e-8);
    }

    // Scenario 2: Same, but with the additional constraint that neither x nor y can get smaller than 0.43,
    // i.e. min(x, y) >= 0.43. This minimization cannot get to its perfect answer, so the best it can do is
    // where x = 0.57, y = 0.43
    {
      HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
          .withHumanReadableLabel(label("minimize abs(x - 0.6) + abs(y - 0.4) s.t. 0 <= x, y <= 1, x + y = 1, min(x, y) >= 0.43"));
      HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
      RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
      RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
      AbsoluteValueSuperVarsGenerator absGenerator = RBIntegrationTest.makeRealObject(AbsoluteValueSuperVarsGenerator.class);
      AbsoluteValueSuperVars absDiffX = absGenerator.generateDumbAbsoluteValueSuperVars(
          lpBuilder.getHighLevelVariablesBuilder(), "abs(x - 0.6)", highLevelVarExpression(1.0, x, -0.6));
      AbsoluteValueSuperVars absDiffY = absGenerator.generateDumbAbsoluteValueSuperVars(
          lpBuilder.getHighLevelVariablesBuilder(), "abs(y - 0.4)", highLevelVarExpression(1.0, y, -0.4));
      MinSuperVar minXY = makeRealObject().generateMin(
          lpBuilder.getHighLevelVariablesBuilder(), "min(x, y)", singleVarExpression(x), singleVarExpression(y),
          createArtificialTermForMinUsingDefaultWeight(RBIntegrationTest.makeRealObject(RBCommonsConstants.class)));
      AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
          .setObjectiveFunction(
              simpleLinearObjectiveFunction(sumOfDisjointHighLevelVars(absDiffX.getAbsoluteValue(), absDiffY.getAbsoluteValue())))
          .addConstraint("x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1)
          .addConstraint("min(x, y) >= 0.43", singleVarExpression(minXY), GREATER_THAN_SCALAR, 0.43)
          .build());
      ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
      double actual = evaluate.applyAsDouble(x);
      assertEquals(0.57, actual, 1e-8);
      assertEquals(0.43, evaluate.applyAsDouble(y), 1e-8);
      assertEquals(0.03, evaluate.applyAsDouble(absDiffX.getAbsoluteValue()), 1e-8);
      assertEquals(0.03, evaluate.applyAsDouble(absDiffY.getAbsoluteValue()), 1e-8);
    }

    // Scenario 3: Like #2, but reversing constrain direction to min(x, y) <= 0.43.
    // The optimization can get to its perfect answer of x = 0.6, y = 0.4; the constraint doesn't affect things.
    // In particular, the fact that x = 0.6 doesn't matter, because y (the min of x and y) is still below 0.43.
    {
      HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
          .withHumanReadableLabel(label("minimize abs(x - 0.6) + abs(y - 0.4) s.t. 0 <= x, y <= 1, x + y = 1, min(x, y) <= 0.43"));
      HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
      RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
      RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
      AbsoluteValueSuperVarsGenerator absGenerator = RBIntegrationTest.makeRealObject(AbsoluteValueSuperVarsGenerator.class);
      AbsoluteValueSuperVars absDiffX = absGenerator.generateDumbAbsoluteValueSuperVars(
          lpBuilder.getHighLevelVariablesBuilder(), "abs(x - 0.6)", highLevelVarExpression(1.0, x, -0.6));
      AbsoluteValueSuperVars absDiffY = absGenerator.generateDumbAbsoluteValueSuperVars(
          lpBuilder.getHighLevelVariablesBuilder(), "abs(y - 0.4)", highLevelVarExpression(1.0, y, -0.4));
      MinSuperVar minXY = makeRealObject().generateMin(
          lpBuilder.getHighLevelVariablesBuilder(), "min(x, y)", singleVarExpression(x), singleVarExpression(y),
          createArtificialTermForMinUsingDefaultWeight(RBIntegrationTest.makeRealObject(RBCommonsConstants.class)));
      AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
          .setObjectiveFunction(
              simpleLinearObjectiveFunction(sumOfDisjointHighLevelVars(absDiffX.getAbsoluteValue(), absDiffY.getAbsoluteValue())))
          .addConstraint("x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1)
          .addConstraint("min(x, y) <= 0.43", singleVarExpression(minXY), LESS_THAN_SCALAR, 0.43)
          .build());
      ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
      double actual = evaluate.applyAsDouble(x);
      assertEquals(0.6, actual, 1e-8);
      assertEquals(0.4, evaluate.applyAsDouble(y), 1e-8);
      assertEquals(0.0, evaluate.applyAsDouble(absDiffX.getAbsoluteValue()), 1e-8);
      assertEquals(0.0, evaluate.applyAsDouble(absDiffY.getAbsoluteValue()), 1e-8);
    }
  }

  @Test
  public void maximizeMin() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("maximize min(x, y) s.t. 0 <= x, y <= 1, x + y = 1, y <= 0.48"));
    HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
    MinSuperVar minOfTwoAbs = makeRealObject().generateMin(
        lpBuilder.getHighLevelVariablesBuilder(),
        "min(x, y)",
        singleVarExpression(x),
        singleVarExpression(y),
        doNotCreateArtificialTermForMin());
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
        .setObjectiveFunction(
            simpleLinearObjectiveFunction(minOfTwoAbs.getHighLevelVarExpression().negate()))
        .addConstraint("x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1)
        .addConstraint("y <= 0.48", singleVarExpression(y), LESS_THAN_SCALAR, 0.48)
        .build());
    ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
    assertEquals(0.52, evaluate.applyAsDouble(x), 1e-8);
    assertEquals(0.48, evaluate.applyAsDouble(y), 1e-8);
    assertEquals(0.48, evaluate.applyAsDouble(minOfTwoAbs), 1e-8);
  }

  private AllRawVariablesAndOptimalValues calculateSolution(LinearOptimizationProgram lp) {
    return assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lp)).getAllRawVariablesAndOptimalValues();
  }

  private RBCommonsConstants makeConstantsObject(double value) {
    return new RBCommonsConstants() {
      @Override
      public double getDefaultWeightForMinAndMaxArtificialTerms() {
        return value;
      }
    };
  }

  @Override
  protected Class<MinSuperVarGenerator> getClassBeingTested() {
    return MinSuperVarGenerator.class;
  }

}
