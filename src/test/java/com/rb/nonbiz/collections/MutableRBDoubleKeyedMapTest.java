package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.BehaviorWhenTwoDoubleKeysAreClose;
import com.rb.nonbiz.types.Epsilon;
import org.junit.Test;

import static com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.newMutableRBDoubleKeyedMap;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.types.Epsilon.ZERO_EPSILON;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static org.junit.Assert.assertEquals;

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
  public void testExactLookup() {
    MutableRBDoubleKeyedMap<String> map = mutableRBDoubleKeyedMapOf(
        1.0, "_1",
        3.0, "_3",
        7.0, "_7");

    for (BehaviorWhenTwoDoubleKeysAreClose behavior : BehaviorWhenTwoDoubleKeysAreClose.values()) {
      assertEquals("_1", map.getOrThrow(1.0, ZERO_EPSILON, behavior));
      assertEquals("_3", map.getOrThrow(3.0, ZERO_EPSILON, behavior));
      assertEquals("_7", map.getOrThrow(7.0, ZERO_EPSILON, behavior));

      assertOptionalEquals("_1", map.getOptional(1.0, ZERO_EPSILON, behavior));
      assertOptionalEquals("_3", map.getOptional(3.0, ZERO_EPSILON, behavior));
      assertOptionalEquals("_7", map.getOptional(7.0, ZERO_EPSILON, behavior));
    }
  }

  @Test
  public void testInexactLookup_noChoiceToBeMade() {
    MutableRBDoubleKeyedMap<String> map = mutableRBDoubleKeyedMapOf(
        1.0, "_1",
        3.0, "_3",
        7.0, "_7");

    Epsilon e = epsilon(0.4);

    // When the request is for a key that's either exactly the same as an existing key, OR one that's near
    // (within epsilon), and there's never a choice to be made (where two existing keys are within epsilon of the
    // key requested), return the value of the exact key.
    for (BehaviorWhenTwoDoubleKeysAreClose behavior : BehaviorWhenTwoDoubleKeysAreClose.values()) {
      rbSetOf(-0.39, 0.0, 0.39)
          .forEach(diff -> {
            assertEquals("_1", map.getOrThrow(1.0 + diff, e, behavior));
            assertEquals("_3", map.getOrThrow(3.0 + diff, e, behavior));
            assertEquals("_7", map.getOrThrow(7.0 + diff, e, behavior));

            assertOptionalEquals("_1", map.getOptional(1.0 + diff, e, behavior));
            assertOptionalEquals("_3", map.getOptional(3.0 + diff, e, behavior));
            assertOptionalEquals("_7", map.getOptional(7.0 + diff, e, behavior));
          });
    }
  }

}
