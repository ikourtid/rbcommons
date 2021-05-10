package com.rb.nonbiz.math.optimization.highlevel.integration;

import com.google.common.collect.Range;
import com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues;
import com.rb.nonbiz.math.optimization.general.LinearOptimizationProgram;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import org.junit.Test;

import java.util.function.BiConsumer;

import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.EQUAL_TO_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.GREATER_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.LESS_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.OptimizationResultTest.assumeFeasible;
import static com.rb.nonbiz.math.optimization.general.SimpleLinearObjectiveFunction.simpleLinearObjectiveFunction;
import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.constantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar.generalSuperVarWithoutAddedConstraintsOrArtificialTerms;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarConstraint.highLevelVarConstraint;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.disjointHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.singleVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.sumOfDisjointHighLevelVars;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_LABEL;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

public class SimpleOptimizationIntegrationTest extends AbstractHighLevelOptimizationIntegrationTest {

  // minimize x subject to 2 <= x
  @Test
  public void minimizesSingleVariable() {
    HighLevelLPBuilder builder = makeBuilder();
    HighLevelVariablesBuilder varBuilder = builder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.atLeast(2.0));
    assertResults(
        builder.setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(x))),
        pair(x, 2.0));
  }

  // maximize x subject to x <= 10
  @Test
  public void maximizesSingleVariable() {
    HighLevelLPBuilder builder = makeBuilder();
    HighLevelVariablesBuilder varBuilder = builder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.atMost(10.0));
    assertResults(
        builder.setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(-1.0, x))),
        pair(x, 10.0));
  }

  // minimize 3x + 2y subject to x >= 5, y >= 6
  @Test
  public void allArePlainVariables() {
    HighLevelLPBuilder builder = makeBuilder();
    RawVariable x = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("x", Range.atLeast(5.0));
    RawVariable y = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("y", Range.atLeast(6.0));
    assertResults(
        builder.setObjectiveFunction(simpleLinearObjectiveFunction(disjointHighLevelVarExpression(3, x, 2, y))),
        pair(x, 5.0),
        pair(y, 6.0));
  }

  // minimize 3x + 2y subject to x >= 5, y >= 6
  @Test
  public void mixOfPlainAndSuperVariables() {
    HighLevelLPBuilder builder = makeBuilder();
    HighLevelVariablesBuilder varBuilder = builder.getHighLevelVariablesBuilder();
    RawVariable x = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("x", Range.atLeast(5.0));
    RawVariable y = builder.getHighLevelVariablesBuilder().addConstrainedRawVariable("y", Range.atLeast(6.0));
    SuperVar threeXplusTwoY = varBuilder.addSuperVar(
        generalSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, disjointHighLevelVarExpression(3, x, 2, y)));
    assertResults(
        builder.setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(threeXplusTwoY))),
        pair(x, 5.0),
        pair(y, 6.0));
  }

  // minimize 3x + 2y subject to x >= 5, y >= 6 (x and y are now supervars).
  // Note how the 3x + 2y supervar can be independently evaluated as a single value, unlike the previous test.
  @Test
  public void allAreSuperVars() {
    HighLevelLPBuilder builder = makeBuilder();
    HighLevelVariablesBuilder varBuilder = builder.getHighLevelVariablesBuilder();
    RawVariable x = varBuilder.addConstrainedRawVariable("x", Range.atLeast(5.0));
    RawVariable y = varBuilder.addConstrainedRawVariable("y", Range.atLeast(6.0));
    SuperVar threeXplusTwoY = varBuilder.addSuperVar(
        generalSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, disjointHighLevelVarExpression(3, x, 2, y)));
    assertResults(
        builder.setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(threeXplusTwoY))),
        pair(x, 5.0),
        pair(y, 6.0),
        pair(threeXplusTwoY, doubleExplained(27, 3 * 5 + 2 * 6)));
  }

  @Test
  public void rumAndCoke() {
    HighLevelLPBuilder builder = makeBuilder().withHumanReadableLabel(label("minimize cost of drink"));
    HighLevelVariablesBuilder varBuilder = builder.getHighLevelVariablesBuilder();

    // Amounts of rum and coke that we will use in the cocktail (in liters).
    // Customers will complain if there's not enough rum in the cocktail; use at least 50 ml
    // We can use any amount of coke, but of course it has to be positive.
    RawVariable rum  = varBuilder.addConstrainedRawVariable("rum",  Range.atLeast(0.05));
    RawVariable coke = varBuilder.addConstrainedRawVariable("coke", Range.atLeast(0.0));
    SuperVar cost = varBuilder.addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(
        label("cost of drink (rum costs $4.5 / liter, and coke $1.5)"),
        disjointHighLevelVarExpression(4.5, rum, 1.5, coke)));
    SuperVar sugar = varBuilder.addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(
        label("total sugar in grams (rum is 25 / liter; coke about 40)"),
        disjointHighLevelVarExpression(25, rum, 40, coke)));
    LinearOptimizationProgram lp = builder
        .setObjectiveFunction(simpleLinearObjectiveFunction(singleVarExpression(cost)))
        .addConstraint(highLevelVarConstraint(label("drink must be exactly 200 ml"),
            sumOfDisjointHighLevelVars(rum, coke), EQUAL_TO_SCALAR, constantTerm(0.2)))
        .addConstraint(highLevelVarConstraint(label("drink can't be too sweet, so no more than 7 grams of sugar"),
            sugar.getHighLevelVarExpression(), LESS_THAN_SCALAR, constantTerm(7)))
        .addConstraint(highLevelVarConstraint(label("drink can't be too cheap, so no less than $0.60"),
            cost.getHighLevelVarExpression(), GREATER_THAN_SCALAR, constantTerm(0.60)))
        .build();
    System.out.println(lp);
    AllRawVariablesAndOptimalValues solution = assumeFeasible(optimizer.minimize(lp)).getAllRawVariablesAndOptimalValues();
    BiConsumer<Double, HighLevelVar> asserter = (expected, var) ->
        assertEquals(expected, evaluator.evaluateHighLevelVar(var, solution), 1e-8);
    asserter.accept(0.1, coke); // 0.1 = 100 ml
    asserter.accept(0.1, rum);
    asserter.accept(doubleExplained(6.5, 25 * 0.1 + 40 * 0.1), sugar);

    // This solution makes sense. The % of coke
    // * can't go below 50%; we can't have a cheaper price, since a 50-50 mix already gives $0.60 (min allowed)
    // * shouldn't go above 50%: we want to minimize cost, and less coke (=> more rum) increases cost
    asserter.accept(doubleExplained(0.60, 4.5 * 0.1 + 1.5 * 0.1), cost);
  }

}
