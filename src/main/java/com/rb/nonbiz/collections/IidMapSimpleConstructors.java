package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableMap;
import com.rb.biz.types.asset.InstrumentId;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.Map;

import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;

/**
 * Various simple utility methods for constructing an {@link IidMap}.
 */
public class IidMapSimpleConstructors {

  // We use SuppressWarnings because we can use the same instance everywhere; the generic class does not matter.
  @SuppressWarnings("rawtypes")
  private static final IidMap EMPTY_INSTANCE = newIidMap(newMutableIidMapWithExpectedSize(1));

  public static <V> IidMap<V> newIidMap(TLongObjectHashMap<V> rawMap) {
    if (rawMap.isEmpty()) {
      // This is a memory performance improvement. If we initialize a MutableIidMap without giving it a size hint,
      // it currently (May 2019) starts with a capacity of 20 items. If the underlying TLongObjectHashMap is empty
      // (e.g. because the code never ended up putting anything in it), then we want the system to be able to
      // garbage-collect it. Since there is only one EMPTY_INSTANCE, we get to reuse it for all cases where an
      // empty IidMap is used. This works because IidMaps are immutable, so it's OK to share a read-only instance.

      // We use the suppression below, because we can use the same instance everywhere; the generic class does not matter.
      //noinspection unchecked
      return EMPTY_INSTANCE;
    }
    return new IidMap<>(rawMap);
  }

  public static <V> IidMap<V> newIidMap(MutableIidMap<V> mutableMap) {
    return new IidMap<>(mutableMap.getRawMap());
  }

  /**
   * There are a few cases where we need to create an {@link IidMap} based off a plain Map,
   * e.g. if we use {@link ImmutableMap#builder}.
   */
  public static <V> IidMap<V> newIidMap(Map<InstrumentId, V> initialMap) {
    if (initialMap.isEmpty()) {
      // This is a memory performance improvement. If we initialize a MutableIidMap without giving it a size hint,
      // it currently (May 2019) starts with a capacity of 20 items. If the underlying TLongObjectHashMap is empty
      // (e.g. because the code never ended up putting anything in it), then we want the system to be able to
      // garbage-collect it. Since there is only one EMPTY_INSTANCE, we get to reuse it for all cases where an
      // empty IidMap is used. This works because IidMaps are immutable, so it's OK to share a read-only instance.

      // We use the suppression below, because we can use the same instance everywhere; the generic class does not matter.
      //noinspection unchecked
      return EMPTY_INSTANCE;
    }
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(initialMap.size());
    initialMap.entrySet().forEach(entry -> {
      InstrumentId instrumentId = entry.getKey();
      V value = entry.getValue();
      mutableMap.put(instrumentId, value);
    });
    return newIidMap(mutableMap);
  }

  /**
   * Unlike ImmutableMap#of, there is no 0-pair override for iidOf.
   * This is to force you to use emptyRBMap, which is more explicit and makes reading tests easier.
   * Likewise for singletonRBMap().
   */
  public static <V> IidMap<V> emptyIidMap() {
    // We use the suppression below, because we can use the same instance everywhere; the generic class does not matter.
    //noinspection unchecked
    return EMPTY_INSTANCE;
  }

