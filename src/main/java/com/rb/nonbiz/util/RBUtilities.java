package com.rb.nonbiz.util;

import java.util.Comparator;
import java.util.function.Function;

// FIXME IAK move this to RBComparators
public class RBUtilities {

  /**
   * Make a {@code Comparator<T>} based on comparing the specified fields in order.
   * The first comparison that results in non-equality will determine the result of the comparator.
   */
  public static <T> Comparator<T> makeComparator(Function<T, Comparable>... comparisonFields) {
    return (t1, t2) -> {
      for (Function<T, Comparable> comparisonField : comparisonFields) {
        Comparable value1 = comparisonField.apply(t1);
        Comparable value2 = comparisonField.apply(t2);
        int comparisonResult = value1.compareTo(value2);
        if (comparisonResult != 0) {
          return comparisonResult;
        }
      }
      return 0;
    };
  }

}
