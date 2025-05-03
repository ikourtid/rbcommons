package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.biz.types.collections.ts.TestHasIidSet;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A2;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A3;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.types.collections.ts.TestHasIidSet.testHasIidSetMatcher;
import static com.rb.nonbiz.collections.IidMapForSingleGrouping.iidMapForSingleGrouping;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

// This test class is not generic, but the publicly exposed static matcher is.
public class IidMapForSingleGroupingTest extends RBTestMatcher<IidMapForSingleGrouping<Double, TestHasIidSet>> {

  @Test
  public void itemNotInGrouping_throws() {
    Function<InstrumentId, IidMapForSingleGrouping<Double, TestHasIidSet>> maker = instrumentId ->
        iidMapForSingleGrouping(
            iidMapOf(
                STOCK_A1,     1.1,
                instrumentId, 2.2),
            new TestHasIidSet(iidSetOf(STOCK_A1, STOCK_A2, STOCK_A3), DUMMY_STRING));
    assertIllegalArgumentException( () -> maker.apply(STOCK_B));
    IidMapForSingleGrouping<Double, TestHasIidSet> doesNotThrow;
    doesNotThrow = maker.apply(STOCK_A2);
    doesNotThrow = maker.apply(STOCK_A3);
  }

  @Override
  public IidMapForSingleGrouping<Double, TestHasIidSet> makeTrivialObject() {
    return iidMapForSingleGrouping(emptyIidMap(), new TestHasIidSet(emptyIidSet(), ""));
  }

  @Override
  public IidMapForSingleGrouping<Double, TestHasIidSet> makeNontrivialObject() {
    return iidMapForSingleGrouping(
        iidMapOf(
            STOCK_A1, 1.1,
            STOCK_A2, 2.2),
        new TestHasIidSet(iidSetOf(STOCK_A1, STOCK_A2, STOCK_A3), "xyz"));
  }

  @Override
  public IidMapForSingleGrouping<Double, TestHasIidSet> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return iidMapForSingleGrouping(
        iidMapOf(
            STOCK_A1, 1.1 + e,
            STOCK_A2, 2.2 + e),
        new TestHasIidSet(iidSetOf(STOCK_A1, STOCK_A2, STOCK_A3), "xyz"));
  }

  @Override
  protected boolean willMatch(IidMapForSingleGrouping<Double, TestHasIidSet> expected,
                              IidMapForSingleGrouping<Double, TestHasIidSet> actual) {
    return iidMapForSingleGroupingMatcher(
        expected,
        f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8),
        f -> testHasIidSetMatcher(f))
        .matches(actual);
  }

  public static <V, S extends HasIidSet> TypeSafeMatcher<IidMapForSingleGrouping<V, S>> iidMapForSingleGroupingMatcher(
      IidMapForSingleGrouping<V, S> expected,
      MatcherGenerator<V> iidMapValueMatcherGenerator,
      MatcherGenerator<S> hasIidSetMatcherGenerator) {
    return makeMatcher(expected,
        matchIidMap(v -> v.getIidMap(), iidMapValueMatcherGenerator),
        match(v -> v.getIidGrouping(), hasIidSetMatcherGenerator));
  }

}
