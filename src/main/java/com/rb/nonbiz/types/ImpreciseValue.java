package com.rb.nonbiz.types;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.rb.nonbiz.types.PreciseValue.formatWithoutCommas;

/**
 * This is like PreciseValue, but it doesn't carry one of the benefits of PreciseValue (the precise value).
 * However, like PreciseValue, it lets us have more clearly named comparison methods:
 * {@code val1.isLessThan(val2)} is clearer than {@code val1.compareTo(val2) < 0}
 * More importantly, since Java doesn't allow typedefs, it gives us a general way to implement simple wrappers
 * around a single double.
 *
 * If PreciseValue didn't already exist, I wouldn't have named this ImpreciseValue,
 * but PreciseValue is used all over the place, so ImpreciseValue makes a nice contrast.
 */
public abstract class ImpreciseValue<T extends ImpreciseValue<T>> extends RBNumeric<T> {

  private final double value;

  protected ImpreciseValue(double value) {
    this.value = value;
  }

  @Override
  public double doubleValue() {
    return value;
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

  public static <T extends ImpreciseValue<T>> List<Double> asDoubleList(List<T> values) {
    return values
        .stream()
        .map(v -> v.doubleValue())
        .collect(Collectors.toList());
  }

  /**
   * Assuming it's possible to do so and not restricted by the generics mechanism
   * (e.g. a comparison between Money and BuyQuantity),
   * this will compare the BigDecimal values. There's a few cases where this is counterintuitive, e.g.
   * buyQuantity(10).almostEquals(sellQuantity(10), 1e-8) is true.
   * The upside is fewer conversions and fewer cases where we have an unexpected result because
   * we forgot to do some conversion.
   */
  public boolean almostEquals(T other, Epsilon epsilon) {
    if (this == other) return true;
    if (other == null) return false;

    return epsilon.areWithin(value, other.doubleValue());
  }

  @Override
  public String toString() {
    return Double.toString(value);
  }

  public String toString(int maxPrecision) {
    return toString(0, maxPrecision);
  }

  public String toString(int minPrecision, int maxPrecision) {
    return formatWithoutCommas(minPrecision, maxPrecision).format(value);
  }

  @Override
  public int compareTo(T other) {
    return Double.compare(value, other.doubleValue());
  }

  public boolean isAlmostZero(Epsilon epsilon) {
    return epsilon.isAlmostZero(value);
  }

  public static <T extends ImpreciseValue<T>> T max(T item1, T item2) {
    return item1.compareTo(item2) < 0 ? item2 : item1;
  }

  public static <T extends ImpreciseValue<T>> T min(T item1, T item2) {
    return item1.compareTo(item2) < 0 ? item1 : item2;
  }

  public static <T extends ImpreciseValue<T>> boolean signsAreOpposite(ImpreciseValue<T> item1, ImpreciseValue<T> item2) {
    return item1.doubleValue() * item2.doubleValue() < 0;
  }

  public static <T extends ImpreciseValue<T>> double sumToDouble(Iterable<T> iterable) {
    return sumToDouble(iterable.iterator());
  }

  public static <T extends ImpreciseValue<T>> double sumToDouble(Iterator<T> iterator) {
    double sum = 0;
    while (iterator.hasNext()) {
      sum += iterator.next().doubleValue();
    }
    return sum;
  }

}
