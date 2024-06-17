package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.rb.biz.types.Money;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.PreciseValue;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_E;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_F;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.biz.types.asset.InstrumentIds.instrumentIdArray;
import static com.rb.nonbiz.collections.IidMapConstructors.iidMapFromStream;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSet;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.longArrayMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.hasLongMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapPreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIidSetEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * This test class is not generic, but the publicly exposed typesafe matcher is.
 */
public class IidMapTest extends RBTestMatcher<IidMap<Money>> {

  @Test
  public void testKeySet() {
    IidMap<String> iidMap = iidMapOf(
        STOCK_A, "a",
        STOCK_B, "b",
        STOCK_C, "c");
    IidMap<String> empty = emptyIidMap();
    // Retrieving the keysets multiple times, because the first time we compute them and return them,
    // but the 2nd time we return the cached values.
    for (int i = 0; i < 3; i ++) {
      assertIidSetEquals(
          emptyIidSet(),
          empty.keySet());
      assertIidSetEquals(
          newIidSet(STOCK_A, STOCK_B, STOCK_C),
          iidMap.keySet());
    }
  }

  /**
   * An undocumented feature of an IidMap is that, if we ever use any operation that needs to sort instrument IDs,
   * then we will cache that ordering, and any future operation (including those that don't rely on order)
   * will also use the previously cached, ordered instrument IDs.
   * Strictly speaking, we shouldn't be testing such an implementation detail.
   * However, I wanted to make sure that my caching optimizations are working, so I had to add some code to test that.
   * So I might as well keep it.
   */
  @Test
  public void testKeySet_showsAsOrderedIfWeEverUseAnyOrderedOperation() {
    Consumer<Consumer<IidMap<Integer>>> assertCachesOrdering = operation -> {
      // The map intentionally starts out-of-order, to avoid a situation e.g. where iidMapOf
      // processes items in order and results in the instruments being stored in increasing order,
      // which would make the test succeed even in cases where the ordered operations aren't caching the
      // order of instrument IDs (which is what we're testing here).
      IidMap<Integer> map = iidMapOf(
          instrumentId(10), 1,
          instrumentId(60), 6,
          instrumentId(20), 2,
          instrumentId(40), 4,
          instrumentId(50), 5,
          instrumentId(30), 3);
      // causes keySet to be generated and the map to cache its items in sorted order
      operation.accept(map);
      for (int i = 0; i < 3; i++) {
        assertEquals(
            newArrayList(instrumentIdArray(10L, 20L, 30L, 40L, 50L, 60L)),
            // the 'newArrayList' consumes the keySet in its default iterator order,
            // so if this assertion succeeds, then it means the items are sorted.
            newArrayList(map.keySet()));
      }
    };

    assertCachesOrdering.accept(theMap -> theMap.forEachIidSortedEntry( (ignored1, ignored2) -> {}));
    assertCachesOrdering.accept(theMap -> {
      RBMap<InstrumentId, Integer> ignored = theMap.orderedTransformKeysAndValuesCopy(v -> v, v -> v);
    });
    assertCachesOrdering.accept(theMap -> {
      IidMap<Integer> ignored = theMap.orderedTransformEntriesCopy((k, v) -> v);
    });
    assertCachesOrdering.accept(theMap -> {
      Stream<InstrumentId> ignored = theMap.sortedInstrumentIdStream();
    });
    assertCachesOrdering.accept(theMap -> {
      Stream<String> ignored = theMap.toIidSortedTransformedEntriesStream( (instrumentId, value) -> DUMMY_STRING);
    });
    assertCachesOrdering.accept(theMap -> {
      Stream<String> ignored = theMap.toIidSortedTransformedValuesStream(instrumentId -> DUMMY_STRING);
    });
    assertCachesOrdering.accept(theMap -> {
      Stream<Integer> ignored = theMap.toIidSortedStream();
    });
  }

  @Test
  public void testOrderedTransformEntriesCopy() {
    List<String> stringList = newArrayList();

    assertThat(
        iidMapOf(
            instrumentId(10), 1,
            instrumentId(60), 6,
            instrumentId(20), 2,
            instrumentId(40), 4,
            instrumentId(50), 5,
            instrumentId(30), 3)
            .orderedTransformEntriesCopy( (instrumentId, value) -> {
              stringList.add(Strings.format("%s_%s", instrumentId.asLong(), value)); // e.g. "10_1"
              // We need to cast to int to avoid the result being a long
              return ((int) instrumentId.asLong()) + value; // e.g. 11
            }),
        iidMapEqualityMatcher(
            iidMapOf(
                // The ordering doesn't matter inside the iidMapOf constructor; just keeping it consistent
                instrumentId(10), 11,
                instrumentId(60), 66,
                instrumentId(20), 22,
                instrumentId(40), 44,
                instrumentId(50), 55,
                instrumentId(30), 33)));

    // Additionally, we must have performed the operations in a fixed order.
    assertThat(
        stringList,
        orderedListEqualityMatcher(ImmutableList.of("10_1", "20_2", "30_3", "40_4", "50_5", "60_6")));
  }

