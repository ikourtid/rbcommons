package com.rb.nonbiz.types;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;
import static com.rb.nonbiz.types.UnitFraction.isValidUnitFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

/**
 * Similar to {@link UnitFraction}, but it's a general fraction that can have any value,
 * including negative or values greater than 1.
 *
 * <p> Note that it is not necessarily in the set Q of rational fractions; see {@link RationalUnitFraction} for that,
 * if you need one that's always between 0 and 1 (inclusive). </p>
 *
 * <p> Unlike {@link UnitFraction} where the value must be in [0, 1], this is really just a {@code BigDecimal} wrapper. </p>
 *
 * <p> You might wonder - why does this even have 'Fraction' in its name, when it can just be any number?
 * We usually use this to describe numbers that are typically in the [0, 1] range, but can occasionally be outside that.
 * For example, the cash portion of a portfolio is typically in [0, 1].
 * However, if we model $100 of a stock with beta of 1.8 as $180 US stocks + cash of -$80,
 * then the modeled (not actual) cash portion can be negative. </p>
 *
 * @see UnitFraction
 */
public class SignedFraction extends PreciseValue<SignedFraction> {

  public static final SignedFraction SIGNED_FRACTION_0 = new SignedFraction(BigDecimal.ZERO);
  public static final SignedFraction SIGNED_FRACTION_1 = new SignedFraction(BigDecimal.ONE);

  private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000);

  @VisibleForTesting
  public static final SignedFraction DUMMY_SIGNED_FRACTION = new SignedFraction(BigDecimal.valueOf(-0.54321));

  protected SignedFraction(BigDecimal value) {
    super(value);
  }

  protected SignedFraction(BigDecimal value, double doubleValue) {
    super(value, doubleValue);
  }

  public static SignedFraction signedFraction(double value) {
    return signedFraction(BigDecimal.valueOf(value), value);
  }

  public static SignedFraction signedFractionInPct(double value) {
    return signedFraction(value / 100);
  }

  public static SignedFraction signedFractionInBps(double value) {
    return signedFraction(value / 10_000);
  }

  public static SignedFraction signedFraction(long numerator, long denominator) {
    RBPreconditions.checkArgument(denominator > 0);
    return new SignedFraction(new BigDecimal(numerator).divide(new BigDecimal(denominator), DEFAULT_MATH_CONTEXT));
  }

  public static SignedFraction signedFraction(String value) {
    return signedFraction(new BigDecimal(value));
  }

  public static SignedFraction signedFraction(BigDecimal value) {
    return new SignedFraction(value);
  }

  public static SignedFraction signedFraction(BigDecimal value, double doubleValue) {
    return new SignedFraction(value, doubleValue);
  }

  public boolean isOne() {
    return this.asBigDecimal().compareTo(BigDecimal.ONE) == 0;
  }

  public boolean isAlmostOne(Epsilon epsilon) {
    return asBigDecimal().subtract(BigDecimal.ONE).abs().compareTo(BigDecimal.valueOf(epsilon.doubleValue())) < 0;
  }

  public SignedFraction complement() {
    return signedFraction(BigDecimal.ONE.subtract(asBigDecimal()));
  }

  public SignedFraction negate() {
    return signedFraction(asBigDecimal().negate());
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

  public SignedFraction add(SignedFraction toAdd) {
    return signedFraction(asBigDecimal().add(toAdd.asBigDecimal()));
  }

  public SignedFraction add(double toAdd) {
    return signedFraction(asBigDecimal().add(new BigDecimal(toAdd, DEFAULT_MATH_CONTEXT)));
  }

  public static SignedFraction sum(Collection<SignedFraction> fractions) {
    SignedFraction sum = SIGNED_FRACTION_0;
    for (SignedFraction fraction : fractions) {
      sum = sum.add(fraction);
    }
    return sum;
  }

  public SignedFraction subtract(SignedFraction remainingToAllocate) {
    return signedFraction(asBigDecimal().subtract(remainingToAllocate.asBigDecimal()));
  }

  public SignedFraction multiply(double multiplier) {
    return signedFraction(asBigDecimal().multiply(BigDecimal.valueOf(multiplier)));
  }

  public SignedFraction multiply(SignedFraction other) {
    return signedFraction(asBigDecimal().multiply(other.asBigDecimal()));
  }

  public SignedFraction divide(SignedFraction other) {
    return divide(other.asBigDecimal());
  }

  public SignedFraction divide(BigDecimal other) {
    if (other.compareTo(BigDecimal.ZERO) == 0) {
      throw new IllegalArgumentException(smartFormat("Cannot divide %s by zero", asBigDecimal()));
    }
    return signedFraction(asBigDecimal().divide(other, DEFAULT_MATH_CONTEXT));
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
