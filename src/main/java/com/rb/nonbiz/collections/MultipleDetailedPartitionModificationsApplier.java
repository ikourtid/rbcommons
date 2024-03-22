package com.rb.nonbiz.collections;

import com.google.inject.Inject;

import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRest;

/**
 * Applies a series of {@link DetailedPartitionModification}s to an original {@link Partition} to generate a new
 * {@link Partition}.
 *
 * <p> Just like {@link SingleDetailedPartitionModificationApplier}, except that it can apply multiple modifications
 * in a row. </p>
 */
public class MultipleDetailedPartitionModificationsApplier {

  @Inject SingleDetailedPartitionModificationApplier singleDetailedPartitionModificationApplier;

  @SafeVarargs
  public final <K> Partition<K> apply(
      Partition<K> originalPartition,
      DetailedPartitionModification<K> first,
      DetailedPartitionModification<K> second,
      DetailedPartitionModification<K>... rest) {
    return RBIterators.forEach(
        // the iterator
        concatenateFirstSecondAndRest(first, second, rest).iterator(),

        // initial value for running value that gets threaded through (output of iteration n = input of n+1)
        originalPartition,

        // the body of the iteration
        (partition, partitionModification) ->
            singleDetailedPartitionModificationApplier.apply(partition, partitionModification));
  }

}
