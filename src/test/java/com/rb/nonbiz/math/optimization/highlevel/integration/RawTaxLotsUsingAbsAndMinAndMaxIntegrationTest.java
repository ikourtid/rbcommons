package com.rb.nonbiz.math.optimization.highlevel.integration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.biz.investing.modeling.RBCommonsConstants;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues;
import com.rb.nonbiz.math.optimization.general.LinearOptimizationProgram;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.math.optimization.highlevel.*;
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
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.sumOfDisjointHighLevelVars;
import static com.rb.nonbiz.math.optimization.highlevel.integration.RawTaxLotsUsingMinAndMaxIntegrationTest.TestResult.testResultMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.integration.RawTaxLotsUsingMinAndMaxIntegrationTest.TestResult.verifiedTestResult;
import static com.rb.nonbiz.math.optimization.lpsolve.LpSolveLinearOptimizerTest.makeRealLpSolveLinearOptimizer;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBIntegrationTest.makeRealObject;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * #see RawTaxLotsUsingMinAndMaxIntegrationTest
 * In fact, you should read that test before you read this one.
 *
 * The difference here is that the targets are not 0% X and 100% Y, but rather
 * 20% X and 80% Y. In addition, we start at 98% X and 2% Y, so it is conceivable that we could buy or sell
 * in either X or Y, which means we need to use an absolute value.
 */
public class RawTaxLotsUsingAbsAndMinAndMaxIntegrationTest {

  private HighLevelVarEvaluator evaluator = makeRealObject(HighLevelVarEvaluator.class);

  /**
   * When (almost) ignoring misallocation, we should just sell the 11 lot (which lets us harvest tax losses {@code =>} good)
   * and then sell the 23 lot as well, since we can't completely ignore misallocation in this test.
   */
  @Test
  public void almostIgnoringMisallocation_sells11_sells23() {
    // Note that we don't *have* to force the tax lots to be sort in a specified way. This particular example penalizes
    // tax lots in the objective in such a way that we would always sell the 27 lot before we sell any of the 37 lot.
    // However, this is not always guaranteed to happen in future more complicated scenarios.
    assertThat(
        getOptimizationResultWithConstrainedTaxLotSellingOrder(0.1, 1e-6),
        testResultMatcher(new TestResult(0.11, 0.23, 0, 0)));
    assertThat(
        getOptimizationResultWithUnconstrainedTaxLotSellingOrder(0.1, 1e-6),
        testResultMatcher(new TestResult(0.11, 0.23, 0, 0)));
  }

  /**
   * When (almost) ignoring tax efficiency, we just sell everything to get to target
   */
  @Test
  public void almostIgnoringTaxEfficiency_constrainedTaxLotSellingOrder_sellsAllX() {
    // Note that we don't *have* to force the tax lots to be sort in a specified way. This particular example penalizes
    // tax lots in the objective in such a way that we would always sell the 27 lot before we sell any of the 37 lot.
    // However, this is not always guaranteed to happen in future more complicated scenarios.

    // only need to sell 17 of the 37 lot to get to target; the remaining 20 (0.2) matches the 20% X target.
    assertThat(
        getOptimizationResultWithConstrainedTaxLotSellingOrder(0.1, 999_999),
        testResultMatcher(new TestResult(0.11, 0.23, 0.27, 0.17)));
    assertThat(
        getOptimizationResultWithUnconstrainedTaxLotSellingOrder(0.1, 999_999),
        testResultMatcher(new TestResult(0.11, 0.23, 0.27, 0.17)));
  }

