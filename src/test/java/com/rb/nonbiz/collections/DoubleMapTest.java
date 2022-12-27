package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableSet;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.PrintsMultilineString;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.Weighted;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Iterator;

import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
import static com.rb.nonbiz.collections.DoubleMap.emptyDoubleMap;
import static com.rb.nonbiz.collections.DoubleMap.singletonDoubleMap;
import static com.rb.nonbiz.collections.RBIterables.sumDoubles;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapDoubleMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DoubleMapTest extends RBTestMatcher<DoubleMap<String>> {

  @Test
  public void testSum() {
    assertEquals(0.0, emptyDoubleMap().sum(), 1e-8);
    assertEquals(0.0, singletonDoubleMap("a", 0.0).sum(), 1e-8);
    assertEquals(1.11, singletonDoubleMap("a", 1.11).sum(), 1e-8);
    assertEquals(0.00, doubleMap(rbMapOf(
        "a",  1.11,
        "b", -1.11)).sum(), 1e-8);
    assertEquals(6.66, doubleMap(rbMapOf(
        "a", 1.11,
        "b", 2.22,
        "c", 3.33)).sum(), 1e-8);
  }

  @Test
  public void testGetters() {
    assertEquals(0, emptyDoubleMap().size());
    assertEquals(
        emptyDoubleMap().entrySet(),
        emptySet());
    assertEquals(0.0, sumDoubles(emptyDoubleMap().values()), 1e-8);

    DoubleMap<String> singletonDoubleMap = singletonDoubleMap("a", 1.11);
    assertEquals(1, singletonDoubleMap.size());
    assertEquals(
        singletonDoubleMap.keySet(),
        ImmutableSet.of("a"));
    assertEquals(1.11, sumDoubles(singletonDoubleMap.values()), 1e-8);

    DoubleMap<String> doubleMap = doubleMap(rbMapOf(
        "a", 1.11,
        "b", 2.22));
    assertEquals(2, doubleMap.size());
    assertEquals(
        ImmutableSet.of("a", "b"),
        doubleMap.keySet());
    assertEquals(3.33, sumDoubles(doubleMap.values()), 1e-8);
  }

  @Test
  public void testIterator() {
    DoubleMap<String> doubleMap = doubleMap(rbMapOf(
        "a", 1.11,
        "b", 2.22,
        "c", 3.33));
    StringBuilder concatenatedKeys = new StringBuilder();
    double sum = 0.0;
    Iterator<Weighted<String>> iterator = doubleMap.iterator();
    while (iterator.hasNext()) {
      Weighted<String> weightedDouble = iterator.next();
      concatenatedKeys.append(weightedDouble.getItem());
      sum += weightedDouble.getWeight();
    }
    assertEquals("abc", concatenatedKeys.toString());
    assertEquals(6.66, sum, 1e-8);
  }

  @Override
  public DoubleMap<String> makeTrivialObject() {
    return emptyDoubleMap();
  }

  @Override
  public DoubleMap<String> makeNontrivialObject() {
    return doubleMap(rbMapOf(
        "a", 1.11,
        "b", 2.22));
  }

  @Override
  public DoubleMap<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return doubleMap(rbMapOf(
        "b", 2.22 + e,
        "a", 1.11 + e));
  }

  @Override
  protected boolean willMatch(DoubleMap<String> expected, DoubleMap<String> actual) {
    return doubleMapMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<DoubleMap<T>> doubleMapMatcher(DoubleMap<T> expected) {
    return epsilonDoubleMapMatcher(expected, DEFAULT_EPSILON_1e_8);
  }

  public static <T> TypeSafeMatcher<DoubleMap<T>> epsilonDoubleMapMatcher(DoubleMap<T> expected, Epsilon epsilon) {
    return makeMatcher(expected, actual ->
        rbMapDoubleMatcher(expected.getRawMap(), epsilon).matches(actual.getRawMap()));
  }

}
