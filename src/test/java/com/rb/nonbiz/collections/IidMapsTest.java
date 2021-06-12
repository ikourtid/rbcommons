package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provider;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidMapVisitors.PairOfIidSetAndIidMapVisitor;
import com.rb.nonbiz.collections.IidMapVisitors.TwoIidMapsVisitor;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.rb.biz.marketdata.FakeInstruments.*;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.biz.types.asset.InstrumentIds.parseInstrumentId;
import static com.rb.nonbiz.collections.IidMapConstructors.iidMapFromCollection;
import static com.rb.nonbiz.collections.IidMapConstructors.iidMapFromFilteredSet;
import static com.rb.nonbiz.collections.IidMapConstructors.iidMapFromIterator;
import static com.rb.nonbiz.collections.IidMapConstructors.iidMapFromIteratorWithExpectedSize;
import static com.rb.nonbiz.collections.IidMapConstructors.iidMapFromRBMap;
import static com.rb.nonbiz.collections.IidMapConstructors.iidMapFromSet;
import static com.rb.nonbiz.collections.IidMapConstructors.iidMapFromStream;
import static com.rb.nonbiz.collections.IidMapConstructors.iidMapFromStreamWithExpectedSize;
import static com.rb.nonbiz.collections.IidMapMergers.mergeIidMapsByValue;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.collections.IidMapTest.iidMapEqualityMatcher;
import static com.rb.nonbiz.collections.IidMapTest.iidMapMatcher;
import static com.rb.nonbiz.collections.IidMapVisitors.visitInstrumentsOfThreeIidMaps;
import static com.rb.nonbiz.collections.IidMaps.filterForPresentValuesAndTransformValuesCopy;
import static com.rb.nonbiz.collections.IidMaps.getWhenAtMostOneIidMapIsNonEmpty;
import static com.rb.nonbiz.collections.IidMaps.invertMapOfDisjointIidSets;
import static com.rb.nonbiz.collections.IidMaps.lockValues;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSet;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentId;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentIdMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalNonEmpty;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_PRICE;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class IidMapsTest {

  @Test
  public void testNoDuplicates() {
    assertIllegalArgumentException( () -> iidMapOf(
        STOCK_A, DUMMY_PRICE,
        STOCK_A, DUMMY_PRICE));

    assertIllegalArgumentException( () -> iidMapOf(
        STOCK_A, DUMMY_PRICE,
        STOCK_B, DUMMY_PRICE,
        STOCK_A, DUMMY_PRICE));
  }

  @Test
  public void testIidMapFromRBMap() {
    BiConsumer<RBMap<InstrumentId, String>, IidMap<String>> asserter = (rbMap, iidMap) ->
        assertThat(
            iidMapFromRBMap(rbMap),
            iidMapEqualityMatcher(iidMap));

    asserter.accept(emptyRBMap(), emptyIidMap());
    asserter.accept(
        rbMapOf(
            STOCK_A, "_a",
            STOCK_B, "_b"),
        iidMapOf(
            STOCK_A, "_a",
            STOCK_B, "_b"));
  }

  @Test
  public void testIidMapFromSet() {
    assertThat(
        iidMapFromSet(
            iidSetOf(STOCK_A, STOCK_B),
            instrumentId -> testHasInstrumentId(instrumentId, 3)),
        iidMapMatcher(
            iidMapOf(
                STOCK_A, testHasInstrumentId(STOCK_A, 3),
                STOCK_B, testHasInstrumentId(STOCK_B, 3)),
            f -> testHasInstrumentIdMatcher(f)));
    assertThat(
        iidMapFromSet(
            emptyIidSet(),
            instrumentId -> testHasInstrumentId(instrumentId, 3)),
        iidMapMatcher(
            emptyIidMap(),
            f -> testHasInstrumentIdMatcher(f)));
  }

  @Test
  public void testIidMapFromCollection() {
    assertThat(
        iidMapFromCollection(
            Collections.<TestHasInstrumentId>emptyList()),
        iidMapMatcher(
            emptyIidMap(),
            f -> testHasInstrumentIdMatcher(f)));
    assertThat(
        iidMapFromCollection(
            singletonList(testHasInstrumentId(STOCK_A, 1.1))),
        iidMapMatcher(
            singletonIidMap(
                STOCK_A, testHasInstrumentId(STOCK_A, 1.1)),
            f -> testHasInstrumentIdMatcher(f)));
    assertThat(
        iidMapFromCollection(
            ImmutableList.of(
                testHasInstrumentId(STOCK_A, 1.1),
                testHasInstrumentId(STOCK_B, 2.2))),
        iidMapMatcher(
            iidMapOf(
                STOCK_A, testHasInstrumentId(STOCK_A, 1.1),
                STOCK_B, testHasInstrumentId(STOCK_B, 2.2)),
            f -> testHasInstrumentIdMatcher(f)));
    assertIllegalArgumentException( () -> iidMapFromCollection(
        ImmutableList.of(
            testHasInstrumentId(STOCK_A, 1.1),
            testHasInstrumentId(STOCK_A, 1.1))));
    assertIllegalArgumentException( () -> iidMapFromCollection(
        ImmutableList.of(
            testHasInstrumentId(STOCK_A, 1.1),
            testHasInstrumentId(STOCK_A, 2.2))));
  }

  @Test
  public void testIidMapFromStream() {
    assertThat(
        iidMapFromStream(
            Stream.<TestHasInstrumentId>empty()),
        iidMapMatcher(
            emptyIidMap(),
            f -> testHasInstrumentIdMatcher(f)));
    assertThat(
        iidMapFromStream(
            Stream.of(testHasInstrumentId(STOCK_A, 1.1))),
        iidMapMatcher(
            singletonIidMap(
                STOCK_A, testHasInstrumentId(STOCK_A, 1.1)),
            f -> testHasInstrumentIdMatcher(f)));
    assertThat(
        iidMapFromStream(
            Stream.of(
                testHasInstrumentId(STOCK_A, 1.1),
                testHasInstrumentId(STOCK_B, 2.2))),
        iidMapMatcher(
            iidMapOf(
                STOCK_A, testHasInstrumentId(STOCK_A, 1.1),
                STOCK_B, testHasInstrumentId(STOCK_B, 2.2)),
            f -> testHasInstrumentIdMatcher(f)));
    assertIllegalArgumentException( () -> iidMapFromStream(
        Stream.of(
            testHasInstrumentId(STOCK_A, 1.1),
            testHasInstrumentId(STOCK_A, 1.1))));
    assertIllegalArgumentException( () -> iidMapFromStream(
        Stream.of(
            testHasInstrumentId(STOCK_A, 1.1),
            testHasInstrumentId(STOCK_A, 2.2))));
  }

  @Test
  public void testIidMapFromStreamOfOptionals() {
    assertThat(
        IidMapConstructors.iidMapFromStreamOfOptionals(
            Stream.of(
                Optional.empty(),
                Optional.of(testHasInstrumentId(STOCK_A, 1.1)),
                Optional.empty(),
                Optional.of(testHasInstrumentId(STOCK_B, 2.2)),
                Optional.empty())),
        iidMapMatcher(
            iidMapOf(
                STOCK_A, testHasInstrumentId(STOCK_A, 1.1),
                STOCK_B, testHasInstrumentId(STOCK_B, 2.2)),
            f -> testHasInstrumentIdMatcher(f)));

    assertThat(
        IidMapConstructors.iidMapFromStreamOfOptionals(
            Stream.of(
                Optional.empty(),
                Optional.empty(),
                Optional.empty())),
        iidMapMatcher(
            IidMapSimpleConstructors.<TestHasInstrumentId>emptyIidMap(),
            f -> testHasInstrumentIdMatcher(f)));

    assertIllegalArgumentException( () -> IidMapConstructors.iidMapFromStreamOfOptionals(
        Stream.of(
            Optional.empty(),
            Optional.of(testHasInstrumentId(STOCK_A, 1.1)),
            Optional.empty(),
            Optional.of(testHasInstrumentId(STOCK_A, 2.2)), // same instrument ID key
            Optional.empty())));
  }

  @Test
  public void testIidMapFromFilteredSet() {
    // both A and B map to non-empty optionals => result map has both A and B
    assertThat(
        iidMapFromFilteredSet(
            iidSetOf(STOCK_A, STOCK_B),
            instrumentId -> Optional.of(testHasInstrumentId(instrumentId, 3))),
        iidMapMatcher(
            iidMapOf(
                STOCK_A, testHasInstrumentId(STOCK_A, 3),
                STOCK_B, testHasInstrumentId(STOCK_B, 3)),
            f -> testHasInstrumentIdMatcher(f)));

    // A maps to non-empty optional, but B to empty => result map has A only
    assertThat(
        iidMapFromFilteredSet(
            iidSetOf(STOCK_A, STOCK_B),
            instrumentId -> instrumentId.equals(STOCK_A)
                ? Optional.of(testHasInstrumentId(instrumentId, 3))
                : Optional.empty()),
        iidMapMatcher(
            singletonIidMap(
                STOCK_A, testHasInstrumentId(STOCK_A, 3)),
            f -> testHasInstrumentIdMatcher(f)));

    // passing in empty map => result is empty
    assertThat(
        iidMapFromFilteredSet(
            emptyIidSet(),
            instrumentId -> Optional.of(testHasInstrumentId(instrumentId, 3))),
        iidMapMatcher(
            emptyIidMap(),
            f -> testHasInstrumentIdMatcher(f)));

    // passing in non-empty map, but values all map to Optional.empty() => result map is empty
    assertThat(
        iidMapFromFilteredSet(
            iidSetOf(STOCK_A, STOCK_B),
            instrumentId -> Optional.<TestHasInstrumentId>empty()),
        iidMapMatcher(
            emptyIidMap(),
            f -> testHasInstrumentIdMatcher(f)));
  }

  @Test
  public void sizeHintIsJustAHint_ifSmallThenThingsStillWork() {
    MutableIidMap<String> mutableMap = newMutableIidMapWithExpectedSize(1);
    mutableMap.putAssumingAbsent(STOCK_A, "AA");
    mutableMap.putAssumingAbsent(STOCK_B, "BB");
    mutableMap.putAssumingAbsent(STOCK_C, "CC");
    MatcherAssert.assertThat(
        IidMapSimpleConstructors.newIidMap(mutableMap),
        iidMapEqualityMatcher(iidMapOf(
            STOCK_A, "AA",
            STOCK_B, "BB",
            STOCK_C, "CC")));
  }

  @Test
  public void testLockValues_outerIidMapVersion() {
    MutableIidMap<MutableRBMap<String, Integer>> mutableMap = newMutableIidMap();
    mutableMap.putAssumingAbsent(STOCK_A, newMutableRBMap());
    mutableMap.putAssumingAbsent(STOCK_B, newMutableRBMap());
    mutableMap.putAssumingAbsent(STOCK_C, newMutableRBMap());
    mutableMap.getOrThrow(STOCK_B).putAssumingAbsent("b1", 1);
    mutableMap.getOrThrow(STOCK_C).putAssumingAbsent("c1", 1);
    mutableMap.getOrThrow(STOCK_C).putAssumingAbsent("c2", 2);
    assertThat(
        lockValues(mutableMap),
        iidMapMatcher(
            iidMapOf(
                STOCK_A, emptyRBMap(),
                STOCK_B, singletonRBMap("b1", 1),
                STOCK_C, rbMapOf(
                    "c1", 1,
                    "c2", 2)),
            f -> rbMapMatcher(f, f2 -> typeSafeEqualTo(f2))));
  }

  @Test
  public void testLockValues_outerRBMapVersion() {
    MutableRBMap<String, MutableIidMap<Integer>> mutableMap = newMutableRBMap();
    mutableMap.putAssumingAbsent("A", newMutableIidMap());
    mutableMap.putAssumingAbsent("B", newMutableIidMap());
    mutableMap.putAssumingAbsent("C", newMutableIidMap());
    mutableMap.getOrThrow("A").putAssumingAbsent(STOCK_A1, 11);
    mutableMap.getOrThrow("A").putAssumingAbsent(STOCK_A2, 22);
    mutableMap.getOrThrow("B").putAssumingAbsent(STOCK_B1, 33);
    assertThat(
        lockValues(mutableMap),
        rbMapMatcher(
            rbMapOf(
                "A", iidMapOf(
                    STOCK_A1, 11,
                    STOCK_A2, 22),
                "B", singletonIidMap(
                    STOCK_B1, 33),
                "C", emptyIidMap()),
            f -> iidMapEqualityMatcher(f)));
     }

  @Test
  public void testMergeIidMapsDisallowingOverlap() {
    IidMap<String> emptyStringMap = emptyIidMap();
    assertThat(
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            emptyStringMap,
            emptyStringMap),
        iidMapEqualityMatcher(
            emptyStringMap));
    assertThat(
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            iidMapOf(
                STOCK_A1, "a1",
                STOCK_A2, "a2"),
            emptyStringMap),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_A1, "a1",
                STOCK_A2, "a2")));

    assertThat(
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            emptyStringMap,
            iidMapOf(
                STOCK_A1, "a1",
                STOCK_A2, "a2")),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_A1, "a1",
                STOCK_A2, "a2")));

    assertIllegalArgumentException( () ->
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            singletonIidMap(STOCK_A1, "a"),
            singletonIidMap(STOCK_A1, "a")));
    assertIllegalArgumentException( () ->
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            singletonIidMap(STOCK_A1, "a"),
            singletonIidMap(STOCK_A1, "b")));
    assertIllegalArgumentException( () ->
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            iidMapOf(
                STOCK_A1, "a1",
                STOCK_A2, "a2"),
            iidMapOf(
                STOCK_A1, "b1",
                STOCK_A3, "b3")));
    assertIllegalArgumentException( () ->
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            iidMapOf(
                STOCK_A1, "a1",
                STOCK_A2, "a2"),
            iidMapOf(
                STOCK_A1, "b1",
                STOCK_A3, "b3"),
            iidMapOf(
                STOCK_A4, "c4",
                STOCK_A5, "c5")));
  }

  @Test
  public void testMergeIidMapsDisallowingOverlap_iteratorOverload() {
    IidMap<String> emptyStringMap = emptyIidMap();

    assertThat(
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            emptyIterator()),
        iidMapEqualityMatcher(
            emptyStringMap));

    assertThat(
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            emptyIterator()),
        iidMapEqualityMatcher(
            emptyStringMap));

    assertThat(
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            singletonList(emptyStringMap).iterator()),
        iidMapEqualityMatcher(
            emptyStringMap));

    assertThat(
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            singletonList(iidMapOf(
                STOCK_A1, "a1",
                STOCK_A2, "a2")).iterator()),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_A1, "a1",
                STOCK_A2, "a2")));

    assertThat(
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            ImmutableList.of(
                emptyStringMap,
                emptyStringMap).iterator()),
        iidMapEqualityMatcher(
            emptyStringMap));
    assertThat(
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            ImmutableList.of(
                iidMapOf(
                    STOCK_A1, "a1",
                    STOCK_A2, "a2"),
                emptyStringMap).iterator()),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_A1, "a1",
                STOCK_A2, "a2")));

    assertThat(
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            ImmutableList.of(
                emptyStringMap,
                iidMapOf(
                    STOCK_A1, "a1",
                    STOCK_A2, "a2")).iterator()),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_A1, "a1",
                STOCK_A2, "a2")));

    assertIllegalArgumentException( () ->
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            ImmutableList.of(
                singletonIidMap(STOCK_A1, "a"),
                singletonIidMap(STOCK_A1, "a")).iterator()));
    assertIllegalArgumentException( () ->
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            ImmutableList.of(
                singletonIidMap(STOCK_A1, "a"),
                singletonIidMap(STOCK_A1, "b")).iterator()));
    assertIllegalArgumentException( () ->
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            ImmutableList.of(
                iidMapOf(
                    STOCK_A1, "a1",
                    STOCK_A2, "a2"),
                iidMapOf(
                    STOCK_A1, "b1",
                    STOCK_A3, "b3")).iterator()));
    assertIllegalArgumentException( () ->
        IidMapMergers.mergeIidMapsDisallowingOverlap(
            ImmutableList.of(
                iidMapOf(
                    STOCK_A1, "a1",
                    STOCK_A2, "a2"),
                iidMapOf(
                    STOCK_A1, "b1",
                    STOCK_A3, "b3"),
                iidMapOf(
                    STOCK_A4, "c4",
                    STOCK_A5, "c5")).iterator()));
  }

  @Test
  public void testMergeIidMapsByTransformedValueAssumingFullOverlap() {
    IidMap<Integer> emptyIntMap = emptyIidMap();
    IidMap<Integer> intMap1 = singletonIidMap(STOCK_A1, 11);
    IidMap<Integer> intMap2 = iidMapOf(
        STOCK_A1, 21,
        STOCK_A2, 31);

    IidMap<Double> emptyDoubleMap = emptyIidMap();
    IidMap<Double> doubleMap1 = singletonIidMap(STOCK_A1, 11.88);
    IidMap<Double> doubleMap2 = iidMapOf(
        STOCK_A1, 21.88,
        STOCK_A2, 31.88);
    IidMap<Double> badDoubleMap1 = singletonIidMap(STOCK_C, DUMMY_DOUBLE);
    IidMap<Double> badDoubleMap2 = iidMapOf(
        STOCK_A1, DUMMY_DOUBLE,
        STOCK_C, DUMMY_DOUBLE);

    BiFunction<IidMap<Integer>, IidMap<Double>, IidMap<String>> maker = (leftMap, rightMap) ->
        IidMapMergers.mergeIidMapsByTransformedValueAssumingFullOverlap(
            leftMap,
            rightMap,
            (int1, boolean2) -> Strings.format("%s_%s", int1, boolean2));
    assertThat(
        maker.apply(emptyIntMap, emptyDoubleMap),
        iidMapEqualityMatcher(
            emptyIidMap()));
    assertThat(
        maker.apply(intMap1, doubleMap1),
        iidMapEqualityMatcher(
            singletonIidMap(STOCK_A1, "11_11.88")));
    assertThat(
        maker.apply(intMap2, doubleMap2),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_A1, "21_21.88",
                STOCK_A2, "31_31.88")));

    // all of the following will fail
    assertIllegalArgumentException( () -> maker.apply(emptyIntMap, doubleMap1));
    assertIllegalArgumentException( () -> maker.apply(emptyIntMap, doubleMap2));

    assertIllegalArgumentException( () -> maker.apply(intMap1, emptyDoubleMap));
    assertIllegalArgumentException( () -> maker.apply(intMap1, badDoubleMap1));
    assertIllegalArgumentException( () -> maker.apply(intMap1, doubleMap2));

    assertIllegalArgumentException( () -> maker.apply(intMap2, emptyDoubleMap));
    assertIllegalArgumentException( () -> maker.apply(intMap2, doubleMap1));
    assertIllegalArgumentException( () -> maker.apply(intMap2, badDoubleMap1));
    assertIllegalArgumentException( () -> maker.apply(intMap2, badDoubleMap2));
  }

  @Test
  public void testVisitInstrumentsOfTwoIidMapsAssumingFullOverlap_keysInMapsAreUnequal_throws() {
    IidMap<Integer> emptyIntMap = emptyIidMap();
    IidMap<Integer> intMap1 = singletonIidMap(STOCK_A1, 11);
    IidMap<Integer> intMap2 = iidMapOf(
        STOCK_A1, 21,
        STOCK_A2, 31);

    IidMap<Double> emptyDoubleMap = emptyIidMap();
    IidMap<Double> doubleMap1 = singletonIidMap(STOCK_A1, 11.88);
    IidMap<Double> doubleMap2 = iidMapOf(
        STOCK_A1, 21.88,
        STOCK_A2, 31.88);
    IidMap<Double> badDoubleMap1 = singletonIidMap(STOCK_C, DUMMY_DOUBLE);
    IidMap<Double> badDoubleMap2 = iidMapOf(
        STOCK_A1, DUMMY_DOUBLE,
        STOCK_C, DUMMY_DOUBLE);

    BiConsumer<IidMap<Integer>, IidMap<Double>> assertWillThrow = (leftMap, rightMap) ->
        assertIllegalArgumentException( () -> IidMapVisitors.visitInstrumentsOfTwoIidMapsAssumingFullOverlap(
            leftMap,
            rightMap,
            (instrumentId, int1, boolean2) -> {}));

    // all of the following will fail
    assertWillThrow.accept(emptyIntMap, doubleMap1);
    assertWillThrow.accept(emptyIntMap, doubleMap2);

    assertWillThrow.accept(intMap1, emptyDoubleMap);
    assertWillThrow.accept(intMap1, badDoubleMap1);
    assertWillThrow.accept(intMap1, doubleMap2);

    assertWillThrow.accept(intMap2, emptyDoubleMap);
    assertWillThrow.accept(intMap2, doubleMap1);
    assertWillThrow.accept(intMap2, badDoubleMap1);
    assertWillThrow.accept(intMap2, badDoubleMap2);
  }

  @Test
  public void testVisitInstrumentsOfTwoIidMapsAssumingFullOverlap_keysInMapsAreEqual() {
    IidMap<Integer> intMap2 = iidMapOf(
        instrumentId(1), 11,
        instrumentId(2), 22);
    IidMap<Double> doubleMap2 = iidMapOf(
        instrumentId(1), 11.1,
        instrumentId(2), 22.2);
    MutableRBSet<String> mutableSet = newMutableRBSet();
    IidMapVisitors.visitInstrumentsOfTwoIidMapsAssumingFullOverlap(
        intMap2,
        doubleMap2,
        (instrumentId, intValue, doubleValue) -> mutableSet.add(Strings.format("%s_%s_%s",
            instrumentId.asLong(), intValue, doubleValue)));
    assertEquals(
        rbSetOf("1_11_11.1", "2_22_22.2"),
        newRBSet(mutableSet));
  }

  @Test
  public void testVisitInstrumentsOfTwoIidMapsAssumingSubset_keysInLeftMap_notSubsetOf_keysInRightMap_throws() {
    IidMap<Integer> emptyIntMap = emptyIidMap();
    IidMap<Integer> intMap1 = singletonIidMap(STOCK_A1, 11);
    IidMap<Integer> intMap2 = iidMapOf(
        STOCK_A1, 21,
        STOCK_A2, 31);

    IidMap<Double> emptyDoubleMap = emptyIidMap();
    IidMap<Double> doubleMap1 = singletonIidMap(STOCK_A1, 11.88);
    IidMap<Double> doubleMap2 = iidMapOf(
        STOCK_A1, 21.88,
        STOCK_A2, 31.88);
    IidMap<Double> doubleMap3 = iidMapOf(
        STOCK_A1, 21.88,
        STOCK_A2, 31.88,
        STOCK_A3, 41.88);

    BiConsumer<IidMap<Integer>, IidMap<Double>> assertWillThrow = (leftMap, rightMap) -> {
      assertIllegalArgumentException( () -> IidMapVisitors.visitInstrumentsOfTwoIidMapsAssumingSubset(
          leftMap,
          rightMap,
          (instrumentId1, int1, double2) -> {},
          (instrumentId, double2) -> {}));
    };

    BiConsumer<IidMap<Integer>, IidMap<Double>> assertWillNotThrow = (leftMap, rightMap) -> {
      IidMapVisitors.visitInstrumentsOfTwoIidMapsAssumingSubset(
          leftMap,
          rightMap,
          (instrumentId1, int1, double2) -> {},
          (instrumentId2, double2) -> {});
    };

    assertWillNotThrow.accept(emptyIntMap, emptyDoubleMap);
    assertWillNotThrow.accept(emptyIntMap, doubleMap1);
    assertWillNotThrow.accept(emptyIntMap, doubleMap2);

    assertWillNotThrow.accept(intMap1, doubleMap1);
    assertWillNotThrow.accept(intMap1, doubleMap2);

    assertWillNotThrow.accept(intMap2, doubleMap2);
    assertWillNotThrow.accept(intMap2, doubleMap3);

    assertWillThrow.accept(intMap1, emptyDoubleMap);
    assertWillThrow.accept(intMap2, emptyDoubleMap);
    assertWillThrow.accept(intMap2, doubleMap1);
  }

  @Test
  public void testVisitInstrumentsOfTwoIidMapsAssumingSubset_keysInMapsAreEqual_doesNotThrow() {
    IidMap<Integer> intMap2 = iidMapOf(
        instrumentId(1), 11,
        instrumentId(2), 22);
    IidMap<Double> doubleMap2 = iidMapOf(
        instrumentId(1), 11.1,
        instrumentId(2), 22.2);
    MutableRBSet<String> mutableSet1 = newMutableRBSet();
    IidMapVisitors.visitInstrumentsOfTwoIidMapsAssumingSubset(
        intMap2,
        doubleMap2,
        (instrumentId, intValue, doubleValue) -> mutableSet1.add(Strings.format("%s_%s_%s",
            instrumentId.asLong(), intValue, doubleValue)),
        (instrumentId, doubleValue) -> mutableSet1.add(Strings.format("KEYS_EQUAL_THIS_CONSUMER_NOT_USED")));
    assertEquals(
        rbSetOf("1_11_11.1", "2_22_22.2"),
        newRBSet(mutableSet1));

    // Since the maps have the same keys, each is technically a "subset" of the other.
    // Therefore we can visit the maps in the opposite order as well:
    MutableRBSet<String> mutableSet2 = newMutableRBSet();
    IidMapVisitors.visitInstrumentsOfTwoIidMapsAssumingSubset(
        doubleMap2,
        intMap2,
        (instrumentId, doubleValue, intValue) -> mutableSet2.add(Strings.format("%s_%s_%s",
            instrumentId.asLong(), intValue, doubleValue)),
        (instrumentId, intValue) -> mutableSet2.add(Strings.format("%s_%s",
            instrumentId.asLong(), intValue)));
    assertEquals(
        rbSetOf("1_11_11.1", "2_22_22.2"),
        newRBSet(mutableSet2));
  }

  @Test
  public void mergeIidMapsByValue_generalCase() {
    assertThat(
        mergeIidMapsByValue(
            (v1, v2) -> Strings.format("%s_%s", v1, v2),
            v1 -> Strings.format("%s.", v1),
            v2 -> Strings.format(".%s", v2),
            iidMapOf(
                STOCK_A, "1",
                STOCK_B, "2"),
            iidMapOf(
                STOCK_B, "3",
                STOCK_C, "4")),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_A, "1.",
                STOCK_B, "2_3",
                STOCK_C, ".4")));

    assertThat(
        mergeIidMapsByValue(
            Integer::sum,
            iidMapOf(
                STOCK_A, 1,
                STOCK_B, 2),
            iidMapOf(
                STOCK_B, 3,
                STOCK_C, 4)),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_A, 1,
                STOCK_B, intExplained(5, 2 + 3),
                STOCK_C, 4)));

    assertThat(
        mergeIidMapsByValue(
            Integer::sum,
            Stream.of(
                iidMapOf(
                    STOCK_A, 1,
                    STOCK_B, 2),
                iidMapOf(
                    STOCK_B, 3,
                    STOCK_C, 4))),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_A, 1,
                STOCK_B, intExplained(5, 2 + 3),
                STOCK_C, 4)));
  }

  @Test
  public void testTwoRBMapsVisitor() {
    MutableRBSet<String> mutableSet = newMutableRBSet();
    IidMapVisitors.visitInstrumentsOfTwoIidMaps(
        iidMapOf(
            instrumentId(10), "a",
            instrumentId(11), "b",
            instrumentId(12), "c",
            instrumentId(13), "d"),
        iidMapOf(
            instrumentId(12), 200L,
            instrumentId(13), 300L,
            instrumentId(14), 400L,
            instrumentId(15), 500L),
        new TwoIidMapsVisitor<String, Long>() {
          @Override
          public void visitInstrumentInLeftMapOnly(InstrumentId keyInLeftMapOnly, String valueInLeftMapOnly) {
            mutableSet.add(Strings.format("L_%s_%s", keyInLeftMapOnly.asLong(), valueInLeftMapOnly));
          }

          @Override
          public void visitInstrumentInRightMapOnly(InstrumentId keyInRightMapOnly, Long valueInRightMapOnly) {
            mutableSet.add(Strings.format("R_%s_%s", keyInRightMapOnly.asLong(), valueInRightMapOnly));
          }

          @Override
          public void visitInstrumentInBothMaps(InstrumentId keyInBothMaps, String valueInLeftMap, Long valueInRightMap) {
            mutableSet.add(Strings.format("B_%s_%s_%s", keyInBothMaps.asLong(), valueInLeftMap, valueInRightMap));
          }
        });
    assertEquals(
        newRBSet(mutableSet),
        rbSetOf(
            "L_10_a", "L_11_b",
            "B_12_c_200", "B_13_d_300",
            "R_14_400", "R_15_500"));
  }

  @Test
  public void testThreeRBMapsVisitor() {
    MutableRBSet<String> mutableSet = newMutableRBSet();

    visitInstrumentsOfThreeIidMaps(
        iidMapOf(
            instrumentId(10), "<10>",
            instrumentId(11), "<11>",
            instrumentId(12), "<12>"),
        iidMapOf(
            instrumentId(10), 100L,
            instrumentId(11), 110L),
        singletonIidMap(
            instrumentId(10), 'X'),
        (instrumentId, maybeString, maybeLong, maybeChar) ->
            mutableSet.add(Strings.format("%s_%s_%s_%s",
                instrumentId.asLong(),
                maybeString.orElse("*"),
                maybeLong.map(v -> v.toString()).orElse("*"),
                maybeChar.map(v -> v.toString()).orElse("*"))));
    assertEquals(
        newRBSet(mutableSet),
        rbSetOf(
            "10_<10>_100_X",
            "11_<11>_110_*",
            "12_<12>_*_*"));
  }

  @Test
  public void testPairOfIidSetAndIidMapVisitor() {
    MutableRBSet<String> mutableSet = newMutableRBSet();
    IidMapVisitors.visitItemsOfIidSetAndIidMap(
        iidSetOf(instrumentId(10), instrumentId(11), instrumentId(12), instrumentId(13)),
        iidMapOf(
            instrumentId(12), "a",
            instrumentId(13), "b",
            instrumentId(14), "c",
            instrumentId(15), "d"),
        new PairOfIidSetAndIidMapVisitor<String>() {
          @Override
          public void visitInstrumentInSetOnly(InstrumentId keyInSetOnly) {
            mutableSet.add(Strings.format("S_%s", keyInSetOnly.asLong()));
          }

          @Override
          public void visitInstrumentInMapOnly(InstrumentId keyInMapOnly, String value) {
            mutableSet.add(Strings.format("M_%s_%s", keyInMapOnly.asLong(), value));
          }

          @Override
          public void visitInstrumentInBothSetAndMap(InstrumentId keyInBothSetAndMap, String value) {
            mutableSet.add(Strings.format("B_%s_%s", keyInBothSetAndMap.asLong(), value));
          }
        });
    assertEquals(
        newRBSet(mutableSet),
        rbSetOf(
            "S_10", "S_11",       // in set only
            "M_14_c", "M_15_d",   // in map only
            "B_12_a", "B_13_b")); // in both set and map
  }

  @Test
  public void testMergeIidMapsByTransformedEntry() {
    BiFunction<InstrumentId, String, String> onlyLeftPresent =
        (instrumentId, valueInLeftMapOnly) ->
            Strings.format("L_%s_%s", instrumentId.asLong(), valueInLeftMapOnly);
    BiFunction<InstrumentId, Long, String> onlyRightPresent =
        (instrumentId, valueInRightMapOnly) ->
            Strings.format("R_%s_%s", instrumentId.asLong(), valueInRightMapOnly);
    TriFunction<InstrumentId, String, Long, String> mergeFunction =
        (instrumentId, valueInLeftMap, valueInRightMap) ->
            Strings.format("B_%s_%s_%s", instrumentId.asLong(), valueInLeftMap, valueInRightMap);
    assertThat(
        IidMapMergers.mergeIidMapsByTransformedEntry(
            mergeFunction,
            onlyLeftPresent,
            onlyRightPresent,
            iidMapOf(
                instrumentId(10), "a",
                instrumentId(11), "b",
                instrumentId(12), "c",
                instrumentId(13), "d"),
            iidMapOf(
                instrumentId(12), 200L,
                instrumentId(13), 300L,
                instrumentId(14), 400L,
                instrumentId(15), 500L)),
        iidMapEqualityMatcher(
            iidMapOf(
                instrumentId(10), "L_10_a",
                instrumentId(11), "L_11_b",
                instrumentId(12), "B_12_c_200",
                instrumentId(13), "B_13_d_300",
                instrumentId(14), "R_14_400",
                instrumentId(15), "R_15_500")));
    assertThat(
        IidMapMergers.mergeIidMapsByTransformedEntry(
            mergeFunction,
            onlyLeftPresent,
            onlyRightPresent,
            emptyIidMap(),
            emptyIidMap()),
        iidMapEqualityMatcher(
            emptyIidMap()));
  }

  @Test
  public void testMergeIidMapsByTransformedValue() {
    Function<String, String> onlyLeftPresent = valueInLeftMapOnly ->
        Strings.format("L_%s", valueInLeftMapOnly);
    Function<Long, String> onlyRightPresent = valueInRightMapOnly ->
        Strings.format("R_%s", valueInRightMapOnly);
    BiFunction<String, Long, String> mergeFunction = (valueInLeftMap, valueInRightMap) ->
        Strings.format("B_%s_%s", valueInLeftMap, valueInRightMap);
    assertThat(
        IidMapMergers.mergeIidMapsByTransformedValue(
            mergeFunction,
            onlyLeftPresent,
            onlyRightPresent,
            iidMapOf(
                instrumentId(10), "a",
                instrumentId(11), "b",
                instrumentId(12), "c",
                instrumentId(13), "d"),
            iidMapOf(
                instrumentId(12), 200L,
                instrumentId(13), 300L,
                instrumentId(14), 400L,
                instrumentId(15), 500L)),
        iidMapEqualityMatcher(
            iidMapOf(
                instrumentId(10), "L_a",
                instrumentId(11), "L_b",
                instrumentId(12), "B_c_200",
                instrumentId(13), "B_d_300",
                instrumentId(14), "R_400",
                instrumentId(15), "R_500")));
    assertThat(
        IidMapMergers.mergeIidMapsByTransformedValue(
            mergeFunction,
            onlyLeftPresent,
            onlyRightPresent,
            emptyIidMap(),
            emptyIidMap()),
        iidMapEqualityMatcher(
            emptyIidMap()));
  }

  @Test
  public void testFilterForPresentValuesAndTransformValuesCopy() {
    BiConsumer<IidMap<Integer>, Function<Integer, Optional<Integer>>> asserter = (expectedResult, transformer) ->
        assertThat(
            filterForPresentValuesAndTransformValuesCopy(
                iidMapOf(
                    STOCK_A, 1,
                    STOCK_B, 2,
                    STOCK_C, 3,
                    STOCK_D, 4,
                    STOCK_E, 5),
                transformer),
            iidMapEqualityMatcher(expectedResult));
    // all are Optional.empty()
    asserter.accept(
        emptyIidMap(),
        v -> Optional.empty());
    // all are non-empty + 100
    asserter.accept(
        iidMapOf(
            STOCK_A, intExplained(101, 100 + 1),
            STOCK_B, intExplained(102, 100 + 2),
            STOCK_C, intExplained(103, 100 + 3),
            STOCK_D, intExplained(104, 100 + 4),
            STOCK_E, intExplained(105, 100 + 5)),
        v -> Optional.of(v + 100));
    // only even ones pass the filter
    asserter.accept(
        iidMapOf(
            STOCK_B, intExplained(102, 100 + 2),
            STOCK_D, intExplained(104, 100 + 4)),
        v -> v % 2 == 0
            ? Optional.of(v + 100)
            : Optional.empty());
  }

  @Test
  public void testVisitSharedInstrumentsOfTwoIidMaps_overloadWithoutInstrumentId() {
    MutableRBSet<String> mutableSet = newMutableRBSet();
    IidMapVisitors.visitSharedInstrumentsOfTwoIidMaps(
        iidMapOf(
            instrumentId(1), "A",
            instrumentId(2), "B",
            instrumentId(3), "C"),
        iidMapOf(
            instrumentId(2), 22,
            instrumentId(3), 33,
            instrumentId(4), 44),
        (stringVal, intVal) -> mutableSet.add(Strings.format("%s_%s", stringVal, intVal)));
    assertEquals(
        rbSetOf("B_22", "C_33"),
        newRBSet(mutableSet));
  }

  @Test
  public void testVisitSharedInstrumentsOfTwoIidMaps_overloadWithInstrumentId() {
    MutableRBSet<String> mutableSet = newMutableRBSet();
    IidMapVisitors.visitSharedInstrumentsOfTwoIidMaps(
        iidMapOf(
            instrumentId(1), "A",
            instrumentId(2), "B",
            instrumentId(3), "C"),
        iidMapOf(
            instrumentId(2), 22,
            instrumentId(3), 33,
            instrumentId(4), 44),
        (instrumentId, stringVal, intVal) -> mutableSet.add(
            Strings.format("%s.%s_%s", instrumentId.asLong(), stringVal, intVal)));
    assertEquals(
        rbSetOf("2.B_22", "3.C_33"),
        newRBSet(mutableSet));
  }

  @Test
  public void testGetOnlyNonEmptyIidMap() {
    assertOptionalNonEmpty(
        getWhenAtMostOneIidMapIsNonEmpty(emptyIidMap(), emptyIidMap()),
        iidMapMatcher(emptyIidMap(), f -> typeSafeEqualTo(f)));
    assertOptionalNonEmpty(
        getWhenAtMostOneIidMapIsNonEmpty(emptyIidMap(), emptyIidMap(), emptyIidMap()),
        iidMapMatcher(emptyIidMap(), f -> typeSafeEqualTo(f)));

    InstrumentId A = STOCK_A;
    InstrumentId B = STOCK_B;
    InstrumentId C = STOCK_C;

    rbSetOf(
        getWhenAtMostOneIidMapIsNonEmpty(singletonIidMap(A, 1), emptyIidMap()),
        getWhenAtMostOneIidMapIsNonEmpty(emptyIidMap(),         singletonIidMap(A, 1)),
        getWhenAtMostOneIidMapIsNonEmpty(singletonIidMap(A, 1), emptyIidMap(),          emptyIidMap()),
        getWhenAtMostOneIidMapIsNonEmpty(emptyIidMap(),         singletonIidMap(A, 1),  emptyIidMap()),
        getWhenAtMostOneIidMapIsNonEmpty(emptyIidMap(),         emptyIidMap(),          singletonIidMap(A, 1)))
        .forEach(result -> assertOptionalNonEmpty(
            result,
            iidMapMatcher(
                singletonIidMap(A, 1), f -> typeSafeEqualTo(f))));
    rbSetOf(
        getWhenAtMostOneIidMapIsNonEmpty(singletonIidMap(A, 1), singletonIidMap(A, 1)),
        getWhenAtMostOneIidMapIsNonEmpty(singletonIidMap(A, 1), singletonIidMap(B, 2)),
        getWhenAtMostOneIidMapIsNonEmpty(singletonIidMap(A, 1), singletonIidMap(A, 1),  emptyIidMap()),
        getWhenAtMostOneIidMapIsNonEmpty(singletonIidMap(A, 1), singletonIidMap(B, 2),  emptyIidMap()),
        getWhenAtMostOneIidMapIsNonEmpty(singletonIidMap(A, 1), emptyIidMap(),          singletonIidMap(A, 1)),
        getWhenAtMostOneIidMapIsNonEmpty(singletonIidMap(A, 1), emptyIidMap(),          singletonIidMap(B, 2)),
        getWhenAtMostOneIidMapIsNonEmpty(emptyIidMap(),         singletonIidMap(A, 1),  singletonIidMap(A, 1)),
        getWhenAtMostOneIidMapIsNonEmpty(emptyIidMap(),         singletonIidMap(A, 1),  singletonIidMap(B, 2)),
        getWhenAtMostOneIidMapIsNonEmpty(singletonIidMap(A, 1), singletonIidMap(A, 1),  singletonIidMap(A, 1)),
        getWhenAtMostOneIidMapIsNonEmpty(singletonIidMap(A, 1), singletonIidMap(B, 2),  singletonIidMap(C, 3)))
        .forEach(result -> assertOptionalEmpty(result));
  }

  @Test
  public void testIidMapFromStream_generalCase_noHasInstrumentIdObjects() {
    BiConsumer<Provider<Stream<Pair<Boolean, String>>>, IidMap<String>> asserter = (stream, expectedIidMap) -> {
      assertThat(
          iidMapFromStream(stream.get(), v -> parseInstrumentId(v.getRight()), v -> '_' + v.getLeft().toString()),
          iidMapEqualityMatcher(expectedIidMap));
      // Trying expected size of 1 < 2 and 3 > 2
      assertThat(
          iidMapFromStreamWithExpectedSize(1, stream.get(), v -> parseInstrumentId(v.getRight()), v -> '_' + v.getLeft().toString()),
          iidMapEqualityMatcher(expectedIidMap));
      assertThat(
          iidMapFromStreamWithExpectedSize(3, stream.get(), v -> parseInstrumentId(v.getRight()), v -> '_' + v.getLeft().toString()),
          iidMapEqualityMatcher(expectedIidMap));
    };

    asserter.accept(
        () -> Stream.of(pair(true, "11"), pair(false, "22")),
        iidMapOf(
            instrumentId(11), "_true",
            instrumentId(22), "_false"));
    asserter.accept(
        () -> Stream.empty(),
        emptyIidMap());
  }

  @Test
  public void testIidMapFromIterator_generalCase_noHasInstrumentIdObjects() {
    BiConsumer<Provider<Iterator<Pair<Boolean, String>>>, IidMap<String>> asserter = (iter, expectedIidMap) -> {
      assertThat(
          iidMapFromIterator(iter.get(), v -> parseInstrumentId(v.getRight()), v -> '_' + v.getLeft().toString()),
          iidMapEqualityMatcher(expectedIidMap));
      // Trying expected size of 1 < 2 and 3 > 2
      assertThat(
          iidMapFromIteratorWithExpectedSize(1, iter.get(), v -> parseInstrumentId(v.getRight()), v -> '_' + v.getLeft().toString()),
          iidMapEqualityMatcher(expectedIidMap));
      assertThat(
          iidMapFromIteratorWithExpectedSize(3, iter.get(), v -> parseInstrumentId(v.getRight()), v -> '_' + v.getLeft().toString()),
          iidMapEqualityMatcher(expectedIidMap));
    };

    asserter.accept(
        () -> ImmutableList.of(pair(true, "11"), pair(false, "22")).iterator(),
        iidMapOf(
            instrumentId(11), "_true",
            instrumentId(22), "_false"));
    asserter.accept(
        () -> Collections.emptyIterator(),
        emptyIidMap());
  }

  @Test
  public void testInvertMapOfDisjointIidSets() {
    assertThat(
        invertMapOfDisjointIidSets(emptyRBMap()),
        iidMapEqualityMatcher(
            emptyIidMap()));
    assertThat(
        invertMapOfDisjointIidSets(rbMapOf(
            "ix1", iidSetOf(STOCK_A, STOCK_B),
            "ix2", singletonIidSet(STOCK_A))),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_A, rbSetOf("ix1", "ix2"),
                STOCK_B, singletonRBSet("ix1"))));
  }

}
