package com.rb.nonbiz.util;

import com.google.common.collect.ImmutableMap;

import java.util.EnumMap;

public class RBEnumMapSimpleConstructors {

  public static <E extends Enum<E>, V> EnumMap<E, V> singletonEnumMap(E onlyEnumKey, V onlyValue) {
    return new EnumMap<>(ImmutableMap.of(onlyEnumKey, onlyValue));
  }

  public static <E extends Enum<E>, V> EnumMap<E, V> enumMapOf(
      E enumKey1, V value1,
      E enumKey2, V value2) {
    return new EnumMap<>(ImmutableMap.of(
        enumKey1, value1,
        enumKey2, value2));
  }

  public static <E extends Enum<E>, V> EnumMap<E, V> enumMapOf(
      E enumKey1, V value1,
      E enumKey2, V value2,
      E enumKey3, V value3) {
    return new EnumMap<>(ImmutableMap.of(
        enumKey1, value1,
        enumKey2, value2,
        enumKey3, value3));
  }

  public static <E extends Enum<E>, V> EnumMap<E, V> enumMapOf(
      E enumKey1, V value1,
      E enumKey2, V value2,
      E enumKey3, V value3,
      E enumKey4, V value4) {
    return new EnumMap<>(ImmutableMap.of(
        enumKey1, value1,
        enumKey2, value2,
        enumKey3, value3,
        enumKey4, value4));
  }

  public static <E extends Enum<E>, V> EnumMap<E, V> enumMapOf(
      E enumKey1, V value1,
      E enumKey2, V value2,
      E enumKey3, V value3,
      E enumKey4, V value4,
      E enumKey5, V value5) {
    return new EnumMap<>(ImmutableMap.of(
        enumKey1, value1,
        enumKey2, value2,
        enumKey3, value3,
        enumKey4, value4,
        enumKey5, value5));
  }

}
