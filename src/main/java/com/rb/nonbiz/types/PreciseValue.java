package com.rb.nonbiz.types;

import com.google.common.annotations.VisibleForTesting;
import com.rb.biz.types.trading.RoundingScale;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.rb.nonbiz.text.SmartFormatter.smartFormat;
import static java.lang.Double.isNaN;
import static java.math.RoundingMode.HALF_UP;

/**
 * A {@code PreciseValue} is the base class of many value classes in the code.
 *
 * <p> This exists because using a plain double may result in an approximate value. E.g. 100.00 could be represented
 * internally as 99.99999999999999. A {@code BigDecimal} avoids that. </p>
 *
 * <p> Another advantage of using {@code PreciseValue} is that it lets us have more clearly named comparison methods: </p>
 *
 * {@code val1.isLessThan(val2)}
 * <p> is clearer than </p>
 * {@code val1.compareTo(val2) < 0}
 */
public abstract class PreciseValue<T extends PreciseValue<T>> extends RBNumeric<T> {

  @VisibleForTesting
  public static final BigDecimal EPSILON_FOR_SNAPPING_TO_ROUND_NUMBER = BigDecimal.valueOf(1e-12);

  protected static NumberFormat formatWithCommas(int maxPrecision) {
    return formatWithCommas(0, maxPrecision);
  }
  protected static NumberFormat formatWithCommas(int minPrecision, int maxPrecision) {
    DecimalFormat format = new DecimalFormat();
    format.setMinimumFractionDigits(minPrecision);
    format.setMaximumFractionDigits(maxPrecision);
    format.setGroupingUsed(true);
    format.setRoundingMode(HALF_UP);
    return format;
  }
  public static NumberFormat formatWithoutCommas(int maxPrecision) {
    return formatWithoutCommas(0, maxPrecision);
  }
  protected static NumberFormat formatWithoutCommas(int minPrecision, int maxPrecision) {
    DecimalFormat format = new DecimalFormat();
    format.setMinimumFractionDigits(minPrecision);
    format.setMaximumFractionDigits(maxPrecision);
    format.setGroupingUsed(false);
    format.setRoundingMode(HALF_UP);
    return format;
  }
  protected static final NumberFormat FORMAT_AS_INTEGER_WITH_COMMAS = formatWithCommas(0);

  private final BigDecimal value;
  protected transient double doubleValue = Double.NaN; // stored so we don't have to recompute it multiple times

  protected PreciseValue(BigDecimal value) {
    RBPreconditions.checkNotNull(value);
    this.value = value;
  }

  protected PreciseValue(BigDecimal value, double doubleValue) {
    this.value = value;
    this.doubleValue = doubleValue;
  }

  public BigDecimal asBigDecimal() {
    return value;
  }

  /**
   * Avoid converting to double unless necessary, as you lose precision this way.
   * This is a shorthand for those times when you need to.
   */
  @Override
  public double doubleValue() {
    if (doubleValue != doubleValue) { // isNan; inlining it here for speed
      synchronized (value) {
        if (doubleValue != doubleValue) {
          doubleValue = value.doubleValue();
        }
      }
    }
    return doubleValue;
  }

  @Override
  public byte byteValue() {
    return (byte) doubleValue();
  }

  @Override
  public short shortValue() {
    return (short) doubleValue();
  }

  @Override
  public int intValue() {
    return (int) doubleValue();
  }

  @Override
  public long longValue() {
    return (long) doubleValue();
  }

  @Override
  public float floatValue() {
    return (float) doubleValue();
  }

  public static <T extends PreciseValue<T>> List<BigDecimal> asBigDecimalList(List<T> values) {
    return values
        .stream()
        .map(v -> v.asBigDecimal())
        .collect(Collectors.toList());
  }

  /**
   * If very near a round number, return the round number, otherwise return the same number.
   */
  public static BigDecimal snapToRound(BigDecimal unsnapped) {
    BigDecimal rounded = unsnapped.setScale(0, RoundingMode.HALF_EVEN);
    BigDecimal absDiff = rounded.subtract(unsnapped).abs();
    return absDiff.compareTo(EPSILON_FOR_SNAPPING_TO_ROUND_NUMBER) <= 0
        ? rounded
        : unsnapped;
  }

