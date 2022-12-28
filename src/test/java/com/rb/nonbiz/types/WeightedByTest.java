package com.rb.nonbiz.types;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.rbNumericValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.WeightedBy.weightedBy;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class WeightedByTest extends RBTestMatcher<WeightedBy<SignedFraction, String>> {

  @Override
  public WeightedBy<SignedFraction, String> makeTrivialObject() {
    return weightedBy("", SIGNED_FRACTION_1);
  }

  @Override
  public WeightedBy<SignedFraction, String> makeNontrivialObject() {
    return weightedBy("abc", signedFraction(-0.1234));
  }

  @Override
  public WeightedBy<SignedFraction, String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return weightedBy("abc", signedFraction(-0.1234 + e));
  }

  @Override
  protected boolean willMatch(WeightedBy<SignedFraction, String> expected, WeightedBy<SignedFraction, String> actual) {
    return weightedByMatcher(expected, v -> typeSafeEqualTo(v)).matches(actual);
  }

  public static <W extends RBNumeric<W>, T> TypeSafeMatcher<WeightedBy<W, T>> weightedByMatcher(
      WeightedBy<W, T> expected, MatcherGenerator<T> itemMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getItem(), itemMatcherGenerator),
        match(v -> v.getWeight(), f -> rbNumericValueMatcher(f, DEFAULT_EPSILON_1e_8)));
  }

}
