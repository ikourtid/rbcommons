package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.Money;
import com.rb.nonbiz.functional.QuadriConsumer;
import com.rb.nonbiz.functional.QuadriFunction;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PositiveMultiplier;
import com.rb.nonbiz.types.RBNumeric;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import static com.google.common.collect.Iterators.singletonIterator;
import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.RBMapMergers.*;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapPreciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.PositiveMultiplier.POSITIVE_MULTIPLIER_1;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class RBMapMergersTest {

  @Test
  public void mergeIntoEithersMap_bothEmpty_returnsEmpty() {
    RBMap<LocalDate, Integer> leftMap = emptyRBMap();
    RBMap<LocalDate, String> rightMap = emptyRBMap();
    assertEquals(
        emptyRBMap(),
        mergeIntoEithersMap(leftMap, rightMap));
  }

  @Test
  public void mergeIntoEithersMap_hasCommonKeys_throws() {
    assertIllegalArgumentException( () -> mergeIntoEithersMap(
        singletonRBMap(LocalDate.of(1974, 4, 4), DUMMY_POSITIVE_INTEGER),
        singletonRBMap(LocalDate.of(1974, 4, 4), DUMMY_STRING)));
  }

  @Test
  public void mergeIntoEithersMap_generalCase() {
    assertEquals(
        rbMapOf(
            LocalDate.of(1974, 4, 1), Either.left(1),
            LocalDate.of(1974, 4, 2), Either.right("b"),
            LocalDate.of(1974, 4, 3), Either.left(3),
            LocalDate.of(1974, 4, 4), Either.right("d")),
        mergeIntoEithersMap(
            rbMapOf(
                LocalDate.of(1974, 4, 1), 1,
                LocalDate.of(1974, 4, 3), 3),
            rbMapOf(
                LocalDate.of(1974, 4, 2), "b",
                LocalDate.of(1974, 4, 4), "d")));
  }

  @Test
  public void testMergeMapsByValue() {
    RBMap<Integer, String> emptyStringMap = emptyRBMap();
    BinaryOperator<String> joiner = (s1, s2) -> Strings.format("%s%s", s1, s2);
    assertEquals(
        emptyStringMap,
        mergeRBMapsByValue(
            joiner,
            emptyStringMap,
            emptyStringMap));
    assertEquals(
        rbMapOf(
            1, "a1",
            2, "a2"),
        mergeRBMapsByValue(
            joiner,
            rbMapOf(
                1, "a1",
                2, "a2"),
            emptyStringMap));
    assertEquals(
        rbMapOf(
            1, "a1",
            2, "a2"),
        mergeRBMapsByValue(
            joiner,
            emptyStringMap,
            rbMapOf(
                1, "a1",
                2, "a2")));
    assertEquals(
        rbMapOf(
            1, "a1b1",
            2, "a2",
            3, "b3"),
        mergeRBMapsByValue(
            joiner,
            rbMapOf(
                1, "a1",
                2, "a2"),
            rbMapOf(
                1, "b1",
                3, "b3")));
    assertEquals(
        rbMapOf(
            1, "a1b1c1",
            2, "a2",
            3, "b3",
            4, "c4"),
        mergeRBMapsByValue(
            joiner,
            rbMapOf(
                1, "a1",
                2, "a2"),
            rbMapOf(
                1, "b1",
                3, "b3"),
            rbMapOf(
                1, "c1",
                4, "c4")));
  }

  @Test
  public void testMergeMapsByTransformedValue() {
    RBMap<Integer, Double>  emptyDoubleMap = emptyRBMap();
    RBMap<Integer, Boolean> emptyBooleanMap = emptyRBMap();
    RBMap<Integer, String>  emptyStringMap = emptyRBMap();
    BiFunction<RBMap<Integer, Double>, RBMap<Integer, Boolean>, RBMap<Integer, String>> merger = (map1, map2) ->
        mergeRBMapsByTransformedValue(
            (s1, s2) -> Strings.format("%s_%s", s1, s2),
            d -> Strings.format("L%s", d),
            d -> Strings.format("R%s", d),
            map1,
            map2);

    assertEquals(
        emptyStringMap,
        merger.apply(emptyDoubleMap, emptyBooleanMap));
    assertEquals(
        rbMapOf(
            1, "L1.1",
            2, "L2.2"),
        merger.apply(
            rbMapOf(
                1, 1.1,
                2, 2.2),
            emptyBooleanMap));
    assertEquals(
        rbMapOf(
            1, "Rtrue",
            2, "Rfalse"),
        merger.apply(
            emptyDoubleMap,
            rbMapOf(
                1, true,
                2, false)));
    assertEquals(
        rbMapOf(
            1, "1.1_true",
            2, "L2.2",
            3, "Rfalse"),
        merger.apply(
            rbMapOf(
                1, 1.1,
                2, 2.2),
            rbMapOf(
                1, true,
                3, false)));
  }

  @Test
  public void testMergeRBMapsDisallowingOverlap() {
    RBMap<Integer, String> emptyStringMap = emptyRBMap();
    assertEquals(
        emptyStringMap,
        mergeRBMapsDisallowingOverlap(
            emptyStringMap,
            emptyStringMap));
    assertEquals(
        rbMapOf(
            1, "a1",
            2, "a2"),
        mergeRBMapsDisallowingOverlap(
            rbMapOf(
                1, "a1",
                2, "a2"),
            emptyStringMap));
    assertEquals(
        rbMapOf(
            1, "a1",
            2, "a2"),
        mergeRBMapsDisallowingOverlap(
            emptyStringMap,
            rbMapOf(
                1, "a1",
                2, "a2")));
    assertIllegalArgumentException( () ->
        mergeRBMapsDisallowingOverlap(
            singletonRBMap(1, "a"),
            singletonRBMap(1, "a")));
    assertIllegalArgumentException( () ->
        mergeRBMapsDisallowingOverlap(
            singletonRBMap(1, "a"),
            singletonRBMap(1, "b")));
    assertIllegalArgumentException( () ->
        mergeRBMapsDisallowingOverlap(
            rbMapOf(
                1, "a1",
                2, "a2"),
            rbMapOf(
                1, "b1",
                3, "b3")));
    assertIllegalArgumentException( () ->
        mergeRBMapsDisallowingOverlap(
            rbMapOf(
                1, "a1",
                2, "a2"),
            rbMapOf(
                1, "b1",
                3, "b3"),
            rbMapOf(
                4, "c4",
                5, "c5")));
  }

  @Test
  public void testMergeRBMapEntriesExpectingSameKeys() {
    TriFunction<String, Integer, Double, String> merger = (key, intValue1, doubleValue2) ->
        Strings.format("%s:%s_%s", key, intValue1.toString(), doubleValue2.toString());

    TriConsumer<RBMap<String, Integer>, RBMap<String, Double>, RBMap<String, String>> asserter =
        (intMap1, doubleMap2, expectedMergedMap) ->
            assertEquals(
                expectedMergedMap,
                mergeRBMapEntriesExpectingSameKeys(
                    (key, v1, v2) -> merger.apply(key, v1, v2),
                    intMap1,
                    doubleMap2));

    asserter.accept(emptyRBMap(), emptyRBMap(), emptyRBMap());
    asserter.accept(
        singletonRBMap("a", 1),
        singletonRBMap("a", 2.2),
        singletonRBMap("a", "a:1_2.2"));
    asserter.accept(
        rbMapOf(
            "a", 1,
            "b", 3),
        rbMapOf(
            "a", 2.2,
            "b", 4.4),
        rbMapOf(
            "a", "a:1_2.2",
            "b", "b:3_4.4"));

    // if the maps have different numbers of keys, an exception will be thrown
    assertIllegalArgumentException( () -> mergeRBMapEntriesExpectingSameKeys(
        merger, singletonRBMap("a", 1), emptyRBMap()));
    assertIllegalArgumentException( () -> mergeRBMapEntriesExpectingSameKeys(
        merger, emptyRBMap(), singletonRBMap("a", 2.2)));
    assertIllegalArgumentException( () -> mergeRBMapEntriesExpectingSameKeys(
        merger,
        singletonRBMap("a", 1),  // 1 key
        rbMapOf(
            "a", 1.1,            // 2 keys; throws
            "b", 2.2)));

    // if the maps have the same numbers of keys, but the keys differ, this will again throw
    assertIllegalArgumentException( () -> mergeRBMapEntriesExpectingSameKeys(
        merger, singletonRBMap("a", 1), singletonRBMap("b", 2.2)));
  }

  // as above, but use the merge overload that does NOT involve the map key
  @Test
  public void testMergeRBMapValuesExpectingSameKeys() {
    BiFunction<Integer, Double, String> merger = (intValue1, doubleValue2) ->
        Strings.format("%s_%s", intValue1.toString(), doubleValue2.toString());

    TriConsumer<RBMap<String, Integer>, RBMap<String, Double>, RBMap<String, String>> asserter =
        (intMap1, doubleMap2, expectedMergedMap) ->
            assertEquals(
                expectedMergedMap,
                mergeRBMapValuesExpectingSameKeys(
                    (v1, v2) -> merger.apply(v1, v2),
                    intMap1,
                    doubleMap2));

    asserter.accept(emptyRBMap(), emptyRBMap(), emptyRBMap());
    asserter.accept(
        singletonRBMap("a", 1),
        singletonRBMap("a", 2.2),
        singletonRBMap("a", "1_2.2"));
    asserter.accept(
        rbMapOf(
            "a", 1,
            "b", 3),
        rbMapOf(
            "a", 2.2,
            "b", 4.4),
        rbMapOf(
            "a", "1_2.2",
            "b", "3_4.4"));

    // if the maps have different numbers of keys, an exception will be thrown
    assertIllegalArgumentException( () -> mergeRBMapValuesExpectingSameKeys(
        merger, singletonRBMap("a", 1), emptyRBMap()));
    assertIllegalArgumentException( () -> mergeRBMapValuesExpectingSameKeys(
        merger, emptyRBMap(), singletonRBMap("a", 2.2)));
    assertIllegalArgumentException( () -> mergeRBMapValuesExpectingSameKeys(
        merger,
        singletonRBMap("a", 1),  // 1 key
        rbMapOf(
            "a", 1.1,            // 2 keys; throws
            "b", 2.2)));

    // if the maps have the same numbers of keys, but the keys differ, this will again throw
    assertIllegalArgumentException( () -> mergeRBMapValuesExpectingSameKeys(
        merger, singletonRBMap("a", 1), singletonRBMap("b", 2.2)));
  }

  @Test
  public void testMergeSortedRBMapEntriesExpectingSameKeys() {
    // use a StringBuilder to keep track of the order in which the keys are inserted into the RBMap
    StringBuilder stringBuilder = new StringBuilder();

    TriFunction<String, Integer, Double, String> merger = (stringKey, intValue1, doubleValue2) -> {
      stringBuilder.append(stringKey);
      return Strings.format("%s:%s_%s", stringKey, intValue1.toString(), doubleValue2);
    };

    // This test doesn't prove that the map entries would come out in a sorted order (which is not guaranteed)
    // but it does show that the merging works.
    assertEquals(
        rbMapOf(
            "a", "a:1_1.1",
            "b", "b:2_2.2",
            "c", "c:3_3.3",
            "d", "d:4_4.4",
            "e", "e:5_5.5",
            "f", "f:6_6.6",
            "g", "g:7_7.7",
            "h", "h:8_8.8",
            "i", "i:9_9.9"),
        // The map entries are inserted in a sorted order, which in practice seem to imply they will come out in
        // the same order. This is convenient, e.g. when printing.
        mergeSortedRBMapEntriesExpectingSameKeys(
            (key, v1, v2) -> merger.apply(key, v1, v2),
            String::compareTo,
            rbMapOf(                // map1 in random key order
                "f", 6,
                "c", 3,
                "d", 4,
                "a", 1,
                "h", 8,
                "e", 5,
                "g", 7,
                "i", 9,
                "b", 2),
            rbMapOf(                // map2 in random key order
                "h", 8.8,
                "i", 9.9,
                "g", 7.7,
                "d", 4.4,
                "e", 5.5,
                "b", 2.2,
                "a", 1.1,
                "f", 6.6,
                "c", 3.3)));

    // check that the entries were inserted in order
    assertEquals(
        "abcdefghi",
        stringBuilder.toString());
  }

  @Test
  public void testMergeSortedRBMapValuesExpectingSameKeys() {
    // use a StringBuilder to keep track of the order in which the keys are inserted into the RBMap
    StringBuilder stringBuilder = new StringBuilder();

    BiFunction<Integer, Double, String> merger = (intValue1, doubleValue2) -> {
      stringBuilder.append(intValue1);
      return Strings.format("%s_%s", intValue1.toString(), doubleValue2.toString());
    };

    // This test doesn't prove that the map entries would come out in a sorted order (which is not guaranteed)
    // but it does show that the merging works.
    assertEquals(
        rbMapOf(
            "a", "1_1.1",
            "b", "2_2.2",
            "c", "3_3.3",
            "d", "4_4.4",
            "e", "5_5.5",
            "f", "6_6.6",
            "g", "7_7.7",
            "h", "8_8.8",
            "i", "9_9.9"),
        // The map entries are inserted in a sorted order, which in practice seems to imply that they will come
        // out in the same order. This is convenient, e.g. when printing.
        mergeSortedRBMapValuesExpectingSameKeys(
            (v1, v2) -> merger.apply(v1, v2),
            String::compareTo,
            rbMapOf(                // map1 in random key order
                "f", 6,
                "c", 3,
                "d", 4,
                "a", 1,
                "h", 8,
                "e", 5,
                "g", 7,
                "i", 9,
                "b", 2),
            rbMapOf(                // map2 in random key order
                "h", 8.8,
                "i", 9.9,
                "g", 7.7,
                "d", 4.4,
                "e", 5.5,
                "b", 2.2,
                "a", 1.1,
                "f", 6.6,
                "c", 3.3)));

    // check that the entries were inserted in order
    assertEquals(
        "123456789",
        stringBuilder.toString());
  }

  @Test
  public void testMergeRBMapEntriesExpectingSameKeys_3mapOverload() {
    QuadriFunction<String, Integer, Double, Boolean, String> merger = (key, doubleValue1, intValue2, boolValue3) ->
        Strings.format("%s:%s_%s[%s]", key, doubleValue1.toString(), intValue2.toString(), boolValue3.toString());

    QuadriConsumer<RBMap<String, Integer>, RBMap<String, Double>, RBMap<String, Boolean>, RBMap<String, String>>
        asserter = (intMap1, doubleMap2, booleanMap3, expectedMergedMap) ->
        assertEquals(
            expectedMergedMap,
            mergeRBMapEntriesExpectingSameKeys(
                intMap1,
                doubleMap2,
                booleanMap3,
                (key, v1, v2, v3) -> merger.apply(key, v1, v2, v3)));

    asserter.accept(emptyRBMap(), emptyRBMap(), emptyRBMap(), emptyRBMap());
    asserter.accept(
        singletonRBMap("a", 1),
        singletonRBMap("a", 2.2),
        singletonRBMap("a", false),
        singletonRBMap("a", "a:1_2.2[false]"));
    asserter.accept(
        rbMapOf(
            "a", 1,
            "b", 3),
        rbMapOf(
            "a", 2.2,
            "b", 4.4),
        rbMapOf(
            "a", false,
            "b", true),
        rbMapOf(
            "a", "a:1_2.2[false]",
            "b", "b:3_4.4[true]"));

    // if the maps have different numbers of keys, an exception will be thrown
    assertIllegalArgumentException( () -> mergeRBMapEntriesExpectingSameKeys(
        singletonRBMap("a", 1), emptyRBMap(), emptyRBMap(), merger));
    assertIllegalArgumentException( () -> mergeRBMapEntriesExpectingSameKeys(
        emptyRBMap(), singletonRBMap("a", 2.2), emptyRBMap(), merger));
    assertIllegalArgumentException( () -> mergeRBMapEntriesExpectingSameKeys(
        singletonRBMap("a", 1),      // 1 key
        rbMapOf(
            "a", 1.1,                // 2 keys; throws
            "b", 2.2),
        singletonRBMap("a", false),  // 1 key
        merger));

    // if the maps have the same numbers of keys, but the keys differ, this will again throw
    assertIllegalArgumentException( () -> mergeRBMapEntriesExpectingSameKeys(
        singletonRBMap("a", 1), singletonRBMap("b", 2.2), singletonRBMap("a", false), merger));
  }

  @Test
  public void testMergeRBMapValuesExpectingSameKeys_3mapOverload() {
    TriFunction<Integer, Double, Boolean, String> merger = (doubleValue1, intValue2, boolValue3) ->
        Strings.format("%s_%s[%s]", doubleValue1.toString(), intValue2.toString(), boolValue3.toString());

    QuadriConsumer<RBMap<String, Integer>, RBMap<String, Double>, RBMap<String, Boolean>, RBMap<String, String>>
        asserter = (intMap1, doubleMap2, booleanMap3, expectedMergedMap) ->
        assertEquals(
            expectedMergedMap,
            mergeRBMapValuesExpectingSameKeys(
                intMap1,
                doubleMap2,
                booleanMap3,
                (v1, v2, v3) -> merger.apply(v1, v2, v3)));

    asserter.accept(emptyRBMap(), emptyRBMap(), emptyRBMap(), emptyRBMap());
    asserter.accept(
        singletonRBMap("a", 1),
        singletonRBMap("a", 2.2),
        singletonRBMap("a", false),
        singletonRBMap("a", "1_2.2[false]"));
    asserter.accept(
        rbMapOf(
            "a", 1,
            "b", 3),
        rbMapOf(
            "a", 2.2,
            "b", 4.4),
        rbMapOf(
            "a", false,
            "b", true),
        rbMapOf(
            "a", "1_2.2[false]",
            "b", "3_4.4[true]"));

    // if the maps have different numbers of keys, an exception will be thrown
    assertIllegalArgumentException( () -> mergeRBMapValuesExpectingSameKeys(
        singletonRBMap("a", 1), emptyRBMap(), emptyRBMap(), merger));
    assertIllegalArgumentException( () -> mergeRBMapValuesExpectingSameKeys(
        emptyRBMap(), singletonRBMap("a", 2.2), emptyRBMap(), merger));
    assertIllegalArgumentException( () -> mergeRBMapValuesExpectingSameKeys(
        singletonRBMap("a", 1),      // 1 key
        rbMapOf(
            "a", 1.1,                // 2 keys; throws
            "b", 2.2),
        singletonRBMap("a", false),  // 1 key
        merger));

    // if the maps have the same numbers of keys, but the keys differ, this will again throw
    assertIllegalArgumentException( () -> mergeRBMapValuesExpectingSameKeys(
        singletonRBMap("a", 1), singletonRBMap("b", 2.2), singletonRBMap("a", false), merger));
  }

  @Test
  public void mergeRBMapsByValue_generalCase() {
    assertEquals(
        rbMapOf(
            "a", "1.",
            "b", "2_3",
            "c", ".4"),
        mergeRBMapsByValue(
            (v1, v2) -> Strings.format("%s_%s", v1, v2),
            v1 -> Strings.format("%s.", v1),
            v2 -> Strings.format(".%s", v2),
            rbMapOf(
                "a", "1",
                "b", "2"),
            rbMapOf(
                "b", "3",
                "c", "4")));
  }

  @Test
  public void mergeRBMapsByTransformedValue_listOfRBMapsOverload() {
    BiConsumer<List<RBMap<String, Integer>>, RBMap<String, String>> asserter = (listOfRBMaps, expectedMergedMap) ->
        assertEquals(
            expectedMergedMap,
            mergeRBMapsByTransformedValue(
                (key, valueList) ->
                    Strings.format("%s=%s", key, StringUtils.join(valueList, ":")),
                listOfRBMaps));

    // merging an empty list of maps gives an empty map
    asserter.accept(
        emptyList(),
        emptyRBMap());

    // merging empty maps gives an empty map
    asserter.accept(
        ImmutableList.of(
            emptyRBMap(),
            emptyRBMap(),
            emptyRBMap()),
        emptyRBMap());

    // "merging" a single map
    asserter.accept(
        singletonList(
          rbMapOf(
              "a", 1,
              "b", 2)),
        rbMapOf(
            "a", "a=1",
            "b", "b=2"));

    // merging multiple overlapping maps
    asserter.accept(
        ImmutableList.of(
            rbMapOf(
                "a", 1,
                "b", 2),
            rbMapOf(
                "b", 3,
                "c", 4),
            emptyRBMap()),     // contributes nothing to the merged result
        rbMapOf(
            "a", "a=1",
            "b", "b=2:3",
            "c", "c=4"));
  }

  @Test
  public void testMergeRBMapsDisallowingOverlap_otherOverload() {
    assertEquals(
        rbMapOf(
            "a", intExplained(10,  1 * 10),
            "b", intExplained(20,  2 * 10),
            "c", intExplained(300, 3 * 100)),
        mergeRBMapsDisallowingOverlap(
            v -> v * 10,
            v -> v * 100,
            rbMapOf(
                "a", 1,
                "b", 2),
            singletonRBMap(
                "c", 3)));
    assertIllegalArgumentException( () -> mergeRBMapsDisallowingOverlap(
        v -> v * DUMMY_POSITIVE_INTEGER,
        v -> v * DUMMY_POSITIVE_INTEGER,
        rbMapOf(
            "a", DUMMY_POSITIVE_INTEGER,
            "b", DUMMY_POSITIVE_INTEGER),
        singletonRBMap(
            "b", DUMMY_POSITIVE_INTEGER)));
  }

  @Test
  public void test_mergeRBMapsAllowingOverlapOnSimilarItemsOnly() {
    BiConsumer<Iterator<RBMap<String, Money>>, RBMap<String, Money>> asserter = (iidMapIterator, expectedResult) ->
        assertThat(
            mergeRBMapsAllowingOverlapOnSimilarItemsOnly(
                iidMapIterator,
                (v1, v2) -> v1.almostEquals(v2, DEFAULT_EPSILON_1e_8)),
            rbMapPreciseValueMatcher(expectedResult, DEFAULT_EPSILON_1e_8));

    asserter.accept(emptyIterator(), emptyRBMap());
    asserter.accept(singletonIterator(emptyRBMap()), emptyRBMap());
    asserter.accept(ImmutableList.<RBMap<String, Money>>of(emptyRBMap(), emptyRBMap()).iterator(), emptyRBMap());

    // Does not complain if there is overlap but the values are similar
    rbSetOf(-1e-9, 0.0, 1e-9).forEach(epsilon -> {
      asserter.accept(
          ImmutableList.of(
              rbMapOf(
                  "A1", money(100),
                  "A2", money(200)),
              rbMapOf(
                  "A2", money(200 + epsilon),
                  "A3", money(300)))
              .iterator(),
          rbMapOf(
              "A1", money(100),
              "A2", money(200),
              "A3", money(300)));
      asserter.accept(
          ImmutableList.of(
              rbMapOf(
                  "A1", money(100),
                  // This value gets processed first, so it ends up in the return value - NOT 200 below
                  "A2", money(200 + epsilon)),
              rbMapOf(
                  "A2", money(200),
                  "A3", money(300)))
              .iterator(),
          rbMapOf(
              "A1", money(100),
              "A2", money(200 + epsilon),
              "A3", money(300)));
    });

    rbSetOf(-999.0, -1.0, -1e-7, 1e-7, 1.0, 999.0).forEach(largeEpsilon ->
        assertIllegalArgumentException( () ->
            mergeRBMapsAllowingOverlapOnSimilarItemsOnly(
                ImmutableList.of(
                    rbMapOf(
                        "A1", money(100),
                        "A2", money(200)),
                    rbMapOf(
                        "A2", money(200 + largeEpsilon),
                        "A3", money(300)))
                    .iterator(),
                (v1, v2) -> v1.almostEquals(v2, DEFAULT_EPSILON_1e_8))));
  }

  @Test
  public void testMergeRBMapEntriesExpectingExactOverlap() {
    BiFunction<RBMap<Integer, String>, RBMap<Integer, Boolean>, RBMap<Integer, String>> merger = (map1, map2) ->
        mergeRBMapEntriesExpectingSameKeys(
            (intKey, stringValueFrom1, booleanFrom2) -> Strings.format("%s.%s.%s", intKey, stringValueFrom1, booleanFrom2),
            map1,
            map2);

    assertEquals(
        rbMapOf(
            1, "1._1.true",
            2, "2._2.false"),
        merger.apply(
            rbMapOf(
                1, "_1",
                2, "_2"),
            rbMapOf(
                1, true,
                2, false)));

    assertEquals(
        singletonRBMap(1, "1._1.true"),
        merger.apply(
            singletonRBMap(1, "_1"),
            singletonRBMap(1, true)));

    //noinspection AssertEqualsBetweenInconvertibleTypes
    assertEquals(
        emptyRBMap(),
        merger.apply(emptyRBMap(), emptyRBMap()));

    // Must be exact overlap in the keys
    assertIllegalArgumentException( () -> merger.apply(
        rbMapOf(
            1, "_1",
            2, "_2"),
        singletonRBMap(
            1, true)));
    assertIllegalArgumentException( () -> merger.apply(
        singletonRBMap(
            1, "_1"),
        rbMapOf(
            1, true,
            2, false)));
  }

  @Test
  public void testDotProduct() {
    RBMap<String, Money> moneyMap = rbMapOf(
        "A", money(10.0),
        "B", money(20.0),
        "C", money(30.0));
    RBMap<String, Money> zeroMoneyMap = rbMapOf(
        "A", ZERO_MONEY,
        "B", ZERO_MONEY,
        "C", ZERO_MONEY);
    RBMap<String, PositiveMultiplier> multiplierMap = rbMapOf(
        "A", positiveMultiplier(1),
        "B", positiveMultiplier(2),
        "C", positiveMultiplier(3));
    RBMap<String, PositiveMultiplier> multiplierOneMap = rbMapOf(
        "A", POSITIVE_MULTIPLIER_1,
        "B", POSITIVE_MULTIPLIER_1,
        "C", POSITIVE_MULTIPLIER_1);

    TriConsumer<RBMap<String, ? extends RBNumeric>, RBMap<String, ? extends RBNumeric>, Double> asserter =
        (map1, map2, expectedDotProduct) ->
            assertEquals(
                expectedDotProduct,
                dotProductOfRBMaps(map1, map2),
                1e-8);

    asserter.accept(multiplierMap, multiplierMap, doubleExplained(   14,  1 *  1 +  2 *  2 +  3 *  3));
    asserter.accept(multiplierMap, moneyMap,      doubleExplained(  140,  1 * 10 +  2 * 20 +  3 * 30));
    asserter.accept(moneyMap,      multiplierMap, doubleExplained(  140, 10 *  1 + 20 *  2 + 30 *  3));
    asserter.accept(moneyMap,      moneyMap,      doubleExplained(1_400, 10 * 10 + 20 * 20 + 30 * 30));

    asserter.accept(zeroMoneyMap, multiplierMap, doubleExplained(0, 0 * 1 + 0 * 2 + 0 * 3));
    asserter.accept(multiplierMap, zeroMoneyMap, doubleExplained(0, 1 * 0 + 2 * 0 + 3 * 0));

    asserter.accept(multiplierOneMap, moneyMap, doubleExplained(60,  1 * 10 +  1 * 20 +  1 * 30));
    asserter.accept(moneyMap, multiplierOneMap, doubleExplained(60, 10 *  1 + 20 *  1 + 30 *  1));

    asserter.accept(zeroMoneyMap,     zeroMoneyMap,     doubleExplained(0, 0 * 0 + 0 * 0 + 0 * 0));
    asserter.accept(multiplierOneMap, multiplierOneMap, doubleExplained(3, 1 * 1 + 1 * 1 + 1 * 1));


    // if the keys don't match, an exception will be thrown
    assertIllegalArgumentException( () -> dotProductOfRBMaps(
        singletonRBMap("a", money(1)),
        singletonRBMap("b", POSITIVE_MULTIPLIER_1)));
    assertIllegalArgumentException( () -> dotProductOfRBMaps(
        rbMapOf(
            "a", money(1),
            "b", money(2)),
        singletonRBMap("a", POSITIVE_MULTIPLIER_1)));
  }

}
