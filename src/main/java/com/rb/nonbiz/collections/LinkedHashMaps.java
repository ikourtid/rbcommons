package com.rb.nonbiz.collections;

import com.rb.nonbiz.util.RBPreconditions;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.newLinkedHashMapWithExpectedSize;
import static com.rb.nonbiz.util.RBPreconditions.checkUnique;
import static java.util.function.Function.identity;

/**
 * Various helper methods related to {@link LinkedHashMap}.
 */
public class LinkedHashMaps {

  /**
   * Creates a {@link LinkedHashMap} which will have the same entries as the {@link RBMap} passed in,
   * but sorted in key order as per the specified {@link Comparator}.
   */
  public static <K, V> LinkedHashMap<K, V> toSortedLinkedHashMap(RBMap<K, V> map, Comparator<K> keysComparator) {
    return toSortedLinkedHashMapWithTransformedKeys(map, keysComparator, identity());
  }

  /**
   * Creates a {@link LinkedHashMap} which will have the same entries as the {@link RBMap} passed in,
   * but sorted in key order as per the specified {@link Comparator}.
   *
   * <p> The returned map will also have its keys transformed according to the key transformer function
   * passed in. </p>
   */
  public static <K1, K2, V> LinkedHashMap<K2, V> toSortedLinkedHashMapWithTransformedKeys(
      RBMap<K1, V> map, Comparator<K1> keysComparator, Function<K1, K2> keyTransformer) {
    return toSortedLinkedHashMapWithTransformedKeysAndValues(map, keysComparator, keyTransformer, identity());
  }

  /**
   * Creates a {@link LinkedHashMap} which will have the same entries as the {@link RBMap} passed in,
   * but sorted in key order as per the specified {@link Comparator}.
   *
   * <p> The returned map will also have its keys and values transformed according to the key and value
   * transformer functions (respectively) that are passed in. </p>
   */
  public static <K1, K2, V1, V2> LinkedHashMap<K2, V2> toSortedLinkedHashMapWithTransformedKeysAndValues(
      RBMap<K1, V1> map,
      Comparator<K1> keysComparator,
      Function<K1, K2> keyTransformer,
      Function<V1, V2> valueTransformer) {
    LinkedHashMap<K2, V2> linkedHashMap = newLinkedHashMapWithExpectedSize(map.size());
    map.forEachSortedEntry(
        (entry1, entry2) -> keysComparator.compare(entry1.getKey(), entry2.getKey()),
        (key, value) -> {
          K2 transformedKey = keyTransformer.apply(key);
          // There's no putAssumingAbsent like there is with RBMap; this is a LinkedHashMap. So we have to do it manually.
          RBPreconditions.checkArgument(
              !linkedHashMap.containsKey(transformedKey),
              "Transformation of LinkedHashMap key %s results in the same key %s from before; map was %s",
              key, transformedKey, map);
          linkedHashMap.put(transformedKey, valueTransformer.apply(value));
        });
    return linkedHashMap;
  }

  /**
   * Concatenates two {@link Map}s into a single {@link LinkedHashMap}.
   *
   * <p> If one of these maps is a {@link LinkedHashMap}, or some other map that guarantees ordering,
   * then the final map will have all sorted entries from the first map, then those of the second map.
   * Otherwise, if there's no guarantee of ordering in the map (e.g. with {@link HashMap}, then the only guarantee
   * is that the first map's items will appear before those of the second.
   * </p>
   */
  public static <K, V> LinkedHashMap<K, V> concatenateMapsIntoLinkedHashMap(
      Map<K, V> map1,
      Map<K, V> map2) {
    checkUnique(Stream.concat(
        map1.keySet().stream(),
        map2.keySet().stream()));

    LinkedHashMap<K, V> concatenatedMap = newLinkedHashMapWithExpectedSize(map1.size() + map2.size());
    concatenatedMap.putAll(map1);
    concatenatedMap.putAll(map2);
    return concatenatedMap;
  }

