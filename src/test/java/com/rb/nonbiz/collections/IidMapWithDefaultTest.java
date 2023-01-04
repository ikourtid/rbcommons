package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A2;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapWithDefault.emptyIidMapWithDefault;
import static com.rb.nonbiz.collections.IidMapWithDefault.iidMapWithDefault;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;

// This test class is not generic, but the publicly exposed static matcher is.
public class IidMapWithDefaultTest extends RBTestMatcher<IidMapWithDefault<Double>> {

  @Test
  public void testTransformDefaultAndOverrides() {
    assertThat(
        iidMapWithDefault(7.7, iidMapOf(
            STOCK_A1, 1.1,
            STOCK_A2, 2.2))
            .transformDefaultAndOverrides(v -> Strings.format("_%s", v)),
        iidMapWithDefaultEqualityMatcher(
            iidMapWithDefault("_7.7", iidMapOf(
                STOCK_A1, "_1.1",
                STOCK_A2, "_2.2"))));
    assertThat(
        emptyIidMapWithDefault(7.7)
            .transformDefaultAndOverrides(v -> Strings.format("_%s", v)),
        iidMapWithDefaultEqualityMatcher(
            emptyIidMapWithDefault("_7.7")));
  }

  @Test
  public void testStreamOfDefaultValuePlusOverrides() {
    // Ordering is not guaranteed, so we will compare these as sets
    assertThat(
        newRBSet(iidMapWithDefault(7.7, iidMapOf(
            STOCK_A1, 1.1,
            STOCK_A2, 2.2)).streamOfDefaultValuePlusOverrides().iterator()),
        rbSetMatcher(
            rbSetOf(1.1, 2.2, 7.7),
            f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8),
            Double::compareTo));
    assertThat(
        newRBSet(emptyIidMapWithDefault(7.7).streamOfDefaultValuePlusOverrides().iterator()),
        rbSetMatcher(
            singletonRBSet(7.7),
            f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8),
            Double::compareTo));
  }

  @Test
  public void testCopyWithUpdatedDefaultValue() {
    assertThat(
        iidMapWithDefault(7.7, iidMapOf(
            STOCK_A1, 1.1,
            STOCK_A2, 2.2))
            .copyWithReplacedDefaultValue(6.6),
        iidMapWithDefaultMatcher(
            iidMapWithDefault(6.6, iidMapOf(
                STOCK_A1, 1.1,
                STOCK_A2, 2.2)),
            f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8)));
    assertThat(
        emptyIidMapWithDefault(0.0).copyWithReplacedDefaultValue(1.1),
        iidMapWithDefaultMatcher(
            emptyIidMapWithDefault(1.1),
            f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8)));
  }

  @Override
  public IidMapWithDefault<Double> makeTrivialObject() {
    return emptyIidMapWithDefault(0.0);
  }

  @Override
  public IidMapWithDefault<Double> makeNontrivialObject() {
    return iidMapWithDefault(7.7, iidMapOf(
        STOCK_A1, 1.1,
        STOCK_A2, 2.2));
  }

  @Override
  public IidMapWithDefault<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return iidMapWithDefault(7.7 + e, iidMapOf(
        STOCK_A1, 1.1 + e,
        STOCK_A2, 2.2 + e));
  }

  @Override
  protected boolean willMatch(IidMapWithDefault<Double> expected, IidMapWithDefault<Double> actual) {
    return iidMapWithDefaultMatcher(expected, f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8)).matches(actual);
  }

  public static <V> TypeSafeMatcher<IidMapWithDefault<V>> iidMapWithDefaultEqualityMatcher(
      IidMapWithDefault<V> expected) {
    return iidMapWithDefaultMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <V> TypeSafeMatcher<IidMapWithDefault<V>> iidMapWithDefaultMatcher(
      IidMapWithDefault<V> expected, MatcherGenerator<V> matcherGenerator) {
    return makeMatcher(expected,
        match(      v -> v.getDefaultValue(), matcherGenerator),
        matchIidMap(v -> v.getRawIidMap(),    matcherGenerator));
  }

}
