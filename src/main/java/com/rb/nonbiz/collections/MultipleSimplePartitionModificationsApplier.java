package com.rb.nonbiz.collections;

import com.google.inject.Inject;

import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRest;

/**
 * Applies a series of {@link SimplePartitionModification}s to an original {@link Partition}.
 *
 * <p> Just like {@link SingleSimplePartitionModificationApplier}, except that it can apply multiple modifications
 * in a row. </p>
 */
public class MultipleSimplePartitionModificationsApplier {

  @Inject SingleSimplePartitionModificationApplier singleSimplePartitionModificationApplier;

  @SafeVarargs
  public final <K> Partition<K> apply(
      Partition<K> originalPartition,
      SimplePartitionModification<K> first,
      SimplePartitionModification<K> second,
      SimplePartitionModification<K>... rest) {
    return RBIterators.forEach(
        // the iterator
        concatenateFirstSecondAndRest(first, second, rest).iterator(),

        // initial value for running value that gets threaded through (output of iteration n = input of n+1)
        originalPartition,

        // the body of the iteration
        (partition, partitionModification) ->
            singleSimplePartitionModificationApplier.apply(partition, partitionModification));
  }

}
