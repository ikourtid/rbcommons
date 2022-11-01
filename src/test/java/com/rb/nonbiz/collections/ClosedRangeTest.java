package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.Year;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.ClosedRange.optionalIntersection;
import static com.rb.nonbiz.collections.ClosedRange.singletonClosedRange;
import static com.rb.nonbiz.collections.RBRangesTest.allNonClosedRanges;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.rangeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;

// This test class is not generic, but the publicly exposed static matcher is.
public class ClosedRangeTest extends RBTestMatcher<ClosedRange<Year>> {

  @Test
  public void endpointsAreReversed_throws() {
    assertIllegalArgumentException( () -> closedRange(Year.of(2005), Year.of(2001)));
    ClosedRange<Year> doesNotThrow;
    doesNotThrow = closedRange(Year.of(2001), Year.of(2005));
    doesNotThrow = closedRange(Year.of(2001), Year.of(2001));
  }

  @Test
  public void rangeIsNotClosedOnBothEnds_throws() {
    allNonClosedRanges(123, 456).forEach(nonClosedRange ->
        assertIllegalArgumentException( () -> ClosedRange.<Integer>closedRange(nonClosedRange)));
  }

  @Test
  public void testOptionalIntersection() {
    assertOptionalEmpty(optionalIntersection(closedRange(1, 3), closedRange(5, 7)));
    assertOptionalEmpty(optionalIntersection(closedRange(5, 7), closedRange(1, 3)));
  }

  @Test
  public void testOptionalIntersection_intersectionIsNotSingleton_returnsNonEmptyIntersection() {
    assertOptionalEquals(
        closedRange(2.2, 3.3),
        optionalIntersection(closedRange(1.1, 3.3), closedRange(2.2, 4.4)));
    assertOptionalEquals(
        closedRange(2.2, 3.3),
        optionalIntersection(closedRange(2.2, 4.4), closedRange(1.1, 3.3)));
  }

  @Test
  public void testOptionalIntersection_intersectionResultIsSingleton_returnsNonEmptyIntersection() {
    assertOptionalEquals(
        closedRange(1.1, 1.1),
        optionalIntersection(closedRange(1.1, 1.1), closedRange(1.1, 3.3)));
    assertOptionalEquals(
        closedRange(1.1, 1.1),
        optionalIntersection(closedRange(1.1, 3.3), closedRange(1.1, 1.1)));

    assertOptionalEquals(
        closedRange(3.3, 3.3),
        optionalIntersection(closedRange(1.1, 3.3), closedRange(3.3, 5.5)));
    assertOptionalEquals(
        closedRange(3.3, 3.3),
        optionalIntersection(closedRange(3.3, 5.5), closedRange(1.1, 3.3)));
  }

  @Test
  public void testOptionalIntersection_intersectionOfTwoSingletons_returnsNonEmptyIntersection() {
    assertOptionalEquals(
        closedRange(1.1, 1.1),
        optionalIntersection(closedRange(1.1, 1.1), closedRange(1.1, 1.1)));
  }

  @Override
  public ClosedRange<Year> makeTrivialObject() {
    return singletonClosedRange(Year.of(2000));
  }

  @Override
  public ClosedRange<Year> makeNontrivialObject() {
    return closedRange(Year.of(2001), Year.of(2005));
  }

  @Override
  public ClosedRange<Year> makeMatchingNontrivialObject() {
    // nothing to tweak here
    return closedRange(Year.of(2001), Year.of(2005));
  }

  @Override
  protected boolean willMatch(ClosedRange<Year> expected, ClosedRange<Year> actual) {
    return closedRangeMatcher(expected, f -> typeSafeEqualTo(f)).matches(actual);
  }

  public static <T extends Comparable<? super T>> TypeSafeMatcher<ClosedRange<T>> closedRangeEqualityMatcher(
      ClosedRange<T> expected) {
    return closedRangeMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <T extends Comparable<? super T>> TypeSafeMatcher<ClosedRange<T>> closedRangeMatcher(
      ClosedRange<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.asRange(), f -> rangeMatcher(f, matcherGenerator)));
  }

}
