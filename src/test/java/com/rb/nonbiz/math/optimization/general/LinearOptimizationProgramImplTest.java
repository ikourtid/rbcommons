package com.rb.nonbiz.math.optimization.general;

import com.google.common.collect.Range;
import com.rb.biz.investing.strategy.optbased.GlobalObjective;
import com.rb.biz.investing.strategy.rulesbased.tlh.InstrumentClass;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.junit.Test;

import static com.google.common.collect.Range.atLeast;
import static com.rb.biz.investing.strategy.optbased.GlobalObjectiveTest.trackingOnlyGlobalObjective;
import static com.rb.biz.investing.strategy.optbased.NaiveSubObjective.naiveSubObjective;
import static com.rb.biz.investing.strategy.optbased.NaiveSubObjectiveCoefficientsTest.naiveSubObjectiveCoefficientsUsingAbs;
import static com.rb.biz.investing.strategy.optbased.TrackingSubObjectiveTest.naiveOnlyTrackingSubObjective;
import static com.rb.biz.investing.strategy.rulesbased.tlh.InstrumentClassTest.instrumentClass;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.allRawVariablesInOrder;
import static com.rb.nonbiz.math.optimization.general.LPBuilder.lpBuilder;
import static com.rb.nonbiz.math.optimization.general.LinearObjectiveFunctionWithArtificialTermsTest.linearObjectiveFunctionWithNoArtificialTerms;
import static com.rb.nonbiz.math.optimization.general.LinearOptimizationProgramTest.linearOptimizationProgramMatcher;
import static com.rb.nonbiz.math.optimization.general.NormalizedLeafSubObjective.normalizedNaiveSubObjective;
import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplierTest.unitNaiveSubObjectiveNormalization;
import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.general.ScalingInstructionsForImprovedLpAccuracy.noScalingForImprovedLpAccuracy;
import static com.rb.nonbiz.math.optimization.general.WeightedRawVariables.weightedRawVariables;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.disjointHighLevelVarExpression;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertNullPointerException;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

public class LinearOptimizationProgramImplTest extends RBTestMatcher<LinearOptimizationProgram> {

  private final InstrumentClass INSTRUMENT_CLASS_A = instrumentClass(1, "A", STOCK_A);
  private final InstrumentClass INSTRUMENT_CLASS_B = instrumentClass(2, "B", STOCK_B);
  private final InstrumentClass INSTRUMENT_CLASS_C = instrumentClass(3, "C", STOCK_C);

  private final RawVariable var0 = rawVariable("var0", 0);
  private final RawVariable var1 = rawVariable("var1", 1);
  private final RawVariable var2 = rawVariable("var2", 2);
  private final AllRawVariablesInOrder allVars = allRawVariablesInOrder(var0, var1, var2);

  RawVariable unknownVar = rawVariable("var3", 12345);

  private final GlobalObjective properObjective = trackingOnlyGlobalObjective(
      naiveOnlyTrackingSubObjective(normalizedNaiveSubObjective(
          naiveSubObjective(disjointHighLevelVarExpression(-100.1, var1, 100.2, var2),
              naiveSubObjectiveCoefficientsUsingAbs(doubleMap(rbMapOf(
                  INSTRUMENT_CLASS_A, 1.1,
                  INSTRUMENT_CLASS_B, 2.2)))),
          unitNaiveSubObjectiveNormalization())));
  private final double[] properConstraintCoefficients = new double[] { 1.1, 2.2, 0.0 };
  private final Range<Double> properRange = atLeast(0.1234);

