package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.types.UnitFraction.isValidUnitFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

/**
 * FIXME IAK WGT comment, AND ALSO remove what's not needed.
 */
public class SubObjectiveTreeWeight extends PreciseValue<SubObjectiveTreeWeight> {

  public static final SubObjectiveTreeWeight SUB_OBJECTIVE_WEIGHT_0 = new SubObjectiveTreeWeight(BigDecimal.ZERO);
  public static final SubObjectiveTreeWeight SUB_OBJECTIVE_WEIGHT_1 = new SubObjectiveTreeWeight(BigDecimal.ONE);

  private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000);

  protected SubObjectiveTreeWeight(BigDecimal value) {
    super(value);
  }

  protected SubObjectiveTreeWeight(BigDecimal value, double doubleValue) {
    super(value, doubleValue);
  }

  public static SubObjectiveTreeWeight subObjectiveTreeWeight(double value) {
    return subObjectiveTreeWeight(BigDecimal.valueOf(value), value);
  }

  public static SubObjectiveTreeWeight subObjectiveTreeWeightInPct(double value) {
    return subObjectiveTreeWeight(value / 100);
  }

  public static SubObjectiveTreeWeight subObjectiveTreeWeightInBps(double value) {
    return subObjectiveTreeWeight(value / 10_000);
  }

  public static SubObjectiveTreeWeight subObjectiveTreeWeight(String value) {
    return subObjectiveTreeWeight(new BigDecimal(value));
  }

  public static SubObjectiveTreeWeight subObjectiveTreeWeight(BigDecimal value) {
    return new SubObjectiveTreeWeight(value);
  }

  public static SubObjectiveTreeWeight subObjectiveTreeWeight(BigDecimal value, double doubleValue) {
    return new SubObjectiveTreeWeight(value, doubleValue);
  }

  public boolean isOne() {
    return this.asBigDecimal().compareTo(BigDecimal.ONE) == 0;
  }

  public boolean isAlmostOne(double epsilon) {
    return asBigDecimal().subtract(BigDecimal.ONE).abs().compareTo(new BigDecimal(epsilon)) < 0;
  }

  @Override
  public String toString() {
    return toPercentString();
  }

  public String toPercentString(int scale) {
    return toPercentString(scale, true);
  }

  public String toPercentString(int scale, boolean includePercentSign) {
    String pct = String.format("%." + scale + "f", 100 * doubleValue());
    return includePercentSign ? Strings.format("%s %", pct) : pct;
  }

  public String toPercentString() {
    return toPercentString(2);
  }

  public String toBasisPoints(int scale) {
    return toBasisPoints(scale, true);
  }

  public String toBasisPoints(int scale, boolean includeSuffix) {
    String bps = asBigDecimal().multiply(TEN_THOUSAND).setScale(scale, RoundingMode.HALF_EVEN).toString();
    return includeSuffix ? Strings.format("%s bps", bps) : bps;
  }

  public String toBasisPoints() {
    return toBasisPoints(2);
  }

  public SubObjectiveTreeWeight add(SubObjectiveTreeWeight toAdd) {
    return subObjectiveTreeWeight(asBigDecimal().add(toAdd.asBigDecimal()));
  }

  public SubObjectiveTreeWeight add(double toAdd) {
    return subObjectiveTreeWeight(asBigDecimal().add(new BigDecimal(toAdd, DEFAULT_MATH_CONTEXT)));
  }

  public static SubObjectiveTreeWeight sum(Collection<SubObjectiveTreeWeight> fractions) {
    SubObjectiveTreeWeight sum = SUB_OBJECTIVE_WEIGHT_0;
    for (SubObjectiveTreeWeight fraction : fractions) {
      sum = sum.add(fraction);
    }
    return sum;
  }

  public SubObjectiveTreeWeight subtract(SubObjectiveTreeWeight remainingToAllocate) {
    return subObjectiveTreeWeight(asBigDecimal().subtract(remainingToAllocate.asBigDecimal()));
  }

  public SubObjectiveTreeWeight multiply(double multiplier) {
    return subObjectiveTreeWeight(asBigDecimal().multiply(BigDecimal.valueOf(multiplier)));
  }

  public SubObjectiveTreeWeight multiply(SubObjectiveTreeWeight other) {
    return subObjectiveTreeWeight(asBigDecimal().multiply(other.asBigDecimal()));
  }

  public SubObjectiveTreeWeight divide(SubObjectiveTreeWeight other) {
    return divide(other.asBigDecimal());
  }

  public SubObjectiveTreeWeight divide(BigDecimal other) {
    if (other.compareTo(BigDecimal.ZERO) == 0) {
      throw new IllegalArgumentException(Strings.format("Cannot divide %s by zero", asBigDecimal()));
    }
    return subObjectiveTreeWeight(asBigDecimal().divide(other, DEFAULT_MATH_CONTEXT));
  }

  public boolean canBeConvertedToUnitFraction() {
    return asBigDecimal().signum() >= 0 && asBigDecimal().compareTo(BigDecimal.ONE) <= 0;
  }

  public UnitFraction toUnitFraction() {
    BigDecimal value = asBigDecimal();
    RBPreconditions.checkArgument(
        isValidUnitFraction(value),
        "Could not convert signed fraction with value %s to a unitFraction in the range of [0, 1]",
        value);
    return unitFraction(value, doubleValue);
  }

}
