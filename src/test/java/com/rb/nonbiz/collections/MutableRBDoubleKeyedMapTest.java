package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.BehaviorWhenTwoDoubleKeysAreClose;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.DoubleFunction;

import static com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.BehaviorWhenTwoDoubleKeysAreClose.THROW_EXCEPTION;
import static com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.BehaviorWhenTwoDoubleKeysAreClose.USE_CEILING;
import static com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.BehaviorWhenTwoDoubleKeysAreClose.USE_FLOOR;
import static com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.BehaviorWhenTwoDoubleKeysAreClose.USE_NEAREST_OR_CEILING;
import static com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.BehaviorWhenTwoDoubleKeysAreClose.USE_NEAREST_OR_FLOOR;
import static com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.newMutableRBDoubleKeyedMap;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.treeMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.stringMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.types.Epsilon.ZERO_EPSILON;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MutableRBDoubleKeyedMapTest {

  public static <V> MutableRBDoubleKeyedMap<V> singletonMutableRBDoubleKeyedMap(
      double key1, V value1) {
    MutableRBDoubleKeyedMap<V> mutableMap = newMutableRBDoubleKeyedMap();
    mutableMap.put(key1, value1);
    return mutableMap;
  }

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
    for (BehaviorWhenTwoDoubleKeysAreClose behavior : BehaviorWhenTwoDoubleKeysAreClose.values()) {
      rbSetOf(-0.41, 0.41)
          .forEach(diff -> {
            assertIllegalArgumentException( () -> map.getOrThrow(1.0 + diff, e, behavior));
            assertIllegalArgumentException( () -> map.getOrThrow(3.0 + diff, e, behavior));
            assertIllegalArgumentException( () -> map.getOrThrow(7.0 + diff, e, behavior));

            assertOptionalEmpty(map.getOptional(1.0 + diff, e, behavior));
            assertOptionalEmpty(map.getOptional(3.0 + diff, e, behavior));
            assertOptionalEmpty(map.getOptional(7.0 + diff, e, behavior));
          });
    }
  }

  @Test
  public void testInexactLookup_choiceMustBeMade() {
    MutableRBDoubleKeyedMap<String> map = mutableRBDoubleKeyedMapOf(
        1.0, "_1",
        3.0, "_3",
        7.0, "_7");

    Epsilon e = epsilon(1.8);

    // 1.4 is 0.5 away from the 'floor key' 1.0, but 1.6 away from the 'ceiling' key 3.0, so now there are two choices.
    assertEquals("_1",                    map.getOrThrow(1.5, e, USE_FLOOR));
    assertEquals("_1",                    map.getOrThrow(1.5, e, USE_NEAREST_OR_FLOOR));
    assertEquals("_1",                    map.getOrThrow(1.5, e, USE_NEAREST_OR_CEILING));
    assertEquals("_3",                    map.getOrThrow(1.5, e, USE_CEILING));
    assertIllegalArgumentException( () -> map.getOrThrow(1.5, e, THROW_EXCEPTION));

    assertOptionalEquals("_1",            map.getOptional(1.5, e, USE_FLOOR));
    assertOptionalEquals("_1",            map.getOptional(1.5, e, USE_NEAREST_OR_FLOOR));
    assertOptionalEquals("_1",            map.getOptional(1.5, e, USE_NEAREST_OR_CEILING));
    assertOptionalEquals("_3",            map.getOptional(1.5, e, USE_CEILING));
    // Note that, even though this is not getOrThrow, we will still throw an exception, because there's more than
    // one choice to be made about what to return.
    assertIllegalArgumentException( () -> map.getOptional(1.5, e, THROW_EXCEPTION));

    // Finally, let's check the case where something is exactly in the middle. This is hard to do with doubles, but
    // thankfully the double calculation of Epsilon.valuesAreWithin ends up being exactly the same for abs(1 - 2)
    // and for abs(3 - 2).
    assertEquals("_1", map.getOrThrow(2.0, e, USE_NEAREST_OR_FLOOR));
    assertEquals("_3", map.getOrThrow(2.0, e, USE_NEAREST_OR_CEILING));
  }

  @Test
  public void testMatcher() {
    DoubleFunction<MutableRBDoubleKeyedMap<String>> maker = diff -> mutableRBDoubleKeyedMapOf(
        1.0 + diff, "_1",
        3.0 + diff, "_3",
        7.0 + diff, "_7");

    MutableRBDoubleKeyedMap<String> exactMap = maker.apply(0.0);

    rbSetOf(-0.19, 0.0, 0.19)
        .forEach(diff ->
            assertThat(
                "The map must match its exact self, plus variations of its keys within epsilon",
                exactMap,
                mutableRBDoubleKeyedMapMatcher(
                    maker.apply(diff), epsilon(0.2), f -> stringMatcher(f))));

    rbSetOf(-0.21, 0.21)
        .forEach(diff ->
            assertThat(
                "The map must NOT match its variations where its keys are tweaked past an epsilon",
                exactMap,
                not(
                    mutableRBDoubleKeyedMapMatcher(
                        maker.apply(diff), epsilon(0.2), f -> stringMatcher(f)))));
  }

  public static <V> TypeSafeMatcher<MutableRBDoubleKeyedMap<V>> mutableRBDoubleKeyedMapMatcher(
      MutableRBDoubleKeyedMap<V> expected,
      Epsilon epsilonForDoubleKeys,
      MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected,
        match(
            v -> v.getRawTreeMapUnsafe(),
            f -> treeMapMatcher(f,
                (key1, key2) -> epsilonForDoubleKeys.valuesAreWithin(key1, key2), valueMatcherGenerator)));
  }

}
