package com.rb.nonbiz.search;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.search.ShortCircuitedBinarySearchResultForLowerBoundOnly.shortCircuitedBinarySearchResultForLowerBoundOnly;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

// This test class is not generic, but the publicly exposed static matcher is.
public class ShortCircuitedBinarySearchResultForLowerBoundOnlyTest
    extends RBTestMatcher<ShortCircuitedBinarySearchResultForLowerBoundOnly<Double, String>> {

  @Override
  public ShortCircuitedBinarySearchResultForLowerBoundOnly<Double, String> makeTrivialObject() {
    return shortCircuitedBinarySearchResultForLowerBoundOnly(0.0, "");
  }

  @Override
  public ShortCircuitedBinarySearchResultForLowerBoundOnly<Double, String> makeNontrivialObject() {
    return shortCircuitedBinarySearchResultForLowerBoundOnly(1.1, "a");
  }

  @Override
  public ShortCircuitedBinarySearchResultForLowerBoundOnly<Double, String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return shortCircuitedBinarySearchResultForLowerBoundOnly(1.1 + e, "a");
  }

  @Override
  protected boolean willMatch(ShortCircuitedBinarySearchResultForLowerBoundOnly<Double, String> expected,
                              ShortCircuitedBinarySearchResultForLowerBoundOnly<Double, String> actual) {
    return shortCircuitedBinarySearchResultForLowerBoundOnlyMatcher(
        expected,
        f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8),
        f -> typeSafeEqualTo(f))
        .matches(actual);
  }

  public static <X, Y> TypeSafeMatcher<ShortCircuitedBinarySearchResultForLowerBoundOnly<X, Y>>
  shortCircuitedBinarySearchResultForLowerBoundOnlyMatcher(
      ShortCircuitedBinarySearchResultForLowerBoundOnly<X, Y> expected,
      MatcherGenerator<X> xMatcherGenerator,
      MatcherGenerator<Y> yMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getLowerBoundX(), xMatcherGenerator),
        match(v -> v.getLowerBoundY(), yMatcherGenerator));
  }

}
