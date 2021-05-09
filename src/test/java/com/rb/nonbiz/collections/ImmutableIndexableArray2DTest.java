package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.math.optimization.general.ConstraintDirection;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Iterator;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.ImmutableIndexableArray2D.immutableIndexableArray2D;
import static com.rb.nonbiz.collections.MutableIndexableArray2DTest.mutableIndexableArray2DMatcher;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.EQUAL_TO_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.GREATER_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.LESS_THAN_SCALAR;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.floatAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * The test is generic, but the publicly exposed matcher (which gets used elsewhere) isn't, so that's good.
 */
public class ImmutableIndexableArray2DTest extends RBTestMatcher<ImmutableIndexableArray2D<String, Boolean, Float>> {

  @Test
  public void iteratorsFailAtCreationIfItemIsInvalid() {
    ImmutableIndexableArray2D<String, Boolean, Float> array = immutableIndexableArray2D(
        new Float[][] { { 1.1f } },
        simpleArrayIndexMapping("a"),
        simpleArrayIndexMapping(false));
    Iterator<Float> doesNotThrow;
    doesNotThrow = array.singleRowIterator("a");
    assertIllegalArgumentException( () -> array.singleRowIterator("b"));
    doesNotThrow = array.singleColumnIterator(false);
    assertIllegalArgumentException( () -> array.singleColumnIterator(true));
  }

  @Test
  public void testSingleRowIterator() {
    ImmutableIndexableArray2D<String, Boolean, ConstraintDirection> array = immutableIndexableArray2D(
        new ConstraintDirection[][] {
            //  false              true
            { EQUAL_TO_SCALAR,     GREATER_THAN_SCALAR }, // a
            { GREATER_THAN_SCALAR, LESS_THAN_SCALAR    }, // b
            { LESS_THAN_SCALAR,    EQUAL_TO_SCALAR     }  // c
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
    Iterator<ConstraintDirection> iter = array.singleRowIterator("b");
    assertEquals(GREATER_THAN_SCALAR, iter.next());
    assertEquals(LESS_THAN_SCALAR, iter.next());
    assertFalse(iter.hasNext());
    assertEquals(
        newArrayList(array.singleRowIterator("a")),
        ImmutableList.of(EQUAL_TO_SCALAR, GREATER_THAN_SCALAR));
    assertEquals(
        newArrayList(array.singleRowIterator("c")),
        ImmutableList.of(LESS_THAN_SCALAR, EQUAL_TO_SCALAR));
  }

  @Test
  public void testSingleColumnIterator() {
    ImmutableIndexableArray2D<String, Boolean, ConstraintDirection> array = immutableIndexableArray2D(
        new ConstraintDirection[][] {
            //  false              true
            { EQUAL_TO_SCALAR,     GREATER_THAN_SCALAR }, // a
            { GREATER_THAN_SCALAR, LESS_THAN_SCALAR    }, // b
            { LESS_THAN_SCALAR,    EQUAL_TO_SCALAR     }  // c
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
    Iterator<ConstraintDirection> iter = array.singleColumnIterator(false);
    assertEquals(EQUAL_TO_SCALAR, iter.next());
    assertEquals(GREATER_THAN_SCALAR, iter.next());
    assertEquals(LESS_THAN_SCALAR, iter.next());
    assertFalse(iter.hasNext());

    iter = array.singleColumnIterator(true);
    assertEquals(GREATER_THAN_SCALAR, iter.next());
    assertEquals(LESS_THAN_SCALAR, iter.next());
    assertEquals(EQUAL_TO_SCALAR, iter.next());
    assertFalse(iter.hasNext());
  }

  @Override
  public ImmutableIndexableArray2D<String, Boolean, Float> makeTrivialObject() {
    return immutableIndexableArray2D(
        new Float[][] { },
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping());
  }

  @Override
  public ImmutableIndexableArray2D<String, Boolean, Float> makeNontrivialObject() {
    return immutableIndexableArray2D(
        new Float[][] {
            { 1.1f, 2.2f },
            { 3.3f, 4.4f },
            { 5.5f, 6.6f }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  public ImmutableIndexableArray2D<String, Boolean, Float> makeMatchingNontrivialObject() {
    float e = 1e-9f; // epsilon
    return immutableIndexableArray2D(
        new Float[][] {
            { 1.1f + e, 2.2f + e },
            { 3.3f + e, 4.4f + e },
            { 5.5f + e, 6.6f + e }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  protected boolean willMatch(ImmutableIndexableArray2D<String, Boolean, Float> expected,
                              ImmutableIndexableArray2D<String, Boolean, Float> actual) {
    return immutableIndexableArray2DMatcher(expected, v -> floatAlmostEqualsMatcher(v, 1e-8f)).matches(actual);
  }

  public static <R, C, V> TypeSafeMatcher<ImmutableIndexableArray2D<R, C, V>> immutableIndexableArray2DEqualityMatcher(
      ImmutableIndexableArray2D<R, C, V> expected) {
    return immutableIndexableArray2DMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <R, C, V> TypeSafeMatcher<ImmutableIndexableArray2D<R, C, V>> immutableIndexableArray2DMatcher(
      ImmutableIndexableArray2D<R, C, V> expected, MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getMutableArray2D(), f -> mutableIndexableArray2DMatcher(f, valueMatcherGenerator)));
  }

}
