package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import gnu.trove.map.hash.TLongObjectHashMap;

public class MutableIidMap<V> extends MutableHasLongMap<InstrumentId, V> {

  private MutableIidMap(TLongObjectHashMap<V> rawMap) {
    super(rawMap);
  }

  public static <V> MutableIidMap<V> newMutableIidMap() {
    return newMutableIidMapWithExpectedSize(DEFAULT_INITIAL_SIZE);
  }

  public static <V> MutableIidMap<V> newMutableIidMapWithExpectedSize(
      int expectedSize) {
    int initialCapacity = (int) (expectedSize / DEFAULT_LOAD_FACTOR);
    return new MutableIidMap<>(new TLongObjectHashMap<V>(initialCapacity, DEFAULT_LOAD_FACTOR));
  }

  public static <V> MutableIidMap<V> newMutableIidMapWithExpectedSizeLike(HasRoughIidCount hasRoughIidCount) {
    return newMutableIidMapWithExpectedSize(hasRoughIidCount.getRoughIidCount());
  }

  public static <V> MutableIidMap<V> newMutableIidMap(
      HasLongMap<InstrumentId, V> initialMap) {
    int initialCapacity = (int) (initialMap.size() / DEFAULT_LOAD_FACTOR);
    TLongObjectHashMap<V> rawMap = new TLongObjectHashMap<V>(initialCapacity, DEFAULT_LOAD_FACTOR);
    initialMap.forEach( (asLong, value) -> rawMap.put(asLong, value));
    return new MutableIidMap<>(rawMap);
  }

}
