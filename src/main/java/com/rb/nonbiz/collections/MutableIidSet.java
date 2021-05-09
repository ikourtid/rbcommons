package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import gnu.trove.set.hash.TLongHashSet;

public class MutableIidSet extends MutableHasLongSet<InstrumentId> {

  private MutableIidSet(TLongHashSet rawSet) {
    super(rawSet);
  }

  public static MutableIidSet newMutableIidSetWithExpectedSize(int expectedSize) {
    int initialCapacity = (int) (expectedSize / DEFAULT_LOAD_FACTOR);
    return new MutableIidSet(new TLongHashSet(initialCapacity, DEFAULT_LOAD_FACTOR));
  }

  public static MutableIidSet newMutableIidSetWithExpectedSizeLike(HasRoughIidCount hasRoughIidCount) {
    return newMutableIidSetWithExpectedSize(hasRoughIidCount.getRoughIidCount());
  }

  public static MutableIidSet newMutableIidSet(IidSet initialSet) {
    int initialCapacity = (int) (initialSet.size() / DEFAULT_LOAD_FACTOR);
    TLongHashSet rawSet = new TLongHashSet(initialCapacity, DEFAULT_LOAD_FACTOR);
    initialSet.longsIterator().forEachRemaining(longId -> rawSet.add(longId));
    return new MutableIidSet(rawSet);
  }

}
