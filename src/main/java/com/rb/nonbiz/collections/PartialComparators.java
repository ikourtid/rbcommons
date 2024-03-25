package com.rb.nonbiz.collections;

import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.collections.PartialComparisonResult.noOrderingDefined;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;

/**
 * Various utility methods pertaining to {@link PartialComparator}.
 */
public class PartialComparators {

  public static PartialComparisonResult partiallyCompareMultiple(
      PartialComparisonResult first, PartialComparisonResult second, PartialComparisonResult...rest) {
    int size = 2 + rest.length;
    int numLessThan = 0;
    int numEqual = 0;
    int numGreaterThan = 0;
    int numUnspecifiedOrder = 0;
    for (PartialComparisonResult partialComparison : concatenateFirstSecondAndRest(first, second, rest)) {
      if (!partialComparison.getRawResult().isPresent()) {
        numUnspecifiedOrder++;
        continue;
      }
      int comparison = partialComparison.getRawResult().getAsInt();
      if (comparison < 0) {
        numLessThan++;
      } else if (comparison > 0) {
        numGreaterThan++;
      } else {
        numEqual++;
      }
    }
    // If at least one comparison says there's no ordering, then the final result has no ordering.
    if (numUnspecifiedOrder > 0) {
      return noOrderingDefined();
    }
    // If some are greater and some are smaller, then there's no ordering.
    if (numLessThan > 0 && numGreaterThan > 0) {
      return noOrderingDefined();
    }
    // OK, we now know that all items have a (non-partial) comparison, and not all are equal.
    return
        numLessThan    > 0 ? PartialComparisonResult.lessThan() :
        numGreaterThan > 0 ? PartialComparisonResult.greaterThan() :
                             PartialComparisonResult.equal(); // all are equal
  }

  public static <T> T maxFromPartialComparatorOrThrow(PartialComparator<T> partialComparator, T value1, T value2) {
    PartialComparisonResult partialComparisonResult = partialComparator.partiallyCompare(value1, value2);
    RBPreconditions.checkArgument(
        partialComparisonResult.isDefined(),
        "We cannot pick the max of %s and %s because they cannot be compared",
        value1, value2);
    return partialComparisonResult.isLessThan() ? value2 : value1;
  }

  public static <T> T minFromPartialComparatorOrThrow(PartialComparator<T> partialComparator, T value1, T value2) {
    PartialComparisonResult partialComparisonResult = partialComparator.partiallyCompare(value1, value2);
    RBPreconditions.checkArgument(
        partialComparisonResult.isDefined(),
        "We cannot pick the min of %s and %s because they cannot be compared",
        value1, value2);
    return partialComparisonResult.isGreaterThan() ? value2 : value1;
  }

}
