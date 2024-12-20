package com.rb.nonbiz.search;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.collections.OneOf3Test.oneOf3Matcher;
import static com.rb.nonbiz.search.BinarySearchResultTest.binarySearchResultMatcher;
import static com.rb.nonbiz.search.PossiblyShortCircuitedBinarySearchResult.notShortCircuitedBinarySearchResult;
import static com.rb.nonbiz.search.PossiblyShortCircuitedBinarySearchResult.resultWhenOnlyValidLowerBoundExists;
import static com.rb.nonbiz.search.ShortCircuitedBinarySearchResultForLowerBoundOnlyTest.shortCircuitedBinarySearchResultForLowerBoundOnlyMatcher;
import static com.rb.nonbiz.search.ShortCircuitedBinarySearchResultForUpperBoundOnlyTest.shortCircuitedBinarySearchResultForUpperBoundOnlyMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

// This test class is not generic, but the publicly exposed static matcher is.
public class PossiblyShortCircuitedBinarySearchResultTest
  extends RBTestMatcher<PossiblyShortCircuitedBinarySearchResult<Double, String>> {

  @Override
  public PossiblyShortCircuitedBinarySearchResult<Double, String> makeTrivialObject() {
    return resultWhenOnlyValidLowerBoundExists(
        new ShortCircuitedBinarySearchResultForLowerBoundOnlyTest().makeTrivialObject());
  }

  @Override
  public PossiblyShortCircuitedBinarySearchResult<Double, String> makeNontrivialObject() {
    return notShortCircuitedBinarySearchResult(new BinarySearchResultTest().makeNontrivialObject());
  }

  @Override
  public PossiblyShortCircuitedBinarySearchResult<Double, String> makeMatchingNontrivialObject() {
    return notShortCircuitedBinarySearchResult(new BinarySearchResultTest().makeMatchingNontrivialObject());
  }

  @Override
  protected boolean willMatch(PossiblyShortCircuitedBinarySearchResult<Double, String> expected,
                              PossiblyShortCircuitedBinarySearchResult<Double, String> actual) {
    return possiblyShortCircuitedBinarySearchResultMatcher(expected,
        f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8),
        f -> typeSafeEqualTo(f))
        .matches(actual);
  }

  public static <X, Y> TypeSafeMatcher<PossiblyShortCircuitedBinarySearchResult<X, Y>>
      possiblyShortCircuitedBinarySearchResultMatcher(
          PossiblyShortCircuitedBinarySearchResult<X, Y> expected,
          MatcherGenerator<X> xMatcherGenerator,
          MatcherGenerator<Y> yMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getRawOneOf3(), f -> oneOf3Matcher(f,
            f2 -> binarySearchResultMatcher(                               f2, xMatcherGenerator, yMatcherGenerator),
            f3 -> shortCircuitedBinarySearchResultForLowerBoundOnlyMatcher(f3, xMatcherGenerator, yMatcherGenerator),
            f4 -> shortCircuitedBinarySearchResultForUpperBoundOnlyMatcher(f4, xMatcherGenerator, yMatcherGenerator))));
  }

}
