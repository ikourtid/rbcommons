package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;

import static com.rb.nonbiz.collections.NonContiguousRangeMap.nonContiguousRangeMap;
import static com.rb.nonbiz.collections.NonContiguousRangeMap.nonContiguousRangeMapWithEnd;
import static com.rb.nonbiz.collections.NonContiguousRangeMap.nonContiguousRangeMapWithNoEnd;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rangeMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class NonContiguousRangeMapTest extends RBTestMatcher<NonContiguousRangeMap<LocalDate, String>> {

  @Test
  public void cannotBeEmpty() {
    assertIllegalArgumentException( () -> nonContiguousRangeMap(emptyList(), emptyList()));
  }

  @Test
  public void testGetOptionalWithHighestKeyBelow_singletonRange() {
    NonContiguousRangeMap<LocalDate, String> rangeMap = nonContiguousRangeMap(
        singletonList(Range.singleton(LocalDate.of(1974, 4, 4))),
        singletonList("abc"));
    assertOptionalEmpty(rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1974, 4, 3)));
    assertOptionalEmpty(rangeMap.getOptional(LocalDate.of(1974, 4, 3)));

    assertOptionalEquals("abc", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1974, 4, 4)));
    assertOptionalEquals("abc", rangeMap.getOptional(LocalDate.of(1974, 4, 4)));

    assertOptionalEquals("abc", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1974, 4, 5)));
    assertOptionalEmpty(rangeMap.getOptional(LocalDate.of(1974, 4, 5)));
  }

  @Test
  public void testGetOptionalWithHighestKeyBelow_rangeWithGap() {
    NonContiguousRangeMap<LocalDate, String> rangeMap = nonContiguousRangeMapWithEnd(
        ImmutableList.of(
            Range.closed(LocalDate.of(1974, 4, 4), LocalDate.of(1975, 5, 5)),
            Range.closed(LocalDate.of(1977, 7, 7), LocalDate.of(1978, 8, 8))),
        ImmutableList.of(
            "1974-75",
            "1977-78"));

    assertOptionalEmpty(rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1974, 4, 3)));
    assertOptionalEquals("1974-75", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1974, 4, 4)));
    assertOptionalEquals("1974-75", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1974, 4, 5)));
    assertOptionalEquals("1974-75", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1975, 5, 4)));
    assertOptionalEquals("1974-75", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1975, 5, 5)));
    assertOptionalEquals("1974-75", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1975, 5, 6)));
    assertOptionalEquals("1974-75", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1976, 6, 6)));
    assertOptionalEquals("1974-75", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1977, 7, 6)));
    assertOptionalEquals("1977-78", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1977, 7, 7)));
    assertOptionalEquals("1977-78", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1977, 7, 8)));
    assertOptionalEquals("1977-78", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1978, 8, 7)));
    assertOptionalEquals("1977-78", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1978, 8, 8)));
    assertOptionalEquals("1977-78", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(1978, 8, 9)));
    assertOptionalEquals("1977-78", rangeMap.getOptionalWithHighestKeyBelow(LocalDate.of(2011, 11, 11)));

    assertOptionalEmpty(rangeMap.getOptional(LocalDate.of(1974, 4, 3)));
    assertOptionalEquals("1974-75", rangeMap.getOptional(LocalDate.of(1974, 4, 4)));
    assertOptionalEquals("1974-75", rangeMap.getOptional(LocalDate.of(1974, 4, 5)));
    assertOptionalEquals("1974-75", rangeMap.getOptional(LocalDate.of(1975, 5, 4)));
    assertOptionalEquals("1974-75", rangeMap.getOptional(LocalDate.of(1975, 5, 5)));
    assertOptionalEmpty(rangeMap.getOptional(LocalDate.of(1975, 5, 6)));
    assertOptionalEmpty(rangeMap.getOptional(LocalDate.of(1976, 6, 6)));
    assertOptionalEmpty(rangeMap.getOptional(LocalDate.of(1977, 7, 6)));
    assertOptionalEquals("1977-78", rangeMap.getOptional(LocalDate.of(1977, 7, 7)));
    assertOptionalEquals("1977-78", rangeMap.getOptional(LocalDate.of(1977, 7, 8)));
    assertOptionalEquals("1977-78", rangeMap.getOptional(LocalDate.of(1978, 8, 7)));
    assertOptionalEquals("1977-78", rangeMap.getOptional(LocalDate.of(1978, 8, 8)));
    assertOptionalEmpty(rangeMap.getOptional(LocalDate.of(1978, 8, 9)));
    assertOptionalEmpty(rangeMap.getOptional(LocalDate.of(2011, 11, 11)));
  }

  @Override
  public NonContiguousRangeMap<LocalDate, String> makeTrivialObject() {
    return nonContiguousRangeMap(
        singletonList(Range.singleton(LocalDate.of(1974, 4, 4))),
        singletonList("abc"));
  }

  @Override
  public NonContiguousRangeMap<LocalDate, String> makeNontrivialObject() {
    return nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(
            Range.closed(LocalDate.of(1974, 4, 4), LocalDate.of(1975, 5, 5)),
            Range.closed(LocalDate.of(1976, 6, 6), LocalDate.of(1977, 7, 7)),
            Range.atLeast(LocalDate.of(1978, 8, 8))),
        ImmutableList.of("1974-75", "1976-77", "1978+"));
  }

  @Override
  public NonContiguousRangeMap<LocalDate, String> makeMatchingNontrivialObject() {
    return nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(
            Range.closed(LocalDate.of(1974, 4, 4), LocalDate.of(1975, 5, 5)),
            Range.closed(LocalDate.of(1976, 6, 6), LocalDate.of(1977, 7, 7)),
            Range.atLeast(LocalDate.of(1978, 8, 8))),
        ImmutableList.of("1974-75", "1976-77", "1978+"));
  }

  @Override
  protected boolean willMatch(NonContiguousRangeMap<LocalDate, String> expected,
                              NonContiguousRangeMap<LocalDate, String> actual) {
    return nonContiguousRangeMapEqualityMatcher(expected).matches(actual);
  }

  public static <K extends Comparable<? super K>, V> TypeSafeMatcher<NonContiguousRangeMap<K, V>>
  nonContiguousRangeMapEqualityMatcher(NonContiguousRangeMap<K, V> expected) {
    return nonContiguousRangeMapMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <K extends Comparable<? super K>, V> TypeSafeMatcher<NonContiguousRangeMap<K, V>>
  nonContiguousRangeMapMatcher(NonContiguousRangeMap<K, V> expected,
                               MatcherGenerator<V> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getRawRangeMap(), f -> rangeMapMatcher(f, matcherGenerator)));
  }

}
