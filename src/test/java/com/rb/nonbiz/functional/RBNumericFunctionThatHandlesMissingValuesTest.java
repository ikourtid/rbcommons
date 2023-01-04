package com.rb.nonbiz.functional;

import com.rb.biz.types.Money;
import com.rb.nonbiz.functional.AllowsMissingValuesTest.TestAllowsMissingValues;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.RBNumeric;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.OptionalDouble;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.functional.RBNumericFunction.rbDoubleIdentityNumericFunction;
import static com.rb.nonbiz.functional.RBNumericFunction.rbDoubleNumericFunction;
import static com.rb.nonbiz.functional.RBNumericFunction.rbNumericFunction;
import static com.rb.nonbiz.functional.RBNumericFunctionTest.rbNumericFunctionUsingSamplingMatcher;
import static com.rb.nonbiz.functional.RBNumericFunctionThatHandlesMissingValues.rbDoubleFunctionThatReturnsEmptyOnMissingValues;
import static com.rb.nonbiz.functional.RBNumericFunctionThatHandlesMissingValues.rbDoubleFunctionThatReturnsSpecifiedDefaultOnMissingValues;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.rbNumericMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

public class RBNumericFunctionThatHandlesMissingValuesTest
    extends RBTestMatcher<RBNumericFunctionThatHandlesMissingValues<Double, Money>> {

  @Test
  public void testFunctionThatReturnsEmptyOptionalOnMissingValues() {
    RBNumericFunctionThatHandlesMissingValues<Double, Money> function = rbDoubleFunctionThatReturnsEmptyOnMissingValues(
        // Not sure why, but we have to cast here
        (RBNumericFunction<Double, Money>) rbDoubleNumericFunction(label("x+10"), v -> v + 10, v -> money(v)));

    assertOptionalAlmostEquals(
        money(doubleExplained(87, 10 + 77)),
        function.apply(new TestAllowsMissingValues(OptionalDouble.of(77))),
        DEFAULT_EPSILON_1e_8);
    assertOptionalEmpty(function.apply(new TestAllowsMissingValues(OptionalDouble.empty())));
  }

  @Test
  public void testFunctionThatAllowsMissing() {
    RBNumericFunctionThatHandlesMissingValues<Double, Money> function = rbDoubleFunctionThatReturnsSpecifiedDefaultOnMissingValues(
        rbNumericFunction(label("x+10"), v -> v + 10, v -> money(v)),
        money(123));

    assertOptionalAlmostEquals(
        money(doubleExplained(87, 10 + 77)),
        function.apply(new TestAllowsMissingValues(OptionalDouble.of(77))),
        DEFAULT_EPSILON_1e_8);
    assertOptionalAlmostEquals(
        money(123),
        function.apply(new TestAllowsMissingValues(OptionalDouble.empty())),
        DEFAULT_EPSILON_1e_8);
  }

  @Override
  public RBNumericFunctionThatHandlesMissingValues<Double, Money> makeTrivialObject() {
    return rbDoubleFunctionThatReturnsEmptyOnMissingValues(
        // Not sure why, but we have to cast here
        (RBNumericFunction<Double, Money>) rbDoubleIdentityNumericFunction(v -> money(v)));
  }

  @Override
  public RBNumericFunctionThatHandlesMissingValues<Double, Money> makeNontrivialObject() {
    return rbDoubleFunctionThatReturnsSpecifiedDefaultOnMissingValues(
        rbNumericFunction(label("x+10"), v -> v + 10, v -> money(v)),
        money(999));
  }

  @Override
  public RBNumericFunctionThatHandlesMissingValues<Double, Money> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbDoubleFunctionThatReturnsSpecifiedDefaultOnMissingValues(
        rbNumericFunction(label("x+10"), v -> v + 10 + e, v -> money(v)),
        money(999 + e));
  }

  @Override
  protected boolean willMatch(RBNumericFunctionThatHandlesMissingValues<Double, Money> expected,
                              RBNumericFunctionThatHandlesMissingValues<Double, Money> actual) {
    return rbNumericFunctionThatHandlesMissingValuesUsingSamplingMatcher(expected, -1.1, 3.3, 7.7).matches(actual);
  }

  // We can never match function objects f and g exactly, because we cannot enumerate all X. However, for testing purposes,
  // it is convenient to be able to at least that f(x_i) ~= g(x_i) for i = 0, ... (for a few points).
  // We will never be 100% sure that the functions match, but it's better than not checking anything at all.
  @SafeVarargs
  public static <X extends Number, Y extends RBNumeric<? super Y>> TypeSafeMatcher<RBNumericFunctionThatHandlesMissingValues<X, Y>>
  rbNumericFunctionThatHandlesMissingValuesUsingSamplingMatcher(
      RBNumericFunctionThatHandlesMissingValues<X, Y> expected, X firstSamplePoint, X ... restSamplePoints) {
    return makeMatcher(expected,
        matchOptional(v -> v.getYForMissingX(),             f -> rbNumericMatcher(f, DEFAULT_EPSILON_1e_8)),
        match(        v -> v.getFunctionForPresentValues(), f -> rbNumericFunctionUsingSamplingMatcher(f,
            firstSamplePoint, restSamplePoints)));
  }

}