  public static <K, V> LinkedHashMap<K, V> singletonLinkedHashMap(
      K key1, V value1) {
    LinkedHashMap<K, V> mutableMap = new LinkedHashMap<>(1);
    mutableMap.put(key1, value1);
    return new LinkedHashMap<>(mutableMap);
  }

  public static <K, V> LinkedHashMap<K, V> linkedHashMapOf(
      K key1, V value1,
      K key2, V value2) {
    checkUnique(key1, key2);
    LinkedHashMap<K, V> mutableMap = new LinkedHashMap<>(2);
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    return new LinkedHashMap<>(mutableMap);
  }

  public static <K, V> LinkedHashMap<K, V> linkedHashMapOf(
      K key1, V value1,
      K key2, V value2,
      K key3, V value3) {
    checkUnique(key1, key2, key3);
    LinkedHashMap<K, V> mutableMap = new LinkedHashMap<>(3);
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    mutableMap.put(key3, value3);
    return new LinkedHashMap<>(mutableMap);
  }

  public static <K, V> LinkedHashMap<K, V> linkedHashMapOf(
      K key1, V value1,
      K key2, V value2,
      K key3, V value3,
      K key4, V value4) {
    checkUnique(key1, key2, key3, key4);
    LinkedHashMap<K, V> mutableMap = new LinkedHashMap<>(4);
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    mutableMap.put(key3, value3);
    mutableMap.put(key4, value4);
    return new LinkedHashMap<>(mutableMap);
  }

  public static <K, V> LinkedHashMap<K, V> linkedHashMapOf(
      K key1, V value1,
      K key2, V value2,
      K key3, V value3,
      K key4, V value4,
      K key5, V value5) {
    checkUnique(key1, key2, key3, key4, key5);
    LinkedHashMap<K, V> mutableMap = new LinkedHashMap<>(5);
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    mutableMap.put(key3, value3);
    mutableMap.put(key4, value4);
    mutableMap.put(key5, value5);
    return new LinkedHashMap<>(mutableMap);
  }

  public static <K, V> LinkedHashMap<K, V> linkedHashMapOf(
      K key1, V value1,
      K key2, V value2,
      K key3, V value3,
      K key4, V value4,
      K key5, V value5,
      K key6, V value6) {
    checkUnique(key1, key2, key3, key4, key5, key6);
    LinkedHashMap<K, V> mutableMap = new LinkedHashMap<>(6);
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    mutableMap.put(key3, value3);
    mutableMap.put(key4, value4);
    mutableMap.put(key5, value5);
    mutableMap.put(key6, value6);
    return new LinkedHashMap<>(mutableMap);
  }

  public static <K, V> LinkedHashMap<K, V> linkedHashMapOf(
      K key1, V value1,
      K key2, V value2,
      K key3, V value3,
      K key4, V value4,
      K key5, V value5,
      K key6, V value6,
      K key7, V value7) {
    checkUnique(key1, key2, key3, key4, key5, key6, key7);
    LinkedHashMap<K, V> mutableMap = new LinkedHashMap<>(7);
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    mutableMap.put(key3, value3);
    mutableMap.put(key4, value4);
    mutableMap.put(key5, value5);
    mutableMap.put(key6, value6);
    mutableMap.put(key7, value7);
    return new LinkedHashMap<>(mutableMap);
  }

  public static <K, V> LinkedHashMap<K, V> linkedHashMapOf(
      K key1, V value1,
      K key2, V value2,
      K key3, V value3,
      K key4, V value4,
      K key5, V value5,
      K key6, V value6,
      K key7, V value7,
      K key8, V value8) {
    checkUnique(key1, key2, key3, key4, key5, key6, key7, key8);
    LinkedHashMap<K, V> mutableMap = new LinkedHashMap<>(8);
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    mutableMap.put(key3, value3);
    mutableMap.put(key4, value4);
    mutableMap.put(key5, value5);
    mutableMap.put(key6, value6);
    mutableMap.put(key7, value7);
    mutableMap.put(key8, value8);
    return new LinkedHashMap<>(mutableMap);
  }

}
