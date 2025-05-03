package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.biz.types.collections.ts.TestHasNonEmptyIidSet;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.rb.biz.marketdata.FakeInstruments.*;
import static com.rb.biz.types.collections.ts.TestHasNonEmptyIidSet.testHasNonEmptyIidSetMatcher;
import static com.rb.nonbiz.collections.IidGroupings.emptyIidGroupings;
import static com.rb.nonbiz.collections.IidGroupings.iidGroupings;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.collections.IidSetTest.iidSetMatcher;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalNonEmpty;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// This test class is not generic, but the publicly exposed static matcher is.
public class IidGroupingsTest extends RBTestMatcher<IidGroupings<TestHasNonEmptyIidSet>> {

  public static <S extends HasNonEmptyIidSet> IidGroupings<S> singletonIidGroupings(S onlyItem) {
    return iidGroupings(singletonList(onlyItem));
  }

  @SafeVarargs
  public static <S extends HasNonEmptyIidSet> IidGroupings<S> testIidGroupings(
      S first,
      S second,
      S... rest) {
    return iidGroupings(concatenateFirstSecondAndRest(first, second, rest));
  }

  @Test
  public void duplicateInstrument_throws() {
    Function<InstrumentId, IidGroupings<TestHasNonEmptyIidSet>> maker = instrumentId ->
        testIidGroupings(
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, instrumentId), "B"));

    assertIllegalArgumentException( () -> maker.apply(STOCK_A1));
    assertIllegalArgumentException( () -> maker.apply(STOCK_A2));
    assertIllegalArgumentException( () -> maker.apply(STOCK_B1)); // different failure reason; an IidSet can't have the same iid twice
    IidGroupings<TestHasNonEmptyIidSet> doesNotThrow = maker.apply(STOCK_B2);
  }

  @Test
  public void emptyIidSet_throws() {
    Function<IidSet, IidGroupings<TestHasNonEmptyIidSet>> maker = iidSet2 ->
        testIidGroupings(
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasNonEmptyIidSet(iidSet2, "B"));
    assertIllegalArgumentException( () -> maker.apply(emptyIidSet()));
    IidGroupings<TestHasNonEmptyIidSet> doesNotThrow;
    doesNotThrow = maker.apply(singletonIidSet(STOCK_B1));
    doesNotThrow = maker.apply(iidSetOf(STOCK_B1, STOCK_B2));
  }

  @Test
  public void testContainsInstrument() {
    IidGroupings<TestHasNonEmptyIidSet> testIidGroupings = testIidGroupings(
        new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), DUMMY_STRING),
        new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), DUMMY_STRING),
        new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), DUMMY_STRING));

    iidSetOf(STOCK_A1, STOCK_A2, STOCK_B1, STOCK_B2, STOCK_C1)
        .forEach(instrumentId -> assertTrue(testIidGroupings.containsInstrument(instrumentId)));
    assertFalse(testIidGroupings.containsInstrument(STOCK_A3));
    assertFalse(testIidGroupings.containsInstrument(STOCK_B4));
    assertFalse(testIidGroupings.containsInstrument(STOCK_C2));
  }

  @Test
  public void testGetOptionalSiblingsExcludingSelf() {
    TestHasNonEmptyIidSet hasNonEmptyIidSetA = new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A");
    TestHasNonEmptyIidSet hasNonEmptyIidSetB = new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B");
    TestHasNonEmptyIidSet hasNonEmptyIidSetC = new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C");

    IidGroupings<TestHasNonEmptyIidSet> testIidGroupings = testIidGroupings(hasNonEmptyIidSetA, hasNonEmptyIidSetB, hasNonEmptyIidSetC);

    assertOptionalEmpty(testIidGroupings.getOptionalSiblingsExcludingSelf(STOCK_D));

    BiConsumer<InstrumentId, IidSet> asserter = (instrumentId, expectedResult) ->
        assertOptionalNonEmpty(
            testIidGroupings.getOptionalSiblingsExcludingSelf(instrumentId),
            iidSetMatcher(expectedResult));

    asserter.accept(STOCK_A1, singletonIidSet(STOCK_A2));
    asserter.accept(STOCK_A2, singletonIidSet(STOCK_A1));
    asserter.accept(STOCK_B1, iidSetOf(STOCK_B2, STOCK_B3));
    asserter.accept(STOCK_B2, iidSetOf(STOCK_B1, STOCK_B3));
    asserter.accept(STOCK_B3, iidSetOf(STOCK_B1, STOCK_B2));
    asserter.accept(STOCK_C1, emptyIidSet());
  }

  @Test
  public void testGetOptionalSiblingsIncludingSelf() {
    TestHasNonEmptyIidSet hasNonEmptyIidSetA = new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A");
    TestHasNonEmptyIidSet hasNonEmptyIidSetB = new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B");
    TestHasNonEmptyIidSet hasNonEmptyIidSetC = new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C");

    IidGroupings<TestHasNonEmptyIidSet> testIidGroupings = testIidGroupings(hasNonEmptyIidSetA, hasNonEmptyIidSetB, hasNonEmptyIidSetC);

    assertOptionalEmpty(testIidGroupings.getOptionalSiblingsIncludingSelf(STOCK_D));

    BiConsumer<InstrumentId, IidSet> asserter = (instrumentId, expectedResult) ->
        assertOptionalNonEmpty(
            testIidGroupings.getOptionalSiblingsIncludingSelf(instrumentId),
            iidSetMatcher(expectedResult));

    asserter.accept(STOCK_A1, iidSetOf(STOCK_A1, STOCK_A2));
    asserter.accept(STOCK_A2, iidSetOf(STOCK_A1, STOCK_A2));
    asserter.accept(STOCK_B1, iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3));
    asserter.accept(STOCK_B2, iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3));
    asserter.accept(STOCK_B3, iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3));
    asserter.accept(STOCK_C1, singletonIidSet(STOCK_C1));
  }

  @Test
  public void testGetSiblingsExcludingSelfOrThrow() {
    TestHasNonEmptyIidSet hasNonEmptyIidSetA = new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A");
    TestHasNonEmptyIidSet hasNonEmptyIidSetB = new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B");
    TestHasNonEmptyIidSet hasNonEmptyIidSetC = new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C");

    IidGroupings<TestHasNonEmptyIidSet> testIidGroupings = testIidGroupings(hasNonEmptyIidSetA, hasNonEmptyIidSetB, hasNonEmptyIidSetC);

    assertIllegalArgumentException( () -> testIidGroupings.getSiblingsExcludingSelfOrThrow(STOCK_D));

    BiConsumer<InstrumentId, IidSet> asserter = (instrumentId, expectedResult) ->
        assertThat(
            testIidGroupings.getSiblingsExcludingSelfOrThrow(instrumentId),
            iidSetMatcher(expectedResult));

    asserter.accept(STOCK_A1, singletonIidSet(STOCK_A2));
    asserter.accept(STOCK_A2, singletonIidSet(STOCK_A1));
    asserter.accept(STOCK_B1, iidSetOf(STOCK_B2, STOCK_B3));
    asserter.accept(STOCK_B2, iidSetOf(STOCK_B1, STOCK_B3));
    asserter.accept(STOCK_B3, iidSetOf(STOCK_B1, STOCK_B2));
    asserter.accept(STOCK_C1, emptyIidSet());
  }

  @Test
  public void testGetSiblingsIncludingSelfIncludingSelf() {
    TestHasNonEmptyIidSet hasNonEmptyIidSetA = new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A");
    TestHasNonEmptyIidSet hasNonEmptyIidSetB = new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B");
    TestHasNonEmptyIidSet hasNonEmptyIidSetC = new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C");

    IidGroupings<TestHasNonEmptyIidSet> testIidGroupings = testIidGroupings(hasNonEmptyIidSetA, hasNonEmptyIidSetB, hasNonEmptyIidSetC);

    assertIllegalArgumentException( () -> testIidGroupings.getSiblingsIncludingSelfOrThrow(STOCK_D));

    BiConsumer<InstrumentId, IidSet> asserter = (instrumentId, expectedResult) ->
        assertThat(
            testIidGroupings.getSiblingsIncludingSelfOrThrow(instrumentId),
            iidSetMatcher(expectedResult));

    asserter.accept(STOCK_A1, iidSetOf(STOCK_A1, STOCK_A2));
    asserter.accept(STOCK_A2, iidSetOf(STOCK_A1, STOCK_A2));
    asserter.accept(STOCK_B1, iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3));
    asserter.accept(STOCK_B2, iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3));
    asserter.accept(STOCK_B3, iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3));
    asserter.accept(STOCK_C1, singletonIidSet(STOCK_C1));
  }

  @Override
  public IidGroupings<TestHasNonEmptyIidSet> makeTrivialObject() {
    return emptyIidGroupings();
  }

  @Override
  public IidGroupings<TestHasNonEmptyIidSet> makeNontrivialObject() {
    return testIidGroupings(
        new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
        new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
        new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C"));
  }

  @Override
  public IidGroupings<TestHasNonEmptyIidSet> makeMatchingNontrivialObject() {
    // Nothing to tweak here, although we could have generalized by creating a variant of TestHasNonEmptyIidSet
    // that also stores e.g. a double instead of a string.
    return testIidGroupings(
        new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
        new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
        new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C"));
  }

  @Override
  protected boolean willMatch(IidGroupings<TestHasNonEmptyIidSet> expected, IidGroupings<TestHasNonEmptyIidSet> actual) {
    return iidGroupingsMatcher(expected, f -> testHasNonEmptyIidSetMatcher(f)).matches(actual);
  }

  public static <S extends HasNonEmptyIidSet> TypeSafeMatcher<IidGroupings<S>> iidGroupingsMatcher(
      IidGroupings<S> expected, MatcherGenerator<S> matcherGenerator) {
    return makeMatcher(expected,
        // Notes:
        // 1. We use a list for determinism, so the ordering inside the list shouldn't affect whether two objects should
        // count as matching or not. However, it can't hurt to be extra conservative here. Worst case, this will
        // create some 'false positive' failing unit tests.
        matchList(v -> v.getRawList(), matcherGenerator),
        // 2. We don't really need to check this because it's a calculated value, but it can't hurt.
        matchIidMap(v -> v.getRawMap(), matcherGenerator));
  }

}
