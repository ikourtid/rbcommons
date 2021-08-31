package com.rb.nonbiz.collections;

import com.google.common.collect.Sets;
import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.nonbiz.types.UnitFraction;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.IidSetOperations.unionOfIidSets;
import static com.rb.nonbiz.collections.RBStreams.sumBigDecimals;

/**
 * Returns the average absolute difference in partition fraction over each key in the two partitions.
 * FIXME IAK ESGSTATS
 *
 * If a key appears in one partition but not another, we will treat that other partition like it has a 0% holding
 * in that key. This is not some limiting assumption; partitions never include entries for keys that have a 0%
 * in the partition, so no key means 0%.
 *
 * See comments in NaiveAllocationAccuracyDiffScoreRawCalculator about why the range of this is between 0 and 2.
 */
public class PartitionPairDifferenceStatsCalculator {

  public <T> BigDecimal calculate(Partition<T> partition1, Partition<T> partition2) {
    return sumBigDecimals(
        Sets.union(partition1.keySet(), partition2.keySet())
            .stream()
            .map(key -> {
              UnitFraction unitFraction1 = partition1.getOrZero(key);
              UnitFraction unitFraction2 = partition2.getOrZero(key);
              return unitFraction1.asBigDecimal().subtract(unitFraction2.asBigDecimal()).abs();
            }));
  }

  /**
   * This is for the other flavor of partitions that we use in the code.
   */
  public <T extends HasInstrumentId> BigDecimal calculate(
      HasInstrumentIdPartition<T> partition1,
      HasInstrumentIdPartition<T> partition2) {
    return sumBigDecimals(
        unionOfIidSets(partition1.getKeysAsIidSet(), partition2.getKeysAsIidSet())
            .stream()
            .map(instrumentId -> {
              UnitFraction unitFraction1 = partition1.getFractionOrZero(instrumentId);
              UnitFraction unitFraction2 = partition2.getFractionOrZero(instrumentId);
              return unitFraction1.asBigDecimal().subtract(unitFraction2.asBigDecimal()).abs();
            }));
  }

}
