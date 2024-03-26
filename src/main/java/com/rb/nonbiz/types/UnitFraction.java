package com.rb.nonbiz.types;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.collections.RBStreams.sumAsBigDecimals;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;

/**
 * Represents a continuous value from 0 to 1 (inclusive).
 *
 * <p> This is a misnomer, but we couldn't find a better name: </p>
 * <ol>
 *   <li> General fractions can of course be less than 0 or greater than 1. </li>
 *   <li> A {@code UnitFraction} object can have irrational values within [ 0, 1 ], e.g.
 *     {@code 0.5 * Math.sqrt(2)}, so it's not truly a fraction in the 'ratio of integers' sense. </li>
 </ol>
 *
 * See {@link RationalUnitFraction} for a special case that enforces that the values are rational numbers.
 */
public class UnitFraction extends PreciseValue<UnitFraction> {

  public static final UnitFraction UNIT_FRACTION_0 = new UnitFraction(BigDecimal.ZERO);
  public static final UnitFraction UNIT_FRACTION_1 = new UnitFraction(BigDecimal.ONE);
  public static final UnitFraction UNIT_FRACTION_ONE_HALF = unitFraction(1, 2);

  @VisibleForTesting
  public static final UnitFraction DUMMY_UNIT_FRACTION = new UnitFraction(BigDecimal.valueOf(0.12345));

