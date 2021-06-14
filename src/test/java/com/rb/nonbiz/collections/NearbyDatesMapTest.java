package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;
import java.util.function.IntFunction;

import static com.rb.nonbiz.collections.NearbyDatesMap.emptyNearbyDatesMap;
import static com.rb.nonbiz.collections.NearbyDatesMap.nearbyDatesMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DATE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;

public class NearbyDatesMapTest extends RBTestMatcher<NearbyDatesMap<Double>> {

  @Test
  public void trivialDateRange_throws() {
    IntFunction<NearbyDatesMap<Double>> maker = maxCalendarRangeAllowed -> nearbyDatesMap(
        singletonRBMap(DUMMY_DATE, DUMMY_DOUBLE),
        maxCalendarRangeAllowed);
    assertIllegalArgumentException( () -> maker.apply(-999));
    assertIllegalArgumentException( () -> maker.apply(-1));
    assertIllegalArgumentException( () -> maker.apply(0));
    NearbyDatesMap<Double> doesNotThrow;
    doesNotThrow = maker.apply(1);
    doesNotThrow = maker.apply(2);
    doesNotThrow = maker.apply(999);
  }

  @Test
  public void violatesDateRange_throws() {
    IntFunction<NearbyDatesMap<Double>> maker = maxCalendarRangeAllowed -> nearbyDatesMap(
        rbMapOf(
            LocalDate.of(1974, 4, 4), 0.44,
            LocalDate.of(1974, 4, 7), 0.77),
        maxCalendarRangeAllowed);
    assertIllegalArgumentException( () -> maker.apply(-999));
    assertIllegalArgumentException( () -> maker.apply(-1));
    assertIllegalArgumentException( () -> maker.apply(0));
    assertIllegalArgumentException( () -> maker.apply(1));
    assertIllegalArgumentException( () -> maker.apply(2));
    NearbyDatesMap<Double> doesNotThrow;
    doesNotThrow = maker.apply(3); // 3 days between the 7th and 4th of April of 1974
    doesNotThrow = maker.apply(999);
  }

  @Override
  public NearbyDatesMap<Double> makeTrivialObject() {
    return emptyNearbyDatesMap(1);
  }

  @Override
  public NearbyDatesMap<Double> makeNontrivialObject() {
    return nearbyDatesMap(
        rbMapOf(
            LocalDate.of(1974, 4, 4), 0.44,
            LocalDate.of(1974, 4, 6), 0.66),
        5);
  }

  @Override
  public NearbyDatesMap<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return nearbyDatesMap(
        rbMapOf(
            LocalDate.of(1974, 4, 4), 0.44 + e,
            LocalDate.of(1974, 4, 6), 0.66 + e),
        5);
  }

  @Override
  protected boolean willMatch(NearbyDatesMap<Double> expected, NearbyDatesMap<Double> actual) {
    return nearbyDatesMapMatcher(expected, f -> doubleAlmostEqualsMatcher(f, 1e-8)).matches(actual);
  }

  public static <V extends Comparable<? super V>> TypeSafeMatcher<NearbyDatesMap<V>> nearbyDatesMapMatcher(
      NearbyDatesMap<V> expected, MatcherGenerator<V> matcherGenerator) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getMaxCalendarRangeAllowed()),
        matchRBMap(v -> v.getRawDateMap(), matcherGenerator));
  }

}
