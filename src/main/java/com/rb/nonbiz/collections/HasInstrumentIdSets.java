package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.HasInstrumentId;

import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;


public class HasInstrumentIdSets {

  public static <T extends HasInstrumentId> HasInstrumentIdSet<T> newHasInstrumentIdSet(
      IidMap<T> iidMap) {
    return new HasInstrumentIdSet<>(iidMap.getRawMapUnsafe());
  }

  public static <T extends HasInstrumentId> HasInstrumentIdSet<T> newHasInstrumentIdSet(
      MutableIidMap<T> mutableMap) {
    return new HasInstrumentIdSet<>(mutableMap.getRawMap());
  }

  /**
   * Unlike ImmutableSet#of, there is no 0-pair override for hasInstrumentIdSetOf.
   * This is to force you to use emptyRBSet, which is more explicit and makes reading tests easier.
   * Likewise for singletonRBMap().
   */
  public static <T extends HasInstrumentId> HasInstrumentIdSet<T> emptyHasInstrumentIdSet() {
    return newHasInstrumentIdSet(newMutableIidMapWithExpectedSize(1));
  }

  /**
   * Unlike ImmutableSet#of, there is no single-pair override for HasInstrumentIdSetOf.
   * This is to force you to use singletonHasInstrumentIdSet, which is more explicit and makes reading tests easier.
   * Likewise for emptyHasInstrumentIdSet().
   */
  public static <T extends HasInstrumentId> HasInstrumentIdSet<T> singletonHasInstrumentIdSet(T k1) {
    MutableIidMap<T> map = newMutableIidMapWithExpectedSize(1);
    map.putAssumingAbsent(k1.getInstrumentId(), k1);
    return newHasInstrumentIdSet(map);
  }

  @SafeVarargs
  public static <T extends HasInstrumentId> HasInstrumentIdSet<T> hasInstrumentIdSetOf(
      T first, T second, T ... rest) {
    MutableIidMap<T> map = newMutableIidMapWithExpectedSize(rest.length + 2); // +2 for 'first' and 'second'
    map.put(first.getInstrumentId(), first); // first one can't already exist in the set that starts out empty
    map.putAssumingAbsent(second.getInstrumentId(), second);
    for (T item : rest) {
      map.putAssumingAbsent(item.getInstrumentId(), item);
    }
    return newHasInstrumentIdSet(map);
  }

}