  private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
  private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000);
  private static final BigDecimal BPS_TO_FRACTION = new BigDecimal("0.0001");

  protected UnitFraction(BigDecimal value) {
    super(value);
  }

  protected UnitFraction(BigDecimal value, double doubleValue) {
    super(value, doubleValue);
  }

  public static UnitFraction unitFraction(double value) {
    return unitFraction(BigDecimal.valueOf(value), value);
  }

  public static UnitFraction unitFractionInPct(double pct) {
    return unitFraction(pct / 100);
  }

  public static UnitFraction unitFractionInBps(double bps) {
    return unitFraction(bps / 10_000);
  }

  public static UnitFraction unitFractionInBps(BigDecimal bps) {
    return unitFraction(bps.multiply(BPS_TO_FRACTION));
  }

  public static UnitFraction unitFraction(BigDecimal value, double doubleValue) {
    RBPreconditions.checkArgument(
        isValidUnitFraction(value),
        "UnitFraction can be between 0 and 1 (inclusive) but was %s",
        value);
    return new UnitFraction(value, doubleValue);
  }

  /**
   * For numerical reasons, we may sometimes end up needing to instantiate a unitFraction that is a tiny bit negative,
   * or a tiny bit above 1. This lets you round those cases to 0 and 1, respectively,
   * while still throwing if the input is significantly outside [0, 1].
   */
  public static UnitFraction forgivingUnitFraction(double value, Epsilon epsilon) {
    return forgivingUnitFraction(BigDecimal.valueOf(value), epsilon);
  }

  /**
   * For numerical reasons, we may sometimes end up needing to instantiate a unitFraction that is a tiny bit negative,
   * or a tiny bit above 1. This lets you round those cases to 0 and 1, respectively,
   * while still throwing if the input is significantly outside [0, 1].
   */
  public static UnitFraction forgivingUnitFraction(BigDecimal value, Epsilon epsilon) {
    double doubleValue = value.doubleValue();
    return (-epsilon.doubleValue() <= doubleValue && value.signum() < 0) ? UNIT_FRACTION_0
        : (1 <= doubleValue && doubleValue <= 1 + epsilon.doubleValue()) ? UNIT_FRACTION_1
        // if value < -epsilon or value > 1 + epsilon, the unitFraction static constructor will throw
        : unitFraction(value, doubleValue);
  }

  /**
   * This is a convenience to the caller; we don't actually store a numerator and denominator in a UnitFraction.
   */
  public static UnitFraction unitFraction(long numerator, long denominator) {
    RBPreconditions.checkArgument(numerator >= 0);
    RBPreconditions.checkArgument(denominator > 0);
    RBPreconditions.checkArgument(
        numerator <= denominator,
        "Numerator %s must be <= denominator %s",
        numerator, denominator);
    return new UnitFraction(new BigDecimal(numerator).divide(new BigDecimal(denominator), DEFAULT_MATH_CONTEXT));
  }

  public static UnitFraction unitFraction(String value) {
    return unitFraction(new BigDecimal(value));
  }

  public static UnitFraction unitFraction(BigDecimal value) {
    RBPreconditions.checkArgument(
        isValidUnitFraction(value),
        "UnitFraction can be between 0 and 1 (inclusive) but was %s",
        value);
    return new UnitFraction(value);
  }

  public static boolean isValidUnitFraction(BigDecimal value) {
    return value.signum() >= 0 && value.compareTo(BigDecimal.ONE) <= 0;
  }

  public boolean isOne() {
    return this.asBigDecimal().compareTo(BigDecimal.ONE) == 0;
  }

  public boolean isAlmostOne(Epsilon epsilon) {
    return asBigDecimal().subtract(BigDecimal.ONE).abs().compareTo(BigDecimal.valueOf(epsilon.doubleValue())) < 0;
  }

  public boolean isAlmostExtreme(Epsilon epsilon) {
    return isAlmostZero(epsilon) || isAlmostOne(epsilon);
  }

  public UnitFraction complement() {
    return unitFraction(BigDecimal.ONE.subtract(asBigDecimal()));
  }

  @Override
  public String toString() {
    // default: use exactly two digits of precision
    return toPercentString(2, 2);
  }

  public String toPercentString() {
    return toPercentString(2);
  }

  public String toPercentString(int maxPrecision) {
    return toPercentString(maxPrecision, true);
  }

  public String toPercentString(int maxPrecision, boolean includePercentSign) {
    return toPercentString(0, maxPrecision, includePercentSign);
  }
  public String toPercentString(int minPrecision, int maxPrecision) {
    return toPercentString(minPrecision, maxPrecision, true);
  }

  public String toPercentString(int minPrecision, int maxPrecision, boolean includePercentSign) {
    // negative precisions could make sense (rounding 123.1 to 120.0 or 100.0), but we don't support them.
    // toPercentString() calls our formatWithoutCommas(), which in turn uses
    // DecimalFormat#setMinimumFractionDigits and DecimalFormat#setMaximumFractionDigits, both of
    // which enforce a minimum of 0 digits.
    RBPreconditions.checkArgument(
        0 <= minPrecision && minPrecision <= maxPrecision,
        "Invalid minPrecision %s or maxPrecision %s",
        minPrecision, maxPrecision);
    String percentString = formatWithoutCommas(minPrecision, maxPrecision).format(doubleValue() * 100.0);
    return includePercentSign
        ? Strings.format("%s %", percentString)
        : percentString;
  }

  public String toBasisPoints(int scale) {
    return toBasisPoints(scale, true);
  }

  public String toBasisPoints(int scale, boolean includeSuffix) {
    String bpsString = formatWithoutCommas(scale).format(asBigDecimal().multiply(TEN_THOUSAND));
    return includeSuffix ? bpsString + " bps" : bpsString;
  }

  public String toBasisPoints() {
    return toBasisPoints(2);
  }

  @Override
  public int compareTo(UnitFraction other) {
    return this.asBigDecimal().compareTo(other.asBigDecimal());
  }

  public UnitFraction add(UnitFraction toAdd) {
    return forgivingUnitFraction(asBigDecimal().add(toAdd.asBigDecimal(), DEFAULT_MATH_CONTEXT), epsilon(1e-15));
  }

  /**
   * For numerical reasons, adding two UnitFractions may give us a result that slightly above 1.
   * #add handles this already, but this allows us to control the epsilon of the 'forgiving-ness'.
   */
  public UnitFraction addForgiving(UnitFraction toAdd, Epsilon epsilon) {
    return forgivingUnitFraction(asBigDecimal().add(toAdd.asBigDecimal(), DEFAULT_MATH_CONTEXT), epsilon);
  }

  public UnitFraction add(double toAdd) {
    return forgivingUnitFraction(asBigDecimal().add(new BigDecimal(toAdd)), epsilon(1e-15));
  }

  public static UnitFraction sum(Collection<UnitFraction> unitFractions) {
    UnitFraction sum = UNIT_FRACTION_0;
    for (UnitFraction unitFraction : unitFractions) {
      sum = sum.add(unitFraction);
    }
    return sum;
  }

  public static UnitFraction sum(Stream<UnitFraction> unitFractionsStream) {
    return unitFractionsStream
        .reduce(UnitFraction::add)
        .orElse(UNIT_FRACTION_0);
  }

  public static UnitFraction sumWithCeilingOf1(Collection<UnitFraction> unitFractions) {
    return sumWithCeilingOf1(unitFractions.stream());
  }

  public static UnitFraction sumWithCeilingOf1(Stream<UnitFraction> unitFractionsStream) {
    Optional<BigDecimal> sum = unitFractionsStream
        .map(v -> v.asBigDecimal())
        .reduce(BigDecimal::add);
    return sum
        .map(v -> unitFraction(v.min(BigDecimal.ONE)))
        .orElse(UNIT_FRACTION_0);
  }

  public static UnitFraction sumWithCeilingOf1(UnitFraction first, UnitFraction second, UnitFraction... rest) {
    return sumWithCeilingOf1(concatenateFirstSecondAndRest(first, second, rest));
  }

  // sum of unit fractions, but if the sum is just over 1, snap to 1.
  public static UnitFraction forgivingSum(Collection<UnitFraction> unitFractions, Epsilon epsilon) {
    return forgivingSum(unitFractions.stream(), epsilon);
  }

  public static UnitFraction forgivingSum(Stream<UnitFraction> unitFractionStream, Epsilon epsilon) {
    return forgivingUnitFraction(
        unitFractionStream.mapToDouble(v -> v.doubleValue()).reduce(0.0, Double::sum),
        epsilon);
  }

  public static UnitFraction sum(UnitFraction first, UnitFraction second, UnitFraction... rest) {
    UnitFraction sum = first.add(second);
    for (UnitFraction item : rest) {
      sum = sum.add(item);
    }
    return sum;
  }

  public static boolean sumToAlmostOne(Epsilon epsilon, UnitFraction first, UnitFraction...rest) {
    return sumToAlmostOne(concatenateFirstAndRest(first, rest), epsilon);
  }

  public static boolean sumToAlmostOne(Collection<UnitFraction> unitFractions, Epsilon epsilon) {
    BigDecimal sum = sumAsBigDecimals(unitFractions);
    return epsilon.isAlmostZero(sum.subtract(BigDecimal.ONE).doubleValue());
  }

  public UnitFraction subtract(UnitFraction remainingToAllocate) {
    return forgivingUnitFraction(asBigDecimal().subtract(remainingToAllocate.asBigDecimal()), epsilon(1e-15));
  }

  /**
   * Subtracts to a valid UnitFraction, but the result would be negative (which isn't a valid UnitFraction),
   * we return UNIT_FRACTION_0.
   */
  public UnitFraction subtractWithFloorOfZero(UnitFraction subtrahend) {
    return isLessThan(subtrahend)
        ? UNIT_FRACTION_0
        : subtract(subtrahend);
  }

  public UnitFraction multiply(UnitFraction other) {
    return unitFraction(asBigDecimal().multiply(other.asBigDecimal()));
  }

  public UnitFraction divide(UnitFraction other) {
    return divide(other.asBigDecimal());
  }

  public UnitFraction divide(BigDecimal other) {
    if (other.compareTo(BigDecimal.ZERO) == 0) {
      throw new IllegalArgumentException(smartFormat("Cannot divide %s by zero", asBigDecimal()));
    }
    return unitFraction(asBigDecimal().divide(other, DEFAULT_MATH_CONTEXT));
  }

  public SignedFraction toSignedFraction() {
    return signedFraction(asBigDecimal());
  }

  public UnitFraction valueOrSnapToZero(Epsilon epsilon) {
    return isAlmostZero(epsilon)
        ? UNIT_FRACTION_0
        : this;
  }

  public UnitFraction valueOrSnapToOne(Epsilon epsilon) {
    return isAlmostOne(epsilon)
        ? UNIT_FRACTION_1
        : this;
  }

}
