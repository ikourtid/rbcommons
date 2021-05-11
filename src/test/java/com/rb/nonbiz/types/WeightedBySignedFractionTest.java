package com.rb.nonbiz.types;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.WeightedBySignedFraction.weightedBySignedFraction;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class WeightedBySignedFractionTest extends RBTestMatcher<WeightedBySignedFraction<String>> {

  @Override
  public WeightedBySignedFraction<String> makeTrivialObject() {
    return weightedBySignedFraction("", SIGNED_FRACTION_1);
  }

  @Override
  public WeightedBySignedFraction<String> makeNontrivialObject() {
    return weightedBySignedFraction("abc", signedFraction(-0.1234));
  }

  @Override
  public WeightedBySignedFraction<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return weightedBySignedFraction("abc", signedFraction(-0.1234 + e));
  }

  @Override
  protected boolean willMatch(WeightedBySignedFraction<String> expected, WeightedBySignedFraction<String> actual) {
    return weightedBySignedFractionMatcher(expected, v -> typeSafeEqualTo(v)).matches(actual);
  }

  public static <T> TypeSafeMatcher<WeightedBySignedFraction<T>> weightedBySignedFractionMatcher(
      WeightedBySignedFraction<T> expected, MatcherGenerator<T> itemMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getItem(), itemMatcherGenerator),
        matchUsingAlmostEquals(v -> v.getWeight(), 1e-8));
  }

}
