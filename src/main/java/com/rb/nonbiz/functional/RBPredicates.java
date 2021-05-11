package com.rb.nonbiz.functional;

import com.google.common.collect.Range;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.function.Predicate;

public class RBPredicates {

  public static <C extends Comparable<? super C>> Predicate<C> isGreaterThan(C otherValue) {
    return c -> c.compareTo(otherValue) > 0;
  }

  public static <C extends Comparable<? super C>> Predicate<C> isGreaterThanOrEqualTo(C otherValue) {
    return c -> c.compareTo(otherValue) >= 0;
  }

  public static <C extends Comparable<? super C>> Predicate<C> isLessThan(C otherValue) {
    return c -> c.compareTo(otherValue) < 0;
  }

  public static <C extends Comparable<? super C>> Predicate<C> isLessThanOrEqualTo(C otherValue) {
    return c -> c.compareTo(otherValue) <= 0;
  }

  public static <C extends Comparable<? super C>> Predicate<C> isEqualTo(C otherValue) {
    return c -> c.compareTo(otherValue) == 0;
  }

  public static <C extends Comparable<? super C>> Predicate<C> isBetweenInclusive(C lower, C upper) {
    return isIn(Range.closed(lower, upper));
  }

  public static <C extends Comparable<? super C>> Predicate<C> isIn(Range<C> range) {
    return c -> range.contains(c);
  }

  public static <T extends RBNumeric<? super T>> Predicate<T> isAlmostEqualTo(T otherValue, double epsilon) {
    RBPreconditions.checkArgument(epsilon >= 0);
    return v -> Math.abs(v.doubleValue() - otherValue.doubleValue()) <= epsilon;
  }

  public static Predicate<Double> isAlmostEqualTo(double otherValue, double epsilon) {
    RBPreconditions.checkArgument(epsilon >= 0);
    return v -> Math.abs(v - otherValue) <= epsilon;
  }

  // Note: isWithin(100, 10%).test(90) is false; it means abs((90 - 100) / 90), which is greater than 10%
  public static <V extends PreciseValue<? super V>> Predicate<V> isWithin(V otherValue, UnitFraction absDiffFraction) {
    return v -> RBBiPredicates.<V>isWithin(absDiffFraction).test(v, otherValue);
  }

}
