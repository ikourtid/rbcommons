package com.rb.biz.investing.modeling.selection.overrides;

import com.rb.biz.investing.modeling.selection.overrides.Overrides.OverridesBuilder;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueAndOverride.AlwaysUseOverrideAndIgnoreExistingValue.alwaysUseOverrideAndIgnoreExistingValue;
import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueAndOverride.OnlyUseOverrideToFurtherReduceExistingValue.onlyUseOverrideToFurtherReduceExistingValue;
import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueAndOverrideTest.behaviorWithValueAndOverrideMatcher;
import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueButNoOverride.UseExistingValueWhenOverrideMissing.useExistingValueWhenOverrideMissing;
import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueButNoOverride.UseFixedValueWhenOverrideMissing.useFixedValueWhenOverrideMissing;
import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueButNoOverrideTest.behaviorWithValueButNoOverrideMatcher;
import static com.rb.biz.investing.modeling.selection.overrides.Overrides.noOverrides;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OverridesTest extends RBTestMatcher<Overrides<String, Double>> {

  @Test
  public void testImpliesNoOverrides() {
    assertTrue(noOverrides().impliesNoOverrides());
    assertFalse(
        "Overrides map is not empty",
        OverridesBuilder.<String, Double>overridesBuilder()
            .setOverridesMap(rbMapOf(
                "a", 1.1,
                "b", 2.2))
            // This is irrelevant in the case where there are no overrides to begin with
            .setBehaviorWithValueAndOverride(alwaysUseOverrideAndIgnoreExistingValue())
            .setBehaviorWithValueButNoOverride(useExistingValueWhenOverrideMissing())
            .throwWhenNoValueAndNoOverride()
            .build()
            .impliesNoOverrides());
    assertFalse(
        "Uses fixed value when the override is missing",
        OverridesBuilder.<String, Double>overridesBuilder()
            .setOverridesMap(emptyRBMap())
            // This is irrelevant in the case where there are no overrides to begin with
            .setBehaviorWithValueAndOverride(alwaysUseOverrideAndIgnoreExistingValue())
            .setBehaviorWithValueButNoOverride(useFixedValueWhenOverrideMissing(DUMMY_DOUBLE))
            .throwWhenNoValueAndNoOverride()
            .build()
            .impliesNoOverrides());
    assertFalse(
        "Uses fixed value when the override is missing",
        OverridesBuilder.<String, Double>overridesBuilder()
            .setOverridesMap(emptyRBMap())
            // This is irrelevant in the case where there are no overrides to begin with
            .setBehaviorWithValueAndOverride(alwaysUseOverrideAndIgnoreExistingValue())
            .setBehaviorWithValueButNoOverride(useExistingValueWhenOverrideMissing())
            .useThisWhenNoValueAndNoOverride(DUMMY_DOUBLE)
            .build()
            .impliesNoOverrides());
  }

  @Override
  public Overrides<String, Double> makeTrivialObject() {
    return noOverrides();
  }

  @Override
  public Overrides<String, Double> makeNontrivialObject() {
    return OverridesBuilder.<String, Double>overridesBuilder()
        .setOverridesMap(rbMapOf(
            "a", 1.1,
            "b", 2.2))
        .setBehaviorWithValueAndOverride(onlyUseOverrideToFurtherReduceExistingValue())
        .setBehaviorWithValueButNoOverride(useFixedValueWhenOverrideMissing(3.3))
        .useThisWhenNoValueAndNoOverride(4.4)
        .build();
  }

  @Override
  public Overrides<String, Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return OverridesBuilder.<String, Double>overridesBuilder()
        .setOverridesMap(rbMapOf(
            "a", 1.1 + e,
            "b", 2.2 + e))
        .setBehaviorWithValueAndOverride(onlyUseOverrideToFurtherReduceExistingValue())
        .setBehaviorWithValueButNoOverride(useFixedValueWhenOverrideMissing(3.3 + e))
        .useThisWhenNoValueAndNoOverride(4.4 + e)
        .build();
  }

  @Override
  protected boolean willMatch(Overrides<String, Double> expected, Overrides<String, Double> actual) {
    return overridesMatcher(expected, f -> doubleAlmostEqualsMatcher(f, 1e-8)).matches(actual);
  }

  public static <K, V extends Comparable> TypeSafeMatcher<Overrides<K, V>> overridesMatcher(
      Overrides<K, V> expected, MatcherGenerator<V> valuesMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getOverridesMap(),                   f -> rbMapMatcher(f, valuesMatcherGenerator)),
        match(v -> v.getBehaviorWithValueAndOverride(),   f -> behaviorWithValueAndOverrideMatcher(f)),
        match(v -> v.getBehaviorWithValueButNoOverride(), f -> behaviorWithValueButNoOverrideMatcher(f, valuesMatcherGenerator)),
        matchOptional(v -> v.getOptionalWhenNoValueAndNoOverride(), valuesMatcherGenerator));
  }

}
