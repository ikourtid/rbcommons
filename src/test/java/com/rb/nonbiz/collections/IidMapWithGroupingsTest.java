package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.biz.types.collections.ts.TestHasNonEmptyIidSet;
import com.rb.nonbiz.collections.IidMapWithGroupings.IidMapForSingleGrouping;
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
import static com.rb.biz.types.collections.ts.TestHasNonEmptyIidSet.testHasNonEmptyIidSetMatcher;
import static com.rb.nonbiz.collections.IidGroupingsTest.iidGroupingsMatcher;
import static com.rb.nonbiz.collections.IidGroupingsTest.testIidGroupings;
import static com.rb.nonbiz.collections.IidMapForSingleGroupingTest.iidMapForSingleGroupingMatcher;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapTest.iidMapMatcher;
import static com.rb.nonbiz.collections.IidMapWithGroupings.IidMapForSingleGrouping.iidMapForSingleGrouping;
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
import static org.hamcrest.MatcherAssert.assertThat;

// This test class is not generic, but the publicly exposed static matcher is.
public class IidMapWithGroupingsTest extends RBTestMatcher<IidMapWithGroupings<Double, TestHasNonEmptyIidSet>> {

  @Test
  public void test_getForIidGrouping() {
    TestHasNonEmptyIidSet groupingA = new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A");
    TestHasNonEmptyIidSet groupingB = new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B");
    TestHasNonEmptyIidSet groupingC = new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C");

    BiConsumer<TestHasNonEmptyIidSet, IidMap<Double>> asserter = (iidGrouping, expectedResult) ->
        assertThat(
            iidMapWithGroupings(
                iidMapOf(
                    STOCK_A1, 7.1,
                    STOCK_A2, 7.2,
                    STOCK_B1, 8.1,
                    STOCK_B2, 8.2),
                testIidGroupings(groupingA, groupingB, groupingC),
                v -> new TestHasNonEmptyIidSet(singletonIidSet(v), "x"))
                .getForIidGrouping(iidGrouping),
            iidMapMatcher(
                expectedResult,
                v -> doubleAlmostEqualsMatcher(v, DEFAULT_EPSILON_1e_8)));

    asserter.accept(groupingA, iidMapOf(STOCK_A1, 7.1, STOCK_A2, 7.2));
    asserter.accept(groupingB, iidMapOf(STOCK_B1, 8.1, STOCK_B2, 8.2));
    asserter.accept(groupingC, emptyIidMap());
  }

