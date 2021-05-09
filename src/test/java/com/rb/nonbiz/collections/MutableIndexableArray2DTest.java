package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.math.optimization.general.ConstraintDirection;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Iterator;
import java.util.function.BiFunction;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.ArrayIndexMappingTest.arrayIndexMappingMatcher;
import static com.rb.nonbiz.collections.MutableIndexableArray2D.mutableIndexableArray2D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.EQUAL_TO_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.GREATER_THAN_SCALAR;
import static com.rb.nonbiz.math.optimization.general.ConstraintDirection.LESS_THAN_SCALAR;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.array2DMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.floatAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * The test is generic, but the publicly exposed matcher (which gets used elsewhere) isn't, so that's good.
 */
public class MutableIndexableArray2DTest extends RBTestMatcher<MutableIndexableArray2D<String, Boolean, Float>> {

  @Test
  public void arraySizeMustMatchRowAndColumnMappingSizes() {
    BiFunction<
            ArrayIndexMapping<String>,
            ArrayIndexMapping<ConstraintDirection>,
        MutableIndexableArray2D<String, ConstraintDirection, Boolean>> maker = (rowMapping, columnMapping) -> mutableIndexableArray2D(
        new Boolean[][] {
            { true, false },
            { false, true },
            { false, true }
        },
        rowMapping,
        columnMapping);
    SimpleArrayIndexMapping<String> goodRowMapping = simpleArrayIndexMapping("a", "b", "c");
    SimpleArrayIndexMapping<ConstraintDirection> goodColumnMapping = simpleArrayIndexMapping(LESS_THAN_SCALAR, EQUAL_TO_SCALAR);
    MutableIndexableArray2D<String, ConstraintDirection, Boolean> doesNotThrow = maker.apply(goodRowMapping, goodColumnMapping);
    assertIllegalArgumentException( () -> maker.apply(goodRowMapping, simpleArrayIndexMapping()));
    assertIllegalArgumentException( () -> maker.apply(goodRowMapping, simpleArrayIndexMapping(LESS_THAN_SCALAR)));
    assertIllegalArgumentException( () -> maker.apply(goodRowMapping,
        simpleArrayIndexMapping(LESS_THAN_SCALAR, EQUAL_TO_SCALAR, GREATER_THAN_SCALAR)));
    assertIllegalArgumentException( () -> maker.apply(simpleArrayIndexMapping(), goodColumnMapping));
    assertIllegalArgumentException( () -> maker.apply(simpleArrayIndexMapping("a"), goodColumnMapping));
    assertIllegalArgumentException( () -> maker.apply(simpleArrayIndexMapping("a", "b"), goodColumnMapping));
    assertIllegalArgumentException( () -> maker.apply(simpleArrayIndexMapping("a", "b", "c", "d"), goodColumnMapping));
  }

  @Test
  public void iteratorsFailAtCreationIfItemIsInvalid() {
    MutableIndexableArray2D<String, Boolean, Float> array = mutableIndexableArray2D(
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
    MutableIndexableArray2D<String, Boolean, ConstraintDirection> array = mutableIndexableArray2D(
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
    MutableIndexableArray2D<String, Boolean, ConstraintDirection> array = mutableIndexableArray2D(
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
  public MutableIndexableArray2D<String, Boolean, Float> makeTrivialObject() {
    return mutableIndexableArray2D(
        new Float[][] { },
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping());
  }

  @Override
  public MutableIndexableArray2D<String, Boolean, Float> makeNontrivialObject() {
    return mutableIndexableArray2D(
        new Float[][] {
            { 1.1f, 2.2f },
            { 3.3f, 4.4f },
            { 5.5f, 6.6f }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  public MutableIndexableArray2D<String, Boolean, Float> makeMatchingNontrivialObject() {
    float e = 1e-9f; // epsilon
    return mutableIndexableArray2D(
        new Float[][] {
            { 1.1f + e, 2.2f + e },
            { 3.3f + e, 4.4f + e },
            { 5.5f + e, 6.6f + e }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  protected boolean willMatch(MutableIndexableArray2D<String, Boolean, Float> expected,
                              MutableIndexableArray2D<String, Boolean, Float> actual) {
    return mutableIndexableArray2DMatcher(expected, v -> floatAlmostEqualsMatcher(v, 1e-8f))
        .matches(actual);
  }

  public static <R, C, V> TypeSafeMatcher<MutableIndexableArray2D<R, C, V>> mutableIndexableArray2DMatcher(
      MutableIndexableArray2D<R, C, V> expected, MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getRawArray(),      f -> array2DMatcher(f, valueMatcherGenerator)),
        match(v -> v.getRowMapping(),    f -> arrayIndexMappingMatcher(f, f2 -> typeSafeEqualTo(f2))),
        match(v -> v.getColumnMapping(), f -> arrayIndexMappingMatcher(f, f2 -> typeSafeEqualTo(f2))));
  }

}
