package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.rb.nonbiz.collections.NonContiguousRangeMap.nonContiguousRangeMap;
import static com.rb.nonbiz.collections.NonContiguousRangeMap.nonContiguousRangeMapWithEnd;
import static com.rb.nonbiz.collections.NonContiguousRangeMap.nonContiguousRangeMapWithNoEnd;
import static com.rb.nonbiz.collections.NonContiguousRangeMap.singletonNonContiguousRangeMapWithEnd;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rangeMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_DOUBLE;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * This concrete test class is not generic, but the publicly exposed static matcher is.
 */
public class NonContiguousNonDiscreteRangeMapTest extends RBTestMatcher<NonContiguousRangeMap<LocalDate, Double>> {

  LocalDate dayA = LocalDate.of(1974, 4, 4);
  LocalDate dayB = LocalDate.of(1975, 5, 5);
  LocalDate dayC = LocalDate.of(1976, 6, 6);
  LocalDate dayD = LocalDate.of(1977, 7, 7);
  LocalDate dayE = LocalDate.of(1978, 8, 8);

  Range<LocalDate> rangeA_A = Range.closed(dayA, dayA);
  Range<LocalDate> rangeA_B = Range.closed(dayA, dayB);
  Range<LocalDate> rangeC_D = Range.closed(dayC, dayD);
  Range<LocalDate> rangeE_inf = Range.atLeast(dayE);

  @Test
  public void hasNoRangesOrItems() {
    assertIllegalArgumentException( () ->
        nonContiguousRangeMapWithEnd(emptyList(), emptyList()));
    assertIllegalArgumentException( () ->
        nonContiguousRangeMapWithNoEnd(emptyList(), emptyList()));
  }

