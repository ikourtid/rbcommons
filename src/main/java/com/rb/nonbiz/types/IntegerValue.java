package com.rb.nonbiz.types;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is like {@link PreciseValue} and {@link ImpreciseValue}, but for int typesafe wrappers.
 * Like those two, it lets us have more clearly named comparison methods:
 * {@code val1.isLessThan(val2)} is clearer than {@code val1.compareTo(val2) < 0}
 * More importantly, since Java doesn't allow typedefs, it gives us a general way to implement simple wrappers
 * around a single int.
 */
public abstract class IntegerValue<T extends IntegerValue<T>> extends RBNumeric<T> {

  private final int value;

  protected IntegerValue(int value) {
    this.value = value;
  }

  @Override
  public double doubleValue() {
    return value;
  }

  @Override
  public byte byteValue() {
    return (byte) value;
  }

  @Override
  public short shortValue() {
    return Shorts.checkedCast(value);
  }

  @Override
  public int intValue() {
    return value;
  }

  @Override
  public long longValue() {
    return value;
  }

  @Override
  public float floatValue() {
    return value;
  }

  public static <T extends IntegerValue<T>> List<Integer> asIntList(List<T> values) {
    return values
        .stream()
        .map(v -> v.intValue())
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }

  @Override
  public int compareTo(T other) {
    return Integer.compare(value, other.intValue());
  }

  public static <T extends IntegerValue<T>> T max(T item1, T item2) {
    return item1.compareTo(item2) < 0 ? item2 : item1;
  }

  public static <T extends IntegerValue<T>> T min(T item1, T item2) {
    return item1.compareTo(item2) < 0 ? item1 : item2;
  }

  public static <T extends IntegerValue<T>> boolean signsAreOpposite(IntegerValue<T> item1, IntegerValue<T> item2) {
    return item1.intValue() * item2.intValue() < 0;
  }

  /**
   * Sums the contents into an int; throws an exception if that would result in overflow.
   */
  public static <T extends IntegerValue<T>> int sumToInt(Iterable<T> iterable) {
    return sumToInt(iterable.iterator());
  }

  /**
   * Sums the contents into an int; throws an exception if that would result in overflow.
   */
  public static <T extends IntegerValue<T>> int sumToInt(Iterator<T> iterator) {
    // This is a Guava mechanism, which throws an exception if there's overflow when it converts a long to an int.
    return Ints.checkedCast(sumToLong(iterator));
  }

  /**
   * Sums the contents into a long; no need to check for overflow while summing, because we'd have to add
   * something like 2 ^ 32 max ints to get to a sum that can't fit inside a long.
   */
  public static <T extends IntegerValue<T>> long sumToLong(Iterable<T> iterable) {
    return sumToLong(iterable.iterator());
  }

  /**
   * Sums the contents into a long; no need to check for overflow while summing, because we'd have to add
   * something like 2 ^ 32 max ints to get to a sum that can't fit inside a long.
   */
  public static <T extends IntegerValue<T>> long sumToLong(Iterator<T> iterator) {
    long sum = 0;
    while (iterator.hasNext()) {
      sum += iterator.next().intValue();
    }
    return sum;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return intValue() == ((IntegerValue<?>) o).intValue();
  }

  @Override
  public int hashCode() {
    return intValue();
  }

}
