package com.rb.nonbiz.math.optimization.highlevel.integration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.biz.investing.modeling.RBCommonsConstants;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues;
import com.rb.nonbiz.math.optimization.general.LinearOptimizationProgram;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.math.optimization.highlevel.*;
import org.hamcrest.TypeSafeMatcher;
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
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBIntegrationTest.makeRealObject;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * This combines usage of min and max supervars.
 * Min supervars (only) are tested elsewhere, in MinSuperVarIntegrationTest
 * Max supervars (only) are tested elsewhere, in MaxSuperVarIntegrationTest
 *
 * Start with 100% x, 0% y. We must sell some X to get closer to the target of 0% x, 100% y.
 * (I am using such extreme numbers so I will not have to use absolute values; this keeps the test simple,
 * and also avoids the issue of whether we'll need artificial terms for the absolute values).
 *
 * 'Starting' really just means that we can only sell X, not buy it. Likewise, we can only buy Y, not sell it.
 *
 * There are 4 lots of 39, 27, 23, 11 shares (total 100). Assume the following % of (current / lot open price)
 *
 * 11 : -30% (at a loss - we'd always want to sell this, because it improves both the allocation and the tax situation)
 * 23 : 0% (doesn't matter)
 * 27 : +10% (at a gain)
 * 39 : +20% (at a gain)
 *
 * Now, let T be a unit fraction (0 to 1) that tells us the benefit we get by booking $1 of tax losses. If e.g. 0.05,
 * then each $1 harvested is like $0.05 of actual money gained right now. For the opposite direction, T gives us
 * the cost of booking $1 of tax gains - it's symmetric.
 *
 * For simplicity, let's consider everything
 * to be in terms of 'money right now' instead of 'ongoing amount made each year'.
 * Let L (lambda) be the cost of each unit of misallocation. In other words, we are willing to spend exactly $L
 * to cure $1 of misallocation.
 *
 * Since both T and L are in terms of $, not % of portfolio, let's assume w.l.o.g. that we have a $1 portfolio
 * for the purposes of this discussion; if we use a bigger portfolio, they will both scale up the same, but their
 * relative weight in the objective function won't change.
 *
 * We want to
 * minimize T * (tax gains incurred) + L * (misallocation from target)
 * == T * (sum (tax gains incurred by selling lots)) + L * ( abs(finalX - 0) + abs(finalY - 1) )
 * The 2nd term can be rewritten as
 * L * ( (finalX - 0) + (1 - finalY) )  // removing abs value since each X can only be overweight and Y underweight
 * == L * (1 + finalX - finalY)
 * == L * (1 + (1 - sellX) - buyY)
 * == -L * (sellX + buyY)  (ignoring constant of 2, since constants don't affect the optimization)
 *
 * Let's rewrite this further, ignoring for now that tax lots can only be sold in order.
 * Let sell11, sell23, sell27, sellOfWorstLot be the quantities we sell from each respective lot (all &ge; 0).
 * First, sellX = sell11 + sell23 + sell27 + sellOfWorstLot
 *
 * The costs (or benefits) of selling from these lots are:
 * sell11 : -30% * sell11 (negative {@code =>} we actually benefit from selling from the 11 lot, b/c it's at a loss)
 * sell23 : 0% * sell23 (neither gain nor loss)
 * sell27 : 10% * sell27 (positive cost of selling 27, because it makes us book a tax gain
 * sellOfWorstLot : 20% * sellOfWorstLot
 *
 * The objective function is therefore
 * T * (-0.3 * sell11 + 0.1 * sell27 + 0.2 * sellOfWorstLot) - L * (sellX + buyY)
 *
 * The constraints are
 * {@code buyX > 0}
 * {@code sell11 + sell23 + sell27 + sellOfWorstLot = buyX} (we're using positive numbers for everything here)
 *
 * but we also need constraints to denote that tax lots can only be sold in a given order, the bigger the tax loss
 * (or smaller the tax gain), the earlier we will sell that lot. Here's how we'll do that.
 * <pre>
 * {@code 0 <= sell11 <= min(11, sellX)}
 * {@code 0 <= sell23 <= min(23, max(0, sellX - 11))}
 * {@code 0 <= sell27 <= min(27, max(0, sellX - (11 + 23)))}
 * {@code 0 <= sellOfWorstLot <= min(39, max(0, sellX - (11 + 23 + 27)))}
 * </pre>
 *
 * Let's see why this works. First of all, obviously the sells must be for a positive quantity (as we defined them),
 * and we could always sell 0, so the lower bound is always 0.
 * Let's look at the right sides now.
 *
 * sell11 makes sense; if we sell e.g. 8 shares of X (sellX == 8) it comes from the sell11 lot.
 * Then min(sellX, 11) = min(8, 11) = 8. So the upper bound for sell11 is 8 then.
 * If we sell e.g. 15 X, then we don't want to ever think that we sold more than 11 from the sell1 lot.
 * min(sellX, 11) = min(15, 11) = 11, so that's fine.
 *
 * sell23: min(23, max(sellX - 11, 0) basically means 'without exceeding 23 (the quantity of the sell23 lot),
 * we will sell out of this lot any quantity that we are trying to sell above 11'.
 *
 * etc.
 */
public class RawTaxLotsUsingMinAndMaxIntegrationTest {

  protected static class TestResult {

    private final double sell11;
    private final double sell23;
    private final double sell27;
    // This one has a less specific name because some tests use 39 and some 37
    private final double sellOfWorstLot;

    protected TestResult(double sell11, double sell23, double sell27, double sellOfWorstLot) {
      this.sell11 = sell11;
      this.sell23 = sell23;
      this.sell27 = sell27;
      this.sellOfWorstLot = sellOfWorstLot;
    }

    static TestResult verifiedTestResult(double sell11, double sell23, double sell27, double sellOfWorstLot, double sellX, double buyY) {
      assertEquals(sellX, sell11 + sell23 + sell27 + sellOfWorstLot, 1e-8);
      assertEquals(sellX, buyY, 1e-8);
      return new TestResult(sell11, sell23, sell27, sellOfWorstLot);
    }

    @Override
    public String toString() {
      return String.format("[sells for (11, 23, 27, worst) = ( %.3f %.3f %.3f %.3f )",
          sell11, sell23, sell27, sellOfWorstLot);
    }

    protected static TypeSafeMatcher<TestResult> testResultMatcher(TestResult expected) {
      return makeMatcher(expected,
          matchUsingDoubleAlmostEquals(v -> v.sell11, 1e-8),
          matchUsingDoubleAlmostEquals(v -> v.sell23, 1e-8),
          matchUsingDoubleAlmostEquals(v -> v.sell27, 1e-8),
          matchUsingDoubleAlmostEquals(v -> v.sellOfWorstLot, 1e-8));
    }

  }


  private HighLevelVarEvaluator evaluator = makeRealObject(HighLevelVarEvaluator.class);

  /**
   * When (almost) ignoring misallocation, we should just sell the 11 lot (which lets us harvest tax losses {@code =>} good)
   * and then sell the 23 lot as well, since we can't completely ignore misallocation in this test.
   */
  @Test
  public void almostIgnoringMisallocation_sells11_sells23() {
    // Note that we don't *have* to force the tax lots to be sort in a specified way. This particular example penalizes
    // tax lots in the objective in such a way that we would always sell the 27 lot before we sell any of the 39 lot.
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
    // tax lots in the objective in such a way that we would always sell the 27 lot before we sell any of the 39 lot.
    // However, this is not always guaranteed to happen in future more complicated scenarios.
    assertThat(
        getOptimizationResultWithConstrainedTaxLotSellingOrder(0.1, 999_999),
        testResultMatcher(new TestResult(0.11, 0.23, 0.27, 0.39)));
    assertThat(
        getOptimizationResultWithUnconstrainedTaxLotSellingOrder(0.1, 999_999),
        testResultMatcher(new TestResult(0.11, 0.23, 0.27, 0.39)));
  }

  /**
   * Let's revisit the objective.
   * T * (-0.3 * sell11 + 0.1 * sell27 + 0.2 * sellOfWorstLot) - L * (sellX + buyY)
   * In all these tests, we use T = 0.1. Also, sellX == buyY. Simplify to
   * -0.03 * sell11 + 0.01 * sell27 + 0.02 * sellOfWorstLot - 2 * L * sellX
   * we need to find the value of L such that it just forces us to sell the 27 lot. Remember that we'll always sell
   * the 23 lot; it just doesn't appear in the objective, because it has a coefficient of 0.
   *
   * That happens when selling some quantity of the 27 lot has same misallocation penalty as its the tax penalty.
   * Since each e.g. 0.01 of X sold increases both sell27 and sellX by the same amount, we have
   * {@code 0.01 = 2 * L <==> L = 0.005}
   *
   * Similarly, if we want to find the point that barely makes us want to sell the 39 lot, we have
   * {@code 0.02 = 2 * L <==> L == 0.01}
   *
   * Note that, in these tests, there will never be a case where we'll ever want to sell a partial lot.
   * This is because the penalties are linear. Once we have some sort of (approximated) quadratic penalty for the
   * misallocation, that will be possible.
   */
  @Test
  public void inBetween_constrainedTaxLotSellingOrder() {
    // Note that we don't *have* to force the tax lots to be sort in a specified way. This particular example penalizes
    // tax lots in the objective in such a way that we would always sell the 27 lot before we sell any of the 39 lot.
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
      assertThat(
          getOptimizationResult.apply(0.1, 0.01001),
          testResultMatcher(new TestResult(0.11, 0.23, 0.27, 0.39)));
    }
  }

  // 1st arg is T from above. Selling a $1 gain will force us to pay tax of e.g. $0.30-$0.40 (value doesn't matter here)
  // Paying that tax now (instead of postponing later) has some cost; we assume that this is equivalent to $0.1.
  // 2nd arg is L from above.
  private TestResult getOptimizationResultWithConstrainedTaxLotSellingOrder(double costOfBookingGains, double riskLambda) {
    HighLevelLPBuilder lpBuilder = makeRealHighLevelLPBuilder()
        .withHumanReadableLabel(label("maximize some combination of tax efficiency & allocation accuracy"));
    HighLevelVariablesBuilder varBuilder = lpBuilder.getHighLevelVariablesBuilder();
    // These per-tax-lot variables will later have their upper bound further restricted, but it can't hurt to start
    // with a loose upper bound of 1. The optimizer may actually be faster this way, e.g. because it can find an
    // initial feasible point sooner by using variable bounds instead of relying on constraints.
    RawVariable sell11 = varBuilder.addConstrainedRawVariable("sell11", Range.closed(0.0, 0.11));
    RawVariable sell23 = varBuilder.addConstrainedRawVariable("sell23", Range.closed(0.0, 0.23));
    RawVariable sell27 = varBuilder.addConstrainedRawVariable("sell27", Range.closed(0.0, 0.27));
    RawVariable sellOfWorstLot = varBuilder.addConstrainedRawVariable("sellOfWorstLot", Range.closed(0.0, 0.39));
    RawVariable buyY   = varBuilder.addConstrainedRawVariable("buyY",   Range.closed(0.0, 1.0));
    SuperVar sellX = varBuilder
        .addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(
            label("sellX"), sumOfDisjointHighLevelVars(sell11, sell23, sell27, sellOfWorstLot)));

    BiFunction<String, HighLevelVarExpression, MaxSuperVar> maxMaker = (label, expr) -> generateMax(
        varBuilder, label, zeroConstantHighLevelVarExpression(), expr);
    MaxSuperVar sell23max = maxMaker.apply("max(0, sellX - 11)", highLevelVarExpression(1.0, sellX, -0.11));
    MaxSuperVar sell27max = maxMaker.apply("max(0, sellX - 34)", highLevelVarExpression(1.0, sellX, doubleExplained(-0.34, -(0.11 + 0.23))));
    MaxSuperVar sellOfWorstLotmax = maxMaker.apply("max(0, sellX - 61)", highLevelVarExpression(1.0, sellX, doubleExplained(-0.61, -(0.11 + 0.23 + 0.27))));

    TriFunction<String, Double, SuperVar, MinSuperVar> minMaker = (label, constTerm, superVar) -> generateMin(
        varBuilder,
        label,
        constantHighLevelVarExpression(constantTerm(constTerm)),
        superVar.getHighLevelVarExpression());

    MinSuperVar sell11upper = minMaker.apply("min(11, sellX)",                         0.11, sellX);
    MinSuperVar sell23upper = minMaker.apply("min(23, max(0, sellX - 11)",             0.23, sell23max);
    MinSuperVar sell27upper = minMaker.apply("min(27, max(0, sellX - (11 + 23))",      0.27, sell27max);
    MinSuperVar sellOfWorstLotupper = minMaker.apply("min(39, max(0, sellX - (11 + 23 + 27))", 0.39, sellOfWorstLotmax);

    AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
        .setObjectiveFunction(
            simpleLinearObjectiveFunction(highLevelVarExpression(
                -0.3 * costOfBookingGains, sell11,
                0.1 * costOfBookingGains, sell27,
                0.2 * costOfBookingGains, sellOfWorstLot,
                -1 * riskLambda, sellX,
                -1 * riskLambda, buyY)))
        .addConstraint("sellX = buyX",       differenceOf2DisjointHighLevelVars(sellX, buyY), EQUAL_TO_SCALAR, 0)
        .addConstraint("sell11 <= its u.b.", differenceOf2DisjointHighLevelVars(sell11upper, sell11), GREATER_THAN_SCALAR, 0)
        .addConstraint("sell23 <= its u.b.", differenceOf2DisjointHighLevelVars(sell23upper, sell23), GREATER_THAN_SCALAR, 0)
        .addConstraint("sell27 <= its u.b.", differenceOf2DisjointHighLevelVars(sell27upper, sell27), GREATER_THAN_SCALAR, 0)
        .addConstraint("sellOfWorstLot <= its u.b.", differenceOf2DisjointHighLevelVars(sellOfWorstLotupper, sellOfWorstLot), GREATER_THAN_SCALAR, 0)
        .build());
    Function<HighLevelVar, Double> evaluate = superVar ->
        evaluator.evaluateHighLevelVar(superVar, variablesAndOptimalValues);
    return verifiedTestResult(
        evaluate.apply(sell11),
        evaluate.apply(sell23),
        evaluate.apply(sell27),
        evaluate.apply(sellOfWorstLot),
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
    RawVariable sell11 = varBuilder.addConstrainedRawVariable("sell11", Range.closed(0.0, 0.11));
    RawVariable sell23 = varBuilder.addConstrainedRawVariable("sell23", Range.closed(0.0, 0.23));
    RawVariable sell27 = varBuilder.addConstrainedRawVariable("sell27", Range.closed(0.0, 0.27));
    RawVariable sellOfWorstLot = varBuilder.addConstrainedRawVariable("sellOfWorstLot", Range.closed(0.0, 0.39));
    RawVariable buyY   = varBuilder.addConstrainedRawVariable("buyY",   Range.closed(0.0, 1.0));
    SuperVar sellX = varBuilder
        .addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(
            label("sellX"), sumOfDisjointHighLevelVars(sell11, sell23, sell27, sellOfWorstLot)));

    AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
        .setObjectiveFunction(
            simpleLinearObjectiveFunction(highLevelVarExpression(
                -0.3 * costOfBookingGains, sell11,
                0.1 * costOfBookingGains, sell27,
                0.2 * costOfBookingGains, sellOfWorstLot,
                -1 * riskLambda, sellX,
                -1 * riskLambda, buyY)))
        .addConstraint("sellX = buyX", differenceOf2DisjointHighLevelVars(sellX, buyY), EQUAL_TO_SCALAR, 0)
        .build());
    Function<HighLevelVar, Double> evaluate = superVar ->
        evaluator.evaluateHighLevelVar(superVar, variablesAndOptimalValues);
    return verifiedTestResult(
        evaluate.apply(sell11),
        evaluate.apply(sell23),
        evaluate.apply(sell27),
        evaluate.apply(sellOfWorstLot),
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

}
