package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBIntegrationTest;
import org.junit.Test;

import java.util.function.DoubleUnaryOperator;

import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRanges.linearApproximationVarRanges;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValues.linearApproximationVarRangesAndValuesWithValuesPrecomputed;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValuesTest.linearApproximationVarRangesAndValuesMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructionsBelowX90.waterSlideApproximationInstructionsBelowX90;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptor.unscaledWaterSlideFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.highlevel.X90.x90;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInBps;
import static org.junit.Assert.assertEquals;

public class WaterSlideBelowX90ApproximationGeneratorTest
    extends RBIntegrationTest<WaterSlideBelowX90ApproximationGenerator> {

  @Test
  public void generalCase() {
    WaterSlideFunctionDescriptor descriptor = unscaledWaterSlideFunctionDescriptor(2.0, 5.0);
    DoubleUnaryOperator f = descriptor.asFunction();
    DoubleUnaryOperator fPrime = makeRealObject(WaterSlideFunctionFirstDerivativeGenerator.class).takeFirstDerivative(descriptor);

    // As x goes to infinity, the slope of the line tends to 1
    doubleExplained(1, fPrime.applyAsDouble(999_999));

    // the 4 hinge points will be 0, 0.3, 0.6, 0.9
    doubleExplained(0.3, fPrime.applyAsDouble(1.572427255));
    doubleExplained(0.6, fPrime.applyAsDouble(3.75));
    doubleExplained(0.9, fPrime.applyAsDouble(10.32370802));

    assertThat(
        makeRealObject().generate(
            descriptor,
            34.567,
            waterSlideApproximationInstructionsBelowX90(unitFraction(doubleExplained(0.9, 0.18 / 0.20)), 4)),
        linearApproximationVarRangesAndValuesMatcher(
            // Using linearApproximationVarRangesAndValuesWithValuesPrecomputed to make explicit
            // the values that get calculated upon construction inside this object (2nd arg)
            linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                // With a real parabola x^2, the derivative is 2*x, so the slope at x = 1, 2, 3, 4 etc.
                // is 2, 4, 6, 8, etc. - so the slope goes up by fixed amounts for each x += 1.
                // However, in this case we essentially start with a parabola and end up with a line,
                // so those x points aren't equally spaced with each other; the are farther and farther
                // away as x increases. This is expected and desirable.
                linearApproximationVarRanges(ImmutableList.of(
                    0.0,
                    1.5724272550821121,
                    3.75,
                    10.323708024174302)),
                ImmutableList.of(
                    doubleExplained(0.0,          f.applyAsDouble(0.0)),
                    doubleExplained(0.241424184,  f.applyAsDouble(1.572427255)),
                    doubleExplained(1.25,         f.applyAsDouble(3.75)),
                    doubleExplained(6.470786694,  f.applyAsDouble(10.32370802))),
                descriptor)));
  }

  @Test
  public void generalCase_usingRealisticProdExample_fewHingePoints() {
    // E.g. we typically decide that an asset class with a target of 10% has an x90 of +/- 5%.
    X90 x90 = x90(0.05);
    WaterSlideFunctionDescriptor descriptor = makeRealObject(WaterSlideFunctionDescriptorGenerator.class)
        .generate(unitFraction(0.9), x90);
    double d = doubleExplained(0.02421610524189263, descriptor.getDivisor());
    DoubleUnaryOperator f = descriptor.asFunction();
    DoubleUnaryOperator fPrime = makeRealObject(WaterSlideFunctionFirstDerivativeGenerator.class)
        .takeFirstDerivative(descriptor);

    // As x goes to infinity, the slope of the line tends to 1
    assertEquals(1, fPrime.applyAsDouble(999_999), 1e-8);
    // At x90, the derivative should be 90% of its final value, i.e. 0.9
    assertEquals(0.9, fPrime.applyAsDouble(x90.getRawDouble()), 1e-8);

    // the first 4 hinge points will be where f'(x) is 0, 0.3, 0.6, 0.9,
    // so the values of all 5 will be 0, 0.007615613, 0.018162079, 0.05, 34.567
    doubleExplained(0.3, fPrime.applyAsDouble(0.007615613));
    doubleExplained(0.6, fPrime.applyAsDouble(0.018162079));
    doubleExplained(0.9, fPrime.applyAsDouble(0.05));

    assertThat(
        makeRealObject().generate(
            descriptor,
            34.567,
            waterSlideApproximationInstructionsBelowX90(unitFraction(0.9), 4)),
        linearApproximationVarRangesAndValuesMatcher(
            // Using linearApproximationVarRangesAndValuesWithValuesPrecomputed to make explicit
            // the values that get calculated upon construction inside this object (2nd arg)
            linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                // With a real parabola x^2, the derivative is 2*x, so the slope at x = 1, 2, 3, 4 etc.
                // is 2, 4, 6, 8, etc. - so the slope goes up by fixed amounts for each x += 1.
                // However, in this case we essentially start with a parabola and end up with a line,
                // so those x points aren't equally spaced with each other; the are farther and farther
                // away as x increases. This is expected and desirable.
                linearApproximationVarRanges(ImmutableList.of(
                    0.0,
                    0.007615613,
                    0.018162079,
                    0.05)),
                ImmutableList.of(
                    doubleExplained(0.0,          f.applyAsDouble(0.0)),
                    doubleExplained(0.001169270,  f.applyAsDouble(0.007615613)),
                    doubleExplained(0.006054026,  f.applyAsDouble(0.018162079)),
                    doubleExplained(0.0313394503, f.applyAsDouble(0.05))),
                descriptor)));
  }

  @Test
  public void generalCase_usingRealisticProdExample_severalHingePoints_assetClassWithTarget10pct() {
    // E.g. we typically decide that an asset class with a target of 10% has an x90 of +/- 5%.
    X90 x90 = x90(0.05);
    WaterSlideFunctionDescriptor descriptor = makeRealObject(WaterSlideFunctionDescriptorGenerator.class)
        .generate(unitFraction(0.9), x90);
    double d = doubleExplained(0.02421610524189263, descriptor.getDivisor());
    DoubleUnaryOperator f = descriptor.asFunction();
    DoubleUnaryOperator fPrime = makeRealObject(WaterSlideFunctionFirstDerivativeGenerator.class)
        .takeFirstDerivative(descriptor);

    // As x goes to infinity, the slope of the line tends to 1
    assertEquals(1, fPrime.applyAsDouble(999_999), 1e-8);
    // At x90, the derivative should be 90% of its final value, i.e. 0.9
    assertEquals(0.9, fPrime.applyAsDouble(x90.getRawDouble()), 1e-8);

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
    doubleExplained(x90.getRawDouble(), unitFractionInBps(500).doubleValue());

    doubleExplained(0.1, fPrime.applyAsDouble(x10));
    doubleExplained(0.2, fPrime.applyAsDouble(x20));
    doubleExplained(0.3, fPrime.applyAsDouble(x30));
    doubleExplained(0.4, fPrime.applyAsDouble(x40));
    doubleExplained(0.5, fPrime.applyAsDouble(x50));
    doubleExplained(0.6, fPrime.applyAsDouble(x60));
    doubleExplained(0.7, fPrime.applyAsDouble(x70));
    doubleExplained(0.8, fPrime.applyAsDouble(x80));
    doubleExplained(0.9, fPrime.applyAsDouble(doubleExplained(x90.getRawDouble(), 0.05)));

    assertThat(
        makeRealObject().generate(
            descriptor,
            34.567,
            waterSlideApproximationInstructionsBelowX90(unitFraction(0.9), 10)),
        linearApproximationVarRangesAndValuesMatcher(
            // Using linearApproximationVarRangesAndValuesWithValuesPrecomputed to make explicit
            // the values that get calculated upon construction inside this object (2nd arg)
            linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                // With a real parabola x^2, the derivative is 2*x, so the slope at x = 1, 2, 3, 4 etc.
                // is 2, 4, 6, 8, etc. - so the slope goes up by fixed amounts for each x += 1.
                // However, in this case we essentially start with a parabola and end up with a line,
                // so those x points aren't equally spaced with each other; the are farther and farther
                // away as x increases. This is expected and desirable.
                linearApproximationVarRanges(ImmutableList.of(
                    0.0, x10, x20, x30, x40, x50, x60, x70, x80, x90.getRawDouble())),
                ImmutableList.of(
                    doubleExplained(0,            f.applyAsDouble(0.0)),
                    doubleExplained(0.000121996,  f.applyAsDouble(x10)),
                    doubleExplained(0.000499354,  f.applyAsDouble(x20)),
                    doubleExplained(0.001169270,  f.applyAsDouble(x30)),
                    doubleExplained(0.002205832,  f.applyAsDouble(x40)),
                    doubleExplained(0.003746245,  f.applyAsDouble(x50)),
                    doubleExplained(0.006054026,  f.applyAsDouble(x60)),
                    doubleExplained(0.009693225,  f.applyAsDouble(x70)),
                    doubleExplained(0.016144070,  f.applyAsDouble(x80)),
                    doubleExplained(0.031339450,  f.applyAsDouble(x90.getRawDouble()))),
                descriptor)));
  }

  @Test
  public void generalCase_usingRealisticProdExample_severalHingePoints_assetClassWithTarget40pct() {
    // E.g. we typically decide that an asset class with a target of 40% has an x90 of +/- 10%.
    X90 x90 = x90(0.10);
    WaterSlideFunctionDescriptor descriptor = makeRealObject(WaterSlideFunctionDescriptorGenerator.class)
        .generate(unitFraction(0.9), x90);
    double d = doubleExplained(0.04843221048378526, descriptor.getDivisor());
    DoubleUnaryOperator f = descriptor.asFunction();
    DoubleUnaryOperator fPrime = makeRealObject(WaterSlideFunctionFirstDerivativeGenerator.class)
        .takeFirstDerivative(descriptor);

    // As x goes to infinity, the slope of the line tends to 1
    assertEquals(1, fPrime.applyAsDouble(999_999), 1e-8);
    // At x90, the derivative should be 90% of its final value, i.e. 0.9
    assertEquals(0.9, fPrime.applyAsDouble(x90.getRawDouble()), 1e-8);

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
    doubleExplained(x90.getRawDouble(), unitFractionInBps(1_000).doubleValue());

    doubleExplained(0.1, fPrime.applyAsDouble(x10));
    doubleExplained(0.2, fPrime.applyAsDouble(x20));
    doubleExplained(0.3, fPrime.applyAsDouble(x30));
    doubleExplained(0.4, fPrime.applyAsDouble(x40));
    doubleExplained(0.5, fPrime.applyAsDouble(x50));
    doubleExplained(0.6, fPrime.applyAsDouble(x60));
    doubleExplained(0.7, fPrime.applyAsDouble(x70));
    doubleExplained(0.8, fPrime.applyAsDouble(x80));
    doubleExplained(0.9, fPrime.applyAsDouble(doubleExplained(x90.getRawDouble(), 0.1)));

    assertThat(
        makeRealObject().generate(
            descriptor,
            34.567,
            waterSlideApproximationInstructionsBelowX90(unitFraction(0.9), 10)),
        linearApproximationVarRangesAndValuesMatcher(
            // Using linearApproximationVarRangesAndValuesWithValuesPrecomputed to make explicit
            // the values that get calculated upon construction inside this object (2nd arg)
            linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                // With a real parabola x^2, the derivative is 2*x, so the slope at x = 1, 2, 3, 4 etc.
                // is 2, 4, 6, 8, etc. - so the slope goes up by fixed amounts for each x += 1.
                // However, in this case we essentially start with a parabola and end up with a line,
                // so those x points aren't equally spaced with each other; the are farther and farther
                // away as x increases. This is expected and desirable.
                linearApproximationVarRanges(ImmutableList.of(
                    0.0, x10, x20, x30, x40, x50, x60, x70, x80, x90.getRawDouble())),
                ImmutableList.of(
                    doubleExplained( 0,            f.applyAsDouble(0.0)),
                    doubleExplained( 0.000243993,  f.applyAsDouble(x10)),
                    doubleExplained( 0.000998707,  f.applyAsDouble(x20)),
                    doubleExplained( 0.002338541,  f.applyAsDouble(x30)),
                    doubleExplained( 0.004411663,  f.applyAsDouble(x40)),
                    doubleExplained( 0.007492489,  f.applyAsDouble(x50)),
                    doubleExplained( 0.012108052,  f.applyAsDouble(x60)),
                    doubleExplained( 0.019386449,  f.applyAsDouble(x70)),
                    doubleExplained( 0.032288140,  f.applyAsDouble(x80)),
                    doubleExplained( 0.062678901,  f.applyAsDouble(x90.getRawDouble()))),
                descriptor)));
  }

  @Test
  public void hasNoInteriorHingePoints() {
    WaterSlideFunctionDescriptor descriptor = unscaledWaterSlideFunctionDescriptor(2.0, 5.0);
    DoubleUnaryOperator f = descriptor.asFunction();
    DoubleUnaryOperator fPrime = makeRealObject(WaterSlideFunctionFirstDerivativeGenerator.class).takeFirstDerivative(descriptor);

    // As x goes to infinity, the slope of the line tends to 1
    doubleExplained(1, fPrime.applyAsDouble(999_999));

    doubleExplained(0.9, fPrime.applyAsDouble(10.323708024174302));

    assertThat(
        makeRealObject().generate(
            descriptor,
            34.567,
            waterSlideApproximationInstructionsBelowX90(unitFraction(0.9), 2)),
        linearApproximationVarRangesAndValuesMatcher(
            // Using linearApproximationVarRangesAndValuesWithValuesPrecomputed to make explicit
            // the values that get calculated upon construction inside this object (2nd arg)
            linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                linearApproximationVarRanges(ImmutableList.of(
                    0.0,
                    10.323708024174302)),
                ImmutableList.of(
                    doubleExplained( 0.0,          f.applyAsDouble(0.0)),
                    doubleExplained( 6.47078669,   f.applyAsDouble(10.323708024))),
                descriptor)));
  }

  @Override
  protected Class<WaterSlideBelowX90ApproximationGenerator> getClassBeingTested() {
    return WaterSlideBelowX90ApproximationGenerator.class;
  }

}
