package com.rb.nonbiz.util;

import com.rb.nonbiz.collections.MutableRBEnumMap;
import com.rb.nonbiz.collections.RBEnumMap;

import static com.rb.nonbiz.collections.MutableRBEnumMap.newMutableRBEnumMap;
import static com.rb.nonbiz.collections.RBEnumMap.newRBEnumMap;

public class RBEnumMapSimpleConstructors {

  public static <E extends Enum<E>, V> RBEnumMap<E, V> emptyRBEnumMap(Class<E> enumClass) {
    return newRBEnumMap(newMutableRBEnumMap(enumClass));
  }

  // We need this because onlyEnumKey.getClass() returns a {@code Class<? extends Enum>}, not a {@code Class<E>}.
  @SuppressWarnings("unchecked")
  public static <E extends Enum<E>, V> RBEnumMap<E, V> singletonRBEnumMap(E onlyEnumKey, V onlyValue) {
    MutableRBEnumMap<E, V> mutableMap = newMutableRBEnumMap(onlyEnumKey.getClass());
    mutableMap.put(onlyEnumKey, onlyValue);
    return newRBEnumMap(mutableMap);
  }

  // We need this because onlyEnumKey.getClass() returns a {@code Class<? extends Enum>}, not a {@code Class<E>}.
  @SuppressWarnings("unchecked")
  public static <E extends Enum<E>, V> RBEnumMap<E, V> rbEnumMapOf(
      E enumKey1, V value1,
      E enumKey2, V value2) {
    MutableRBEnumMap<E, V> mutableMap = newMutableRBEnumMap(enumKey1.getClass());
    mutableMap.putAssumingAbsent(enumKey1, value1);
    mutableMap.putAssumingAbsent(enumKey2, value2);
    return newRBEnumMap(mutableMap);
  }

  // We need this because onlyEnumKey.getClass() returns a {@code Class<? extends Enum>}, not a {@code Class<E>}.
  @SuppressWarnings("unchecked")
  public static <E extends Enum<E>, V> RBEnumMap<E, V> rbEnumMapOf(
      E enumKey1, V value1,
      E enumKey2, V value2,
      E enumKey3, V value3) {
    MutableRBEnumMap<E, V> mutableMap = newMutableRBEnumMap(enumKey1.getClass());
    mutableMap.putAssumingAbsent(enumKey1, value1);
    mutableMap.putAssumingAbsent(enumKey2, value2);
    mutableMap.putAssumingAbsent(enumKey3, value3);
    return newRBEnumMap(mutableMap);
  }

  // We need this because onlyEnumKey.getClass() returns a {@code Class<? extends Enum>}, not a {@code Class<E>}.
  @SuppressWarnings("unchecked")
  public static <E extends Enum<E>, V> RBEnumMap<E, V> rbEnumMapOf(
      E enumKey1, V value1,
      E enumKey2, V value2,
      E enumKey3, V value3,
      E enumKey4, V value4) {
    MutableRBEnumMap<E, V> mutableMap = newMutableRBEnumMap(enumKey1.getClass());
    mutableMap.putAssumingAbsent(enumKey1, value1);
    mutableMap.putAssumingAbsent(enumKey2, value2);
    mutableMap.putAssumingAbsent(enumKey3, value3);
    mutableMap.putAssumingAbsent(enumKey4, value4);
    return newRBEnumMap(mutableMap);
  }

  // We need this because onlyEnumKey.getClass() returns a {@code Class<? extends Enum>}, not a {@code Class<E>}.
  @SuppressWarnings("unchecked")
  public static <E extends Enum<E>, V> RBEnumMap<E, V> rbEnumMapOf(
      E enumKey1, V value1,
      E enumKey2, V value2,
      E enumKey3, V value3,
      E enumKey4, V value4,
      E enumKey5, V value5) {
    MutableRBEnumMap<E, V> mutableMap = newMutableRBEnumMap(enumKey1.getClass());
    mutableMap.putAssumingAbsent(enumKey1, value1);
    mutableMap.putAssumingAbsent(enumKey2, value2);
    mutableMap.putAssumingAbsent(enumKey3, value3);
    mutableMap.putAssumingAbsent(enumKey4, value4);
    mutableMap.putAssumingAbsent(enumKey5, value5);
    return newRBEnumMap(mutableMap);
  }

}
