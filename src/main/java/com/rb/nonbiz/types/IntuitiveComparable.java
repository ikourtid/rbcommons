package com.rb.nonbiz.types;

/**
 * Java's {@link Comparable} interface has well-defined semantics, but it's hard to read.
 * {@code foo1.compareTo(foo2) < 0 }
 * is harder to read than
 * {@code foo1.isLessThan(foo2) }
 *
 * <p> This interface adds some unintuitive comparison methods that are easier to read.
 * It is a bit unusual, because all its methods are 'default'.
 * </p>
 */
public interface IntuitiveComparable<T> extends Comparable<T> {

  default boolean isGreaterThan(T other) {
    return this.compareTo(other) > 0;
  }

  default boolean isGreaterThanOrEqualTo(T other) {
    return this.compareTo(other) >= 0;
  }

  default boolean isLessThan(T other) {
    return this.compareTo(other) < 0;
  }

  default boolean isLessThanOrEqualTo(T other) {
    return this.compareTo(other) <= 0;
  }

}
