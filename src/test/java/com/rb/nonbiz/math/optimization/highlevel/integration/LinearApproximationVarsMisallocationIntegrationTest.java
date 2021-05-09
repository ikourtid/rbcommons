package com.rb.nonbiz.math.optimization.highlevel.integration;

import com.google.common.collect.Range;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.PairOfSameType;
import com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues;
import com.rb.nonbiz.math.optimization.general.FeasibleOptimizationResult;
import com.rb.nonbiz.math.optimization.highlevel.*;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.PairOfSameType.pairOfSameType;
import static com.rb.nonbiz.collections.RBRanges.rangeForAbsValue;
import static com.rb.nonbiz.collections.RBRanges.shiftDoubleRange;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.EQUAL_TO_SCALAR;
import static com.rb.nonbiz.math.optimization.general.OptimizationResultTest.assumeFeasible;
import static com.rb.nonbiz.math.optimization.general.SimpleLinearObjectiveFunction.simpleLinearObjectiveFunction;
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
import static org.junit.Assert.assertEquals;

/**
 * We want to formulate the optimization so that the farther we get from a target, the faster we penalize.
 * For example: assume target of x, y, z = 10% 40% 50%
 * assume z is forced overweight to 70%. The solution should be
 * x, y, z = 6% 24% 70%
 * in misallocation terms, this is
 * -4% -16% +20%
 * rewriting without percentages, and ignoring z which is fixed anyway:
 * -0.20 = -0.04 -0.16
 * = - 0.4 * 10% - 0.4 * 40%
 * However, we get the same answer with e.g. -0.03 and -0.17; it's still -0.20.
 * We need some way to minimize the sums of squares, so they balance out.
 * This is the whole reason I created the non-linear-approximation infrastructure in the first place!
 *
 * Put another way: if I don't do this square trick, then here's what happens:
 * z is 20% overweight. There's no constraint that would keep it from becoming even more overweight,
 * but in practice, given the way the objective function looks, that will never happen. So, to simplify this discussion,
 * let's assume that z will be 70% in the final portfolio - not more than 70%.
 * OK, so x and y have to both be underweight by a total of 20% between themselves. Now, being underweight 1% in x
 * is just as bad as being underweight by 4% in y, by construction. This is because we care about misallocation
 * RELATIVE to the target %, not just in absolute terms. But using LP, this would just cause y to go to
 * 40% - 20% = 20% - i.e. put all this 'underweightness' into , and leave cash at 10%. As far as I know, there's no way
 * to distribute this underweightness in a pro-rata fashion, so to speak, at least using an LP formulation.
 *
 * Let's try to improve on this then.
 *
 * Say, minimize (mX * 10%) ^ 2 + (mY * 40%) ^ 2 = .01 * mX ^ 2 + 0.16 * mY ^ 2
 * Minimization works the same subject to a multiplier, so let's rewrite to this, for simplicity:
 * mX ^ 2 + 16 * mY ^ 2
 * in this case, mX + mY = -0.2, so we can rewrite as
 * mX ^ 2 + 16 * (0.2 + mX) ^ 2
 * = mX ^ 2 + 16 * (mX ^ 2 + 0.4 * mX + 0.04)
 * = 17 * mX ^ 2 + 6.4 * mX + 0.64
 * to minimize, set derivative to 0
 * 0 = 34 * mX + 6.4
 *   mX = -6.4 / 34 = -0.188235294 - not the desired.
 *
 * But that was wrong. I later realized that the correct way is to minimize
 * mX^2 / 10% + mY^2 / 40% - equivalently, 4*mX^2 + mY^2
 * = 4*mX^2 + (-0.2 - mX)^2
 * = 5*mX^2 + 0.4 * mX + 0.04
 * set derivative to 0 to minimize:
 * 0 = 10*mX + 0.4
 * mX = -0.04 = -4%
 * mY = -0.2 - mX = -0.16 = -16%
 *
 * ... so that's good. It means that if we minimize sum(misallocation_x ^ 2 * target weight of x)
 * then we will get the desired answer. x at 6% (10% target - 4%) is 40% less than its target of 10%.
 * Likewise, y at 24% (40% target - 16%) is also 40% less than its target of 40%.
 *
 * This particular scenario is described in the test named
 * x10pct_y40pct_z50pct_zSignificantlyForcedOverweight_othersShareMisallocation
 */
public class LinearApproximationVarsMisallocationIntegrationTest {

  private final double COARSE_EPSILON = 0.025;

