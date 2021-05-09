package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;
import com.rb.nonbiz.util.RBSubsetPreconditions;

/**
 * Not a great name...
 * For an original partition of N items (e.g. 10% cash, 40% A, 50% B)
 * we generate 3 partitions, where we bump up each of (cash, A, B) by e.g. 1%,
 * and another 3, where we bump down instead of up.
 *
 * This is useful for researching the local gradient of improvement of the residual allocation
 * in the presence external assets.
 */
public class PartitionGradient<T> {

  private final Partition<T> originalPartition;
  private final UnitFraction bumpAmount;
  private final RBMap<T, Partition<T>> partitionsWhenBumpingUp;
  private final RBMap<T, Partition<T>> partitionsWhenBumpingDown;

  private PartitionGradient(
      Partition<T> originalPartition,
      UnitFraction bumpAmount,
      RBMap<T, Partition<T>> partitionsWhenBumpingUp,
      RBMap<T, Partition<T>> partitionsWhenBumpingDown) {
    this.originalPartition = originalPartition;
    this.bumpAmount = bumpAmount;
    this.partitionsWhenBumpingUp = partitionsWhenBumpingUp;
    this.partitionsWhenBumpingDown = partitionsWhenBumpingDown;
  }

  public static <T> PartitionGradient<T> partitionGradient(
      Partition<T> originalPartition,
      UnitFraction bumpAmount,
      RBMap<T, Partition<T>> partitionsWhenBumpingUp,
      RBMap<T, Partition<T>> partitionsWhenBumpingDown) {
    RBPreconditions.checkArgument(
        !bumpAmount.isAlmostZero(1e-8),
        "It makes no sense to bump by 0; it implies no bumping: %s",
        originalPartition);
    RBPreconditions.checkArgument(
        !bumpAmount.isAlmostOne(1e-8),
        "Can't bump by 1; it would have to be a singleton partition, and there's nothing else to take its place: %s",
        originalPartition);
    RBPreconditions.checkArgument(
        partitionsWhenBumpingUp.values()
            .stream()
            .allMatch(p -> p.keySet().equals(originalPartition.keySet())));
    RBPreconditions.checkArgument(
        partitionsWhenBumpingDown.values()
            .stream()
            .allMatch(p -> p.keySet().equals(originalPartition.keySet())));
    RBSimilarityPreconditions.checkBothSame(
        partitionsWhenBumpingUp.keySet(),
        originalPartition.keySet(),
        "For each item in the partition, we will try bumping it up, so the 'up' map must match the keys");
    // We can always add to an existing key (or add a new one),
    // but for the keys being 'unextended', we can't do so beyond their actual membership.
    // So e.g. cash at 20 bps cannot be unextended by 1%, and therefoe will not live in the 'bumping down' map.
    RBSubsetPreconditions.checkIsSubset(
        partitionsWhenBumpingDown.keySet(),
        originalPartition.keySet(),
        "The 'bumped down' map will only contain keys that can be reduced by %s , but no other ones",
        bumpAmount);
    return new PartitionGradient<>(originalPartition, bumpAmount, partitionsWhenBumpingUp, partitionsWhenBumpingDown);
  }

  public Partition<T> getOriginalPartition() {
    return originalPartition;
  }

  public UnitFraction getBumpAmount() {
    return bumpAmount;
  }

  public RBMap<T, Partition<T>> getPartitionsWhenBumpingUp() {
    return partitionsWhenBumpingUp;
  }

  public RBMap<T, Partition<T>> getPartitionsWhenBumpingDown() {
    return partitionsWhenBumpingDown;
  }

  @Override
  public String toString() {
    return Strings.format("[PG orig= %s bumpAmt= %s bumpUp= %s bumpDown= %s PG]",
        originalPartition, bumpAmount, partitionsWhenBumpingUp, partitionsWhenBumpingDown);
  }

}
