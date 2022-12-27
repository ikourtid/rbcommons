package com.rb.biz.investing.modeling.selection.overrides;

import com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueButNoOverride.Visitor;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Function;

import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueButNoOverride.UseExistingValueWhenOverrideMissing.useExistingValueWhenOverrideMissing;
import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueButNoOverride.UseFixedValueWhenOverrideMissing.useFixedValueWhenOverrideMissing;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.optionalMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_DOUBLE;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class BehaviorWithValueButNoOverrideTest extends RBTestMatcher<BehaviorWithValueButNoOverride<String>> {

  @Test
  public void differentTypesDoNotMatch() {
    BehaviorWithValueButNoOverride<Double> obj1 = useExistingValueWhenOverrideMissing();
    BehaviorWithValueButNoOverride<Double> obj2 = useFixedValueWhenOverrideMissing(DUMMY_DOUBLE);
    assertThat(obj1, not(behaviorWithValueButNoOverrideMatcher(obj2, f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8))));
    assertThat(obj2, not(behaviorWithValueButNoOverrideMatcher(obj1, f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8))));
  }

  @Override
  public BehaviorWithValueButNoOverride<String> makeTrivialObject() {
    return useExistingValueWhenOverrideMissing();
  }

  @Override
  public BehaviorWithValueButNoOverride<String> makeNontrivialObject() {
    return useFixedValueWhenOverrideMissing("abc");
  }

  @Override
  public BehaviorWithValueButNoOverride<String> makeMatchingNontrivialObject() {
    return useFixedValueWhenOverrideMissing("abc");
  }

  @Override
  protected boolean willMatch(BehaviorWithValueButNoOverride<String> expected,
                              BehaviorWithValueButNoOverride<String> actual) {
    return behaviorWithValueButNoOverrideMatcher(expected, f -> typeSafeEqualTo(f)).matches(actual);
  }

  public static <T> TypeSafeMatcher<BehaviorWithValueButNoOverride<T>> behaviorWithValueButNoOverrideMatcher(
      BehaviorWithValueButNoOverride<T> expected, MatcherGenerator<T> matcherGenerator) {
    // we can't use the GeneralVisitorMatcher infra here, because this visitor is special: it always returns the same
    // type. This works because we only have 2 types here, although it is not very pretty.
    return makeMatcher(expected, actual -> {
      Function<BehaviorWithValueButNoOverride<T>, Optional<T>> fixedValueGetter =
          obj -> Optional.ofNullable(obj.visit(new Visitor<T>() {
            @Override
            public T visitUseExistingValueWhenOverrideMissing() {
              return null;
            }

            @Override
            public T visitUseFixedValueWhenOverrideMissing(T fixedValue) {
              return fixedValue;
            }
          }));
      Optional<T> expectedFixedValue = fixedValueGetter.apply(expected);
      Optional<T> actualFixedValue = fixedValueGetter.apply(actual);
      return optionalMatcher(expectedFixedValue, matcherGenerator).matches(actualFixedValue);
    });
  }

}
