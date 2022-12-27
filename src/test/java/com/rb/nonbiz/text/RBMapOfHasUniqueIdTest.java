package com.rb.nonbiz.text;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.PairTest.pairMatcher;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.unorderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.RBMapsOfHasUniqueId.emptyRBMapOfHasUniqueId;
import static com.rb.nonbiz.text.RBMapsOfHasUniqueId.rbMapOfHasUniqueIdOf;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueId;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueIdMatcher;
import static com.rb.nonbiz.text.UniqueId.uniqueId;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static java.util.Comparator.comparing;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

// This test class is not generic, but the publicly exposed static typesafe matcher is.
public class RBMapOfHasUniqueIdTest extends RBTestMatcher<RBMapOfHasUniqueId<TestHasUniqueId, Double>> {

  @Test
  public void testGetOrThrow() {
    RBMapOfHasUniqueId<TestHasUniqueId, Double> map = rbMapOfHasUniqueIdOf(
        testHasUniqueId(uniqueId("a"), unitFraction(0.11)), 1.1,
        testHasUniqueId(uniqueId("b"), unitFraction(0.22)), 2.2);
    assertEquals(1.1, map.getOrThrow(testHasUniqueId(uniqueId("a"), unitFraction(0.11))), 1e-8);
    assertEquals(2.2, map.getOrThrow(testHasUniqueId(uniqueId("b"), unitFraction(0.22))), 1e-8);
    // This is desired; despite unitFraction(0.33), only the unique ID matters
    assertEquals(2.2, map.getOrThrow(testHasUniqueId(uniqueId("b"), unitFraction(0.33))));
    assertIllegalArgumentException( () -> map.getOrThrow(testHasUniqueId(uniqueId("c"), unitFraction(0.22))));
    assertIllegalArgumentException( () -> map.getOrThrow(testHasUniqueId(uniqueId("c"), unitFraction(0.33))));
  }

  @Test
  public void testGetOrDefault() {
    RBMapOfHasUniqueId<TestHasUniqueId, Double> map = rbMapOfHasUniqueIdOf(
        testHasUniqueId(uniqueId("a"), unitFraction(0.11)), 1.1,
        testHasUniqueId(uniqueId("b"), unitFraction(0.22)), 2.2);
    assertEquals(1.1, map.getOrDefault(testHasUniqueId(uniqueId("a"), unitFraction(0.11)), 7.7), 1e-8);
    assertEquals(2.2, map.getOrDefault(testHasUniqueId(uniqueId("b"), unitFraction(0.22)), 7.7), 1e-8);
    // This is desired; despite unitFraction(0.33), only the unique ID matters
    assertEquals(2.2, map.getOrDefault(testHasUniqueId(uniqueId("b"), unitFraction(0.33)), 7.7), 1e-8);
    assertEquals(7.7, map.getOrDefault(testHasUniqueId(uniqueId("c"), unitFraction(0.22)), 7.7), 1e-8);
    assertEquals(7.7, map.getOrDefault(testHasUniqueId(uniqueId("c"), unitFraction(0.33)), 7.7), 1e-8);
  }

