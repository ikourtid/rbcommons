package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.Partition.partitionFromPositiveWeightsWhichMayNotSumTo1;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.types.Epsilon.epsilon;

/**
 * Applies a modification ({@link DetailedPartitionModification}) to a {@link Partition}, resulting in a new, modified
 * partition. For the semantics here, please see the tests for this class, as well as {@link DetailedPartitionModification}.
 */
public class SingleDetailedPartitionModificationApplier {

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
          actualOriginalUnitFraction.almostEquals(
              expectedOriginalUnitFraction,
              // getEpsilonForRemovalSanityChecks is a UnitFraction, but almostEquals needs an Epsilon.
              epsilon(detailedPartitionModification.getEpsilonForRemovalSanityChecks().doubleValue())),
          "Key %s to be removed from the original partition was supposed to have a weight of %s , but was %s ; partition: %s",
          key, expectedOriginalUnitFraction, actualOriginalUnitFraction, originalPartition);
      // Nothing else to do here; we just won't put an entry for this key in the final weights.
    });

    detailedPartitionModification.getKeysToDecrease().forEachEntry( (key, fractionDecrease) ->
      mutableMap.putAssumingAbsent(key,
          originalPartition.getRawFractionsMap().getOrThrow(
                  key,
                  "Key %s was meant to be decreased by %s , but it does not exist in the original partition: %s",
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

    // See comments in getEpsilonForNetAdditionSanityCheck on why we need this. In short, there are some cases
    // (often in test code) where the additions/increases aren't exactly the same as the deletions/removals
    // (using an epsilon of detailedPartitionModification.getEpsilonForNetAdditionSanityCheck())
    // which would cause the final partition fractions to sum up to a number slightly away from 100%.
    // partitionFromPositiveWeightsWhichMayNotSumTo1 renormalizes the weights to sum to 100%.
    // It can't hurt to do it always, but we only want to do it if it's needed, both for performance reasons (secondary)
    // and to use the preconditions inside Partition#partition that check that the sum is (almost exactly) 100%.
    return detailedPartitionModification.getEpsilonForNetAdditionSanityCheck().isPresent()
        ? partitionFromPositiveWeightsWhichMayNotSumTo1(newRBMap(mutableMap))
        : partition(newRBMap(mutableMap));
  }

}
