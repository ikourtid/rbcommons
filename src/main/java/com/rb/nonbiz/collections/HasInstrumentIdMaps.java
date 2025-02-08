package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableMap;
import com.rb.biz.types.asset.HasInstrumentId;

import java.util.function.Function;

import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.Pair.pair;

/**
 * Various static utility methods pertaining to {@link HasInstrumentIdMap}.
 */
public class HasInstrumentIdMaps {

  public static <T extends HasInstrumentId, V> HasInstrumentIdMap<T, V> newHasInstrumentIdMap(
      MutableIidMap<Pair<T, V>> mutableMap) {
    return new HasInstrumentIdMap<>(mutableMap.getRawMap());
  }

  /**
   * Unlike {@link ImmutableMap#of()}, there is no 0-pair override.
   * This is to force you to use emptyRBMap, which is more explicit and makes reading tests easier.
   * Likewise for singletonRBMap().
   */
  public static <T extends HasInstrumentId, V> HasInstrumentIdMap<T, V> emptyHasInstrumentIdMap() {
    return newHasInstrumentIdMap(newMutableIidMapWithExpectedSize(1));
  }

  /**
   * Unlike {@link ImmutableMap#of()}, there is no single-pair override for HasInstrumentIdMapOf.
   * This is to force you to use singletonHasInstrumentIdMap, which is more explicit and makes reading tests easier.
   * Likewise for emptyHasInstrumentIdMap().
   */
  public static <T extends HasInstrumentId, V> HasInstrumentIdMap<T, V> singletonHasInstrumentIdMap(T k1, V v1) {
    MutableIidMap<Pair<T, V>> map = newMutableIidMapWithExpectedSize(1);
    map.putAssumingAbsent(k1.getInstrumentId(), pair(k1, v1));
    return newHasInstrumentIdMap(map);
  }

  public static <T extends HasInstrumentId, V> HasInstrumentIdMap<T, V> hasInstrumentIdMapOf(
      T k1, V v1,
      T k2, V v2) {
    MutableIidMap<Pair<T, V>> map = newMutableIidMapWithExpectedSize(2);
    map.putAssumingAbsent(k1.getInstrumentId(), pair(k1, v1));
    map.putAssumingAbsent(k2.getInstrumentId(), pair(k2, v2));
    return newHasInstrumentIdMap(map);
  }

  public static <T extends HasInstrumentId, V> HasInstrumentIdMap<T, V> hasInstrumentIdMapOf(
      T k1, V v1,
      T k2, V v2,
      T k3, V v3) {
    MutableIidMap<Pair<T, V>> map = newMutableIidMapWithExpectedSize(3);
    map.putAssumingAbsent(k1.getInstrumentId(), pair(k1, v1));
    map.putAssumingAbsent(k2.getInstrumentId(), pair(k2, v2));
    map.putAssumingAbsent(k3.getInstrumentId(), pair(k3, v3));
    return newHasInstrumentIdMap(map);
  }

  public static <T extends HasInstrumentId, V> HasInstrumentIdMap<T, V> hasInstrumentIdMapOf(
      T k1, V v1,
      T k2, V v2,
      T k3, V v3,
      T k4, V v4) {
    MutableIidMap<Pair<T, V>> map = newMutableIidMapWithExpectedSize(4);
    map.putAssumingAbsent(k1.getInstrumentId(), pair(k1, v1));
    map.putAssumingAbsent(k2.getInstrumentId(), pair(k2, v2));
    map.putAssumingAbsent(k3.getInstrumentId(), pair(k3, v3));
    map.putAssumingAbsent(k4.getInstrumentId(), pair(k4, v4));
    return newHasInstrumentIdMap(map);
  }

  public static <T extends HasInstrumentId, V> HasInstrumentIdMap<T, V> hasInstrumentIdMapOf(
      T k1, V v1,
      T k2, V v2,
      T k3, V v3,
      T k4, V v4,
      T k5, V v5) {
    MutableIidMap<Pair<T, V>> map = newMutableIidMapWithExpectedSize(5);
    map.putAssumingAbsent(k1.getInstrumentId(), pair(k1, v1));
    map.putAssumingAbsent(k2.getInstrumentId(), pair(k2, v2));
    map.putAssumingAbsent(k3.getInstrumentId(), pair(k3, v3));
    map.putAssumingAbsent(k4.getInstrumentId(), pair(k4, v4));
    map.putAssumingAbsent(k5.getInstrumentId(), pair(k5, v5));
    return newHasInstrumentIdMap(map);
  }

