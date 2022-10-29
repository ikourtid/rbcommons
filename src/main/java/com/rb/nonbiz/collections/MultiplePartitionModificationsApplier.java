package com.rb.nonbiz.collections;

import com.google.inject.Inject;

import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRest;

/**
 * Applies a series of {@link PartitionModification}s to an original {@link Partition}.
 *
 * <p> Just like {@link SinglePartitionModificationApplier}, except that it can apply multiple modifications
 * in a row. </p>
 */
public class MultiplePartitionModificationsApplier {

  @Inject SinglePartitionModificationApplier singlePartitionModificationApplier;

  @SafeVarargs
  public final <K> Partition<K> apply(
      Partition<K> originalPartition,
      PartitionModification<K> first,
      PartitionModification<K> second,
      PartitionModification<K> ... rest) {
    return RBIterators.forEach(
        // the iterator
        concatenateFirstSecondAndRest(first, second, rest).iterator(),

        // initial value for running value that gets threaded through (output of iteration n = input of n+1)
        originalPartition,

        // the body of the iteration
        (partition, partitionModification) -> singlePartitionModificationApplier.apply(partition, partitionModification));
  }

}
