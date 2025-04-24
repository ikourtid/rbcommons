package com.rb.nonbiz.collections;

import com.rb.biz.types.collections.ts.TestHasIidSet;
import com.rb.nonbiz.testmatchers.RBMatchers;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBValueMatchers;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Epsilon;
import org.junit.Test;

import static com.rb.nonbiz.collections.IidGroupingsTest.testIidGroupingsMatcher;
import static com.rb.nonbiz.collections.IidMapWithGroupings.emptyIidMapWithGroupings;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class IidMapWithGroupingsTest extends RBTestMatcher<IidMapWithGroupings<Double, TestHasIidSet>> {

  @Test
  public void reminder() {
    fail("FIXME IAK");
  }

  @Override
  public IidMapWithGroupings<Double, TestHasIidSet> makeTrivialObject() {
    return emptyIidMapWithGroupings();
  }

  @Override
  public IidMapWithGroupings<Double, TestHasIidSet> makeNontrivialObject() {
    return null;
  }

  @Override
  public IidMapWithGroupings<Double, TestHasIidSet> makeMatchingNontrivialObject() {
    return null;
  }

  @Override
  protected boolean willMatch(IidMapWithGroupings<Double, TestHasIidSet> expected,
                              IidMapWithGroupings<Double, TestHasIidSet> actual) {
    return iidMapWithGroupingsMatcher(expected, f -> preciseValueMatcher(f, DEFAULT_EPSILON_1e_8));
  }

  public static <V, S extends HasIidSet> TypeSafeMatcher<IidMapWithGroupings<V, S>> iidMapWithGroupingsMatcher(
      IidMapWithGroupings<V, S> expected,
      MatcherGenerator<V> iidMapValueMatcherGenerator,
      MatcherGenerator<S> hasIidSetMatcherGenerator) {
    return makeMatcher(expected,
        matchIidMap(v -> v.getIidMap(), iidMapValueMatcherGenerator),
        match(      v -> v.getIidGroupings(), f -> testIidGroupingsMatcher(f, hasIidSetMatcherGenerator)));
  }

}
