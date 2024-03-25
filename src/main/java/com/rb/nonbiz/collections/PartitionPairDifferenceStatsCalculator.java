package com.rb.nonbiz.collections;

import com.google.common.collect.Sets;
import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.nonbiz.collections.PartitionPairDifferenceStats.PartitionPairDifferenceStatsBuilder;

import static com.rb.nonbiz.collections.IidSetOperations.unionOfIidSets;
import static com.rb.nonbiz.collections.PartitionPairDifferenceStats.PartitionPairDifferenceStatsBuilder.partitionPairDifferenceStatsBuilder;

/**
 * Returns statistics about the differences in a pair of partitions.
 *
 * <p> If a key appears in one partition but not another, we will treat that other partition like it has a 0% holding
 * in that key. This is not some limiting assumption; partitions never include entries for keys that have a 0%
 * in the partition, so no key means 0%. </p>
 */
public class PartitionPairDifferenceStatsCalculator {

  public <T> PartitionPairDifferenceStats calculate(Partition<T> partition1, Partition<T> partition2) {
    PartitionPairDifferenceStatsBuilder builder = partitionPairDifferenceStatsBuilder();
    Sets.union(partition1.keySet(), partition2.keySet())
        .forEach(key -> builder.addDifference(
            partition1.getOrZero(key),
            partition2.getOrZero(key)));
    return builder.build();
  }

  /**
   * This is for the other flavor of partitions that we use in the code.
   */
  public <T extends HasInstrumentId> PartitionPairDifferenceStats calculate(
      HasInstrumentIdPartition<T> partition1,
      HasInstrumentIdPartition<T> partition2) {
    PartitionPairDifferenceStatsBuilder builder = partitionPairDifferenceStatsBuilder();
    unionOfIidSets(partition1.getKeysAsIidSet(), partition2.getKeysAsIidSet())
        .forEach(instrumentId -> builder.addDifference(
            partition1.getFractionOrZero(instrumentId),
            partition2.getFractionOrZero(instrumentId)));
    return builder.build();
  }

}