  @Test
  public void testForEachEntry() {
    MutableRBSet<String> mutableSet = newMutableRBSet();
    // I am adding elements in a shuffled order so it's less likely that they will all end up ordered by accident.
    iidMapOf(
        instrumentId(10), 1,
        instrumentId(60), 6,
        instrumentId(20), 2,
        instrumentId(40), 4,
        instrumentId(50), 5,
        instrumentId(30), 3)
        .forEachEntry( (instrumentId, str) -> mutableSet.addAssumingAbsent(
            Strings.format("%s_%s", instrumentId.asLong(), str)));
    assertEquals(
        rbSetOf("10_1", "20_2", "30_3", "40_4", "50_5", "60_6"),
        newRBSet(mutableSet));
  }

  @Test
  public void testForSortedEntry() {
    BiConsumer<Comparator<Pair<InstrumentId, Integer>>, List<String>> asserter =
        (comparator, expectedList) -> {

          List<String> list = newArrayList();
          // I am adding elements in a shuffled order so it's less likely that they will all end up ordered by accident.
          iidMapOf(
              instrumentId(10), 6,
              instrumentId(60), 1,
              instrumentId(20), 5,
              instrumentId(40), 3,
              instrumentId(50), 2,
              instrumentId(30), 4)
              .forEachSortedEntry(
                  (instrumentId, str) -> list.add(
                      Strings.format("%s_%s", instrumentId.asLong(), str)),
                  comparator);

          assertThat(
              list,
              orderedListEqualityMatcher(expectedList));
        };
    asserter.accept(comparing(pair -> pair.getLeft()),  ImmutableList.of("10_6", "20_5", "30_4", "40_3", "50_2", "60_1"));
    asserter.accept(comparing(pair -> pair.getRight()), ImmutableList.of("60_1", "50_2", "40_3", "30_4", "20_5", "10_6"));
  }

  @Test
  public void testForEachIidSortedEntry() {
    List<String> list = newArrayList();
    // I am adding elements in a shuffled order so it's less likely that they will all end up ordered by accident.
    iidMapOf(
        instrumentId(10), 1,
        instrumentId(60), 6,
        instrumentId(20), 2,
        instrumentId(40), 4,
        instrumentId(50), 5,
        instrumentId(30), 3)
        .forEachIidSortedEntry( (instrumentId, str) -> list.add(
            Strings.format("%s_%s", instrumentId.asLong(), str)));
    assertEquals(
        ImmutableList.of("10_1", "20_2", "30_3", "40_4", "50_5", "60_6"),
        list);
  }

  @Test
  public void testForEachSortedValue() {
    List<String> list = newArrayList();
    // Adding elements in a shuffled order so it's less likely that they will all end up ordered by accident.
    iidMapOf(
        instrumentId(10), 1,
        instrumentId(60), 6,
        instrumentId(20), 2,
        instrumentId(40), 4,
        instrumentId(50), 5,
        instrumentId(30), 3)
        .forEachIidSortedValue(str -> list.add(
            Strings.format("value=%s", str)));
    assertEquals(
        ImmutableList.of("value=1", "value=2", "value=3", "value=4", "value=5", "value=6"),
        list);
  }

  @Test
  public void testForEachValueSortedEntry() {
    List<String> list = newArrayList();
    // I am adding elements in a shuffled order so it's less likely that they will all end up ordered by accident.
    iidMapOf(
        instrumentId(10), 1,
        instrumentId(60), 6,
        instrumentId(20), 2,
        instrumentId(40), 4,
        instrumentId(50), 5,
        instrumentId(30), 3)
        .forEachValueSortedEntry(
            (instrumentId, str) -> list.add(Strings.format("%s_%s", instrumentId.asLong(), str)),
            (i1, i2) -> -1 * Integer.compare(i1, i2)); // comparing by inverse integer value in map
    assertEquals(
        ImmutableList.of("60_6", "50_5", "40_4", "30_3", "20_2", "10_1"),
        list);
  }