  /**
   * If very near a round number, return the round number, otherwise return the same number.
   */
  public static <T extends PreciseValue<T>> BigDecimal snapToRound(T unsnapped) {
    return snapToRound(unsnapped.asBigDecimal());
  }
  /**
   * If super very near 0, return 0, otherwise return whatever number was passed.
   * This is useful e.g. for results of subtraction of 2 quantities that can be slightly off because
   * of numeric reasons.
   */
  public static BigDecimal snapToZero(BigDecimal unsnapped) {
    BigDecimal snapped = snapToRound(unsnapped);
    if (snapped.signum() == 0) {
      return BigDecimal.ZERO;
    }
    return unsnapped;
  }

  /**
   * If super very near 0, return 0, otherwise return whatever number was passed.
   * This is useful e.g. for results of subtraction of 2 quantities that can be slightly off because
   * of numeric reasons.
   */
  public static <T extends PreciseValue<T>> BigDecimal snapToZero(T unsnapped) {
    return snapToZero(unsnapped.asBigDecimal());
  }

  /**
   * Assuming it's possible to do so and not restricted by the generics mechanism
   * (e.g. a comparison between Money and BuyQuantity),
   * this will compare the BigDecimal values. There's a few cases where this is counterintuitive, e.g.
   * buyQuantity(10).almostEquals(sellQuantity(10), 1e-8) is true.
   * The upside is fewer conversions and fewer cases where we have an unexpected result because
   * we forgot to do some conversion.
   */
  public <S extends T> boolean almostEquals(S other, Epsilon epsilon) {
    if (other == null) {
      throw new IllegalArgumentException(smartFormat("almostEquals(%s, %s) cannot have a null 'other' argument", other, epsilon));
    }
    if (this == other) return true;

    return bigDecimalsAlmostEqual(this.asBigDecimal(), other.asBigDecimal(), epsilon);
  }

  public static <V extends PreciseValue<V>> boolean preciseValuesAlmostEqual(V v1, V v2, Epsilon epsilon) {
    return bigDecimalsAlmostEqual(v1.asBigDecimal(), v2.asBigDecimal(), epsilon);
  }

  public static boolean bigDecimalsAlmostEqual(BigDecimal bd1, BigDecimal bd2, Epsilon epsilon) {
    if (bd1 == null || bd2 == null) {
      throw new IllegalArgumentException(
          Strings.format("bigDecimalsAlmostEqual(%s, %s, %s) cannot have a null argument", bd1, bd2, epsilon));
    }

    if (bd1.equals(bd2)) {
      return true;
    }

    return bd1.subtract(bd2).abs().compareTo(BigDecimal.valueOf(epsilon.doubleValue())) <= 0;
  }

  // IDE-generated, except for last line
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PreciseValue that = (PreciseValue) o;

