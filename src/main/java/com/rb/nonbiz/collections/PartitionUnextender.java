package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;

/**
 * <p> This does the opposite of the {@link PartitionExtender}; it reduces the weight of an item in the partition. </p>
 *
 * <p> This has the semantics that if we extend by e.g. 0.2, then 0.2 refers to the total AFTER
 * extending. For example, extending a $1m portfolio with 0.2 KO will result in $250k KO (0.2 = 1.25m / 1m).
 * One *could* also have alternate semantics, so that 0.25 would achieve the same effect as above. However: </p>
 *
 * <ul>
 * <li> this way we could only extend by 100% (since we pass in a unit fraction whose max value is 1);
 * i.e. we can't add $101 KO to a portfolio that has $100.
 * Of course, we don't *have* to pass in a UnitFraction, but it's tighter semantics than passing any arbitrary
 * multiplier. In fact, all values in [0, 1) make sense; 1 does not because we'd have to add an infinite quantity
 * of this new item (or increase an existing item by an infinite amount) to have it become 100% of the new partition. </li>
 *
 * <li> the reason I originally created the PartitionExtender was to 'splice in' 20 bps of cash to an existing
 * target allocation. This 20 bps is typically defined in terms of the final portfolio, not the original. </li>
 * </ul>
 *
 * <p>
 * OK, so we want to keep the semantics symmetric here. The PartitionExtender conforms to:
 * "new value" = "old value" + unitFractionOfNewTotal * "new value"
 * e.g.
 * $1.25m = $1m + 0.2 * $1.25m
 * So what would 0.2 mean in this case if we reduce, in this example using 0.2 (but reducing down)?
 * "new value" = "old value" - unitFractionOfNewTotal * "new value"
 * </p>
 *
 * <pre>
 *   &lt;==&gt; new = old - 0.2 * new
 *   &lt;==&gt; new = old / 1.2 = 0.83333333333 * old
 * </pre>
 *
 * <p> The problem with these semantics though is that the extending and 'unextending' are not inverse functions.
 * It would be clear to have unextend(extend(partition, 0.2), 0.2) == partition.
 * Plus, it's a bit easier to think about it. In the case of extending, there actually is some new amount that
 * is 0.2 of the new total. In the case of unextending, there isn't; it's not in the new total. </p>
 *
 * <p> The other advantage of these semantics is that, just like with PartitionExtender, the range of valid values to
 * 'unextend' by is again in [0, 1). If we 'unextend' by 0, we do nothing (easy); if we unextend by 100% - epsilon
 * an item that's 100% - epsilon of a 2-item partition, we'll still a valid partition, namely 100% of the other item. </p>
 *
 * <p> So these are the semantics we will use. </p>
 *
 * <p> Therefore, post-unextension partition "value" = (1 - 0.2) * "pre", in this example. I.e. $1m = 0.8 * $1.25m </p>
 */
public class PartitionUnextender {

  // existingKey = existing key to reduce or altogether remove
  public <K> Partition<K> unextend(Partition<K> startingPartition, K existingKey, UnitFraction unitFractionOfOldTotal) {
    RBPreconditions.checkArgument(
        !unitFractionOfOldTotal.isZero() && !unitFractionOfOldTotal.isOne(),
        "Cannot remove %s from a partition if its weight of the old total is 0 or 1; got %s ; partition was %s",
        existingKey, unitFractionOfOldTotal, startingPartition);
    RBPreconditions.checkArgument(
        startingPartition.size() > 1,
        "It makes no sense to unextend a singleton partition of %s; there's no other key to bump up correspondingly",
        startingPartition);
    MutableRBMap<K, UnitFraction> newFractions = newMutableRBMapWithExpectedSize(startingPartition.size());
    UnitFraction divisor = UNIT_FRACTION_1.subtract(unitFractionOfOldTotal);
    // Add all the other items, with their (now bumped up) weights in the new partition.
    startingPartition.keySet()
        .stream()
        .filter(key -> !key.equals(existingKey))
        .forEach(key -> newFractions.putAssumingAbsent(key, startingPartition.getFraction(key).divide(divisor)));

    UnitFraction oldMembership = getOrThrow(
        startingPartition.getRawFractionsMap().getOptional(existingKey),
        "You are trying to reduce %s by %s but it doesn't exist in the partition of %s",
        existingKey, unitFractionOfOldTotal, startingPartition);
    // Now possibly add the item being 'unextended', or skip it altogether if we are unextending its entire %
    if (unitFractionOfOldTotal.almostEquals(oldMembership, DEFAULT_EPSILON_1e_8)) {
      // removing entire amount; do nothing
    } else {
      RBPreconditions.checkArgument(
          unitFractionOfOldTotal.isLessThan(oldMembership),
          "We are trying to reduce %s by %s which is more than its weight of %s in partition %s",
          existingKey, unitFractionOfOldTotal, oldMembership, startingPartition);
      newFractions.putAssumingAbsent(existingKey, oldMembership.subtract(unitFractionOfOldTotal).divide(divisor));
    }
    return partition(newRBMap(newFractions));
  }

}
