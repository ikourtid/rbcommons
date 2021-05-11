package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapWithDefault.emptyRBMapWithDefault;
import static com.rb.nonbiz.collections.RBMapWithDefault.rbMapWithDefault;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;

// This test class is not generic, but the publicly exposed static matcher is.
public class RBMapWithDefaultTest extends RBTestMatcher<RBMapWithDefault<String, Double>> {

  @Test
  public void testAllValuesPlusDefaultAsStream() {
    // This test uses sets so we won't have to worry about the ordering that the maps values will get returned in.
    assertThat(
        newRBSet(rbMapWithDefault(7, rbMapOf(
            "A1", 1,
            "A2", 2))
            .allValuesPlusDefaultAsStream()),
        rbSetEqualsMatcher(
            rbSetOf(1, 2, 7)));
  }

  @Override
  public RBMapWithDefault<String, Double> makeTrivialObject() {
    return emptyRBMapWithDefault(0.0);
  }

  @Override
  public RBMapWithDefault<String, Double> makeNontrivialObject() {
    return rbMapWithDefault(7.7, rbMapOf(
        "A1", 1.1,
        "A2", 2.2));
  }

  @Override
  public RBMapWithDefault<String, Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbMapWithDefault(7.7 + e, rbMapOf(
        "A1", 1.1 + e,
        "A2", 2.2 + e));
  }

  @Override
  protected boolean willMatch(RBMapWithDefault<String, Double> expected, RBMapWithDefault<String, Double> actual) {
    return rbMapWithDefaultMatcher(expected, f -> doubleAlmostEqualsMatcher(f, 1e-8)).matches(actual);
  }

  public static <K, V> TypeSafeMatcher<RBMapWithDefault<K, V>> rbMapWithDefaultEqualityMatcher(
      RBMapWithDefault<K, V> expected) {
    return rbMapWithDefaultMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <K, V> TypeSafeMatcher<RBMapWithDefault<K, V>> rbMapWithDefaultMatcher(
      RBMapWithDefault<K, V> expected, MatcherGenerator<V> matcherGenerator) {
    return makeMatcher(expected,
        match(     v -> v.getDefaultValue(), matcherGenerator),
        matchRBMap(v -> v.getRawRBMap(),     matcherGenerator));
  }

}
