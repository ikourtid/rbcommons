package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableMap;

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
    return newRBMap(ImmutableMap.of(k1, v1, k2, v2));
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
    return newRBMap(ImmutableMap.of(k1, v1, k2, v2, k3, v3));
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    return newRBMap(ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4));
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    return newRBMap(ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
    return newRBMap(new ImmutableMap.Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .build());
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7) {
    return newRBMap(new ImmutableMap.Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .build());
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8) {
    return newRBMap(new ImmutableMap.Builder<K, V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .put(k6, v6)
        .put(k7, v7)
        .put(k8, v8)
        .build());
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
    return newRBMap(new ImmutableMap.Builder<K, V>()
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
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
    return newRBMap(new ImmutableMap.Builder<K, V>()
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
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                           K k11, V v11) {
    return newRBMap(new ImmutableMap.Builder<K, V>()
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
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                           K k11, V v11, K k12, V v12) {
    return newRBMap(new ImmutableMap.Builder<K, V>()
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
  }

  public static <K, V> RBMap<K, V> rbMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                           K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                           K k11, V v11, K k12, V v12, K k13, V v13) {
    return newRBMap(new ImmutableMap.Builder<K, V>()
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
  }

}