  AbsoluteValueSuperVarsGenerator absoluteValueSuperVarsGenerator =
      new AbsoluteValueSuperVarsGenerator();
  GeometricallyIncreasingLinearApproximationVarRangesGenerator geometricallyIncreasingLinearApproximationVarRangesGenerator =
      makeRealObject(GeometricallyIncreasingLinearApproximationVarRangesGenerator.class);
  HighLevelVarEvaluator evaluator = makeRealObject(HighLevelVarEvaluator.class);
  LinearApproximationVarsGenerator linearApproximationVarsGenerator =
      new LinearApproximationVarsGenerator() {{
        this.linearApproximationExpressionsGenerator = new LinearApproximationExpressionsGenerator();
      }};

  @Test
  public void x10pct_y40pct_z50pct_allUnrestricted_allocatesPerfectly() {
    x10pct_y40pct_z50pct_helper(
        closedRange(0.0, 1.0),
        closedRange(0.0, 1.0),
        closedRange(0.0, 1.0),
        0.1,
        0.4,
        0.5);
  }

  @Test
  public void x10pct_y40pct_z50pct_allAreSlightlyRestrictedButPerfectSolutionStillPossible_allocatesPerfectly() {
    x10pct_y40pct_z50pct_helper(
        closedRange(0.01, 0.98), // x can be anywhere between 1% and 98% of the total portfolio
        closedRange(0.03, 0.96), // y can be anywhere between 3% and 96% of the total portfolio
        closedRange(0.05, 0.94), // z can be anywhere between 5% and 94% of the total portfolio
        0.1,  // target for x is 10% of the portfolio
        0.4,  // target for y is 40% of the portfolio
        0.5); // target for z is 50% of the portfolio
  }

  @Test
  public void x10pct_y40pct_z50pct_allAreVeryRestrictedButPerfectSolutionStillPossible_allocatesPerfectly() {
    double rangeHalfWidth = 0.01;
    x10pct_y40pct_z50pct_helper(
        closedRange(0.1 - rangeHalfWidth, 0.1 + rangeHalfWidth),
        closedRange(0.4 - rangeHalfWidth, 0.4 + rangeHalfWidth),
        closedRange(0.5 - rangeHalfWidth, 0.5 + rangeHalfWidth),
        0.1,
        0.4,
        0.5);
  }

  @Test
  public void x10pct_y40pct_z50pct_zIsVeryRestrictedButPerfectSolutionStillPossible_allocatesPerfectly() {
    x10pct_y40pct_z50pct_helper(
        closedRange(0.0, 1.0),
        closedRange(0.0, 1.0),
        closedRange(0.48, 1.0),
        0.1,
        0.4,
        0.5);
  }

  @Test
  public void x10pct_y40pct_z50pct_zSlightlyForcedOverweight_othersShareMisallocation() {
    double amount = 0.02;
    x10pct_y40pct_z50pct_helper(
        closedRange(0.0, 1.0),
        closedRange(0.0, 1.0),
        closedRange(0.50 + amount, 1.0),
        // Z would normally be 0.5 but is forced overweight to be at least 0.52.
        // This 0.02 (0.5 - 0.52) overweightness will be distributed pro rata to the X and Y
        doubleExplained(0.096, 0.1 - amount * doubleExplained(0.2, 0.1 / (0.1 + 0.4))),
        doubleExplained(0.384, 0.4 - amount * doubleExplained(0.8, 0.4 / (0.1 + 0.4))),
        0.50 + amount);
  }

