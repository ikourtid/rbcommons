package com.rb.nonbiz.collections;

/**
 * This is like java.lang.Comparable, except that it also supports partial ordering.
 * That is, we could have a &lt; b and a &lt; c, but no definite relationship between b and c.
 */
public interface PartiallyComparable<T> {

  PartialComparisonResult partiallyCompareTo(T o);

}
