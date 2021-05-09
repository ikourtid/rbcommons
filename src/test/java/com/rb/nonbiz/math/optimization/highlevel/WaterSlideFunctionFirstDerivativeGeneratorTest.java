package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.function.DoubleUnaryOperator;

import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptor.unscaledWaterSlideFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptor.waterSlideFunctionDescriptor;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static org.junit.Assert.assertEquals;

public class WaterSlideFunctionFirstDerivativeGeneratorTest extends RBTest<WaterSlideFunctionFirstDerivativeGenerator> {

  @Test
  public void testDerivativeIsCorrect() {
    // Because this is a mathematical function, the only easy way I can think of testing this is
    // to use the definition of the derivative and see if [f(x) - f(x + dx)] / dx
    // is similar enough to f'(x), for a few values of x.
    // I am intentionally using non-round numbers in the WaterSlideFunctionDescriptor
    // so we won't have things working by accident in case e.g. using an exponent of 2
    // becomes a multiplier of 1 in some derivation, or a power of 1, or something.
    WaterSlideFunctionDescriptor descriptor = unscaledWaterSlideFunctionDescriptor(2.2, 5.5);
    DoubleUnaryOperator f = descriptor.asFunction();
    DoubleUnaryOperator fPrime = makeTestObject().takeFirstDerivative(descriptor);

    // This approximation may be more accurate, since it is centered on x,
    // and moves by dx in both directions (up & down).
    double dx = 1e-6;
    double e = 1e-8; // epsilon
    ImmutableList.of(1e-5, 1e-4, 1e-3, 0.01, 0.1, 0.2, 0.5, 1.0, 2.0, 5.0, 10.0, 100.0)
        .forEach(x -> assertEquals(
            (f.applyAsDouble(x + dx) - f.applyAsDouble(x - dx)) / (2 * dx),
            fPrime.applyAsDouble(x), e));
  }

  @Test
  public void slopeIs45degreesSufficientlyFarOut() {
    // I am intentionally using non-round numbers in the WaterSlideFunctionDescriptor
    // so we won't have things working by accident in case e.g. using an exponent of 2
    // becomes a multiplier of 1 in some derivation, or a power of 1, or something.
    for (double divisor : ImmutableList.of(0.025, 0.1, 0.2, 0.4, 2.0, 5.0, 10.0)) {
      DoubleUnaryOperator fPrime = makeTestObject().takeFirstDerivative(
          unscaledWaterSlideFunctionDescriptor(2, divisor));
      double e = 0.002; // epsilon
      ImmutableList.of(200.0, 1_000.0, 1_000_000.0)
          .forEach(x -> assertEquals("Using " + x, 1, fPrime.applyAsDouble(x), e));
    }
  }

  @Test
  public void slopeIsEqualToScalingMultiplierSufficientlyFarOut() {
    // I am intentionally using non-round numbers in the WaterSlideFunctionDescriptor
    // so we won't have things working by accident in case e.g. using an exponent of 2
    // becomes a multiplier of 1 in some derivation, or a power of 1, or something.
    for (double divisor : ImmutableList.of(0.025, 0.1, 0.2, 0.4, 2.0, 5.0, 10.0)) {
      for (double scaler : ImmutableList.of(0.9, 1.0, 1.1)) {
        DoubleUnaryOperator fPrime = makeTestObject().takeFirstDerivative(
            waterSlideFunctionDescriptor(2, divisor, positiveMultiplier(scaler)));
        double e = 0.002; // epsilon
        ImmutableList.of(200.0, 1_000.0, 1_000_000.0)
            .forEach(x -> assertEquals("Using " + x, scaler, fPrime.applyAsDouble(x), e));
      }
    }
  }

  @Override
  protected WaterSlideFunctionFirstDerivativeGenerator makeTestObject() {
    return new WaterSlideFunctionFirstDerivativeGenerator();
  }

}