  /**
   * See corresponding method in RawTaxLotsUsingMinAndMaxIntegrationTest.
   */
  @Test
  public void inBetween_constrainedTaxLotSellingOrder() {
    // Note that we don't *have* to force the tax lots to be sort in a specified way. This particular example penalizes
    // tax lots in the objective in such a way that we would always sell the 27 lot before we sell any of the 37 lot.
    // However, this is not always guaranteed to happen in future more complicated scenarios.
    for (BiFunction<Double, Double, TestResult> getOptimizationResult : ImmutableList.<BiFunction<Double, Double, TestResult>>of(
        (costOfBookingGains, riskLambda) -> getOptimizationResultWithConstrainedTaxLotSellingOrder(costOfBookingGains, riskLambda),
        (costOfBookingGains, riskLambda) -> getOptimizationResultWithUnconstrainedTaxLotSellingOrder(costOfBookingGains, riskLambda))) {
      assertThat(
          getOptimizationResult.apply(0.1, 0.00499),
          testResultMatcher(new TestResult(0.11, 0.23, 0, 0)));
      assertThat(
          getOptimizationResult.apply(0.1, 0.00501),
          testResultMatcher(new TestResult(0.11, 0.23, 0.27, 0)));
      assertThat(
          getOptimizationResult.apply(0.1, 0.00999),
          testResultMatcher(new TestResult(0.11, 0.23, 0.27, 0)));
      // only need to sell 17 of the 37 lot to get to target; the remaining 20 (0.2) matches the 20% X target.
      assertThat(
          getOptimizationResult.apply(0.1, 0.01001),
          testResultMatcher(new TestResult(0.11, 0.23, 0.27, 0.17)));
    }
  }

  private TestResult getOptimizationResultWithConstrainedTaxLotSellingOrder(double costOfBookingGains, double riskLambda) {
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
        .generateDumbAbsoluteValueSuperVars(varBuilder, "absDiffX", highLevelVarExpression(1.0, finalX, -0.2));
    AbsoluteValueSuperVars absDiffY = makeRealObject(AbsoluteValueSuperVarsGenerator.class)
        .generateDumbAbsoluteValueSuperVars(varBuilder, "absDiffY", highLevelVarExpression(1.0, finalY, -0.8));

    BiFunction<String, HighLevelVarExpression, MaxSuperVar> maxMaker = (label, expr) -> generateMax(
        varBuilder, label, zeroConstantHighLevelVarExpression(), expr);
    MaxSuperVar sell23max = maxMaker.apply("max(0, sellX - 11)", highLevelVarExpression(1.0, sellX, -0.11));
    MaxSuperVar sell27max = maxMaker.apply("max(0, sellX - 34)", highLevelVarExpression(1.0, sellX, doubleExplained(-0.34, -(0.11 + 0.23))));
    MaxSuperVar sell37max = maxMaker.apply("max(0, sellX - 61)", highLevelVarExpression(1.0, sellX, doubleExplained(-0.61, -(0.11 + 0.23 + 0.27))));

    TriFunction<String, Double, SuperVar, MinSuperVar> minMaker = (label, constTerm, superVar) -> generateMin(
        varBuilder, label, constantHighLevelVarExpression(constantTerm(constTerm)), superVar.getHighLevelVarExpression());

    MinSuperVar sell11upper = minMaker.apply("min(11, sellX)",                         0.11, sellX);
    MinSuperVar sell23upper = minMaker.apply("min(23, max(0, sellX - 11)",             0.23, sell23max);
    MinSuperVar sell27upper = minMaker.apply("min(27, max(0, sellX - (11 + 23))",      0.27, sell27max);
    MinSuperVar sell37upper = minMaker.apply("min(37, max(0, sellX - (11 + 23 + 27))", 0.37, sell37max);

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

  private TestResult getOptimizationResultWithUnconstrainedTaxLotSellingOrder(double costOfBookingGains, double riskLambda) {
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
        .generateDumbAbsoluteValueSuperVars(varBuilder, "absDiffX", highLevelVarExpression(1.0, finalX, -0.2));
    AbsoluteValueSuperVars absDiffY = makeRealObject(AbsoluteValueSuperVarsGenerator.class)
        .generateDumbAbsoluteValueSuperVars(varBuilder, "absDiffY", highLevelVarExpression(1.0, finalY, -0.8));

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
        builder, labelString, left, right, createArtificialTermForMinUsingDefaultWeight(makeRealObject(RBCommonsConstants.class)));
  }

  private MaxSuperVar generateMax(
      HighLevelVariablesBuilder builder, String labelString, HighLevelVarExpression left, HighLevelVarExpression right) {
    return makeRealObject(MaxSuperVarGenerator.class).generateMax(
        builder, labelString, left, right, createArtificialTermForMaxUsingDefaultWeight(makeRealObject(RBCommonsConstants.class)));
  }

  private AllRawVariablesAndOptimalValues calculateSolution(LinearOptimizationProgram lp) {
    return assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lp)).getAllRawVariablesAndOptimalValues();
  }

}
