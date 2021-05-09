package com.rb.nonbiz.math.optimization.highlevel.integration;

import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues;
import com.rb.nonbiz.math.optimization.general.FeasibleOptimizationResult;
import com.rb.nonbiz.math.optimization.general.LinearOptimizationProgram;
import com.rb.nonbiz.math.optimization.highlevel.*;
import org.junit.Test;

import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.EQUAL_TO_SCALAR;
import static com.rb.nonbiz.math.optimization.general.OptimizationResultTest.assumeFeasible;
import static com.rb.nonbiz.math.optimization.general.SimpleLinearObjectiveFunction.simpleLinearObjectiveFunction;
import static com.rb.nonbiz.math.optimization.highlevel.ArbitraryFunctionDescriptor.arbitraryFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.highlevel.GeometricallyIncreasingRangesGenerationInstructions.GeometricallyIncreasingRangesGenerationInstructionsBuilder.geometricallyIncreasingRangesGenerationInstructionsBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelLPBuilderTest.makeRealHighLevelLPBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.highLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.sumOfDisjointHighLevelVars;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValues.linearApproximationVarRangesAndValues;
import static com.rb.nonbiz.math.optimization.highlevel.QuadraticFunctionDescriptor.quadraticFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.lpsolve.LpSolveLinearOptimizerTest.makeRealLpSolveLinearOptimizer;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBIntegrationTest.makeRealObject;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_LABEL;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class LinearApproximationVarsSimpleIntegrationTest {

  private final double COARSE_EPSILON = 0.025;

  HighLevelVarEvaluator evaluator = makeRealObject(HighLevelVarEvaluator.class);
  GeometricallyIncreasingLinearApproximationVarRangesGenerator geometricallyIncreasingLinearApproximationVarRangesGenerator =
      makeRealObject(GeometricallyIncreasingLinearApproximationVarRangesGenerator.class);
  LinearApproximationVarsGenerator linearApproximationVarsGenerator =
      new LinearApproximationVarsGenerator() {{
        this.linearApproximationExpressionsGenerator = new LinearApproximationExpressionsGenerator();
      }};

  @Test
  public void testSum_twoVariables_equalizesHeights_lowerBoundForVarsIsZero() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("minimize x^2 + y^2 subject to x + y = 1"));
    LinearApproximationVars x = generateVarsForSquare(lpBuilder, "x", closedRange(0.0, 1.0), 0.01, 1.2);
    LinearApproximationVars y = generateVarsForSquare(lpBuilder, "y", closedRange(0.0, 1.0), 0.01, 1.2);
    FeasibleOptimizationResult solution = assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(
            sumOfDisjointHighLevelVars(x.getApproximatedNonLinearPart(), y.getApproximatedNonLinearPart())))
        .addConstraint(
            "x + y = 1", sumOfDisjointHighLevelVars(x.getLinearPart(), y.getLinearPart()), EQUAL_TO_SCALAR, 1.0)
        .build()));
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = solution.getAllRawVariablesAndOptimalValues();
    double finalX = evaluator.evaluateHighLevelVar(x.getLinearPart(), variablesAndOptimalValues);
    double finalY = evaluator.evaluateHighLevelVar(y.getLinearPart(), variablesAndOptimalValues);
    // solution is x=0.5, y=0.5, which gives 0.5^2 + 0.5^2 = 0.25 + 0.25 = 0.5
    assertEquals(0.5, finalX, COARSE_EPSILON);
    assertEquals(0.5, finalY, COARSE_EPSILON);
    // Even though the X and Y will be approximate, their sum will not
    assertEquals(1, finalX + finalY, 1.01 * 1e-8);

    double finalXsquared = evaluator.evaluateHighLevelVar(x.getApproximatedNonLinearPart(), variablesAndOptimalValues);
    double finalYsquared = evaluator.evaluateHighLevelVar(y.getApproximatedNonLinearPart(), variablesAndOptimalValues);
    assertEquals(doubleExplained(0.25, 0.5 * 0.5), finalXsquared, COARSE_EPSILON);
    assertEquals(0.25, finalYsquared, COARSE_EPSILON);
  }

  @Test
  public void blogPostExample_testSum_twoVariables_unequalWeights_lowerBoundForVarsIsZero() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("minimize x^2 + 4*y^2 subject to x + y = 1"));
    LinearApproximationVars x = generateVarsForSquare(lpBuilder, "x", closedRange(0.0, 1.0), 0.01, 1.2);
    LinearApproximationVars y = generateVarsForSquare(lpBuilder, "y", closedRange(0.0, 1.0), 0.01, 1.2);
    LinearOptimizationProgram lp = lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(
            highLevelVarExpression(1, x.getApproximatedNonLinearPart(), 4, y.getApproximatedNonLinearPart())))
        .addConstraint(
            "x + y = 1", sumOfDisjointHighLevelVars(x.getLinearPart(), y.getLinearPart()), EQUAL_TO_SCALAR, 1.0)
        .build();
    AllRawVariablesAndOptimalValues variablesAndOptimalValues =
        assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lp)).getAllRawVariablesAndOptimalValues();
    System.out.println(lp);
    ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
    double finalX = evaluate.applyAsDouble(x.getLinearPart());
    double finalY = evaluate.applyAsDouble(y.getLinearPart());
    double COARSE_EPSILON = 0.025;
    assertEquals(0.8, finalX, COARSE_EPSILON);
    assertEquals(0.2, finalY, COARSE_EPSILON);
    // Even though the X and Y will be approximate, their sum will not, since we have a constraint of x + y = 1
    assertEquals(1, finalX + finalY, 1.01 * 1e-8);
    assertEquals(doubleExplained(0.64, 0.8 * 0.8), evaluate.applyAsDouble(x.getApproximatedNonLinearPart()), COARSE_EPSILON);
    assertEquals(doubleExplained(0.04, 0.2 * 0.2), evaluate.applyAsDouble(y.getApproximatedNonLinearPart()), COARSE_EPSILON);
  }

  @Test
  public void xToTheK_zeroTo1() {
    // For any x^k for 0 < k, the min should be 0, and min of -x^k should be -1^k
    // There's no point in ever approximating f(x)=x, but this is here as a sanity check.
    assertOptimalForApproximationOfSingleFunction(closedRange(0.0, 1.0), v ->  Math.sqrt(v), 0);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.0, 1.0), v -> v, 0);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.0, 1.0), v ->  Math.pow(v, 1.5), 0);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.0, 1.0), v ->  Math.pow(v, 2.0), 0);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.0, 1.0), v ->  Math.pow(v, 2.5), 0);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.0, 1.0), v ->  Math.pow(v, 3.0), 0);

    assertOptimalForApproximationOfSingleFunction(closedRange(0.0, 1.0), v -> -v, 1);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.0, 1.0), v -> -Math.sqrt(v), 1);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.0, 1.0), v -> -Math.pow(v, 1.5), 1);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.0, 1.0), v -> -Math.pow(v, 2.0), 1);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.0, 1.0), v -> -Math.pow(v, 3.5), 1);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.0, 1.0), v -> -Math.pow(v, 3.0), 1);
  }

  @Test
  public void xToTheK_tighterLimitsThan0To1() {
    // For any x^k for 0 < k in the range [0.2, 0.7],
    // the min should be at 0.2, and the min of -x^k should be at 0.7.
    // There's no point in ever approximating f(x)=x, but this is here as a sanity check.
    assertOptimalForApproximationOfSingleFunction(closedRange(0.2, 0.7), v ->  Math.sqrt(v), 0.2);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.2, 0.7), v -> v, 0.2);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.2, 0.7), v ->  Math.pow(v, 1.5), 0.2);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.2, 0.7), v ->  Math.pow(v, 2.0), 0.2);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.2, 0.7), v ->  Math.pow(v, 2.5), 0.2);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.2, 0.7), v ->  Math.pow(v, 3.0), 0.2);

    assertOptimalForApproximationOfSingleFunction(closedRange(0.2, 0.7), v -> -v, 0.7);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.2, 0.7), v -> -Math.sqrt(v), 0.7);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.2, 0.7), v -> -Math.pow(v, 1.5), 0.7);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.2, 0.7), v -> -Math.pow(v, 2.0), 0.7);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.2, 0.7), v -> -Math.pow(v, 3.5), 0.7);
    assertOptimalForApproximationOfSingleFunction(closedRange(0.2, 0.7), v -> -Math.pow(v, 3.0), 0.7);
  }

  @Test
  public void testSum_threeVariables_equalizesHeights_lowerBoundForVarsIsZero() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("minimize x^2 + y^2 + z^2 subject to x + y + z = 1"));
    LinearApproximationVars x = generateVarsForSquare(lpBuilder, "x", closedRange(0.0, 1.0), 0.01, 1.2);
    LinearApproximationVars y = generateVarsForSquare(lpBuilder, "y", closedRange(0.0, 1.0), 0.01, 1.2);
    LinearApproximationVars z = generateVarsForSquare(lpBuilder, "z", closedRange(0.0, 1.0), 0.01, 1.2);
    FeasibleOptimizationResult solution = assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(
            sumOfDisjointHighLevelVars(x.getApproximatedNonLinearPart(), y.getApproximatedNonLinearPart(), z.getApproximatedNonLinearPart())))
        .addConstraint("x + y + z = 1",
            sumOfDisjointHighLevelVars(x.getLinearPart(), y.getLinearPart(), z.getLinearPart()), EQUAL_TO_SCALAR, 1.0)
        .build()));
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = solution.getAllRawVariablesAndOptimalValues();
    double finalX = evaluator.evaluateHighLevelVar(x.getLinearPart(), variablesAndOptimalValues);
    double finalY = evaluator.evaluateHighLevelVar(y.getLinearPart(), variablesAndOptimalValues);
    double finalZ = evaluator.evaluateHighLevelVar(z.getLinearPart(), variablesAndOptimalValues);
    assertEquals(1 / 3.0, finalX, COARSE_EPSILON);
    assertEquals(1 / 3.0, finalY, COARSE_EPSILON);
    assertEquals(1 / 3.0, finalZ, COARSE_EPSILON);
    // Even though the X, Y, Z will be approximate, their sum will not
    assertEquals(1, finalX + finalY + finalZ, 1e-7);

    double finalXsquared = evaluator.evaluateHighLevelVar(x.getApproximatedNonLinearPart(), variablesAndOptimalValues);
    double finalYsquared = evaluator.evaluateHighLevelVar(y.getApproximatedNonLinearPart(), variablesAndOptimalValues);
    double finalZsquared = evaluator.evaluateHighLevelVar(z.getApproximatedNonLinearPart(), variablesAndOptimalValues);
    assertEquals(doubleExplained(0.11111111, (1 / 3.0) * (1 / 3.0)), finalXsquared, COARSE_EPSILON);
    assertEquals(0.11111111, finalYsquared, COARSE_EPSILON);
    assertEquals(0.11111111, finalZsquared, COARSE_EPSILON);
  }

  @Test
  public void testSum_threeVariables_equalizesHeightsForXandY_lowerBoundForZisAboveIdeal() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label(
            "minimize x^2 + y^2 + z^2 subject to x + y + z = 1 & 0.5 < z"));
    LinearApproximationVars x = generateVarsForSquare(lpBuilder, "x", closedRange(0.0, 1.0), 0.01, 1.2);
    LinearApproximationVars y = generateVarsForSquare(lpBuilder, "y", closedRange(0.0, 1.0), 0.01, 1.2);
    LinearApproximationVars z = generateVarsForSquare(lpBuilder, "z", closedRange(0.5, 1.0), 0.01, 1.2);
    LinearOptimizationProgram lp = lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(
            sumOfDisjointHighLevelVars(x.getApproximatedNonLinearPart(), y.getApproximatedNonLinearPart(), z.getApproximatedNonLinearPart())))
        .addConstraint("x + y + z = 1",
            sumOfDisjointHighLevelVars(x.getLinearPart(), y.getLinearPart(), z.getLinearPart()), EQUAL_TO_SCALAR, 1.0)
        .build();
    System.out.println(lp);
    FeasibleOptimizationResult solution = assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lp));
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = solution.getAllRawVariablesAndOptimalValues();
    double finalX = evaluator.evaluateHighLevelVar(x.getLinearPart(), variablesAndOptimalValues);
    double finalY = evaluator.evaluateHighLevelVar(y.getLinearPart(), variablesAndOptimalValues);
    double finalZ = evaluator.evaluateHighLevelVar(z.getLinearPart(), variablesAndOptimalValues);
    assertEquals(0.25, finalX, COARSE_EPSILON);
    assertEquals(0.25, finalY, COARSE_EPSILON);
    assertEquals(
        "z is constrained to be > 0.5, so best solution has x = 0.25 and y = 0.25 (equal to each other)",
        0.50, finalZ, COARSE_EPSILON);
    // Even though the X, Y, Z will be approximate, their sum will not
    assertEquals(1, finalX + finalY + finalZ, 1e-7);
  }

  @Test
  public void testSum_twoVariables_unequalWeights_lowerBoundForVarsIsZero_cubeInsteadOfSquare() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("minimize misallocation x^3 + 8*y^3 subject to x + y = 1"));
    ArbitraryFunctionDescriptor cubic = arbitraryFunctionDescriptor(v -> Math.pow(v, 3));
    LinearApproximationVars x = generateVars(lpBuilder, "x", closedRange(0.0, 1.0), 0.01, 1.2, cubic);
    LinearApproximationVars y = generateVars(lpBuilder, "y", closedRange(0.0, 1.0), 0.01, 1.2, cubic);
    FeasibleOptimizationResult solution = assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(
            highLevelVarExpression(1, x.getApproximatedNonLinearPart(), 8, y.getApproximatedNonLinearPart())))
        .addConstraint(
            "x + y = 1", sumOfDisjointHighLevelVars(x.getLinearPart(), y.getLinearPart()), EQUAL_TO_SCALAR, 1.0)
        .build()));
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = solution.getAllRawVariablesAndOptimalValues();
    double finalX = evaluator.evaluateHighLevelVar(x.getLinearPart(), variablesAndOptimalValues);
    double finalY = evaluator.evaluateHighLevelVar(y.getLinearPart(), variablesAndOptimalValues);
    assertEquals(0.7404131788799999, finalX, COARSE_EPSILON);
    assertEquals(0.2595868211200001, finalY, COARSE_EPSILON);
    // Even though the X and Y will be approximate, their sum will not
    assertEquals(1, finalX + finalY, 1.01 * 1e-8);

    // It's hard to analytically show in this test why this solution is truly the best,
    // but one can informally show this by perturbing the X and Y a bit and showing that the solution is worse in both cases.
    double actualSolution = Math.pow(finalX, 3) + 8 * Math.pow(finalY, 3);
    assertTrue(actualSolution < Math.pow(finalX - 0.01, 3) + 8 * Math.pow(finalY + 0.01, 3));
    assertTrue(actualSolution < Math.pow(finalX + 0.01, 3) + 8 * Math.pow(finalY - 0.01, 3));

    double finalXcubed = evaluator.evaluateHighLevelVar(x.getApproximatedNonLinearPart(), variablesAndOptimalValues);
    double finalYcubed = evaluator.evaluateHighLevelVar(y.getApproximatedNonLinearPart(), variablesAndOptimalValues);
    assertEquals(doubleExplained(0.4059031493258673, Math.pow(finalX, 3)), finalXcubed, COARSE_EPSILON);
    assertEquals(doubleExplained(0.017492340411693213, Math.pow(finalY, 3)), finalYcubed, COARSE_EPSILON);
  }

  @Test
  public void blogPostExample_usesSquareRoots() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("minimize -sqrt(x) - 2*sqrt(y) subject to x + y = 1"));
    ArbitraryFunctionDescriptor squareRoot = arbitraryFunctionDescriptor(v -> Math.sqrt(v));
    LinearApproximationVars x = generateVars(lpBuilder, "x", closedRange(0.0, 1.0), 0.01, 1.2, squareRoot);
    LinearApproximationVars y = generateVars(lpBuilder, "y", closedRange(0.0, 1.0), 0.01, 1.2, squareRoot);
    FeasibleOptimizationResult solution = assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(
            highLevelVarExpression(-1, x.getApproximatedNonLinearPart(), -2, y.getApproximatedNonLinearPart())))
        .addConstraint(
            "x + y = 1", sumOfDisjointHighLevelVars(x.getLinearPart(), y.getLinearPart()), EQUAL_TO_SCALAR, 1.0)
        .build()));
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = solution.getAllRawVariablesAndOptimalValues();
    double finalX = evaluator.evaluateHighLevelVar(x.getLinearPart(), variablesAndOptimalValues);
    double finalY = evaluator.evaluateHighLevelVar(y.getLinearPart(), variablesAndOptimalValues);
    assertEquals(0.2, finalX, COARSE_EPSILON);
    assertEquals(0.8, finalY, COARSE_EPSILON);
    // Even though the X and Y will be approximate, their sum will not
    assertEquals(1, finalX + finalY, 1.01 * 1e-8);

    // It's harder to analytically show in this test why this solution is truly the best:
    // Set derivative to 0:
    // 0.5 / sqrt(x) - 1 / sqrt(1 - x) = 0
    // x = 0.2 gives 0. You can also plot the function and verify this.

    // You can informally show this by perturbing the X and Y a bit and showing that the solution is worse in both cases.
    DoubleUnaryOperator f = v -> -1 * Math.sqrt(v) - 2 * Math.sqrt(1 - v);
    double actualSolution = f.applyAsDouble(0.2);
    assertTrue(actualSolution < f.applyAsDouble(0.2 + 1e-4));
    assertTrue(actualSolution < f.applyAsDouble(0.2 - 1e-4));

    double finalSqrtX = evaluator.evaluateHighLevelVar(x.getApproximatedNonLinearPart(), variablesAndOptimalValues);
    double finalSqrtY = evaluator.evaluateHighLevelVar(y.getApproximatedNonLinearPart(), variablesAndOptimalValues);
    assertEquals(doubleExplained(0.447213595, Math.sqrt(0.2)), finalSqrtX, COARSE_EPSILON);
    assertEquals(doubleExplained(0.894427191, Math.sqrt(0.8)), finalSqrtY, COARSE_EPSILON);
  }

  // Here's a mini-proof. You can also plot the function and verify this.
  // Set derivative to 0 after rewriting y as 1-x:
  // - 3 * 0.5 / sqrt(x) - 2 * exp(1-x) * (-1)
  // = -1.5 / sqrt(x) + 2*exp(1-x)
  // x ~= 0.0915 makes the derivative 0.
  @Test
  public void blogPostExample_usesBothExpAndSquareRoot_doesNotUseConstraint() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("minimize -3*sqrt(x) - 2*e^(1-x); no constraints"));
    FunctionDescriptor f = arbitraryFunctionDescriptor(v -> -3 * Math.sqrt(v) - 2 * Math.exp(1 - v));
    LinearApproximationVars x = generateVars(lpBuilder, "x", closedRange(0.0, 0.1), 0.01, 1, f);
    LinearOptimizationProgram lp = lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(
            x.getApproximatedNonLinearPart().getHighLevelVarExpression()))
        .build();
    System.out.println(lp);
    AllRawVariablesAndOptimalValues variablesAndOptimalValues =
        assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lp)).getAllRawVariablesAndOptimalValues();
    double finalX = evaluator.evaluateHighLevelVar(x.getLinearPart(), variablesAndOptimalValues);
    double optimalX = 0.0915;
    assertEquals(optimalX, finalX, COARSE_EPSILON);
    assertEquals(
        f.asFunction().applyAsDouble(optimalX),
        evaluator.evaluateHighLevelVar(x.getApproximatedNonLinearPart(), variablesAndOptimalValues),
        COARSE_EPSILON);

    // You can informally show this by perturbing the X and Y a bit and showing that the solution is worse in both cases.
    double actualSolution = f.asFunction().applyAsDouble(optimalX);
    assertTrue(actualSolution < f.asFunction().applyAsDouble(optimalX + 0.001));
    assertTrue(actualSolution < f.asFunction().applyAsDouble(optimalX - 0.001));
  }

  /**
   * This demonstrates that we can't always minimize any general function.
   * I think we can optimize any function of a single variable, but once we start having an objective function
   * that COMBINES more than one approximated variable, then we are not guaranteed that the individual
   * line segment variables will "fill up" from "left to right".
   * E.g. if x1 can go from [0, 0.25] and x2 from [0.25, 0.60], we want the following combinations to be valid for
   * x1   x2
   * 0    0.25
   * 0.01 0.25
   * 0.02 0.25
   * ...
   * 0.24 0.25
   * 0.25 0.25
   * 0.25 0.26
   * 0.25 0.27
   * ...
   * 0.25 0.59
   * 0.25 0.60
   *
   * but NOT e.g.
   * 0.01 0.26
   *
   * Not all functions will have this property.
   * This isn't a huge deal, because we don't need too much generality in the code. This would only matter
   * if I ever had to open-source this LP infra and rigorously explain what works and what doesn't.
   * In the meantime, keep this in mind.
   */
  @Test
  public void blogPostExample_usesBothExpAndSquareRoot_usesConstraint() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("minimize -3*sqrt(x) - 2*e^y subject to x + y = 1; does not work here"));
    LinearApproximationVars x = generateVars(lpBuilder, "x", closedRange(0.0, 1.0), 0.01, 1, arbitraryFunctionDescriptor(v -> Math.sqrt(v)));
    LinearApproximationVars y = generateVars(lpBuilder, "y", closedRange(0.0, 1.0), 0.01, 1, arbitraryFunctionDescriptor(v -> Math.exp(v)));
    LinearOptimizationProgram lp = lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(highLevelVarExpression(
            -3, x.getApproximatedNonLinearPart(), -2, y.getApproximatedNonLinearPart())))
        .addConstraint(
            "x + y = 1", sumOfDisjointHighLevelVars(x.getLinearPart(), y.getLinearPart()), EQUAL_TO_SCALAR, 1.0)
        .build();
    System.out.println(lp);
    AllRawVariablesAndOptimalValues variablesAndOptimalValues =
        assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lp)).getAllRawVariablesAndOptimalValues();
    double finalX = evaluator.evaluateHighLevelVar(x.getLinearPart(), variablesAndOptimalValues);

    double optimalX = 0.0915;
    // You can informally show this by perturbing the X and Y a bit and showing that the solution is worse in both cases.
    DoubleUnaryOperator f = v -> -3 * Math.sqrt(v) - 2 * Math.exp(1 - v);
    double actualSolution = f.applyAsDouble(optimalX);
    assertTrue(actualSolution < f.applyAsDouble(optimalX + 0.001));
    assertTrue(actualSolution < f.applyAsDouble(optimalX - 0.001));

    assertNotEquals("Using this LP formulation, we can't solve the problem", optimalX, finalX, COARSE_EPSILON);
  }

  private void assertOptimalForApproximationOfSingleFunction(
      ClosedRange<Double> range, DoubleUnaryOperator f, double expectedOptimalForLinear) {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder().withHumanReadableLabel(DUMMY_LABEL);
    LinearApproximationVars x = generateVars(lpBuilder, "x", range, 0.01, 1, arbitraryFunctionDescriptor(f));
    LinearOptimizationProgram lp = lpBuilder
        .setObjectiveFunction(simpleLinearObjectiveFunction(
            x.getApproximatedNonLinearPart().getHighLevelVarExpression()))
        .build();
    AllRawVariablesAndOptimalValues variablesAndOptimalValues =
        assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lp)).getAllRawVariablesAndOptimalValues();
    ToDoubleFunction<HighLevelVar> evaluate = var -> evaluator.evaluateHighLevelVar(var, variablesAndOptimalValues);
    assertEquals(expectedOptimalForLinear, evaluate.applyAsDouble(x.getLinearPart()), COARSE_EPSILON);
    double expectedOptimalForApproximated = f.applyAsDouble(expectedOptimalForLinear);
    assertEquals(expectedOptimalForApproximated, evaluate.applyAsDouble(x.getApproximatedNonLinearPart()), COARSE_EPSILON);
  }

  private LinearApproximationVars generateVars(
      HighLevelLPBuilder builder, String varPrefix, ClosedRange<Double> rangeForFinal,
      double initialStep, double stepMultiplier, FunctionDescriptor functionDescriptor) {
    LinearApproximationVarRanges linearApproximationVarRanges =
        geometricallyIncreasingLinearApproximationVarRangesGenerator.calculateGeometricallyIncreasingRanges(
            geometricallyIncreasingRangesGenerationInstructionsBuilder()
                .setInitialStep(initialStep)
                .setStepMultiplier(stepMultiplier)
                .setBoundsForOriginalExpression(rangeForFinal)
                .approximationStartsAt0()
                .doNotStopMultiplyingStepsUntilEndOfInterval()
                .build());
    return linearApproximationVarsGenerator.generateLinearApproximationOfSingleVariable(
        builder.getHighLevelVariablesBuilder(),
        varPrefix,
        linearApproximationVarRangesAndValues(linearApproximationVarRanges, functionDescriptor));
  }

  private LinearApproximationVars generateVarsForSquare(
      HighLevelLPBuilder builder, String varPrefix, ClosedRange<Double> rangeForFinal, double initialStep, double stepMultiplier) {
    return generateVars(builder, varPrefix, rangeForFinal, initialStep, stepMultiplier, quadraticFunctionDescriptor());
  }

}
