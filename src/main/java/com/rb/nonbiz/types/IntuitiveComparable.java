package com.rb.nonbiz.types;

/**
 * Java's {@link Comparable} interface has well-defined semantics, but it's hard to read.
 * {@code foo1.compareTo(foo2) < 0 }
 * is harder to read than
 * {@code foo1.isLessThan(foo2) }
 * This interface adds some unintuitive comparison methods that are easier to read.
 */
public interface IntuitiveComparable<T> extends Comparable<T> {

  boolean isGreaterThan(T other);

  boolean isGreaterThanOrEqualTo(T other);

  boolean isLessThan(T other);

  boolean isLessThanOrEqualTo(T other);

}
