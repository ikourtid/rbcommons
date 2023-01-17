package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.types.Epsilon;
import org.junit.Test;

import static com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.newMutableRBDoubleKeyedMap;
import static com.rb.nonbiz.collections.MutableRBDoubleKeyedMapTest.mutableRBDoubleKeyedMapMatcher;
import static com.rb.nonbiz.collections.RBDoubleKeyedMap.newRBDoubleKeyedMap;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class RBDoubleKeyedMapTest {

  public static <V> RBDoubleKeyedMap<V> rbDoubleKeyedMapOf(
      double key1, V value1,
      double key2, V value2) {
    MutableRBDoubleKeyedMap<V> mutableMap = newMutableRBDoubleKeyedMap();
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    return newRBDoubleKeyedMap(mutableMap);
  }

  public static <V> RBDoubleKeyedMap<V> rbDoubleKeyedMapOf(
      double key1, V value1,
      double key2, V value2,
      double key3, V value3) {
    MutableRBDoubleKeyedMap<V> mutableMap = newMutableRBDoubleKeyedMap();
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    mutableMap.put(key3, value3);
    return newRBDoubleKeyedMap(mutableMap);
  }

  public static <V> RBDoubleKeyedMap<V> rbDoubleKeyedMapOf(
      double key1, V value1,
      double key2, V value2,
      double key3, V value3,
      double key4, V value4) {
    MutableRBDoubleKeyedMap<V> mutableMap = newMutableRBDoubleKeyedMap();
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    mutableMap.put(key3, value3);
    mutableMap.put(key4, value4);
    return newRBDoubleKeyedMap(mutableMap);
  }

  public static <V> RBDoubleKeyedMap<V> rbDoubleKeyedMapOf(
      double key1, V value1,
      double key2, V value2,
      double key3, V value3,
      double key4, V value4,
      double key5, V value5) {
    MutableRBDoubleKeyedMap<V> mutableMap = newMutableRBDoubleKeyedMap();
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    mutableMap.put(key3, value3);
    mutableMap.put(key4, value4);
    mutableMap.put(key5, value5);
    return newRBDoubleKeyedMap(mutableMap);
  }

  @Test
  public void reminder() {
    fail("");
  }

  public static <V> TypeSafeMatcher<RBDoubleKeyedMap<V>> rbDoubleKeyedMapMatcher(
      RBDoubleKeyedMap<V> expected,
      Epsilon epsilonForDoubleKeys,
      MatcherGenerator<V> matcherGenerator) {
    return makeMatcher(expected,
        match(
            v -> v.getRawMutableMapUnsafe(),
            f -> mutableRBDoubleKeyedMapMatcher(f, epsilonForDoubleKeys, matcherGenerator)));
  }

}
