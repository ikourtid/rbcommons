package com.rb.nonbiz.math.optimization.highlevel.integration;

import com.google.common.collect.Range;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues;
import com.rb.nonbiz.math.optimization.general.LinearOptimizationProgram;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.math.optimization.highlevel.AbsoluteValueSuperVars;
import org.junit.Ignore;
import org.junit.Test;

import java.util.function.BiConsumer;

import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.GREATER_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.OptimizationResultTest.assumeFeasible;
import static com.rb.nonbiz.math.optimization.general.SimpleLinearObjectiveFunction.simpleLinearObjectiveFunction;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.differenceOf2DisjointHighLevelVars;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.singleVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.sumOfHighLevelVars;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static org.junit.Assert.assertEquals;

public class AbsoluteValueOptimizationIntegrationTest extends AbstractHighLevelOptimizationIntegrationTest {

  // minimize abs(x) with no constraints
  @Test
  public void minimizeAbsOfUnconstrained_returns0() {
    HighLevelLPBuilder builder = makeBuilder();
    RawVariable x = builder.getHighLevelVariablesBuilder().addRawVariable("x");
    AbsoluteValueSuperVars absX = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        builder.getHighLevelVariablesBuilder(), "abs(x)", singleVarExpression(x));
    assertResults(
        builder.setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(absX.getAbsoluteValue()))),
        pair(x, 0.0),
        pair(absX.getPositivePart(), 0.0),
        pair(absX.getNegativePart(), 0.0),
        pair(absX.getSigned(), 0.0),
        pair(absX.getAbsoluteValue(), 0.0));
  }

  @Test
  public void blogPostExample1() {
    HighLevelLPBuilder builder = makeBuilder();
    RawVariable x = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("x", Range.closed(-5.0, -3.0));
    AbsoluteValueSuperVars absX = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        builder.getHighLevelVariablesBuilder(), "abs(x)", singleVarExpression(x));
    LinearOptimizationProgram lp = builder
        .withHumanReadableLabel(label("minimize |x| subject to -5 <= x <= -3"))
        .setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(absX.getAbsoluteValue())))
        .build();
    AllRawVariablesAndOptimalValues solution = assumeFeasible(optimizer.minimize(lp)).getAllRawVariablesAndOptimalValues();
    BiConsumer<Double, HighLevelVar> asserter = (expected, var) ->
        assertEquals(expected, evaluator.evaluateHighLevelVar(var, solution), 1e-8);
    asserter.accept(-3.0, x);
    asserter.accept(3.0, absX.getAbsoluteValue());
  }

  @Test
  public void blogPostExample2() {
    HighLevelLPBuilder builder = makeBuilder();
    RawVariable x = builder.getHighLevelVariablesBuilder().addRawVariable("x");
    AbsoluteValueSuperVars absX = absoluteValueSuperVarsGenerator.generateSmartAbsoluteValueSuperVars(
        builder.getHighLevelVariablesBuilder(), "abs(x)", singleVarExpression(x), Range.closed(-5.0, -3.0));
    LinearOptimizationProgram lp = builder
        .withHumanReadableLabel(label("minimize |x| subject to -5 <= x <= -3"))
        .setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(absX.getAbsoluteValue())))
        .build();
    AllRawVariablesAndOptimalValues solution = assumeFeasible(optimizer.minimize(lp)).getAllRawVariablesAndOptimalValues();
    BiConsumer<Double, HighLevelVar> asserter = (expected, var) ->
        assertEquals(expected, evaluator.evaluateHighLevelVar(var, solution), 1e-8);
    asserter.accept(-3.0, x);
    asserter.accept(3.0, absX.getAbsoluteValue());
  }

  @Test
  public void blogPostExample3() {
    HighLevelLPBuilder builder = makeBuilder();
    AbsoluteValueSuperVars absX = absoluteValueSuperVarsGenerator.generateAbsoluteValueSuperVarsOfSingleVariable(
        builder.getHighLevelVariablesBuilder(), "abs(x)", Range.closed(-5.0, -3.0));
    LinearOptimizationProgram lp = builder
        .withHumanReadableLabel(label("minimize |x| subject to -5 <= x <= -3"))
        .setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(absX.getAbsoluteValue())))
        .build();
    System.out.println(lp);
    AllRawVariablesAndOptimalValues solution = assumeFeasible(optimizer.minimize(lp)).getAllRawVariablesAndOptimalValues();
    BiConsumer<Double, HighLevelVar> asserter = (expected, var) ->
        assertEquals(expected, evaluator.evaluateHighLevelVar(var, solution), 1e-8);
    asserter.accept(-3.0, absX.getSigned());
    asserter.accept(3.0, absX.getAbsoluteValue());
  }

  @Test
  public void blogPostExample4() {
    HighLevelLPBuilder builder = makeBuilder();
    AbsoluteValueSuperVars absX = absoluteValueSuperVarsGenerator.generateAbsoluteValueSuperVarsOfSingleVariable(
        builder.getHighLevelVariablesBuilder(), "abs(x)", Range.closed(-5.0, -3.0));
    LinearOptimizationProgram lp = builder
        .withHumanReadableLabel(label("minimize |x| subject to -5 <= x <= -3 and |x| > 3.5"))
        .addConstraint("|x| > 3.5", absX.getAbsoluteValue().getHighLevelVarExpression(), GREATER_THAN_SCALAR, 3.5)
        .setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(absX.getAbsoluteValue())))
        .build();
    System.out.println(lp);
    AllRawVariablesAndOptimalValues solution = assumeFeasible(optimizer.minimize(lp)).getAllRawVariablesAndOptimalValues();
    BiConsumer<Double, HighLevelVar> asserter = (expected, var) ->
        assertEquals(expected, evaluator.evaluateHighLevelVar(var, solution), 1e-8);
    asserter.accept(-3.5, absX.getSigned());
    asserter.accept(3.5, absX.getAbsoluteValue());
  }

  @Test
  public void minimizeConstrainedX_absIsUnconstrained() {
    TriConsumer<Range<Double>, Double, Double> minimizer =
        (range, expectedX, expectedAbsX) -> {
          HighLevelLPBuilder builder = makeBuilder();
          RawVariable x = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("x", range);
          AbsoluteValueSuperVars absX = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
              builder.getHighLevelVariablesBuilder(), "abs(x)", singleVarExpression(x));
          assertResults(
              builder.setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(absX.getAbsoluteValue()))),
              pair(x, expectedX),
              pair(absX.getAbsoluteValue(), expectedAbsX));
        };
    minimizer.accept(Range.atLeast( 2.0),  2.0, 2.0);// minimize abs(x) subject to x > 2
    minimizer.accept(Range.atLeast(-2.0),  0.0, 0.0);// minimize abs(x) subject to x > -2
    minimizer.accept(Range.atMost(  2.0),  0.0, 0.0);// minimize abs(x) subject to x < 2
    minimizer.accept(Range.atMost( -2.0), -2.0, 2.0);// minimize abs(x) subject to x < -2
  }

  // Compared to minimizeConstrainedX_absIsUnconstrained, this uses a constrained abs value variable.
  // This means that we don't create a variable for the positive part if we know for sure the expression can
  // only be negative - and vice versa. This is meant as a performance / decluttering optimization
  // since it saves us from creating unnecessary variables.
  @Test
  public void minimizeConstrainedX_absIsConstrained() {
    minimizeAbsEfficiently(Range.atLeast( 2.0), Range.atLeast( 2.0),  2.0, 2.0); // minimize abs(x) subject to x > 2
    minimizeAbsEfficiently(Range.atLeast(-2.0), Range.atLeast(-2.0),  0.0, 0.0); // minimize abs(x) subject to x > -2
    minimizeAbsEfficiently(Range.atMost(  2.0), Range.atMost(  2.0),    0.0, 0.0); // minimize abs(x) subject to x < 2
    minimizeAbsEfficiently(Range.atMost( -2.0), Range.atMost( -2.0),  -2.0, 2.0); // minimize abs(x) subject to x < -2
  }

  private void minimizeAbsEfficiently(
      Range<Double> rangeForVar,
      Range<Double> rangeForAbs, double expectedX, double expectedAbsX) {
    HighLevelLPBuilder builder = makeBuilder();
    RawVariable x = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("x", rangeForVar);
    AbsoluteValueSuperVars absX = absoluteValueSuperVarsGenerator.generateSmartAbsoluteValueSuperVars(
        builder.getHighLevelVariablesBuilder(), "abs(x)", singleVarExpression(x), rangeForAbs);
    assertResults(
        builder.setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(absX.getAbsoluteValue()))),
        pair(x, expectedX),
        pair(absX.getAbsoluteValue(), expectedAbsX));
  }

  @Test
  public void absMustBeConstant_createsNoVars_canHandle() {
    // We can't run an optimization with 0 variables.
    // That's too much of a corner case, so I won't worry about solving this in code.
    // However, in order for me to test that a fully constrained absolute variable becomes a constant,
    // I'll have to introduce some other variable (y).
    HighLevelLPBuilder builder = makeBuilder();
    RawVariable x = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("x", Range.closed(7.0, 7.0));
    RawVariable y = builder.getHighLevelVariablesBuilder().addRawVariable("y");
    AbsoluteValueSuperVars absX = absoluteValueSuperVarsGenerator.generateSmartAbsoluteValueSuperVars(
        builder.getHighLevelVariablesBuilder(), "abs(x)", singleVarExpression(x), Range.closed(7.0, 7.0));
    AbsoluteValueSuperVars absY = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        builder.getHighLevelVariablesBuilder(), "abs(y)", singleVarExpression(y));
    assertResults(
        builder
            .setObjectiveFunction(simpleLinearObjectiveFunction(
                sumOfHighLevelVars(absX.getAbsoluteValue(), absY.getAbsoluteValue()))),
        pair(x, 7.0),
        pair(y, 0.0),
        pair(absX.getAbsoluteValue(), 7.0),
        pair(absY.getAbsoluteValue(), 0.0));
  }

  @Ignore("This will not work; see below on why")
  @Test
  public void maximizeConstrainedX_absIsUnconstrained_doesNotWork() {
    // MAXIMIZING instead of minimizing an objective function does not work, I think.
    // Minimizing |x| is easy because when we minimize x_pos - x_neg, we want to push both towards zero,
    // so one of them ends up being 0. However, when we maximize |x|, both can get huge.
    // E.g. if the solution is 5, then pos = 1,000,005 and neg = 1,000,000 would work -
    // but we want only one of the two (pos, neg) to be non-zero normally. We can't enforce an 'either'
    // constraint, so the way this normally works is implicitly through the minimization,
    // since both get pushed towards 0.
    TriConsumer<Range<Double>, Double, Double> maximizer =
        (range, expectedX, expectedAbsX) -> {
          HighLevelLPBuilder builder = makeBuilder();
          RawVariable x = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("x", range);
          AbsoluteValueSuperVars absX = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
              builder.getHighLevelVariablesBuilder(), "abs(x)", singleVarExpression(x));
          assertResults(
              builder.setObjectiveFunction(simpleLinearObjectiveFunction(
                  singleVarExpression(-1.0, absX.getAbsoluteValue()))),
              pair(x, expectedX),
              pair(absX.getAbsoluteValue(), expectedAbsX));
        };
    maximizer.accept(Range.closed(-5.0,  2.0),  5.0, 5.0); // maximize abs(x) subject to -5 < x < 2
    maximizer.accept(Range.closed(-5.0, -3.0), -5.0, 5.0); // maximize abs(x) subject to -5 < x < -3
    maximizer.accept(Range.closed(-3.0,  5.0),  5.0, 5.0); // maximize abs(x) subject to -3 < x < 5
    maximizer.accept(Range.closed( 3.0,  5.0),  5.0, 5.0); // maximize abs(x) subject to  3 < x < 5
    maximizer.accept(Range.closed(-5.0, -5.0), -5.0, 5.0); // maximize abs(x) subject to -5 < x < -5
    maximizer.accept(Range.closed(-5.0, -5.0), -5.0, 5.0); // maximize abs(x) subject to  5 < x < 5
  }

  // Minimize abs(x - y) subject to 2 < x < 5, 6 < y.
  @Test
  public void simpleAbsValue_usesLessEfficientUnconstrainedAbsValSuperVar() {
    HighLevelLPBuilder builder = makeBuilder();
    RawVariable x = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("x", Range.closed(2.0, 5.0));
    RawVariable y = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("y", Range.atLeast(6.0));
    AbsoluteValueSuperVars absDiff = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        builder.getHighLevelVariablesBuilder(), "abs(x-y)", differenceOf2DisjointHighLevelVars(x, y));
    assertResults(
        builder.setObjectiveFunction(simpleLinearObjectiveFunction(
            singleVarExpression(absDiff.getAbsoluteValue()))),
        pair(x, 5.0),
        pair(y, 6.0),
        pair(absDiff.getAbsoluteValue(), 1.0));
  }

  // Minimize abs(y - x) subject to 2 < x < 5, 6 < y.
  @Test
  public void simpleAbsValue_reversedSigns() {
    HighLevelLPBuilder builder = makeBuilder();
    RawVariable x = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("x", Range.closed(2.0, 5.0));
    RawVariable y = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("y", Range.atLeast(6.0));
    AbsoluteValueSuperVars absDiff = absoluteValueSuperVarsGenerator.generateDumbAbsoluteValueSuperVars(
        builder.getHighLevelVariablesBuilder(), "abs(y-x)", differenceOf2DisjointHighLevelVars(y, x));
    assertResults(
        builder.setObjectiveFunction(simpleLinearObjectiveFunction(
            singleVarExpression(absDiff.getAbsoluteValue()))),
        pair(x, 5.0),
        pair(y, 6.0),
        pair(absDiff.getAbsoluteValue(), 1.0));
  }

}
