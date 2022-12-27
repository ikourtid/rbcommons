package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.Money;
import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A2;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.HasInstrumentIdMaps.emptyHasInstrumentIdMap;
import static com.rb.nonbiz.collections.HasInstrumentIdMaps.hasInstrumentIdMapOf;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapTest.iidMapEqualityMatcher;
import static com.rb.nonbiz.collections.IidMapTest.iidMapPreciseValueMatcher;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSet;
import static com.rb.nonbiz.collections.PairTest.pairMatcher;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentId;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentIdMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.unorderedCollectionMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.hasLongMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * This test class is not generic, but the publicly exposed typesafe matcher is.
 */
public class HasInstrumentIdMapTest extends RBTestMatcher<HasInstrumentIdMap<TestHasInstrumentId, Money>> {

  private final HasInstrumentIdMap<TestHasInstrumentId, Money> TEST_MAP = hasInstrumentIdMapOf(
      testHasInstrumentId(instrumentId(1), 1.1), money(11.11),
      testHasInstrumentId(instrumentId(6), 6.6), money(66.66),
      testHasInstrumentId(instrumentId(2), 2.2), money(22.22),
      testHasInstrumentId(instrumentId(5), 5.5), money(55.55),
      testHasInstrumentId(instrumentId(4), 4.4), money(44.44),
      testHasInstrumentId(instrumentId(3), 3.3), money(33.33));

  @Test
  public void testForEachEntry() {
    MutableRBSet<String> stringsSet = newMutableRBSet();
    // I am adding elements in a shuffled order so it's less likely that they will all end up ordered by accident.
    hasInstrumentIdMapOf(
        testHasInstrumentId(instrumentId(111), 1.1), money(11.11),
        testHasInstrumentId(instrumentId(222), 2.2), money(22.22))
        .forEachEntry( (testHasInstrumentId, money) -> stringsSet.add(Strings.format("%s _ %s", testHasInstrumentId, money)));
    assertThat(
        newRBSet(stringsSet),
        rbSetEqualsMatcher(
            rbSetOf("iid 111 1.1 _ $ 11.11", "iid 222 2.2 _ $ 22.22")));
  }

  @Test
  public void testInstrumentIdKeysIterator() {
    // Using newRBSet makes this test ignore the ordering,
    // which is useful since instrumentIdKeysIterator doesn't guarantee an ordering.
    assertEquals(
        newRBSet(TEST_MAP.instrumentIdKeysIterator()),
        rbSetOf(instrumentId(1), instrumentId(2), instrumentId(3), instrumentId(4), instrumentId(5), instrumentId(6)));
  }

  @Test
  public void testSortedInstrumentIdStream() {
    assertEquals(
        TEST_MAP.sortedInstrumentIdStream().collect(Collectors.toList()),
        ImmutableList.of(
            instrumentId(1),
            instrumentId(2),
            instrumentId(3),
            instrumentId(4),
            instrumentId(5),
            instrumentId(6)));
  }

  @Test
  public void testValuesStream() {
    assertThat(
        TEST_MAP.valuesStream().collect(Collectors.toList()),
        unorderedCollectionMatcher(
            ImmutableList.of(money(11.11), money(22.22), money(33.33), money(44.44), money(55.55), money(66.66)),
            f -> preciseValueMatcher(f, DEFAULT_EPSILON_1e_8),
            Money::compareTo));
  }

  @Test
  public void testTransformedStream() {
    HasInstrumentIdMap<TestHasInstrumentId, Money> map = hasInstrumentIdMapOf(
        testHasInstrumentId(instrumentId(111), 1.1), money(11.11),
        testHasInstrumentId(instrumentId(222), 2.2), money(22.22));
    assertEquals(
        newRBSet(map.transformedStream( (testHasInstrumentId, money) ->
            Strings.format("%s _ %s", testHasInstrumentId.toString(), money.toString()))),
        rbSetOf("iid 111 1.1 _ $ 11.11", "iid 222 2.2 _ $ 22.22"));
  }

  @Test
  public void testGetHasInstrumentIdOrThrow() {
    assertThat(
        TEST_MAP.getHasInstrumentIdOrThrow(instrumentId(1)),
        testHasInstrumentIdMatcher(
            testHasInstrumentId(instrumentId(1), 1.1)));
    assertThat(
        TEST_MAP.getHasInstrumentIdOrThrow(instrumentId(1), "msg"),
        testHasInstrumentIdMatcher(
            testHasInstrumentId(instrumentId(1), 1.1)));
    assertIllegalArgumentException( () -> TEST_MAP.getHasInstrumentIdOrThrow(instrumentId(999)));
    assertIllegalArgumentException( () -> TEST_MAP.getHasInstrumentIdOrThrow(instrumentId(999), "msg"));
  }

