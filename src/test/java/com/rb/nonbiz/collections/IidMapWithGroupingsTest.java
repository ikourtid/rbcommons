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
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B2;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B3;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.types.collections.ts.TestHasIidSet.testHasIidSetMatcher;
import static com.rb.nonbiz.collections.IidGroupingsTest.testIidGroupings;
import static com.rb.nonbiz.collections.IidGroupingsTest.testIidGroupingsMatcher;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapWithGroupings.emptyIidMapWithGroupings;
import static com.rb.nonbiz.collections.IidMapWithGroupings.iidMapWithGroupings;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.junit.Assert.fail;

// This test class is not generic, but the publicly exposed static matcher is.
public class IidMapWithGroupingsTest extends RBTestMatcher<IidMapWithGroupings<Double, TestHasIidSet>> {

  @Test
  public void instrumentNotInGroupings_throws() {
    Function<InstrumentId, IidMapWithGroupings<Double, TestHasIidSet>> maker = instrumentId -> iidMapWithGroupings(
        iidMapOf(
            STOCK_A1, 7.1,
            STOCK_A2, 7.2,
            STOCK_B1, 8.1,
            instrumentId, 8.2),
        testIidGroupings(
            new TestHasIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
            new TestHasIidSet(singletonIidSet(STOCK_C1), "C")));

    assertIllegalArgumentException( () -> maker.apply(STOCK_D));

    IidMapWithGroupings<Double, TestHasIidSet> doesNotThrow;
    doesNotThrow = maker.apply(STOCK_B3);
    doesNotThrow = maker.apply(STOCK_C1);
  }

  @Override
  public IidMapWithGroupings<Double, TestHasIidSet> makeTrivialObject() {
    return emptyIidMapWithGroupings();
  }

  @Override
  public IidMapWithGroupings<Double, TestHasIidSet> makeNontrivialObject() {
    return iidMapWithGroupings(
        iidMapOf(
            STOCK_A1, 7.1,
            STOCK_A2, 7.2,
            STOCK_B1, 8.1,
            STOCK_B2, 8.2),
        testIidGroupings(
            new TestHasIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
            new TestHasIidSet(singletonIidSet(STOCK_C1), "C")));
  }

  @Override
  public IidMapWithGroupings<Double, TestHasIidSet> makeMatchingNontrivialObject() {
    return iidMapWithGroupings(
        iidMapOf(
            STOCK_A1, 7.1,
            STOCK_A2, 7.2,
            STOCK_B1, 8.1,
            STOCK_B2, 8.2),
        testIidGroupings(
            new TestHasIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
            new TestHasIidSet(singletonIidSet(STOCK_C1), "C")));
  }

  @Override
  protected boolean willMatch(IidMapWithGroupings<Double, TestHasIidSet> expected,
                              IidMapWithGroupings<Double, TestHasIidSet> actual) {
    return iidMapWithGroupingsMatcher(expected,
        f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8),
        f -> testHasIidSetMatcher(f))
        .matches(actual);
  }

  public static <V, S extends HasIidSet> TypeSafeMatcher<IidMapWithGroupings<V, S>> iidMapWithGroupingsMatcher(
      IidMapWithGroupings<V, S> expected,
      MatcherGenerator<V> iidMapValueMatcherGenerator,
      MatcherGenerator<S> hasIidSetMatcherGenerator) {
    return makeMatcher(expected,
        matchIidMap(v -> v.getTopLevelIidMap(), iidMapValueMatcherGenerator),
        match(      v -> v.getIidGroupings(), f -> testIidGroupingsMatcher(f, hasIidSetMatcherGenerator)));
  }

}
