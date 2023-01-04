package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.iidMapImpreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static com.rb.nonbiz.types.PositiveMultipliersIidMap.emptyPositiveMultipliersIidMap;
import static com.rb.nonbiz.types.PositiveMultipliersIidMap.positiveMultipliersIidMap;
import static com.rb.nonbiz.types.PositiveMultipliersIidMap.singletonPositiveMultipliersIidMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PositiveMultipliersIidMapTest extends RBTestMatcher<PositiveMultipliersIidMap> {

  @Test
  public void testGetters() {
    PositiveMultipliersIidMap emptyMap = emptyPositiveMultipliersIidMap();
    assertEquals(0, emptyMap.size());
    assertTrue(emptyMap.isEmpty());

    PositiveMultipliersIidMap positiveMultipliersIidMap = singletonPositiveMultipliersIidMap(
        STOCK_A, positiveMultiplier(1.23));
    assertEquals(1, positiveMultipliersIidMap.size());
    assertFalse(positiveMultipliersIidMap.isEmpty());
  }

  @Override
  public PositiveMultipliersIidMap makeTrivialObject() {
    return emptyPositiveMultipliersIidMap();
  }

  @Override
  public PositiveMultipliersIidMap makeNontrivialObject() {
    return positiveMultipliersIidMap(iidMapOf(
        STOCK_A, positiveMultiplier(1.1),
        STOCK_B, positiveMultiplier(2.2)));
  }

  @Override
  public PositiveMultipliersIidMap makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return positiveMultipliersIidMap(iidMapOf(
        STOCK_A, positiveMultiplier(1.1 + e),
        STOCK_B, positiveMultiplier(2.2 + e)));
  }

  @Override
  protected boolean willMatch(PositiveMultipliersIidMap expected, PositiveMultipliersIidMap actual) {
    return positiveMultipliersIidMapMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<PositiveMultipliersIidMap> positiveMultipliersIidMapMatcher(
      PositiveMultipliersIidMap expected) {
    return positiveMultipliersIidMapMatcher(expected, DEFAULT_EPSILON_1e_8);
  }

  public static TypeSafeMatcher<PositiveMultipliersIidMap> positiveMultipliersIidMapMatcher(
      PositiveMultipliersIidMap expected, Epsilon epsilon) {
    return makeMatcher(expected,
        match(v -> v.getRawMap(), f -> iidMapImpreciseValueMatcher(f, epsilon)));
  }

}
