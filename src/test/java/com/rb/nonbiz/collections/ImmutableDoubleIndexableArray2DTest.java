package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Iterator;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2D.emptyImmutableDoubleIndexableArray2D;
import static com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2D.immutableDoubleIndexableArray2D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray2D.immutableIndexableArray2D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray2DTest.immutableIndexableArray2DMatcher;
import static com.rb.nonbiz.collections.MutableDoubleIndexableArray2DTest.mutableDoubleIndexableArray2DMatcher;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.doubleListMatcher;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_BOOLEAN;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The test is generic, but the publicly exposed matcher (which gets used elsewhere) isn't, so that's good.
 */
public class ImmutableDoubleIndexableArray2DTest extends RBTestMatcher<ImmutableDoubleIndexableArray2D<String, Boolean>> {

  @Test
  public void iteratorsFailAtCreationIfItemIsInvalid() {
    ImmutableDoubleIndexableArray2D<String, Boolean> array = immutableDoubleIndexableArray2D(
        new double[][] { { 1.1 } },
        simpleArrayIndexMapping("a"),
        simpleArrayIndexMapping(false));
    Iterator<Double> doesNotThrow;
    doesNotThrow = array.singleRowIterator("a");
    assertIllegalArgumentException( () -> array.singleRowIterator("b"));
    doesNotThrow = array.singleColumnIterator(false);
    assertIllegalArgumentException( () -> array.singleColumnIterator(true));
  }

  @Test
  public void testSingleRowIterator() {
    double e = 1e-9; // epsilon
    ImmutableDoubleIndexableArray2D<String, Boolean> array = immutableDoubleIndexableArray2D(
        new double[][] {
            { 1.1, 2.2 },
            { 3.3, 4.4 },
            { 5.5, 6.6 }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
    Iterator<Double> iter = array.singleRowIterator("b");
    assertEquals(3.3, iter.next(), e);
    assertEquals(4.4, iter.next(), e);
    assertFalse(iter.hasNext());
    assertThat(
        newArrayList(array.singleRowIterator("a")),
        doubleListMatcher(ImmutableList.of(1.1, 2.2), e));

    assertThat(
        array.singleRowIterator("a"),
        iteratorMatcher(
            ImmutableList.of(1.1, 2.2).iterator(),
            f -> doubleAlmostEqualsMatcher(f, e)));
    assertThat(
        array.singleRowIterator("c"),
        iteratorMatcher(
            ImmutableList.of(5.5, 6.6).iterator(),
            f -> doubleAlmostEqualsMatcher(f, e)));
  }

  @Test
  public void testSingleColumnIterator() {
    double e = 1e-9; // epsilon
    ImmutableDoubleIndexableArray2D<String, Boolean> array = immutableDoubleIndexableArray2D(
        new double[][] {
            { 1.1, 2.2 },
            { 3.3, 4.4 },
            { 5.5, 6.6 }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
    Iterator<Double> iter = array.singleColumnIterator(true);
    assertEquals(2.2, iter.next(), e);
    assertEquals(4.4, iter.next(), e);
    assertEquals(6.6, iter.next(), e);
    assertFalse(iter.hasNext());

    iter = array.singleColumnIterator(false);
    assertEquals(1.1, iter.next(), e);
    assertEquals(3.3, iter.next(), e);
    assertEquals(5.5, iter.next(), e);
    assertFalse(iter.hasNext());
  }

  @Test
  public void testTransform() {
    assertThat(
        immutableDoubleIndexableArray2D(
            new double[][] {
                { 1.1, 2.2 },
                { 3.3, 4.4 },
                { 5.5, 6.6 }
            },
            simpleArrayIndexMapping("a", "b", "c"),
            simpleArrayIndexMapping(false, true))
        .transform( (rowKey, columnKey, value) -> Strings.format("%s_%s_%s",
            rowKey, columnKey, value)),
        immutableIndexableArray2DMatcher(
            immutableIndexableArray2D(
                new String[][] {
                    { "a_false_1.1", "a_true_2.2" },
                    { "b_false_3.3", "b_true_4.4" },
                    { "c_false_5.5", "c_true_6.6" }
                },
                simpleArrayIndexMapping("a", "b", "c"),
                simpleArrayIndexMapping(false, true)),
            f -> typeSafeEqualTo(f)));
  }

  @Test
  public void testIsSquare() {
    assertFalse(
        immutableDoubleIndexableArray2D(
            new double[][] {
                { 1.1, 2.2 },
                { 3.3, 4.4 },
                { 5.5, 6.6 }
            },
            simpleArrayIndexMapping("a", "b", "c"),
            simpleArrayIndexMapping(false, true))
            .isSquare());
    assertTrue(
        immutableDoubleIndexableArray2D(
            new double[][] {
                { 1.1, 2.2 },
                { 3.3, 4.4 }
            },
            simpleArrayIndexMapping("a", "b"),
            simpleArrayIndexMapping(false, true))
            .isSquare());
    assertTrue(
        immutableDoubleIndexableArray2D(
            new double[][] {
                { DUMMY_DOUBLE }
            },
            simpleArrayIndexMapping(DUMMY_STRING),
            simpleArrayIndexMapping(DUMMY_BOOLEAN))
            .isSquare());
  }

  @Override
  public ImmutableDoubleIndexableArray2D<String, Boolean> makeTrivialObject() {
    return emptyImmutableDoubleIndexableArray2D();
  }

  @Override
  public ImmutableDoubleIndexableArray2D<String, Boolean> makeNontrivialObject() {
    return immutableDoubleIndexableArray2D(
        new double[][] {
            { 1.1, 2.2 },
            { 3.3, 4.4 },
            { 5.5, 6.6 }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  public ImmutableDoubleIndexableArray2D<String, Boolean> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return immutableDoubleIndexableArray2D(
        new double[][] {
            { 1.1 + e, 2.2 + e },
            { 3.3 + e, 4.4 + e },
            { 5.5 + e, 6.6 + e }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  protected boolean willMatch(ImmutableDoubleIndexableArray2D<String, Boolean> expected,
                              ImmutableDoubleIndexableArray2D<String, Boolean> actual) {
    return immutableDoubleIndexableArray2DMatcher(expected).matches(actual);
  }

  public static <R, C> TypeSafeMatcher<ImmutableDoubleIndexableArray2D<R, C>> immutableDoubleIndexableArray2DMatcher(
      ImmutableDoubleIndexableArray2D<R, C> expected) {
    return immutableDoubleIndexableArray2DMatcher(expected, 1e-8);
  }

  public static <R, C> TypeSafeMatcher<ImmutableDoubleIndexableArray2D<R, C>> immutableDoubleIndexableArray2DMatcher(
      ImmutableDoubleIndexableArray2D<R, C> expected, double epsilon) {
    return makeMatcher(expected,
        match(v -> v.getMutableArray2D(), f -> mutableDoubleIndexableArray2DMatcher(f, epsilon)));
  }

}
