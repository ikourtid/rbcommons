package com.rb.nonbiz.functional;

import com.rb.biz.types.Money;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.RBNumeric;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstAndRest;
import static com.rb.nonbiz.functional.RBNumericFunction.rbIdentityNumericFunction;
import static com.rb.nonbiz.functional.RBNumericFunction.rbNumericFunction;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

public class RBNumericFunctionTest extends RBTestMatcher<RBNumericFunction<Double, Money>> {

  @Override
  public RBNumericFunction<Double, Money> makeTrivialObject() {
    return rbIdentityNumericFunction(v -> money(v));
  }

  @Override
  public RBNumericFunction<Double, Money> makeNontrivialObject() {
    return rbNumericFunction(label("x+10"), v -> v + 10, v -> money(v));
  }

  @Override
  public RBNumericFunction<Double, Money> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbNumericFunction(label("x+10"), v -> v + 10 + e, v -> money(v));
  }

  @Override
  protected boolean willMatch(RBNumericFunction<Double, Money> expected, RBNumericFunction<Double, Money> actual) {
    return rbNumericFunctionUsingSamplingMatcher(expected, -1.1, 3.3, 7.7).matches(actual);
  }

  // We can never match function objects f and g exactly, because we cannot enumerate all X. However, for testing purposes,
  // it is convenient to be able to at least that f(x_i) ~= g(x_i) for i = 0, ... (for a few points).
  // We will never be 100% sure that the functions match, but it's better than not checking anything at all.
  @SafeVarargs
  public static <X extends Number, Y extends RBNumeric<? super Y>> TypeSafeMatcher<RBNumericFunction<X, Y>>
  rbNumericFunctionUsingSamplingMatcher(
      RBNumericFunction<X, Y> expected, X firstSamplePoint, X ... restSamplePoints) {
    return makeMatcher(expected, actual ->
        concatenateFirstAndRest(firstSamplePoint, restSamplePoints)
          .allMatch(x ->
              DEFAULT_EPSILON_1e_8.areWithin(
                  expected.getRawFunction().applyAsDouble(x.doubleValue()),
                  actual.getRawFunction().applyAsDouble(x.doubleValue()))));
  }

}
