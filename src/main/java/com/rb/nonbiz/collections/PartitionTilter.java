package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.PositiveMultipliersMap;

import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.collections.RBSets.noSharedItems;

/**
 * 'Tilts' a partition by bumping some (or all) of a partition's item weights up or down,
 * with the result being normalized so that the final weights still sum to 100%.
 *
 * <p> Like almost everywhere in the codebase, we use immutable data classes, so this creates a new tilted
 * partition. It will not modify the one passed in. </p>
 *
 * <p> Multipliers for keys not in the partition will get ignored (i.e. they will not cause an exception).
 * Similarly, a missing multiplier for a key will be interpreted as 'no multiplier'. </p>
 */
public class PartitionTilter {

  public <K> Partition<K> tiltPartition(Partition<K> original, PositiveMultipliersMap<K> multipliers) {
    if (noSharedItems(original.getRawFractionsMap().keySet(), multipliers.getRawMap().keySet())) {
      return original; // small performance optimization
    }
    return Partition.partitionFromPositiveWeightsWhichMayNotSumTo1(
        doubleMap(
            original.getRawFractionsMap()
                .transformEntriesCopy( (key, originalWeight) ->
                    transformOptional(
                        multipliers.getRawMap().getOptional(key),
                        positiveMultiplier -> originalWeight.doubleValue() * positiveMultiplier.doubleValue())
                        .orElse(originalWeight.doubleValue()))));
  }

}