  @Test
  public void x10pct_z90pct_zSlightlyForcedOverweight_xBecomesMoreUnderweight_simpler() {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder().withHumanReadableLabel(DUMMY_LABEL);
    // These really stand for 'x misallocation' etc. There's no y in this example.
    // I created this to track down a bug that's hard to see with the large tests.
    PairOfSameType<SuperVar> xOff = generateVars(lpBuilder.getHighLevelVariablesBuilder(), "xOff", 0.4, closedRange(0.0, 1.0), 0.03, 4);
    PairOfSameType<SuperVar> zOff = generateVars(lpBuilder.getHighLevelVariablesBuilder(), "zOff", 0.6, closedRange(0.62, 1.0), 0.03, 4);
    SuperVar xOffSigned = xOff.getLeft();
    SuperVar zOffSigned = zOff.getLeft();
    SuperVar xOffSquared = xOff.getRight();
    SuperVar zOffSquared = zOff.getRight();

    FeasibleOptimizationResult solution = assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lpBuilder
        // assume an allocation of 40% x, 60% z
        // Then, the misallocations should be penalized in a proportional way - e.g. being 4% overweight x
        // is just as bad as being 6% overweight/underweight z.
        // So we want to minimize 0.4 * xOff^2 + 0.6 * zOff^2
        .setObjectiveFunction(simpleLinearObjectiveFunction(
            highLevelVarExpression(
                coeff(0.4), xOffSquared,
                coeff(0.6), zOffSquared)))
        .addConstraint("misallocations for x + z = 0",
            sumOfDisjointHighLevelVars(xOffSigned, zOffSigned), EQUAL_TO_SCALAR, 0.0)
        .build()));
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = solution.getAllRawVariablesAndOptimalValues();
    double finalXoff = evaluator.evaluateHighLevelVar(xOffSigned, variablesAndOptimalValues);
    double finalZoff = evaluator.evaluateHighLevelVar(zOffSigned, variablesAndOptimalValues);
    double expectedXoff = doubleExplained(-0.02, 0.38 - 0.4);
    double expectedZoff = doubleExplained( 0.02, 0.62 - 0.6);
    String message = Strings.format("(x, z) = ( %s %s ) vs expected ( %s %s )",
        finalXoff, finalZoff, expectedXoff, expectedZoff);
    assertEquals(message, expectedXoff, finalXoff, COARSE_EPSILON);
    assertEquals(message, expectedZoff, finalZoff, COARSE_EPSILON);
    // Even though the X, Z will be approximate, their sum will not
    assertEquals(0, finalXoff + finalZoff, 1e-7);

    double finalXoffSquared = evaluator.evaluateHighLevelVar(xOffSquared, variablesAndOptimalValues);
    double finalZoffSquared = evaluator.evaluateHighLevelVar(zOffSquared, variablesAndOptimalValues);

    assertEquals("x", expectedXoff * expectedXoff, finalXoffSquared, COARSE_EPSILON);
    assertEquals("z", expectedZoff * expectedZoff, finalZoffSquared, COARSE_EPSILON);
  }

  @Test
  public void x10pct_y40pct_z50pct_zSignificantlyForcedOverweight_othersShareMisallocation() {
    x10pct_y40pct_z50pct_helper(
        closedRange(0.0, 1.0),
        closedRange(0.0, 1.0),
        closedRange(0.7, 1.0),
        // Z would normally be 0.5 but is forced overweight to be at least 0.7.
        // This 0.2 (0.7 - 0.5) overweightness will be distributed pro rata to the X and Y
        doubleExplained(0.06, 0.1 - 0.2 * doubleExplained(0.2, 0.1 / (0.1 + 0.4))),
        doubleExplained(0.24, 0.4 - 0.2 * doubleExplained(0.8, 0.4 / (0.1 + 0.4))),
        0.7);
  }

  @Test
  public void x10pct_y40pct_z50pct_zSlightlyForcedUnderweight_othersShareMisallocation() {
    x10pct_y40pct_z50pct_helper(
        closedRange(0.0, 1.0),
        closedRange(0.0, 1.0),
        closedRange(0.0, 0.48),
        // Z would normally be 0.5 but is forced underweight to be at most 0.48.
        // This 0.02 (0.52 - 0.5) overweightness will be distributed pro rata to the X and Y
        doubleExplained(0.104, 0.1 + 0.02 * doubleExplained(0.2, 0.1 / (0.1 + 0.4))),
        doubleExplained(0.416, 0.4 + 0.02 * doubleExplained(0.8, 0.4 / (0.1 + 0.4))),
        0.48);
  }

  @Test
  public void x10pct_y40pct_z50pct_zSignificantlyForcedUnderweight_othersShareMisallocation() {
    x10pct_y40pct_z50pct_helper(
        closedRange(0.0, 1.0),
        closedRange(0.0, 1.0),
        closedRange(0.0, 0.3),
        // Z would normally be 0.5 but is forced underweight to be at most 0.3.
        // This 0.2 (0.5 - 0.3) overweightness will be distributed pro rata to the X and Y
        doubleExplained(0.14, 0.1 + 0.2 * doubleExplained(0.2, 0.1 / (0.1 + 0.4))),
        doubleExplained(0.56, 0.4 + 0.2 * doubleExplained(0.8, 0.4 / (0.1 + 0.4))),
        0.3);
  }

  private void x10pct_y40pct_z50pct_helper(
      ClosedRange<Double> rangeX, ClosedRange<Double> rangeY, ClosedRange<Double> rangeZ,
      double expectedX, double expectedY, double expectedZ) {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder().withHumanReadableLabel(DUMMY_LABEL);
    // These really stand for 'x misallocation' etc.
    PairOfSameType<SuperVar> xOff = generateVars(lpBuilder.getHighLevelVariablesBuilder(), "xOff", 0.1, rangeX, 0.001, 1.1);
    PairOfSameType<SuperVar> yOff = generateVars(lpBuilder.getHighLevelVariablesBuilder(), "yOff", 0.4, rangeY, 0.001, 1.1);
    PairOfSameType<SuperVar> zOff = generateVars(lpBuilder.getHighLevelVariablesBuilder(), "zOff", 0.5, rangeZ, 0.001, 1.1);
    SuperVar xOffSigned = xOff.getLeft();
    SuperVar yOffSigned = yOff.getLeft();
    SuperVar zOffSigned = zOff.getLeft();
    SuperVar xOffSquared = xOff.getRight();
    SuperVar yOffSquared = yOff.getRight();
    SuperVar zOffSquared = zOff.getRight();

    FeasibleOptimizationResult solution = assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lpBuilder
        // assume an allocation of 10% x, 40% y, 50% z
        // Then, the misallocations should be penalized in a proportional way - e.g. being 1% overweight x
        // is just as bad as being 4% overweight/underweight y and 5% overweight/underweight z.
        // So we want to minimize xOff^2 / 0.1 + yOff^2 / 0.4 + zOff^2 / 0.5
        .setObjectiveFunction(simpleLinearObjectiveFunction(
            highLevelVarExpression(
                coeff(0.1), xOffSquared,
                coeff(0.4), yOffSquared,
                coeff(0.5), zOffSquared)))
        .addConstraint("misallocations for x + y + z = 0",
            sumOfDisjointHighLevelVars(xOffSigned, yOffSigned, zOffSigned), EQUAL_TO_SCALAR, 0.0)
        .build()));
    AllRawVariablesAndOptimalValues variablesAndOptimalValues = solution.getAllRawVariablesAndOptimalValues();
    double finalXoff = evaluator.evaluateHighLevelVar(xOffSigned, variablesAndOptimalValues);
    double finalYoff = evaluator.evaluateHighLevelVar(yOffSigned, variablesAndOptimalValues);
    double finalZoff = evaluator.evaluateHighLevelVar(zOffSigned, variablesAndOptimalValues);
    double expectedXoff = expectedX - 0.1;
    double expectedYoff = expectedY - 0.4;
    double expectedZoff = expectedZ - 0.5;
    String message = Strings.format("(x, y, z) = ( %s %s %s ) vs expected ( %s %s %s )",
        finalXoff, finalYoff, finalZoff, expectedXoff, expectedYoff, expectedZoff);
    assertEquals(message, expectedXoff, finalXoff, COARSE_EPSILON);
    assertEquals(message, expectedYoff, finalYoff, COARSE_EPSILON);
    assertEquals(message, expectedZoff, finalZoff, COARSE_EPSILON);
    // Even though the X, Y, Z will be approximate, their sum will not
    assertEquals(0, finalXoff + finalYoff + finalZoff, 1e-7);

    double finalXoffSquared = evaluator.evaluateHighLevelVar(xOffSquared, variablesAndOptimalValues);
    double finalYoffSquared = evaluator.evaluateHighLevelVar(yOffSquared, variablesAndOptimalValues);
    double finalZoffSquared = evaluator.evaluateHighLevelVar(zOffSquared, variablesAndOptimalValues);

    assertEquals("x^2", expectedXoff * expectedXoff, finalXoffSquared, COARSE_EPSILON);
    assertEquals("y^2", expectedYoff * expectedYoff, finalYoffSquared, COARSE_EPSILON);
    assertEquals("z^2", expectedZoff * expectedZoff, finalZoffSquared, COARSE_EPSILON);
  }

  private double coeff(double target) {
    return 1 / target;
  }

  private PairOfSameType<SuperVar> generateVars(
      HighLevelVariablesBuilder builder, String varPrefix, double target, ClosedRange<Double> rangeForFinal,
      double initialStep, double stepMultiplier) {
    Range<Double> rangeForMisallocation = shiftDoubleRange(rangeForFinal.asRange(), -1 * target);
    AbsoluteValueSuperVars absMisallocation =
        absoluteValueSuperVarsGenerator.generateAbsoluteValueSuperVarsOfSingleVariable(
            builder, varPrefix, rangeForMisallocation);
    ClosedRange<Double> rangeForAbs = closedRange(rangeForAbsValue(rangeForMisallocation));
    LinearApproximationVarRanges linearApproximationVarRanges =
        geometricallyIncreasingLinearApproximationVarRangesGenerator.calculateGeometricallyIncreasingRanges(
            geometricallyIncreasingRangesGenerationInstructionsBuilder()
                .setInitialStep(initialStep)
                .setStepMultiplier(stepMultiplier)
                .setBoundsForOriginalExpression(rangeForAbs)
                .approximationStartsAt0()
                .doNotStopMultiplyingStepsUntilEndOfInterval()
                .build());
    return pairOfSameType(
        absMisallocation.getSigned(),
        linearApproximationVarsGenerator.generateLinearApproximationOfExistingHighLevelVar(
            builder, Strings.format("|%s|", varPrefix),
            linearApproximationVarRangesAndValues(linearApproximationVarRanges, quadraticFunctionDescriptor()),
            absMisallocation.getAbsoluteValue())
            .getApproximatedNonLinearPart());
  }

}
