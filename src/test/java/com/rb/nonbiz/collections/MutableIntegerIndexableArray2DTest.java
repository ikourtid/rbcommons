package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.collections.ArrayIndexMappingTest.arrayIndexMappingMatcher;
import static com.rb.nonbiz.collections.MutableIntegerIndexableArray2D.mutableIntegerIndexableArray2D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.intArray2DMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;

/**
 * The test is generic, but the publicly exposed matcher (which gets used elsewhere) isn't, so that's good.
 */
public class MutableIntegerIndexableArray2DTest extends RBTestMatcher<MutableIntegerIndexableArray2D<String, Boolean>> {

  public static <R, C> MutableIntegerIndexableArray2D<R, C> emptyIntegerIndexableArray2D() {
    return mutableIntegerIndexableArray2D(
        new int[][] { {} },
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping());
  }

  @Override
  public MutableIntegerIndexableArray2D<String, Boolean> makeTrivialObject() {
    return emptyIntegerIndexableArray2D();
  }

  @Override
  public MutableIntegerIndexableArray2D<String, Boolean> makeNontrivialObject() {
    return mutableIntegerIndexableArray2D(
        new int[][] {
            { 11, 22 },
            { 33, 44 },
            { 55, 66 }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  public MutableIntegerIndexableArray2D<String, Boolean> makeMatchingNontrivialObject() {
    return mutableIntegerIndexableArray2D(
        new int[][] {
            { 11, 22 },
            { 33, 44 },
            { 55, 66 }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  protected boolean willMatch(MutableIntegerIndexableArray2D<String, Boolean> expected,
                              MutableIntegerIndexableArray2D<String, Boolean> actual) {
    return mutableIntegerIndexableArray2DMatcher(expected).matches(actual);
  }

  public static <R, C> TypeSafeMatcher<MutableIntegerIndexableArray2D<R, C>> mutableIntegerIndexableArray2DMatcher(
      MutableIntegerIndexableArray2D<R, C> expected) {
    return makeMatcher(expected,
        match(v -> v.getRawArrayUnsafe(), f -> intArray2DMatcher(f)),
        match(v -> v.getRowMapping(),     f -> arrayIndexMappingMatcher(f, f2 -> typeSafeEqualTo(f2))),
        match(v -> v.getColumnMapping(),  f -> arrayIndexMappingMatcher(f, f2 -> typeSafeEqualTo(f2))));
  }

}
