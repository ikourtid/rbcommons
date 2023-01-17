package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.LongCounter;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.Collections2.permutations;
import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.biz.types.asset.InstrumentIds.instrumentIdArray;
import static com.rb.biz.types.asset.InstrumentIds.instrumentIdList;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapTest.iidMapEqualityMatcher;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSetFromPossibleDuplicates;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.hasLongSetMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIidSetEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.longExplained;
import static com.rb.nonbiz.types.LongCounter.longCounter;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IidSetTest extends RBTestMatcher<IidSet> {

  @Test
  public void testAsSortedList() {
    // It's possible that the set is stored in order somehow, and asSortedList() forgets to sort and just works by accident.
    // We use 'permutations' because the order that things get added to the set would probably catch such a bug.
    permutations(instrumentIdList(1L, 2L, 3L, 4L, 5L)).forEach(list ->
        assertThat(
            newIidSet(list).asSortedList(),
            orderedListEqualityMatcher(instrumentIdList(1L, 2L, 3L, 4L, 5L))));
  }

  @Test
  public void testInstrumentIdsIterator() {
    assertThat(
        newRBSet(iidSetOf(STOCK_A, STOCK_B, STOCK_C).iterator()),
        rbSetEqualsMatcher(rbSetOf(STOCK_A, STOCK_B, STOCK_C)));
  }

  // This test is useful in showing us that, if we need the InstrumentIds in sorted order,
  // we have to explicitly sort them; it doesn't happen for free.
  @Test
  public void testInstrumentIdsIteratorDoesNotAutomaticallySort() {
    // It's hard to test that the iterator does not sort stuff; what if it returns items sorted by accident?
    // Therefore, we will create a set large enough that it is statistically impossible that the items are returned
    // in the correct order.
    List<InstrumentId> unsortedInstruments = newArrayList();
    // This adds 100 instruments not in numeric order
    IntStream.range(0, 10).forEach(secondDigit ->
        IntStream.range(0, 10).forEach(firstDigit ->
            unsortedInstruments.add(instrumentId(1_000 + 10 * firstDigit + secondDigit))));
    List<InstrumentId> sortedInstruments = unsortedInstruments.stream().sorted().collect(Collectors.toList());
    assertThat(
        "There's a tiny chance that 100 instruments will appear in sorted order by accident",
        newIidSet(unsortedInstruments).iterator(),
        not(iteratorEqualityMatcher(sortedInstruments.iterator())));
  }

  @Test
  public void testSortedInstrumentIdStream() {
    assertEquals(
        ImmutableList.of(STOCK_A, STOCK_B, STOCK_C, STOCK_D),
        iidSetOf(STOCK_B, STOCK_D, STOCK_A, STOCK_C)
            .sortedStream()
            .collect(Collectors.toList()));
  }

  @Test
  public void testSize() {
    assertEquals(0, emptyIidSet().size());
    assertEquals(1, singletonIidSet(STOCK_A).size());
    assertEquals(2, iidSetOf(STOCK_A, STOCK_B).size());
    assertEquals(3, iidSetOf(STOCK_A, STOCK_B, STOCK_C).size());
  }

  @Test
  public void testIsEmpty() {
    assertTrue(emptyIidSet().isEmpty());
    assertFalse(singletonIidSet(STOCK_A).isEmpty());
    assertFalse(iidSetOf(STOCK_A, STOCK_B).isEmpty());
    assertFalse(iidSetOf(STOCK_A, STOCK_B, STOCK_C).isEmpty());
  }

  @Test
  public void testContains() {
    assertTrue(singletonIidSet(STOCK_A).contains(STOCK_A));
    assertFalse(singletonIidSet(STOCK_A).contains(STOCK_B));

    IidSet set = iidSetOf(STOCK_A, STOCK_B);
    assertTrue(set.contains(STOCK_A));
    assertTrue(set.contains(STOCK_B));
    assertFalse(set.contains(STOCK_C));
  }

  @Test
  public void testToIidMap() {
    assertEmptyIidMap(emptyIidSet().toIidMap(key -> "_" + key));
    assertThat(
        newIidSet(instrumentIdArray(1L, 2L, 3L, 4L, 5L))
            .toIidMap(key -> "_" + key),
        iidMapEqualityMatcher(
            iidMapOf(
                instrumentId(1), "_iid 1",
                instrumentId(2), "_iid 2",
                instrumentId(3), "_iid 3",
                instrumentId(4), "_iid 4",
                instrumentId(5), "_iid 5")));
  }

  @Test
  public void testToIidMapWithFilteredKeys() {
    assertEmptyIidMap(emptyIidSet().toIidMapWithFilteredKeys(key -> Optional.of("_" + key)));
    Function<InstrumentId, Optional<String>> onlyKeepOddNumberedIds = v -> v.asLong() % 2 == 0
        ? Optional.empty()
        : Optional.of(Strings.format("_%s", v));
    assertThat(
        newIidSet(instrumentIdArray(1L, 2L, 3L, 4L, 5L))
            .toIidMapWithFilteredKeys(onlyKeepOddNumberedIds),
        iidMapEqualityMatcher(
            iidMapOf(
                instrumentId(1), "_iid 1",
                instrumentId(3), "_iid 3",
                instrumentId(5), "_iid 5")));
    assertEmptyIidMap(newIidSet(instrumentIdArray(1L, 2L, 3L, 4L, 5L))
        .toIidMapWithFilteredKeys(key -> Optional.empty()));
    assertEmptyIidMap(emptyIidSet()
        .toIidMapWithFilteredKeys(key -> Optional.empty()));
  }

  @Test
  public void testOrderedToIidMap() {
    List<InstrumentId> items = newArrayList();
    assertEmptyIidMap(
        emptyIidSet().orderedToIidMap(
            instrumentId -> {
              items.add(instrumentId); // intentional side-effect
              return "_" + instrumentId.asLong();
            }));
    assertThat(
        // intentionally out of order, in case the construction process of an IidSet (which is unordered)
        // is more likely to store things in order if we specify them in order.
        newIidSet(instrumentIdArray(5L, 3L, 1L, 4L, 2L))
            .orderedToIidMap(
                key -> {
                  items.add(key); // intentional side-effect
                  return "_" + key;
                }),
        iidMapEqualityMatcher(
            iidMapOf(
                instrumentId(1), "_iid 1",
                instrumentId(2), "_iid 2",
                instrumentId(3), "_iid 3",
                instrumentId(4), "_iid 4",
                instrumentId(5), "_iid 5")));
    assertEquals(
        "With orderedToIidMap, entries must have been added in increasing instrument ID order",
        newArrayList(instrumentIdArray(1L, 2L, 3L, 4L, 5L)),
        items);
  }

  @Test
  public void testHandlingOfDuplicatesInIput() {
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B),
        newIidSet(STOCK_A, STOCK_B));
    List<InstrumentId> ab = ImmutableList.of(STOCK_A, STOCK_B, STOCK_A);
    assertIllegalArgumentException( () -> newIidSet(STOCK_A, STOCK_B, STOCK_A));
    assertIllegalArgumentException( () -> newIidSet(ab));

    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B),
        newIidSetFromPossibleDuplicates(STOCK_A, STOCK_B, STOCK_A));
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B),
        newIidSetFromPossibleDuplicates(ab));
  }

  @Test
  public void testFilter() {
    IidSet original = newIidSet(instrumentIdArray(1L, 2L, 3L, 4L, 5L));
    BiConsumer<Predicate<InstrumentId>, IidSet> asserter = (predicate, expectedResult) ->
        assertIidSetEquals(expectedResult, original.filter(predicate));

    asserter.accept(iid -> iid.equals(instrumentId(777)),  emptyIidSet());
    asserter.accept(iid -> iid.asLong() % 2 == 0,          newIidSet(instrumentId(2), instrumentId(4)));
    asserter.accept(iid -> iid.asLong() % 2 != 0,          newIidSet(instrumentId(1), instrumentId(3), instrumentId(5)));
    asserter.accept(iid -> iid.equals(instrumentId(5)),    singletonIidSet(instrumentId(5)));
    asserter.accept(iid -> !iid.equals(instrumentId(777)), original);
  }

  @Test
  public void testIfNonEmpty() {
    BiConsumer<IidSet, Long> assertStoresSum = (iidSet, expectedResult) -> {
      LongCounter sumOfItems = longCounter();
      iidSet.ifNonEmpty(nonEmptySet -> sumOfItems.incrementBy(
          nonEmptySet.stream().mapToLong(instrumentId -> instrumentId.asLong()).sum()));
      assertEquals(
          expectedResult.longValue(),
          sumOfItems.get());
    };

    assertStoresSum.accept(
        emptyIidSet(),
        0L);
    assertStoresSum.accept(
        singletonIidSet(instrumentId(100)),
        100L);
    assertStoresSum.accept(
        iidSetOf(instrumentId(1), instrumentId(10), instrumentId(100)),
        longExplained(111, 1 + 10 + 100));
  }

  private <V> void assertEmptyIidMap(IidMap<V> iidMap) {
    assertThat(
        iidMap,
        iidMapEqualityMatcher(emptyIidMap()));
  }

  @Override
  public IidSet makeTrivialObject() {
    return emptyIidSet();
  }

  @Override
  public IidSet makeNontrivialObject() {
    return iidSetOf(STOCK_A, STOCK_B, STOCK_C);
  }

  @Override
  public IidSet makeMatchingNontrivialObject() {
    return iidSetOf(STOCK_A, STOCK_B, STOCK_C);
  }

  @Override
  protected boolean willMatch(IidSet expected, IidSet actual) {
    return iidSetMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<IidSet> iidSetMatcher(IidSet expected) {
    return makeMatcher(expected,
        match(v -> (HasLongSet<InstrumentId>) v, f -> hasLongSetMatcher(f)));
  }

}
