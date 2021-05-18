package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.ImmutableIndexableArray1D.emptyImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1D.immutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.immutableIndexableArray1DMatcher;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.singletonImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.testImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArrays1D.immutableIndexableArray1DFromStream;
import static com.rb.nonbiz.collections.ImmutableIndexableArrays1D.mergeImmutableIndexableArrays1DByValue;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.PairTest.pairEqualityMatcher;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

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

}
