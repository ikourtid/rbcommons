package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.functional.QuadriConsumer;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.ArrayIndexMappingTest.arrayIndexMappingMatcher;
import static com.rb.nonbiz.collections.MutableIndexableArray3D.mutableIndexableArray3D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.search.Filter.filter;
import static com.rb.nonbiz.search.Filter.noFilter;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.array3DMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.doubleListMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_BOOLEAN;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * The test is generic, but the publicly exposed matcher (which gets used elsewhere) isn't, so that's good.
 */
public class MutableIndexableArray3DTest extends RBTestMatcher<MutableIndexableArray3D<String, Boolean, Integer, Double>> {

  @Test
  public void testArraySizePreconditions() {
    ArrayIndexMapping<String>  goodXMapping = simpleArrayIndexMapping("a", "b", "c");
    ArrayIndexMapping<Boolean> goodYMapping = simpleArrayIndexMapping(false, true);
    ArrayIndexMapping<Integer> goodZMapping = simpleArrayIndexMapping(10, 11);
    TriFunction<
        ArrayIndexMapping<String>,
        ArrayIndexMapping<Boolean>,
        ArrayIndexMapping<Integer>,
        MutableIndexableArray3D<String, Boolean, Integer, Double>> maker =
        (xMapping, yMapping, zMapping) -> mutableIndexableArray3D(
            new Double[][][] {
                { { 111.0, 112.0 }, { 121.0, 122.0 } },
                { { 211.0, 212.0 }, { 221.0, 222.0 } },
                { { 311.0, 312.0 }, { 321.0, 322.0 } }
            },
            xMapping,
            yMapping,
            zMapping);

    MutableIndexableArray3D<String, Boolean, Integer, Double> doesNotThrow = maker.apply(goodXMapping, goodYMapping, goodZMapping);
    RBSet.<ArrayIndexMapping<String>>rbSetOf(
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping("v1"),
        simpleArrayIndexMapping("v1", "v2"),
        simpleArrayIndexMapping("v1", "v2", "v3", "v4"))
        .forEach(badXMapping ->
            assertIllegalArgumentException( () -> maker.apply(badXMapping, goodYMapping, goodZMapping)));

    RBSet.<ArrayIndexMapping<Boolean>>rbSetOf(
        simpleArrayIndexMapping(),
        // unfortunately, with boolean, we don't have 3 unique values to test the case of a large mapping
        simpleArrayIndexMapping(DUMMY_BOOLEAN))
        .forEach(badYMapping ->
            assertIllegalArgumentException( () -> maker.apply(goodXMapping, badYMapping, goodZMapping)));

    RBSet.<ArrayIndexMapping<Integer>>rbSetOf(
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping(10),
        simpleArrayIndexMapping(10, 11, 12))
        .forEach(badZMapping ->
            assertIllegalArgumentException( () -> maker.apply(goodXMapping, goodYMapping, badZMapping)));
  }

  @Test
  public void testToTransformedStreamInXYZOrder() {
    assertEquals(
        ImmutableList.of(
            "a_false_10_111.0", "a_false_11_112.0", "a_true_10_121.0", "a_true_11_122.0",
            "b_false_10_211.0", "b_false_11_212.0", "b_true_10_221.0", "b_true_11_222.0",
            "c_false_10_311.0", "c_false_11_312.0", "c_true_10_321.0", "c_true_11_322.0"),
        mutableIndexableArray3D(
            new Double[][][] {
                { { 111.0, 112.0 }, { 121.0, 122.0 } },
                { { 211.0, 212.0 }, { 221.0, 222.0 } },
                { { 311.0, 312.0 }, { 321.0, 322.0 } }
            },
            simpleArrayIndexMapping("a", "b", "c"),
            simpleArrayIndexMapping(false, true),
            simpleArrayIndexMapping(10, 11))
            .toTransformedStreamInXYZOrder( (x, y, z, v) -> Strings.format("%s_%s_%s_%s", x, y, z, v))
            .collect(Collectors.toList()));
  }

