package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.PairOfSameType.pairOfSameType;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static junit.framework.TestCase.assertEquals;

// This test class is not generic, but the publicly exposed matcher is.
public class PairOfSameTypeTest extends RBTestMatcher<PairOfSameType<Double>> {

  @Test
  public void implementsEquals() {
    assertEquals(pairOfSameType(1, "a"), pairOfSameType(1, "a"));
  }

  @Override
  public PairOfSameType<Double> makeTrivialObject() {
    return pairOfSameType(0.0, 0.0);
  }

  @Override
  public PairOfSameType<Double> makeNontrivialObject() {
    return pairOfSameType(-1.1, 3.3);
  }

  @Override
  public PairOfSameType<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return pairOfSameType(-1.1 + e, 3.3 + e);
  }

  @Override
  protected boolean willMatch(PairOfSameType<Double> expected, PairOfSameType<Double> actual) {
    return pairOfSameTypeMatcher(expected, f -> doubleAlmostEqualsMatcher(f, 1e-8)).matches(actual);
  }

  public static <T>TypeSafeMatcher<PairOfSameType<T>> pairOfSameTypeMatcher(
      PairOfSameType<T> expected,
      MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getLeft(),  matcherGenerator),
        match(v -> v.getRight(), matcherGenerator));
  }

}
