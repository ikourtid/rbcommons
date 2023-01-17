package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.BehaviorWhenTwoDoubleKeysAreClose;
import com.rb.nonbiz.types.Epsilon;
import org.junit.Test;

import static com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.newMutableRBDoubleKeyedMap;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.types.Epsilon.ZERO_EPSILON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MutableRBDoubleKeyedMapTest {

  public static <V> MutableRBDoubleKeyedMap<V> mutableRBDoubleKeyedMapOf(
      double key1, V value1,
      double key2, V value2) {
    MutableRBDoubleKeyedMap<V> mutableMap = newMutableRBDoubleKeyedMap();
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    return mutableMap;
  }

  public static <V> MutableRBDoubleKeyedMap<V> mutableRBDoubleKeyedMapOf(
      double key1, V value1,
      double key2, V value2,
      double key3, V value3) {
    MutableRBDoubleKeyedMap<V> mutableMap = newMutableRBDoubleKeyedMap();
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    mutableMap.put(key3, value3);
    return mutableMap;
  }

  public static <V> MutableRBDoubleKeyedMap<V> mutableRBDoubleKeyedMapOf(
      double key1, V value1,
      double key2, V value2,
      double key3, V value3,
      double key4, V value4) {
    MutableRBDoubleKeyedMap<V> mutableMap = newMutableRBDoubleKeyedMap();
    mutableMap.put(key1, value1);
    mutableMap.put(key2, value2);
    mutableMap.put(key3, value3);
    mutableMap.put(key4, value4);
    return mutableMap;
  }

  public static <V> MutableRBDoubleKeyedMap<V> mutableRBDoubleKeyedMapOf(
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
    return mutableMap;
  }

  @Test
  public void testGetOptional_allCases() {
    MutableRBDoubleKeyedMap<String> map = mutableRBDoubleKeyedMapOf(
        1.0, "_1",
        3.0, "_3",
        7.0, "_7");

    // Simple getters for the exact values
    for (BehaviorWhenTwoDoubleKeysAreClose behavior : BehaviorWhenTwoDoubleKeysAreClose.values()) {
      assertEquals("_1", map.getOrThrow(1.0, ZERO_EPSILON, behavior));
      assertEquals("_3", map.getOrThrow(3.0, ZERO_EPSILON, behavior));
      assertEquals("_7", map.getOrThrow(7.0, ZERO_EPSILON, behavior));

      assertOptionalEquals("_1", map.getOptional(1.0, ZERO_EPSILON, behavior));
      assertOptionalEquals("_3", map.getOptional(3.0, ZERO_EPSILON, behavior));
      assertOptionalEquals("_7", map.getOptional(7.0, ZERO_EPSILON, behavior));
    }

  }

}
