package com.rb.nonbiz.math.optimization.highlevel.integration;

import com.google.common.collect.Range;
import com.rb.biz.investing.modeling.RBCommonsConstants;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues;
import com.rb.nonbiz.math.optimization.general.LinearOptimizationProgram;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.math.optimization.highlevel.integration.RawTaxLotsUsingMinAndMaxIntegrationTest.TestResult;
import org.junit.Test;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.EQUAL_TO_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.GREATER_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.OptimizationResultTest.assumeFeasible;
import static com.rb.nonbiz.math.optimization.general.SimpleLinearObjectiveFunction.simpleLinearObjectiveFunction;
import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.constantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMaxUsingDefaultWeight.createArtificialTermForMaxUsingDefaultWeight;
import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMinUsingDefaultWeight.createArtificialTermForMinUsingDefaultWeight;
import static com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar.generalSuperVarWithoutAddedConstraintsOrArtificialTerms;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelLPBuilderTest.makeRealHighLevelLPBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.constantHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.zeroConstantHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.differenceOf2DisjointHighLevelVars;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.highLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.singleVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.sumOfDisjointHighLevelVars;
import static com.rb.nonbiz.math.optimization.highlevel.integration.RawTaxLotsUsingMinAndMaxIntegrationTest.TestResult.testResultMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.integration.RawTaxLotsUsingMinAndMaxIntegrationTest.TestResult.verifiedTestResult;
import static com.rb.nonbiz.math.optimization.lpsolve.LpSolveLinearOptimizerTest.makeRealLpSolveLinearOptimizer;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBIntegrationTest.makeRealObject;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * #see RawTaxLotsUsingAbsAndMinAndMaxIntegrationTest
 * In fact, you should read that test before you read this one.
 *
 * The difference here is that we are trying to force (via constraints) the tax lots to be sold in the exact opposite order
 * from what the objective function is implying: we try force the optimizer to sell the 37 lot first, then the 27,
 * then 23, then 11, even though 11 is the lossiest and should be sold first.
 * Unfortunately, the trick does not work. I started writing this test thinking that we would
 * somehow be able to force the bad lots to be sold first, but since that's not the case, I should at least keep
 * this test around to document the reasoning about why this trick doesn't work.
 */
public class RawTaxLotsForcingDifferentSellingOrderIntegrationTest {

  private HighLevelVarEvaluator evaluator = makeRealObject(HighLevelVarEvaluator.class);

  /**
   * When (almost) ignoring misallocation, the original tests (the ones that don't try to force a reverse selling
   * order on the tax lots) would result in us selling the 11 lot (which lets us harvest tax losses {@code =>} good)
   * and then sell the 23 lot as well, since we can't completely ignore misallocation in this test.
   *
   * In our case, that shouldn't be able to happen, since we want to force the tax lots to be sold in reverse order,
   * i.e. least sellworthy to most sellworthy. This means the 37, then 27, then 23, then 11 lot.
   *
   * The reason why this doesn't work is obvious only in retrospect.
   * The artificial terms we create for the min and the max will always work if those min and max values don't affect
   * the optimization. However, in this case they do: if e.g. a max becomes as tight as we'd like it to be
   * (in other words, as low as possible),
   * it will result in selling a tax lot that makes the objective worse, which in this case we actually wanted to happen
   * through this trickery with min/max. However, it's better for the optimizer to NOT push the max variable to
   * become small enough to 'touch' one of the two expressions it's taking the max of.
   * Objective-wise, there's relatively little to gain by pushing that max low,
   * but a lot to gain by keeping the max 'loose' (higher than where it should be)
   * and allowing to sell the lossiest lot first, even though we don't desire that.
   */
  @Test
  public void almostIgnoringMisallocation_sells11_sells23() {
    assertThat(
        getOptimizationResultWithForciblyReversedTaxLotSellingOrder(0.1, 1e-6),
        testResultMatcher(new TestResult(0.11, 0.23, 0, 0)));
  }

  private TestResult getOptimizationResultWithForciblyReversedTaxLotSellingOrder(double costOfBookingGains, double riskLambda) {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("maximize some combination of tax efficiency & allocation accuracy"));
    HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();

