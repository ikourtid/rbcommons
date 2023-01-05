package com.rb.nonbiz.types;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.types.WeightedByUnitFraction.weightedByUnitFraction;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class WeightedByUnitFractionTest extends RBTestMatcher<WeightedByUnitFraction<String>> {

  @Override
  public WeightedByUnitFraction<String> makeTrivialObject() {
    return weightedByUnitFraction("", UNIT_FRACTION_1);
  }

  @Override
  public WeightedByUnitFraction<String> makeNontrivialObject() {
    return weightedByUnitFraction("abc", unitFraction(0.1234));
  }

  @Override
  public WeightedByUnitFraction<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return weightedByUnitFraction("abc", unitFraction(0.1234 + e));
  }

  @Override
  protected boolean willMatch(WeightedByUnitFraction<String> expected, WeightedByUnitFraction<String> actual) {
    return weightedByUnitFractionMatcher(expected, v -> typeSafeEqualTo(v)).matches(actual);
  }

  public static <T> TypeSafeMatcher<WeightedByUnitFraction<T>> weightedByUnitFractionMatcher(
      WeightedByUnitFraction<T> expected, MatcherGenerator<T> itemMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getItem(), itemMatcherGenerator),
        matchUsingAlmostEquals(v -> v.getWeight(), DEFAULT_EPSILON_1e_8));
  }

}
