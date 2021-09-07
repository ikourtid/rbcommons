package com.rb.nonbiz.types;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.rbNumericValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.WeightedBy.weightedBy;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class WeightedByTest extends RBTestMatcher<WeightedBy<String, SignedFraction>> {

  @Override
  public WeightedBy<String, SignedFraction> makeTrivialObject() {
    return weightedBy("", SIGNED_FRACTION_1);
  }

  @Override
  public WeightedBy<String, SignedFraction> makeNontrivialObject() {
    return weightedBy("abc", signedFraction(-0.1234));
  }

  @Override
  public WeightedBy<String, SignedFraction> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return weightedBy("abc", signedFraction(-0.1234 + e));
  }

  @Override
  protected boolean willMatch(WeightedBy<String, SignedFraction> expected, WeightedBy<String, SignedFraction> actual) {
    return weightedByMatcher(expected, v -> typeSafeEqualTo(v)).matches(actual);
  }

  public static <T, W extends RBNumeric<W>> TypeSafeMatcher<WeightedBy<T, W>> weightedByMatcher(
      WeightedBy<T, W> expected, MatcherGenerator<T> itemMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getItem(), itemMatcherGenerator),
        match(v -> v.getWeight(), f -> rbNumericValueMatcher(f, 1e-8)));
  }

}
