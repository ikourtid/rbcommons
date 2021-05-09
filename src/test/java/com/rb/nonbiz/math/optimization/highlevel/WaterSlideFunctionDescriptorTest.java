package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.PositiveMultiplier;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptor.unscaledWaterSlideFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptor.waterSlideFunctionDescriptor;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingImpreciseAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.PositiveMultiplier.POSITIVE_MULTIPLIER_1;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static org.junit.Assert.assertEquals;

public class WaterSlideFunctionDescriptorTest extends RBTestMatcher<WaterSlideFunctionDescriptor> {

  @Test
  public void exponentCannotBeBelowOne() {
    double validDivisor = 1;
    PositiveMultiplier validScaler = POSITIVE_MULTIPLIER_1;
    ImmutableList.of(-999.0, -1.0, -0.1, 0.0, 0.1, 0.999)
        .forEach(invalidExponent ->
            assertIllegalArgumentException( () -> waterSlideFunctionDescriptor(invalidExponent, validDivisor, validScaler)));
    ImmutableList.of(1.0, 1.0001, 2.0, 999.0)
        .forEach(validExponent -> {
          WaterSlideFunctionDescriptor doesNotThrow = waterSlideFunctionDescriptor(validExponent, validDivisor, validScaler);
        });
  }

  @Test
  public void divisorMustBePositive() {
    double validExponent = 2;
    PositiveMultiplier validScaler = POSITIVE_MULTIPLIER_1;
    ImmutableList.of(-999.0, -1.0, -0.1, 0.0)
        .forEach(invalidDivisor ->
            assertIllegalArgumentException( () -> waterSlideFunctionDescriptor(validExponent, invalidDivisor, validScaler)));
    ImmutableList.of(0.001, 1.0, 1.0001, 2.0, 999.0)
        .forEach(validDivisor -> {
          WaterSlideFunctionDescriptor doesNotThrow = waterSlideFunctionDescriptor(validExponent, validDivisor, validScaler);
        });
  }

  @Test
  public void testAsFunctionText() {
    assertEquals(
        "0.9900*5.5000*((1+(x/5.5000)^2.2000)^(1/2.2000)-1)",
        waterSlideFunctionDescriptor(2.2, 5.5, positiveMultiplier(0.99)).asFunctionText());
  }

  @Test
  public void testHashCodeAndEquals() {
    // These have to be exactly the same, not epsilon-same
    WaterSlideFunctionDescriptor obj1 = waterSlideFunctionDescriptor(2.2, 3.3, positiveMultiplier(0.99));
    WaterSlideFunctionDescriptor obj2 = waterSlideFunctionDescriptor(2.2, 3.3, positiveMultiplier(0.99));
    assertEquals(obj1, obj2);
    assertEquals(obj1.hashCode(), obj2.hashCode());
  }

  @Override
  public WaterSlideFunctionDescriptor makeTrivialObject() {
    return unscaledWaterSlideFunctionDescriptor(1, 1);
  }

  @Override
  public WaterSlideFunctionDescriptor makeNontrivialObject() {
    return waterSlideFunctionDescriptor(2.2, 3.3, positiveMultiplier(0.99));
  }

  @Override
  public WaterSlideFunctionDescriptor makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return waterSlideFunctionDescriptor(2.2 + e, 3.3 + e, positiveMultiplier(0.99 + e));
  }

  @Override
  protected boolean willMatch(WaterSlideFunctionDescriptor expected, WaterSlideFunctionDescriptor actual) {
    return waterSlideFunctionDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<WaterSlideFunctionDescriptor> waterSlideFunctionDescriptorMatcher(
      WaterSlideFunctionDescriptor expected) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getExponent(), 1e-8),
        matchUsingDoubleAlmostEquals(v -> v.getDivisor(),  1e-8),
        matchUsingImpreciseAlmostEquals(v -> v.getScalingMultiplier(), 1e-8));
  }

}