  @Test
  public void instrumentNotInGroupings_usingConstructorThatThrows() {
    Function<InstrumentId, IidMapWithGroupings<Double, TestHasNonEmptyIidSet>> maker = instrumentId -> iidMapWithGroupings(
        iidMapOf(
            STOCK_A1, 7.1,
            STOCK_A2, 7.2,
            STOCK_B1, 8.1,
            instrumentId, 8.2),
        testIidGroupings(
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
            new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C")));

    assertIllegalArgumentException( () -> maker.apply(STOCK_D));

    IidMapWithGroupings<Double, TestHasNonEmptyIidSet> doesNotThrow;
    doesNotThrow = maker.apply(STOCK_B3);
    doesNotThrow = maker.apply(STOCK_C1);
  }

  @Test
  public void instrumentNotInGroupings_usingConstructorThatCreatesGroupingsOnTheFly() {
    Function<InstrumentId, IidMapWithGroupings<Double, TestHasNonEmptyIidSet>> maker = instrumentId -> iidMapWithGroupings(
        iidMapOf(
            STOCK_A1, 7.1,
            STOCK_A2, 7.2,
            STOCK_B1, 8.1,
            instrumentId, 8.2),
        testIidGroupings(
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
            new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C")),
        v -> new TestHasNonEmptyIidSet(singletonIidSet(v), "x"));

    IidMapWithGroupings<Double, TestHasNonEmptyIidSet> doesNotThrow;
    doesNotThrow = maker.apply(STOCK_B3);
    doesNotThrow = maker.apply(STOCK_C1);

    // This is the difference vs. instrumentNotInGroupings_usingConstructorThatThrows;
    // Note that this generates a trivial grouping with only STOCK_D in it
    assertThat(
        maker.apply(STOCK_D),
        iidMapWithGroupingsMatcher(
            iidMapWithGroupings(
                iidMapOf(
                    STOCK_A1, 7.1,
                    STOCK_A2, 7.2,
                    STOCK_B1, 8.1,
                    STOCK_D, 8.2),
                testIidGroupings(
                    new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
                    new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
                    new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C"),
                    new TestHasNonEmptyIidSet(singletonIidSet(STOCK_D), "x"))),
            f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8),
            f -> testHasNonEmptyIidSetMatcher(f)));
  }

  @Test
  public void testGetGroupedIidMap() {
    TestHasNonEmptyIidSet groupingA = new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A");
    TestHasNonEmptyIidSet groupingB = new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B");
    TestHasNonEmptyIidSet groupingC = new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C");

    IidMapForSingleGrouping<Double, TestHasNonEmptyIidSet> forA = iidMapForSingleGrouping(
        iidMapOf(
            STOCK_A1, 7.1,
            STOCK_A2, 7.2),
        groupingA);
    IidMapForSingleGrouping<Double, TestHasNonEmptyIidSet> forB = iidMapForSingleGrouping(
        iidMapOf(
            STOCK_B1, 8.1,
            STOCK_B2, 8.2),
        groupingB);

    assertThat(
        iidMapWithGroupings(
            iidMapOf(
                STOCK_A1, 7.1,
                STOCK_A2, 7.2,
                STOCK_B1, 8.1,
                STOCK_B2, 8.2),
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
            f -> iidMapForSingleGroupingMatcher(f,
                f2 -> doubleAlmostEqualsMatcher(f2, DEFAULT_EPSILON_1e_8),
                f2 -> testHasNonEmptyIidSetMatcher(f2))));
  }

  @Override
  public IidMapWithGroupings<Double, TestHasNonEmptyIidSet> makeTrivialObject() {
    return emptyIidMapWithGroupings();
  }

  @Override
  public IidMapWithGroupings<Double, TestHasNonEmptyIidSet> makeNontrivialObject() {
    return iidMapWithGroupings(
        iidMapOf(
            STOCK_A1, 7.1,
            STOCK_A2, 7.2,
            STOCK_B1, 8.1,
            STOCK_B2, 8.2),
        testIidGroupings(
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
            new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C")));
  }

  @Override
  public IidMapWithGroupings<Double, TestHasNonEmptyIidSet> makeMatchingNontrivialObject() {
    return iidMapWithGroupings(
        iidMapOf(
            STOCK_A1, 7.1,
            STOCK_A2, 7.2,
            STOCK_B1, 8.1,
            STOCK_B2, 8.2),
        testIidGroupings(
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_A1, STOCK_A2), "A"),
            new TestHasNonEmptyIidSet(iidSetOf(STOCK_B1, STOCK_B2, STOCK_B3), "B"),
            new TestHasNonEmptyIidSet(singletonIidSet(STOCK_C1), "C")));
  }

  @Override
  protected boolean willMatch(IidMapWithGroupings<Double, TestHasNonEmptyIidSet> expected,
                              IidMapWithGroupings<Double, TestHasNonEmptyIidSet> actual) {
    return iidMapWithGroupingsMatcher(expected,
        f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8),
        f -> testHasNonEmptyIidSetMatcher(f))
        .matches(actual);
  }

  public static <V, S extends HasNonEmptyIidSet> TypeSafeMatcher<IidMapWithGroupings<V, S>> iidMapWithGroupingsMatcher(
      IidMapWithGroupings<V, S> expected,
      MatcherGenerator<V> iidMapValueMatcherGenerator,
      MatcherGenerator<S> hasNonEmptyIidSetMatcherGenerator) {
    return makeMatcher(expected,
        matchIidMap(v -> v.getTopLevelIidMap(), iidMapValueMatcherGenerator),
        match(      v -> v.getIidGroupings(),   f -> iidGroupingsMatcher(f, hasNonEmptyIidSetMatcherGenerator)),
        // This is calculated, but can't hurt to check
        matchIidMap(v -> v.getGroupedIidMap(),  f -> iidMapForSingleGroupingMatcher(
            f, iidMapValueMatcherGenerator, hasNonEmptyIidSetMatcherGenerator)));
  }

}
