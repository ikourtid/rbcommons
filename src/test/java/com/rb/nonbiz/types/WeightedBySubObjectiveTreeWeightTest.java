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
import static com.rb.nonbiz.types.SubObjectiveTreeWeight.SUB_OBJECTIVE_WEIGHT_1;
import static com.rb.nonbiz.types.SubObjectiveTreeWeight.subObjectiveTreeWeight;
import static com.rb.nonbiz.types.WeightedBySubObjectiveTreeWeight.weightedBySubObjectiveTreeWeight;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class WeightedBySubObjectiveTreeWeightTest extends RBTestMatcher<WeightedBySubObjectiveTreeWeight<String>> {

  @Override
  public WeightedBySubObjectiveTreeWeight<String> makeTrivialObject() {
    return weightedBySubObjectiveTreeWeight("", SUB_OBJECTIVE_WEIGHT_1);
  }

  @Override
  public WeightedBySubObjectiveTreeWeight<String> makeNontrivialObject() {
    return weightedBySubObjectiveTreeWeight("abc", subObjectiveTreeWeight(-0.1234));
  }

  @Override
  public WeightedBySubObjectiveTreeWeight<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return weightedBySubObjectiveTreeWeight("abc", subObjectiveTreeWeight(-0.1234 + e));
  }

  @Override
  protected boolean willMatch(WeightedBySubObjectiveTreeWeight<String> expected,
                              WeightedBySubObjectiveTreeWeight<String> actual) {
    return weightedBySubObjectiveTreeWeightMatcher(expected, v -> typeSafeEqualTo(v)).matches(actual);
  }

  public static <T> TypeSafeMatcher<WeightedBySubObjectiveTreeWeight<T>> weightedBySubObjectiveTreeWeightMatcher(
      WeightedBySubObjectiveTreeWeight<T> expected, MatcherGenerator<T> itemMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getItem(), itemMatcherGenerator),
        matchUsingAlmostEquals(v -> v.getWeight(), 1e-8));
  }

}
