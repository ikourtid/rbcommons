package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;

/**
 * Applies a modification ({@link DetailedPartitionModification}) to a {@link Partition}, resulting in a new, modified
 * partition. For the semantics here, please see the tests for this class, as well as {@link DetailedPartitionModification}.
 */
public class SinglePartitionModificationApplier {

  public <T> Partition<T> apply(
      Partition<T> originalPartition,
      DetailedPartitionModification<T> detailedPartitionModification) {
    MutableRBMap<T, UnitFraction> mutableMap = newMutableRBMapWithExpectedSize(
        originalPartition.size()
            + detailedPartitionModification.getKeysToAdd().size()
            - detailedPartitionModification.getKeysToRemove().size());

    detailedPartitionModification.getKeysToAdd().forEachEntry( (key, newFraction) -> {
      RBPreconditions.checkArgument(
          !originalPartition.containsKey(key),
          "Key %s was meant to be added with a fraction of %s , but it already exists in the original partition: %s",
          key, newFraction, originalPartition);
      mutableMap.putAssumingAbsent(key, newFraction);
    });

    detailedPartitionModification.getKeysToIncrease().forEachEntry( (key, additionalFraction) ->
        mutableMap.putAssumingAbsent(
            key, UnitFraction.sum(
                additionalFraction,
                originalPartition.getRawFractionsMap().getOrThrow(
                    key,
                    "Key %s was meant to be increased by %s , but it does not exist in the original partition: %s",
                    key, additionalFraction, originalPartition))));

    detailedPartitionModification.getKeysToRemove().forEachEntry( (key, expectedOriginalUnitFraction) -> {
      UnitFraction actualOriginalUnitFraction = originalPartition.getRawFractionsMap().getOrThrow(
          key,
          "Key %s was meant to be removed, but it does not exist in the original partition: %s",
          key, originalPartition);
      RBPreconditions.checkArgument(
          actualOriginalUnitFraction.almostEquals(expectedOriginalUnitFraction, 1e-8),
          "Key %s to be removed from the original partition was supposed to have a weight of %s , but was %s ; partition: %s",
          key, expectedOriginalUnitFraction, actualOriginalUnitFraction, originalPartition);
      // Nothing else to do here; we just won't put an entry for this key in the final weights.
    });

    detailedPartitionModification.getKeysToDecrease().forEachEntry( (key, fractionDecrease) ->
      mutableMap.putAssumingAbsent(key,
          originalPartition.getRawFractionsMap().getOrThrow(
                  key,
                  "Key %s was meant to be increased by %s , but it does not exist in the original partition: %s",
                  key, fractionDecrease, originalPartition)
              .subtract(fractionDecrease)));

    // Finally, for any item in the original partition that was not added / increased / decreased,
    // and also not intended for deletion, copy its contents over to the final partition. That item was not meant to
    // be changed.
    originalPartition.getRawFractionsMap().forEachEntry( (key, originalFraction) -> {
      if (!mutableMap.containsKey(key) && !detailedPartitionModification.getKeysToRemove().containsKey(key)) {
        mutableMap.putAssumingAbsent(key, originalFraction);
      }
    });

    return partition(newRBMap(mutableMap));
  }

}
