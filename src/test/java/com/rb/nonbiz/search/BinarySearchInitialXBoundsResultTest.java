package com.rb.nonbiz.search;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.ClosedRangeTest.closedRangeMatcher;
import static com.rb.nonbiz.collections.OneOf3Test.oneOf3Matcher;
import static com.rb.nonbiz.search.BinarySearchInitialXBoundsResult.binarySearchBoundsCanBracketTargetY;
import static com.rb.nonbiz.search.BinarySearchInitialXBoundsResult.xLowerBoundEvaluatesToAboveTargetY;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

// This test class is not generic, but the publicly exposed static matcher is.
public class BinarySearchInitialXBoundsResultTest extends RBTestMatcher<BinarySearchInitialXBoundsResult<Double>> {

  @Override
  public BinarySearchInitialXBoundsResult<Double> makeTrivialObject() {
    return xLowerBoundEvaluatesToAboveTargetY(0.0);
  }

  @Override
  public BinarySearchInitialXBoundsResult<Double> makeNontrivialObject() {
    return binarySearchBoundsCanBracketTargetY(closedRange(-1.1, 3.3));
  }

  @Override
  public BinarySearchInitialXBoundsResult<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return binarySearchBoundsCanBracketTargetY(closedRange(-1.1 + e, 3.3 + e));
  }

  @Override
  protected boolean willMatch(BinarySearchInitialXBoundsResult<Double> expected,
                              BinarySearchInitialXBoundsResult<Double> actual) {
    return binarySearchInitialXBoundsResultMatcher(expected, f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <X extends Comparable<? super X>> TypeSafeMatcher<BinarySearchInitialXBoundsResult<X>>
  binarySearchInitialXBoundsResultMatcher(
      BinarySearchInitialXBoundsResult<X> expected, MatcherGenerator<X> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getRawOneOf3(), f -> oneOf3Matcher(f,
            f2 -> closedRangeMatcher(f2, matcherGenerator),
            matcherGenerator,
            matcherGenerator)));
  }

}
