package com.rb.nonbiz.collections;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBRanges.optionalIntersection;

/**
 * Find the intersection of a high-priority range and a low-priority range.
 *
 * <p> If the intersection of the two ranges is empty, this will return a singleton range containing the
 * closest single point from the "high priority" range. </p>
 */
public class PrioritizedRangeMerger {

  public <T extends Comparable<? super T>> Range<T> merge(
      Range<T> highPriorityRange,
      Range<T> lowPriorityRange) {
    Optional<Range<T>> maybeIntersection = optionalIntersection(highPriorityRange, lowPriorityRange);
    if (maybeIntersection.isPresent()) {
      // if there is an intersection between the two ranges, return it
      return maybeIntersection.get();
    }

    // No intersection. Therefore, both the high-priority range and the low-priority range must each have
    // (at least) one end point. Either the high-priority range has an upper bound the low-priority range
    // has (higher) lower bound, or vice-versa. But they both have at least one bound.

    // The low-priority range must be entirely above or below the high-priority range.
    // To decide whether it's above or below, we just need one point from the lower-priority range.
    // E.g. [2, 4] is below [8, 11]. It doesn't matter if we use 8 or 11 to compare to [2, 4].
    T onePointLowPriorityRange = lowPriorityRange.hasLowerBound()
                                 ? lowPriorityRange.lowerEndpoint()
                                 : lowPriorityRange.upperEndpoint();
    if (highPriorityRange.hasUpperBound() &&
        highPriorityRange.upperEndpoint().compareTo(onePointLowPriorityRange) < 0) {
      // The low-priority range is entirely above the high-priority range.
      // Return the upper bound of the high-priority range.
      RBPreconditions.checkArgument(
          highPriorityRange.upperBoundType() == BoundType.CLOSED,
          "High-priority range %s must have a closed upper bound.",
          highPriorityRange);
      return Range.singleton(highPriorityRange.upperEndpoint());
    }

    if (highPriorityRange.hasLowerBound() &&
        highPriorityRange.lowerEndpoint().compareTo(onePointLowPriorityRange) > 0) {
      // The low-priority range is entirely below the high-priority range.
      // Return the lower bound of the high-priority range.
      RBPreconditions.checkArgument(
          highPriorityRange.lowerBoundType() == BoundType.CLOSED,
          "High-priority range %s must have a closed lower bound.",
          highPriorityRange);
      return Range.singleton(highPriorityRange.lowerEndpoint());
    }

    throw new IllegalArgumentException(Strings.format(
        "Internal error: should not be here. high-priority range %s low-priority range %s",
        highPriorityRange, lowPriorityRange));
  }

}
