package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.Range;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.math.optimization.general.SingleSuperVarArtificialObjectiveFunctionTerms;
import com.rb.nonbiz.text.HumanReadableLabel;

import java.util.List;
import java.util.function.Function;

import static com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar.constantSuperVar;
import static com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar.generalSuperVar;
import static com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar.generalSuperVarWithoutAddedConstraintsOrArtificialTerms;
import static com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar.generalSuperVarWithoutArtificialTerms;
import static com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar.superVarOfSingleVariable;
import static com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar.zeroConstantSuperVar;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationIndividualSegmentSuperVar.linearApproximationIndividualSegmentSuperVar;
import static com.rb.nonbiz.math.optimization.highlevel.MaxSuperVar.maxOfConstantsSuperVar;
import static com.rb.nonbiz.math.optimization.highlevel.MaxSuperVar.maxSuperVar;
import static com.rb.nonbiz.math.optimization.highlevel.MinSuperVar.minOfConstantsSuperVar;
import static com.rb.nonbiz.math.optimization.highlevel.MinSuperVar.minSuperVar;

/**
 * Every time you add a static constructor to any subclass of {@link SuperVar}, you should also create a corresponding
 * test-only static constructor here, but one that returns the actual SuperVar, instead of a {@link PartialSuperVar}.
 *
 * For more, see {@link PartialSuperVar}.
 */
public class RBPublicTestOnlySuperVarConstructors {

  public static GeneralSuperVar testGeneralSuperVar(
      HumanReadableLabel label,
      HighLevelVarExpression highLevelVarExpression,
      Function<HighLevelVar, List<HighLevelVarConstraint>> generatorOfAddedConstraints,
      SingleSuperVarArtificialObjectiveFunctionTerms singleSuperVarArtificialObjectiveFunctionTerms) {
    return generalSuperVar(
        label, highLevelVarExpression, generatorOfAddedConstraints, singleSuperVarArtificialObjectiveFunctionTerms)
        .unsafeGetSuperVar();
  }

  public static GeneralSuperVar testGeneralSuperVarWithoutArtificialTerms(
      HumanReadableLabel label,
      HighLevelVarExpression highLevelVarExpression,
      Function<HighLevelVar, List<HighLevelVarConstraint>> generatorOfAddedConstraints) {
    return generalSuperVarWithoutArtificialTerms(label, highLevelVarExpression, generatorOfAddedConstraints)
        .unsafeGetSuperVar();
  }

  public static GeneralSuperVar testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(
      HumanReadableLabel label, HighLevelVarExpression highLevelVarExpression) {
    return generalSuperVarWithoutAddedConstraintsOrArtificialTerms(label, highLevelVarExpression)
        .unsafeGetSuperVar();
  }

  public static GeneralSuperVar testSuperVarOfSingleVariable(RawVariable rawVariable) {
    return superVarOfSingleVariable(rawVariable)
        .unsafeGetSuperVar();
  }

  public static GeneralSuperVar testSuperVarOfSingleVariable(
      RawVariable rawVariable,
      Function<HighLevelVar, List<HighLevelVarConstraint>> generatorOfAddedConstraints) {
    return superVarOfSingleVariable(rawVariable, generatorOfAddedConstraints)
        .unsafeGetSuperVar();
  }

  public static GeneralSuperVar testConstantSuperVar(HumanReadableLabel label, ConstantTerm constantTerm) {
    return constantSuperVar(label, constantTerm)
        .unsafeGetSuperVar();
  }

  public static GeneralSuperVar testZeroConstantSuperVar(HumanReadableLabel label) {
    return zeroConstantSuperVar(label)
        .unsafeGetSuperVar();
  }

  public static MinSuperVar testMinSuperVar(
      HumanReadableLabel label,
      RawVariable rawVariableForMin,
      Function<HighLevelVar, List<HighLevelVarConstraint>> generatorOfAddedConstraints,
      SingleSuperVarArtificialObjectiveFunctionTerms singleSuperVarArtificialObjectiveFunctionTerms) {
    return minSuperVar(
        label, rawVariableForMin, generatorOfAddedConstraints, singleSuperVarArtificialObjectiveFunctionTerms)
        .unsafeGetSuperVar();
  }

  public static MinSuperVar testMinOfConstantsSuperVar(HumanReadableLabel label, ConstantTerm constantTerm) {
    return minOfConstantsSuperVar(label, constantTerm)
        .unsafeGetSuperVar();
  }

  public static MaxSuperVar testMaxSuperVar(
      HumanReadableLabel label,
      RawVariable rawVariableForMax,
      Function<HighLevelVar, List<HighLevelVarConstraint>> generatorOfAddedConstraints,
      SingleSuperVarArtificialObjectiveFunctionTerms singleSuperVarArtificialObjectiveFunctionTerms) {
    return maxSuperVar(
        label, rawVariableForMax, generatorOfAddedConstraints, singleSuperVarArtificialObjectiveFunctionTerms)
        .unsafeGetSuperVar();
  }

  public static MaxSuperVar testMaxOfConstantsSuperVar(HumanReadableLabel label, ConstantTerm constantTerm) {
    return maxOfConstantsSuperVar(label, constantTerm)
        .unsafeGetSuperVar();
  }

  public static LinearApproximationIndividualSegmentSuperVar testLinearApproximationIndividualSegmentSuperVar(
      HumanReadableLabel label, RawVariable rawRawVariable, Range<Double> range, double slope) {
    return linearApproximationIndividualSegmentSuperVar(
        label, rawRawVariable, range, slope)
        .unsafeGetSuperVar();
  }

}