  @Test
  public void testForEachKeySortedEntry() {
    List<String> list = newArrayList();
    // I am adding elements in a shuffled order so it's less likely that they will all end up ordered by accident.
    iidMapOf(
        instrumentId(10), 1,
        instrumentId(60), 6,
        instrumentId(20), 2,
        instrumentId(40), 4,
        instrumentId(50), 5,
        instrumentId(30), 3)
        .forEachKeySortedEntry(
            (instrumentId, str) -> list.add(Strings.format("%s_%s", instrumentId.asLong(), str)),
            comparing(InstrumentId::asLong, reverseOrder()));
    assertEquals(
        ImmutableList.of("60_6", "50_5", "40_4", "30_3", "20_2", "10_1"),
        list);
  }

  @Test
  public void testForEachInKeyOrder() {
    StringBuilder sb = new StringBuilder();
    // I am adding elements in a shuffled order so it's less likely that they will all end up ordered by accident.
    iidMapOf(
        STOCK_A, "a",
        STOCK_F, "f",
        STOCK_B, "b",
        STOCK_D, "d",
        STOCK_E, "e",
        STOCK_C, "c")
        .forEachInKeyOrder( (keyLong, str) -> sb.append(str));
    assertEquals("abcdef", sb.toString());
  }

  @Test
  public void testSortedAndUnsortedKeyStreams() {
    IidMap<String> map = iidMapOf(
        instrumentId(1), DUMMY_STRING,
        instrumentId(6), DUMMY_STRING,
        instrumentId(2), DUMMY_STRING,
        instrumentId(4), DUMMY_STRING,
        instrumentId(5), DUMMY_STRING,
        instrumentId(3), DUMMY_STRING);
    assertIidSetEquals(
        newIidSet(map.instrumentIdStream().collect(Collectors.toSet())),
        newIidSet(
            instrumentId(1),
            instrumentId(2),
            instrumentId(3),
            instrumentId(4),
            instrumentId(5),
            instrumentId(6)));
    assertEquals(
        map.sortedInstrumentIdStream().collect(Collectors.toList()),
        ImmutableList.of(
            instrumentId(1),
            instrumentId(2),
            instrumentId(3),
            instrumentId(4),
            instrumentId(5),
            instrumentId(6)));
    assertThat(
        map.sortedKeysStream().toArray(),
        longArrayMatcher(new long[] { 1, 2, 3, 4, 5, 6 }));
  }

