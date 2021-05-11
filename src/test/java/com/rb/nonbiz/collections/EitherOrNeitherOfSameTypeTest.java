package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.EitherOrNeitherOfSameType.left;
import static com.rb.nonbiz.collections.EitherOrNeitherOfSameType.neither;
import static com.rb.nonbiz.collections.EitherOrNeitherOfSameType.right;
import static com.rb.nonbiz.collections.EitherOrNeitherTest.eitherOrNeitherMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class EitherOrNeitherOfSameTypeTest extends RBTestMatcher<EitherOrNeitherOfSameType<Double>> {

  @Test
  public void testAsList() {
    assertEquals(singletonList("a"), left("a").asList());
    assertEquals(singletonList("a"), right("a").asList());
    assertEquals(emptyList(), neither().asList());
  }

  @Override
  public EitherOrNeitherOfSameType<Double> makeTrivialObject() {
    return neither();
  }

  @Override
  public EitherOrNeitherOfSameType<Double> makeNontrivialObject() {
    return left(1.1);
  }

  @Override
  public EitherOrNeitherOfSameType<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return left(1.1 + e);
  }

  @Override
  protected boolean willMatch(EitherOrNeitherOfSameType<Double> expected, EitherOrNeitherOfSameType<Double> actual) {
    return eitherOrNeitherOfSameTypeMatcher(expected, d -> doubleAlmostEqualsMatcher(d, 1e-8)).matches(actual);
  }

  public static <T> TypeSafeMatcher<EitherOrNeitherOfSameType<T>> eitherOrNeitherOfSameTypeMatcher(
      EitherOrNeitherOfSameType<T> expected, MatcherGenerator<T> valueMatcherGenerator) {
    return makeMatcher(expected, actual ->
        eitherOrNeitherMatcher(expected.getRawValue(), valueMatcherGenerator, valueMatcherGenerator)
            .matches(actual.getRawValue()));
  }

}
