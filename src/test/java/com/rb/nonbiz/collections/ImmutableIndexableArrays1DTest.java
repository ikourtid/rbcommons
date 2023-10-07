package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.asset.InstrumentId;
import org.junit.Test;

import java.util.Comparator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A2;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A3;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapTest.iidMapDoubleMatcher;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1D.emptyImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1D.immutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.immutableIndexableArray1DMatcher;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.singletonImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.testImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArrays1D.iidMapToImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArrays1D.immutableIndexableArray1DFromStream;
import static com.rb.nonbiz.collections.ImmutableIndexableArrays1D.immutableIndexableArray1DToIidMap;
import static com.rb.nonbiz.collections.ImmutableIndexableArrays1D.immutableIndexableArray1DToRBMap;
import static com.rb.nonbiz.collections.ImmutableIndexableArrays1D.mergeImmutableIndexableArrays1DByValue;
import static com.rb.nonbiz.collections.ImmutableIndexableArrays1D.rbMapToImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.PairTest.pairEqualityMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapDoubleMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Map.Entry.comparingByKey;
import static java.util.Map.Entry.comparingByValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class ImmutableIndexableArrays1DTest {

  @Test
  public void testMergeImmutableIndexableArrays1DByValue() {
    ImmutableIndexableArray1D<String, Integer> abc = testImmutableIndexableArray1D("a", 100, "b", 101, "c", 102);
    ImmutableIndexableArray1D<String, Integer> bcd = testImmutableIndexableArray1D("b", 103, "c", 104, "d", 105);
    ImmutableIndexableArray1D<String, Integer> cde = testImmutableIndexableArray1D("c", 106, "d", 107, "e", 108);

    rbSetOf(
        mergeImmutableIndexableArrays1DByValue(size -> new Integer[size], Integer::sum, abc, bcd, cde),
        mergeImmutableIndexableArrays1DByValue(size -> new Integer[size], Integer::sum, ImmutableList.of(abc, bcd, cde)))
        .forEach(actual -> assertThat(
            "Testing case of 3 lists",
            actual,
            immutableIndexableArray1DMatcher(
                immutableIndexableArray1D(simpleArrayIndexMapping("a", "b", "c", "d", "e"), new Integer[] {
                    100,
                    intExplained(204, 101 + 103),
                    intExplained(312, 102 + 104 + 106),
                    intExplained(212, 105 + 107),
                    108 }),
                k -> typeSafeEqualTo(k),
                v -> typeSafeEqualTo(v))));

    rbSetOf(
        mergeImmutableIndexableArrays1DByValue(size -> new Integer[size], Integer::sum, abc, bcd),
        mergeImmutableIndexableArrays1DByValue(size -> new Integer[size], Integer::sum, ImmutableList.of(abc, bcd)))
        .forEach(actual -> assertThat(
            "Testing case of 2 lists",
            actual,
            immutableIndexableArray1DMatcher(
                immutableIndexableArray1D(simpleArrayIndexMapping("a", "b", "c", "d"), new Integer[] {
                    100,
                    intExplained(204, 101 + 103),
                    intExplained(206, 102 + 104),
                    105 }),
                k -> typeSafeEqualTo(k),
                v -> typeSafeEqualTo(v))));

    // The case of 1 item in the list can only be handled by the overload that takes in a list, not the varargs one.
    assertThat(
        "'merging' a single list results in that same list",
        mergeImmutableIndexableArrays1DByValue(size -> new Integer[size], Integer::sum, singletonList(abc)),
        immutableIndexableArray1DMatcher(
            abc,
            k -> typeSafeEqualTo(k),
            v -> typeSafeEqualTo(v)));

    assertIllegalArgumentException( () ->
        mergeImmutableIndexableArrays1DByValue(size -> new Integer[size], Integer::sum, emptyList()));
  }

  @Test
  public void testMergeImmutableIndexableArrays1DByValue_canHandleEmpty() {
    ImmutableIndexableArray1D<String, Integer> empty = emptyImmutableIndexableArray1D(new Integer[] {});

    assertThat(
        mergeImmutableIndexableArrays1DByValue(size -> new Integer[size], Integer::sum, empty, empty),
        immutableIndexableArray1DMatcher(empty, k -> typeSafeEqualTo(k), v -> typeSafeEqualTo(v)));
    assertThat(
        mergeImmutableIndexableArrays1DByValue(size -> new Integer[size], Integer::sum, empty, empty, empty),
        immutableIndexableArray1DMatcher(empty, k -> typeSafeEqualTo(k), v -> typeSafeEqualTo(v)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testImmutableIndexableArray1DFromStream() {
    IntFunction<Pair<String, Integer>[]> arrayInstantiator = size -> new Pair[size];
    BiConsumer<ImmutableIndexableArray1D<String, Pair<String, Integer>>, Stream<Pair<String, Integer>>> asserter =
        (expectedResult, stream) -> assertThat(
            immutableIndexableArray1DFromStream(
                arrayInstantiator,
                stream,
                v -> v.getLeft()),
            immutableIndexableArray1DMatcher(
                expectedResult,
                k -> typeSafeEqualTo(k),
                v1 -> pairEqualityMatcher(v1)));

    asserter.accept(
        emptyImmutableIndexableArray1D(new Pair[] {}),
        Stream.empty());
    asserter.accept(
        singletonImmutableIndexableArray1D("a", pair("a", 11)),
        Stream.of(pair("a", 11)));
    asserter.accept(
        testImmutableIndexableArray1D("a", pair("a", 11), "b", pair("b", 22)),
        Stream.of(pair("a", 11), pair("b", 22)));
  }

  @Test
  public void testImmutableIndexableArray1DFromStream_throwsOnSameKey() {
    assertIllegalArgumentException( () -> immutableIndexableArray1DFromStream(
        size -> new Pair[size],
        Stream.of(pair("a", 11), pair("a", 22)),
        v -> v.getLeft()));
  }

  @Test
  public void testImmutableIndexableArray1DToRBMap() {
    assertTrue(
        immutableIndexableArray1DToRBMap(emptyImmutableIndexableArray1D(new Double[] {}))
            .isEmpty());

    assertThat(
        immutableIndexableArray1DToRBMap(testImmutableIndexableArray1D(
            "A1", 1.1,
            "A2", 2.2,
            "A3", 3.3)),
        rbMapDoubleMatcher(
            rbMapOf(
                "A1", 1.1,
                "A2", 2.2,
                "A3", 3.3),
            DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testRBMapToImmutableIndexableArray1D() {
    Comparator<Map.Entry<String, Double>> comparatorByKey   = comparingByKey();
    Comparator<Map.Entry<String, Double>> comparatorByValue = comparingByValue();

    // Of course, we can also compare based on a combination of InstrumentId *and* double, but it's hard to test this.

    rbSetOf(comparatorByKey, comparatorByValue)
        .forEach(comparator ->
            assertTrue(
                rbMapToImmutableIndexableArray1D(emptyRBMap(), comparator).isEmpty()));

    BiConsumer<Comparator<Map.Entry<String, Double>>, ImmutableIndexableArray1D<String, Double>> asserter =
        (comparator, expectedResult) ->
            assertThat(
                rbMapToImmutableIndexableArray1D(
                    rbMapOf(
                        "A1", 9.9,
                        "A2", 8.8,
                        "A3", 7.7),
                    comparator),
                immutableIndexableArray1DMatcher(
                    expectedResult,
                    v -> typeSafeEqualTo(v),
                    v -> doubleAlmostEqualsMatcher(v, DEFAULT_EPSILON_1e_8)));

    asserter.accept(
        comparatorByKey,
        testImmutableIndexableArray1D(
            "A1", 9.9,
            "A2", 8.8,
            "A3", 7.7));
    asserter.accept(
        comparatorByKey.reversed(),
        testImmutableIndexableArray1D(
            "A3", 7.7,
            "A2", 8.8,
            "A1", 9.9));

    asserter.accept(
        comparatorByValue,
        testImmutableIndexableArray1D(
            "A3", 7.7,
            "A2", 8.8,
            "A1", 9.9));
    asserter.accept(
        comparatorByValue.reversed(),
        testImmutableIndexableArray1D(
            "A1", 9.9,
            "A2", 8.8,
            "A3", 7.7));
  }

  @Test
  public void testImmutableIndexableArray1DToIidMap() {
    assertTrue(
        immutableIndexableArray1DToIidMap(emptyImmutableIndexableArray1D(new Double[] {}))
            .isEmpty());

    assertThat(
        immutableIndexableArray1DToIidMap(testImmutableIndexableArray1D(
            STOCK_A1, 1.1,
            STOCK_A2, 2.2,
            STOCK_A3, 3.3)),
        iidMapDoubleMatcher(
            iidMapOf(
                STOCK_A1, 1.1,
                STOCK_A2, 2.2,
                STOCK_A3, 3.3),
            DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testIidMapToImmutableIndexableArray1D() {
    Comparator<Pair<InstrumentId, Double>> comparatorByInstrumentId = comparing(pair -> pair.getLeft());
    Comparator<Pair<InstrumentId, Double>> comparatorByValue        = comparing(pair -> pair.getRight());
    // Of course, we can also compare based on a combination of InstrumentId *and* double, but it's hard to test this.

    rbSetOf(comparatorByInstrumentId, comparatorByValue)
        .forEach(comparator ->
            assertTrue(
                iidMapToImmutableIndexableArray1D(emptyIidMap(), comparator).isEmpty()));

    BiConsumer<Comparator<Pair<InstrumentId, Double>>, ImmutableIndexableArray1D<InstrumentId, Double>> asserter =
        (comparator, expectedResult) ->
            assertThat(
                iidMapToImmutableIndexableArray1D(
                    iidMapOf(
                        STOCK_A1, 9.9,
                        STOCK_A2, 8.8,
                        STOCK_A3, 7.7),
                    comparator),
                immutableIndexableArray1DMatcher(
                    expectedResult,
                    v -> typeSafeEqualTo(v),
                    v -> doubleAlmostEqualsMatcher(v, DEFAULT_EPSILON_1e_8)));

    asserter.accept(
        comparatorByInstrumentId,
        testImmutableIndexableArray1D(
            STOCK_A1, 9.9,
            STOCK_A2, 8.8,
            STOCK_A3, 7.7));
    asserter.accept(
        comparatorByInstrumentId.reversed(),
        testImmutableIndexableArray1D(
            STOCK_A3, 7.7,
            STOCK_A2, 8.8,
            STOCK_A1, 9.9));

    asserter.accept(
        comparatorByValue,
        testImmutableIndexableArray1D(
            STOCK_A3, 7.7,
            STOCK_A2, 8.8,
            STOCK_A1, 9.9));
    asserter.accept(
        comparatorByValue.reversed(),
        testImmutableIndexableArray1D(
            STOCK_A1, 9.9,
            STOCK_A2, 8.8,
            STOCK_A3, 7.7));
  }

}
