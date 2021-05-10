package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBIntegrationTest;
import org.junit.Test;

import java.util.function.DoubleUnaryOperator;

import static com.rb.nonbiz.math.optimization.highlevel.ApproximatedWaterSlideFunctionDescriptor.approximatedWaterSlideFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRanges.linearApproximationVarRanges;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValues.linearApproximationVarRangesAndValuesWithValuesPrecomputed;
import static com.rb.nonbiz.math.optimization.highlevel.LinearlyApproximatedWaterSlideFunction.linearlyApproximatedWaterSlideFunction;
import static com.rb.nonbiz.math.optimization.highlevel.LinearlyApproximatedWaterSlideFunctionTest.linearlyApproximatedWaterSlideFunctionMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructions.waterSlideApproximationInstructions;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructionsAboveX90.waterSlideApproximationInstructionsAboveX90;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructionsBelowX90.waterSlideApproximationInstructionsBelowX90;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptor.unscaledWaterSlideFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.highlevel.X90.x90;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInBps;
import static org.junit.Assert.assertEquals;

public class LinearlyApproximatedWaterSlideFunctionGeneratorTest
    extends RBIntegrationTest<LinearlyApproximatedWaterSlideFunctionGenerator> {

  @Test
  public void realisticProdExample_severalHingePoints_assetClassWithTarget10pct() {
    // E.g. we typically decide that an asset class with a target of 10% has an x90 of +/- 5%.
    double x90 = 0.05;
    WaterSlideFunctionDescriptor descriptor = makeDescriptorWithThisX90(x90);

    double d = doubleExplained(0.02421610524189263, descriptor.getDivisor());
    DoubleUnaryOperator f = descriptor.asFunction();
    DoubleUnaryOperator fPrime = makeRealObject(WaterSlideFunctionFirstDerivativeGenerator.class)
        .takeFirstDerivative(descriptor);

    // As x goes to infinity, the slope of the line tends to 1
    assertEquals(1, fPrime.applyAsDouble(999_999), 1e-8);
    // At x90, the derivative should be 90% of its final value, i.e. 0.9
    assertEquals(0.9, fPrime.applyAsDouble(x90), 1e-8);

    ApproximatedWaterSlideFunctionDescriptor approximationWithElevenHingePoints = approximatedWaterSlideFunctionDescriptor(
        descriptor,
        waterSlideApproximationInstructions(
            waterSlideApproximationInstructionsBelowX90(unitFraction(0.9), 10),
            waterSlideApproximationInstructionsAboveX90(34.567, 1.6)));

    // the first 10 hinge points will be where f'(x) is 0, 0.1, 0.2, ..., 0.9
    // unitFractionInBps makes things a bit easier to read, because there are no leading zeroes.
    double x10 = unitFractionInBps( 24.338101496746).doubleValue();
    double x20 = unitFractionInBps( 49.430917827160).doubleValue();
    double x30 = unitFractionInBps( 76.156127779230).doubleValue();
    double x40 = unitFractionInBps(105.687747911482).doubleValue();
    double x50 = unitFractionInBps(139.811748796923).doubleValue();
    double x60 = unitFractionInBps(181.620789308789).doubleValue();
    double x70 = unitFractionInBps(237.365309174665).doubleValue();
    double x80 = unitFractionInBps(322.881403215624).doubleValue();
    doubleExplained(x90, unitFractionInBps(500).doubleValue());

    // These are the hinge points above the x90 point at 0.05 = 5%, but before we hit 1.0
    double hp1 = doubleExplained(0.06_77118596774923,  x90 + (x90 - x80));
    double hp2 = doubleExplained(0.09_60508351622362,  hp1 + 1.6 * (hp1 - x90));
    double hp3 = doubleExplained(0.14_139319593782645, hp2 + 1.6 * (hp2 - hp1));
    double hp4 = doubleExplained(0.21_394097317877087, hp3 + 1.6 * (hp3 - hp2));
    double hp5 = doubleExplained(0.33_00174167642819,  hp4 + 1.6 * (hp4 - hp3));
    double hp6 = doubleExplained(0.51_57397265010997,  hp5 + 1.6 * (hp5 - hp4));
    double hp7 = doubleExplained(0.81_2895422080008,   hp6 + 1.6 * (hp6 - hp5));

    doubleExplained(0.1, fPrime.applyAsDouble(x10));
    doubleExplained(0.2, fPrime.applyAsDouble(x20));
    doubleExplained(0.3, fPrime.applyAsDouble(x30));
    doubleExplained(0.4, fPrime.applyAsDouble(x40));
    doubleExplained(0.5, fPrime.applyAsDouble(x50));
    doubleExplained(0.6, fPrime.applyAsDouble(x60));
    doubleExplained(0.7, fPrime.applyAsDouble(x70));
    doubleExplained(0.8, fPrime.applyAsDouble(x80));
    doubleExplained(0.9, fPrime.applyAsDouble(doubleExplained(x90, 0.05)));

    assertThat(
        makeRealObject().generate(approximationWithElevenHingePoints),
        linearlyApproximatedWaterSlideFunctionMatcher(
            linearlyApproximatedWaterSlideFunction(
                descriptor,
                // Using linearApproximationVarRangesAndValuesWithValuesPrecomputed to make explicit
                // the values that get calculated upon construction inside this object (2nd arg)
                linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                    // With a real parabola x^2, the derivative is 2*x, so the slope at x = 1, 2, 3, 4 etc.
                    // is 2, 4, 6, 8, etc. - so the slope goes up by fixed amounts for each x += 1.
                    // However, in this case we essentially start with a parabola and end up with a line,
                    // so those x points aren't equally spaced with each other; the are farther and farther
                    // away as x increases. This is expected and desirable.
                    linearApproximationVarRanges(ImmutableList.of(
                        0.0, x10, x20, x30, x40, x50, x60, x70, x80, x90,
                        hp1, hp2, hp3, hp4, hp5, hp6, hp7,
                        1.0,
                        34.567)),
                    ImmutableList.of(
                        doubleExplained(0,           f.applyAsDouble(0.0)),
                        doubleExplained(0.000121996, f.applyAsDouble(x10)),
                        doubleExplained(0.000499354, f.applyAsDouble(x20)),
                        doubleExplained(0.001169270, f.applyAsDouble(x30)),
                        doubleExplained(0.002205832, f.applyAsDouble(x40)),
                        doubleExplained(0.003746245, f.applyAsDouble(x50)),
                        doubleExplained(0.006054026, f.applyAsDouble(x60)),
                        doubleExplained(0.009693225, f.applyAsDouble(x70)),
                        doubleExplained(0.016144070, f.applyAsDouble(x80)),
                        doubleExplained(0.031339450, f.applyAsDouble(x90)),

                        doubleExplained(0.047695756, f.applyAsDouble(hp1)),
                        doubleExplained(0.074840357, f.applyAsDouble(hp2)),
                        doubleExplained(0.119235823, f.applyAsDouble(hp3)),
                        doubleExplained(0.191091024, f.applyAsDouble(hp4)),
                        doubleExplained(0.306688587, f.applyAsDouble(hp5)),
                        doubleExplained(0.492091831, f.applyAsDouble(hp6)),
                        doubleExplained(0.789039935, f.applyAsDouble(hp7)),

                        doubleExplained(0.976077061, f.applyAsDouble(1.0)),

                        doubleExplained(34.542792377,  f.applyAsDouble(34.567))),
                    descriptor))));

  }

  @Test
  public void realisticProdExample_severalHingePoints_assetClassWithTarget40pct() {
    // E.g. we typically decide that an asset class with a target of 40% has an x90 of +/- 10%.
    double x90 = 0.10;
    WaterSlideFunctionDescriptor descriptor = makeDescriptorWithThisX90(x90);
    double d = doubleExplained(0.04843221048378526, descriptor.getDivisor());
    DoubleUnaryOperator f = descriptor.asFunction();
    DoubleUnaryOperator fPrime = makeRealObject(WaterSlideFunctionFirstDerivativeGenerator.class)
        .takeFirstDerivative(descriptor);

    // As x goes to infinity, the slope of the line tends to 1
    assertEquals(1, fPrime.applyAsDouble(999_999), 1e-8);
    // At x90, the derivative should be 90% of its final value, i.e. 0.9
    assertEquals(0.9, fPrime.applyAsDouble(x90), 1e-8);

    ApproximatedWaterSlideFunctionDescriptor approximationWithElevenHingePoints = approximatedWaterSlideFunctionDescriptor(
        descriptor,
        waterSlideApproximationInstructions(
            waterSlideApproximationInstructionsBelowX90(unitFraction(0.9), 10),
            waterSlideApproximationInstructionsAboveX90(34.567, 1.6)));

    // the first 10 hinge points will be where f'(x) is 0, 0.1, 0.2, ..., 0.9
    // unitFractionInBps makes things a bit easier to read, because there are no leading zeroes.
    double x10 = unitFractionInBps( 48.676203).doubleValue();
    double x20 = unitFractionInBps( 98.861835).doubleValue();
    double x30 = unitFractionInBps(152.312255).doubleValue();
    double x40 = unitFractionInBps(211.375495).doubleValue();
    double x50 = unitFractionInBps(279.623497).doubleValue();
    double x60 = unitFractionInBps(363.24157).doubleValue();
    double x70 = unitFractionInBps(474.730619).doubleValue();
    double x80 = unitFractionInBps(645.762807).doubleValue();
    doubleExplained(x90, unitFractionInBps(1_000).doubleValue());

    // These are the hinge points above the x90 point at 0.10 = 10%, but before we hit 1.0
    double hp1 = doubleExplained(0.13_542371930000002, x90 + (x90 - x80));
    double hp2 = doubleExplained(0.19_210167018000004, hp1 + 1.6 * (hp1 - x90));
    double hp3 = doubleExplained(0.28_27863915880001,  hp2 + 1.6 * (hp2 - hp1));
    double hp4 = doubleExplained(0.42_788194584080025, hp3 + 1.6 * (hp3 - hp2));
    double hp5 = doubleExplained(0.66_00348326452805,  hp4 + 1.6 * (hp4 - hp3));

    doubleExplained(0.1, fPrime.applyAsDouble(x10));
    doubleExplained(0.2, fPrime.applyAsDouble(x20));
    doubleExplained(0.3, fPrime.applyAsDouble(x30));
    doubleExplained(0.4, fPrime.applyAsDouble(x40));
    doubleExplained(0.5, fPrime.applyAsDouble(x50));
    doubleExplained(0.6, fPrime.applyAsDouble(x60));
    doubleExplained(0.7, fPrime.applyAsDouble(x70));
    doubleExplained(0.8, fPrime.applyAsDouble(x80));
    doubleExplained(0.9, fPrime.applyAsDouble(doubleExplained(x90, 0.1)));

    assertThat(
        makeRealObject().generate(approximationWithElevenHingePoints),
        linearlyApproximatedWaterSlideFunctionMatcher(
            linearlyApproximatedWaterSlideFunction(
                descriptor,
                // Using linearApproximationVarRangesAndValuesWithValuesPrecomputed to make explicit
                // the values that get calculated upon construction inside this object (2nd arg)
                linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                    // With a real parabola x^2, the derivative is 2*x, so the slope at x = 1, 2, 3, 4 etc.
                    // is 2, 4, 6, 8, etc. - so the slope goes up by fixed amounts for each x += 1.
                    // However, in this case we essentially start with a parabola and end up with a line,
                    // so those x points aren't equally spaced with each other; the are farther and farther
                    // away as x increases. This is expected and desirable.
                    linearApproximationVarRanges(ImmutableList.of(
                        0.0, x10, x20, x30, x40, x50, x60, x70, x80, x90,
                        hp1, hp2, hp3, hp4, hp5,
                        1.0,
                        34.567)),
                    ImmutableList.of(
                        doubleExplained(0,           f.applyAsDouble(0.0)),
                        doubleExplained(0.000243993, f.applyAsDouble(x10)),
                        doubleExplained(0.000998707, f.applyAsDouble(x20)),
                        doubleExplained(0.002338541, f.applyAsDouble(x30)),
                        doubleExplained(0.004411663, f.applyAsDouble(x40)),
                        doubleExplained(0.007492489, f.applyAsDouble(x50)),
                        doubleExplained(0.012108052, f.applyAsDouble(x60)),
                        doubleExplained(0.019386449, f.applyAsDouble(x70)),
                        doubleExplained(0.032288140, f.applyAsDouble(x80)),
                        doubleExplained(0.062678901, f.applyAsDouble(x90)),

                        doubleExplained(0.095391511, f.applyAsDouble(hp1)),
                        doubleExplained(0.149680714, f.applyAsDouble(hp2)),
                        doubleExplained(0.238471645, f.applyAsDouble(hp3)),
                        doubleExplained(0.382182047, f.applyAsDouble(hp4)),
                        doubleExplained(0.613377172, f.applyAsDouble(hp5)),

                        doubleExplained(0.952739942, f.applyAsDouble(1.0)),

                        doubleExplained(34.518601719,  f.applyAsDouble(34.567))),
                    descriptor))));
  }

  @Test
  public void hasNoInteriorHingePointsBelowX90() {
    WaterSlideFunctionDescriptor descriptor = makeDescriptorWithThisX90(0.05);
    DoubleUnaryOperator f = descriptor.asFunction();
    DoubleUnaryOperator fPrime = makeRealObject(WaterSlideFunctionFirstDerivativeGenerator.class).takeFirstDerivative(descriptor);

    // As x goes to infinity, the slope of the line tends to 1
    doubleExplained(1, fPrime.applyAsDouble(999_999));

    ApproximatedWaterSlideFunctionDescriptor approximation = approximatedWaterSlideFunctionDescriptor(
        descriptor,
        waterSlideApproximationInstructions(
            waterSlideApproximationInstructionsBelowX90(unitFraction(0.9), 2),
            // a huge step multiplier of 111 will result in the linear approximation above the x90 of 0.05
            // having only a single hinge point at 0.05 + length of last segment in the approximation of 0 to x90
            // = 0.10
            waterSlideApproximationInstructionsAboveX90(34.567, 111)));

    doubleExplained(0.9, fPrime.applyAsDouble(0.05));

    assertThat(
        makeRealObject().generate(approximation),
        linearlyApproximatedWaterSlideFunctionMatcher(
            linearlyApproximatedWaterSlideFunction(
                descriptor,
                // Using linearApproximationVarRangesAndValuesWithValuesPrecomputed to make explicit
                // the values that get calculated upon construction inside this object (2nd arg)
                linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                    linearApproximationVarRanges(ImmutableList.of(
                        0.0,
                        0.05,
                        0.10,
                        1.0,
                        34.567)),
                    ImmutableList.of(
                        doubleExplained( 0.0,                f.applyAsDouble(0.0)),
                        doubleExplained(0.03133945031366292, f.applyAsDouble(0.05)),
                        doubleExplained(0.07867422352173034, f.applyAsDouble(0.10)),
                        doubleExplained(0.976077061661234,   f.applyAsDouble(1.0)),
                        doubleExplained(34.542792377121074,  f.applyAsDouble(34.567))),
                    descriptor))));
  }

  @Test
  public void printRealisticExample() {
    System.out.println(makeRealObject().generate(approximatedWaterSlideFunctionDescriptor(
        unscaledWaterSlideFunctionDescriptor(2.0, 1 / 5.0),
        waterSlideApproximationInstructions(
            waterSlideApproximationInstructionsBelowX90(unitFraction(0.9), 20),
            waterSlideApproximationInstructionsAboveX90(50, 1.6)))));
  }

  private WaterSlideFunctionDescriptor makeDescriptorWithThisX90(double x90point) {
    return makeRealObject(WaterSlideFunctionDescriptorGenerator.class)
        .generate(unitFraction(0.9), x90(x90point));
  }

  @Override
  protected Class<LinearlyApproximatedWaterSlideFunctionGenerator> getClassBeingTested() {
    return LinearlyApproximatedWaterSlideFunctionGenerator.class;
  }

}