  @Test
  public void test_keySet_keyStream_values() {
    TestHasUniqueId testHasUniqueId1 = testHasUniqueId(uniqueId("1"), unitFraction(0.11));
    TestHasUniqueId testHasUniqueId2 = testHasUniqueId(uniqueId("2"), unitFraction(0.22));
    TestHasUniqueId testHasUniqueId3 = testHasUniqueId(uniqueId("3"), unitFraction(0.33));
    TestHasUniqueId testHasUniqueId4 = testHasUniqueId(uniqueId("4"), unitFraction(0.44));
    TestHasUniqueId testHasUniqueId5 = testHasUniqueId(uniqueId("5"), unitFraction(0.55));
    // Intentionally constructing this in a different order, to show that this does not affect the result.
    RBMapOfHasUniqueId<TestHasUniqueId, Double> map = rbMapOfHasUniqueIdOf(
        testHasUniqueId1, 1.1,
        testHasUniqueId4, 4.4,
        testHasUniqueId3, 3.3,
        testHasUniqueId2, 2.2,
        testHasUniqueId5, 5.5);

    assertThat(
        map.keySet(),
        rbSetMatcher(
            rbSetOf(testHasUniqueId1, testHasUniqueId2, testHasUniqueId3, testHasUniqueId4, testHasUniqueId5),
            f -> testHasUniqueIdMatcher(f),
            comparing(v -> v.getUniqueId())));
    // This somehow also works with an orderedListMatcher, but there's no guarantee I think,
    // so in the test I'll just use unorderedListMatcher.
    assertThat(
        map.keyStream().collect(Collectors.toList()),
        unorderedListMatcher(
            ImmutableList.of(testHasUniqueId1, testHasUniqueId2, testHasUniqueId3, testHasUniqueId4, testHasUniqueId5),
            f -> testHasUniqueIdMatcher(f),
            comparing(v -> v.getUniqueId())));
    assertThat(
        map.sortedKeyStream().collect(Collectors.toList()),
        orderedListMatcher(
            ImmutableList.of(testHasUniqueId1, testHasUniqueId2, testHasUniqueId3, testHasUniqueId4, testHasUniqueId5),
            f -> testHasUniqueIdMatcher(f)));
    assertThat(
        map.values(),
        rbSetMatcher(
            rbSetOf(1.1, 2.2, 3.3, 4.4, 5.5),
            f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8),
            Double::compare));
  }

  @Test
  public void testTransformValuesCopy() {
    TestHasUniqueId testHasUniqueId1 = testHasUniqueId(uniqueId("a"), unitFraction(0.11));
    TestHasUniqueId testHasUniqueId2 = testHasUniqueId(uniqueId("b"), unitFraction(0.22));
    RBMapOfHasUniqueId<TestHasUniqueId, Double> map = rbMapOfHasUniqueIdOf(
        testHasUniqueId1, 1.1,
        testHasUniqueId2, 2.2);
    assertThat(
        map.transformValuesCopy(d -> money(700 + d)),
        rbMapOfHasUniqueIdMatcher(
            rbMapOfHasUniqueIdOf(
                testHasUniqueId1, money(701.1),
                testHasUniqueId2, money(702.2)),
            f -> testHasUniqueIdMatcher(f),
            f -> preciseValueMatcher(f, DEFAULT_EPSILON_1e_8)));

    RBMapOfHasUniqueId<TestHasUniqueId, Double> emptyMap = emptyRBMapOfHasUniqueId();
    assertEquals(0, emptyMap.transformValuesCopy(d -> money(700 + d)).size());
  }

  @Override
  public RBMapOfHasUniqueId<TestHasUniqueId, Double> makeTrivialObject() {
    return emptyRBMapOfHasUniqueId();
  }

  @Override
  public RBMapOfHasUniqueId<TestHasUniqueId, Double> makeNontrivialObject() {
    return rbMapOfHasUniqueIdOf(
        testHasUniqueId(uniqueId("a"), unitFraction(0.11)), 1.1,
        testHasUniqueId(uniqueId("b"), unitFraction(0.22)), 2.2);
  }

  @Override
  public RBMapOfHasUniqueId<TestHasUniqueId, Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbMapOfHasUniqueIdOf(
        testHasUniqueId(uniqueId("a"), unitFraction(0.11)), 1.1 + e,
        testHasUniqueId(uniqueId("b"), unitFraction(0.22)), 2.2 + e);
  }

  @Override
  protected boolean willMatch(RBMapOfHasUniqueId<TestHasUniqueId, Double> expected,
                              RBMapOfHasUniqueId<TestHasUniqueId, Double> actual) {
    return rbMapOfHasUniqueIdMatcher(
        expected,
        k -> testHasUniqueIdMatcher(k),
        v -> doubleAlmostEqualsMatcher(v, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <K extends HasUniqueId<K>, V> TypeSafeMatcher<RBMapOfHasUniqueId<K, V>> rbMapOfHasUniqueIdMatcher(
      RBMapOfHasUniqueId<K, V> expected,
      MatcherGenerator<K> keyMatcherGenerator,
      MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected,
        matchRBMap(v -> v.getRawMap(), f -> pairMatcher(f, keyMatcherGenerator, valueMatcherGenerator)));
  }

}
