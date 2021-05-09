package com.rb.nonbiz.math.optimization.highlevel.integration;

import com.google.common.collect.Range;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues;
import com.rb.nonbiz.math.optimization.general.LinearOptimizationProgram;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.math.optimization.highlevel.AbsoluteValueSuperVars;
import com.rb.nonbiz.math.optimization.highlevel.AbsoluteValueSuperVarsGenerator;
import com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVars;
import com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarsGenerator;
import com.rb.nonbiz.math.optimization.highlevel.integration.RawTaxLotsUsingMinAndMaxIntegrationTest.TestResult;
import org.junit.Test;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.EQUAL_TO_SCALAR;
import static com.rb.nonbiz.math.optimization.general.OptimizationResultTest.assumeFeasible;
import static com.rb.nonbiz.math.optimization.general.SimpleLinearObjectiveFunction.simpleLinearObjectiveFunction;
import static com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar.generalSuperVarWithoutAddedConstraintsOrArtificialTerms;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelLPBuilderTest.makeRealHighLevelLPBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.highLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.sumOfDisjointHighLevelVars;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValuesTest.linearApproximationVarRangesAndValuesForQuadratic;
import static com.rb.nonbiz.math.optimization.highlevel.integration.RawTaxLotsUsingMinAndMaxIntegrationTest.TestResult.testResultMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.integration.RawTaxLotsUsingMinAndMaxIntegrationTest.TestResult.verifiedTestResult;
import static com.rb.nonbiz.math.optimization.lpsolve.LpSolveLinearOptimizerTest.makeRealLpSolveLinearOptimizer;
import static com.rb.nonbiz.testutils.RBIntegrationTest.makeRealObject;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * In fact, you should read that test before you read this one.
 *
 * The difference here is that we use a quadratic (approximated) misallocation penalty for misallocation.
 * Basically, this means that e.g. holding X at 82% vs 80%
 * is more than twice as bad as holding 81% instead of 80%.
 *
 * The reason we are doing this is to simulate a situation where we want to sell a partial lot. If the misallocation
 * penalty is linear (even if it's the absolute value), then the only time we'd ever sell a partial lot is if the full
 * lot 'straddles' the ideal position. For example, if X target is 80% and we have a 65 and 35 lot, then we can't get
 * to 80 without selling some partial lot (whether the 65 or 35).
 *
 * @see RawTaxLotsUsingAbsAndMinAndMaxIntegrationTest
 */
public class RawTaxLotsQuadraticMisallocationIntegrationTest {

  private HighLevelVarEvaluator evaluator = makeRealObject(HighLevelVarEvaluator.class);

  /**
   * When (almost) ignoring misallocation, we should just sell the 11 lot (which lets us harvest tax losses {@code =>} good)
   * and then sell the 23 lot as well, since we can't completely ignore misallocation in this test.
   */
  @Test
  public void almostIgnoringMisallocation_sells11_sells23() {
    assertThat(
        getOptimizationResult(0.1, 1e-5),
        testResultMatcher(new TestResult(0.11, 0.23, 0, 0)));
  }

  /**
   * When (almost) ignoring tax efficiency, we just sell everything to get to target
   */
  @Test
  public void almostIgnoringTaxEfficiency_constrainedTaxLotSellingOrder_sellsAllX() {
    // only need to sell 17 of the 37 lot to get to target; the remaining 20 (0.2) matches the 20% X target.
    assertThat(
        getOptimizationResult(0.1, 999_999),
        testResultMatcher(new TestResult(0.11, 0.23, 0.27, 0.17)));
  }

  @Test
  public void inBetween_constrainedTaxLotSellingOrder() {
    TriConsumer<Double, Integer, Integer> asserter = (riskLambda, sell27, sell37) -> assertThat(
        getOptimizationResult(0.1, riskLambda),
        testResultMatcher(new TestResult(0.11, 0.23, 0.01 * sell27, 0.01 * sell37)));

    // OK, so clearly I ran some code and then generated the following assertions... not the other way round.
    // It is hard to show why these particular numbers should work. But the general idea should be clear:
    //
    // a) monotonicity: as the risk lambda (1st arg in the asserter) increases, i.e. we care more about
    // matching the target, then we sell more of the 3rd 23-lot (2nd arg in the asserter) and possibly the 4th 3-lot
    // (3rd arg in the asserter) in order to match the target. As per the test
    // almostIgnoringTaxEfficiency_constrainedTaxLotSellingOrder_sellsAllX
    // the most we'd ever sell is all of the 23-lot and 17 shares from the 37 lot (since the target for X is 20, so
    // we want to hold at least 20; selling more than that will make both tax efficiency AND target allocation
    // accuracy worse).
    asserter.accept(0.0050, 0, 0);
    asserter.accept(0.0060, 2, 0);
    asserter.accept(0.0070, 8, 0);
    asserter.accept(0.0080, 13, 0);
    asserter.accept(0.0090, 16, 0);
    asserter.accept(0.0100, 19, 0);
    asserter.accept(0.0110, 21, 0);
    asserter.accept(0.0120, 23, 0);
    asserter.accept(0.0130, 25, 0);
    asserter.accept(0.0140, 26, 0);

    // b) 'quadratic-ness': look at the cases where we are selling all of the 27 lot below. This way you avoid
    // looking at the confounding factor that the 27 lot has a different tax efficiency penalty than the 37 lot.
    // Now, there is a relatively small change to the required risk lambda (0.015 -> 0.058)
    // to make us sell 8 shares of the 37 lot. However, to sell another 8 (almost the entire 17 shares which
    // would make us hit the target), we need to go from 0.058 to 0.334, a bigger change. This is because selling
    // the last 8 shares will make less of a difference to the square of the misallocation, since we already got
    // pretty close to it. In other words, we don't get as much about getting close to perfection, so there needs to
    // be a big increase in the risk lambda in order for us to bother selling gain-y shares to get to our target.
    // Note that this is also approximated, since we are not *really* using the square here.
    //
    // Also, note that there's nothing in the optimization that forces the optimizer to return integers (or
    // integer multiples of 0.01, to be exact). That just happens because of the # of points in the quadratic
    // approximation, which are now 100 because otherwise the test takes a bit long to run if I use a finer
    // approximation. In fact,
    // when I ran it with 500 points (local change), I got a case where e.g. we tried to sell 0.056 from the 0.27 of
    // the 3rd lot. I'm using ints here because it makes each test in the asserter lines below easier to read.
    asserter.accept(0.0150, 27, 0);
    asserter.accept(0.0300, 27, 0);
    asserter.accept(0.0310, 27, 1);
    asserter.accept(0.0320, 27, 1);
    asserter.accept(0.0330, 27, 2);
    asserter.accept(0.0340, 27, 2);
    asserter.accept(0.0350, 27, 3);
    asserter.accept(0.0370, 27, 3);
    asserter.accept(0.0390, 27, 4);
    asserter.accept(0.0400, 27, 5);
    asserter.accept(0.0430, 27, 5);
    asserter.accept(0.0440, 27, 6);
    asserter.accept(0.0470, 27, 6);
    asserter.accept(0.0480, 27, 7);
    asserter.accept(0.0520, 27, 7);
    asserter.accept(0.0530, 27, 8);
    asserter.accept(0.0580, 27, 8);
    asserter.accept(0.0590, 27, 9);
    asserter.accept(0.0660, 27, 9);
    asserter.accept(0.0670, 27, 10);
    asserter.accept(0.0760, 27, 10);
    asserter.accept(0.0770, 27, 11);
    asserter.accept(0.0900, 27, 11);
    asserter.accept(0.0910, 27, 12);
    asserter.accept(0.1100, 27, 12);
    asserter.accept(0.1110, 27, 12);
    asserter.accept(0.1420, 27, 13);
    asserter.accept(0.1430, 27, 14);
    asserter.accept(0.1990, 27, 14);
    asserter.accept(0.2000, 27, 15);
    asserter.accept(0.3330, 27, 15);
    asserter.accept(0.3340, 27, 16);
  }

  private TestResult getOptimizationResult(double costOfBookingGains, double riskLambda) {
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

    List<Double> cutoffPointsForQuadraticApproximation = IntStream.range(0, 100)
        .mapToObj(i -> i / 100.0)
        .collect(Collectors.toList());
    BiFunction<String, AbsoluteValueSuperVars, LinearApproximationVars> squareMaker = (name, absVars) ->
        makeRealObject(LinearApproximationVarsGenerator.class).generateLinearApproximationOfExistingHighLevelVar(
            varBuilder,
            name,
            linearApproximationVarRangesAndValuesForQuadratic(cutoffPointsForQuadraticApproximation),
            absVars.getAbsoluteValue());
    LinearApproximationVars diffXsq = squareMaker.apply("|dX|²", absDiffX);
    LinearApproximationVars diffYsq = squareMaker.apply("|dY|²", absDiffY);

    AllRawVariablesAndOptimalValues variablesAndOptimalValues = calculateSolution(lpBuilder
        .setObjectiveFunction(
            simpleLinearObjectiveFunction(highLevelVarExpression(
                -0.3 * costOfBookingGains, sell11x,
                0.1 * costOfBookingGains, sell27x,
                0.2 * costOfBookingGains, sell37x,
                // I should also treat Y as having a lot of 2 that can be sold & which has a tax gain / loss
                // that should be implied in the objective function. I'm ignoring it for simplicity though.
                riskLambda, diffXsq.getApproximatedNonLinearPart(),
                riskLambda, diffYsq.getApproximatedNonLinearPart())))
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

  private AllRawVariablesAndOptimalValues calculateSolution(LinearOptimizationProgram lp) {
    return assumeFeasible(makeRealLpSolveLinearOptimizer().minimize(lp)).getAllRawVariablesAndOptimalValues();
  }

}
