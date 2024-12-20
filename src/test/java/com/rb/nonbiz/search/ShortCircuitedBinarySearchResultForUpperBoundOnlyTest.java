package com.rb.nonbiz.search;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.search.ShortCircuitedBinarySearchResultForUpperBoundOnly.shortCircuitedBinarySearchResultForUpperBoundOnly;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

// This test class is not generic, but the publicly exposed static matcher is.
public class ShortCircuitedBinarySearchResultForUpperBoundOnlyTest
extends RBTestMatcher<ShortCircuitedBinarySearchResultForUpperBoundOnly<Double, String>> {

  @Override
  public ShortCircuitedBinarySearchResultForUpperBoundOnly<Double, String> makeTrivialObject() {
    return shortCircuitedBinarySearchResultForUpperBoundOnly(0.0, "");
  }

  @Override
  public ShortCircuitedBinarySearchResultForUpperBoundOnly<Double, String> makeNontrivialObject() {
    return shortCircuitedBinarySearchResultForUpperBoundOnly(1.1, "a");
  }

  @Override
  public ShortCircuitedBinarySearchResultForUpperBoundOnly<Double, String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return shortCircuitedBinarySearchResultForUpperBoundOnly(1.1 + e, "a");
  }

  @Override
  protected boolean willMatch(ShortCircuitedBinarySearchResultForUpperBoundOnly<Double, String> expected,
                              ShortCircuitedBinarySearchResultForUpperBoundOnly<Double, String> actual) {
    return shortCircuitedBinarySearchResultForUpperBoundOnlyMatcher(
        expected,
        f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8),
        f -> typeSafeEqualTo(f))
        .matches(actual);
  }

  public static <X, Y> TypeSafeMatcher<ShortCircuitedBinarySearchResultForUpperBoundOnly<X, Y>>
      shortCircuitedBinarySearchResultForUpperBoundOnlyMatcher(
          ShortCircuitedBinarySearchResultForUpperBoundOnly<X, Y> expected,
          MatcherGenerator<X> xMatcherGenerator,
          MatcherGenerator<Y> yMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getUpperBoundX(), xMatcherGenerator),
        match(v -> v.getUpperBoundY(), yMatcherGenerator));
  }

}
