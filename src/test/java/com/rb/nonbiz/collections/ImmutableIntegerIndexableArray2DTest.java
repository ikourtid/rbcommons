package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.collections.ImmutableIntegerIndexableArray2D.emptyImmutableIntegerIndexableArray2D;
import static com.rb.nonbiz.collections.ImmutableIntegerIndexableArray2D.immutableIntegerIndexableArray2D;
import static com.rb.nonbiz.collections.MutableIntegerIndexableArray2DTest.mutableIntegerIndexableArray2DMatcher;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

/**
 * The test is generic, but the publicly exposed matcher (which gets used elsewhere) isn't, so that's good.
 */
public class ImmutableIntegerIndexableArray2DTest extends RBTestMatcher<ImmutableIntegerIndexableArray2D<String, Boolean>> {

  @Override
  public ImmutableIntegerIndexableArray2D<String, Boolean> makeTrivialObject() {
    return emptyImmutableIntegerIndexableArray2D();
  }

  @Override
  public ImmutableIntegerIndexableArray2D<String, Boolean> makeNontrivialObject() {
    return immutableIntegerIndexableArray2D(
        new int[][] {
            { 11, 22 },
            { 33, 44 },
            { 55, 66 }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  public ImmutableIntegerIndexableArray2D<String, Boolean> makeMatchingNontrivialObject() {
    return immutableIntegerIndexableArray2D(
        new int[][] {
            { 11, 22 },
            { 33, 44 },
            { 55, 66 }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  protected boolean willMatch(ImmutableIntegerIndexableArray2D<String, Boolean> expected,
                              ImmutableIntegerIndexableArray2D<String, Boolean> actual) {
    return immutableIntegerIndexableArray2DMatcher(expected).matches(actual);
  }

  public static <R, C> TypeSafeMatcher<ImmutableIntegerIndexableArray2D<R, C>> immutableIntegerIndexableArray2DMatcher(
      ImmutableIntegerIndexableArray2D<R, C> expected) {
    return makeMatcher(expected,
        match(v -> v.getMutableArray2D(), f -> mutableIntegerIndexableArray2DMatcher(f)));
  }

}
