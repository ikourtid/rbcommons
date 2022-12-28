package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapImpreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static com.rb.nonbiz.types.PositiveMultipliersMap.emptyPositiveMultipliersMap;
import static com.rb.nonbiz.types.PositiveMultipliersMap.positiveMultipliersMap;
import static com.rb.nonbiz.types.PositiveMultipliersMap.singletonPositiveMultipliersMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// This test class is not generic, but the publicly exposed static matcher is.
public class PositiveMultipliersMapTest extends RBTestMatcher<PositiveMultipliersMap<String>> {

  @Test
  public void testGetters() {
    PositiveMultipliersMap<String> emptyMap = emptyPositiveMultipliersMap();
    assertEquals(0, emptyMap.size());
    assertTrue(emptyMap.isEmpty());

    PositiveMultipliersMap<String> positiveMultipliersMap = singletonPositiveMultipliersMap(
        "A", positiveMultiplier(1.23));
    assertEquals(1, positiveMultipliersMap.size());
    assertFalse(positiveMultipliersMap.isEmpty());
  }

  @Override
  public PositiveMultipliersMap<String> makeTrivialObject() {
    return emptyPositiveMultipliersMap();
  }

  @Override
  public PositiveMultipliersMap<String> makeNontrivialObject() {
    return positiveMultipliersMap(rbMapOf(
        "a", positiveMultiplier(1.1),
        "b", positiveMultiplier(2.2)));
  }

  @Override
  public PositiveMultipliersMap<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return positiveMultipliersMap(rbMapOf(
        "a", positiveMultiplier(1.1 + e),
        "b", positiveMultiplier(2.2 + e)));
  }

  @Override
  protected boolean willMatch(PositiveMultipliersMap<String> expected, PositiveMultipliersMap<String> actual) {
    return positiveMultipliersMapMatcher(expected).matches(actual);
  }

  public static <K> TypeSafeMatcher<PositiveMultipliersMap<K>> positiveMultipliersMapMatcher(
      PositiveMultipliersMap<K> expected) {
    return positiveMultipliersMapMatcher(expected, DEFAULT_EPSILON_1e_8);
  }

  public static <K> TypeSafeMatcher<PositiveMultipliersMap<K>> positiveMultipliersMapMatcher(
      PositiveMultipliersMap<K> expected, Epsilon epsilon) {
    return makeMatcher(expected,
        match(v -> v.getRawMap(), f -> rbMapImpreciseValueMatcher(f, epsilon)));
  }

}