  /**
   * Unlike ImmutableMap#of, there is no single-pair override for iidOf.
   * This is to force you to use singletoniid, which is more explicit and makes reading tests easier.
   * Likewise for emptyiid().
   */
  public static <V> IidMap<V> singletonIidMap(InstrumentId k1, V v1) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(1);
    map.putAssumingAbsent(k1, v1);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(2);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(3);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(4);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(5);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(6);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(7);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7,
                                       InstrumentId k8, V v8) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(8);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    map.putAssumingAbsent(k8, v8);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7,
                                       InstrumentId k8, V v8,
                                       InstrumentId k9, V v9) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(9);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    map.putAssumingAbsent(k8, v8);
    map.putAssumingAbsent(k9, v9);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7,
                                       InstrumentId k8, V v8,
                                       InstrumentId k9, V v9,
                                       InstrumentId k10, V v10) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(10);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    map.putAssumingAbsent(k8, v8);
    map.putAssumingAbsent(k9, v9);
    map.putAssumingAbsent(k10, v10);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7,
                                       InstrumentId k8, V v8,
                                       InstrumentId k9, V v9,
                                       InstrumentId k10, V v10,
                                       InstrumentId k11, V v11) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(11);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    map.putAssumingAbsent(k8, v8);
    map.putAssumingAbsent(k9, v9);
    map.putAssumingAbsent(k10, v10);
    map.putAssumingAbsent(k11, v11);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7,
                                       InstrumentId k8, V v8,
                                       InstrumentId k9, V v9,
                                       InstrumentId k10, V v10,
                                       InstrumentId k11, V v11,
                                       InstrumentId k12, V v12) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(12);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    map.putAssumingAbsent(k8, v8);
    map.putAssumingAbsent(k9, v9);
    map.putAssumingAbsent(k10, v10);
    map.putAssumingAbsent(k11, v11);
    map.putAssumingAbsent(k12, v12);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7,
                                       InstrumentId k8, V v8,
                                       InstrumentId k9, V v9,
                                       InstrumentId k10, V v10,
                                       InstrumentId k11, V v11,
                                       InstrumentId k12, V v12,
                                       InstrumentId k13, V v13) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(13);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    map.putAssumingAbsent(k8, v8);
    map.putAssumingAbsent(k9, v9);
    map.putAssumingAbsent(k10, v10);
    map.putAssumingAbsent(k11, v11);
    map.putAssumingAbsent(k12, v12);
    map.putAssumingAbsent(k13, v13);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7,
                                       InstrumentId k8, V v8,
                                       InstrumentId k9, V v9,
                                       InstrumentId k10, V v10,
                                       InstrumentId k11, V v11,
                                       InstrumentId k12, V v12,
                                       InstrumentId k13, V v13,
                                       InstrumentId k14, V v14) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(14);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    map.putAssumingAbsent(k8, v8);
    map.putAssumingAbsent(k9, v9);
    map.putAssumingAbsent(k10, v10);
    map.putAssumingAbsent(k11, v11);
    map.putAssumingAbsent(k12, v12);
    map.putAssumingAbsent(k13, v13);
    map.putAssumingAbsent(k14, v14);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7,
                                       InstrumentId k8, V v8,
                                       InstrumentId k9, V v9,
                                       InstrumentId k10, V v10,
                                       InstrumentId k11, V v11,
                                       InstrumentId k12, V v12,
                                       InstrumentId k13, V v13,
                                       InstrumentId k14, V v14,
                                       InstrumentId k15, V v15) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(15);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    map.putAssumingAbsent(k8, v8);
    map.putAssumingAbsent(k9, v9);
    map.putAssumingAbsent(k10, v10);
    map.putAssumingAbsent(k11, v11);
    map.putAssumingAbsent(k12, v12);
    map.putAssumingAbsent(k13, v13);
    map.putAssumingAbsent(k14, v14);
    map.putAssumingAbsent(k15, v15);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7,
                                       InstrumentId k8, V v8,
                                       InstrumentId k9, V v9,
                                       InstrumentId k10, V v10,
                                       InstrumentId k11, V v11,
                                       InstrumentId k12, V v12,
                                       InstrumentId k13, V v13,
                                       InstrumentId k14, V v14,
                                       InstrumentId k15, V v15,
                                       InstrumentId k16, V v16) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(16);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    map.putAssumingAbsent(k8, v8);
    map.putAssumingAbsent(k9, v9);
    map.putAssumingAbsent(k10, v10);
    map.putAssumingAbsent(k11, v11);
    map.putAssumingAbsent(k12, v12);
    map.putAssumingAbsent(k13, v13);
    map.putAssumingAbsent(k14, v14);
    map.putAssumingAbsent(k15, v15);
    map.putAssumingAbsent(k16, v16);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7,
                                       InstrumentId k8, V v8,
                                       InstrumentId k9, V v9,
                                       InstrumentId k10, V v10,
                                       InstrumentId k11, V v11,
                                       InstrumentId k12, V v12,
                                       InstrumentId k13, V v13,
                                       InstrumentId k14, V v14,
                                       InstrumentId k15, V v15,
                                       InstrumentId k16, V v16,
                                       InstrumentId k17, V v17) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(17);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    map.putAssumingAbsent(k8, v8);
    map.putAssumingAbsent(k9, v9);
    map.putAssumingAbsent(k10, v10);
    map.putAssumingAbsent(k11, v11);
    map.putAssumingAbsent(k12, v12);
    map.putAssumingAbsent(k13, v13);
    map.putAssumingAbsent(k14, v14);
    map.putAssumingAbsent(k15, v15);
    map.putAssumingAbsent(k16, v16);
    map.putAssumingAbsent(k17, v17);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7,
                                       InstrumentId k8, V v8,
                                       InstrumentId k9, V v9,
                                       InstrumentId k10, V v10,
                                       InstrumentId k11, V v11,
                                       InstrumentId k12, V v12,
                                       InstrumentId k13, V v13,
                                       InstrumentId k14, V v14,
                                       InstrumentId k15, V v15,
                                       InstrumentId k16, V v16,
                                       InstrumentId k17, V v17,
                                       InstrumentId k18, V v18) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(18);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    map.putAssumingAbsent(k8, v8);
    map.putAssumingAbsent(k9, v9);
    map.putAssumingAbsent(k10, v10);
    map.putAssumingAbsent(k11, v11);
    map.putAssumingAbsent(k12, v12);
    map.putAssumingAbsent(k13, v13);
    map.putAssumingAbsent(k14, v14);
    map.putAssumingAbsent(k15, v15);
    map.putAssumingAbsent(k16, v16);
    map.putAssumingAbsent(k17, v17);
    map.putAssumingAbsent(k18, v18);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7,
                                       InstrumentId k8, V v8,
                                       InstrumentId k9, V v9,
                                       InstrumentId k10, V v10,
                                       InstrumentId k11, V v11,
                                       InstrumentId k12, V v12,
                                       InstrumentId k13, V v13,
                                       InstrumentId k14, V v14,
                                       InstrumentId k15, V v15,
                                       InstrumentId k16, V v16,
                                       InstrumentId k17, V v17,
                                       InstrumentId k18, V v18,
                                       InstrumentId k19, V v19) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(19);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    map.putAssumingAbsent(k8, v8);
    map.putAssumingAbsent(k9, v9);
    map.putAssumingAbsent(k10, v10);
    map.putAssumingAbsent(k11, v11);
    map.putAssumingAbsent(k12, v12);
    map.putAssumingAbsent(k13, v13);
    map.putAssumingAbsent(k14, v14);
    map.putAssumingAbsent(k15, v15);
    map.putAssumingAbsent(k16, v16);
    map.putAssumingAbsent(k17, v17);
    map.putAssumingAbsent(k18, v18);
    map.putAssumingAbsent(k19, v19);
    return newIidMap(map);
  }

  public static <V> IidMap<V> iidMapOf(InstrumentId k1, V v1,
                                       InstrumentId k2, V v2,
                                       InstrumentId k3, V v3,
                                       InstrumentId k4, V v4,
                                       InstrumentId k5, V v5,
                                       InstrumentId k6, V v6,
                                       InstrumentId k7, V v7,
                                       InstrumentId k8, V v8,
                                       InstrumentId k9, V v9,
                                       InstrumentId k10, V v10,
                                       InstrumentId k11, V v11,
                                       InstrumentId k12, V v12,
                                       InstrumentId k13, V v13,
                                       InstrumentId k14, V v14,
                                       InstrumentId k15, V v15,
                                       InstrumentId k16, V v16,
                                       InstrumentId k17, V v17,
                                       InstrumentId k18, V v18,
                                       InstrumentId k19, V v19,
                                       InstrumentId k20, V v20) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(20);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    map.putAssumingAbsent(k6, v6);
    map.putAssumingAbsent(k7, v7);
    map.putAssumingAbsent(k8, v8);
    map.putAssumingAbsent(k9, v9);
    map.putAssumingAbsent(k10, v10);
    map.putAssumingAbsent(k11, v11);
    map.putAssumingAbsent(k12, v12);
    map.putAssumingAbsent(k13, v13);
    map.putAssumingAbsent(k14, v14);
    map.putAssumingAbsent(k15, v15);
    map.putAssumingAbsent(k16, v16);
    map.putAssumingAbsent(k17, v17);
    map.putAssumingAbsent(k18, v18);
    map.putAssumingAbsent(k19, v19);
    map.putAssumingAbsent(k20, v20);
    return newIidMap(map);
  }

}
