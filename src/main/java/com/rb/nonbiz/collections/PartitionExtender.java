package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;

/**
 * 'Extends' an existing partition by adding a new item and normalizing the rest.
 * <p> E.g. extending partition {a {@code ->}  .6, b {@code ->}  .4} by adding c {@code ->}  .2
 * will result in {a {@code ->}  .48, b {@code ->}  .32, c {@code ->}  .2}
 * (i.e. normalize everything else to sum to (1 - 0.2) but have the same relative proportions as before.) </p>
 *
 * @see PartitionUnextender
 */
public class PartitionExtender {

  public <K> Partition<K> extend(Partition<K> startingPartition, K newKey, UnitFraction unitFractionOfNewTotal) {
    RBPreconditions.checkArgument(
        !unitFractionOfNewTotal.isZero() && !unitFractionOfNewTotal.isOne(),
        "Cannot add %s to a partition if its weight of the new total is 0 or 1; got %s ; partition was %s",
        newKey, unitFractionOfNewTotal, startingPartition);
    MutableRBMap<K, UnitFraction> newFractions = newMutableRBMapWithExpectedSize(startingPartition.size() + 1);
    UnitFraction multiplier = UNIT_FRACTION_1.subtract(unitFractionOfNewTotal);
    startingPartition.keySet().forEach(key ->
        newFractions.putAssumingAbsent(key, startingPartition.getFraction(key).multiply(multiplier)));
    newFractions.putOrModifyExisting(newKey, unitFractionOfNewTotal, UnitFraction::add);
    return partition(newRBMap(newFractions));
  }

}
