package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.biz.types.collections.ts.TestHasNonEmptyIidSet;
import com.rb.nonbiz.collections.IidSetWithGroupings.IidSetForSingleGrouping;
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
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.collections.IidSetWithGroupings.IidSetForSingleGrouping.iidSetForSingleGrouping;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchIidSet;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;

// This test class is not generic, but the publicly exposed static matcher is.
public class IidSetForSingleGroupingTest extends RBTestMatcher<IidSetForSingleGrouping<TestHasNonEmptyIidSet>> {

  @Test
  public void itemNotInGrouping_throws() {
    Function<InstrumentId, IidSetForSingleGrouping<TestHasNonEmptyIidSet>> maker = instrumentId ->
        iidSetForSingleGrouping(
            iidSetOf(
                STOCK_A1,
                instrumentId),
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2, STOCK_A3), DUMMY_STRING));
    assertIllegalArgumentException( () -> maker.apply(STOCK_B));
    IidSetForSingleGrouping<TestHasNonEmptyIidSet> doesNotThrow;
    doesNotThrow = maker.apply(STOCK_A2);
    doesNotThrow = maker.apply(STOCK_A3);
  }

  @Override
  public IidSetForSingleGrouping<TestHasNonEmptyIidSet> makeTrivialObject() {
    return iidSetForSingleGrouping(emptyIidSet(), new TestHasNonEmptyIidSet(singletonIidSet(STOCK_A), ""));
  }

  @Override
  public IidSetForSingleGrouping<TestHasNonEmptyIidSet> makeNontrivialObject() {
    return iidSetForSingleGrouping(
        iidSetOf(
            STOCK_A1,
            STOCK_A2),
        new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2, STOCK_A3), "xyz"));
  }

  @Override
  public IidSetForSingleGrouping<TestHasNonEmptyIidSet> makeMatchingNontrivialObject() {
    return iidSetForSingleGrouping(
        iidSetOf(
            STOCK_A1,
            STOCK_A2),
        new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2, STOCK_A3), "xyz"));
  }

  @Override
  protected boolean willMatch(IidSetForSingleGrouping<TestHasNonEmptyIidSet> expected,
                              IidSetForSingleGrouping<TestHasNonEmptyIidSet> actual) {
    return iidSetForSingleGroupingMatcher(
        expected,
        f -> testHasNonEmptyIidSetMatcher(f))
        .matches(actual);
  }

  public static <S extends HasNonEmptyIidSet> TypeSafeMatcher<IidSetForSingleGrouping<S>> iidSetForSingleGroupingMatcher(
      IidSetForSingleGrouping<S> expected,
      MatcherGenerator<S> hasIidSetMatcherGenerator) {
    return makeMatcher(expected,
        matchIidSet(v -> v.getIidSet()),
        match(      v -> v.getIidGrouping(), hasIidSetMatcherGenerator));
  }

}
