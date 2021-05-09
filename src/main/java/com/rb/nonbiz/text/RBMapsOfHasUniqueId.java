package com.rb.nonbiz.text;

import static com.rb.nonbiz.text.MutableRBMapOfHasUniqueId.newMutableRBMapOfHasUniqueId;
import static com.rb.nonbiz.text.MutableRBMapOfHasUniqueId.newMutableRBMapOfHasUniqueIdWithExpectedSize;
import static com.rb.nonbiz.text.RBMapOfHasUniqueId.rbMapOfHasUniqueId;

/**
 * Utility functions dealing with {@link RBMapsOfHasUniqueId}.
 *
 * @see RBMapsOfHasUniqueId
 */
public class RBMapsOfHasUniqueId {

  public static <K extends HasUniqueId<K>, V> RBMapOfHasUniqueId<K, V> emptyRBMapOfHasUniqueId() {
    MutableRBMapOfHasUniqueId<K, V> mutableMap = newMutableRBMapOfHasUniqueId();
    return rbMapOfHasUniqueId(mutableMap);
  }

  public static <K extends HasUniqueId<K>, V> RBMapOfHasUniqueId<K, V> singletonRBMapOfHasUniqueId(
      K key, V value) {
    MutableRBMapOfHasUniqueId<K, V> mutableMap = newMutableRBMapOfHasUniqueIdWithExpectedSize(1);
    mutableMap.putAssumingAbsent(key, value);
    return rbMapOfHasUniqueId(mutableMap);
  }

  public static <K extends HasUniqueId<K>, V> RBMapOfHasUniqueId<K, V> rbMapOfHasUniqueIdOf(
      K key1, V value1,
      K key2, V value2) {
    MutableRBMapOfHasUniqueId<K, V> mutableMap = newMutableRBMapOfHasUniqueIdWithExpectedSize(2);
    mutableMap.putAssumingAbsent(key1, value1);
    mutableMap.putAssumingAbsent(key2, value2);
    return rbMapOfHasUniqueId(mutableMap);
  }

  public static <K extends HasUniqueId<K>, V> RBMapOfHasUniqueId<K, V> rbMapOfHasUniqueIdOf(
      K key1, V value1,
      K key2, V value2,
      K key3, V value3) {
    MutableRBMapOfHasUniqueId<K, V> mutableMap = newMutableRBMapOfHasUniqueIdWithExpectedSize(3);
    mutableMap.putAssumingAbsent(key1, value1);
    mutableMap.putAssumingAbsent(key2, value2);
    mutableMap.putAssumingAbsent(key3, value3);
    return rbMapOfHasUniqueId(mutableMap);
  }

  public static <K extends HasUniqueId<K>, V> RBMapOfHasUniqueId<K, V> rbMapOfHasUniqueIdOf(
      K key1, V value1,
      K key2, V value2,
      K key3, V value3,
      K key4, V value4) {
    MutableRBMapOfHasUniqueId<K, V> mutableMap = newMutableRBMapOfHasUniqueIdWithExpectedSize(4);
    mutableMap.putAssumingAbsent(key1, value1);
    mutableMap.putAssumingAbsent(key2, value2);
    mutableMap.putAssumingAbsent(key3, value3);
    mutableMap.putAssumingAbsent(key4, value4);
    return rbMapOfHasUniqueId(mutableMap);
  }

  public static <K extends HasUniqueId<K>, V> RBMapOfHasUniqueId<K, V> rbMapOfHasUniqueIdOf(
      K key1, V value1,
      K key2, V value2,
      K key3, V value3,
      K key4, V value4,
      K key5, V value5) {
    MutableRBMapOfHasUniqueId<K, V> mutableMap = newMutableRBMapOfHasUniqueIdWithExpectedSize(5);
    mutableMap.putAssumingAbsent(key1, value1);
    mutableMap.putAssumingAbsent(key2, value2);
    mutableMap.putAssumingAbsent(key3, value3);
    mutableMap.putAssumingAbsent(key4, value4);
    mutableMap.putAssumingAbsent(key5, value5);
    return rbMapOfHasUniqueId(mutableMap);
  }

}
