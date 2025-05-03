package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.biz.types.collections.ts.TestHasNonEmptyIidSet;
import com.rb.nonbiz.collections.IidMapWithGroupings.IidMapForSingleGrouping;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A2;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A3;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.types.collections.ts.TestHasNonEmptyIidSet.testHasNonEmptyIidSetMatcher;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapWithGroupings.IidMapForSingleGrouping.iidMapForSingleGrouping;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

// This test class is not generic, but the publicly exposed static matcher is.
public class IidMapForSingleGroupingTest extends RBTestMatcher<IidMapForSingleGrouping<Double, TestHasNonEmptyIidSet>> {

  @Test
  public void itemNotInGrouping_throws() {
    Function<InstrumentId, IidMapForSingleGrouping<Double, TestHasNonEmptyIidSet>> maker = instrumentId ->
        iidMapForSingleGrouping(
            iidMapOf(
                STOCK_A1,     1.1,
                instrumentId, 2.2),
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2, STOCK_A3), DUMMY_STRING));
    assertIllegalArgumentException( () -> maker.apply(STOCK_B));
    IidMapForSingleGrouping<Double, TestHasNonEmptyIidSet> doesNotThrow;
    doesNotThrow = maker.apply(STOCK_A2);
    doesNotThrow = maker.apply(STOCK_A3);
  }

  @Override
  public IidMapForSingleGrouping<Double, TestHasNonEmptyIidSet> makeTrivialObject() {
    return iidMapForSingleGrouping(emptyIidMap(), new TestHasNonEmptyIidSet(singletonIidSet(STOCK_A), ""));
  }

  @Override
  public IidMapForSingleGrouping<Double, TestHasNonEmptyIidSet> makeNontrivialObject() {
    return iidMapForSingleGrouping(
        iidMapOf(
            STOCK_A1, 1.1,
            STOCK_A2, 2.2),
        new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2, STOCK_A3), "xyz"));
  }

  @Override
  public IidMapForSingleGrouping<Double, TestHasNonEmptyIidSet> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return iidMapForSingleGrouping(
        iidMapOf(
            STOCK_A1, 1.1 + e,
            STOCK_A2, 2.2 + e),
        new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2, STOCK_A3), "xyz"));
  }

  @Override
  protected boolean willMatch(IidMapForSingleGrouping<Double, TestHasNonEmptyIidSet> expected,
                              IidMapForSingleGrouping<Double, TestHasNonEmptyIidSet> actual) {
    return iidMapForSingleGroupingMatcher(
        expected,
        f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8),
        f -> testHasNonEmptyIidSetMatcher(f))
        .matches(actual);
  }

  public static <V, S extends HasNonEmptyIidSet> TypeSafeMatcher<IidMapForSingleGrouping<V, S>> iidMapForSingleGroupingMatcher(
      IidMapForSingleGrouping<V, S> expected,
      MatcherGenerator<V> iidMapValueMatcherGenerator,
      MatcherGenerator<S> hasIidSetMatcherGenerator) {
    return makeMatcher(expected,
        matchIidMap(v -> v.getIidMap(),      iidMapValueMatcherGenerator),
        match(      v -> v.getIidGrouping(), hasIidSetMatcherGenerator));
  }

}
