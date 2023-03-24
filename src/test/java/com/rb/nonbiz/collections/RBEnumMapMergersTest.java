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

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static com.google.common.collect.Iterators.singletonIterator;
import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.RBEnumMap.newRBEnumMap;
import static com.rb.nonbiz.collections.RBEnumMapMergers.dotProductOfRBEnumMaps;
import static com.rb.nonbiz.collections.RBEnumMapMergers.mergeIntoEithersEnumMap;
import static com.rb.nonbiz.collections.RBEnumMapMergers.mergeRBEnumMapEntriesExpectingSameKeys;
import static com.rb.nonbiz.collections.RBEnumMapMergers.mergeRBEnumMapValuesExpectingSameKeys;
import static com.rb.nonbiz.collections.RBEnumMapMergers.mergeRBEnumMapsAllowingOverlapOnSimilarItemsOnly;
import static com.rb.nonbiz.collections.RBEnumMapMergers.mergeRBEnumMapsByTransformedValue;
import static com.rb.nonbiz.collections.RBEnumMapMergers.mergeRBEnumMapsByValue;
import static com.rb.nonbiz.collections.RBEnumMapMergers.mergeRBEnumMapsDisallowingOverlap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbEnumMapMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.PositiveMultiplier.POSITIVE_MULTIPLIER_1;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.emptyRBEnumMap;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.rbEnumMapOf;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.singletonRBEnumMap;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class RBEnumMapMergersTest {

  private enum LocalTestEnum { E1, E2, E3, E4, E5, E6, E7, E8, E9, E10 }

  @Test
  public void mergeIntoEithersMap_bothEmpty_returnsEmpty() {
    RBEnumMap<LocalTestEnum, Integer> leftMap = emptyRBEnumMap(LocalTestEnum.class);
    RBEnumMap<LocalTestEnum, String> rightMap = emptyRBEnumMap(LocalTestEnum.class);
    assertEquals(
        emptyRBEnumMap(LocalTestEnum.class),
        mergeIntoEithersEnumMap(leftMap, rightMap));
  }

  @Test
  public void mergeIntoEithersMap_hasCommonKeys_throws() {
    assertIllegalArgumentException( () -> mergeIntoEithersEnumMap(
        singletonRBEnumMap(LocalTestEnum.E4, DUMMY_POSITIVE_INTEGER),
        singletonRBEnumMap(LocalTestEnum.E4, DUMMY_STRING)));
  }

  @Test
  public void mergeIntoEithersMap_generalCase() {
    assertEquals(
        rbEnumMapOf(
            LocalTestEnum.E1, Either.left(1),
            LocalTestEnum.E2, Either.right("v2"),
            LocalTestEnum.E3, Either.left(3),
            LocalTestEnum.E4, Either.right("v4")),
        mergeIntoEithersEnumMap(
            rbEnumMapOf(
                LocalTestEnum.E1, 1,
                LocalTestEnum.E3, 3),
            rbEnumMapOf(
                LocalTestEnum.E2, "v2",
                LocalTestEnum.E4, "v4")));
  }

  @Test
  public void testMergeMapsByValue() {
    RBEnumMap<LocalTestEnum, String> emptyStringMap = emptyRBEnumMap(LocalTestEnum.class);
    BinaryOperator<String> joiner = (s1, s2) -> Strings.format("%s%s", s1, s2);
    assertEquals(
        emptyStringMap,
        mergeRBEnumMapsByValue(
            joiner,
            emptyStringMap,
            emptyStringMap));
    assertEquals(
        rbEnumMapOf(
            LocalTestEnum.E1, "v1",
            LocalTestEnum.E2, "v2"),
        mergeRBEnumMapsByValue(
            joiner,
            rbEnumMapOf(
                LocalTestEnum.E1, "v1",
                LocalTestEnum.E2, "v2"),
            emptyStringMap));
    assertEquals(
        rbEnumMapOf(
            LocalTestEnum.E1, "v1",
            LocalTestEnum.E2, "v2"),
        mergeRBEnumMapsByValue(
            joiner,
            emptyStringMap,
            rbEnumMapOf(
                LocalTestEnum.E1, "v1",
                LocalTestEnum.E2, "v2")));
    assertEquals(
        rbEnumMapOf(
            LocalTestEnum.E1, "v1av1b",
            LocalTestEnum.E2, "v2",
            LocalTestEnum.E3, "v3"),
        mergeRBEnumMapsByValue(
            joiner,
            rbEnumMapOf(
                LocalTestEnum.E1, "v1a",
                LocalTestEnum.E2, "v2"),
            rbEnumMapOf(
                LocalTestEnum.E1, "v1b",
                LocalTestEnum.E3, "v3")));
    assertEquals(
        rbEnumMapOf(
            LocalTestEnum.E1, "v1av1bv1c",
            LocalTestEnum.E2, "v2",
            LocalTestEnum.E3, "v3",
            LocalTestEnum.E4, "v4"),
        mergeRBEnumMapsByValue(
            joiner,
            rbEnumMapOf(
                LocalTestEnum.E1, "v1a",
                LocalTestEnum.E2, "v2"),
            rbEnumMapOf(
                LocalTestEnum.E1, "v1b",
                LocalTestEnum.E3, "v3"),
            rbEnumMapOf(
                LocalTestEnum.E1, "v1c",
                LocalTestEnum.E4, "v4")));
  }

  @Test
  public void testMergeMapsByTransformedValue() {
    RBEnumMap<LocalTestEnum, Double>  emptyDoubleMap = emptyRBEnumMap(LocalTestEnum.class);
    RBEnumMap<LocalTestEnum, Boolean> emptyBooleanMap = emptyRBEnumMap(LocalTestEnum.class);
    RBEnumMap<LocalTestEnum, String>  emptyStringMap = emptyRBEnumMap(LocalTestEnum.class);
    BiFunction<RBEnumMap<LocalTestEnum, Double>, RBEnumMap<LocalTestEnum, Boolean>, RBEnumMap<LocalTestEnum, String>> merger = (map1, map2) ->
        mergeRBEnumMapsByTransformedValue(
            (s1, s2) -> Strings.format("%s_%s", s1, s2),
            d -> Strings.format("L%s", d),
            d -> Strings.format("R%s", d),
            map1,
            map2);

    assertEquals(
        emptyStringMap,
        merger.apply(emptyDoubleMap, emptyBooleanMap));
    assertEquals(
        rbEnumMapOf(
            LocalTestEnum.E1, "L1.1",
            LocalTestEnum.E2, "L2.2"),
        merger.apply(
            rbEnumMapOf(
                LocalTestEnum.E1, 1.1,
                LocalTestEnum.E2, 2.2),
            emptyBooleanMap));
    assertEquals(
        rbEnumMapOf(
            LocalTestEnum.E1, "Rtrue",
            LocalTestEnum.E2, "Rfalse"),
        merger.apply(
            emptyDoubleMap,
            rbEnumMapOf(
                LocalTestEnum.E1, true,
                LocalTestEnum.E2, false)));
    assertEquals(
        rbEnumMapOf(
            LocalTestEnum.E1, "1.1_true",
            LocalTestEnum.E2, "L2.2",
            LocalTestEnum.E3, "Rfalse"),
        merger.apply(
            rbEnumMapOf(
                LocalTestEnum.E1, 1.1,
                LocalTestEnum.E2, 2.2),
            rbEnumMapOf(
                LocalTestEnum.E1, true,
                LocalTestEnum.E3, false)));
  }

  @Test
  public void testMergeRBEnumMapsDisallowingOverlap() {
    RBEnumMap<LocalTestEnum, String> emptyStringMap = emptyRBEnumMap(LocalTestEnum.class);
    assertEquals(
        emptyStringMap,
        mergeRBEnumMapsDisallowingOverlap(
            emptyStringMap,
            emptyStringMap));
    assertEquals(
        rbEnumMapOf(
            LocalTestEnum.E1, "a1",
            LocalTestEnum.E2, "a2"),
        mergeRBEnumMapsDisallowingOverlap(
            rbEnumMapOf(
                LocalTestEnum.E1, "a1",
                LocalTestEnum.E2, "a2"),
            emptyStringMap));
    assertEquals(
        rbEnumMapOf(
            LocalTestEnum.E1, "a1",
            LocalTestEnum.E2, "a2"),
        mergeRBEnumMapsDisallowingOverlap(
            emptyStringMap,
            rbEnumMapOf(
                LocalTestEnum.E1, "a1",
                LocalTestEnum.E2, "a2")));
    assertIllegalArgumentException( () ->
        mergeRBEnumMapsDisallowingOverlap(
            singletonRBEnumMap(LocalTestEnum.E1, LocalTestEnum.E1 ),
            singletonRBEnumMap(LocalTestEnum.E1, LocalTestEnum.E1 )));
    assertIllegalArgumentException( () ->
        mergeRBEnumMapsDisallowingOverlap(
            singletonRBEnumMap(LocalTestEnum.E1, LocalTestEnum.E1 ),
            singletonRBEnumMap(LocalTestEnum.E1, LocalTestEnum.E2 )));
    assertIllegalArgumentException( () ->
        mergeRBEnumMapsDisallowingOverlap(
            rbEnumMapOf(
                LocalTestEnum.E1, "a1",
                LocalTestEnum.E2, "a2"),
            rbEnumMapOf(
                LocalTestEnum.E1, "E21",
                LocalTestEnum.E3, "E23")));
    assertIllegalArgumentException( () ->
        mergeRBEnumMapsDisallowingOverlap(
            rbEnumMapOf(
                LocalTestEnum.E1, "a1",
                LocalTestEnum.E2, "a2"),
            rbEnumMapOf(
                LocalTestEnum.E1, "E21",
                LocalTestEnum.E3, "E23"),
            rbEnumMapOf(
                LocalTestEnum.E4, "E34",
                LocalTestEnum.E5, "E35")));
  }

  @Test
  public void testMergeRBEnumMapEntriesExpectingSameKeys() {
    TriFunction<LocalTestEnum, Integer, Double, String> merger = (key, intValue1, doubleValue2) ->
        Strings.format("%s:%s_%s", key, intValue1.toString(), doubleValue2.toString());

    TriConsumer<
        RBEnumMap<LocalTestEnum, Integer>,
        RBEnumMap<LocalTestEnum, Double>,
        RBEnumMap<LocalTestEnum, String>> asserter =
        (intMap1, doubleMap2, expectedMergedMap) ->
            assertEquals(
                expectedMergedMap,
                mergeRBEnumMapEntriesExpectingSameKeys(
                    (key, v1, v2) -> merger.apply(key, v1, v2),
                    intMap1,
                    doubleMap2));

    asserter.accept(
        emptyRBEnumMap(LocalTestEnum.class),
        emptyRBEnumMap(LocalTestEnum.class),
        emptyRBEnumMap(LocalTestEnum.class));
    asserter.accept(
        singletonRBEnumMap(LocalTestEnum.E1, 1),
        singletonRBEnumMap(LocalTestEnum.E1, 2.2),
        singletonRBEnumMap(LocalTestEnum.E1, "E1:1_2.2"));
    asserter.accept(
        rbEnumMapOf(
            LocalTestEnum.E1, 1,
            LocalTestEnum.E2, 3),
        rbEnumMapOf(
            LocalTestEnum.E1, 2.2,
            LocalTestEnum.E2, 4.4),
        rbEnumMapOf(
            LocalTestEnum.E1, "E1:1_2.2",
            LocalTestEnum.E2, "E2:3_4.4"));

    // if the maps have different numbers of keys, an exception will be thrown
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        merger, singletonRBEnumMap(LocalTestEnum.E1, 1), emptyRBEnumMap(LocalTestEnum.class)));
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        merger, emptyRBEnumMap(LocalTestEnum.class), singletonRBEnumMap(LocalTestEnum.E1, 2.2)));
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        merger,
        singletonRBEnumMap(LocalTestEnum.E1, 1),  // 1 key
        rbEnumMapOf(
            LocalTestEnum.E1, 1.1,            // 2 keys; throws
            LocalTestEnum.E2, 2.2)));

    // if the maps have the same numbers of keys, but the keys differ, this will again throw
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        merger, singletonRBEnumMap(LocalTestEnum.E1, 1), singletonRBEnumMap(LocalTestEnum.E2, 2.2)));
  }

  // as above, but use the merge overload that does NOT involve the map key
  @Test
  public void testMergeRBEnumMapValuesExpectingSameKeys() {
    BiFunction<Integer, Double, String> merger = (intValue1, doubleValue2) ->
        Strings.format("%s_%s", intValue1.toString(), doubleValue2.toString());

    TriConsumer<RBEnumMap<LocalTestEnum, Integer>, RBEnumMap<LocalTestEnum, Double>, RBEnumMap<LocalTestEnum, String>> asserter =
        (intMap1, doubleMap2, expectedMergedMap) ->
            assertEquals(
                expectedMergedMap,
                mergeRBEnumMapValuesExpectingSameKeys(
                    (v1, v2) -> merger.apply(v1, v2),
                    intMap1,
                    doubleMap2));

    asserter.accept(emptyRBEnumMap(LocalTestEnum.class), emptyRBEnumMap(LocalTestEnum.class), emptyRBEnumMap(LocalTestEnum.class));
    asserter.accept(
        singletonRBEnumMap(LocalTestEnum.E1, 1),
        singletonRBEnumMap(LocalTestEnum.E1, 2.2),
        singletonRBEnumMap(LocalTestEnum.E1, "1_2.2"));
    asserter.accept(
        rbEnumMapOf(
            LocalTestEnum.E1, 1,
            LocalTestEnum.E2, 3),
        rbEnumMapOf(
            LocalTestEnum.E1, 2.2,
            LocalTestEnum.E2, 4.4),
        rbEnumMapOf(
            LocalTestEnum.E1, "1_2.2",
            LocalTestEnum.E2, "3_4.4"));

    // if the maps have different numbers of keys, an exception will be thrown
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        merger, singletonRBEnumMap(LocalTestEnum.E1, 1), emptyRBEnumMap(LocalTestEnum.class)));
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        merger, emptyRBEnumMap(LocalTestEnum.class), singletonRBEnumMap(LocalTestEnum.E1, 2.2)));
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        merger,
        singletonRBEnumMap(LocalTestEnum.E1, 1),  // 1 key
        rbEnumMapOf(
            LocalTestEnum.E1, 1.1,            // 2 keys; throws
            LocalTestEnum.E2, 2.2)));

    // if the maps have the same numbers of keys, but the keys differ, this will again throw
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        merger, singletonRBEnumMap(LocalTestEnum.E1, 1), singletonRBEnumMap(LocalTestEnum.E2, 2.2)));
  }

  @Test
  public void testMergeRBEnumMapEntriesExpectingSameKeys_larger() {
    // use a StringBuilder to keep track of the order in which the keys are inserted into the RBEnumMap
    StringBuilder stringBuilder = new StringBuilder();

    TriFunction<LocalTestEnum, Integer, Double, String> merger = (stringKey, intValue1, doubleValue2) -> {
      stringBuilder.append(stringKey);
      return Strings.format("%s:%s_%s", stringKey, intValue1.toString(), doubleValue2);
    };

    // This test doesn't prove that the map entries would come out in a sorted order (which is not guaranteed)
    // but it does show that the merging works.
    assertEquals(
        newRBEnumMap(LocalTestEnum.class, rbMapOf(
            LocalTestEnum.E1, "E1:1_1.1",
            LocalTestEnum.E2, "E2:2_2.2",
            LocalTestEnum.E3, "E3:3_3.3",
            LocalTestEnum.E4, "E4:4_4.4",
            LocalTestEnum.E5, "E5:5_5.5",
            LocalTestEnum.E6, "E6:6_6.6",
            LocalTestEnum.E7, "E7:7_7.7",
            LocalTestEnum.E8, "E8:8_8.8",
            LocalTestEnum.E9, "E9:9_9.9")),
        // The map entries are inserted in a sorted order, which in practice seem to imply they will come out in
        // the same order. This is convenient, e.g. when printing.
        mergeRBEnumMapEntriesExpectingSameKeys(
            (key, v1, v2) -> merger.apply(key, v1, v2),
            newRBEnumMap(LocalTestEnum.class, rbMapOf( // map1 in random key order
                LocalTestEnum.E6, 6,
                LocalTestEnum.E3, 3,
                LocalTestEnum.E4, 4,
                LocalTestEnum.E1, 1,
                LocalTestEnum.E8, 8,
                LocalTestEnum.E5, 5,
                LocalTestEnum.E7, 7,
                LocalTestEnum.E9, 9,
                LocalTestEnum.E2, 2)),
            newRBEnumMap(LocalTestEnum.class, rbMapOf( // map2 in random key order
                LocalTestEnum.E8, 8.8,
                LocalTestEnum.E9, 9.9,
                LocalTestEnum.E7, 7.7,
                LocalTestEnum.E4, 4.4,
                LocalTestEnum.E5, 5.5,
                LocalTestEnum.E2, 2.2,
                LocalTestEnum.E1, 1.1,
                LocalTestEnum.E6, 6.6,
                LocalTestEnum.E3, 3.3))));

    // check that the entries were inserted in order
    assertEquals(
        "E1E2E3E4E5E6E7E8E9",
        stringBuilder.toString());
  }

  @Test
  public void testMergeRBEnumMapValuesExpectingSameKeys_larger() {
    // use a StringBuilder to keep track of the order in which the keys are inserted into the RBEnumMap
    StringBuilder stringBuilder = new StringBuilder();

    BiFunction<Integer, Double, String> merger = (intValue1, doubleValue2) -> {
      stringBuilder.append(intValue1);
      return Strings.format("%s_%s", intValue1.toString(), doubleValue2.toString());
    };

    // This test doesn't prove that the map entries would come out in a sorted order (which is not guaranteed)
    // but it does show that the merging works.
    assertEquals(
        newRBEnumMap(LocalTestEnum.class, rbMapOf(
            LocalTestEnum.E1, "1_1.1",
            LocalTestEnum.E2, "2_2.2",
            LocalTestEnum.E3, "3_3.3",
            LocalTestEnum.E4, "4_4.4",
            LocalTestEnum.E5, "5_5.5",
            LocalTestEnum.E6, "6_6.6",
            LocalTestEnum.E7, "7_7.7",
            LocalTestEnum.E8, "8_8.8",
            LocalTestEnum.E9, "9_9.9")),
        // The map entries are inserted in a sorted order, which in practice seems to imply that they will come
        // out in the same order. This is convenient, e.g. when printing.
        mergeRBEnumMapValuesExpectingSameKeys(
            (v1, v2) -> merger.apply(v1, v2),
            newRBEnumMap(LocalTestEnum.class, rbMapOf( // map1 in random key order
                LocalTestEnum.E6, 6,
                LocalTestEnum.E3, 3,
                LocalTestEnum.E4, 4,
                LocalTestEnum.E1, 1,
                LocalTestEnum.E8, 8,
                LocalTestEnum.E5, 5,
                LocalTestEnum.E7, 7,
                LocalTestEnum.E9, 9,
                LocalTestEnum.E2, 2)),
            newRBEnumMap(LocalTestEnum.class, rbMapOf( // map2 in random key order
                LocalTestEnum.E8, 8.8,
                LocalTestEnum.E9, 9.9,
                LocalTestEnum.E7, 7.7,
                LocalTestEnum.E4, 4.4,
                LocalTestEnum.E5, 5.5,
                LocalTestEnum.E2, 2.2,
                LocalTestEnum.E1, 1.1,
                LocalTestEnum.E6, 6.6,
                LocalTestEnum.E3, 3.3))));

    // check that the entries were inserted in order
    assertEquals(
        "123456789",
        stringBuilder.toString());
  }

  @Test
  public void testMergeRBEnumMapEntriesExpectingSameKeys_3mapOverload() {
    QuadriFunction<LocalTestEnum, Integer, Double, Boolean, String> merger = (key, doubleValue1, intValue2, boolValue3) ->
        Strings.format("%s:%s_%s[%s]", key, doubleValue1.toString(), intValue2.toString(), boolValue3.toString());

    QuadriConsumer<
        RBEnumMap<LocalTestEnum, Integer>,
        RBEnumMap<LocalTestEnum, Double>,
        RBEnumMap<LocalTestEnum, Boolean>,
        RBEnumMap<LocalTestEnum, String>>
        asserter = (intMap1, doubleMap2, booleanMap3, expectedMergedMap) ->
        assertEquals(
            expectedMergedMap,
            mergeRBEnumMapEntriesExpectingSameKeys(
                intMap1,
                doubleMap2,
                booleanMap3,
                (key, v1, v2, v3) -> merger.apply(key, v1, v2, v3)));

    asserter.accept(emptyRBEnumMap(LocalTestEnum.class), emptyRBEnumMap(LocalTestEnum.class), emptyRBEnumMap(LocalTestEnum.class), emptyRBEnumMap(LocalTestEnum.class));
    asserter.accept(
        singletonRBEnumMap(LocalTestEnum.E1, 1),
        singletonRBEnumMap(LocalTestEnum.E1, 2.2),
        singletonRBEnumMap(LocalTestEnum.E1, false),
        singletonRBEnumMap(LocalTestEnum.E1, "E1:1_2.2[false]"));
    asserter.accept(
        rbEnumMapOf(
            LocalTestEnum.E1, 1,
            LocalTestEnum.E2, 3),
        rbEnumMapOf(
            LocalTestEnum.E1, 2.2,
            LocalTestEnum.E2, 4.4),
        rbEnumMapOf(
            LocalTestEnum.E1, false,
            LocalTestEnum.E2, true),
        rbEnumMapOf(
            LocalTestEnum.E1, "E1:1_2.2[false]",
            LocalTestEnum.E2, "E2:3_4.4[true]"));

    // if the maps have different numbers of keys, an exception will be thrown
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        singletonRBEnumMap(LocalTestEnum.E1, 1),
        emptyRBEnumMap(LocalTestEnum.class),
        emptyRBEnumMap(LocalTestEnum.class),
        merger));
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        emptyRBEnumMap(LocalTestEnum.class), singletonRBEnumMap(LocalTestEnum.E1, 2.2), emptyRBEnumMap(LocalTestEnum.class), merger));
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        singletonRBEnumMap(LocalTestEnum.E1, 1),      // 1 key
        rbEnumMapOf(
            LocalTestEnum.E1, 1.1,                // 2 keys; throws
            LocalTestEnum.E2, 2.2),
        singletonRBEnumMap(LocalTestEnum.E1, false),  // 1 key
        merger));

    // if the maps have the same numbers of keys, but the keys differ, this will again throw
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        singletonRBEnumMap(LocalTestEnum.E1, 1), singletonRBEnumMap(LocalTestEnum.E2, 2.2), singletonRBEnumMap(LocalTestEnum.E1, false), merger));
  }

  @Test
  public void testMergeRBEnumMapValuesExpectingSameKeys_3mapOverload() {
    TriFunction<Integer, Double, Boolean, String> merger = (doubleValue1, intValue2, boolValue3) ->
        Strings.format("%s_%s[%s]", doubleValue1.toString(), intValue2.toString(), boolValue3.toString());

    QuadriConsumer<RBEnumMap<LocalTestEnum, Integer>, RBEnumMap<LocalTestEnum, Double>, RBEnumMap<LocalTestEnum, Boolean>, RBEnumMap<LocalTestEnum, String>>
        asserter = (intMap1, doubleMap2, booleanMap3, expectedMergedMap) ->
        assertEquals(
            expectedMergedMap,
            mergeRBEnumMapValuesExpectingSameKeys(
                intMap1,
                doubleMap2,
                booleanMap3,
                (v1, v2, v3) -> merger.apply(v1, v2, v3)));

    asserter.accept(emptyRBEnumMap(LocalTestEnum.class), emptyRBEnumMap(LocalTestEnum.class), emptyRBEnumMap(LocalTestEnum.class), emptyRBEnumMap(LocalTestEnum.class));
    asserter.accept(
        singletonRBEnumMap(LocalTestEnum.E1, 1),
        singletonRBEnumMap(LocalTestEnum.E1, 2.2),
        singletonRBEnumMap(LocalTestEnum.E1, false),
        singletonRBEnumMap(LocalTestEnum.E1, "1_2.2[false]"));
    asserter.accept(
        rbEnumMapOf(
            LocalTestEnum.E1, 1,
            LocalTestEnum.E2, 3),
        rbEnumMapOf(
            LocalTestEnum.E1, 2.2,
            LocalTestEnum.E2, 4.4),
        rbEnumMapOf(
            LocalTestEnum.E1, false,
            LocalTestEnum.E2, true),
        rbEnumMapOf(
            LocalTestEnum.E1, "1_2.2[false]",
            LocalTestEnum.E2, "3_4.4[true]"));

    // if the maps have different numbers of keys, an exception will be thrown
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        singletonRBEnumMap(LocalTestEnum.E1, 1), emptyRBEnumMap(LocalTestEnum.class), emptyRBEnumMap(LocalTestEnum.class), merger));
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        emptyRBEnumMap(LocalTestEnum.class), singletonRBEnumMap(LocalTestEnum.E1, 2.2), emptyRBEnumMap(LocalTestEnum.class), merger));
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        singletonRBEnumMap(LocalTestEnum.E1, 1),      // 1 key
        rbEnumMapOf(
            LocalTestEnum.E1, 1.1,                // 2 keys; throws
            LocalTestEnum.E2, 2.2),
        singletonRBEnumMap(LocalTestEnum.E1, false),  // 1 key
        merger));

    // if the maps have the same numbers of keys, but the keys differ, this will again throw
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        singletonRBEnumMap(LocalTestEnum.E1, 1), singletonRBEnumMap(LocalTestEnum.E2, 2.2), singletonRBEnumMap(LocalTestEnum.E1, false), merger));
  }

  @Test
  public void mergeRBEnumMapsByValue_generalCase() {
    assertEquals(
        rbEnumMapOf(
            LocalTestEnum.E1, "1.",
            LocalTestEnum.E2, "2_3",
            LocalTestEnum.E3, ".4"),
        mergeRBEnumMapsByValue(
            (v1, v2) -> Strings.format("%s_%s", v1, v2),
            v1 -> Strings.format("%s.", v1),
            v2 -> Strings.format(".%s", v2),
            rbEnumMapOf(
                LocalTestEnum.E1, "1",
                LocalTestEnum.E2, "2"),
            rbEnumMapOf(
                LocalTestEnum.E2, "3",
                LocalTestEnum.E3, "4")));
  }

  @Test
  public void mergeRBEnumMapsByTransformedValue_listOfRBEnumMapsOverload() {
    Function<List<RBEnumMap<LocalTestEnum, Integer>>, RBEnumMap<LocalTestEnum, String>> maker =
        listOfRBEnumMaps -> mergeRBEnumMapsByTransformedValue(
            (key, valueList) ->
                Strings.format("%s=%s", key, StringUtils.join(valueList, ":")),
            listOfRBEnumMaps);

    BiConsumer<List<RBEnumMap<LocalTestEnum, Integer>>, RBEnumMap<LocalTestEnum, String>> asserter =
        (listOfRBEnumMaps, expectedMergedMap) ->
            assertEquals(
                expectedMergedMap, maker.apply(listOfRBEnumMaps));

    // merging an empty list of maps throws an exception (unlike the RBMap equivalent)
    assertIllegalArgumentException( () -> maker.apply(emptyList()));

    // merging empty maps gives an empty map
    asserter.accept(
        ImmutableList.of(
            emptyRBEnumMap(LocalTestEnum.class),
            emptyRBEnumMap(LocalTestEnum.class),
            emptyRBEnumMap(LocalTestEnum.class)),
        emptyRBEnumMap(LocalTestEnum.class));

    // "merging" a single map
    asserter.accept(
        singletonList(
            rbEnumMapOf(
                LocalTestEnum.E1, 1,
                LocalTestEnum.E2, 2)),
        rbEnumMapOf(
            LocalTestEnum.E1, "E1=1",
            LocalTestEnum.E2, "E2=2"));

    // merging multiple overlapping maps
    asserter.accept(
        ImmutableList.of(
            rbEnumMapOf(
                LocalTestEnum.E1, 1,
                LocalTestEnum.E2, 2),
            rbEnumMapOf(
                LocalTestEnum.E2, 3,
                LocalTestEnum.E3, 4),
            emptyRBEnumMap(LocalTestEnum.class)),     // contributes nothing to the merged result
        rbEnumMapOf(
            LocalTestEnum.E1, "E1=1",
            LocalTestEnum.E2, "E2=2:3",
            LocalTestEnum.E3, "E3=4"));
  }

  @Test
  public void testMergeRBEnumMapsDisallowingOverlap_otherOverload() {
    assertEquals(
        rbEnumMapOf(
            LocalTestEnum.E1, intExplained(10,  1 * 10),
            LocalTestEnum.E2, intExplained(20,  2 * 10),
            LocalTestEnum.E3, intExplained(300, 3 * 100)),
        mergeRBEnumMapsDisallowingOverlap(
            v -> v * 10,
            v -> v * 100,
            rbEnumMapOf(
                LocalTestEnum.E1, 1,
                LocalTestEnum.E2, 2),
            singletonRBEnumMap(
                LocalTestEnum.E3, 3)));
    assertIllegalArgumentException( () -> mergeRBEnumMapsDisallowingOverlap(
        v -> v * DUMMY_POSITIVE_INTEGER,
        v -> v * DUMMY_POSITIVE_INTEGER,
        rbEnumMapOf(
            LocalTestEnum.E1, DUMMY_POSITIVE_INTEGER,
            LocalTestEnum.E2, DUMMY_POSITIVE_INTEGER),
        singletonRBEnumMap(
            LocalTestEnum.E2, DUMMY_POSITIVE_INTEGER)));
  }

  @Test
  public void test_mergeRBEnumMapsAllowingOverlapOnSimilarItemsOnly() {
    Function<Iterator<RBEnumMap<LocalTestEnum, Money>>, RBEnumMap<LocalTestEnum, Money>> maker = iidMapIterator ->
        mergeRBEnumMapsAllowingOverlapOnSimilarItemsOnly(
            iidMapIterator,
            (v1, v2) -> v1.almostEquals(v2, DEFAULT_EPSILON_1e_8));

    BiConsumer<Iterator<RBEnumMap<LocalTestEnum, Money>>, RBEnumMap<LocalTestEnum, Money>> asserter =
        (iidMapIterator, expectedResult) ->
            assertThat(
                maker.apply(iidMapIterator),
                rbEnumMapMatcher(expectedResult, f -> preciseValueMatcher(f, DEFAULT_EPSILON_1e_8)));

    assertIllegalArgumentException( () -> maker.apply(emptyIterator()));
    asserter.accept(singletonIterator(emptyRBEnumMap(LocalTestEnum.class)), emptyRBEnumMap(LocalTestEnum.class));
    asserter.accept(ImmutableList.<RBEnumMap<LocalTestEnum, Money>>of(emptyRBEnumMap(LocalTestEnum.class), emptyRBEnumMap(LocalTestEnum.class)).iterator(), emptyRBEnumMap(LocalTestEnum.class));

    // Does not complain if there is overlap but the values are similar
    rbSetOf(-1e-9, 0.0, 1e-9).forEach(epsilon -> {
      asserter.accept(
          ImmutableList.of(
                  rbEnumMapOf(
                      LocalTestEnum.E1, money(100),
                      LocalTestEnum.E2, money(200)),
                  rbEnumMapOf(
                      LocalTestEnum.E2, money(200 + epsilon),
                      LocalTestEnum.E3, money(300)))
              .iterator(),
          rbEnumMapOf(
              LocalTestEnum.E1, money(100),
              LocalTestEnum.E2, money(200),
              LocalTestEnum.E3, money(300)));
      asserter.accept(
          ImmutableList.of(
                  rbEnumMapOf(
                      LocalTestEnum.E1, money(100),
                      // This value gets processed first, so it ends up in the return value - NOT 200 below
                      LocalTestEnum.E2, money(200 + epsilon)),
                  rbEnumMapOf(
                      LocalTestEnum.E2, money(200),
                      LocalTestEnum.E3, money(300)))
              .iterator(),
          rbEnumMapOf(
              LocalTestEnum.E1, money(100),
              LocalTestEnum.E2, money(200 + epsilon),
              LocalTestEnum.E3, money(300)));
    });

    rbSetOf(-999.0, -1.0, -1e-7, 1e-7, 1.0, 999.0).forEach(largeEpsilon ->
        assertIllegalArgumentException( () ->
            mergeRBEnumMapsAllowingOverlapOnSimilarItemsOnly(
                ImmutableList.of(
                        rbEnumMapOf(
                            LocalTestEnum.E1, money(100),
                            LocalTestEnum.E2, money(200)),
                        rbEnumMapOf(
                            LocalTestEnum.E2, money(200 + largeEpsilon),
                            LocalTestEnum.E3, money(300)))
                    .iterator(),
                (v1, v2) -> v1.almostEquals(v2, DEFAULT_EPSILON_1e_8))));
  }

  @Test
  public void testMergeRBEnumMapEntriesExpectingExactOverlap() {
    BiFunction<RBEnumMap<LocalTestEnum, String>, RBEnumMap<LocalTestEnum, Boolean>, RBEnumMap<LocalTestEnum, String>> merger = (map1, map2) ->
        mergeRBEnumMapEntriesExpectingSameKeys(
            (intKey, stringValueFrom1, booleanFrom2) -> Strings.format("%s.%s.%s", intKey, stringValueFrom1, booleanFrom2),
            map1,
            map2);

    assertEquals(
        rbEnumMapOf(
            LocalTestEnum.E1, "E1._1.true",
            LocalTestEnum.E2, "E2._2.false"),
        merger.apply(
            rbEnumMapOf(
                LocalTestEnum.E1, "_1",
                LocalTestEnum.E2, "_2"),
            rbEnumMapOf(
                LocalTestEnum.E1, true,
                LocalTestEnum.E2, false)));

    assertEquals(
        singletonRBEnumMap(LocalTestEnum.E1, "E1._1.true"),
        merger.apply(
            singletonRBEnumMap(LocalTestEnum.E1, "_1"),
            singletonRBEnumMap(LocalTestEnum.E1, true)));

    //noinspection AssertEqualsBetweenInconvertibleTypes
    assertEquals(
        emptyRBEnumMap(LocalTestEnum.class),
        merger.apply(emptyRBEnumMap(LocalTestEnum.class), emptyRBEnumMap(LocalTestEnum.class)));

    // Must be exact overlap in the keys
    assertIllegalArgumentException( () -> merger.apply(
        rbEnumMapOf(
            LocalTestEnum.E1, "_1",
            LocalTestEnum.E2, "_2"),
        singletonRBEnumMap(
            LocalTestEnum.E1, true)));
    assertIllegalArgumentException( () -> merger.apply(
        singletonRBEnumMap(
            LocalTestEnum.E1, "_1"),
        rbEnumMapOf(
            LocalTestEnum.E1, true,
            LocalTestEnum.E2, false)));
  }

  @Test
  public void testDotProduct() {
    RBEnumMap<LocalTestEnum, Money> moneyMap = rbEnumMapOf(
        LocalTestEnum.E1, money(10.0),
        LocalTestEnum.E2, money(20.0),
        LocalTestEnum.E3, money(30.0));
    RBEnumMap<LocalTestEnum, Money> zeroMoneyMap = rbEnumMapOf(
        LocalTestEnum.E1, ZERO_MONEY,
        LocalTestEnum.E2, ZERO_MONEY,
        LocalTestEnum.E3, ZERO_MONEY);
    RBEnumMap<LocalTestEnum, PositiveMultiplier> multiplierMap = rbEnumMapOf(
        LocalTestEnum.E1, positiveMultiplier(1),
        LocalTestEnum.E2, positiveMultiplier(2),
        LocalTestEnum.E3, positiveMultiplier(3));
    RBEnumMap<LocalTestEnum, PositiveMultiplier> multiplierOneMap = rbEnumMapOf(
        LocalTestEnum.E1, POSITIVE_MULTIPLIER_1,
        LocalTestEnum.E2, POSITIVE_MULTIPLIER_1,
        LocalTestEnum.E3, POSITIVE_MULTIPLIER_1);

    TriConsumer<RBEnumMap<LocalTestEnum, ? extends RBNumeric>, RBEnumMap<LocalTestEnum, ? extends RBNumeric>, Double> asserter =
        (map1, map2, expectedDotProduct) ->
            assertEquals(
                expectedDotProduct,
                dotProductOfRBEnumMaps(map1, map2),
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
    assertIllegalArgumentException( () -> dotProductOfRBEnumMaps(
        singletonRBEnumMap(LocalTestEnum.E1, money(1)),
        singletonRBEnumMap(LocalTestEnum.E2, POSITIVE_MULTIPLIER_1)));
    assertIllegalArgumentException( () -> dotProductOfRBEnumMaps(
        rbEnumMapOf(
            LocalTestEnum.E1, money(1),
            LocalTestEnum.E2, money(2)),
        singletonRBEnumMap(LocalTestEnum.E1, POSITIVE_MULTIPLIER_1)));
  }

}
