package com.rb.nonbiz.collections;

import com.google.common.collect.Range;
import com.rb.nonbiz.text.Strings;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBRanges.optionalIntersection;

/**
 * Find the intersection of a high-priority and a low-priority ranges.
 *
 * <p> If the intersection of the two ranges is empty, return a range containing at least one point
 * from the "high priority" range. </p>
 */
public class PrioritizedRangeMerger {

  public <T extends Comparable<? super T>> Range<T> merge(
      Range<T> highPriorityRange,
      Range<T> lowPriorityRange) {
    Optional<Range<T>> maybeIntersection = optionalIntersection(highPriorityRange, lowPriorityRange);
    if (maybeIntersection.isPresent()) {
      return maybeIntersection.get();
    } else {
      // No intersection. Therefore, one range must be entirely above or below the other range.
      // To decide whether it's above or below, we just need one point from the lower-priority range.
      T onePointLowPriorityRange = lowPriorityRange.hasLowerBound()
                                   ? lowPriorityRange.lowerEndpoint()
                                   : lowPriorityRange.upperEndpoint();
      if (highPriorityRange.hasUpperBound() &&
          highPriorityRange.upperEndpoint().compareTo(onePointLowPriorityRange) < 0) {
        // The low-priority range is entirely above the high-priority range.
        // Return the upper bound of the high-priority range.
        return Range.singleton(highPriorityRange.upperEndpoint());
      }
      if (highPriorityRange.hasLowerBound() &&
          highPriorityRange.lowerEndpoint().compareTo(onePointLowPriorityRange) > 0) {
        // The low-priority range is entirely below the high-priority range.
        // Return the lower bound of the high-priority range.
        return Range.singleton(highPriorityRange.lowerEndpoint());
      }
      throw new IllegalArgumentException(Strings.format(
          "Internal error: should not be here. high-priority range %s low-priority range %s",
          highPriorityRange, lowPriorityRange));
    }
  }

}
