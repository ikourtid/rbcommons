package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBMapWithOptionalDefault.emptyRBMapWithOptionalDefaultMissing;
import static com.rb.nonbiz.collections.RBMapWithOptionalDefault.emptyRBMapWithOptionalDefaultPresent;
import static com.rb.nonbiz.collections.RBMapWithOptionalDefault.rbMapWithOptionalDefaultMissing;
import static com.rb.nonbiz.collections.RBMapWithOptionalDefault.rbMapWithOptionalDefaultPresent;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

// This test class is not generic, but the publicly exposed static matcher is.
public class RBMapWithOptionalDefaultTest extends RBTestMatcher<RBMapWithOptionalDefault<String, Double>> {

  @Test
  public void testGetOrDefault() {
    RBMapWithOptionalDefault<String, Integer> withDefaultPresent =  rbMapWithOptionalDefaultPresent(77, rbMapOf(
        "A1", 11,
        "A2", 22));
    assertOptionalEquals(11, withDefaultPresent.getOrDefault("A1"));
    assertOptionalEquals(22, withDefaultPresent.getOrDefault("A2"));
    assertOptionalEquals(77, withDefaultPresent.getOrDefault("missing key"));

    RBMapWithOptionalDefault<String, Integer> withDefaultMissing =  rbMapWithOptionalDefaultMissing(rbMapOf(
        "A1", 11,
        "A2", 22));
    assertOptionalEquals(11, withDefaultMissing.getOrDefault("A1"));
    assertOptionalEquals(22, withDefaultMissing.getOrDefault("A2"));
    assertOptionalEmpty(withDefaultMissing.getOrDefault("missing key"));

    assertOptionalEquals(77, emptyRBMapWithOptionalDefaultPresent(77).getOrDefault("any key"));
    assertOptionalEquals(77, emptyRBMapWithOptionalDefaultPresent(77).getOrDefault(""));

    assertOptionalEmpty(emptyRBMapWithOptionalDefaultMissing().getOrDefault("any key"));
    assertOptionalEmpty(emptyRBMapWithOptionalDefaultMissing().getOrDefault(""));
  }

  @Test
  public void testHasNoDefaultValueOrOverrides() {
    assertTrue(emptyRBMapWithOptionalDefaultMissing().hasNoDefaultValueOrOverrides());
    assertFalse(rbMapWithOptionalDefaultPresent(123, singletonRBMap(DUMMY_STRING, 456)).hasNoDefaultValueOrOverrides());
    assertFalse(rbMapWithOptionalDefaultMissing(singletonRBMap(DUMMY_STRING, 456))     .hasNoDefaultValueOrOverrides());
    assertFalse(emptyRBMapWithOptionalDefaultPresent(123)                              .hasNoDefaultValueOrOverrides());
  }

  @Override
  public RBMapWithOptionalDefault<String, Double> makeTrivialObject() {
    return emptyRBMapWithOptionalDefaultMissing();
  }

  @Override
  public RBMapWithOptionalDefault<String, Double> makeNontrivialObject() {
    return rbMapWithOptionalDefaultPresent(7.7, rbMapOf(
        "A1", 1.1,
        "A2", 2.2));
  }

  @Override
  public RBMapWithOptionalDefault<String, Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbMapWithOptionalDefaultPresent(7.7 + e, rbMapOf(
        "A1", 1.1 + e,
        "A2", 2.2 + e));
  }

  @Override
  protected boolean willMatch(RBMapWithOptionalDefault<String, Double> expected,
                              RBMapWithOptionalDefault<String, Double> actual) {
    return rbMapWithOptionalDefaultMatcher(expected, f -> doubleAlmostEqualsMatcher(f, 1e-8)).matches(actual);
  }

  public static <K, V> TypeSafeMatcher<RBMapWithOptionalDefault<K, V>> rbMapWithOptionalDefaultEqualityMatcher(
      RBMapWithOptionalDefault<K, V> expected) {
    return rbMapWithOptionalDefaultMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <K, V> TypeSafeMatcher<RBMapWithOptionalDefault<K, V>> rbMapWithOptionalDefaultMatcher(
      RBMapWithOptionalDefault<K, V> expected, MatcherGenerator<V> matcherGenerator) {
    return makeMatcher(expected,
        matchOptional(v -> v.getOptionalDefaultValue(), matcherGenerator),
        matchRBMap(   v -> v.getRawRBMap(),             matcherGenerator));
  }

}
