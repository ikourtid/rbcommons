package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static com.rb.nonbiz.collections.ContiguousDiscreteRangeMap.contiguousDiscreteRangeMap;
import static com.rb.nonbiz.collections.ContiguousDiscreteRangeMap.singletonContiguousDiscreteRangeMap;
import static com.rb.nonbiz.collections.NonContiguousNonDiscreteRangeMapTest.nonContiguousRangeMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_DOUBLE;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

/**
 * This test class is not generic, but the publicly exposed test matcher is.
 */
public class ContiguousDiscreteRangeMapTest extends RBTestMatcher<ContiguousDiscreteRangeMap<LocalDate, Double>> {

  private final LocalDate dayA = LocalDate.of(1974, 4, 4);
  private final LocalDate dayB = LocalDate.of(1975, 5, 5);
  private final LocalDate dayC = LocalDate.of(1976, 6, 6);

  @Test
  public void emptyMap_throws() {
    // The exception is thrown from 'deeper inside', namely the NonContiguousRangeMap constructor,
    // but let's test this here as well.
    assertIllegalArgumentException( () ->
        ContiguousDiscreteRangeMap.<LocalDate, Double>
            contiguousDiscreteRangeMap(emptyList(), emptyList(), date -> date.plusDays(1)));
  }

  @Test
  public void mustBeContiguous_otherwiseThrows() {
    List<Double> twoDoubles = ImmutableList.of(DUMMY_DOUBLE, DUMMY_DOUBLE);
    Function<Range<LocalDate>, ContiguousDiscreteRangeMap<LocalDate, Double>> constructor = secondRange ->
        contiguousDiscreteRangeMap(
        ImmutableList.of(
            Range.closed(dayA, dayB.minusDays(1)),
            secondRange),
        twoDoubles,
        date -> date.plusDays(1));
    // has overlap on dayB
    assertIllegalArgumentException( () -> constructor.apply(Range.closed(dayB.minusDays(1), dayB.minusDays(1))));
    assertIllegalArgumentException( () -> constructor.apply(Range.closed(dayB.minusDays(1), dayB)));
    assertIllegalArgumentException( () -> constructor.apply(Range.closed(dayB.minusDays(1), dayB.plusDays(1))));
    assertIllegalArgumentException( () -> constructor.apply(Range.atLeast(dayB.minusDays(1))));
    ContiguousDiscreteRangeMap<LocalDate, Double> doesNotThrow;
    doesNotThrow = constructor.apply(Range.closed(dayB, dayB));
    doesNotThrow = constructor.apply(Range.closed(dayB, dayC));
    // has hole on day 1
    assertIllegalArgumentException( () -> constructor.apply(Range.closed(dayB.plusDays(1), dayB.plusDays(1))));
    assertIllegalArgumentException( () -> constructor.apply(Range.closed(dayB.plusDays(1), dayC)));
    assertIllegalArgumentException( () -> constructor.apply(Range.atLeast(dayB.plusDays(1))));
  }

  @Test
  public void unboundedOnRightSide_works() {
    ContiguousDiscreteRangeMap<Integer, String> map = contiguousDiscreteRangeMap(
        ImmutableList.of(Range.closed(0, 1), Range.singleton(2), Range.atLeast(3)),
        ImmutableList.of("0-1", "2 only", "3+"),
        i -> i + 1);
    assertIllegalArgumentException( () -> map.getOrThrow(-1));
    assertEquals("0-1", map.getOrThrow(0));
    assertEquals("0-1", map.getOrThrow(1));
    assertEquals("2 only", map.getOrThrow(2));
    assertEquals("3+", map.getOrThrow(3));
    assertEquals("3+", map.getOrThrow(4));
    assertEquals("3+", map.getOrThrow(999));
  }

  @Override
  public ContiguousDiscreteRangeMap<LocalDate, Double> makeTrivialObject() {
    return singletonContiguousDiscreteRangeMap(Range.closed(dayA, dayA), 0.5);
  }

  @Override
  public ContiguousDiscreteRangeMap<LocalDate, Double> makeNontrivialObject() {
    return contiguousDiscreteRangeMap(
        ImmutableList.of(
            Range.closed(dayA, dayB.minusDays(1)),
            Range.closed(dayB, dayC.minusDays(1)),
            Range.atLeast(dayC)),
        ImmutableList.of(0.5, 1.5, 2.5),
        date -> date.plusDays(1));
  }

  @Override
  public ContiguousDiscreteRangeMap<LocalDate, Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return contiguousDiscreteRangeMap(
        ImmutableList.of(
            Range.closed(dayA, dayB.minusDays(1)),
            Range.closed(dayB, dayC.minusDays(1)),
            Range.atLeast(dayC)),
        ImmutableList.of(0.5 + e, 1.5 + e, 2.5 + e),
        date -> date.plusDays(1));
  }

  @Override
  protected boolean willMatch(ContiguousDiscreteRangeMap<LocalDate, Double> expected,
                              ContiguousDiscreteRangeMap<LocalDate, Double> actual) {
    return contiguousDiscreteRangeMapMatcher(expected, v -> doubleAlmostEqualsMatcher(v, 1e-8)).matches(actual);
  }

  public static <K extends Comparable<? super K>, V>
  TypeSafeMatcher<ContiguousDiscreteRangeMap<K, V>> contiguousDiscreteRangeMapMatcher(
      ContiguousDiscreteRangeMap<K, V> expected, MatcherGenerator<V> valuesMatcherGenerator) {
    return makeMatcher(expected, actual ->
        nonContiguousRangeMapMatcher(expected.getUnderlyingMap(), valuesMatcherGenerator)
            .matches(actual.getUnderlyingMap()));
  }

  public static <K extends Comparable<? super K>, V>
  TypeSafeMatcher<ContiguousDiscreteRangeMap<K, V>> contiguousDiscreteRangeMapEqualityMatcher(
      ContiguousDiscreteRangeMap<K, V> expected) {
    return contiguousDiscreteRangeMapMatcher(expected, v -> typeSafeEqualTo(v));
  }

}
