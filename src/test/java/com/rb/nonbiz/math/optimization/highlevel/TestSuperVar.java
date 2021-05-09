package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.text.HumanReadableLabel;

import java.util.List;

import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.constantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.DisjointVariablesSafeguard.CHECK_FOR_DISJOINT_RAW_VARIABLES;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarConstraint.minMaxConstraints;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.disjointHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarWithWeight.highLevelVarWithWeight;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_LABEL;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class TestSuperVar extends SuperVar {

  private final double weight1;
  private final double weight2;
  private final ConstantTerm constantTerm;
  private final RawVariable var1;
  private final RawVariable var2;

  public TestSuperVar(double weight1, double weight2, double constTerm) {
    this.weight1 = weight1;
    this.weight2 = weight2;
    this.constantTerm = constantTerm(constTerm);
    this.var1 = rawVariable("x", 0);
    this.var2 = rawVariable("y", 2);
  }

  public TestSuperVar(double weight1, RawVariable var1, double weight2, RawVariable var2, double constTerm) {
    this.weight1 = weight1;
    this.weight2 = weight2;
    this.constantTerm = constantTerm(constTerm);
    this.var1 = var1;
    this.var2 = var2;
  }

  @Override
  public HumanReadableLabel getHumanReadableLabel() {
    return DUMMY_LABEL;
  }

  @Override
  public HighLevelVarExpression getHighLevelVarExpression() {
    return disjointHighLevelVarExpression(
        ImmutableList.of(
            highLevelVarWithWeight(var1, weight1),
            highLevelVarWithWeight(var2, weight2)),
        constantTerm);
  }

  @Override
  public List<HighLevelVarConstraint> getAdditionalConstraints() {
    return minMaxConstraints(
        closedUnitFractionRange(unitFraction(0.11), unitFraction(0.33)),
        CHECK_FOR_DISJOINT_RAW_VARIABLES)
        .apply(this);
  }

}
