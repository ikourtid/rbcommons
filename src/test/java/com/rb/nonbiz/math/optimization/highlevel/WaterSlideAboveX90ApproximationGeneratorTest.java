package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBIntegrationTest;
import org.junit.Test;

import java.util.function.DoubleUnaryOperator;

import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRanges.linearApproximationVarRanges;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValues.linearApproximationVarRangesAndValuesWithValuesPrecomputed;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValuesTest.linearApproximationVarRangesAndValuesMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructionsAboveX90.waterSlideApproximationInstructionsAboveX90;
import static com.rb.nonbiz.math.optimization.highlevel.X90.x90;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class WaterSlideAboveX90ApproximationGeneratorTest
    extends RBIntegrationTest<WaterSlideAboveX90ApproximationGenerator> {

  @Test
  public void generalCase() {
    WaterSlideFunctionDescriptor descriptor = makeRealObject(WaterSlideFunctionDescriptorGenerator.class)
        .generate(unitFraction(0.9), x90(0.05));
    DoubleUnaryOperator f = descriptor.asFunction();
    assertThat(
        makeRealObject().generate(
            waterSlideApproximationInstructionsAboveX90(50.0, 2.0),
            descriptor, // x90 = 5%
            Range.closed(0.03, 0.05)), // imaginary last range in the approximation below the x90 (not shown)
        linearApproximationVarRangesAndValuesMatcher(
            // Using linearApproximationVarRangesAndValuesWithValuesPrecomputed to show explicitly
            // the values that would otherwise get computed and stored inside this object.
            linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                linearApproximationVarRanges(ImmutableList.of(
                    0.05,
                    doubleExplained(0.07, 0.05 + (0.05 - 0.03)),
                    doubleExplained(0.11, 0.07 + 2 * (0.07 - 0.05)),
                    doubleExplained(0.19, 0.11 + 2 * (0.11 - 0.07)),
                    doubleExplained(0.35, 0.19 + 2 * (0.19 - 0.11)),
                    doubleExplained(0.67, 0.35 + 2 * (0.35 - 0.19)),
                    1.0,
                    50.0)),
                ImmutableList.of(
                    doubleExplained(0.03_133945031366292,  f.applyAsDouble(0.05)),
                    doubleExplained(0.04_9854265035880516, f.applyAsDouble(0.07)),
                    doubleExplained(0.08_841790273565571,  f.applyAsDouble(0.11)),
                    doubleExplained(0.16_73208879293651,   f.applyAsDouble(0.19)),
                    doubleExplained(0.32_662063706581723,  f.applyAsDouble(0.35)),
                    doubleExplained(0.64_62213786095747,   f.applyAsDouble(0.67)),
                    doubleExplained(0.97_6077061661234,    f.applyAsDouble(1.0)),
                    doubleExplained(49.97578975895529,     f.applyAsDouble(50.0))),
                descriptor)));
  }

  @Override
  protected Class<WaterSlideAboveX90ApproximationGenerator> getClassBeingTested() {
    return WaterSlideAboveX90ApproximationGenerator.class;
  }

}