  @Test
  public void testFilteredValuesStreamXYZ() {
    MutableIndexableArray3D<String, Boolean, Integer, Double> original = mutableIndexableArray3D(
        new Double[][][] {
            // false/10 false/11 true/10 true/11
            { { 111.0, 112.0 }, { 121.0, 122.0 } }, // "a"
            { { 211.0, 212.0 }, { 221.0, 222.0 } }, // "b"
            { { 311.0, 312.0 }, { 321.0, 322.0 } }  // "c"
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true),
        simpleArrayIndexMapping(10, 11));
    QuadriConsumer<String, Boolean, Integer, List<Double>> asserter =
        (xFilter, yFilter, zFilter, expectedResult) ->
            assertThat(
                original.filteredValuesStreamXYZ(
                    xFilter == null ? noFilter() : filter(xFilter),
                    yFilter == null ? noFilter() : filter(yFilter),
                    zFilter == null ? noFilter() : filter(zFilter))
                    .collect(Collectors.toList()),
                doubleListMatcher(expectedResult, DEFAULT_EPSILON_1e_8));
    // Note that the 'expected results' lists appear in a way that resembles the original grid,
    // so that it's easy to see visually what gets included.

    // no filters => returns all items
    asserter.accept(null, null, null, ImmutableList.of(
        111.0, 112.0, 121.0, 122.0,
        211.0, 212.0, 221.0, 222.0,
        311.0, 312.0, 321.0, 322.0));
    // filter single X
    asserter.accept("a", null, null, ImmutableList.of(
        111.0, 112.0, 121.0, 122.0));
    asserter.accept("b", null, null, ImmutableList.of(
        211.0, 212.0, 221.0, 222.0));
    asserter.accept("c", null, null, ImmutableList.of(
        311.0, 312.0, 321.0, 322.0));
    // filter single Y
    asserter.accept(null, FALSE, null, ImmutableList.of(
        111.0, 112.0,
        211.0, 212.0,
        311.0, 312.0));
    asserter.accept(null, TRUE, null, ImmutableList.of(
        121.0, 122.0,
        221.0, 222.0,
        321.0, 322.0));
    // filter single Z
    asserter.accept(null, null, 10, ImmutableList.of(
        111.0,        121.0,
        211.0,        221.0,
        311.0,        321.0));
    asserter.accept(null, null, 11, ImmutableList.of(
        112.0,        122.0,
        212.0,        222.0,
        312.0,        322.0));

    // filter X and Y
    asserter.accept("a", FALSE, null, ImmutableList.of(
        111.0, 112.0));
    asserter.accept("a", TRUE, null, ImmutableList.of(
        121.0, 122.0));
    asserter.accept("b", FALSE, null, ImmutableList.of(
        211.0, 212.0));
    asserter.accept("b", TRUE, null, ImmutableList.of(
        221.0, 222.0));
    asserter.accept("c", FALSE, null, ImmutableList.of(
        311.0, 312.0));
    asserter.accept("c", TRUE, null, ImmutableList.of(
        321.0, 322.0));

    // filter X and Z
    asserter.accept("a", null, 10, ImmutableList.of(
        111.0, 121.0));
    asserter.accept("a", null, 11, ImmutableList.of(
        112.0, 122.0));
    asserter.accept("b", null, 10, ImmutableList.of(
        211.0, 221.0));
    asserter.accept("b", null, 11, ImmutableList.of(
        212.0, 222.0));
    asserter.accept("c", null, 10, ImmutableList.of(
        311.0, 321.0));
    asserter.accept("c", null, 11, ImmutableList.of(
        312.0, 322.0));

    // filter Y and Z
    asserter.accept(null, FALSE, 10, ImmutableList.of(
        111.0,
        211.0,
        311.0));
    asserter.accept(null, FALSE, 11, ImmutableList.of(
        112.0,
        212.0,
        312.0));
    asserter.accept(null, TRUE, 10, ImmutableList.of(
        121.0,
        221.0,
        321.0));
    asserter.accept(null, TRUE, 11, ImmutableList.of(
        122.0,
        222.0,
        322.0));

    // fixing everything
    asserter.accept("a", FALSE, 10, singletonList(111.0));
    asserter.accept("a", FALSE, 11, singletonList(112.0));
    asserter.accept("a", TRUE,  10, singletonList(121.0));
    asserter.accept("a", TRUE,  11, singletonList(122.0));
    asserter.accept("b", FALSE, 10, singletonList(211.0));
    asserter.accept("b", FALSE, 11, singletonList(212.0));
    asserter.accept("b", TRUE,  10, singletonList(221.0));
    asserter.accept("b", TRUE,  11, singletonList(222.0));
    asserter.accept("c", FALSE, 10, singletonList(311.0));
    asserter.accept("c", FALSE, 11, singletonList(312.0));
    asserter.accept("c", TRUE,  10, singletonList(321.0));
    asserter.accept("c", TRUE,  11, singletonList(322.0));

    // Specifying invalid x
    assertIllegalArgumentException( () -> original.filteredValuesStreamXYZ(filter("d"), noFilter(),    noFilter()));
    assertIllegalArgumentException( () -> original.filteredValuesStreamXYZ(filter("d"), noFilter(),    filter(10)));
    assertIllegalArgumentException( () -> original.filteredValuesStreamXYZ(filter("d"), filter(FALSE), noFilter()));
    assertIllegalArgumentException( () -> original.filteredValuesStreamXYZ(filter("d"), filter(FALSE), filter(10)));

    // We can't specify an invalid y in this test because boolean has only 2 values. Oh well...
    // Specifying invalid y
    assertIllegalArgumentException( () -> original.filteredValuesStreamXYZ(noFilter(),  noFilter(),    filter(777)));
    assertIllegalArgumentException( () -> original.filteredValuesStreamXYZ(noFilter(),  filter(FALSE), filter(777)));
    assertIllegalArgumentException( () -> original.filteredValuesStreamXYZ(filter("a"), noFilter(),    filter(777)));
    assertIllegalArgumentException( () -> original.filteredValuesStreamXYZ(filter("a"), filter(FALSE), filter(777)));
  }

