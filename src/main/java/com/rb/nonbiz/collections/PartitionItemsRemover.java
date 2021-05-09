package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.UnitFraction;

import java.util.Map;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;

/**
 * Removes some items from a partition, and normalizes the remaining ones to add to 1
 * (so their *relative* weights remain the same).
 *
 * This is the opposite of PartitionExtender, which adds instead of removing.
 * @see PartitionExtender
 */
public class PartitionItemsRemover {

  public <K> Partition<K> removeItemsFromPartition(Partition<K> startingPartition, RBSet<K> partitionItemsToRemove) {
    if (partitionItemsToRemove.isEmpty()) {
      throw new IllegalArgumentException(Strings.format(
          "We could allow removing 0 items and returning the same partition, but disallowing catches likely bugs: %s",
          startingPartition));
    }
    if (!startingPartition.keySet().containsAll(partitionItemsToRemove.asSet())) {
      throw new IllegalArgumentException(Strings.format(
          "Some items to be removed are not in the partition. To remove= %s ; partition= %s",
          partitionItemsToRemove, startingPartition));
    }
    if (RBSets.difference(startingPartition.keySet(), partitionItemsToRemove).isEmpty()) {
      throw new IllegalArgumentException(Strings.format(
          "You cannot remove ALL items from the partition. To remove= %s ; partition= %s",
          partitionItemsToRemove, startingPartition));
    }
    MutableRBMap<K, UnitFraction> remainingFractions = newMutableRBMapWithExpectedSize(
        startingPartition.size() - partitionItemsToRemove.size());
    for (K key : startingPartition.keySet()) {
      if (!partitionItemsToRemove.contains(key)) {
        remainingFractions.put(key, startingPartition.getFraction(key));
      }
    }
    UnitFraction sumOfRemaining = UnitFraction.sum(remainingFractions.values());
    for (Map.Entry<K, UnitFraction> entry : remainingFractions.entrySet()) {
      K key = entry.getKey();
      UnitFraction unadjustedUnitFraction = entry.getValue();
      UnitFraction adjustedUnitFraction = unadjustedUnitFraction.divide(sumOfRemaining);
      entry.setValue(adjustedUnitFraction);
    }
    return partition(newRBMap(remainingFractions));
  }

}
