package com.rb.nonbiz.collections;

import com.google.inject.Inject;
import com.rb.nonbiz.types.UnitFraction;

import static com.rb.nonbiz.collections.PartitionGradient.partitionGradient;
import static com.rb.nonbiz.collections.RBSet.newRBSet;

/**
 * For a partition of e.g. A, B, and C, this will create 3 partitions where A, B, and C are bumped up
 * respectively by bumpAmount, e.g. 1% (as per PartitionExtender - i.e. bumping everything else down proportionately).
 * Likewise for bumping down, except that if A is below 1% in the original partition, we can't bump it down by 1%,
 * so A wouldn't get included.
 *
 * This is useful (Jan 2018) for researching the gradient of improvement of tracking error if we bump up or down
 * each asset class (in isolation) in the target allocation.
 */
public class PartitionGradientCalculator {

  @Inject PartitionExtender partitionExtender;
  @Inject PartitionUnextender partitionUnextender;

  public <T> PartitionGradient<T> calculatePartitionGradient(Partition<T> partition, UnitFraction bumpAmount) {
    return partitionGradient(partition, bumpAmount,
        newRBSet(partition.keySet()).toRBMap(key -> partitionExtender.extend(partition, key, bumpAmount)),
        newRBSet(partition.keySet()
            .stream()
            .filter(key -> partition.getOrZero(key).doubleValue() > bumpAmount.doubleValue() + 1e-8) // safely above
            .iterator())
            .toRBMap(key -> partitionUnextender.unextend(partition, key, bumpAmount)));
  }

}