  @Override
  public MutableIndexableArray3D<String, Boolean, Integer, Double> makeTrivialObject() {
    return mutableIndexableArray3D(
        new Double[][][] { },
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping());
  }

  @Override
  public MutableIndexableArray3D<String, Boolean, Integer, Double> makeNontrivialObject() {
    return mutableIndexableArray3D(
        new Double[][][] {
            { { 111.0, 112.0 }, { 121.0, 122.0 } },
            { { 211.0, 212.0 }, { 221.0, 222.0 } },
            { { 311.0, 312.0 }, { 321.0, 322.0 } }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true),
        simpleArrayIndexMapping(10, 11));
  }

  @Override
  public MutableIndexableArray3D<String, Boolean, Integer, Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return mutableIndexableArray3D(
        new Double[][][] {
            { { 111 + e, 112 + e }, { 121 + e, 122 + e } },
            { { 211 + e, 212 + e }, { 221 + e, 222 + e } },
            { { 311 + e, 312 + e }, { 321 + e, 322 + e } }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true),
        simpleArrayIndexMapping(10, 11));
  }

  @Override
  protected boolean willMatch(MutableIndexableArray3D<String, Boolean, Integer, Double> expected,
                              MutableIndexableArray3D<String, Boolean, Integer, Double> actual) {
    return mutableIndexableArray3DMatcher(expected, v -> doubleAlmostEqualsMatcher(v, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <X, Y, Z, V> TypeSafeMatcher<MutableIndexableArray3D<X, Y, Z, V>> mutableIndexableArray3DMatcher(
      MutableIndexableArray3D<X, Y, Z, V> expected, MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getRawArrayUnsafe(), f -> array3DMatcher(f, valueMatcherGenerator)),
        match(v -> v.getXMapping(),       f -> arrayIndexMappingMatcher(f, f2 -> typeSafeEqualTo(f2))),
        match(v -> v.getYMapping(),       f -> arrayIndexMappingMatcher(f, f2 -> typeSafeEqualTo(f2))),
        match(v -> v.getZMapping(),       f -> arrayIndexMappingMatcher(f, f2 -> typeSafeEqualTo(f2))));
  }

}
