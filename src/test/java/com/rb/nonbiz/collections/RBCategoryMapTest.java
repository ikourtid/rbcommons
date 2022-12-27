package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableSet;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBCategoryMap.emptyRBCategoryMap;
import static com.rb.nonbiz.collections.RBCategoryMap.rbCategoryMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;

// This test class is not generic, but the publicly exposed typesafe matcher is.
public class RBCategoryMapTest extends RBTestMatcher<RBCategoryMap<String, Double>> {

  @Test
  public void testSpecialConstructors() {
    assertThat(
        rbCategoryMap(ImmutableSet.of("a", "b"), () -> 1.1),
        rbCategoryMapMatcher(
            rbCategoryMap(1.1, rbMapOf(
                "a", 1.1,
                "b", 1.1)),
            f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8)));

    assertThat(
        rbCategoryMap(ImmutableSet.of("a", "b"), "ALL", v -> v + "_"),
        rbCategoryMapMatcher(
            rbCategoryMap("ALL", rbMapOf(
                "a", "a_",
                "b", "b_")),
            f -> typeSafeEqualTo(f)));
  }

  @Override
  public RBCategoryMap<String, Double> makeTrivialObject() {
    return emptyRBCategoryMap(0.0);
  }

  @Override
  public RBCategoryMap<String, Double> makeNontrivialObject() {
    return rbCategoryMap(3.3, rbMapOf(
        "a", 1.1,
        "b", 2.2));
  }

  @Override
  public RBCategoryMap<String, Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbCategoryMap(3.3 + e, rbMapOf(
        "a", 1.1 + e,
        "b", 2.2 + e));
  }

  @Override
  protected boolean willMatch(RBCategoryMap<String, Double> expected, RBCategoryMap<String, Double> actual) {
    return rbCategoryMapMatcher(expected, f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <K, V> TypeSafeMatcher<RBCategoryMap<K, V>> rbCategoryMapMatcher(
      RBCategoryMap<K, V> expected, MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getValueRegardlessOfCategory(), valueMatcherGenerator),
        matchRBMap(v -> v.getRawMap(), valueMatcherGenerator));
  }

}
