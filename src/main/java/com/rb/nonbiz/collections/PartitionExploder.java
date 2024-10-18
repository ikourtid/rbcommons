package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.UnitFraction;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;

/**
 * 'Explodes' certain items in the partition.
 *
 * <p> E.g. 2-asset-id partition consists of a stock X and an ETF, and we know the ETF has 3 constituent stocks,
 * then the final partition will contain 4 stocks, or 3 if X is already one of the constituents of the ETF.
 * Let's say ETF contains stocks X, Y, and Z, at 10% / 40% / 50% breakdown, and that X / ETF are 70% / 30% of the
 * initial partition. The weight of Y will be 40% * 30%, and the weight of X will be 70% (from the top-level
 * partition) plus 30% * 10% (from the ETF). </p>
 *
 * <p> Given the input data structures, this only works for a single level,
 * i.e. not if we have a partition of partitions of partitions, etc.
 * Such an object can't be represented by a simple {@link Partition} anyway, since the value type in the
 * {@link Partition} is the same. </p>
 */
public class PartitionExploder {

  public <T> Partition<T> explode(
      Partition<T> initial,
      RBMap<T, Partition<T>> subPartitions) {
    if (subPartitions.isEmpty()) {
      return initial; // performance optimization
    }
    // Starting with the initial top-level weights for any key that's pointing to an object and not to a partition
    MutableRBMap<T, UnitFraction> fractionsMap = newMutableRBMap(initial.getRawFractionsMap()
        .filterKeys(key -> !subPartitions.containsKey(key)));

    subPartitions.forEachEntry( (key, subPartition) ->
        subPartition.getRawFractionsMap().forEachEntry( (subKey, unitFractionInSubPartition) ->
        fractionsMap.putOrModifyExisting(
            subKey,
            initial.getFraction(key).multiply(unitFractionInSubPartition),
            UnitFraction::add)));
    return partition(newRBMap(fractionsMap));
  }

}