  @Test
  public void testGetValueOrThrow() {
    assertAlmostEquals(TEST_MAP.getValueOrThrow(instrumentId(1)),        money(11.11), 1e-8);
    assertAlmostEquals(TEST_MAP.getValueOrThrow(instrumentId(1), "msg"), money(11.11), 1e-8);
    assertIllegalArgumentException( () -> TEST_MAP.getValueOrThrow(instrumentId(999)));
    assertIllegalArgumentException( () -> TEST_MAP.getValueOrThrow(instrumentId(999), "msg"));
  }

  @Test
  public void testGetValueOrDefault() {
    assertAlmostEquals(TEST_MAP.getValueOrDefault(instrumentId(1),   money(99.99)), money(11.11), 1e-8);
    assertAlmostEquals(TEST_MAP.getValueOrDefault(instrumentId(999), money(99.99)), money(99.99), 1e-8);
  }

  @Test
  public void testForEachInInstrumentIdOrder() {
    StringBuilder sb = new StringBuilder();
    TEST_MAP.forEachInInstrumentIdOrder( (testHasInstrumentId, money) -> sb.append(
        Strings.format("%s %s|", testHasInstrumentId.toString(), money.toString())));
    assertEquals(
        "iid 1 1.1 $ 11.11|iid 2 2.2 $ 22.22|iid 3 3.3 $ 33.33|iid 4 4.4 $ 44.44|iid 5 5.5 $ 55.55|iid 6 6.6 $ 66.66|",
        sb.toString());
  }

  @Test
  public void testTransformEntriesCopy() {
    assertThat(
        TEST_MAP.transformEntriesCopy( (testHasInstrumentId, money) ->
            Strings.format("%s _ %s", testHasInstrumentId.toString(), money.add(money(100)).toString())),
        hasInstrumentIdMapMatcher(
            hasInstrumentIdMapOf(
                testHasInstrumentId(instrumentId(1), 1.1), "iid 1 1.1 _ $ 111.11",
                testHasInstrumentId(instrumentId(6), 6.6), "iid 6 6.6 _ $ 166.66",
                testHasInstrumentId(instrumentId(2), 2.2), "iid 2 2.2 _ $ 122.22",
                testHasInstrumentId(instrumentId(5), 5.5), "iid 5 5.5 _ $ 155.55",
                testHasInstrumentId(instrumentId(4), 4.4), "iid 4 4.4 _ $ 144.44",
                testHasInstrumentId(instrumentId(3), 3.3), "iid 3 3.3 _ $ 133.33"),
            k -> testHasInstrumentIdMatcher(k),
            v -> typeSafeEqualTo(v)));
  }

  @Test
  public void testToIidMap() {
    assertThat(
        emptyHasInstrumentIdMap().toIidMap(),
        iidMapEqualityMatcher(emptyIidMap()));
    assertThat(
        hasInstrumentIdMapOf(
            testHasInstrumentId(STOCK_A1, 1.1), money(11.11),
            testHasInstrumentId(STOCK_A2, 2.2), money(22.22))
            .toIidMap(),
        iidMapPreciseValueMatcher(
            iidMapOf(
                STOCK_A1, money(11.11),
                STOCK_A2, money(22.22)),
            1e-8));
  }

  @Override
  public HasInstrumentIdMap<TestHasInstrumentId, Money> makeTrivialObject() {
    return emptyHasInstrumentIdMap();
  }

  @Override
  public HasInstrumentIdMap<TestHasInstrumentId, Money> makeNontrivialObject() {
    return hasInstrumentIdMapOf(
        testHasInstrumentId(STOCK_A1, 1.1), money(11.11),
        testHasInstrumentId(STOCK_A2, 2.2), money(22.22));
  }

  @Override
  public HasInstrumentIdMap<TestHasInstrumentId, Money> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return hasInstrumentIdMapOf(
        testHasInstrumentId(STOCK_A1, 1.1 + e), money(11.11 + e),
        testHasInstrumentId(STOCK_A2, 2.2 + e), money(22.22 + e));
  }

  @Override
  protected boolean willMatch(HasInstrumentIdMap<TestHasInstrumentId, Money> expected,
                              HasInstrumentIdMap<TestHasInstrumentId, Money> actual) {
    return hasInstrumentIdMapMatcher(expected, f1 -> testHasInstrumentIdMatcher(f1), f2 -> preciseValueMatcher(f2, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <T extends HasInstrumentId, V> TypeSafeMatcher<HasInstrumentIdMap<T, V>> hasInstrumentIdMapMatcher(
      HasInstrumentIdMap<T, V> expected, MatcherGenerator<T> keyMatcherGenerator, MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected, actual ->
        hasLongMapMatcher(expected, f -> pairMatcher(f, keyMatcherGenerator, valueMatcherGenerator))
            .matches(actual));
  }

  public static <T extends HasInstrumentId, V> TypeSafeMatcher<HasInstrumentIdMap<T, V>>
  hasInstrumentIdMapEqualityMatcher(HasInstrumentIdMap<T, V> expected) {
    return hasInstrumentIdMapMatcher(expected, f1 -> typeSafeEqualTo(f1), f2 -> typeSafeEqualTo(f2));
  }

}
