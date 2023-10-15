package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.nonbiz.collections.IidBiMap.iidBiMap;
import static com.rb.nonbiz.collections.IidBiMaps.emptyIidBiMap;
import static com.rb.nonbiz.collections.IidBiMaps.singletonIidBiMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.collections.IidMapTest.iidMapEqualityMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class IidBiMapTest extends RBTestMatcher<IidBiMap<String>> {

  @Test
  public void testGetters() {
    IidBiMap<String> iidBiMap = singletonIidBiMap(STOCK_A, "A");

    assertThat(
        iidBiMap.getItemFromInstrumentId(),
        iidMapEqualityMatcher(singletonIidMap(STOCK_A, "A")));

    assertEquals(
        iidBiMap.getInstrumentIdFromItem(),
        singletonRBMap("A", STOCK_A));

    assertFalse(iidBiMap.isEmpty());
    assertTrue(emptyIidBiMap().isEmpty());

    assertEquals(1, iidBiMap.size());
    assertEquals(0, emptyIidBiMap().size());
  }

  @Test
  public void mappingIsNotInvertible_throws() {
    assertIllegalArgumentException( () -> iidBiMap(iidMapOf(
        STOCK_A, "X",
        STOCK_B, "X")));
  }

  @Test
  public void testTransformValuesCopyOrThrow() {
    IidBiMap<Integer> map = iidBiMap(iidMapOf(
        STOCK_A, 11,
        STOCK_B, 22));
    assertThat(
        map.transformValuesCopyOrThrow(v -> "_" + Integer.toString(v)),
        iidBiMapMatcher(
            iidBiMap(iidMapOf(
                STOCK_A, "_11",
                STOCK_B, "_22"))));
    // Since this is a bidirectional map, we can't have the same value appearing more than once.
    assertIllegalArgumentException( () -> map.transformValuesCopyOrThrow(v -> "sameValue"));
  }

  @Override
  public IidBiMap<String> makeTrivialObject() {
    return emptyIidBiMap();
  }

  @Override
  public IidBiMap<String> makeNontrivialObject() {
    return iidBiMap(iidMapOf(
        STOCK_A, "AAA",
        STOCK_B, "BBB"));
  }

  @Override
  public IidBiMap<String> makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return iidBiMap(iidMapOf(
        STOCK_A, "AAA",
        STOCK_B, "BBB"));
  }

  @Override
  protected boolean willMatch(IidBiMap<String> expected, IidBiMap<String> actual) {
    return iidBiMapMatcher(expected).matches(actual);
  }

  // No need to pass in a MatcherGenerator<V>, because the requirement for generating an IidBiMap
  // is a type V that implements a non-trivial hashCode / equals.
  // That is, all objects have an implementation; there's no need to implement some interface, because these are
  // derived from java.lang.Object. My point is that we implement it ourselves so that it's not the default
  // behavior of a pointer comparison.
  public static <V> TypeSafeMatcher<IidBiMap<V>> iidBiMapMatcher(IidBiMap<V> expected) {
    return makeMatcher(expected,
        match(v -> v.getItemFromInstrumentId(), f -> iidMapEqualityMatcher(f)),
        match(v -> v.getInstrumentIdFromItem(), f -> rbMapMatcher(f, f2 -> typeSafeEqualTo(f2))));
  }

}
