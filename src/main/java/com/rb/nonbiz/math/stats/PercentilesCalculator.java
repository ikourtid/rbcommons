package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;
import java.util.stream.Collectors;

/**
 * For a list of sorted items, calculates percentiles per the list specified (e.g. 5th, 10th, 50th, 90th, 95th).
 */
public class PercentilesCalculator {

  /**
   * 1st arg MUST be sorted. We can't ascertain it here easily, otherwise we'd have to
   * a) double-verify (the caller presumably already sorts)
   * b) pass a comparator, just for purposes of sanity checking.
   *
   * 'percentiles' does not have to be sorted, but that would be a clearer calling pattern.
   */
  public <T> List<T> getApproximatePercentiles(List<T> sortedItems, List<UnitFraction> percentiles) {
    RBPreconditions.checkArgument(
        !sortedItems.isEmpty(),
        "You cannot get percentiles for an empty list");
    RBPreconditions.checkArgument(
        percentiles.stream()
            .allMatch(f -> !f.isOne()),
        "All percentile fractions must be < 1 : %s",
        percentiles);
    RBPreconditions.checkArgument(
        !percentiles.isEmpty(),
        "Chances are you don't want NO percentages (percentiles list is empty); items are %s",
        sortedItems);
    int total = sortedItems.size();
    return percentiles
        .stream()
        .map(f -> sortedItems.get((int) Math.floor(f.asBigDecimal().doubleValue() * total)))
        .collect(Collectors.toList());
  }

}
