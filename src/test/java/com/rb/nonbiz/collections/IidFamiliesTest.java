package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.biz.types.collections.ts.TestHasIidSet;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A2;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B2;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B3;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.types.collections.ts.TestHasIidSet.testHasIidSetMatcher;
import static com.rb.nonbiz.collections.IidFamilies.emptyIidFamilies;
import static com.rb.nonbiz.collections.IidFamilies.iidFamilies;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static java.util.Collections.singletonList;
import static org.junit.Assert.fail;

// This test class is not generic, but the publicly exposed static matcher is.
public class IidFamiliesTest extends RBTestMatcher<IidFamilies<TestHasIidSet>> {

  public static <V extends HasIidSet> IidFamilies<V> singletonIidFamilies(V onlyItem) {
    return iidFamilies(singletonList(onlyItem));
  }

  @SafeVarargs
  public static <V extends HasIidSet> IidFamilies<V> testIidFamilies(
      V first,
      V second,
      V ... rest) {
    return iidFamilies(concatenateFirstSecondAndRest(first, second, rest));
  }

  @Test
  public void duplicateInstrument_throws() {
    Function<InstrumentId, IidFamilies<TestHasIidSet>> maker = instrumentId ->
        testIidFamilies(
            new TestHasIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasIidSet(iidSetOf(STOCK_B1, instrumentId), "B"));

    assertIllegalArgumentException( () -> maker.apply(STOCK_A1));
    assertIllegalArgumentException( () -> maker.apply(STOCK_A2));
    assertIllegalArgumentException( () -> maker.apply(STOCK_B1)); // different failure reason; an IidSet can't have the same iid twice
    IidFamilies<TestHasIidSet> doesNotThrow = maker.apply(STOCK_B2);
  }

  @Test
  public void emptyIidSet_throws() {
    Function<IidSet, IidFamilies<TestHasIidSet>> maker = iidSet2 ->
        testIidFamilies(
            new TestHasIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasIidSet(iidSet2, "B"));
    assertIllegalArgumentException( () -> maker.apply(emptyIidSet()));
    IidFamilies<TestHasIidSet> doesNotThrow;
    doesNotThrow = maker.apply(singletonIidSet(STOCK_B1));
    doesNotThrow = maker.apply(iidSetOf(STOCK_B1, STOCK_B2));
  }

  @Test
  public void testGetOptionalSiblings() {
    TestHasIidSet hasIidSetA = new TestHasIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A");
    TestHasIidSet hasIidSetB = new TestHasIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B");
    TestHasIidSet hasIidSetC = new TestHasIidSet(singletonIidSet(STOCK_C1), "C");

    IidFamilies<TestHasIidSet> testIidFamilies = testIidFamilies(hasIidSetA, hasIidSetB, hasIidSetC);

    assertOptionalEmpty(testIidFamilies.getOptionalSiblings(STOCK_D));

    BiConsumer<InstrumentId, >
  }

  @Override
  public IidFamilies<TestHasIidSet> makeTrivialObject() {
    return emptyIidFamilies();
  }

  @Override
  public IidFamilies<TestHasIidSet> makeNontrivialObject() {
    return testIidFamilies(
        new TestHasIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
        new TestHasIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
        new TestHasIidSet(singletonIidSet(STOCK_C1), "C"));
  }

  @Override
  public IidFamilies<TestHasIidSet> makeMatchingNontrivialObject() {
    // Nothing to tweak here, although we could have generalized by creating a variant of TestHasIidSet
    // that also stores e.g. a double instead of a string.
    return testIidFamilies(
        new TestHasIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
        new TestHasIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
        new TestHasIidSet(singletonIidSet(STOCK_C1), "C"));
  }

  @Override
  protected boolean willMatch(IidFamilies<TestHasIidSet> expected, IidFamilies<TestHasIidSet> actual) {
    return testIidFamiliesMatcher(expected, f -> testHasIidSetMatcher(f)).matches(actual);
  }

  public static <V extends HasIidSet> TypeSafeMatcher<IidFamilies<V>> testIidFamiliesMatcher(
      IidFamilies<V> expected, MatcherGenerator<V> matcherGenerator) {
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
