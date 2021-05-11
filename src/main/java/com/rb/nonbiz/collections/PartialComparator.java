package com.rb.nonbiz.collections;

/**
 * Like java.util.Comparator, except for partial ordering.
 */
public interface PartialComparator<T> {

  PartialComparisonResult partiallyCompare(T o1, T o2);

}
