package com.rb.nonbiz.collections;

import java.util.Comparator;
import java.util.LinkedHashMap;

import static com.google.common.collect.Maps.newLinkedHashMapWithExpectedSize;

/**
 * Various helper methods related to {@link LinkedHashMap}, which is currently (July 2022) not widely used.
 */
public class LinkedHashMaps {

  public static <K, V> LinkedHashMap<K, V> toSortedLinkedHashMap(RBMap<K, V> map, Comparator<K> keysComparator) {
    LinkedHashMap<K, V> linkedHashMap = newLinkedHashMapWithExpectedSize(map.size());
    map.forEachSortedEntry(
        (entry1, entry2) -> keysComparator.compare(entry1.getKey(), entry2.getKey()),
        (key, value) -> linkedHashMap.put(key, value));
    return linkedHashMap;
  }

}
