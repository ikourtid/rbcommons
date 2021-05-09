package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRanges.linearApproximationVarRanges;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValues.linearApproximationVarRangesAndValuesWithValuesPrecomputed;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValuesTest.linearApproximationVarRangesAndValuesMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.LinearlyApproximatedWaterSlideFunction.linearlyApproximatedWaterSlideFunction;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptor.unscaledWaterSlideFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptor.waterSlideFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptorTest.waterSlideFunctionDescriptorMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.X90.x90;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBIntegrationTest.makeRealObject;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class LinearlyApproximatedWaterSlideFunctionTest extends RBTestMatcher<LinearlyApproximatedWaterSlideFunction> {

  @Test
  public void hingePointEvaluationsMustMatch() {
    WaterSlideFunctionDescriptor functionDescriptor = unscaledWaterSlideFunctionDescriptor(2, 5);
    LinearApproximationVarRangesAndValues validRangesAndValues = linearApproximationVarRangesAndValuesWithValuesPrecomputed(
        linearApproximationVarRanges(ImmutableList.of(
            0.0,
            1.0,
            2.0,
            3.0)),
        ImmutableList.of(
            doubleExplained(0,             5.0 * (Math.sqrt(1 + Math.pow(0 / 5.0, 2)) - 1)),
            doubleExplained(0.09901951359, 5.0 * (Math.sqrt(1 + Math.pow(1 / 5.0, 2)) - 1)),
            doubleExplained(0.38516480713, 5.0 * (Math.sqrt(1 + Math.pow(2 / 5.0, 2)) - 1)),
            doubleExplained(0.83095189484, 5.0 * (Math.sqrt(1 + Math.pow(3 / 5.0, 2)) - 1))),
        functionDescriptor);
    LinearlyApproximatedWaterSlideFunction doesNotThrow =
        linearlyApproximatedWaterSlideFunction(functionDescriptor, validRangesAndValues);
    double e = 1e-4; // large-ish epsilon
    assertIllegalArgumentException( () -> linearlyApproximatedWaterSlideFunction(unscaledWaterSlideFunctionDescriptor(2 + e, 5), validRangesAndValues));
    assertIllegalArgumentException( () -> linearlyApproximatedWaterSlideFunction(unscaledWaterSlideFunctionDescriptor(2 - e, 5), validRangesAndValues));
    assertIllegalArgumentException( () -> linearlyApproximatedWaterSlideFunction(unscaledWaterSlideFunctionDescriptor(2, 5 + e), validRangesAndValues));
    assertIllegalArgumentException( () -> linearlyApproximatedWaterSlideFunction(unscaledWaterSlideFunctionDescriptor(2, 5 - e), validRangesAndValues));
  }

  /**
   * This uses a more realistic example of a water slide function descriptor that we could use in practice
   * for an asset class with a target of 10% where its x90 would be +/- 5%.
   * However, the extra "realistic-ness" doesn't add much value here, because all we're doing is checking that
   * f(x) matches the expected value. It doesn't check that the hinge points are generated at the appropriate
   * distances.
   */
  @Test
  public void hingePointEvaluationsMustMatch_moreRealisticExample() {
    WaterSlideFunctionDescriptor functionDescriptor = makeRealObject(WaterSlideFunctionDescriptorGenerator.class)
        .generate(unitFraction(0.9), x90(0.05));
    double d = doubleExplained(0.02421610524189263, functionDescriptor.getDivisor());

    LinearApproximationVarRangesAndValues validRangesAndValues = linearApproximationVarRangesAndValuesWithValuesPrecomputed(
        linearApproximationVarRanges(ImmutableList.of(
            0.0,
            1.0,
            2.0,
            3.0)),
        ImmutableList.of(
            doubleExplained(0,             d * (Math.sqrt(1 + Math.pow(0 / d, 2)) - 1)),
            doubleExplained(0.97607706166, d * (Math.sqrt(1 + Math.pow(1 / d, 2)) - 1)),
            doubleExplained(1.97593049432, d * (Math.sqrt(1 + Math.pow(2 / d, 2)) - 1)),
            doubleExplained(2.97588162979, d * (Math.sqrt(1 + Math.pow(3 / d, 2)) - 1))),
        functionDescriptor);
    LinearlyApproximatedWaterSlideFunction doesNotThrow =
        linearlyApproximatedWaterSlideFunction(functionDescriptor, validRangesAndValues);
    double e = 1e-4; // large-ish epsilon
    assertIllegalArgumentException( () -> linearlyApproximatedWaterSlideFunction(unscaledWaterSlideFunctionDescriptor(2 + e, 5), validRangesAndValues));
    assertIllegalArgumentException( () -> linearlyApproximatedWaterSlideFunction(unscaledWaterSlideFunctionDescriptor(2 - e, 5), validRangesAndValues));
    assertIllegalArgumentException( () -> linearlyApproximatedWaterSlideFunction(unscaledWaterSlideFunctionDescriptor(2, 5 + e), validRangesAndValues));
    assertIllegalArgumentException( () -> linearlyApproximatedWaterSlideFunction(unscaledWaterSlideFunctionDescriptor(2, 5 - e), validRangesAndValues));
  }

  @Override
  public LinearlyApproximatedWaterSlideFunction makeTrivialObject() {
    return linearlyApproximatedWaterSlideFunction(
        unscaledWaterSlideFunctionDescriptor(2, 1),
        // Just using static ctor to make explicit the values that get calculated upon construction inside this object (2nd arg)
        linearApproximationVarRangesAndValuesWithValuesPrecomputed(
            linearApproximationVarRanges(ImmutableList.of(0.0, 1.0)),
            ImmutableList.of(
                doubleExplained(0,           Math.sqrt(1 + Math.pow(0, 2)) - 1),
                doubleExplained(0.414213562, Math.sqrt(1 + Math.pow(1, 2)) - 1)),
            unscaledWaterSlideFunctionDescriptor(2, 1)));
  }

  @Override
  public LinearlyApproximatedWaterSlideFunction makeNontrivialObject() {
    return linearlyApproximatedWaterSlideFunction(
        unscaledWaterSlideFunctionDescriptor(2, 5),
        // Just using static ctor to make explicit the values that get calculated upon construction inside this object (2nd arg)
        linearApproximationVarRangesAndValuesWithValuesPrecomputed(
            linearApproximationVarRanges(ImmutableList.of(
                0.0,
                1.0,
                2.0,
                3.0)),
            ImmutableList.of(
                doubleExplained(0,             5.0 * (Math.sqrt(1 + Math.pow(0 / 5.0, 2)) - 1)),
                doubleExplained(0.09901951359, 5.0 * (Math.sqrt(1 + Math.pow(1 / 5.0, 2)) - 1)),
                doubleExplained(0.38516480713, 5.0 * (Math.sqrt(1 + Math.pow(2 / 5.0, 2)) - 1)),
                doubleExplained(0.83095189484, 5.0 * (Math.sqrt(1 + Math.pow(3 / 5.0, 2)) - 1))),
            unscaledWaterSlideFunctionDescriptor(2, 5)));
  }

  @Override
  public LinearlyApproximatedWaterSlideFunction makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    WaterSlideFunctionDescriptor functionDescriptor = waterSlideFunctionDescriptor(2 + e, 5 + e, positiveMultiplier(1 + e));
    return linearlyApproximatedWaterSlideFunction(
        functionDescriptor,
        // Just using static ctor to make explicit the values that get calculated upon construction inside this object (2nd arg)
        linearApproximationVarRangesAndValuesWithValuesPrecomputed(
            linearApproximationVarRanges(ImmutableList.of(
                0.0, // e will intentionally give an exception - see prod code
                1.0 + e,
                2.0 + e,
                3.0 + e)),
            ImmutableList.of(
                0.0, // this has to be actually 0 (not epsilon-almost-0), since it is not calculated numerically.
                0.09901951359 + e,
                0.38516480713 + e,
                0.83095189484 + e),
            functionDescriptor));
  }

  @Override
  protected boolean willMatch(LinearlyApproximatedWaterSlideFunction expected,
                              LinearlyApproximatedWaterSlideFunction actual) {
    return linearlyApproximatedWaterSlideFunctionMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<LinearlyApproximatedWaterSlideFunction> linearlyApproximatedWaterSlideFunctionMatcher(
      LinearlyApproximatedWaterSlideFunction expected) {
    return makeMatcher(expected,
        match(v -> v.getWaterSlideFunctionDescriptor(),          f -> waterSlideFunctionDescriptorMatcher(f)),
        match(v -> v.getLinearApproximationVarRangesAndValues(), f -> linearApproximationVarRangesAndValuesMatcher(f)));
  }

}
