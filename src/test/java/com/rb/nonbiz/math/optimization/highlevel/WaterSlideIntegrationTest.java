package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.DoubleUnaryOperator;

import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructionsBelowX90.waterSlideApproximationInstructionsBelowX90;
import static com.rb.nonbiz.math.optimization.highlevel.X90.x90;
import static com.rb.nonbiz.math.optimization.highlevel.X90CalculationInstructionsForDirectIndexing.x90CalculationInstructionsForDirectIndexingWithoutOverrides;
import static com.rb.nonbiz.math.optimization.highlevel.X90Test.x90Matcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.doubleListMatcher;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBIntegrationTest.makeRealObject;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInPct;
import static org.junit.Assert.assertEquals;

public class WaterSlideIntegrationTest {

  @Test
  public void sanityCheckStuff() {
    X90 x90for3pct = makeRealObject(X90Calculator.class).calculateX90WithoutOverrides(
        unitFractionInPct(3), // e.g. AAPL at 3% will have its x90 at 1.5%
        x90CalculationInstructionsForDirectIndexingWithoutOverrides(unitFraction(0.5)));
    assertThat(
        x90for3pct,
        x90Matcher(x90(0.015)));

    WaterSlideFunctionDescriptor functionDescriptor = makeRealObject(WaterSlideFunctionDescriptorGenerator.class)
        .generate(unitFraction(0.9), x90for3pct);
    DoubleUnaryOperator firstDerivative = makeRealObject(WaterSlideFunctionFirstDerivativeGenerator.class)
        .takeFirstDerivative(functionDescriptor);
    assertEquals(
        "The waterslide should be 0 and flat around x=0 (i.e. no misallocation)",
        0.0,
        functionDescriptor.asFunction().applyAsDouble(0),
        1e-8);
    assertEquals(
        "The derivative should be 0 at x=0 because the waterslide is flat at 0",
        0.0,
        firstDerivative.applyAsDouble(0),
        1e-8);

    assertEquals(
        "By construction, the derivative evaluated at x90 should be 0.9",
        0.9,
        firstDerivative.applyAsDouble(doubleExplained(0.015, x90for3pct.getRawDouble())),
        1e-8);

    BiConsumer<Integer, List<Double>> assertHingePoints = (numHingePoints, expectedHingePointsInBps) ->
        assertThat(
            makeRealObject(WaterSlideBelowX90ApproximationGenerator.class)
                .generate(functionDescriptor, 1.01, waterSlideApproximationInstructionsBelowX90(unitFraction(0.9), numHingePoints))
                .getLinearApproximationVarRanges()
                .getHingePoints(),
            doubleListMatcher(Lists.transform(expectedHingePointsInBps, v -> v / 10_000.0), 1e-6));

    // With 2 hinge points, we are really only creating hinge points for x0 = 0 and x90 = 0.015.
    // With 3 hinge points, we are creating hinge points for x0, x45, x90
    // With 4 hinge points, we are creating hinge points for x0, x30, x60, x90
    // With 7 hinge points, we are creating hinge points for x0, x15, x30, x45, x60, x75, x90

    //                                           x0   x15    x30    x45    x60    x75    x90
    assertHingePoints.accept(2, ImmutableList.of(0.0,                                    150.0));
    assertHingePoints.accept(3, ImmutableList.of(0.0,               36.61,               150.0));
    assertHingePoints.accept(4, ImmutableList.of(0.0,        22.84,        54.49,        150.0));
    assertHingePoints.accept(7, ImmutableList.of(0.0, 11.02, 22.84, 36.61, 54.49, 82.38, 150.0));
  }

}
