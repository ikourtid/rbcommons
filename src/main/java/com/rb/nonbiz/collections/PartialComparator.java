package com.rb.nonbiz.collections;

import java.util.Comparator;

/**
 * Like java.util.{@link Comparator}, except for partial ordering, i.e. when two items may not always necessarily be
 * comparable.
 *
 * <p> For example, a pair of constraints P1 to keep short-term and long-term realized gains to be under $100 and $200, respectively,
 * is stricter than a pair of constraints P1 to keep them under $700 and $800.
 * But we can't compare P1 against a pair of constraints P3 to keep them both under $99 and $201, respectively.
 * The short-term constraint of P3 is stricter than that of P1, but the long-term is less strict. </p>
 *
 * <p> More informally, all else being equal, it's better to be young and rich than being old and poor.
 * But comparing young and rich vs. old and poor is not straightforward. </p>
 */
public interface PartialComparator<T> {

  PartialComparisonResult partiallyCompare(T o1, T o2);

}
