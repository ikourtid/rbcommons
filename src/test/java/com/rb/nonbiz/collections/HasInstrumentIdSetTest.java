package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.HasInstrumentIdSets.emptyHasInstrumentIdSet;
import static com.rb.nonbiz.collections.HasInstrumentIdSets.hasInstrumentIdSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentId;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentIdMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.hasLongMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIidSetEquals;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This test class is not generic, but the publicly visible static matcher is.
 */
public class HasInstrumentIdSetTest extends RBTestMatcher<HasInstrumentIdSet<TestHasInstrumentId>> {

  private final HasInstrumentIdSet<TestHasInstrumentId> TEST_SET = hasInstrumentIdSetOf(
      testHasInstrumentId(instrumentId(1), 1.1),
      testHasInstrumentId(instrumentId(6), 6.6),
      testHasInstrumentId(instrumentId(2), 2.2),
      testHasInstrumentId(instrumentId(5), 5.5),
      testHasInstrumentId(instrumentId(4), 4.4),
      testHasInstrumentId(instrumentId(3), 3.3));

  @Test
  public void testInstrumentIdKeysIterator() {
    assertThat(
        newRBSet(TEST_SET.instrumentIdKeysIterator()),
        rbSetEqualsMatcher(rbSetOf(
            instrumentId(1), instrumentId(2), instrumentId(3),
            instrumentId(4), instrumentId(5), instrumentId(6))));
  }

  @Test
  public void testGetIidSet() {
    assertIidSetEquals(
        emptyIidSet(),
        emptyHasInstrumentIdSet().toIidSet());
    assertIidSetEquals(
        iidSetOf(
            instrumentId(1), instrumentId(2), instrumentId(3),
            instrumentId(4), instrumentId(5), instrumentId(6)),
        TEST_SET.toIidSet());
  }

  @Test
  public void testSortedStream() {
    assertThat(
        TEST_SET.sortedStream().iterator(),
        iteratorMatcher(
            ImmutableList.of(
                testHasInstrumentId(instrumentId(1), 1.1),
                testHasInstrumentId(instrumentId(2), 2.2),
                testHasInstrumentId(instrumentId(3), 3.3),
                testHasInstrumentId(instrumentId(4), 4.4),
                testHasInstrumentId(instrumentId(5), 5.5),
                testHasInstrumentId(instrumentId(6), 6.6))
            .iterator(),
            f -> testHasInstrumentIdMatcher(f)));
  }

  @Test
  public void testSortedInstrumentIdStream() {
    assertEquals(
        TEST_SET.sortedInstrumentIdStream().collect(Collectors.toList()),
        ImmutableList.of(
            instrumentId(1), instrumentId(2), instrumentId(3),
            instrumentId(4), instrumentId(5), instrumentId(6)));
  }

  @Override
  public HasInstrumentIdSet<TestHasInstrumentId> makeTrivialObject() {
    return emptyHasInstrumentIdSet();
  }

  @Override
  public HasInstrumentIdSet<TestHasInstrumentId> makeNontrivialObject() {
    return hasInstrumentIdSetOf(
        testHasInstrumentId(instrumentId(1), 1.1),
        testHasInstrumentId(instrumentId(2), 2.2),
        testHasInstrumentId(instrumentId(3), 3.3));
  }

  @Override
  public HasInstrumentIdSet<TestHasInstrumentId> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return hasInstrumentIdSetOf(
        testHasInstrumentId(instrumentId(1), 1.1 + e),
        testHasInstrumentId(instrumentId(2), 2.2 + e),
        testHasInstrumentId(instrumentId(3), 3.3 + e));
  }

  @Override
  protected boolean willMatch(HasInstrumentIdSet<TestHasInstrumentId> expected,
                              HasInstrumentIdSet<TestHasInstrumentId> actual) {
    return hasInstrumentIdSetMatcher(expected, f -> testHasInstrumentIdMatcher(f)).matches(actual);
  }

  public static <T extends HasInstrumentId> TypeSafeMatcher<HasInstrumentIdSet<T>> hasInstrumentIdSetMatcher(
      HasInstrumentIdSet<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected, actual ->
        hasLongMapMatcher(expected, matcherGenerator).matches(actual));
  }

}
