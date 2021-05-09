package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.LongCounter;

import java.util.Comparator;

import static com.rb.nonbiz.collections.RBIterables.consecutivePairsForEach;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.types.LongCounter.longCounter;
import static java.util.Comparator.naturalOrder;

public class RBComparables {

  @SafeVarargs
  public static <T extends Comparable<? super T>> T min(T first, T...rest) {
    T minValue = first;
    for (T value : rest) {
      if (value.compareTo(minValue) < 0) {
        minValue = value;
      }
    }
    return minValue;
  }

  @SafeVarargs
  public static <T extends Comparable<? super T>> T max(T first, T...rest) {
    T maxValue = first;
    for (T value : rest) {
      if (value.compareTo(maxValue) > 0) {
        maxValue = value;
      }
    }
    return maxValue;
  }

  /**
   * True if and only if {@code (v1 > v2 > v3)} or {@code (v1 < v2 < v3)}; likewise for more items.
   */
  public static <P extends Comparable<? super P>> boolean strictlyMonotonic(P first, P second, P ... rest) {
    return strictlyMonotonic(naturalOrder(), first, second, rest);
  }

  /**
   * True if and only if {@code (v1 > v2 > v3)} or {@code (v1 < v2 < v3)} as per the comparator; likewise for more items.
   */
  @SafeVarargs
  public static <P> boolean strictlyMonotonic(
      Comparator<? super P> comparator, P first, P second, P ... rest) {
    LongCounter numIncreasing = longCounter();
    LongCounter numEqual      = longCounter();
    LongCounter numDecreasing = longCounter();
    consecutivePairsForEach(
        concatenateFirstSecondAndRest(first, second, rest),
        (v1, v2) -> {
          int comparison = comparator.compare(v1, v2);
          LongCounter l = comparison < 0 ? numIncreasing
                        : comparison > 0 ? numDecreasing
                                         : numEqual;
          l.increment();
        });
    if (numEqual.get() > 0) {
      return false;
    }
    // return false if we have both increasing and decreasing items.
    return !(numIncreasing.get() > 0 && numDecreasing.get() > 0);
  }

  /**
   * True if and only if {@code (v1 >= mid >= v2)} or {@code (v1 <= mid <= v2)} ; likewise for more items.
   */
  @SafeVarargs
  public static <P extends Comparable<? super P>> boolean monotonic(P first, P second, P ... rest) {
    return monotonic(naturalOrder(), first, second, rest);
  }

  /**
   * True if and only if {@code (v1 >= mid >= v2)} or {@code (v1 <= mid <= v2)} ; likewise for more items.
   */
  @SafeVarargs
  public static <P> boolean monotonic(
      Comparator<? super P> comparator, P first, P second, P ... rest) {
    LongCounter numIncreasing = longCounter();
    LongCounter numEqual      = longCounter();
    LongCounter numDecreasing = longCounter();
    consecutivePairsForEach(
        concatenateFirstSecondAndRest(first, second, rest),
        (v1, v2) -> {
          int comparison = comparator.compare(v1, v2);
          LongCounter l = comparison < 0 ? numIncreasing
                        : comparison > 0 ? numDecreasing
                                         : numEqual;
          l.increment();
        });
    // return false if we have both increasing and decreasing items.
    return !(numIncreasing.get() > 0 && numDecreasing.get() > 0);
  }

}
