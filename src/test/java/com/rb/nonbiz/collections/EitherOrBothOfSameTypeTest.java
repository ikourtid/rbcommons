package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Optional;

import static com.rb.nonbiz.collections.EitherOrBothOfSameType.both;
import static com.rb.nonbiz.collections.EitherOrBothOfSameType.eitherOrBothOfSameType;
import static com.rb.nonbiz.collections.EitherOrBothOfSameType.leftOnly;
import static com.rb.nonbiz.collections.EitherOrBothOfSameType.rightOnly;
import static com.rb.nonbiz.collections.EitherOrBothTest.eitherOrBothMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class EitherOrBothOfSameTypeTest extends RBTestMatcher<EitherOrBothOfSameType<Double>> {

  @Test
  public void implementsEquals() {
    assertEquals(
        eitherOrBothOfSameType(Optional.of("a"), Optional.of("b")),
        eitherOrBothOfSameType(Optional.of("a"), Optional.of("b")));
    assertEquals(
        eitherOrBothOfSameType(Optional.of("a"), Optional.empty()),
        eitherOrBothOfSameType(Optional.of("a"), Optional.empty()));
    assertEquals(
        eitherOrBothOfSameType(Optional.empty(), Optional.of("b")),
        eitherOrBothOfSameType(Optional.empty(), Optional.of("b")));
  }

  @Test
  public void testAsList() {
    assertEquals(singletonList("a"), leftOnly("a").asList());
    assertEquals(singletonList("a"), rightOnly("a").asList());
    assertEquals(ImmutableList.of("a", "b"), both("a", "b").asList());
  }

  @Override
  public EitherOrBothOfSameType<Double> makeTrivialObject() {
    return leftOnly(1.1);
  }

  @Override
  public EitherOrBothOfSameType<Double> makeNontrivialObject() {
    return both(2.2, 3.3);
  }

  @Override
  public EitherOrBothOfSameType<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return both(2.2 + e, 3.3 + e);
  }

  @Override
  protected boolean willMatch(EitherOrBothOfSameType<Double> expected, EitherOrBothOfSameType<Double> actual) {
    return eitherOrBothOfSameTypeMatcher(expected, d -> doubleAlmostEqualsMatcher(d, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <T> TypeSafeMatcher<EitherOrBothOfSameType<T>> eitherOrBothOfSameTypeMatcher(
      EitherOrBothOfSameType<T> expected, MatcherGenerator<T> valueMatcherGenerator) {
    return makeMatcher(expected, actual ->
        eitherOrBothMatcher(expected.getRawValue(), valueMatcherGenerator, valueMatcherGenerator)
            .matches(actual.getRawValue()));
  }

}
