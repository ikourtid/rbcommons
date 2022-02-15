package com.rb.nonbiz.collections;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.LongCounter;
import com.rb.nonbiz.types.Pointer;
import com.rb.nonbiz.util.RBPreconditions;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSetWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapMergers.mergeRBMapsDisallowingOverlap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.biMapEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertThrows;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.types.LongCounter.longCounter;
import static com.rb.nonbiz.types.Pointer.initializedPointer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RBMapTest {

  @Test
  public void testContainsOnlyKey() {
    assertFalse(emptyRBMap().containsOnlyKey("a"));
    assertTrue(singletonRBMap("a", 0.123).containsOnlyKey("a"));
    assertFalse(singletonRBMap("b", 0.123).containsOnlyKey("a"));
    assertFalse(
        rbMapOf(
            "a", 0.123,
            "b", 0.456)
            .containsOnlyKey("a"));
    assertFalse(
        rbMapOf(
            "c", 0.123,
            "b", 0.456)
            .containsOnlyKey("a"));
  }

  @Test
  public void implementsEquals() {
    assertEquals(emptyRBMap(), emptyRBMap());
    assertEquals(singletonRBMap("a", 1), singletonRBMap("a", 1));
    assertEquals(
        rbMapOf(
            "a", 1,
            "b", 2),
        rbMapOf(
            "a", 1,
            "b", 2));
  }

  @Test
  public void keysAppearMultipleTimes_throws() {
    assertIllegalArgumentException( () -> rbMapOf("a", 1, "a", 1));
  }

  @Test
  public void disallowsNullKeys() {
    assertThrows(NullPointerException.class, () -> rbMapOf(
        "a", 1,
        null, 2));
  }

  @Test
  public void disallowsNullValues() {
    assertThrows(NullPointerException.class,  () -> singletonRBMap(
        "a", null));
  }

  @Test
  public void get_disallowsNullKeys() {
    RBMap<String, Integer> map = singletonRBMap("a", 1);
    assertIllegalArgumentException( () -> map.getOptional(null));
    assertIllegalArgumentException( () -> map.getOrDefault(null, 1));
    assertIllegalArgumentException( () -> map.getOrDefault(null, () -> 1));
  }

  @Test
  public void testContains() {
    RBMap<String, Integer> map = singletonRBMap("a", 1);
    assertTrue( map.containsKey("a"));
    assertFalse(map.containsKey("b"));

    assertTrue( map.containsValue(1));
    assertFalse(map.containsValue(2));
  }

  @Test
  public void testIsEmpty() {
    assertTrue(emptyRBMap().isEmpty());
    assertFalse(singletonRBMap("a", 1).isEmpty());
  }

  @Test
  public void testGetOrDefault() {
    RBMap<String, Integer> map = singletonRBMap("a", 1);
    assertEquals(1, map.getOrDefault("a", 123).intValue());
    assertEquals(123, map.getOrDefault("b", 123).intValue());
    assertEquals(123, map.getOrDefault("b", () -> 123).intValue());
    // The exception does not get thrown, since the supplier is not evaluated
    assertEquals(1, map.getOrDefault("a", () -> { throw new IllegalArgumentException(); }).intValue());
    assertIllegalArgumentException( () -> map.getOrDefault("b", () -> { throw new IllegalArgumentException(); }));
  }

  @Test
  public void testToString() {
    assertTrue(
        "The order a map gets iterated over is not guaranteed",
        rbSetOf("{a=1, b=2}", "{b=2, a=1}").contains(
            rbMapOf("a", 1, "b", 2).toString()));
  }

  @Test
  public void testMergeRBMapsByValue() {
    assertEquals(
        rbMapOf(
            "a", 1 + 30,
            "b", 2,
            "c", 40),
        RBMapMergers.mergeRBMapsByValue(
            (v1, v2) -> v1 + v2, rbMapOf(
                "a", 1,
                "b", 2),
            rbMapOf(
                "a", 30,
                "c", 40)
        ));
  }

  @Test
  public void testMergedRBMapsByValue_emptyCase() {
    assertEquals(
        emptyRBMap(),
        RBMapMergers.mergeRBMapsByValue(
            (v1, v2) -> null, emptyRBMap(),
            emptyRBMap()
        )); // return value of BiFunction doesn't matter as it doesn't get used
  }

  @Test
  public void transformValuesCopy() {
    RBMap<String, Integer> emptyMap = emptyRBMap();
    assertEquals(
        emptyMap,
        emptyMap.transformValuesCopy(intValue -> intValue + 10));
    assertEquals(
        rbMapOf("a", 11, "b", 12),
        rbMapOf("a",  1, "b",  2).transformValuesCopy(intValue -> intValue + 10));
  }

  @Test
  public void testFilterKeys() {
    RBMap<String, Integer> emptyMap = emptyRBMap();
    assertEquals(
        emptyMap,
        emptyMap.filterKeys(k -> k.equals("a")));
    assertEquals(
        emptyMap,
        singletonRBMap("b", 2).filterKeys(k -> k.equals("a")));
    assertEquals(
        singletonRBMap("a", 1),
        rbMapOf("a", 1, "b", 2).filterKeys(k -> k.equals("a")));
  }

  @Test
  public void testFilterValues() {
    RBMap<String, Integer> emptyMap = emptyRBMap();
    assertEquals(
        emptyMap,
        emptyMap.filterValues(v -> v.equals(1)));
    assertEquals(
        emptyMap,
        singletonRBMap("b", 2).filterValues(v -> v == 1));
    assertEquals(
        singletonRBMap("a", 1),
        rbMapOf("a", 1, "b", 2).filterValues(v -> v == 1));
  }

  @Test
  public void testFilterEntries() {
    RBMap<String, Integer> emptyMap = emptyRBMap();
    BiPredicate<String, Integer> biPredicate = (key, value) -> "a1".equals(key + value);
    assertEquals(
        emptyMap,
        emptyMap.filterEntries(biPredicate));
    assertEquals(
        emptyRBMap(),
        singletonRBMap("b", 2)
            .filterEntries(biPredicate));
    assertEquals(
        singletonRBMap("a", 1),
        rbMapOf("a", 1, "b", 2)
            .filterEntries(biPredicate));
    assertEquals(
        emptyMap,
        rbMapOf("a", 2, "b", 2)
            .filterEntries(biPredicate));
    assertEquals(
        emptyMap,
        rbMapOf("c", 1, "b", 2)
            .filterEntries(biPredicate));
  }

  @Test
  public void testFilterKeysAndTransformValuesCopy() {
    RBMap<String, Integer> emptyMap = emptyRBMap();
    assertEquals(
        emptyMap,
        emptyMap.filterKeysAndTransformValuesCopy(intValue -> intValue + 10, k -> true));
    assertEquals(
        emptyMap,
        emptyMap.filterKeysAndTransformValuesCopy(intValue -> intValue + 10, k -> false));
    assertEquals(
        singletonRBMap("a", intExplained(11, 1 + 10)),
        rbMapOf("a",  1, "b",  2)
            .filterKeysAndTransformValuesCopy(intValue -> intValue + 10, k -> k.equals("a")));
    assertEquals(
        emptyRBMap(),
        rbMapOf("a",  1, "b",  2)
            .filterKeysAndTransformValuesCopy(intValue -> intValue + 10, k -> k.equals("xyz")));
  }

  @Test
  public void testFilterKeysAndTransformEntriesCopy() {
    TriConsumer<RBMap<Integer, Integer>, Predicate<Integer>, RBMap<Integer, String>> asserter =
        (inputMap, mustKeepKey, outputMap) ->
            assertEquals(
                outputMap,
                inputMap.filterKeysAndTransformEntriesCopy( (intKey, intValue) -> intKey + "_" + intValue, mustKeepKey));
    asserter.accept(emptyRBMap(), key -> true, emptyRBMap());
    asserter.accept(
        rbMapOf(
            10, 700,
            11, 701),
        key -> true,
        rbMapOf(
            10, "10_700",
            11, "11_701"));
    asserter.accept(
        rbMapOf(
            10, 700,
            11, 701),
        key -> key == 10,
        singletonRBMap(
            10, "10_700"));
  }

  // filter values, then transform on the filtered values
  @Test
  public void testFiterAndTransformValuesCopy() {
    RBMap<String, Integer> emptyMap = emptyRBMap();
    assertEquals(
        emptyMap,
        emptyMap.filterValuesAndTransformValuesCopy(intValue -> intValue + 10, v1 -> true));
    assertEquals(
        emptyMap,
        emptyMap.filterValuesAndTransformValuesCopy(intValue -> intValue + 10, v1 -> false));
    assertEquals(
        rbMapOf(
            "a", intExplained(11, 1 + 10),
            "b", intExplained(12, 2 + 10)),
        rbMapOf("a",  1, "b",  2)
            .filterValuesAndTransformValuesCopy(intValue -> intValue + 10, v1 -> v1 > 0));
    assertEquals(
        singletonRBMap("a", intExplained(11, 1 + 10)),
        rbMapOf("a",  1, "b",  2)
            .filterValuesAndTransformValuesCopy(intValue -> intValue + 10, v1 -> v1 == 1));
    assertEquals(
        emptyRBMap(),
        rbMapOf("a",  1, "b",  2)
            .filterValuesAndTransformValuesCopy(intValue -> intValue + 10, v1 -> v1 > 10));
  }

  // transform values, then filter on transformed values
  @Test
  public void testTransformAndFiterValuesCopy() {
    RBMap<String, Integer> emptyMap = emptyRBMap();
    assertEquals(
        emptyMap,
        emptyMap.transformAndFilterValuesCopy(intValue -> intValue + 10, v1 -> true));
    assertEquals(
        emptyMap,
        emptyMap.transformAndFilterValuesCopy(intValue -> intValue + 10, v1 -> false));
    assertEquals(
        rbMapOf(
            "a", intExplained(11, 1 + 10),
            "b", intExplained(12, 2 + 10)),
        rbMapOf("a",  1, "b",  2)
            .transformAndFilterValuesCopy(intValue -> intValue + 10, v1 -> v1 > 0));
    assertEquals(
        singletonRBMap("a", intExplained(11, 1 + 10)),
        rbMapOf("a",  1, "b",  2)
            .transformAndFilterValuesCopy(intValue -> intValue + 10, v1 -> v1 == 11));
    assertEquals(
        emptyRBMap(),
        rbMapOf("a",  1, "b",  2)
            .transformAndFilterValuesCopy(intValue -> intValue + 10, v1 -> v1 > 20));
  }

  @Test
  public void transformEntriesCopy() {
    BiFunction<String, Integer, String> entryTransformer = (key, value) -> Strings.format("%s_%s", key, value);
    RBMap<String, Integer> emptyMap = emptyRBMap();
    assertEquals(
        emptyRBMap(),
        emptyMap.transformEntriesCopy(entryTransformer));
    assertEquals(
        rbMapOf("a", "a_1", "b", "b_2"),
        rbMapOf("a", 1, "b", 2).transformEntriesCopy(entryTransformer));
  }

  @Test
  public void testOrderedTransformEntriesCopy_behavesLikeSimpleTransformEntriesCopyIfNoSideEffects() {
    BiFunction<String, Integer, String> entryTransformer = (key, value) -> Strings.format("%s_%s", key, value);
    RBMap<String, Integer> emptyMap = emptyRBMap();
    assertEquals(
        emptyRBMap(),
        emptyMap.orderedTransformEntriesCopy(entryTransformer, String::compareTo));
    assertEquals(
        rbMapOf("a", "a_1", "b", "b_2"),
        rbMapOf("a", 1, "b", 2).orderedTransformEntriesCopy(entryTransformer, String::compareTo));
  }

  @Test
  public void testOrderedTransformEntriesCopy_iteratesWithDeterministicOrder() {
    RBMap<String, Integer> original = rbMapOf(
        "a", 1,
        "b", 2,
        "c", 3,
        "d", 4,
        "e", 5);
    RBMap<String, String> transformed = rbMapOf(
        "a", "a_1",
        "b", "b_2",
        "c", "c_3",
        "d", "d_4",
        "e", "e_5");
    Comparator<String> stringComparator = String::compareTo;
    {
      List<String> orderedKeys = newArrayList();
      assertEquals(
          transformed,
          original.orderedTransformEntriesCopy( (key, value) -> {
                orderedKeys.add(key); // intentional side effect
                return Strings.format("%s_%s", key, value);
              },
              stringComparator));
      assertThat(
          orderedKeys,
          orderedListMatcher(ImmutableList.of("a", "b", "c", "d", "e"), s -> typeSafeEqualTo(s)));
    }
    {
      List<String> orderedKeys = newArrayList();
      assertEquals(
          transformed,
          original.orderedTransformEntriesCopy( (key, value) -> {
                orderedKeys.add(key); // intentional side effect
                return Strings.format("%s_%s", key, value);
              },
              stringComparator.reversed()));
      assertThat(
          orderedKeys,
          orderedListMatcher(ImmutableList.of("e", "d", "c", "b", "a"), s -> typeSafeEqualTo(s)));
    }
  }

  @Test
  public void testOrderedTransformValuesCopy_iteratesWithDeterministicOrder() {
    RBMap<String, Integer> original = rbMapOf(
        "a", 1,
        "b", 2,
        "c", 3,
        "d", 4,
        "e", 5);
    RBMap<String, Integer> transformed = rbMapOf(
        "a", 101,
        "b", 102,
        "c", 103,
        "d", 104,
        "e", 105);
    Comparator<String> stringComparator = String::compareTo;
    {
      List<Integer> orderedValues = newArrayList();
      assertEquals(
          transformed,
          original.orderedTransformValuesCopy(
              value -> {
                orderedValues.add(value); // intentional side effect
                return value + 100;
              },
              stringComparator));
      assertThat(
          orderedValues,
          orderedListMatcher(ImmutableList.of(1, 2, 3, 4, 5), s -> typeSafeEqualTo(s)));
    }
    {
      List<Integer> orderedValues = newArrayList();
      assertEquals(
          transformed,
          original.orderedTransformValuesCopy(
              value -> {
                orderedValues.add(value); // intentional side effect
                return value + 100;
              },
              stringComparator.reversed()));
      assertThat(
          orderedValues,
          orderedListMatcher(ImmutableList.of(5, 4, 3, 2, 1), s -> typeSafeEqualTo(s)));
    }
  }

  @Test
  public void testRandomlyOrderedTransformValuesCopy_iteratesWithDeterministicOrder() {
    RBMap<String, Integer> original = rbMapOf(
        "a", 1,
        "b", 2,
        "c", 3,
        "d", 4,
        "e", 5);
    RBMap<String, Integer> transformed = rbMapOf(
        "a", 11,
        "b", 12,
        "c", 13,
        "d", 14,
        "e", 15);

    MutableRBSet<Long> uniqueValueOrderingsEncountered = newMutableRBSetWithExpectedSize(100);
    long currentTimeMillis = System.currentTimeMillis();

    // It's hard to test something that has randomness in it. But here's a reasonable way.
    // With a map of size 5, there are 5 factorial = 120 permutations.
    // If we do e.g. 100 transformations in random order, we would expect to have at least e.g. 20 unique permutations
    // show up.
    Supplier<List<Integer>> run100 = () -> {
      List<Integer> valueOrderingsEncountered = newArrayListWithExpectedSize(100);
      Random random = new Random(currentTimeMillis);
      for (int i = 0; i < 100; i++) {
        Pointer<Long> longPointer = initializedPointer(0L);
        assertEquals(
            transformed,
            original.randomlyOrderedTransformValuesCopy(
                value -> {
                  longPointer.set(100 * longPointer.getOrThrow() + value); // intentional side effect
                  valueOrderingsEncountered.add(value);
                  return value + 10;
                },
                random));
        // For example, if the order of keys happens to be abcde, then this will be 1112131415.
        uniqueValueOrderingsEncountered.add(longPointer.getOrThrow());
      }
      return valueOrderingsEncountered;
    };

    List<Integer> valuesFromRun1 = run100.get(); // run and save result

    // I ran this several times and I saw values around 70, and no value below 60.
    // But let's use 40 to be safe. This is saying that we'll see at least 40 unique permutations out of the 120.
    assertThat(
        uniqueValueOrderingsEncountered.size(),
        greaterThan(40));

    List<Integer> valuesFromRun2 = run100.get(); // run and save result

    // We had a bug where the random number generator wasn't getting used. If so, the probability that the two
    // lists below are the same is minuscule. So this test is guaranteed to pass if we are indeed using the RNG,
    // and has a teeny chance of passing if not.
    assertEquals(
        "Since we ran with a random number generated with the same seed, the results should be the same",
        valuesFromRun1,
        valuesFromRun2);
  }

  @Test
  public void testTransformEntriesFilterValues() {
    BiFunction<String, Integer, String> entryTransformer = (key, value) -> Strings.format("%s_%s", key, value);
    RBMap<String, Integer> emptyMap = emptyRBMap();
    Predicate<String> mustKeepValuesWithA = value -> value.contains("a");
    assertEquals(
        emptyRBMap(),
        emptyMap.transformEntriesAndFilterValuesCopy(entryTransformer, mustKeepValuesWithA));
    assertEquals(
        emptyRBMap(),
        rbMapOf("b", 2, "c", 3).transformEntriesAndFilterValuesCopy(entryTransformer, mustKeepValuesWithA));
    assertEquals(
        singletonRBMap("a", "a_1"),
        rbMapOf("a", 1, "b", 2).transformEntriesAndFilterValuesCopy(entryTransformer, mustKeepValuesWithA));
    assertEquals(
        rbMapOf("a", "a_1", "ab", "ab_12"),
        rbMapOf("a", 1, "ab", 12).transformEntriesAndFilterValuesCopy(entryTransformer, mustKeepValuesWithA));
  }

  @Test
  public void testTransformEntriesToOptionalAndKeepIfPresnet() {
    BiFunction<String, Integer, Optional<String>> entryTransformer = (key, value) -> key.equals("a") || value == 2
        ? Optional.of(Strings.format("%s_%s", key, value))
        : Optional.empty();
    RBMap<String, Integer> emptyMap = emptyRBMap();
    assertEquals(
        emptyRBMap(),
        emptyMap.transformEntriesToOptionalAndKeepIfPresentCopy(entryTransformer));
    assertEquals(
        rbMapOf(
            "a", "a_1",
            "b", "b_2"),
        rbMapOf(
            "a", 1,
            "b", 2,
            "c", 3).transformEntriesToOptionalAndKeepIfPresentCopy(entryTransformer));
    assertEquals(
        singletonRBMap("a", "a_1"),
        rbMapOf(
            "a", 1,
            "c", 3).transformEntriesToOptionalAndKeepIfPresentCopy(entryTransformer));
  }

  @Test
  public void transformKeysCopy() {
    RBMap<String, Integer> emptyMap = emptyRBMap();
    assertEquals(
        emptyMap,
        emptyMap.transformKeysCopy(key -> "x" + key));
    assertEquals(
        rbMapOf("xa", 11, "xb", 12),
        rbMapOf( "a", 11,  "b", 12).transformKeysCopy(key -> "x" + key));
  }

  @Test
  public void transformKeysAndValuesCopy() {
    RBMap<String, Integer> emptyMap = emptyRBMap();
    assertEquals(
        emptyMap,
        emptyMap.transformKeysAndValuesCopy(key -> "x" + key, intValue -> intValue + 10));
    assertEquals(
        rbMapOf("xa", 11, "xb", 12),
        rbMapOf( "a",  1,  "b",  2).transformKeysAndValuesCopy(key -> "x" + key, intValue -> intValue + 10));
  }

  @Test
  public void transformKeysAndValuesCopy_testOverloadWithBiFunction() {
    RBMap<String, Integer> emptyMap = emptyRBMap();

    BiFunction<String, Integer, String> transformedKeyAndIntValueTransformer = (transformedKey, intValue) ->
        Strings.format("%s_%s", transformedKey, intValue + 10);
    assertEquals(
        emptyRBMap(),
        emptyMap.transformKeysAndValuesCopy(key -> "x" + key, transformedKeyAndIntValueTransformer));
    assertEquals(
        rbMapOf(
            "xa", "xa_11",
            "xb", "xb_12"),
        rbMapOf(
            "a", 1,
            "b", 2).transformKeysAndValuesCopy(key -> "x" + key, transformedKeyAndIntValueTransformer));
  }

  @Test
  public void orderedTransformKeysAndValuesCopy() {
    RBMap<String, Integer> emptyMap = emptyRBMap();
    assertEquals(
        emptyMap,
        emptyMap.orderedTransformKeysAndValuesCopy(
            key -> "x" + key,
            intValue -> intValue + 10,
            Comparator.naturalOrder()));

    // check if the transformed map is transvered in order
    LongCounter previousValue = longCounter();
    assertEquals(
        rbMapOf(
            "xa", 11,
            "xb", 12,
            "xc", 13,
            "xd", 14,
            "xe", 15),
        rbMapOf(
            "e", 5,  // list elements "out of order" to make sure the ordering has an effect
            "d", 4,
            "c", 3,
            "b", 2,
            "a", 1).orderedTransformKeysAndValuesCopy(
            key -> "x" + key,
            intValue -> {
              RBPreconditions.checkArgument(
                  intValue >= previousValue.get(),
                  "currentValue %s should be greater than or equal to previous value %s",
                  intValue, previousValue.get());
              previousValue.increment();
              return intValue + 10;
            },
            Comparator.naturalOrder()));
  }

  @Test
  public void testMergeRBMapsExpectingNoOverlap() {
    assertEquals(
        emptyRBMap(),
        mergeRBMapsDisallowingOverlap(emptyRBMap(), emptyRBMap()));
    assertEquals(
        rbMapOf(
            "a", 1,
            "b", 2,
            "c", 3,
            "d", 4),
        mergeRBMapsDisallowingOverlap(
            rbMapOf(
                "a", 1,
                "b", 2),
            rbMapOf(
                "c", 3,
                "d", 4)));
    assertIllegalArgumentException( () ->
        mergeRBMapsDisallowingOverlap(singletonRBMap("a", 1), singletonRBMap("a", 1)));
    assertIllegalArgumentException( () ->
        mergeRBMapsDisallowingOverlap(singletonRBMap("a", 1), singletonRBMap("a", 2)));
  }

  @Test
  public void testCopyWithOverridesApplied() {
    RBMap<String, Integer> originalMap = rbMapOf("a", 1, "b", 2, "c", 3);

    // 'd' does not exist in the original map
    assertIllegalArgumentException( () -> originalMap.copyWithOverridesApplied(singletonRBMap("d", 789)));
    assertIllegalArgumentException( () -> originalMap.copyWithOverridesApplied(rbMapOf("a", 1, "d", 789)));
    assertIllegalArgumentException( () -> originalMap.copyWithOverridesApplied(rbMapOf("a", 987, "d", 789)));

    assertEquals(rbMapOf("a", 71, "b", 72, "c", 3), originalMap.copyWithOverridesApplied(rbMapOf("a", 71, "b", 72)));
    assertEquals(rbMapOf("a", 71, "b",  2, "c", 3), originalMap.copyWithOverridesApplied(singletonRBMap("a", 71)));
    assertEquals(rbMapOf("a",  1, "b",  2, "c", 3), originalMap.copyWithOverridesApplied(emptyRBMap()));
  }

  @Test
  public void testCopyWithKeysRemoved() {
    assertIllegalArgumentException( () -> singletonRBMap("a", 1) .copyWithKeysRemoved(singletonRBSet("b")));
    assertIllegalArgumentException( () -> emptyRBMap()           .copyWithKeysRemoved(singletonRBSet("b")));
    assertIllegalArgumentException( () -> rbMapOf("a", 1, "b", 2).copyWithKeysRemoved(rbSetOf("b", "c")));

    assertEquals(emptyRBMap(),            singletonRBMap("a", 1) .copyWithKeysRemoved(singletonRBSet("a")));
    assertEquals(singletonRBMap("a", 1),  singletonRBMap("a", 1) .copyWithKeysRemoved(emptyRBSet()));
    assertEquals(rbMapOf("a", 1, "b", 2), rbMapOf("a", 1, "b", 2).copyWithKeysRemoved(emptyRBSet()));
    assertEquals(singletonRBMap("a", 1),  rbMapOf("a", 1, "b", 2).copyWithKeysRemoved(singletonRBSet("b")));
    assertEquals(singletonRBMap("b", 2),  rbMapOf("a", 1, "b", 2).copyWithKeysRemoved(singletonRBSet("a")));
    assertEquals(emptyRBMap(),            rbMapOf("a", 1, "b", 2).copyWithKeysRemoved(rbSetOf("a", "b")));
  }

  @Test
  public void itemAdded() {
    assertEquals(singletonRBMap("a", 1), emptyRBMap().withItemAddedAssumingAbsent("a", 1));
    assertEquals(rbMapOf("a", 1, "b", 2), singletonRBMap("a", 1).withItemAddedAssumingAbsent("b", 2));
    assertIllegalArgumentException( () -> singletonRBMap("a", 1).withItemAddedAssumingAbsent("a", 1));
    assertIllegalArgumentException( () -> singletonRBMap("a", 1).withItemAddedAssumingAbsent("a", 2));
  }

  @Test
  public void testAllowingNull() {
    MutableRBMap<String, Long> mutableMap = newMutableRBMap();
    mutableMap.putAssumingAbsentAllowingNullValue("a", 1L);
    mutableMap.putAssumingAbsentAllowingNullValue("b", null);
    RBMap<String, Long> rbMap = newRBMap(mutableMap);

    assertEquals(1L, rbMap.getOrThrowAllowingNull("a").longValue());
    assertEquals(1L, rbMap.getOrThrowAllowingNull("a", "message").longValue());
    assertNull(rbMap.getOrThrowAllowingNull("b"));
    assertNull(rbMap.getOrThrowAllowingNull("b", "message"));

    assertEquals(1L, rbMap.getOrThrow("a").longValue());
    assertEquals(1L, rbMap.getOrThrow("a", "message").longValue());
    assertIllegalArgumentException( () -> rbMap.getOrThrow("b")); // not allowing null
    assertIllegalArgumentException( () -> rbMap.getOrThrow("b", "message")); // not allowing null
  }

  @Test
  public void testToBiMap_mapIsInvertible_returnsBiMap() {
    assertThat(
        emptyRBMap().toBiMap(),
        biMapEqualityMatcher(
            ImmutableBiMap.builder().build()));
    assertThat(
        rbMapOf(
            "a", 1,
            "b", 2)
            .toBiMap(),
        biMapEqualityMatcher(
            ImmutableBiMap.<String, Integer>builder()
                .put("a", 1)
                .put("b", 2)
                .build()));
  }

  @Test
  public void testToBiMap_mapIsNotInvertible_throws() {
    assertIllegalArgumentException( () ->
        rbMapOf(
            "a", 1,
            "b", 2,
            "c", 1)
            .toBiMap());
  }

  @Test
  public void testToBiMap_mapIsInvertibleButBadIdeaWithDoubles_doesNotThrow() {
    BiMap<String, Double> doesNotThrow = rbMapOf(
        "a", 1.1,
        "b", 2.2,
        "c", 1.1 + 1e-14)
        .toBiMap();
    // Even though the following two work, they're not guaranteed to, so I'm not leaving them in.
    //    assertTrue(biMap.inverse().containsKey(1.1 + 1e-14));
    //    assertTrue(biMap.inverse().containsKey(1.1 + 7 * 1e-14 - 6 * 1e-14));
  }

  @Test
  public void testToRBSet() {
    BiConsumer<RBMap<String, Integer>, RBSet<String>> asserter = (map, expectedSet) ->
    assertThat(
        map.toRBSet( (stringKey, intValue) -> Strings.format("%s_%s", stringKey, intValue)),
        rbSetEqualsMatcher(expectedSet));

    asserter.accept(
        rbMapOf(
            "a", 1,
            "b", 2,
            "c", 3),
        rbSetOf("a_1", "b_2", "c_3"));
    asserter.accept(
        singletonRBMap(
            "a", 1),
        singletonRBSet("a_1"));
    asserter.accept(emptyRBMap(), emptyRBSet());

    assertThat(
        rbMapOf(
            "a", 1,
            "b", 2)
        .toRBSet( (ignoredStringKey, ignoredIntValue) -> "same_value"),
        rbSetEqualsMatcher(singletonRBSet("same_value")));

    assertThat(
        rbMapOf(
            "a", 1,
            "b", 2,
            "c", 1,
            "d", 3)
            .toRBSet( (ignoredStringKey, intValue) -> intValue),
        rbSetEqualsMatcher(rbSetOf(1, 2, 3)));
  }

}