  @Test
  public void testTransformValuesCopy() {
    assertThat(
        emptyIidMap().transformValuesCopy(intValue -> Strings.format("_%s_", intValue)),
        iidMapEqualityMatcher(
            emptyIidMap()));
    assertThat(
        iidMapOf(
            STOCK_A, 1,
            STOCK_F, 6,
            STOCK_B, 2,
            STOCK_D, 4,
            STOCK_E, 5,
            STOCK_C, 3)
            .transformValuesCopy(intValue -> Strings.format("_%s_", intValue)),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_A, "_1_",
                STOCK_B, "_2_",
                STOCK_C, "_3_",
                STOCK_D, "_4_",
                STOCK_E, "_5_",
                STOCK_F, "_6_")));
  }

  @Test
  public void transformKeysAndValuesCopy() {
    assertEquals(
        emptyRBMap(),
        IidMapSimpleConstructors.<Integer>emptyIidMap()
            .transformKeysAndValuesCopy(key -> "x" + key.asLong(), intValue -> intValue + 10));
    assertEquals(
        rbMapOf("x111", 11, "x222", 12),
        iidMapOf(instrumentId(111),  1,  instrumentId(222),  2)
            .transformKeysAndValuesCopy(key -> "x" + key.asLong(), intValue -> intValue + 10));
  }

  @Test
  public void testTransformEntriesCopy() {
    BiFunction<InstrumentId, Integer, String> transformer =
        (instrumentId, intValue) -> Strings.format("%s_%s", instrumentId.asLong(), intValue);
    assertThat(
        IidMapSimpleConstructors.<Integer>emptyIidMap().transformEntriesCopy(transformer),
        iidMapEqualityMatcher(
            emptyIidMap()));
    assertThat(
        iidMapOf(
            instrumentId(10), 1,
            instrumentId(60), 6,
            instrumentId(20), 2,
            instrumentId(40), 4,
            instrumentId(50), 5,
            instrumentId(30), 3)
            .transformEntriesCopy(transformer),
        iidMapEqualityMatcher(
            iidMapOf(
                instrumentId(10), "10_1",
                instrumentId(20), "20_2",
                instrumentId(30), "30_3",
                instrumentId(40), "40_4",
                instrumentId(50), "50_5",
                instrumentId(60), "60_6")));
  }

  @Test
  public void testTransformEntriesAndFilterValuesCopy() {
    Predicate<String> transformedValuePredicate = v2 -> v2.contains("1") || v2.contains("3");
    BiConsumer<IidMap<Integer>, IidMap<String>> asserter = (integerIidMap, expectedResult) ->
        assertThat(
            integerIidMap.transformEntriesAndFilterValuesCopy(
                (instrumentId, intValue) -> Strings.format("%s_%s", instrumentId.asLong(), intValue),
                transformedValuePredicate),
            iidMapEqualityMatcher(
                expectedResult));

    asserter.accept(emptyIidMap(), emptyIidMap());

    asserter.accept(
        iidMapOf(
            instrumentId(10), 1,
            instrumentId(60), 6,
            instrumentId(20), 2,
            instrumentId(40), 4,
            instrumentId(50), 5,
            instrumentId(30), 3),
        iidMapOf(
            instrumentId(10), "10_1",
            instrumentId(30), "30_3"));
  }

  @Test
  public void testTransformEntriesToOptionalAndKeepIfPresent() {
    BiFunction<InstrumentId, Integer, Optional<String>> transformer =
        (instrumentId, intValue) -> intValue % 2 == 0
            ? Optional.empty()
            : Optional.of(Strings.format("%s_%s", instrumentId.asLong(), intValue));
    assertThat(
        IidMapSimpleConstructors.<Integer>emptyIidMap().transformEntriesToOptionalAndKeepIfPresentCopy(transformer),
        iidMapEqualityMatcher(
            emptyIidMap()));
    assertThat(
        iidMapOf(
            instrumentId(10), 1,
            instrumentId(60), 6,
            instrumentId(20), 2,
            instrumentId(40), 4,
            instrumentId(50), 5,
            instrumentId(30), 3)
            .transformEntriesToOptionalAndKeepIfPresentCopy(transformer),
        iidMapEqualityMatcher(
            iidMapOf(
                instrumentId(10), "10_1",
                instrumentId(30), "30_3",
                instrumentId(50), "50_5")));
  }

  @Test
  public void testToTransformedValuesStream() {
    BiConsumer<IidMap<Integer>, RBSet<String>> asserter = (input, expectedResult) ->
        assertThat(
            newRBSet(input
                .toTransformedValuesStream(v -> Strings.format("_%s_", v))
                .collect(Collectors.toSet())),
            rbSetEqualsMatcher(expectedResult));

    asserter.accept(emptyIidMap(), emptyRBSet());
    asserter.accept(
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2,
            STOCK_C, 3),
        rbSetOf("_1_", "_2_", "_3_"));
  }

  @Test
  public void testToTransformedEntriesStream() {
    BiConsumer<IidMap<Integer>, RBSet<String>> asserter = (input, expectedResult) ->
        assertThat(
            newRBSet(input
                .toTransformedEntriesStream( (instrumentId, v) -> Strings.format("%s_%s_", instrumentId.asLong(), v))
                .collect(Collectors.toSet())),
            rbSetEqualsMatcher(expectedResult));

    asserter.accept(emptyIidMap(), emptyRBSet());
    asserter.accept(
        iidMapOf(
            instrumentId(10), 1,
            instrumentId(20), 2,
            instrumentId(30), 3),
        rbSetOf("10_1_", "20_2_", "30_3_"));
  }

  @Test
  public void testToIidSortedStream() {
    BiConsumer<IidMap<Integer>, List<Integer>> asserter = (input, expectedResult) ->
        assertEquals(
            expectedResult,
            input
                .toIidSortedStream()
                .collect(Collectors.toList()));

    asserter.accept(emptyIidMap(), emptyList());
    asserter.accept(
        iidMapOf(
            instrumentId(11), 1,
            instrumentId(16), 6,
            instrumentId(12), 2,
            instrumentId(14), 4,
            instrumentId(15), 5,
            instrumentId(13), 3),
        ImmutableList.of(1, 2, 3, 4, 5, 6));
  }

  @Test
  public void testToSortedTransformedValuesStream() {
    BiConsumer<IidMap<Integer>, List<String>> asserter = (input, expectedResult) ->
        assertEquals(
            expectedResult,
            input
                .toIidSortedTransformedValuesStream(v -> Strings.format("_%s_", v))
                .collect(Collectors.toList()));

    asserter.accept(emptyIidMap(), emptyList());
    asserter.accept(
        iidMapOf(
            STOCK_A, 1,
            STOCK_F, 6,
            STOCK_B, 2,
            STOCK_D, 4,
            STOCK_E, 5,
            STOCK_C, 3),
        ImmutableList.of("_1_", "_2_", "_3_", "_4_", "_5_", "_6_"));
  }

  @Test
  public void testToSortedTransformedEntriesStream() {
    BiConsumer<IidMap<Integer>, List<String>> asserter = (input, expectedResult) ->
        assertEquals(
            expectedResult,
            input
                .toIidSortedTransformedEntriesStream( (instrumentId, intValue) ->
                    Strings.format("%s_%s", intValue, instrumentId.asLong()))
                .collect(Collectors.toList()));

    asserter.accept(emptyIidMap(), emptyList());
    asserter.accept(
        iidMapOf(
            instrumentId(10), 1,
            instrumentId(60), 6,
            instrumentId(20), 2,
            instrumentId(40), 4,
            instrumentId(50), 5,
            instrumentId(30), 3),
        ImmutableList.of("1_10", "2_20", "3_30", "4_40", "5_50", "6_60"));
  }

  @Test
  public void testToSortedTransformedEntriesStream_overloadThatUsesComparator() {
    BiConsumer<IidMap<Integer>, List<String>> asserter = (input, expectedResult) ->
        assertEquals(
            expectedResult,
            input
                .toSortedTransformedEntriesStream(
                    (instrumentId, intValue) -> Strings.format("%s_%s", intValue, instrumentId.asLong()),
                    (i1, i2) -> -1 * Integer.compare(i1, i2)) // comparing by inverse integer value in map
                .collect(Collectors.toList()));

    asserter.accept(emptyIidMap(), emptyList());
    asserter.accept(
        iidMapOf( // using random order to make sure the result is not due to some accidental determinism in IidMap logic
            instrumentId(10), 1,
            instrumentId(60), 6,
            instrumentId(20), 2,
            instrumentId(40), 4,
            instrumentId(50), 5,
            instrumentId(30), 3),
        ImmutableList.of("6_60", "5_50", "4_40", "3_30", "2_20", "1_10"));
  }

  @Test
  public void testToSortedTransformedEntriesStream_overloadThatUsesComparatorAndTwoTransformers() {
    BiConsumer<IidMap<Integer>, List<String>> asserter = (input, expectedResult) ->
        assertEquals(
            expectedResult,
            input
                .toSortedTransformedEntriesStream(
                    (instrumentId, intValue) -> Strings.format("%s_%s", intValue, instrumentId.asLong()),
                    (instrumentId, intValue) -> instrumentId.asLong() + intValue + 0.123, // convert to double for comparison
                    (double1, double2) -> -1 * Double.compare(double1, double2)) // comparing by inverse integer value in map
                .collect(Collectors.toList()));

    asserter.accept(emptyIidMap(), emptyList());
    asserter.accept(
        iidMapOf( // using random order to make sure the result is not due to some accidental determinism in IidMap logic
            instrumentId(10), 1,
            instrumentId(60), 6,
            instrumentId(20), 2,
            instrumentId(40), 4,
            instrumentId(50), 5,
            instrumentId(30), 3),
        ImmutableList.of("6_60", "5_50", "4_40", "3_30", "2_20", "1_10"));
  }

  @Test
  public void testFilterKeysAndTransformValuesCopy() {
    TriConsumer<IidMap<String>, IidMap<Integer>, Predicate<InstrumentId>> asserter =
        (expectedResult, inputMap, predicate) -> assertThat(
            expectedResult,
            iidMapEqualityMatcher(
                inputMap.filterKeysAndTransformValuesCopy(
                    intValue -> Strings.format("_%s", intValue + 100),
                    predicate)));

    asserter.accept(emptyIidMap(), emptyIidMap(), k -> true);
    asserter.accept(emptyIidMap(), emptyIidMap(), k -> false);

    // only A passes filter
    asserter.accept(
        singletonIidMap(STOCK_A, "_101"),
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2),
        k -> k.equals(STOCK_A));

    // both A and B pass filter
    asserter.accept(
        iidMapOf(
            STOCK_A, "_101",
            STOCK_B, "_102"),
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2),
        k -> rbSetOf(STOCK_A, STOCK_B).contains(k));

    // neither A nor B pass the filter
    asserter.accept(
        emptyIidMap(),
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2),
        k -> k.equals(STOCK_C));
  }

  @Test
  public void testFilterKeysAndTransformEntriesCopy() {
    TriConsumer<IidMap<String>, IidMap<Integer>, Predicate<InstrumentId>> asserter =
        (expectedResult, inputMap, predicate) -> assertThat(
            expectedResult,
            iidMapEqualityMatcher(
                inputMap.filterKeysAndTransformEntriesCopy(
                    (instrumentId, intValue) -> Strings.format("%s_%s", instrumentId.asLong(), intValue + 100),
                    predicate)));

    asserter.accept(emptyIidMap(), emptyIidMap(), k -> true);
    asserter.accept(emptyIidMap(), emptyIidMap(), k -> false);

    // only A passes filter
    asserter.accept(
        singletonIidMap(instrumentId(10), "10_101"),
        iidMapOf(
            instrumentId(10), 1,
            instrumentId(20), 2),
        k -> k.equals(instrumentId(10)));

    // both A and B pass filter
    asserter.accept(
        iidMapOf(
            instrumentId(10), "10_101",
            instrumentId(20), "20_102"),
        iidMapOf(
            instrumentId(10), 1,
            instrumentId(20), 2),
        k -> rbSetOf(instrumentId(10), instrumentId(20)).contains(k));

    // neither A nor B pass the filter
    asserter.accept(
        emptyIidMap(),
        iidMapOf(
            instrumentId(10), 1,
            instrumentId(20), 2),
        k -> k.equals(STOCK_C));
  }

  @Test
  public void testTransformAndFilterValuesCopy() {
    TriConsumer<IidMap<String>, IidMap<Integer>, Predicate<String>> asserter =
        (expectedResult, inputMap, predicate) -> assertThat(
            expectedResult,
            iidMapEqualityMatcher(
                inputMap.transformAndFilterValuesCopy(
                    intValue -> Strings.format("_%s", intValue + 100),
                    predicate)));

    asserter.accept(emptyIidMap(), emptyIidMap(), k -> true);
    asserter.accept(emptyIidMap(), emptyIidMap(), k -> false);

    // only A passes filter
    asserter.accept(
        singletonIidMap(STOCK_A, "_101"),
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2),
        v1 -> v1.equals("_101"));

    // both A and B pass filter
    asserter.accept(
        iidMapOf(
            STOCK_A, "_101",
            STOCK_B, "_102"),
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2),
        v1 -> v1.contains("_10"));

    // neither A nor B pass the filter
    asserter.accept(
        emptyIidMap(),
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2),
        v1 -> v1.contains("NOT_PRESENT"));
  }

  @Test
  public void testFilterValues() {
    TriConsumer<IidMap<Integer>, IidMap<Integer>, Predicate<Integer>> asserter =
        (expectedResult, inputMap, predicate) -> assertThat(
            expectedResult,
            iidMapEqualityMatcher(
                inputMap.filterValues(predicate)));

    asserter.accept(emptyIidMap(), emptyIidMap(), k -> true);
    asserter.accept(emptyIidMap(), emptyIidMap(), k -> false);

    // only odd values pass filter
    asserter.accept(
        iidMapOf(
            STOCK_A, 1,
            STOCK_C, 3),
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2,
            STOCK_C, 3,
            STOCK_D, 4),
        k -> k % 2 != 0);

    // all values pass the filter
    asserter.accept(
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2,
            STOCK_C, 3,
            STOCK_D, 4),
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2,
            STOCK_C, 3,
            STOCK_D, 4),
        k -> k < 999);

    // none of the values pass the filter
    asserter.accept(
        emptyIidMap(),
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2,
            STOCK_C, 3,
            STOCK_D, 4),
        k -> k > 999);
  }

  @Test
  public void testFilterKeys() {
    TriConsumer<IidMap<Integer>, IidMap<Integer>, Predicate<InstrumentId>> asserter =
        (expectedResult, inputMap, predicate) -> assertThat(
            expectedResult,
            iidMapEqualityMatcher(
                inputMap.filterKeys(predicate)));

    asserter.accept(emptyIidMap(), emptyIidMap(), k -> true);
    asserter.accept(emptyIidMap(), emptyIidMap(), k -> false);

    // only A passes the filter
    asserter.accept(
        singletonIidMap(
            STOCK_A, 1),
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2,
            STOCK_C, 3,
            STOCK_D, 4),
        k -> k.equals(STOCK_A));

    // all values pass the filter
    asserter.accept(
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2,
            STOCK_C, 3,
            STOCK_D, 4),
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2,
            STOCK_C, 3,
            STOCK_D, 4),
        k -> !k.equals(STOCK_E));

    // none of the values pass the filter
    asserter.accept(
        emptyIidMap(),
        iidMapOf(
            STOCK_A, 1,
            STOCK_B, 2,
            STOCK_C, 3,
            STOCK_D, 4),
        k -> k.equals(STOCK_E));
  }

  @Test
  public void testCopyWithInstrumentIdRemoved() {
    IidMap<Integer> empty = emptyIidMap();
    IidMap<Integer> a = singletonIidMap(STOCK_A, 1);
    IidMap<Integer> b = singletonIidMap(STOCK_B, 2);
    IidMap<Integer> ab = iidMapOf(
        STOCK_A, 1,
        STOCK_B, 2);
    assertIllegalArgumentException( () ->     a.copyWithInstrumentIdRemoved(STOCK_B));
    assertIllegalArgumentException( () -> empty.copyWithInstrumentIdRemoved(STOCK_B));

    TriConsumer<IidMap<Integer>, IidMap<Integer>, InstrumentId> asserter = (expectedResult, initialMap, iidRemove) ->
        assertThat(
            initialMap.copyWithInstrumentIdRemoved(iidRemove),
            iidMapEqualityMatcher(expectedResult));

    asserter.accept(empty,  a, STOCK_A);
    asserter.accept(a,     ab, STOCK_B);
    asserter.accept(b,     ab, STOCK_A);
  }

  @Test
  public void testCopyWithPresentInstrumentIdsRemoved() {
    IidMap<Integer> empty = emptyIidMap();
    IidMap<Integer> a = singletonIidMap(STOCK_A, 1);
    IidMap<Integer> b = singletonIidMap(STOCK_B, 2);
    IidMap<Integer> ab = iidMapOf(
        STOCK_A, 1,
        STOCK_B, 2);
    assertIllegalArgumentException( () -> a    .copyWithPresentInstrumentIdsRemoved(singletonIidSet(STOCK_B)));
    assertIllegalArgumentException( () -> empty.copyWithPresentInstrumentIdsRemoved(singletonIidSet(STOCK_B)));
    assertIllegalArgumentException( () -> ab   .copyWithPresentInstrumentIdsRemoved(iidSetOf(STOCK_B, STOCK_C)));

    TriConsumer<IidMap<Integer>, IidMap<Integer>, IidSet> asserter = (expectedResult, initialMap, keysToRemove) ->
        assertThat(
            initialMap.copyWithPresentInstrumentIdsRemoved(keysToRemove),
            iidMapEqualityMatcher(expectedResult));

    asserter.accept(empty,  a, singletonIidSet(STOCK_A));
    asserter.accept(a,      a, emptyIidSet());
    asserter.accept(ab,    ab, emptyIidSet());
    asserter.accept(a,     ab, singletonIidSet(STOCK_B));
    asserter.accept(b,     ab, singletonIidSet(STOCK_A));
    asserter.accept(empty, ab, iidSetOf(STOCK_A, STOCK_B));
  }

  @Test
  public void testCopyWithPossiblyAbsentInstrumentIdsRemoved() {
    IidMap<Integer> empty = emptyIidMap();
    IidMap<Integer> a = singletonIidMap(STOCK_A, 1);
    IidMap<Integer> b = singletonIidMap(STOCK_B, 2);
    IidMap<Integer> ab = iidMapOf(
        STOCK_A, 1,
        STOCK_B, 2);
    TriConsumer<IidMap<Integer>, IidMap<Integer>, IidSet> asserter = (expectedResult, initialMap, keysToRemove) ->
        assertThat(
            initialMap.copyWithPossiblyAbsentInstrumentIdsRemoved(keysToRemove),
            iidMapEqualityMatcher(expectedResult));

    asserter.accept(a,         a, singletonIidSet(STOCK_B));
    asserter.accept(empty, empty, singletonIidSet(STOCK_B));
    asserter.accept(a,        ab, iidSetOf(STOCK_B, STOCK_C));

    // The remaining behavior is just like with copyWithPresentInstrumentIdsRemoved
    asserter.accept(empty,     a, singletonIidSet(STOCK_A));
    asserter.accept(a,         a, emptyIidSet());
    asserter.accept(ab,       ab, emptyIidSet());
    asserter.accept(a,        ab, singletonIidSet(STOCK_B));
    asserter.accept(b,        ab, singletonIidSet(STOCK_A));
    asserter.accept(empty,    ab, iidSetOf(STOCK_A, STOCK_B));
  }

  @Test
  public void testToRBMap() {
    assertThat(
        emptyIidMap().toRBMap(),
        rbMapMatcher(emptyRBMap(), f -> typeSafeEqualTo(f)));

    assertThat(
        iidMapOf(
            instrumentId(1), money(0.1),
            instrumentId(2), money(0.2))
            .toRBMap(),
        rbMapPreciseValueMatcher(
            rbMapOf(
                instrumentId(1), money(0.1),
                instrumentId(2), money(0.2)),
            DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testToStringKeyedRBMap() {
    assertThat(
        emptyIidMap().toStringKeyedRBMap(),
        rbMapMatcher(emptyRBMap(), f -> typeSafeEqualTo(f)));

    assertThat(
        iidMapOf(
            instrumentId(1), money(0.1),
            instrumentId(2), money(0.2))
            .toStringKeyedRBMap(),
        rbMapPreciseValueMatcher(
            rbMapOf(
                "1", money(0.1),
                "2", money(0.2)),
            DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void instrumentIdStreamIsNotDeterministic() {
    List<InstrumentId> instrumentIdsInRandomOrder = LongStream
        .range(1, 100)
        .mapToObj(i -> instrumentId((long) (1_000_000_000 * Math.random())))
        .collect(Collectors.toList());
    List<InstrumentId> instrumentIdsInIncreasingOrder = instrumentIdsInRandomOrder
        .stream()
        .sorted()
        .collect(Collectors.toList());

    IidMap<String> mapConstructedInRandomOrder = iidMapFromStream(
        instrumentIdsInRandomOrder.stream(), v -> v, v -> Long.toString(v.asLong()));

    // I was hoping that we can just use an iidmap's key stream with the expectation that it's deterministic.
    // Unfortunately, as this test shows, there's no determinism. This shows that the order is neither
    // increasing/decreasing, nor the same (or reverse) of the order the items got inserted. So we can't rely on it.
    rbSetOf(
        instrumentIdsInIncreasingOrder,
        Lists.reverse(instrumentIdsInIncreasingOrder),
        instrumentIdsInRandomOrder,
        Lists.reverse(instrumentIdsInRandomOrder))
        .forEach(instrumentIdsList ->
            assertThat(
                mapConstructedInRandomOrder.instrumentIdStream().collect(Collectors.toList()),
                not(orderedListEqualityMatcher(instrumentIdsList))));
  }

  @Override
  public IidMap<Money> makeTrivialObject() {
    return emptyIidMap();
  }

  @Override
  public IidMap<Money> makeNontrivialObject() {
    return iidMapOf(
        STOCK_A, money(0.1),
        STOCK_B, money(0.2),
        STOCK_C, money(0.3));
  }

  @Override
  public IidMap<Money> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return iidMapOf(
        STOCK_A, money(0.1 + e),
        STOCK_B, money(0.2 + e),
        STOCK_C, money(0.3 + e));
  }

  @Override
  protected boolean willMatch(IidMap<Money> expected, IidMap<Money> actual) {
    return iidMapPreciseValueMatcher(expected, DEFAULT_EPSILON_1e_8).matches(actual);
  }

  public static <V> TypeSafeMatcher<IidMap<V>> iidMapMatcher(
      IidMap<V> expected, MatcherGenerator<V> matcherGenerator) {
    return makeMatcher(expected, actual ->
        hasLongMapMatcher(expected, matcherGenerator).matches(actual));
  }

  public static <V> TypeSafeMatcher<IidMap<V>> iidMapEqualityMatcher(
      IidMap<V> expected) {
    return makeMatcher(expected, actual ->
        hasLongMapMatcher(expected, f -> typeSafeEqualTo(f)).matches(actual));
  }

  public static TypeSafeMatcher<IidMap<Double>> iidMapDoubleMatcher(
      IidMap<Double> expected, Epsilon epsilon) {
    return makeMatcher(expected, actual ->
        hasLongMapMatcher(expected, f -> doubleAlmostEqualsMatcher(f, epsilon)).matches(actual));
  }

  public static <V extends PreciseValue> TypeSafeMatcher<IidMap<V>> iidMapPreciseValueMatcher(
      IidMap<V> expected, Epsilon epsilon) {
    return makeMatcher(expected, actual ->
        hasLongMapMatcher(expected, f -> preciseValueMatcher(f, epsilon)).matches(actual));
  }

}
