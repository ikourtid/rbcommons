package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Iterator;
import java.util.function.DoubleFunction;

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
import static com.rb.nonbiz.types.Epsilon.epsilon;
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
    Epsilon e = epsilon(1e-9);
    ImmutableDoubleIndexableArray2D<String, Boolean> array = immutableDoubleIndexableArray2D(
        new double[][] {
            { 1.1, 2.2 },
            { 3.3, 4.4 },
            { 5.5, 6.6 }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
    Iterator<Double> iter = array.singleRowIterator("b");
    assertEquals(3.3, iter.next(), e.doubleValue());
    assertEquals(4.4, iter.next(), e.doubleValue());
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

  @Test
  public void test_isSquareWithRowKeysSameAsColumnKeys() {
    assertTrue(
        immutableDoubleIndexableArray2D(
            new double[][] {
                { 1.1, 2.2 },
                { 3.3, 4.4 }
            },
            simpleArrayIndexMapping("a", "b"),
            simpleArrayIndexMapping("a", "b"))
            .isSquareWithRowKeysSameAsColumnKeys());
    assertFalse(
        "column keys are same as row keys, but different ordering",
        immutableDoubleIndexableArray2D(
            new double[][] {
                { 1.1, 2.2 },
                { 3.3, 4.4 }
            },
            simpleArrayIndexMapping("a", "b"),
            simpleArrayIndexMapping("b", "a"))
            .isSquareWithRowKeysSameAsColumnKeys());
    assertFalse(
        "column keys are different than row keys",
        immutableDoubleIndexableArray2D(
            new double[][] {
                { 1.1, 2.2 },
                { 3.3, 4.4 }
            },
            simpleArrayIndexMapping("a", "b"),
            simpleArrayIndexMapping("a", "c"))
            .isSquareWithRowKeysSameAsColumnKeys());
    assertFalse(
        "column keys are of a different type than row keys",
        immutableDoubleIndexableArray2D(
            new double[][] {
                { 1.1, 2.2 },
                { 3.3, 4.4 }
            },
            simpleArrayIndexMapping("a", "b"),
            simpleArrayIndexMapping(77, 88))
            .isSquareWithRowKeysSameAsColumnKeys());
    assertFalse(
        "not a square array",
        immutableDoubleIndexableArray2D(
            new double[][] {
                { 1.1, 2.2 },
                { 3.3, 4.4 },
                { 5.5, 6.6 }
            },
            simpleArrayIndexMapping("a", "b", "c"),
            simpleArrayIndexMapping("a", "b"))
            .isSquareWithRowKeysSameAsColumnKeys());
  }

  @Test
  public void test_isLogicallyAndPhysicallySymmetric_varyEpsilon() {
    DoubleFunction<Boolean> maker = epsilon ->
        immutableDoubleIndexableArray2D(
            new double[][] {
                { 1.1, 2.2, 3.3 + epsilon },
                { 2.2, 4.4, 5.5 + epsilon },
                { 3.3, 5.5, 6.6 + epsilon } // this epsilon in the right column creates asymmetry
            },
            simpleArrayIndexMapping("a", "b", "c"),
            simpleArrayIndexMapping("a", "b", "c"))
            .isLogicallyAndPhysicallySymmetric(1e-8);

    assertTrue(
        "exactly equal",
        maker.apply(0));

    assertTrue(
        "off by a tiny epsilon; still symmetric",
        maker.apply(1e-9));

    assertFalse(
        "off by an amount more than epsilon; not symmetric",
        maker.apply(1e-7));
  }

  @Test
  public void dataAreLogicallyButNotPhysicallySymmetric_returnsFalse() {
    assertTrue(
        immutableDoubleIndexableArray2D(
            new double[][] {
                { 1.1, 2.2, 3.3 },
                { 2.2, 4.4, 5.5 },
                { 3.3, 5.5, 6.6 }
            },
            simpleArrayIndexMapping("a", "b", "c"),
            simpleArrayIndexMapping("a", "b", "c"))
            .isLogicallyAndPhysicallySymmetric(1e-8));

    // The 2nd row was moved to the top, and the row keys were also changed to b, a, c to match that move.
    ImmutableDoubleIndexableArray2D<String, String> logicallySymmetric = immutableDoubleIndexableArray2D(
        new double[][] {
            { 2.2, 4.4, 5.5 },
            { 1.1, 2.2, 3.3 },
            { 3.3, 5.5, 6.6 }
        },
        simpleArrayIndexMapping("b", "a", "c"),
        simpleArrayIndexMapping("a", "b", "c"));
    assertFalse(logicallySymmetric.isLogicallyAndPhysicallySymmetric(1e-8));
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
