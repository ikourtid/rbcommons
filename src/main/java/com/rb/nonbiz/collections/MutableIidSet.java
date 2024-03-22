package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import gnu.trove.set.hash.TLongHashSet;

/**
 * A mutable version of an {@link IidSet}.
 *
 * <p> Our convention is to use such mutable classes in as few places as possible, and when we are finished
 * putting in data, 'lock' it by wrapping it into a corresponding immutable class - in this case, {@link IidSet}. </p>
 */
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