    // We can't use .equals for BigDecimal because 0.0 != 0
    return value.compareTo(that.value) == 0;
  }

  // IDE-generated
  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value.toString();
  }

  public String toString(int minPrecision, int maxPrecision) {
    return formatWithoutCommas(minPrecision, maxPrecision).format(value);
  }

  @Override
  public int compareTo(T other) {
    return asBigDecimal().compareTo(other.asBigDecimal());
  }

  public boolean isGreaterThanOrAlmostEqualTo(T other, Epsilon epsilon) {
    return this.asBigDecimal().compareTo(other.asBigDecimal().subtract(BigDecimal.valueOf(epsilon.doubleValue()))) >= 0;
  }

  public boolean isLessThanOrAlmostEqualTo(T other, Epsilon epsilon) {
    return this.asBigDecimal().compareTo(other.asBigDecimal().add(BigDecimal.valueOf(epsilon.doubleValue()))) <= 0;
  }

  /**
   * PreciseValues may be precise, but the process that generates them may not be.
   * We often want to check that e.g. the quantity to trade is less than some upper bound.
   * For numerical reasons, given how quantities are generated off he output of the optimizer,
   * it is possible that it is 'functionally equal' to its upper bound, but just epsilon above it.
   * In those cases, we want to use #isSafelyLessThan with a tiny epsilon
   *
   * Note that there is no isSafelyLessThanOrEqualTo; once we start adding a double non-zero epsilon,
   * that itself will cause some tiny numerical inaccuracy, so it doesn't make sense to do equality comparisons.
   */
  public boolean isSafelyLessThan(T other, Epsilon epsilon) {
    return this.doubleValue() < other.doubleValue() + epsilon.doubleValue();
  }

  /**
   * @see #isSafelyLessThan
   */
  public boolean isSafelyGreaterThan(T other, Epsilon epsilon) {
    return this.doubleValue() > other.doubleValue() - epsilon.doubleValue();
  }

  public boolean isZero() {
    return !isNaN(doubleValue)
        ? doubleValue == 0
        : asBigDecimal().signum() == 0;
  }

  public boolean isPositive() {
    return !isNaN(doubleValue)
        ? doubleValue > 0
        : asBigDecimal().signum() > 0;
  }

  public boolean isPositiveOrZero() {
    return !isNegative();
  }

  public boolean isNegative() {
    return !isNaN(doubleValue)
        ? doubleValue < 0
        : asBigDecimal().signum() < 0;
  }

  public boolean isNegativeOrZero() {
    return !isPositive();
  }

  public boolean isAlmostZero(Epsilon epsilon) {
    // small performance optimization; first check for exact 0, so as to avoid the slightly slower 2nd check
    return asBigDecimal().signum() == 0
        || epsilon.isAlmostZero(doubleValue());
  }

  public boolean isAlmostZeroButNotZero(Epsilon epsilon) {
    return asBigDecimal().signum() != 0
        && epsilon.isAlmostZero(doubleValue());
  }

  public boolean isRoundToScale(RoundingScale roundingScale) {
    return isRoundToScale(roundingScale.getRawInt());
  }

  public boolean isRoundToScale(int scale) {
    // check if round using "classic" rounding: 2.5 -> 3, -2.5 -> -3
    return asBigDecimal().compareTo(asBigDecimal().setScale(scale, HALF_UP)) == 0;
  }

  /**
   * Returns true if this is a round number, and not just round subject to epsilon.
   */
  public boolean isExactlyRound() {
    // Performance optimization for the cases where something is already round
    // https://stackoverflow.com/questions/1078953/check-if-bigdecimal-is-integer-value
    if (asBigDecimal().scale() <= 0) {
      return true;
    }
    // Even though the above is false, this could still be a round number. It just requires the slightly slower
    // operation of stripTrailingZeros. But at least we save that in the cases caught by the 'if' above.
    return asBigDecimal().stripTrailingZeros().scale() <= 0;
  }

  public static <T extends PreciseValue<T>> T max(T item1, T item2) {
    return item1.compareTo(item2) < 0 ? item2 : item1;
  }

  public static <T extends PreciseValue<T>> T min(T item1, T item2) {
    return item1.compareTo(item2) < 0 ? item1 : item2;
  }

  /**
   * Returns true if we pass in a positive and a negative number. If either is 0, then this also always returns false.
   */
  public static <T extends PreciseValue<T>> boolean signsAreOpposite(PreciseValue<T> item1, PreciseValue<T> item2) {
    return item1.asBigDecimal().signum() * item2.asBigDecimal().signum() < 0;
  }

  /**
   * Returns true if positive AND positive, negative AND negative, or zero AND zero.
   */
  public static boolean signsAreSame(PreciseValue<?> item1, PreciseValue<?> item2) {
    return item1.asBigDecimal().signum() == item2.asBigDecimal().signum();
  }

  public static <T extends PreciseValue<T>> BigDecimal sumToBigDecimal(Iterable<T> iterable) {
    return sumToBigDecimal(iterable.iterator());
  }

  public static <T extends PreciseValue<T>> BigDecimal sumToBigDecimal(Iterator<T> iterator) {
    BigDecimal sum = null;
    while (iterator.hasNext()) {
      BigDecimal thisItem = iterator.next().asBigDecimal();
      sum = sum == null
          ? thisItem
          : sum.add(thisItem);
    }
    return sum == null ? BigDecimal.ZERO : sum;
  }

}
