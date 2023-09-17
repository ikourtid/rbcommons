package com.rb.nonbiz.collections;

import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.BitSet;
import java.util.List;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;

/**
 * Various helper methods pertaining to {@link BitSet}.
 */
public class RBBitSets {

  public static BitSet emptyBitSet() {
    return new BitSet(0);
  }

  public static BitSet singletonBitSet(boolean onlyValue) {
    BitSet bitSet = new BitSet(1);
    bitSet.set(0, onlyValue);
    return bitSet;
  }

  public static BitSet bitSetOf(boolean first, boolean second, boolean ... rest) {
    int size = 2 + rest.length;
    BitSet bitSet = new BitSet(size);
    bitSet.set(0, first);
    bitSet.set(1, second);
    for (int i = 0; i < rest.length; i++) {
      bitSet.set(i + 2, rest[i]);
    }
    return bitSet;
  }

  /**
   * Returns a new list that only includes the items from the original list when the bitset is set to true for that
   * array index.
   */
  public static <T> List<T> filterListUsingBitSet(List<T> list, BitSet bitSet) {
    // Normally, we would check that the bitset and the list have the same size, but with BitSet, there is no easy
    // way to do that, surprisingly. But we can at least check for the following.
    RBPreconditions.checkArgument(
        list.size() >= bitSet.length(),
        "The list size %s must be >= the bitset length of %s (length is measured using the index of the last 'true' bit).",
        list.size(), bitSet.length());
    List<T> toReturn = newArrayListWithExpectedSize(bitSet.cardinality());
    for (int i = 0; i < list.size(); i++) {
      if (bitSet.get(i)) {
        toReturn.add(list.get(i));
      }
    }
    return toReturn;
  }

}
