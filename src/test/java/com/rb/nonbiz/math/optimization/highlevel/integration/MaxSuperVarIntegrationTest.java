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
import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMaxUsingDefaultWeight.createArtificialTermForMaxUsingDefaultWeight;
import static com.rb.nonbiz.math.optimization.highlevel.DoNotCreateArtificialTermForMax.doNotCreateArtificialTermForMax;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelLPBuilderTest.makeRealHighLevelLPBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.highLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.singleVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.sumOfDisjointHighLevelVars;
import static com.rb.nonbiz.math.optimization.lpsolve.LpSolveLinearOptimizerTest.makeRealLpSolveLinearOptimizer;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static org.junit.Assert.assertEquals;

public class MaxSuperVarIntegrationTest extends RBIntegrationTest<MaxSuperVarGenerator> {

  private HighLevelVarEvaluator evaluator = makeRealObject(HighLevelVarEvaluator.class);

  @Test
  public void simplestTestUsingMax() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("minimize max(x, y) subject to 0 <= x, y <= 1 and x + y = 1"));
    HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
    // We do not create artificial objective function terms for the maxXY supervar,
    // because the objective function already tries to minimize maxXY, so we don't need an additional
    // artificial term to make that happen.
    SuperVar maxXY = makeRealObject().generateMax(
        lpBuilder.getHighLevelVariablesBuilder(),
        "max(x,y)", singleVarExpression(x), singleVarExpression(y), doNotCreateArtificialTermForMax());
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(maxXY)))
        .addConstraint("x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1.0)
        .build());
    ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
    assertEquals(0.5, evaluate.applyAsDouble(x), 1e-8);
    assertEquals(0.5, evaluate.applyAsDouble(y), 1e-8);
    assertEquals(0.5, evaluate.applyAsDouble(maxXY), 1e-8);
  }

  @Test
  public void simpleTestUsingMax() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("minimize max(3x, y) subject to 0 <= x, y <= 1 and x + y = 1"));
    HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
    // We do not create artificial objective function terms for the max3XandY supervar,
    // because the objective function already tries to minimize max3XandY, so we don't need an additional
    // artificial term to make that happen.
    SuperVar max3XandY = makeRealObject().generateMax(
        lpBuilder.getHighLevelVariablesBuilder(),
        "max(3x,y)", singleVarExpression(3, x), singleVarExpression(y), doNotCreateArtificialTermForMax());
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(max3XandY)))
        .addConstraint("x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1.0)
        .build());
    ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
    assertEquals(0.25, evaluate.applyAsDouble(x), 1e-8);
    assertEquals(0.75, evaluate.applyAsDouble(y), 1e-8);
    assertEquals(0.75, evaluate.applyAsDouble(max3XandY), 1e-8);
  }

  @Test
  public void simpleTestUsingMax_expressionsHaveSomeConstants_maxIsRightExpression() {
    simpleTestUsingMax_expressionsHaveSomeConstants_helper(false);
  }

  @Test
  public void simpleTestUsingMax_expressionsHaveSomeConstants_maxIsLeftExpression() {
    simpleTestUsingMax_expressionsHaveSomeConstants_helper(true);
  }

  private void simpleTestUsingMax_expressionsHaveSomeConstants_helper(boolean threeXplus7comesFirst) {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("minimize max(3x+7, y+8) subject to 0 <= x, y <= 1 and x + y = 1"));
    HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
    HighLevelVarExpression threeXplus7 = highLevelVarExpression(3, x, 7);
    HighLevelVarExpression yPlus8 = highLevelVarExpression(1, y, 8);
    SuperVar maxXY = makeRealObject().generateMax(
        lpBuilder.getHighLevelVariablesBuilder(),
        "max(x,y)",
        threeXplus7comesFirst ? threeXplus7 : yPlus8,
        threeXplus7comesFirst ? yPlus8 : threeXplus7,
        doNotCreateArtificialTermForMax());
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(maxXY)))
        .addConstraint(
            "x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1.0)
        .build());
    ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
    assertEquals(0.5, evaluate.applyAsDouble(x), 1e-8);
    assertEquals(0.5, evaluate.applyAsDouble(y), 1e-8);
    assertEquals(doubleExplained(8.5, Math.max(3 * 0.5 + 7, 0.5 + 8)), evaluate.applyAsDouble(maxXY), 1e-8);
  }

  @Test
  public void simpleMax_maxDoesNotParticipateInObjective() {
    // This test has 2 scenarios so we can juxtapose the results.
    // The curly braces create new scopes so we can avoid any variable overlaps.

    // Scenario 1: no max of any sort. Indicates that this minimization can get to its perfect answer
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
              simpleLinearObjectiveFunction(sumOfDisjointHighLevelVars(absDiffX.getAbsoluteValue(), absDiffY.getAbsoluteValue())))
          .addConstraint(
              "x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1)
          .build());
      ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
      assertEquals(0.6, evaluate.applyAsDouble(x), 1e-8);
      assertEquals(0.4, evaluate.applyAsDouble(y), 1e-8);
      assertEquals(0.0, evaluate.applyAsDouble(absDiffX.getAbsoluteValue()), 1e-8);
      assertEquals(0.0, evaluate.applyAsDouble(absDiffY.getAbsoluteValue()), 1e-8);
    }

    // Scenario 2: Same, but with the additional constraint that neither x nor y can get bigger than 0.57,
    // i.e. max(x, y) <= 0.57. This minimization cannot get to its perfect answer, so the best it can do is
    // where x = 0.57, y = 0.43
    {
      HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
          .withHumanReadableLabel(label("minimize abs(x - 0.6) + abs(y - 0.4) s.t. 0 <= x, y <= 1, x + y = 1, max(x, y) <= 0.57"));
      HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
      RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
      RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
      AbsoluteValueSuperVarsGenerator absGenerator = RBIntegrationTest.makeRealObject(AbsoluteValueSuperVarsGenerator.class);
      AbsoluteValueSuperVars absDiffX = absGenerator.generateDumbAbsoluteValueSuperVars(
          lpBuilder.getHighLevelVariablesBuilder(), "abs(x - 0.6)", highLevelVarExpression(1.0, x, -0.6));
      AbsoluteValueSuperVars absDiffY = absGenerator.generateDumbAbsoluteValueSuperVars(
          lpBuilder.getHighLevelVariablesBuilder(), "abs(y - 0.4)", highLevelVarExpression(1.0, y, -0.4));
      MaxSuperVar maxXY = makeRealObject().generateMax(
          lpBuilder.getHighLevelVariablesBuilder(), "max(x, y)", singleVarExpression(x), singleVarExpression(y),
          createArtificialTermForMaxUsingDefaultWeight(RBIntegrationTest.makeRealObject(RBCommonsConstants.class)));
      AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
          .setObjectiveFunction(
              simpleLinearObjectiveFunction(sumOfDisjointHighLevelVars(absDiffX.getAbsoluteValue(), absDiffY.getAbsoluteValue())))
          .addConstraint("x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1)
          .addConstraint("max(x, y) <= 0.57", singleVarExpression(maxXY), LESS_THAN_SCALAR, 0.57)
          .build());
      ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
      double actual = evaluate.applyAsDouble(x);
      assertEquals(0.57, actual, 1e-8);
      assertEquals(0.43, evaluate.applyAsDouble(y), 1e-8);
      assertEquals(0.03, evaluate.applyAsDouble(absDiffX.getAbsoluteValue()), 1e-8);
      assertEquals(0.03, evaluate.applyAsDouble(absDiffY.getAbsoluteValue()), 1e-8);
    }

    // Scenario 3: Like #2, but reversing constrain direction to max(x, y) >= 0.57.
    // The optimization can get to its perfect answer of x = 0.6, y = 0.4; the constraint doesn't affect things.
    // In particular, the fact that y = 0.4 doesn't matter, because x (the max of x and y) is still above 0.57.
    {
      HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
          .withHumanReadableLabel(label("minimize abs(x - 0.6) + abs(y - 0.4) s.t. 0 <= x, y <= 1, x + y = 1, max(x, y) >= 0.57"));
      HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
      RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
      RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
      AbsoluteValueSuperVarsGenerator absGenerator = RBIntegrationTest.makeRealObject(AbsoluteValueSuperVarsGenerator.class);
      AbsoluteValueSuperVars absDiffX = absGenerator.generateDumbAbsoluteValueSuperVars(
          varBuilder, "abs(x - 0.6)", highLevelVarExpression(1.0, x, -0.6));
      AbsoluteValueSuperVars absDiffY = absGenerator.generateDumbAbsoluteValueSuperVars(
          varBuilder, "abs(y - 0.4)", highLevelVarExpression(1.0, y, -0.4));
      MaxSuperVar maxXY = makeRealObject().generateMax(
          varBuilder, "max(x, y)", singleVarExpression(x), singleVarExpression(y),
          createArtificialTermForMaxUsingDefaultWeight(RBIntegrationTest.makeRealObject(RBCommonsConstants.class)));
      AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
          .setObjectiveFunction(
              simpleLinearObjectiveFunction(sumOfDisjointHighLevelVars(absDiffX.getAbsoluteValue(), absDiffY.getAbsoluteValue())))
          .addConstraint("x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1)
          .addConstraint("max(x, y) <= 0.57", singleVarExpression(maxXY), GREATER_THAN_SCALAR, 0.57)
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
  public void minimizeMaxOfTwoAbs() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("minimize max(abs(x - 0.6), abs(y - 0.4)) s.t. 0 <= x, y <= 1, x + y = 1, y >= 0.48"));
    HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.closed(0.0, 1.0));
    AbsoluteValueSuperVarsGenerator absGenerator = RBIntegrationTest.makeRealObject(AbsoluteValueSuperVarsGenerator.class);
    AbsoluteValueSuperVars absDiffX = absGenerator.generateDumbAbsoluteValueSuperVars(
        varBuilder, "abs(x - 0.6)", highLevelVarExpression(1.0, x, -0.6));
    AbsoluteValueSuperVars absDiffY = absGenerator.generateDumbAbsoluteValueSuperVars(
        varBuilder, "abs(y - 0.4)", highLevelVarExpression(1.0, y, -0.4));
    MaxSuperVar maxOfTwoAbs = makeRealObject().generateMax(
        varBuilder, "max(abs(x - 0.6), abs(y - 0.4)",
        absDiffX.getAbsoluteValue().getHighLevelVarExpression(),
        absDiffY.getAbsoluteValue().getHighLevelVarExpression(),
        doNotCreateArtificialTermForMax());
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
        .setObjectiveFunction(
            simpleLinearObjectiveFunction(maxOfTwoAbs.getHighLevelVarExpression()))
        .addConstraint("x + y = 1", sumOfDisjointHighLevelVars(x, y), EQUAL_TO_SCALAR, 1)
        .addConstraint("y >= 0.48", singleVarExpression(y), GREATER_THAN_SCALAR, 0.48)
        .build());
    ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
    double actual = evaluate.applyAsDouble(x);
    assertEquals(0.52, actual, 1e-8);
    assertEquals(0.48, evaluate.applyAsDouble(y), 1e-8);
    assertEquals(0.08, evaluate.applyAsDouble(absDiffX.getAbsoluteValue()), 1e-8);
    assertEquals(0.08, evaluate.applyAsDouble(absDiffY.getAbsoluteValue()), 1e-8);
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
  protected Class<MaxSuperVarGenerator> getClassBeingTested() {
    return MaxSuperVarGenerator.class;
  }

}
