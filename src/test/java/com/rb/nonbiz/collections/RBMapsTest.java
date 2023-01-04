package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.Money;
import com.rb.nonbiz.types.PositiveMultiplier;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBMaps.filterForPresentValuesAndTransformValuesCopy;
import static com.rb.nonbiz.collections.RBMaps.filterMapKeys;
import static com.rb.nonbiz.collections.RBMaps.getWhenUpToOneRBMapIsNonEmpty;
import static com.rb.nonbiz.collections.RBMaps.impreciseValueMapsAlmostEqual;
import static com.rb.nonbiz.collections.RBMaps.lockValues;
import static com.rb.nonbiz.collections.RBMaps.mapEntrySet;
import static com.rb.nonbiz.collections.RBMaps.mapEntrySetToDouble;
import static com.rb.nonbiz.collections.RBMaps.preciseValueMapsAlmostEqual;
import static com.rb.nonbiz.collections.RBMaps.sharedItemMap;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalNonEmpty;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBMapsTest {

  @Test
  public void testFilterMap() {
    RBMap<Integer, String> originalMap = rbMapOf(
        1, "a",
        2, "b",
        3, "c");
    assertEquals(
        "Filters nothing",
        originalMap,
        filterMapKeys(originalMap, key -> key < 4));
    assertEquals(
        "Filters 1 of 3 items",
        rbMapOf(
            1, "a",
            2, "b"),
        filterMapKeys(originalMap, key -> key < 3));
    assertEquals(
        "Filters all items",
        emptyRBMap(),
        filterMapKeys(originalMap, key -> key < 0));
  }

  @Test
  public void testSharedItemMap() {
    assertEquals(
        rbMapOf(
            "a", 123,
            "b", 123),
        sharedItemMap(rbSetOf("a", "b"), 123));
    assertEquals(
        emptyRBMap(),
        sharedItemMap(emptyRBSet(), 123));
  }

  @Test
  public void testLockValues() {
    MutableRBMap<String, MutableRBMap<String, Integer>> mutableMap = newMutableRBMap();
    mutableMap.putAssumingAbsent("a", newMutableRBMap());
    mutableMap.putAssumingAbsent("b", newMutableRBMap());
    mutableMap.putAssumingAbsent("c", newMutableRBMap());
    mutableMap.getOrThrow("b").putAssumingAbsent("b1", 1);
    mutableMap.getOrThrow("c").putAssumingAbsent("c1", 1);
    mutableMap.getOrThrow("c").putAssumingAbsent("c2", 2);
    assertEquals(
        rbMapOf(
            "a", emptyRBMap(),
            "b", singletonRBMap("b1", 1),
            "c", rbMapOf(
                "c1", 1,
                "c2", 2)),
        lockValues(mutableMap));
  }

  @Test
  public void testFilterForPresentValuesAndTransformValuesCopy() {
    BiConsumer<RBMap<String, Integer>, Function<Integer, Optional<Integer>>> asserter = (expectedResult, transformer) ->
        assertEquals(
            expectedResult,
            filterForPresentValuesAndTransformValuesCopy(
                rbMapOf(
                    "A", 1,
                    "B", 2,
                    "C", 3,
                    "D", 4,
                    "E", 5),
                transformer));
    // all are Optional.empty()
    asserter.accept(
        emptyRBMap(),
        v -> Optional.empty());
    // all are non-empty + 100
    asserter.accept(
        rbMapOf(
            "A", intExplained(101, 100 + 1),
            "B", intExplained(102, 100 + 2),
            "C", intExplained(103, 100 + 3),
            "D", intExplained(104, 100 + 4),
            "E", intExplained(105, 100 + 5)),
        v -> Optional.of(v + 100));
    // only even ones pass the filter
    asserter.accept(
        rbMapOf(
            "B", intExplained(102, 100 + 2),
            "D", intExplained(104, 100 + 4)),
        v -> v % 2 == 0
            ? Optional.of(v + 100)
            : Optional.empty());
  }

  @Test
  public void testGetOnlyNonEmptyRBMap() {
    assertOptionalNonEmpty(
        getWhenUpToOneRBMapIsNonEmpty(emptyRBMap(), emptyRBMap()),
        rbMapMatcher(emptyRBMap(), f -> typeSafeEqualTo(f)));
    assertOptionalNonEmpty(
        getWhenUpToOneRBMapIsNonEmpty(emptyRBMap(), emptyRBMap(), emptyRBMap()),
        rbMapMatcher(emptyRBMap(), f -> typeSafeEqualTo(f)));
    rbSetOf(
        getWhenUpToOneRBMapIsNonEmpty(singletonRBMap("a", 1), emptyRBMap()),
        getWhenUpToOneRBMapIsNonEmpty(emptyRBMap(),           singletonRBMap("a", 1)),
        getWhenUpToOneRBMapIsNonEmpty(singletonRBMap("a", 1), emptyRBMap(),           emptyRBMap()),
        getWhenUpToOneRBMapIsNonEmpty(emptyRBMap(),           singletonRBMap("a", 1), emptyRBMap()),
        getWhenUpToOneRBMapIsNonEmpty(emptyRBMap(),           emptyRBMap(),           singletonRBMap("a", 1)))
        .forEach(result -> assertOptionalNonEmpty(
            result,
            rbMapMatcher(
                singletonRBMap("a", 1), f -> typeSafeEqualTo(f))));
    rbSetOf(
        getWhenUpToOneRBMapIsNonEmpty(singletonRBMap("a", 1), singletonRBMap("a", 1)),
        getWhenUpToOneRBMapIsNonEmpty(singletonRBMap("a", 1), singletonRBMap("b", 2)),
        getWhenUpToOneRBMapIsNonEmpty(singletonRBMap("a", 1), singletonRBMap("a", 1), emptyRBMap()),
        getWhenUpToOneRBMapIsNonEmpty(singletonRBMap("a", 1), singletonRBMap("b", 2), emptyRBMap()),
        getWhenUpToOneRBMapIsNonEmpty(singletonRBMap("a", 1), emptyRBMap(),           singletonRBMap("a", 1)),
        getWhenUpToOneRBMapIsNonEmpty(singletonRBMap("a", 1), emptyRBMap(),           singletonRBMap("b", 2)),
        getWhenUpToOneRBMapIsNonEmpty(emptyRBMap(),           singletonRBMap("a", 1), singletonRBMap("a", 1)),
        getWhenUpToOneRBMapIsNonEmpty(emptyRBMap(),           singletonRBMap("a", 1), singletonRBMap("b", 2)),
        getWhenUpToOneRBMapIsNonEmpty(singletonRBMap("a", 1), singletonRBMap("a", 1), singletonRBMap("a", 1)),
        getWhenUpToOneRBMapIsNonEmpty(singletonRBMap("a", 1), singletonRBMap("b", 2), singletonRBMap("c", 3)))
        .forEach(result -> assertOptionalEmpty(result));
  }

  @Test
  public void testMapEntrySet() {
    BiConsumer<RBMap<String, Integer>, List<String>> asserter = (map, expectedResult) ->
        assertThat(
            mapEntrySet(map, (key, value) -> String.format("%s_%s", key, value))
                .collect(Collectors.toList()),
            orderedListEqualityMatcher(expectedResult));

    asserter.accept(rbMapOf("a", 1, "b", 2), ImmutableList.of("a_1", "b_2"));
    asserter.accept(emptyRBMap(),            emptyList());
  }

  @Test
  public void testMapEntrySetToDouble() {
    BiConsumer<RBMap<Integer, Double>, Double> asserter = (map, expectedResult) ->
        assertEquals(
            expectedResult,
            mapEntrySetToDouble(map, (key, value) -> key * 10 + value).sum(),
            1e-8);

    asserter.accept(rbMapOf(1, 7.7, 2, 8.8), doubleExplained(46.5, 1 * 10.0 + 7.7 + 2 * 10 + 8.8));
    asserter.accept(emptyRBMap(),            0.0);
  }

  @Test
  public void testPreciseValueMapsAlmostEquals() {
    Function<Double, RBMap<String, Money>> maker = epsilon -> rbMapOf(
        "A", money(1.11 + epsilon),
        "B", money(2.22 + epsilon));

    RBMap<String, Money> map1 = maker.apply(0.0);

    assertFalse(preciseValueMapsAlmostEqual(map1, maker.apply(-0.1),  DEFAULT_EPSILON_1e_8));
    assertFalse(preciseValueMapsAlmostEqual(map1, maker.apply(-1e-7), DEFAULT_EPSILON_1e_8));

    assertTrue( preciseValueMapsAlmostEqual(map1, maker.apply(-1e-9), DEFAULT_EPSILON_1e_8));
    assertTrue( preciseValueMapsAlmostEqual(map1, maker.apply( 0.0),  DEFAULT_EPSILON_1e_8));
    assertTrue( preciseValueMapsAlmostEqual(map1, maker.apply( 1e-9), DEFAULT_EPSILON_1e_8));

    assertFalse(preciseValueMapsAlmostEqual(map1, maker.apply( 1e-7), DEFAULT_EPSILON_1e_8));
    assertFalse(preciseValueMapsAlmostEqual(map1, maker.apply( 0.1),  DEFAULT_EPSILON_1e_8));

    // if the keys don't match, then the maps aren't equal
    assertFalse(preciseValueMapsAlmostEqual(map1, singletonRBMap("A", money(1.11)), DEFAULT_EPSILON_1e_8));
    assertFalse(preciseValueMapsAlmostEqual(map1, singletonRBMap("B", money(2.22)), DEFAULT_EPSILON_1e_8));
    assertFalse(preciseValueMapsAlmostEqual(map1, rbMapOf(
        "A", money(1.11),
        "B", money(2.22),
        "C", money(3.33)),
        DEFAULT_EPSILON_1e_8));

    // also works on empty maps
    RBMap<String, Money> emptyMap = emptyRBMap();
    assertTrue( preciseValueMapsAlmostEqual(emptyMap, emptyMap, DEFAULT_EPSILON_1e_8));
    assertFalse(preciseValueMapsAlmostEqual(map1, emptyRBMap(), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testImpreciseValueMapsAlmostEquals() {
    Function<Double, RBMap<String, PositiveMultiplier>> maker = epsilon -> rbMapOf(
        "A", positiveMultiplier(1.11 + epsilon),
        "B", positiveMultiplier(2.22 + epsilon));

    RBMap<String, PositiveMultiplier> map1 = maker.apply(0.0);

    assertFalse(impreciseValueMapsAlmostEqual(map1, maker.apply(-0.1),  DEFAULT_EPSILON_1e_8));
    assertFalse(impreciseValueMapsAlmostEqual(map1, maker.apply(-1e-7), DEFAULT_EPSILON_1e_8));

    assertTrue( impreciseValueMapsAlmostEqual(map1, maker.apply(-1e-9), DEFAULT_EPSILON_1e_8));
    assertTrue( impreciseValueMapsAlmostEqual(map1, maker.apply( 0.0),  DEFAULT_EPSILON_1e_8));
    assertTrue( impreciseValueMapsAlmostEqual(map1, maker.apply( 1e-9), DEFAULT_EPSILON_1e_8));

    assertFalse(impreciseValueMapsAlmostEqual(map1, maker.apply( 1e-7), DEFAULT_EPSILON_1e_8));
    assertFalse(impreciseValueMapsAlmostEqual(map1, maker.apply( 0.1),  DEFAULT_EPSILON_1e_8));

    // if the keys don't match, then the maps aren't equal
    assertFalse(impreciseValueMapsAlmostEqual(map1, singletonRBMap("A", positiveMultiplier(1.11)), DEFAULT_EPSILON_1e_8));
    assertFalse(impreciseValueMapsAlmostEqual(map1, singletonRBMap("B", positiveMultiplier(2.22)), DEFAULT_EPSILON_1e_8));
    assertFalse(impreciseValueMapsAlmostEqual(map1, rbMapOf(
        "A", positiveMultiplier(1.11),
        "B", positiveMultiplier(2.22),
        "C", positiveMultiplier(3.33)),
        DEFAULT_EPSILON_1e_8));

    // also works on empty maps
    RBMap<String, PositiveMultiplier> emptyMap = emptyRBMap();
    assertTrue( impreciseValueMapsAlmostEqual(emptyMap, emptyMap, DEFAULT_EPSILON_1e_8));
    assertFalse(impreciseValueMapsAlmostEqual(map1, emptyRBMap(), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void hasDuplicates_throws() {
    assertIllegalArgumentException( () -> rbMapOf(
        "a", 123,
        "a", 456));
    assertIllegalArgumentException( () -> rbMapOf(
        "a", 123,
        "a", 123));
    RBMap<String, Integer> doesNotThrow = rbMapOf(
        "a", DUMMY_POSITIVE_INTEGER,
        "b", DUMMY_POSITIVE_INTEGER);
  }

}
