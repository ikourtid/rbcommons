package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Iterator;
import java.util.function.BiFunction;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.ArrayIndexMappingTest.arrayIndexMappingMatcher;
import static com.rb.nonbiz.collections.MutableDoubleIndexableArray2D.mutableDoubleIndexableArray2D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.doubleArray2DMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.doubleListMatcher;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * The test is generic, but the publicly exposed matcher (which gets used elsewhere) isn't, so that's good.
 */
public class MutableDoubleIndexableArray2DTest extends RBTestMatcher<MutableDoubleIndexableArray2D<String, Boolean>> {

  public static <R, C> MutableDoubleIndexableArray2D<R, C> emptyDoubleIndexableArray2D() {
    return mutableDoubleIndexableArray2D(
        new double[][] { },
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping());
  }

  @Test
  public void arraySizeMustMatchRowAndColumnMappingSizes() {
    BiFunction<
            ArrayIndexMapping<String>,
            ArrayIndexMapping<TestEnumXYZ>,
            MutableDoubleIndexableArray2D<String, TestEnumXYZ>> maker = (rowMapping, columnMapping) -> mutableDoubleIndexableArray2D(
        new double[][] {
            { 1.1, 2.2 },
            { 3.3, 4.4 },
            { 5.5, 6.6 }
        },
        rowMapping,
        columnMapping);
    SimpleArrayIndexMapping<String> goodRowMapping = simpleArrayIndexMapping("a", "b", "c");
    SimpleArrayIndexMapping<TestEnumXYZ> goodColumnMapping = simpleArrayIndexMapping(TestEnumXYZ.X, TestEnumXYZ.Y);
    MutableDoubleIndexableArray2D<String, TestEnumXYZ> doesNotThrow = maker.apply(goodRowMapping, goodColumnMapping);
    assertIllegalArgumentException( () -> maker.apply(goodRowMapping, simpleArrayIndexMapping()));
    assertIllegalArgumentException( () -> maker.apply(goodRowMapping, simpleArrayIndexMapping(TestEnumXYZ.X)));
    assertIllegalArgumentException( () -> maker.apply(goodRowMapping,
        simpleArrayIndexMapping(TestEnumXYZ.X, TestEnumXYZ.Y, TestEnumXYZ.Z)));
    assertIllegalArgumentException( () -> maker.apply(simpleArrayIndexMapping(), goodColumnMapping));
    assertIllegalArgumentException( () -> maker.apply(simpleArrayIndexMapping("a"), goodColumnMapping));
    assertIllegalArgumentException( () -> maker.apply(simpleArrayIndexMapping("a", "b"), goodColumnMapping));
    assertIllegalArgumentException( () -> maker.apply(simpleArrayIndexMapping("a", "b", "c", "d"), goodColumnMapping));
  }

  @Test
  public void iteratorsFailAtCreationIfItemIsInvalid() {
    MutableDoubleIndexableArray2D<String, Boolean> array = mutableDoubleIndexableArray2D(
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
    MutableDoubleIndexableArray2D<String, Boolean> array = mutableDoubleIndexableArray2D(
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
    MutableDoubleIndexableArray2D<String, Boolean> array = mutableDoubleIndexableArray2D(
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

  @Override
  public MutableDoubleIndexableArray2D<String, Boolean> makeTrivialObject() {
    return emptyDoubleIndexableArray2D();
  }

  @Override
  public MutableDoubleIndexableArray2D<String, Boolean> makeNontrivialObject() {
    return mutableDoubleIndexableArray2D(
        new double[][] {
            { 1.1, 2.2 },
            { 3.3, 4.4 },
            { 5.5, 6.6 }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  public MutableDoubleIndexableArray2D<String, Boolean> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return mutableDoubleIndexableArray2D(
        new double[][] {
            { 1.1 + e, 2.2 + e },
            { 3.3 + e, 4.4 + e },
            { 5.5 + e, 6.6 + e }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  protected boolean willMatch(MutableDoubleIndexableArray2D<String, Boolean> expected,
                              MutableDoubleIndexableArray2D<String, Boolean> actual) {
    return mutableDoubleIndexableArray2DMatcher(expected).matches(actual);
  }

  public static <R, C> TypeSafeMatcher<MutableDoubleIndexableArray2D<R, C>> mutableDoubleIndexableArray2DMatcher(
      MutableDoubleIndexableArray2D<R, C> expected) {
    return mutableDoubleIndexableArray2DMatcher(expected, 1e-8);
  }

  public static <R, C> TypeSafeMatcher<MutableDoubleIndexableArray2D<R, C>> mutableDoubleIndexableArray2DMatcher(
      MutableDoubleIndexableArray2D<R, C> expected, double epsilon) {
    return makeMatcher(expected,
        match(v -> v.getRawArray(),      f -> doubleArray2DMatcher(f, epsilon)),
        match(v -> v.getRowMapping(),    f -> arrayIndexMappingMatcher(f, f2 -> typeSafeEqualTo(f2))),
        match(v -> v.getColumnMapping(), f -> arrayIndexMappingMatcher(f, f2 -> typeSafeEqualTo(f2))));
  }

}
