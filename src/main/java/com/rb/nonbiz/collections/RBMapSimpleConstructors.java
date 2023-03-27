package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Map;

public class RBMapSimpleConstructors {

  private static final RBMap EMPTY_INSTANCE = new RBMap<>(ImmutableMap.of());

  public static <K, V> RBMap<K, V> newRBMap(Map<K, V> rawMap) {
    return rawMap.isEmpty()
           ? EMPTY_INSTANCE
           : new RBMap<K, V>(rawMap);
  }

  public static <K, V> RBMap<K, V> newRBMap(MutableRBMap<K, V> mutableRBMap) {
    return mutableRBMap.isEmpty()
           ? EMPTY_INSTANCE
           : new RBMap<K, V>(mutableRBMap.asMap());
  }

  /**
   * Unlike ImmutableMap#of, there is no 0-pair override for rbMapOf.
   * This is to force you to use emptyRBMap, which is more explicit and makes reading tests easier.
   * Likewise for singletonRBMap().
   */
  public static <K, V> RBMap<K, V> emptyRBMap() {
    return EMPTY_INSTANCE;
  }

  /**
   * Unlike ImmutableMap#of, there is no single-pair override for rbMapOf.
   * This is to force you to use singletonRBMap, which is more explicit and makes reading tests easier.
   * Likewise for emptyRBMap().
   */
  public static <K, V> RBMap<K, V> singletonRBMap(K k1, V v1) {
    return newRBMap(ImmutableMap.of(k1, v1));
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2) {
    RBMap<K, V> rbMap = newRBMap(ImmutableMap.of(k1, v1, k2, v2));
    checkSize(rbMap, 2);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
    RBMap<K, V> rbMap = newRBMap(ImmutableMap.of(k1, v1, k2, v2, k3, v3));
    checkSize(rbMap, 3);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    RBMap<K, V> rbMap = newRBMap(ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4));
    checkSize(rbMap, 4);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    RBMap<K, V> rbMap = newRBMap(ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
    checkSize(rbMap, 5);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .build());
    checkSize(rbMap, 6);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .build());
    checkSize(rbMap, 7);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .build());
    checkSize(rbMap, 8);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .put(k9, v9)
        .build());
    checkSize(rbMap, 9);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .put(k9, v9)
        .put(k10, v10)
        .build());
    checkSize(rbMap, 10);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                           K k11, V v11) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .put(k9, v9)
        .put(k10, v10)
        .put(k11, v11)
        .build());
    checkSize(rbMap, 11);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                           K k11, V v11, K k12, V v12) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .put(k9, v9)
        .put(k10, v10)
        .put(k11, v11)
        .put(k12, v12)
        .build());
    checkSize(rbMap, 12);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                           K k11, V v11, K k12, V v12, K k13, V v13) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .put(k9, v9)
        .put(k10, v10)
        .put(k11, v11)
        .put(k12, v12)
        .put(k13, v13)
        .build());
    checkSize(rbMap, 13);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                           K k11, V v11, K k12, V v12, K k13, V v13, K k14, V v14) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .put(k9, v9)
        .put(k10, v10)
        .put(k11, v11)
        .put(k12, v12)
        .put(k13, v13)
        .put(k14, v14)
        .build());
    checkSize(rbMap, 14);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                           K k11, V v11, K k12, V v12, K k13, V v13, K k14, V v14, K k15, V v15) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .put(k9, v9)
        .put(k10, v10)
        .put(k11, v11)
        .put(k12, v12)
        .put(k13, v13)
        .put(k14, v14)
        .put(k15, v15)
        .build());
    checkSize(rbMap, 15);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                           K k11, V v11, K k12, V v12, K k13, V v13, K k14, V v14,
                                           K k15, V v15, K k16, V v16) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .put(k9, v9)
        .put(k10, v10)
        .put(k11, v11)
        .put(k12, v12)
        .put(k13, v13)
        .put(k14, v14)
        .put(k15, v15)
        .put(k16, v16)
        .build());
    checkSize(rbMap, 16);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                           K k11, V v11, K k12, V v12, K k13, V v13, K k14, V v14,
                                           K k15, V v15, K k16, V v16, K k17, V v17) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .put(k9, v9)
        .put(k10, v10)
        .put(k11, v11)
        .put(k12, v12)
        .put(k13, v13)
        .put(k14, v14)
        .put(k15, v15)
        .put(k16, v16)
        .put(k17, v17)
        .build());
    checkSize(rbMap, 17);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                           K k11, V v11, K k12, V v12, K k13, V v13, K k14, V v14,
                                           K k15, V v15, K k16, V v16, K k17, V v17, K k18, V v18) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .put(k9, v9)
        .put(k10, v10)
        .put(k11, v11)
        .put(k12, v12)
        .put(k13, v13)
        .put(k14, v14)
        .put(k15, v15)
        .put(k16, v16)
        .put(k17, v17)
        .put(k18, v18)
        .build());
    checkSize(rbMap, 18);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                           K k11, V v11, K k12, V v12, K k13, V v13, K k14, V v14,
                                           K k15, V v15, K k16, V v16, K k17, V v17, K k18, V v18,
                                           K k19, V v19) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .put(k9, v9)
        .put(k10, v10)
        .put(k11, v11)
        .put(k12, v12)
        .put(k13, v13)
        .put(k14, v14)
        .put(k15, v15)
        .put(k16, v16)
        .put(k17, v17)
        .put(k18, v18)
        .put(k19, v19)
        .build());
    checkSize(rbMap, 19);
    return rbMap;
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                           K k11, V v11, K k12, V v12, K k13, V v13, K k14, V v14,
                                           K k15, V v15, K k16, V v16, K k17, V v17, K k18, V v18,
                                           K k19, V v19, K k20, V v20) {
    RBMap<K, V> rbMap = newRBMap(new Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .put(k9, v9)
        .put(k10, v10)
        .put(k11, v11)
        .put(k12, v12)
        .put(k13, v13)
        .put(k14, v14)
        .put(k15, v15)
        .put(k16, v16)
        .put(k17, v17)
        .put(k18, v18)
        .put(k19, v19)
        .put(k20, v20)
        .build());
    checkSize(rbMap, 20);
    return rbMap;
  }

  // To make sure that no keys are duplicates, check that the size is as expected.
  // We do this rather than use .putAssumingAbsent() because the Map Builder (from Guava)
  // does not have putAssumingAbsent().
  private static <K, V> void checkSize(RBMap<K, V> rbMap, int n) {
    RBPreconditions.checkArgument(
        rbMap.size() == n,
        "RBMap should have been of size %s but had size %s: %s",
        n, rbMap.size(), rbMap);
  }

}
