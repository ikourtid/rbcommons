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

import java.time.TestEnum1234;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import static com.google.common.collect.Iterators.singletonIterator;
import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.RBEnumMapMergers.*;
import static com.rb.nonbiz.collections.RBEnumMapSimpleConstructors.emptyRBEnumMap;
import static com.rb.nonbiz.collections.RBEnumMapSimpleConstructors.rbEnumMapOf;
import static com.rb.nonbiz.collections.RBEnumMapSimpleConstructors.singletonRBEnumMap;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBEnumMapMatchers.rBEnumMapPreciseValueMatcher;
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

  private enum TestEnum12345 { E1, E2, E3, E4, E5 }
  
  @Test
  public void mergeIntoEithersMap_bothEmpty_returnsEmpty() {
    RBEnumMap<TestEnum12345, Integer> leftMap = emptyRBEnumMap(TestEnum12345.class);
    RBEnumMap<TestEnum12345, String> rightMap = emptyRBEnumMap(TestEnum12345.class);
    assertEquals(
        emptyRBEnumMap(TestEnum12345.class),
        mergeIntoEithersEnumMap(leftMap, rightMap));
  }

  @Test
  public void mergeIntoEithersMap_hasCommonKeys_throws() {
    assertIllegalArgumentException( () -> mergeIntoEithersEnumMap(
        singletonRBEnumMap(TestEnum12345.E4, DUMMY_POSITIVE_INTEGER),
        singletonRBEnumMap(TestEnum12345.E4, DUMMY_STRING)));
  }

  @Test
  public void mergeIntoEithersMap_generalCase() {
    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, Either.left(1),
            TestEnum12345.E2, Either.right(TestEnum12345.E2 ),
            TestEnum12345.E3, Either.left(3),
            TestEnum12345.E4, Either.right("d")),
        mergeIntoEithersEnumMap(
            rbEnumMapOf(
                TestEnum12345.E1, TestEnum12345.E1,
                TestEnum12345.E3, 3),
            rbEnumMapOf(
                TestEnum12345.E2, TestEnum12345.E2,
                TestEnum12345.E4, "d")));
  }

  @Test
  public void testMergeMapsByValue() {
    RBEnumMap<TestEnum12345, String> emptyStringMap = emptyRBEnumMap(TestEnum12345.class);
    BinaryOperator<String> joiner = (s1, s2) -> Strings.format("%s%s", s1, s2);
    assertEquals(
        emptyStringMap,
        mergeRBEnumMapsByValue(
            joiner,
            emptyStringMap,
            emptyStringMap));
    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, "a1",
            TestEnum12345.E2, "a2"),
        mergeRBEnumMapsByValue(
            joiner,
            rbEnumMapOf(
                TestEnum12345.E1, "a1",
                TestEnum12345.E2, "a2"),
            emptyStringMap));
    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, "a1",
            TestEnum12345.E2, "a2"),
        mergeRBEnumMapsByValue(
            joiner,
            emptyStringMap,
            rbEnumMapOf(
                TestEnum12345.E1, "a1",
                TestEnum12345.E2, "a2")));
    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, "a1b1",
            TestEnum12345.E2, "a2",
            TestEnum12345.E3, "b3"),
        mergeRBEnumMapsByValue(
            joiner,
            rbEnumMapOf(
                TestEnum12345.E1, "a1",
                TestEnum12345.E2, "a2"),
            rbEnumMapOf(
                TestEnum12345.E1, "b1",
                TestEnum12345.E3, "b3")));
    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, "a1b1c1",
            TestEnum12345.E2, "a2",
            TestEnum12345.E3, "b3",
            TestEnum12345.E4, "c4"),
        mergeRBEnumMapsByValue(
            joiner,
            rbEnumMapOf(
                TestEnum12345.E1, "a1",
                TestEnum12345.E2, "a2"),
            rbEnumMapOf(
                TestEnum12345.E1, "b1",
                TestEnum12345.E3, "b3"),
            rbEnumMapOf(
                TestEnum12345.E1, "c1",
                TestEnum12345.E4, "c4")));
  }

  @Test
  public void testMergeMapsByTransformedValue() {
    RBEnumMap<TestEnum12345, Double>  emptyDoubleMap = emptyRBEnumMap(TestEnum12345.class);
    RBEnumMap<TestEnum12345, Boolean> emptyBooleanMap = emptyRBEnumMap(TestEnum12345.class);
    RBEnumMap<TestEnum12345, String>  emptyStringMap = emptyRBEnumMap(TestEnum12345.class);
    BiFunction<RBEnumMap<TestEnum12345, Double>, RBEnumMap<TestEnum12345, Boolean>, RBEnumMap<TestEnum12345, String>> merger = (map1, map2) ->
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
            TestEnum12345.E1, "L1.1",
            TestEnum12345.E2, "L2.2"),
        merger.apply(
            rbEnumMapOf(
                TestEnum12345.E1, 1.1,
                TestEnum12345.E2, 2.2),
            emptyBooleanMap));
    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, "Rtrue",
            TestEnum12345.E2, "Rfalse"),
        merger.apply(
            emptyDoubleMap,
            rbEnumMapOf(
                TestEnum12345.E1, true,
                TestEnum12345.E2, false)));
    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, "1.1_true",
            TestEnum12345.E2, "L2.2",
            TestEnum12345.E3, "Rfalse"),
        merger.apply(
            rbEnumMapOf(
                TestEnum12345.E1, 1.1,
                TestEnum12345.E2, 2.2),
            rbEnumMapOf(
                TestEnum12345.E1, true,
                TestEnum12345.E3, false)));
  }

  @Test
  public void testMergeRBEnumMapsDisallowingOverlap() {
    RBEnumMap<TestEnum12345, String> emptyStringMap = emptyRBEnumMap(TestEnum12345.class);
    assertEquals(
        emptyStringMap,
        mergeRBEnumMapsDisallowingOverlap(
            emptyStringMap,
            emptyStringMap));
    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, "a1",
            TestEnum12345.E2, "a2"),
        mergeRBEnumMapsDisallowingOverlap(
            rbEnumMapOf(
                TestEnum12345.E1, "a1",
                TestEnum12345.E2, "a2"),
            emptyStringMap));
    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, "a1",
            TestEnum12345.E2, "a2"),
        mergeRBEnumMapsDisallowingOverlap(
            emptyStringMap,
            rbEnumMapOf(
                TestEnum12345.E1, "a1",
                TestEnum12345.E2, "a2")));
    assertIllegalArgumentException( () ->
        mergeRBEnumMapsDisallowingOverlap(
            singletonRBEnumMap(TestEnum12345.E1, TestEnum12345.E1 ),
            singletonRBEnumMap(TestEnum12345.E1, TestEnum12345.E1 )));
    assertIllegalArgumentException( () ->
        mergeRBEnumMapsDisallowingOverlap(
            singletonRBEnumMap(TestEnum12345.E1, TestEnum12345.E1 ),
            singletonRBEnumMap(TestEnum12345.E1, TestEnum12345.E2 )));
    assertIllegalArgumentException( () ->
        mergeRBEnumMapsDisallowingOverlap(
            rbEnumMapOf(
                TestEnum12345.E1, "a1",
                TestEnum12345.E2, "a2"),
            rbEnumMapOf(
                TestEnum12345.E1, "b1",
                TestEnum12345.E3, "b3")));
    assertIllegalArgumentException( () ->
        mergeRBEnumMapsDisallowingOverlap(
            rbEnumMapOf(
                TestEnum12345.E1, "a1",
                TestEnum12345.E2, "a2"),
            rbEnumMapOf(
                TestEnum12345.E1, "b1",
                TestEnum12345.E3, "b3"),
            rbEnumMapOf(
                TestEnum12345.E4, "c4",
                TestEnum12345.E5, "c5")));
  }

  @Test
  public void testMergeRBEnumMapEntriesExpectingSameKeys() {
    TriFunction<TestEnum12345, Integer, Double, String> merger = (key, intValue1, doubleValue2) ->
        Strings.format("%s:%s_%s", key, intValue1.toString(), doubleValue2.toString());

    TriConsumer<
        RBEnumMap<TestEnum12345, Integer>, 
        RBEnumMap<TestEnum12345, Double>, 
        RBEnumMap<TestEnum12345, String>> asserter =
        (intMap1, doubleMap2, expectedMergedMap) ->
            assertEquals(
                expectedMergedMap,
                mergeRBEnumMapEntriesExpectingSameKeys(
                    (key, v1, v2) -> merger.apply(key, v1, v2),
                    intMap1,
                    doubleMap2));

    asserter.accept(
        emptyRBEnumMap(TestEnum12345.class), 
        emptyRBEnumMap(TestEnum12345.class),
        emptyRBEnumMap(TestEnum12345.class));
    asserter.accept(
        singletonRBEnumMap(TestEnum12345.E1, 1),
        singletonRBEnumMap(TestEnum12345.E1, 2.2),
        singletonRBEnumMap(TestEnum12345.E1, "a:1_2.2"));
    asserter.accept(
        rbEnumMapOf(
            TestEnum12345.E1, 1,
            TestEnum12345.E2, 3),
        rbEnumMapOf(
            TestEnum12345.E1, 2.2,
            TestEnum12345.E2, 4.4),
        rbEnumMapOf(
            TestEnum12345.E1, "a:1_2.2",
            TestEnum12345.E2, "b:3_4.4"));

    // if the maps have different numbers of keys, an exception will be thrown
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        merger, singletonRBEnumMap(TestEnum12345.E1, 1), emptyRBEnumMap(TestEnum12345.class)));
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        merger, emptyRBEnumMap(TestEnum12345.class), singletonRBEnumMap(TestEnum12345.E1, 2.2)));
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        merger,
        singletonRBEnumMap(TestEnum12345.E1, 1),  // 1 key
        rbEnumMapOf(
            TestEnum12345.E1, 1.1,            // 2 keys; throws
            TestEnum12345.E2, 2.2)));

    // if the maps have the same numbers of keys, but the keys differ, this will again throw
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        merger, singletonRBEnumMap(TestEnum12345.E1, 1), singletonRBEnumMap(TestEnum12345.E2, 2.2)));
  }

  // as above, but use the merge overload that does NOT involve the map key
  @Test
  public void testMergeRBEnumMapValuesExpectingSameKeys() {
    BiFunction<Integer, Double, String> merger = (intValue1, doubleValue2) ->
        Strings.format("%s_%s", intValue1.toString(), doubleValue2.toString());

    TriConsumer<RBEnumMap<TestEnum12345, Integer>, RBEnumMap<TestEnum12345, Double>, RBEnumMap<TestEnum12345, String>> asserter =
        (intMap1, doubleMap2, expectedMergedMap) ->
            assertEquals(
                expectedMergedMap,
                mergeRBEnumMapValuesExpectingSameKeys(
                    (v1, v2) -> merger.apply(v1, v2),
                    intMap1,
                    doubleMap2));

    asserter.accept(emptyRBEnumMap(TestEnum12345.class), emptyRBEnumMap(TestEnum12345.class), emptyRBEnumMap(TestEnum12345.class));
    asserter.accept(
        singletonRBEnumMap(TestEnum12345.E1, 1),
        singletonRBEnumMap(TestEnum12345.E1, 2.2),
        singletonRBEnumMap(TestEnum12345.E1, "1_2.2"));
    asserter.accept(
        rbEnumMapOf(
            TestEnum12345.E1, 1,
            TestEnum12345.E2, 3),
        rbEnumMapOf(
            TestEnum12345.E1, 2.2,
            TestEnum12345.E2, 4.4),
        rbEnumMapOf(
            TestEnum12345.E1, "1_2.2",
            TestEnum12345.E2, "3_4.4"));

    // if the maps have different numbers of keys, an exception will be thrown
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        merger, singletonRBEnumMap(TestEnum12345.E1, 1), emptyRBEnumMap(TestEnum12345.class)));
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        merger, emptyRBEnumMap(TestEnum12345.class), singletonRBEnumMap(TestEnum12345.E1, 2.2)));
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        merger,
        singletonRBEnumMap(TestEnum12345.E1, 1),  // 1 key
        rbEnumMapOf(
            TestEnum12345.E1, 1.1,            // 2 keys; throws
            TestEnum12345.E2, 2.2)));

    // if the maps have the same numbers of keys, but the keys differ, this will again throw
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        merger, singletonRBEnumMap(TestEnum12345.E1, 1), singletonRBEnumMap(TestEnum12345.E2, 2.2)));
  }

  @Test
  public void testMergeSortedRBEnumMapEntriesExpectingSameKeys() {
    // use a StringBuilder to keep track of the order in which the keys are inserted into the RBEnumMap
    StringBuilder stringBuilder = new StringBuilder();

    TriFunction<TestEnum12345, Integer, Double, String> merger = (stringKey, intValue1, doubleValue2) -> {
      stringBuilder.append(stringKey);
      return Strings.format("%s:%s_%s", stringKey, intValue1.toString(), doubleValue2);
    };

    // This test doesn't prove that the map entries would come out in a sorted order (which is not guaranteed)
    // but it does show that the merging works.
    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, "a:1_1.1",
            TestEnum12345.E2, "b:2_2.2",
            TestEnum12345.E3, "c:3_3.3",
            "d", "d:4_4.4",
            "e", "e:5_5.5",
            "f", "f:6_6.6",
            "g", "g:7_7.7",
            "h", "h:8_8.8",
            "i", "i:9_9.9"),
        // The map entries are inserted in a sorted order, which in practice seem to imply they will come out in
        // the same order. This is convenient, e.g. when printing.
        mergeSortedRBEnumMapEntriesExpectingSameKeys(
            (key, v1, v2) -> merger.apply(key, v1, v2),
            String::compareTo,
            rbEnumMapOf(                // map1 in random key order
                "f", 6,
                TestEnum12345.E3, TestEnum12345.E3,
                "d", TestEnum12345.E4,
                TestEnum12345.E1, TestEnum12345.E1,
                "h", 8,
                "e", TestEnum12345.E5,
                "g", 7,
                "i", 9,
                TestEnum12345.E2, 2),
            rbEnumMapOf(                // map2 in random key order
                "h", 8.8,
                "i", 9.9,
                "g", 7.7,
                "d", 4.4,
                "e", 5.5,
                TestEnum12345.E2, 2.2,
                TestEnum12345.E1, 1.1,
                "f", 6.6,
                TestEnum12345.E3, 3.3)));

    // check that the entries were inserted in order
    assertEquals(
        "abcdefghi",
        stringBuilder.toString());
  }

  @Test
  public void testMergeSortedRBEnumMapValuesExpectingSameKeys() {
    // use a StringBuilder to keep track of the order in which the keys are inserted into the RBEnumMap
    StringBuilder stringBuilder = new StringBuilder();

    BiFunction<Integer, Double, String> merger = (intValue1, doubleValue2) -> {
      stringBuilder.append(intValue1);
      return Strings.format("%s_%s", intValue1.toString(), doubleValue2.toString());
    };

    // This test doesn't prove that the map entries would come out in a sorted order (which is not guaranteed)
    // but it does show that the merging works.
    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, "1_1.1",
            TestEnum12345.E2, "2_2.2",
            TestEnum12345.E3, "3_3.3",
            "d", "4_4.4",
            "e", "5_5.5",
            "f", "6_6.6",
            "g", "7_7.7",
            "h", "8_8.8",
            "i", "9_9.9"),
        // The map entries are inserted in a sorted order, which in practice seems to imply that they will come
        // out in the same order. This is convenient, e.g. when printing.
        mergeSortedRBEnumMapValuesExpectingSameKeys(
            (v1, v2) -> merger.apply(v1, v2),
            String::compareTo,
            rbEnumMapOf(                // map1 in random key order
                "f", 6,
                TestEnum12345.E3, TestEnum12345.E3,
                "d", TestEnum12345.E4,
                TestEnum12345.E1, TestEnum12345.E1,
                "h", 8,
                "e", TestEnum12345.E5,
                "g", 7,
                "i", 9,
                TestEnum12345.E2, 2),
            rbEnumMapOf(                // map2 in random key order
                "h", 8.8,
                "i", 9.9,
                "g", 7.7,
                "d", 4.4,
                "e", 5.5,
                TestEnum12345.E2, 2.2,
                TestEnum12345.E1, 1.1,
                "f", 6.6,
                TestEnum12345.E3, 3.3)));

    // check that the entries were inserted in order
    assertEquals(
        "123456789",
        stringBuilder.toString());
  }

  @Test
  public void testMergeRBEnumMapEntriesExpectingSameKeys_3mapOverload() {
    QuadriFunction<TestEnum12345, Integer, Double, Boolean, String> merger = (key, doubleValue1, intValue2, boolValue3) ->
        Strings.format("%s:%s_%s[%s]", key, doubleValue1.toString(), intValue2.toString(), boolValue3.toString());

    QuadriConsumer<
        RBEnumMap<TestEnum12345, Integer>,
        RBEnumMap<TestEnum12345, Double>,
        RBEnumMap<TestEnum12345, Boolean>, 
        RBEnumMap<TestEnum12345, String>>
        asserter = (intMap1, doubleMap2, booleanMap3, expectedMergedMap) ->
        assertEquals(
            expectedMergedMap,
            mergeRBEnumMapEntriesExpectingSameKeys(
                intMap1,
                doubleMap2,
                booleanMap3,
                (key, v1, v2, v3) -> merger.apply(key, v1, v2, v3)));

    asserter.accept(emptyRBEnumMap(TestEnum12345.class), emptyRBEnumMap(TestEnum12345.class), emptyRBEnumMap(TestEnum12345.class), emptyRBEnumMap(TestEnum12345.class));
    asserter.accept(
        singletonRBEnumMap(TestEnum12345.E1, 1),
        singletonRBEnumMap(TestEnum12345.E1, 2.2),
        singletonRBEnumMap(TestEnum12345.E1, false),
        singletonRBEnumMap(TestEnum12345.E1, "a:1_2.2[false]"));
    asserter.accept(
        rbEnumMapOf(
            TestEnum12345.E1, 1,
            TestEnum12345.E2, 3),
        rbEnumMapOf(
            TestEnum12345.E1, 2.2,
            TestEnum12345.E2, 4.4),
        rbEnumMapOf(
            TestEnum12345.E1, false,
            TestEnum12345.E2, true),
        rbEnumMapOf(
            TestEnum12345.E1, "a:1_2.2[false]",
            TestEnum12345.E2, "b:3_4.4[true]"));

    // if the maps have different numbers of keys, an exception will be thrown
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        singletonRBEnumMap(TestEnum12345.E1, 1),
        emptyRBEnumMap(TestEnum12345.class),
        emptyRBEnumMap(TestEnum12345.class),
        merger));
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        emptyRBEnumMap(TestEnum12345.class), singletonRBEnumMap(TestEnum12345.E1, 2.2), emptyRBEnumMap(TestEnum12345.class), merger));
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        singletonRBEnumMap(TestEnum12345.E1, 1),      // 1 key
        rbEnumMapOf(
            TestEnum12345.E1, 1.1,                // 2 keys; throws
            TestEnum12345.E2, 2.2),
        singletonRBEnumMap(TestEnum12345.E1, false),  // 1 key
        merger));

    // if the maps have the same numbers of keys, but the keys differ, this will again throw
    assertIllegalArgumentException( () -> mergeRBEnumMapEntriesExpectingSameKeys(
        singletonRBEnumMap(TestEnum12345.E1, 1), singletonRBEnumMap(TestEnum12345.E2, 2.2), singletonRBEnumMap(TestEnum12345.E1, false), merger));
  }

  @Test
  public void testMergeRBEnumMapValuesExpectingSameKeys_3mapOverload() {
    TriFunction<Integer, Double, Boolean, String> merger = (doubleValue1, intValue2, boolValue3) ->
        Strings.format("%s_%s[%s]", doubleValue1.toString(), intValue2.toString(), boolValue3.toString());

    QuadriConsumer<RBEnumMap<TestEnum12345, Integer>, RBEnumMap<TestEnum12345, Double>, RBEnumMap<TestEnum12345, Boolean>, RBEnumMap<TestEnum12345, String>>
        asserter = (intMap1, doubleMap2, booleanMap3, expectedMergedMap) ->
        assertEquals(
            expectedMergedMap,
            mergeRBEnumMapValuesExpectingSameKeys(
                intMap1,
                doubleMap2,
                booleanMap3,
                (v1, v2, v3) -> merger.apply(v1, v2, v3)));

    asserter.accept(emptyRBEnumMap(TestEnum12345.class), emptyRBEnumMap(TestEnum12345.class), emptyRBEnumMap(TestEnum12345.class), emptyRBEnumMap(TestEnum12345.class));
    asserter.accept(
        singletonRBEnumMap(TestEnum12345.E1, 1),
        singletonRBEnumMap(TestEnum12345.E1, 2.2),
        singletonRBEnumMap(TestEnum12345.E1, false),
        singletonRBEnumMap(TestEnum12345.E1, "1_2.2[false]"));
    asserter.accept(
        rbEnumMapOf(
            TestEnum12345.E1, 1,
            TestEnum12345.E2, 3),
        rbEnumMapOf(
            TestEnum12345.E1, 2.2,
            TestEnum12345.E2, 4.4),
        rbEnumMapOf(
            TestEnum12345.E1, false,
            TestEnum12345.E2, true),
        rbEnumMapOf(
            TestEnum12345.E1, "1_2.2[false]",
            TestEnum12345.E2, "3_4.4[true]"));

    // if the maps have different numbers of keys, an exception will be thrown
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        singletonRBEnumMap(TestEnum12345.E1, 1), emptyRBEnumMap(TestEnum12345.class), emptyRBEnumMap(TestEnum12345.class), merger));
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        emptyRBEnumMap(TestEnum12345.class), singletonRBEnumMap(TestEnum12345.E1, 2.2), emptyRBEnumMap(TestEnum12345.class), merger));
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        singletonRBEnumMap(TestEnum12345.E1, 1),      // 1 key
        rbEnumMapOf(
            TestEnum12345.E1, 1.1,                // 2 keys; throws
            TestEnum12345.E2, 2.2),
        singletonRBEnumMap(TestEnum12345.E1, false),  // 1 key
        merger));

    // if the maps have the same numbers of keys, but the keys differ, this will again throw
    assertIllegalArgumentException( () -> mergeRBEnumMapValuesExpectingSameKeys(
        singletonRBEnumMap(TestEnum12345.E1, 1), singletonRBEnumMap(TestEnum12345.E2, 2.2), singletonRBEnumMap(TestEnum12345.E1, false), merger));
  }

  @Test
  public void mergeRBEnumMapsByValue_generalCase() {
    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, "1.",
            TestEnum12345.E2, "2_3",
            TestEnum12345.E3, ".4"),
        mergeRBEnumMapsByValue(
            (v1, v2) -> Strings.format("%s_%s", v1, v2),
            v1 -> Strings.format("%s.", v1),
            v2 -> Strings.format(".%s", v2),
            rbEnumMapOf(
                TestEnum12345.E1, "1",
                TestEnum12345.E2, "2"),
            rbEnumMapOf(
                TestEnum12345.E2, "3",
                TestEnum12345.E3, "4")));
  }

  @Test
  public void mergeRBEnumMapsByTransformedValue_listOfRBEnumMapsOverload() {
    BiConsumer<List<RBEnumMap<TestEnum12345, Integer>>, RBEnumMap<TestEnum12345, String>> asserter = (listOfRBEnumMaps, expectedMergedMap) ->
        assertEquals(
            expectedMergedMap,
            mergeRBEnumMapsByTransformedValue(
                (key, valueList) ->
                    Strings.format("%s=%s", key, StringUtils.join(valueList, ":")),
                listOfRBEnumMaps));

    // merging an empty list of maps gives an empty map
    asserter.accept(
        emptyList(),
        emptyRBEnumMap(TestEnum12345.class));

    // merging empty maps gives an empty map
    asserter.accept(
        ImmutableList.of(
            emptyRBEnumMap(TestEnum12345.class),
            emptyRBEnumMap(TestEnum12345.class),
            emptyRBEnumMap(TestEnum12345.class)),
        emptyRBEnumMap(TestEnum12345.class));

    // "merging" a single map
    asserter.accept(
        singletonList(
          rbEnumMapOf(
              TestEnum12345.E1, 1,
              TestEnum12345.E2, 2)),
        rbEnumMapOf(
            TestEnum12345.E1, "a=1",
            TestEnum12345.E2, "b=2"));

    // merging multiple overlapping maps
    asserter.accept(
        ImmutableList.of(
            rbEnumMapOf(
                TestEnum12345.E1, 1,
                TestEnum12345.E2, 2),
            rbEnumMapOf(
                TestEnum12345.E2, 3,
                TestEnum12345.E3, 4),
            emptyRBEnumMap(TestEnum12345.class)),     // contributes nothing to the merged result
        rbEnumMapOf(
            TestEnum12345.E1, "a=1",
            TestEnum12345.E2, "b=2:3",
            TestEnum12345.E3, "c=4"));
  }

  @Test
  public void testMergeRBEnumMapsDisallowingOverlap_otherOverload() {
    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, intExplained(10,  1 * 10),
            TestEnum12345.E2, intExplained(20,  2 * 10),
            TestEnum12345.E3, intExplained(300, 3 * 100)),
        mergeRBEnumMapsDisallowingOverlap(
            v -> v * 10,
            v -> v * 100,
            rbEnumMapOf(
                TestEnum12345.E1, TestEnum12345.E1,
                TestEnum12345.E2, 2),
            singletonRBEnumMap(
                TestEnum12345.E3, 3)));
    assertIllegalArgumentException( () -> mergeRBEnumMapsDisallowingOverlap(
        v -> v * DUMMY_POSITIVE_INTEGER,
        v -> v * DUMMY_POSITIVE_INTEGER,
        rbEnumMapOf(
            TestEnum12345.E1, DUMMY_POSITIVE_INTEGER,
            TestEnum12345.E2, DUMMY_POSITIVE_INTEGER),
        singletonRBEnumMap(
            TestEnum12345.E2, DUMMY_POSITIVE_INTEGER)));
  }

  @Test
  public void test_mergeRBEnumMapsAllowingOverlapOnSimilarItemsOnly() {
    BiConsumer<Iterator<RBEnumMap<TestEnum12345, Money>>, RBEnumMap<TestEnum12345, Money>> asserter = (iidMapIterator, expectedResult) ->
        assertThat(
            mergeRBEnumMapsAllowingOverlapOnSimilarItemsOnly(
                iidMapIterator,
                (v1, v2) -> v1.almostEquals(v2, DEFAULT_EPSILON_1e_8)),
            rbEnumMapPreciseValueMatcher(expectedResult, DEFAULT_EPSILON_1e_8));

    asserter.accept(emptyIterator(), emptyRBEnumMap(TestEnum12345.class));
    asserter.accept(singletonIterator(emptyRBEnumMap(TestEnum12345.class)), emptyRBEnumMap(TestEnum12345.class));
    asserter.accept(ImmutableList.<RBEnumMap<TestEnum12345, Money>>of(emptyRBEnumMap(TestEnum12345.class), emptyRBEnumMap(TestEnum12345.class)).iterator(), emptyRBEnumMap(TestEnum12345.class));

    // Does not complain if there is overlap but the values are similar
    rbSetOf(-1e-9, 0.0, 1e-9).forEach(epsilon -> {
      asserter.accept(
          ImmutableList.of(
              rbEnumMapOf(
                TestEnum12345.E1, money(100),
                  TestEnum12345.E2, money(200)),
              rbEnumMapOf(
                  TestEnum12345.E2, money(200 + epsilon),
                  TestEnum12345.E3, money(300)))
              .iterator(),
          rbEnumMapOf(
              TestEnum12345.E1, money(100),
              TestEnum12345.E2, money(200),
              TestEnum12345.E3, money(300)));
      asserter.accept(
          ImmutableList.of(
              rbEnumMapOf(
                  TestEnum12345.E1, money(100),
                  // This value gets processed first, so it ends up in the return value - NOT 200 below
                  TestEnum12345.E2, money(200 + epsilon)),
              rbEnumMapOf(
                  TestEnum12345.E2, money(200),
                  TestEnum12345.E3, money(300)))
              .iterator(),
          rbEnumMapOf(
              TestEnum12345.E1, money(100),
              TestEnum12345.E2, money(200 + epsilon),
              TestEnum12345.E3, money(300)));
    });

    rbSetOf(-999.0, -1.0, -1e-7, 1e-7, 1.0, 999.0).forEach(largeEpsilon ->
        assertIllegalArgumentException( () ->
            mergeRBEnumMapsAllowingOverlapOnSimilarItemsOnly(
                ImmutableList.of(
                    rbEnumMapOf(
                        TestEnum12345.E1, money(100),
                        TestEnum12345.E2, money(200)),
                    rbEnumMapOf(
                        TestEnum12345.E2, money(200 + largeEpsilon),
                        TestEnum12345.E3, money(300)))
                    .iterator(),
                (v1, v2) -> v1.almostEquals(v2, DEFAULT_EPSILON_1e_8))));
  }

  @Test
  public void testMergeRBEnumMapEntriesExpectingExactOverlap() {
    BiFunction<RBEnumMap<TestEnum12345, String>, RBEnumMap<TestEnum12345, Boolean>, RBEnumMap<TestEnum12345, String>> merger = (map1, map2) ->
        mergeRBEnumMapEntriesExpectingSameKeys(
            (intKey, stringValueFrom1, booleanFrom2) -> Strings.format("%s.%s.%s", intKey, stringValueFrom1, booleanFrom2),
            map1,
            map2);

    assertEquals(
        rbEnumMapOf(
            TestEnum12345.E1, "1._1.true",
            TestEnum12345.E2, "2._2.false"),
        merger.apply(
            rbEnumMapOf(
                TestEnum12345.E1, "_1",
                TestEnum12345.E2, "_2"),
            rbEnumMapOf(
                TestEnum12345.E1, true,
                TestEnum12345.E2, false)));

    assertEquals(
        singletonRBEnumMap(TestEnum12345.E1, "1._1.true"),
        merger.apply(
            singletonRBEnumMap(TestEnum12345.E1, "_1"),
            singletonRBEnumMap(TestEnum12345.E1, true)));

    //noinspection AssertEqualsBetweenInconvertibleTypes
    assertEquals(
        emptyRBEnumMap(TestEnum12345.class),
        merger.apply(emptyRBEnumMap(TestEnum12345.class), emptyRBEnumMap(TestEnum12345.class)));

    // Must be exact overlap in the keys
    assertIllegalArgumentException( () -> merger.apply(
        rbEnumMapOf(
            TestEnum12345.E1, "_1",
            TestEnum12345.E2, "_2"),
        singletonRBEnumMap(
            TestEnum12345.E1, true)));
    assertIllegalArgumentException( () -> merger.apply(
        singletonRBEnumMap(
            TestEnum12345.E1, "_1"),
        rbEnumMapOf(
            TestEnum12345.E1, true,
            TestEnum12345.E2, false)));
  }

  @Test
  public void testDotProduct() {
    RBEnumMap<TestEnum12345, Money> moneyMap = rbEnumMapOf(
        TestEnum12345.E1, money(10.0),
        TestEnum12345.E2, money(20.0),
        TestEnum12345.E3, money(30.0));
    RBEnumMap<TestEnum12345, Money> zeroMoneyMap = rbEnumMapOf(
        TestEnum12345.E1, ZERO_MONEY,
        TestEnum12345.E2, ZERO_MONEY,
        TestEnum12345.E3, ZERO_MONEY);
    RBEnumMap<TestEnum12345, PositiveMultiplier> multiplierMap = rbEnumMapOf(
        TestEnum12345.E1, positiveMultiplier(1),
        TestEnum12345.E2, positiveMultiplier(2),
        TestEnum12345.E3, positiveMultiplier(3));
    RBEnumMap<TestEnum12345, PositiveMultiplier> multiplierOneMap = rbEnumMapOf(
        TestEnum12345.E1, POSITIVE_MULTIPLIER_1,
        TestEnum12345.E2, POSITIVE_MULTIPLIER_1,
        TestEnum12345.E3, POSITIVE_MULTIPLIER_1);

    TriConsumer<RBEnumMap<TestEnum12345, ? extends RBNumeric>, RBEnumMap<TestEnum12345, ? extends RBNumeric>, Double> asserter =
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
        singletonRBEnumMap(TestEnum12345.E1, money(1)),
        singletonRBEnumMap(TestEnum12345.E2, POSITIVE_MULTIPLIER_1)));
    assertIllegalArgumentException( () -> dotProductOfRBEnumMaps(
        rbEnumMapOf(
            TestEnum12345.E1, money(1),
            TestEnum12345.E2, money(2)),
        singletonRBEnumMap(TestEnum12345.E1, POSITIVE_MULTIPLIER_1)));
  }

}