  public static <T extends HasInstrumentId, V> HasInstrumentIdMap<T, V> hasInstrumentIdMapOf(
      T k1, V v1,
      T k2, V v2,
      T k3, V v3,
      T k4, V v4,
      T k5, V v5,
      T k6, V v6) {
    MutableIidMap<Pair<T, V>> map = newMutableIidMapWithExpectedSize(6);
    map.putAssumingAbsent(k1.getInstrumentId(), pair(k1, v1));
    map.putAssumingAbsent(k2.getInstrumentId(), pair(k2, v2));
    map.putAssumingAbsent(k3.getInstrumentId(), pair(k3, v3));
    map.putAssumingAbsent(k4.getInstrumentId(), pair(k4, v4));
    map.putAssumingAbsent(k5.getInstrumentId(), pair(k5, v5));
    map.putAssumingAbsent(k6.getInstrumentId(), pair(k6, v6));
    return newHasInstrumentIdMap(map);
  }

  public static <T extends HasInstrumentId, V> HasInstrumentIdMap<T, V> hasInstrumentIdMapOf(
      T k1, V v1,
      T k2, V v2,
      T k3, V v3,
      T k4, V v4,
      T k5, V v5,
      T k6, V v6,
      T k7, V v7) {
    MutableIidMap<Pair<T, V>> map = newMutableIidMapWithExpectedSize(9);
    map.putAssumingAbsent(k1.getInstrumentId(), pair(k1, v1));
    map.putAssumingAbsent(k2.getInstrumentId(), pair(k2, v2));
    map.putAssumingAbsent(k3.getInstrumentId(), pair(k3, v3));
    map.putAssumingAbsent(k4.getInstrumentId(), pair(k4, v4));
    map.putAssumingAbsent(k5.getInstrumentId(), pair(k5, v5));
    map.putAssumingAbsent(k6.getInstrumentId(), pair(k6, v6));
    map.putAssumingAbsent(k7.getInstrumentId(), pair(k7, v7));
    return newHasInstrumentIdMap(map);
  }

  public static <T extends HasInstrumentId, V> HasInstrumentIdMap<T, V> hasInstrumentIdMapOf(
      T k1, V v1,
      T k2, V v2,
      T k3, V v3,
      T k4, V v4,
      T k5, V v5,
      T k6, V v6,
      T k7, V v7,
      T k8, V v8) {
    MutableIidMap<Pair<T, V>> map = newMutableIidMapWithExpectedSize(9);
    map.putAssumingAbsent(k1.getInstrumentId(), pair(k1, v1));
    map.putAssumingAbsent(k2.getInstrumentId(), pair(k2, v2));
    map.putAssumingAbsent(k3.getInstrumentId(), pair(k3, v3));
    map.putAssumingAbsent(k4.getInstrumentId(), pair(k4, v4));
    map.putAssumingAbsent(k5.getInstrumentId(), pair(k5, v5));
    map.putAssumingAbsent(k6.getInstrumentId(), pair(k6, v6));
    map.putAssumingAbsent(k7.getInstrumentId(), pair(k7, v7));
    map.putAssumingAbsent(k8.getInstrumentId(), pair(k8, v8));
    return newHasInstrumentIdMap(map);
  }

  public static <T extends HasInstrumentId, V> HasInstrumentIdMap<T, V> hasInstrumentIdMapOf(
      T k1, V v1,
      T k2, V v2,
      T k3, V v3,
      T k4, V v4,
      T k5, V v5,
      T k6, V v6,
      T k7, V v7,
      T k8, V v8,
      T k9, V v9) {
    MutableIidMap<Pair<T, V>> map = newMutableIidMapWithExpectedSize(9);
    map.putAssumingAbsent(k1.getInstrumentId(), pair(k1, v1));
    map.putAssumingAbsent(k2.getInstrumentId(), pair(k2, v2));
    map.putAssumingAbsent(k3.getInstrumentId(), pair(k3, v3));
    map.putAssumingAbsent(k4.getInstrumentId(), pair(k4, v4));
    map.putAssumingAbsent(k5.getInstrumentId(), pair(k5, v5));
    map.putAssumingAbsent(k6.getInstrumentId(), pair(k6, v6));
    map.putAssumingAbsent(k7.getInstrumentId(), pair(k7, v7));
    map.putAssumingAbsent(k8.getInstrumentId(), pair(k8, v8));
    map.putAssumingAbsent(k9.getInstrumentId(), pair(k9, v9));
    return newHasInstrumentIdMap(map);
  }

  public static <T extends HasInstrumentId, V> HasInstrumentIdMap<T, V> hasInstrumentIdMapFromSet(
      HasInstrumentIdSet<T> hasInstrumentIds,
      Function<T, V> transformer) {
    MutableIidMap<Pair<T, V>> mutableMap = newMutableIidMapWithExpectedSize(hasInstrumentIds.size());
    hasInstrumentIds
        .stream()
        .forEach(hasInstrumentId -> mutableMap.putAssumingAbsent(hasInstrumentId.getInstrumentId(),
            pair(hasInstrumentId, transformer.apply(hasInstrumentId))));
    return newHasInstrumentIdMap(mutableMap);
  }

}