  @Test
  public void happyPath_onlyHasConstraints_doesNotThrow() {
    LinearOptimizationProgram ignored = lpBuilder(allVars)
        .withScalingInstructionsForImprovedLpAccuracy(new ScalingInstructionsForImprovedLpAccuracyTest().makeDummyObject())
        .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithNoArtificialTerms(properObjective))
        .withRawVariableCombinationGreaterThanScalar("test constraint name", properConstraintCoefficients, 33.3)
        .build();
  }

  @Test
  public void happyPath_onlyHasVariableRanges_doesNotThrow() {
    LinearOptimizationProgram ignored = lpBuilder(allVars)
        .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithNoArtificialTerms((properObjective)))
        .withScalingInstructionsForImprovedLpAccuracy(new ScalingInstructionsForImprovedLpAccuracyTest().makeDummyObject())
        .withVariableRange(var0, properRange)
        .build();
  }

  @Test
  public void hasNoConstraintsOrVariableBounds_throws() {
    assertNullPointerException( () -> lpBuilder(allVars)
        .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithNoArtificialTerms(properObjective))
        .build());
  }

  @Test
  public void hasNoObjective_throws() {
    assertNullPointerException( () -> lpBuilder(allVars)
        .withVariableRange(var0, properRange)
        .build());
  }

  @Test
  public void attemptingToSetObjectiveFunctionAgain_throws() {
    assertIllegalArgumentException( () -> lpBuilder(allVars)
        .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithNoArtificialTerms(properObjective))
        .withLinearObjectiveFunctionWithArtificialTermsToMinimize(linearObjectiveFunctionWithNoArtificialTerms(properObjective)));
  }

  @Test
  public void triesToBoundUnknownVariable_throws() {
    assertIllegalArgumentException( () -> lpBuilder(allVars).withVariableRange(unknownVar, properRange));
  }

  @Test
  public void triesToSetAPointlessUnboundedRange_throws() {
    assertIllegalArgumentException( () -> lpBuilder(allVars).withVariableRange(var0, Range.all()));
  }

  @Override
  public LinearOptimizationProgram makeTrivialObject() {
    RawVariable var = rawVariable("x", 0);
    AllRawVariablesInOrder allRawVariablesInOrder = allRawVariablesInOrder(var);
    return lpBuilder(allRawVariablesInOrder)
        .withLinearObjectiveFunctionWithArtificialTermsToMinimize(
            new LinearObjectiveFunctionWithArtificialTermsTest().makeTrivialObject())
        .withScalingInstructionsForImprovedLpAccuracy(noScalingForImprovedLpAccuracy())
        .withInitialFeasiblePoint(new InitialFeasiblePointTest().makeTrivialObject())
        .withVariableRange(var, Range.closed(-123.45, 543.21))
        .build();
  }

  @Override
  public LinearOptimizationProgram makeNontrivialObject() {
    RawVariable varX = rawVariable("x", 0);
    RawVariable varY = rawVariable("y", 1);
    RawVariable varZ = rawVariable("z", 2);
    AllRawVariablesInOrder allRawVariablesInOrder = allRawVariablesInOrder(varX, varY, varZ);
    return lpBuilder(allRawVariablesInOrder)
        .withLinearObjectiveFunctionWithArtificialTermsToMinimize(
            new LinearObjectiveFunctionWithArtificialTermsTest().makeNontrivialObject())
        .withScalingInstructionsForImprovedLpAccuracy(new ScalingInstructionsForImprovedLpAccuracyTest().makeNontrivialObject())
        .withInitialFeasiblePoint(new InitialFeasiblePointTest().makeNontrivialObject())
        // no range on X
        .withVariableRange(varY, Range.closed(-11.11, 11.11))
        .withVariableRange(varZ, Range.closed(-22.22, 22.22))
        .withVariableCombinationEqualToScalar("2x + 3y = 4", weightedRawVariables(doubleMap(rbMapOf(varX, 2.0, varY, 3.0))), 4.0)
        .withVariableCombinationEqualToScalar("5x + -6z = 7", weightedRawVariables(doubleMap(rbMapOf(varX, 5.0, varY, -6.0))), 7.0)
        .withVariableCombinationLessThanScalar("9x + 8y + 7z < -6", weightedRawVariables(doubleMap(rbMapOf(varX, 9.0, varY, 8.0, varZ, 7.0))), -6)
        .withHumanReadableLabel(label("abc123"))
        .build();
  }

  @Override
  public LinearOptimizationProgram makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    RawVariable varX = rawVariable("x", 0);
    RawVariable varY = rawVariable("y", 1);
    RawVariable varZ = rawVariable("z", 2);
    AllRawVariablesInOrder allRawVariablesInOrder = allRawVariablesInOrder(varX, varY, varZ);
    return lpBuilder(allRawVariablesInOrder)
        .withLinearObjectiveFunctionWithArtificialTermsToMinimize(
            new LinearObjectiveFunctionWithArtificialTermsTest().makeMatchingNontrivialObject())
        .withScalingInstructionsForImprovedLpAccuracy(new ScalingInstructionsForImprovedLpAccuracyTest().makeMatchingNontrivialObject())
        .withInitialFeasiblePoint(new InitialFeasiblePointTest().makeMatchingNontrivialObject())
        // no range on X
        .withVariableRange(varY, Range.closed(-11.11 + e, 11.11 + e))
        .withVariableRange(varZ, Range.closed(-22.22 + e, 22.22 + e))
        .withVariableCombinationEqualToScalar("2x + 3y = 4", weightedRawVariables(doubleMap(rbMapOf(varX, 2.0 + e, varY, 3.0 + e))), 4.0 + e)
        .withVariableCombinationEqualToScalar("5x + -6z = 7", weightedRawVariables(doubleMap(rbMapOf(varX, 5.0 + e, varY, -6.0 + e))), 7.0 + e)
        .withVariableCombinationLessThanScalar("9x + 8y + 7z < -6", weightedRawVariables(doubleMap(rbMapOf(varX, 9.0 + e, varY, 8.0 + e, varZ, 7.0 + e))), -6 + e)
        .withHumanReadableLabel(label("abc123"))
        .build();
  }

  @Override
  protected boolean willMatch(LinearOptimizationProgram expected, LinearOptimizationProgram actual) {
    return linearOptimizationProgramMatcher(expected).matches(actual);
  }
  
  // If you're looking for the TypeSafeMatcher<LinearOptimizationProgramImpl> in this (usual) place,
  // note that TypeSafeMatcher<LinearOptimizationProgram> exists in LinearOptimizationProgramTest instead.

}
