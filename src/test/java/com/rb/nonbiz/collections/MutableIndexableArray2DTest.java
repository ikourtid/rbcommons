package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Iterator;
import java.util.function.BiFunction;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.ArrayIndexMappingTest.arrayIndexMappingMatcher;
import static com.rb.nonbiz.collections.MutableIndexableArray2D.mutableIndexableArray2D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
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
            ArrayIndexMapping<TestEnumXYZ>,
        MutableIndexableArray2D<String, TestEnumXYZ, Boolean>> maker = (rowMapping, columnMapping) -> mutableIndexableArray2D(
        new Boolean[][] {
            { true, false },
            { false, true },
            { false, true }
        },
        rowMapping,
        columnMapping);
    SimpleArrayIndexMapping<String> goodRowMapping = simpleArrayIndexMapping("a", "b", "c");
    SimpleArrayIndexMapping<TestEnumXYZ> goodColumnMapping = simpleArrayIndexMapping(TestEnumXYZ.Z, TestEnumXYZ.X);
    MutableIndexableArray2D<String, TestEnumXYZ, Boolean> doesNotThrow = maker.apply(goodRowMapping, goodColumnMapping);
    assertIllegalArgumentException( () -> maker.apply(goodRowMapping, simpleArrayIndexMapping()));
    assertIllegalArgumentException( () -> maker.apply(goodRowMapping, simpleArrayIndexMapping(TestEnumXYZ.Z)));
    assertIllegalArgumentException( () -> maker.apply(goodRowMapping,
        simpleArrayIndexMapping(TestEnumXYZ.Z, TestEnumXYZ.X, TestEnumXYZ.Y)));
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
    MutableIndexableArray2D<String, Boolean, TestEnumXYZ> array = mutableIndexableArray2D(
        new TestEnumXYZ[][] {
            //  false              true
            { TestEnumXYZ.X, TestEnumXYZ.Y }, // a
            { TestEnumXYZ.Y, TestEnumXYZ.Z    }, // b
            { TestEnumXYZ.Z, TestEnumXYZ.X     }  // c
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
    Iterator<TestEnumXYZ> iter = array.singleRowIterator("b");
    assertEquals(TestEnumXYZ.Y, iter.next());
    assertEquals(TestEnumXYZ.Z, iter.next());
    assertFalse(iter.hasNext());
    assertEquals(
        newArrayList(array.singleRowIterator("a")),
        ImmutableList.of(TestEnumXYZ.X, TestEnumXYZ.Y));
    assertEquals(
        newArrayList(array.singleRowIterator("c")),
        ImmutableList.of(TestEnumXYZ.Z, TestEnumXYZ.X));
  }

  @Test
  public void testSingleColumnIterator() {
    MutableIndexableArray2D<String, Boolean, TestEnumXYZ> array = mutableIndexableArray2D(
        new TestEnumXYZ[][] {
            //  false              true
            { TestEnumXYZ.X,     TestEnumXYZ.Y }, // a
            { TestEnumXYZ.Y, TestEnumXYZ.Z    }, // b
            { TestEnumXYZ.Z,    TestEnumXYZ.X     }  // c
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
    Iterator<TestEnumXYZ> iter = array.singleColumnIterator(false);
    assertEquals(TestEnumXYZ.X, iter.next());
    assertEquals(TestEnumXYZ.Y, iter.next());
    assertEquals(TestEnumXYZ.Z, iter.next());
    assertFalse(iter.hasNext());

    iter = array.singleColumnIterator(true);
    assertEquals(TestEnumXYZ.Y, iter.next());
    assertEquals(TestEnumXYZ.Z, iter.next());
    assertEquals(TestEnumXYZ.X, iter.next());
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
