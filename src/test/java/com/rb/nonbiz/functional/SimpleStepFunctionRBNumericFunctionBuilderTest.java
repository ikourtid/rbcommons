package com.rb.nonbiz.functional;

import com.rb.biz.types.Money;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.RBNumeric;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.functional.RBNumericFunctionTest.rbNumericFunctionUsingSamplingMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertNullPointerException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_LABEL;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static org.junit.Assert.assertEquals;

// The builder is not quite a data class, so RBTestMatcher is not appropriate here.
public class SimpleStepFunctionRBNumericFunctionBuilderTest extends
    RBTestMatcher<SimpleStepFunctionRBNumericFunctionBuilder<Double, Money>> {

  // the y-value at x = X0 is the same as for x > X0
  @Test
  public void testSetYForXLessThanX0() {
    RBNumericFunction<Double, Money> rbNumericFunction =
        SimpleStepFunctionRBNumericFunctionBuilder.<Double, Money>simpleStepFunctionRBNumericFunctionBuilder()
            .setLabel(DUMMY_LABEL)
            .setInstantiator(v -> money(v))
            .setX0(10.0)
            .setYForXLessThanX0(money(7))
            .setYForXGreaterThanOrEqualToX0(money(19))
            .build();

    assertEquals(money(7),  rbNumericFunction.apply(-999.9));
    assertEquals(money(7),  rbNumericFunction.apply(0.0));
    assertEquals(money(7),  rbNumericFunction.apply(9.99));
    assertEquals(money(7),  rbNumericFunction.apply(10.0 - 1e-15));
    assertEquals(money(19), rbNumericFunction.apply(10.0));      // boundary y-value matches the "greater than" y-value
    assertEquals(money(19), rbNumericFunction.apply(10.0 + 1e-15));
    assertEquals(money(19), rbNumericFunction.apply(20.0));
    assertEquals(money(19), rbNumericFunction.apply(999.9));
  }

  // the y-value at x = X0 is the same as for x < X0
  @Test
  public void testSetYForXLessThanOrEqualX0() {
    RBNumericFunction<Double, Money> rbNumericFunction =
        SimpleStepFunctionRBNumericFunctionBuilder.<Double, Money>simpleStepFunctionRBNumericFunctionBuilder()
            .useAutoLabel()
            .setInstantiator(v -> money(v))
            .setX0(10.0)
            .setYForXLessThanOrEqualToX0(money(7))
            .setYForXGreaterThanX0(money(19))
            .build();

    assertEquals(money(7),  rbNumericFunction.apply(-999.9));
    assertEquals(money(7),  rbNumericFunction.apply(0.0));
    assertEquals(money(7),  rbNumericFunction.apply(9.99));
    assertEquals(money(7),  rbNumericFunction.apply(10.0 - 1e-15));
    assertEquals(money(7),  rbNumericFunction.apply(10.0));     // the only difference vs. above; now matches "less than" y-value
    assertEquals(money(19), rbNumericFunction.apply(10.0 + 1e-15));
    assertEquals(money(19), rbNumericFunction.apply(20.0));
    assertEquals(money(19), rbNumericFunction.apply(999.9));
  }

  @Test
  public void improperCombinations_throw() {
    // can't set one y-value for x <= x0 AND another y-value for x >= x0; will have two values for x = x0
    assertIllegalArgumentException( () ->
        SimpleStepFunctionRBNumericFunctionBuilder.<Double, Money>simpleStepFunctionRBNumericFunctionBuilder()
            .useAutoLabelWithPrefix("abc")
            .setInstantiator(v -> money(v))
            .setX0(DUMMY_DOUBLE)
            .setYForXLessThanOrEqualToX0(   money(1))  // y-value for x <= x0
            .setYForXGreaterThanOrEqualToX0(money(2))  // y-value for x >= x0; two values for x = x0
            .build());

    // can't set one y-value for x < x0 AND another y-value for x > x0; nothing specified for x = x0
    assertNullPointerException( () ->
        SimpleStepFunctionRBNumericFunctionBuilder.<Double, Money>simpleStepFunctionRBNumericFunctionBuilder()
            .setLabel(DUMMY_LABEL)
            .setInstantiator(v -> money(v))
            .setX0(DUMMY_DOUBLE)
            .setYForXLessThanX0(   money(1))  // y-value for x < x0
            .setYForXGreaterThanX0(money(2))  // y-value for x > x0; nothing specified for x = x0
            .build());
  }

  @Override
  public SimpleStepFunctionRBNumericFunctionBuilder<Double, Money> makeTrivialObject() {
    return SimpleStepFunctionRBNumericFunctionBuilder.<Double, Money>simpleStepFunctionRBNumericFunctionBuilder()
        .setLabel(label(""))
        .setInstantiator(v -> money(v))
        .setX0(1.0)
        .setYForXGreaterThanOrEqualToX0(money(1))
        .setYForXLessThanX0(ZERO_MONEY);
  }

  @Override
  public SimpleStepFunctionRBNumericFunctionBuilder<Double, Money> makeNontrivialObject() {
    return SimpleStepFunctionRBNumericFunctionBuilder.<Double, Money>simpleStepFunctionRBNumericFunctionBuilder()
        .setLabel(label("abc"))
        .setInstantiator(v -> money(v))
        .setX0(12.345)
        .setYForXGreaterThanOrEqualToX0(money(11.11))
        .setYForXLessThanX0(            money(22.22));
  }

  @Override
  public SimpleStepFunctionRBNumericFunctionBuilder<Double, Money> makeMatchingNontrivialObject() {
    double e = 1e-9;
    return SimpleStepFunctionRBNumericFunctionBuilder.<Double, Money>simpleStepFunctionRBNumericFunctionBuilder()
        .setLabel(label("abc"))
        .setInstantiator(v -> money(v))
        .setX0(12.345 + e)
        .setYForXGreaterThanOrEqualToX0(money(11.11 + e))
        .setYForXLessThanX0(            money(22.22 + e));
  }

  @Override
  protected boolean willMatch(
      SimpleStepFunctionRBNumericFunctionBuilder<Double, Money> expected,
      SimpleStepFunctionRBNumericFunctionBuilder<Double, Money> actual) {
    return simpleStepFunctionRBNumericFunctionBuilderMatcher(expected, -999.9, -1.1, 3.3, 7.7, 999.9).matches(actual);
  }

  @SafeVarargs
  public static <X extends Number, Y extends RBNumeric<? super Y>> TypeSafeMatcher<SimpleStepFunctionRBNumericFunctionBuilder<X, Y>>
  simpleStepFunctionRBNumericFunctionBuilderMatcher(
      SimpleStepFunctionRBNumericFunctionBuilder<X, Y> expected, X firstSamplePoint, X ... restSamplePoints) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getX0().doubleValue(),            1e-8),
        matchUsingDoubleAlmostEquals(v -> v.getY0().doubleValue(),            1e-8),
        matchUsingDoubleAlmostEquals(v -> v.getY1().doubleValue(),            1e-8),
        matchUsingDoubleAlmostEquals(v -> v.getYForXEqualsX0().doubleValue(), 1e-8),
        match(v -> v.getInstantiator(), f -> rbNumericFunctionUsingSamplingMatcher(f, firstSamplePoint, restSamplePoints)));
  }

}
