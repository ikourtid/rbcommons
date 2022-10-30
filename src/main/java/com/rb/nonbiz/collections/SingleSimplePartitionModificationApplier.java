package com.rb.nonbiz.collections;

import com.rb.nonbiz.math.RBBigDecimals;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.RBDoubles;
import com.rb.nonbiz.types.RBDoubles.EpsilonComparisonVisitor;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBVoid.rbVoid;
import static com.rb.nonbiz.math.RBBigDecimals.epsilonCompareBigDecimals;

/**
 * Applies a modification ({@link DetailedPartitionModification}) to a {@link Partition}, resulting in a new, modified
 * partition. For the semantics here, please see the tests for this class, as well as {@link DetailedPartitionModification}.
 */
public class SingleSimplePartitionModificationApplier {

  public <T> Partition<T> apply(
      Partition<T> originalPartition,
      SimplePartitionModification<T> detailedPartitionModification) {
    // The size may be an overestimate, because some items may end up being altogether removed, if the weight to
    // decrease them by is the same as their original weight.
    MutableRBMap<T, UnitFraction> mutableMap = newMutableRBMapWithExpectedSize(
        originalPartition.size()
            + detailedPartitionModification.getKeysToAddOrIncrease().size());

    detailedPartitionModification.getKeysToAddOrIncrease().forEachEntry( (key, fractionToAdd) ->
      mutableMap.putAssumingAbsent(key,
          UnitFraction.sum(originalPartition.getOrZero(key), fractionToAdd)));

    detailedPartitionModification.getKeysToRemoveOrDecrease().forEachEntry( (key, fractionToSubtract) -> {
      UnitFraction originalFraction = originalPartition.getRawFractionsMap().getOrThrow(
          key,
          "Trying to subtract the weight of key %s by %s , but the key does not exist in the original partition: %s",
          key, fractionToSubtract, originalPartition);
      epsilonCompareBigDecimals(
          originalFraction.asBigDecimal(),
          fractionToSubtract.asBigDecimal(),
          1e-8,
          new EpsilonComparisonVisitor<RBVoid>() {
            @Override
            public RBVoid visitRightIsGreater(double ignored) {
              throw new IllegalArgumentException(Strings.format(
                  "Original fraction for %s is %s , so if we subtract %s , we'll get a negative number. Partition: %s",
                  key, originalFraction, fractionToSubtract, originalPartition));
            }

            @Override
            public RBVoid visitAlmostEqual() {
              // Do nothing; we are subtracting the same amount as the original, so the final partition won't
              // have an entry for this key, because a partition only stores non-zero items.
              return rbVoid();
            }

            @Override
            public RBVoid visitLeftIsGreater(double ignored) {
              mutableMap.putAssumingAbsent(key, originalFraction.subtract(fractionToSubtract));
              return rbVoid();
            }
          });
    });

    // Finally, for any item in the original partition that was not added / increased / removed / decreased,
    // copy its contents over to the final partition. That item was not meant to be changed.
    originalPartition.getRawFractionsMap().forEachEntry( (key, originalFraction) -> {
      if (!detailedPartitionModification.getKeysToAddOrIncrease().containsKey(key) &&
          !detailedPartitionModification.getKeysToRemoveOrDecrease().containsKey(key)) {
        mutableMap.putAssumingAbsent(key, originalFraction);
      }
    });

    return partition(newRBMap(mutableMap));
  }

}