  @Test
  public void numItemsDoesNotMatchNumRanges_throws() {
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithEnd(
        ImmutableList.of(rangeA_B, rangeC_D), nDoubles(1)));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithEnd(
        ImmutableList.of(rangeA_B, rangeC_D), nDoubles(3)));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeA_B, rangeE_inf), nDoubles(1)));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeA_B, rangeE_inf), nDoubles(3)));
  }

  @Test
  public void singlePointRangesAreAllowed() {
    NonContiguousRangeMap<LocalDate, Double> doesNotThrow = nonContiguousRangeMapWithEnd(
        singletonList(rangeA_A),
        singletonList(DUMMY_DOUBLE));
  }

  @Test
  public void simplestRangeOfTypeAtLeastIsAllowed() {
    NonContiguousRangeMap<LocalDate, Double> doesNotThrow = nonContiguousRangeMapWithNoEnd(
        singletonList(rangeE_inf),
        singletonList(DUMMY_DOUBLE));
  }

  @Test
  public void rangeWithEnd_comparableKeysMustBeIncreasing_otherwiseThrows() {
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithEnd(
        ImmutableList.of(rangeA_B, rangeA_B), nDoubles(2)));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithEnd(
        ImmutableList.of(rangeC_D, rangeA_B), nDoubles(2)));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithEnd(
        ImmutableList.of(rangeA_A, rangeA_B), nDoubles(2)));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithEnd(
        ImmutableList.of(rangeA_A, rangeA_A), nDoubles(2)));
    NonContiguousRangeMap<LocalDate, Double> doesNotThrow = nonContiguousRangeMapWithEnd(
        ImmutableList.of(rangeA_B, rangeC_D), nDoubles(2));
  }

  @Test
  public void rangeWithNoEnd_comparableKeysMustBeIncreasing_otherwiseThrows() {
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeA_B, rangeA_B, rangeE_inf), nDoubles(3)));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeC_D, rangeA_B, rangeE_inf), nDoubles(3)));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeA_A, rangeA_B, rangeE_inf), nDoubles(3)));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeA_A, rangeA_A, rangeE_inf), nDoubles(3)));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeA_B, rangeC_D, Range.atLeast(dayD)), nDoubles(3)));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeA_B, rangeC_D, Range.atLeast(dayC)), nDoubles(3)));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(Range.closed(dayA, dayE), rangeE_inf), nDoubles(2)));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(Range.closed(dayA, dayE.plusDays(1)), rangeE_inf), nDoubles(2)));
    NonContiguousRangeMap<LocalDate, Double> doesNotThrow = nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeA_B, rangeC_D, rangeE_inf), nDoubles(3));
  }

  @Test
  public void firstAndOnlyRangeCanBeEitherClosedOrAtLeast() {
    for (Range<LocalDate> invalidRange : rbSetOf(
        Range.closedOpen(dayC, dayD),
        Range.openClosed(dayC, dayD),
        Range.open(dayC, dayD),
        Range.greaterThan(dayC),
        Range.lessThan(dayD),
        Range.atMost(dayD),
        Range.<LocalDate>all())) {
      assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
          singletonList(invalidRange), nDoubles(1)));
      assertIllegalArgumentException( () -> nonContiguousRangeMapWithEnd(
          singletonList(invalidRange), nDoubles(1)));
      assertIllegalArgumentException( () -> nonContiguousRangeMap(
          singletonList(invalidRange), nDoubles(1)));
    }

    NonContiguousRangeMap<LocalDate, Double> doesNotThrow;
    doesNotThrow = nonContiguousRangeMap(
        singletonList(Range.closed(dayC, dayD)), nDoubles(1));
    doesNotThrow = nonContiguousRangeMapWithEnd(
        singletonList(Range.closed(dayC, dayD)), nDoubles(1));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
        singletonList(Range.closed(dayC, dayD)), nDoubles(1)));

    doesNotThrow = nonContiguousRangeMap(
        singletonList(Range.atLeast(dayC)), nDoubles(1));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithEnd(
        singletonList(Range.atLeast(dayC)), nDoubles(1)));
    doesNotThrow = nonContiguousRangeMapWithNoEnd(
        singletonList(Range.atLeast(dayC)), nDoubles(1));
  }

  @Test
  public void firstRangeIsClosed_secondRangeCanBeEitherClosedOrAtLeast() {
    for (Range<LocalDate> invalidRange : rbSetOf(
        Range.closedOpen(dayC, dayD),
        Range.openClosed(dayC, dayD),
        Range.open(dayC, dayD),
        Range.greaterThan(dayC),
        Range.lessThan(dayD),
        Range.atMost(dayD),
        Range.<LocalDate>all())) {
      assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
          ImmutableList.of(rangeA_B, invalidRange), nDoubles(2)));
      assertIllegalArgumentException( () -> nonContiguousRangeMapWithEnd(
          ImmutableList.of(rangeA_B, invalidRange), nDoubles(2)));
      assertIllegalArgumentException( () -> nonContiguousRangeMap(
          ImmutableList.of(rangeA_B, invalidRange), nDoubles(2)));
    }

    NonContiguousRangeMap<LocalDate, Double> doesNotThrow;
    doesNotThrow = nonContiguousRangeMap(
        ImmutableList.of(rangeA_B, Range.closed(dayC, dayD)), nDoubles(2));
    doesNotThrow = nonContiguousRangeMapWithEnd(
        ImmutableList.of(rangeA_B, Range.closed(dayC, dayD)), nDoubles(2));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeA_B, Range.closed(dayC, dayD)), nDoubles(2)));

    doesNotThrow = nonContiguousRangeMap(
        ImmutableList.of(rangeA_B, Range.atLeast(dayC)), nDoubles(2));
    assertIllegalArgumentException( () -> nonContiguousRangeMapWithEnd(
        ImmutableList.of(rangeA_B, Range.atLeast(dayC)), nDoubles(2)));
    doesNotThrow = nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeA_B, Range.atLeast(dayC)), nDoubles(2));
  }

  @Test
  public void happyPath_keysAreDates_nonContiguousRangeMapWithEnd_returnsValuesInRange() {
    NonContiguousRangeMap<LocalDate, String> map = nonContiguousRangeMapWithEnd(
        ImmutableList.of(rangeA_B, rangeC_D),
        ImmutableList.of("a", "b"));
    assertOptionalEmpty(map.getOptional(dayA.minusDays(1)));
    assertOptionalEquals("a", map.getOptional(dayA));
    assertOptionalEquals("a", map.getOptional(dayA.plusDays(1)));
    assertOptionalEquals("a", map.getOptional(dayB.minusDays(1)));
    assertOptionalEquals("a", map.getOptional(dayB));
    assertOptionalEmpty(map.getOptional(dayB.plusDays(1))); // first day of gap
    assertOptionalEmpty(map.getOptional(dayC.minusDays(1))); // last day of gap
    assertOptionalEquals("b", map.getOptional(dayC));
    assertOptionalEquals("b", map.getOptional(dayC.plusDays(1)));
    assertOptionalEquals("b", map.getOptional(dayD.minusDays(1)));
    assertOptionalEquals("b", map.getOptional(dayD));
    assertOptionalEmpty(map.getOptional(dayD.plusDays(1)));

    assertIllegalArgumentException( () -> map.getOrThrow(dayA.minusDays(1)));
    assertEquals("a", map.getOrThrow(dayA));
    assertEquals("a", map.getOrThrow(dayA.plusDays(1)));
    assertEquals("a", map.getOrThrow(dayB.minusDays(1)));
    assertEquals("a", map.getOrThrow(dayB));
    assertIllegalArgumentException( () -> map.getOrThrow(dayB.plusDays(1))); // first day of gap
    assertIllegalArgumentException( () -> map.getOrThrow(dayC.minusDays(1))); // last day of gap
    assertEquals("b", map.getOrThrow(dayC));
    assertEquals("b", map.getOrThrow(dayC.plusDays(1)));
    assertEquals("b", map.getOrThrow(dayD.minusDays(1)));
    assertEquals("b", map.getOrThrow(dayD));
    assertIllegalArgumentException( () -> map.getOrThrow(dayD.plusDays(1)));
  }

  @Test
  public void happyPath_keysAreDoubles_nonContiguousRangeMapWithEnd_returnsValuesInRange() {
    NonContiguousRangeMap<Double, String> map = nonContiguousRangeMapWithEnd(
        ImmutableList.of(Range.closed(1.5, 2.5), Range.closed(4.5, 5.5)),
        ImmutableList.of("[1.5, 2.5]", "[4.5, 5.5]"));
    assertOptionalEmpty(map.getOptional(-999.0));
    assertOptionalEmpty(map.getOptional(1.4999));
    assertEquals("[1.5, 2.5]", map.getOrThrow(1.5));
    assertEquals("[1.5, 2.5]", map.getOrThrow(1.5001));
    assertEquals("[1.5, 2.5]", map.getOrThrow(2.4999));
    assertEquals("[1.5, 2.5]", map.getOrThrow(2.5));
    assertOptionalEmpty(map.getOptional(2.5001));
    assertOptionalEmpty(map.getOptional(4.4999));
    assertEquals("[4.5, 5.5]", map.getOrThrow(4.5));
    assertEquals("[4.5, 5.5]", map.getOrThrow(4.5001));
    assertEquals("[4.5, 5.5]", map.getOrThrow(5.4999));
    assertEquals("[4.5, 5.5]", map.getOrThrow(5.5));
    assertOptionalEmpty(map.getOptional(5.5001));
  }

  @Test
  public void happyPath_keysAreDates_nonContiguousRangeMapWithNoEnd_returnsValuesInRange() {
    NonContiguousRangeMap<LocalDate, String> map = nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeA_B, rangeC_D, rangeE_inf),
        ImmutableList.of("a", "b", "c"));
    assertOptionalEmpty(map.getOptional(dayA.minusDays(1)));
    assertOptionalEquals("a", map.getOptional(dayA));
    assertOptionalEquals("a", map.getOptional(dayA.plusDays(1)));
    assertOptionalEquals("a", map.getOptional(dayB.minusDays(1)));
    assertOptionalEquals("a", map.getOptional(dayB));
    assertOptionalEmpty(map.getOptional(dayB.plusDays(1))); // first day of gap
    assertOptionalEmpty(map.getOptional(dayC.minusDays(1))); // last day of gap
    assertOptionalEquals("b", map.getOptional(dayC));
    assertOptionalEquals("b", map.getOptional(dayC.plusDays(1)));
    assertOptionalEquals("b", map.getOptional(dayD.minusDays(1)));
    assertOptionalEquals("b", map.getOptional(dayD));
    assertOptionalEmpty(map.getOptional(dayD.plusDays(1)));
    assertOptionalEmpty(map.getOptional(dayE.minusDays(1)));
    assertOptionalEquals("c", map.getOptional(dayE));
    assertOptionalEquals("c", map.getOptional(dayE.plusDays(1)));
    assertOptionalEquals("c", map.getOptional(dayE.plusDays(10_000)));

    assertIllegalArgumentException( () -> map.getOrThrow(dayA.minusDays(1)));
    assertEquals("a", map.getOrThrow(dayA));
    assertEquals("a", map.getOrThrow(dayA.plusDays(1)));
    assertEquals("a", map.getOrThrow(dayB.minusDays(1)));
    assertEquals("a", map.getOrThrow(dayB));
    assertIllegalArgumentException( () -> map.getOrThrow(dayB.plusDays(1))); // first day of gap
    assertIllegalArgumentException( () -> map.getOrThrow(dayC.minusDays(1))); // last day of gap
    assertEquals("b", map.getOrThrow(dayC));
    assertEquals("b", map.getOrThrow(dayC.plusDays(1)));
    assertEquals("b", map.getOrThrow(dayD.minusDays(1)));
    assertEquals("b", map.getOrThrow(dayD));
    assertIllegalArgumentException( () -> map.getOrThrow(dayD.plusDays(1)));
    assertIllegalArgumentException( () -> map.getOrThrow(dayE.minusDays(1)));
    assertEquals("c", map.getOrThrow(dayE));
    assertEquals("c", map.getOrThrow(dayE.plusDays(1)));
    assertEquals("c", map.getOrThrow(dayE.plusDays(10_000)));
  }

  @Test
  public void happyPath_keysAreDoubles_nonContiguousRangeMapWithNoEnd_returnsValuesInRange() {
    NonContiguousRangeMap<Double, String> map = nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(Range.closed(1.5, 2.5), Range.closed(4.5, 5.5), Range.atLeast(7.5)),
        ImmutableList.of("[1.5, 2.5]", "[4.5, 5.5]", "[7.5, +inf)"));
    assertOptionalEmpty(map.getOptional(-999.0));
    assertOptionalEmpty(map.getOptional(1.4999));
    assertEquals("[1.5, 2.5]", map.getOrThrow(1.5));
    assertEquals("[1.5, 2.5]", map.getOrThrow(1.5001));
    assertEquals("[1.5, 2.5]", map.getOrThrow(2.4999));
    assertEquals("[1.5, 2.5]", map.getOrThrow(2.5));
    assertOptionalEmpty(map.getOptional(2.5001));
    assertOptionalEmpty(map.getOptional(4.4999));
    assertEquals("[4.5, 5.5]", map.getOrThrow(4.5));
    assertEquals("[4.5, 5.5]", map.getOrThrow(4.5001));
    assertEquals("[4.5, 5.5]", map.getOrThrow(5.4999));
    assertEquals("[4.5, 5.5]", map.getOrThrow(5.5));
    assertOptionalEmpty(map.getOptional(5.5001));
    assertOptionalEmpty(map.getOptional(7.4999));
    assertEquals("[7.5, +inf)", map.getOrThrow(7.5));
    assertEquals("[7.5, +inf)", map.getOrThrow(7.5001));
    assertEquals("[7.5, +inf)", map.getOrThrow(10_000.0));
  }

  private List<Double> nDoubles(int n) {
    return Collections.nCopies(n, DUMMY_DOUBLE);
  }

  @Override
  public NonContiguousRangeMap<LocalDate, Double> makeTrivialObject() {
    return singletonNonContiguousRangeMapWithEnd(rangeA_A, 0.0);
  }

  @Override
  public NonContiguousRangeMap<LocalDate, Double> makeNontrivialObject() {
    return nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeA_B, rangeC_D, rangeE_inf),
        ImmutableList.of(4.4, 5.5, 6.6));
  }

  @Override
  public NonContiguousRangeMap<LocalDate, Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return nonContiguousRangeMapWithNoEnd(
        ImmutableList.of(rangeA_B, rangeC_D, rangeE_inf),
        ImmutableList.of(4.4 + e, 5.5 + e, 6.6 + e));
  }

  @Override
  protected boolean willMatch(NonContiguousRangeMap<LocalDate, Double> expected, NonContiguousRangeMap<LocalDate, Double> actual) {
    return nonContiguousRangeMapMatcher(expected, v -> doubleAlmostEqualsMatcher(v, DEFAULT_EPSILON_1e_8)).matches(actual);
  }

  public static <K extends Comparable<? super K>, V> TypeSafeMatcher<NonContiguousRangeMap<K, V>>
  nonContiguousRangeMapMatcher(NonContiguousRangeMap<K, V> expected, MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected, actual ->
        rangeMapMatcher(expected.getRawRangeMap(), valueMatcherGenerator).matches(actual.getRawRangeMap()));
  }

  public static <K extends Comparable<? super K>, V> TypeSafeMatcher<NonContiguousRangeMap<K, V>>
  nonContiguousRangeMapEqualityMatcher(NonContiguousRangeMap<K, V> expected) {
    return nonContiguousRangeMapMatcher(expected, v -> typeSafeEqualTo(v));
  }

}
