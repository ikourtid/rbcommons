package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.biz.types.collections.ts.TestHasNonEmptyIidSet;
import com.rb.nonbiz.collections.IidSetWithGroupings.IidSetForSingleGrouping;
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
import static com.rb.biz.types.collections.ts.TestHasNonEmptyIidSet.testHasNonEmptyIidSetMatcher;
import static com.rb.nonbiz.collections.IidGroupingsTest.iidGroupingsMatcher;
import static com.rb.nonbiz.collections.IidGroupingsTest.testIidGroupings;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapTest.iidMapMatcher;
import static com.rb.nonbiz.collections.IidSetForSingleGroupingTest.iidSetForSingleGroupingMatcher;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.collections.IidSetWithGroupings.IidSetForSingleGrouping.iidSetForSingleGrouping;
import static com.rb.nonbiz.collections.IidSetWithGroupings.emptyIidSetWithGroupings;
import static com.rb.nonbiz.collections.IidSetWithGroupings.iidSetWithGroupings;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.Match.matchIidSet;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.hamcrest.MatcherAssert.assertThat;

// This test class is not generic, but the publicly exposed static matcher is.
public class IidSetWithGroupingsTest extends RBTestMatcher<IidSetWithGroupings<TestHasNonEmptyIidSet>> {

  @Test
  public void instrumentNotInGroupings_usingConstructorThatThrows() {
    Function<InstrumentId, IidSetWithGroupings<TestHasNonEmptyIidSet>> maker = instrumentId -> iidSetWithGroupings(
        iidSetOf(
            STOCK_A1,
            STOCK_A2,
            STOCK_B1,
            instrumentId),
        testIidGroupings(
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
            new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C")));

    assertIllegalArgumentException( () -> maker.apply(STOCK_D));

    IidSetWithGroupings<TestHasNonEmptyIidSet> doesNotThrow;
    doesNotThrow = maker.apply(STOCK_B3);
    doesNotThrow = maker.apply(STOCK_C1);
  }

  @Test
  public void instrumentNotInGroupings_usingConstructorThatCreatesGroupingsOnTheFly() {
    Function<InstrumentId, IidSetWithGroupings<TestHasNonEmptyIidSet>> maker = instrumentId -> iidSetWithGroupings(
        iidSetOf(
            STOCK_A1,
            STOCK_A2,
            STOCK_B1,
            instrumentId),
        testIidGroupings(
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
            new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C")),
        v -> new TestHasNonEmptyIidSet(singletonIidSet(v), "x"));

    IidSetWithGroupings<TestHasNonEmptyIidSet> doesNotThrow;
    doesNotThrow = maker.apply(STOCK_B3);
    doesNotThrow = maker.apply(STOCK_C1);

    // This is the difference vs. instrumentNotInGroupings_usingConstructorThatThrows;
    // Note that this generates a trivial grouping with only STOCK_D in it
    assertThat(
        maker.apply(STOCK_D),
        iidSetWithGroupingsMatcher(
            iidSetWithGroupings(
                iidSetOf(
                    STOCK_A1,
                    STOCK_A2,
                    STOCK_B1,
                    STOCK_D),
                testIidGroupings(
                    new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
                    new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
                    new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C"),
                    new TestHasNonEmptyIidSet(singletonIidSet(STOCK_D), "x"))),
            f -> testHasNonEmptyIidSetMatcher(f)));
  }

  @Test
  public void testGetGroupedIidMap() {
    TestHasNonEmptyIidSet groupingA = new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A");
    TestHasNonEmptyIidSet groupingB = new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B");
    TestHasNonEmptyIidSet groupingC = new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C");

    IidSetForSingleGrouping<TestHasNonEmptyIidSet> forA = iidSetForSingleGrouping(
        iidSetOf(
            STOCK_A1,
            STOCK_A2),
        groupingA);
    IidSetForSingleGrouping<TestHasNonEmptyIidSet> forB = iidSetForSingleGrouping(
        iidSetOf(
            STOCK_B1,
            STOCK_B2),
        groupingB);

    assertThat(
        iidSetWithGroupings(
            iidSetOf(
                STOCK_A1,
                STOCK_A2,
                STOCK_B1,
                STOCK_B2),
            testIidGroupings(
                groupingA,
                groupingB,
                groupingC))
            .getGroupedIidMap(),
        iidMapMatcher(
            iidMapOf(
                STOCK_A1, forA,
                STOCK_A2, forA,
                STOCK_B1, forB,
                STOCK_B2, forB),
            f -> iidSetForSingleGroupingMatcher(f, f2 -> testHasNonEmptyIidSetMatcher(f2))));
  }

  @Override
  public IidSetWithGroupings<TestHasNonEmptyIidSet> makeTrivialObject() {
    return emptyIidSetWithGroupings();
  }

  @Override
  public IidSetWithGroupings<TestHasNonEmptyIidSet> makeNontrivialObject() {
    return iidSetWithGroupings(
        iidSetOf(
            STOCK_A1,
            STOCK_A2,
            STOCK_B1,
            STOCK_B2),
        testIidGroupings(
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
            new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C")));
  }

  @Override
  public IidSetWithGroupings<TestHasNonEmptyIidSet> makeMatchingNontrivialObject() {
    return iidSetWithGroupings(
        iidSetOf(
            STOCK_A1,
            STOCK_A2,
            STOCK_B1,
            STOCK_B2),
        testIidGroupings(
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
            new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C")));
  }

  @Override
  protected boolean willMatch(IidSetWithGroupings<TestHasNonEmptyIidSet> expected,
                              IidSetWithGroupings<TestHasNonEmptyIidSet> actual) {
    return iidSetWithGroupingsMatcher(expected,
        f -> testHasNonEmptyIidSetMatcher(f))
        .matches(actual);
  }

  public static <S extends HasNonEmptyIidSet> TypeSafeMatcher<IidSetWithGroupings<S>> iidSetWithGroupingsMatcher(
      IidSetWithGroupings<S> expected,
      MatcherGenerator<S> hasNonEmptyIidSetMatcherGenerator) {
    return makeMatcher(expected,
        matchIidSet(v -> v.getTopLevelIidSet()),
        match(      v -> v.getIidGroupings(),   f -> iidGroupingsMatcher(f, hasNonEmptyIidSetMatcherGenerator)),
        // This is calculated, but can't hurt to check
        matchIidMap(v -> v.getGroupedIidMap(), f -> iidSetForSingleGroupingMatcher(
            f, hasNonEmptyIidSetMatcherGenerator)));
  }

}