    // These per-tax-lot variables will later have their upper bound further restricted, but it can't hurt to start
    // with a loose upper bound of 1. The optimizer may actually be faster this way, e.g. because it can find an
    // initial feasible point sooner by using variable bounds instead of relying on constraints.
    RawVariable sell11x = varBuilder.addConstrainedRawVariable("sell11x", Range.closed(0.0, 0.11));
    RawVariable sell23x = varBuilder.addConstrainedRawVariable("sell23x", Range.closed(0.0, 0.23));
    RawVariable sell27x = varBuilder.addConstrainedRawVariable("sell27x", Range.closed(0.0, 0.27));
    RawVariable sell37x = varBuilder.addConstrainedRawVariable("sell37x", Range.closed(0.0, 0.37));
    RawVariable buyX    = varBuilder.addConstrainedRawVariable("buyX", Range.closed(0.0, 0.02));
    SuperVar sellX = varBuilder.addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(
        label("sellX"), sumOfDisjointHighLevelVars(sell11x, sell23x, sell27x, sell37x)));
    RawVariable buyY    = varBuilder.addConstrainedRawVariable("buyY",  Range.closed(0.0, 0.98));
    RawVariable sellY   = varBuilder.addConstrainedRawVariable("sellY", Range.closed(0.0, 0.02));

    SuperVar finalX = varBuilder.addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(label("finalX"),
      highLevelVarExpression(1.0, buyX, -1.0, sellX, 0.98)));
    SuperVar finalY = varBuilder.addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(label("finalY"),
        highLevelVarExpression(1.0, buyY, -1.0, sellY, 0.02)));
    AbsoluteValueSuperVars absDiffX = makeRealObject(AbsoluteValueSuperVarsGenerator.class)
        .generateDumbAbsoluteValueSuperVars(
            varBuilder, "absDiffX", highLevelVarExpression(1.0, finalX, -0.2));
    AbsoluteValueSuperVars absDiffY = makeRealObject(AbsoluteValueSuperVarsGenerator.class)
        .generateDumbAbsoluteValueSuperVars(
            varBuilder, "absDiffY", highLevelVarExpression(1.0, finalY, -0.8));

    BiFunction<String, HighLevelVarExpression, MaxSuperVar> maxMaker = (label, expr) -> generateMax(
        varBuilder, label, zeroConstantHighLevelVarExpression(), expr);
    MaxSuperVar sell37max = maxMaker.apply("max(0, sellX)",      singleVarExpression(sellX));
    MaxSuperVar sell27max = maxMaker.apply("max(0, sellX - 37)", highLevelVarExpression(1.0, sellX, -0.37));
    MaxSuperVar sell23max = maxMaker.apply("max(0, sellX - 64)", highLevelVarExpression(1.0, sellX, doubleExplained(-0.64, -(0.37 + 0.27))));
    MaxSuperVar sell11max = maxMaker.apply("max(0, sellX - 87)", highLevelVarExpression(1.0, sellX, doubleExplained(-0.87, -(0.37 + 0.27 + 0.23))));

    TriFunction<String, Double, SuperVar, MinSuperVar> minMaker = (label, constTerm, superVar) -> generateMin(
        varBuilder, label, constantHighLevelVarExpression(constantTerm(constTerm)), superVar.getHighLevelVarExpression());

    MinSuperVar sell37upper = minMaker.apply("min(37, max(0, sellX))",      0.37, sell37max);
    MinSuperVar sell27upper = minMaker.apply("min(27, max(0, sellX - 37)",  0.27, sell27max);
    MinSuperVar sell23upper = minMaker.apply("min(23, max(0, sellX - 64)",  0.23, sell23max);
    MinSuperVar sell11upper = minMaker.apply("min(11, max(0, sellX - 87))", 0.11, sell11max);

    AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
        .setObjectiveFunction(
            simpleLinearObjectiveFunction(highLevelVarExpression(
                -0.3 * costOfBookingGains, sell11x,
                0.1 * costOfBookingGains, sell27x,
                0.2 * costOfBookingGains, sell37x,
                // I should also treat Y as having a lot of 2 that can be sold & which has a tax gain / loss
                // that should be implied in the objective function. I'm ignoring it for simplicity though.
                riskLambda, absDiffX.getAbsoluteValue(),
                riskLambda, absDiffY.getAbsoluteValue())))
        .addConstraint("finalX + finalY = 1", sumOfDisjointHighLevelVars(finalX, finalY), EQUAL_TO_SCALAR, 1)
        .addConstraint("sell11x <= its u.b.", differenceOf2DisjointHighLevelVars(sell11upper, sell11x), GREATER_THAN_SCALAR, 0)
        .addConstraint("sell23x <= its u.b.", differenceOf2DisjointHighLevelVars(sell23upper, sell23x), GREATER_THAN_SCALAR, 0)
        .addConstraint("sell27x <= its u.b.", differenceOf2DisjointHighLevelVars(sell27upper, sell27x), GREATER_THAN_SCALAR, 0)
        .addConstraint("sell37x <= its u.b.", differenceOf2DisjointHighLevelVars(sell37upper, sell37x), GREATER_THAN_SCALAR, 0)
        .build());
    Function<HighLevelVar, Double> evaluate = superVar ->
        evaluator.evaluateHighLevelVar(superVar, variablesAndOptimalValues);
    return verifiedTestResult(
        evaluate.apply(sell11x),
        evaluate.apply(sell23x),
        evaluate.apply(sell27x),
        evaluate.apply(sell37x),
        evaluate.apply(sellX),
        evaluate.apply(buyY));
  }

  private MinSuperVar generateMin(
      HighLevelVariablesBuilder builder, String labelString, HighLevelVarExpression left, HighLevelVarExpression right) {
    return makeRealObject(MinSuperVarGenerator.class).generateMin(
        builder, labelString, left, right,
        createArtificialTermForMinUsingDefaultWeight(makeRealObject(RBCommonsConstants.class)));
  }

  private MaxSuperVar generateMax(
      HighLevelVariablesBuilder builder, String labelString, HighLevelVarExpression left, HighLevelVarExpression right) {
    return makeRealObject(MaxSuperVarGenerator.class).generateMax(
        builder, labelString, left, right,
        createArtificialTermForMaxUsingDefaultWeight(makeRealObject(RBCommonsConstants.class)));
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

}
