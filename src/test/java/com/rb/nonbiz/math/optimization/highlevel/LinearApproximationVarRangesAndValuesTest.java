package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import static com.rb.nonbiz.math.optimization.highlevel.ArbitraryFunctionDescriptor.arbitraryFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.highlevel.FunctionDescriptorTest.functionDescriptorMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRanges.linearApproximationVarRanges;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValues.linearApproximationVarRangesAndValues;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValues.linearApproximationVarRangesAndValuesWithValuesPrecomputed;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesTest.linearApproximationVarRangesMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.QuadraticFunctionDescriptor.quadraticFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptor.unscaledWaterSlideFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptor.waterSlideFunctionDescriptor;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.doubleListMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinearApproximationVarRangesAndValuesTest extends RBTestMatcher<LinearApproximationVarRangesAndValues> {

  public static LinearApproximationVarRangesAndValues testLinearApproximationVarRangesAndValuesWithSeed(double seed) {
    // Simulates a typical scenario where we approximate x^2
    // with range [0, 1], initial step 0.01, step multiplier 3
    return linearApproximationVarRangesAndValuesWithValuesPrecomputed(
        linearApproximationVarRanges(ImmutableList.of(
            seed + 0.0,
            seed + 0.01,
            seed + 0.03,
            seed + 0.09,
            seed + 0.27,
            seed + 0.81,
            seed + 1.00)),
        ImmutableList.of(
            seed + 0.0,
            seed + 0.0001,
            seed + 0.0009,
            seed + 0.0081,
            seed + 0.0729,
            seed + 0.6561,
            seed + 1.00),
        quadraticFunctionDescriptor());
  }

  public static LinearApproximationVarRangesAndValues singleSegmentLinearApproximationVarRangesAndValues(
      ClosedRange<Double> onlyLineSegment, double slope) {
    return linearApproximationVarRangesAndValuesWithValuesPrecomputed(
        linearApproximationVarRanges(ImmutableList.of(
            onlyLineSegment.lowerEndpoint(),
            onlyLineSegment.upperEndpoint())),
        ImmutableList.of(
            slope * onlyLineSegment.lowerEndpoint(),
            slope * onlyLineSegment.upperEndpoint()),
        arbitraryFunctionDescriptor(v -> slope * v));
  }

  // Shorthand
  public static LinearApproximationVarRangesAndValues linearApproximationVarRangesAndValuesForQuadratic(
      List<Double> hingePoints) {
    return linearApproximationVarRangesAndValues(
        linearApproximationVarRanges(hingePoints), quadraticFunctionDescriptor());
  }

  // Shorthand
  public static LinearApproximationVarRangesAndValues linearApproximationVarRangesAndValuesForQuadratic(
      Double...hingePoints) {
    return linearApproximationVarRangesAndValues(
        linearApproximationVarRanges(Arrays.asList(hingePoints)), quadraticFunctionDescriptor());
  }

  // Shorthand
  public static LinearApproximationVarRangesAndValues linearApproximationVarRangesAndValuesForArbitraryFunction(
      List<Double> hingePoints, DoubleUnaryOperator f) {
    return linearApproximationVarRangesAndValues(
        linearApproximationVarRanges(hingePoints), arbitraryFunctionDescriptor(f));
  }

  @Test
  public void unequalSizes_throws() {
    assertIllegalArgumentException( () -> linearApproximationVarRangesAndValuesWithValuesPrecomputed(
        linearApproximationVarRanges(ImmutableList.of(0.0, 10.0)),
        ImmutableList.of(0.0, 100.0, 400.0),
        quadraticFunctionDescriptor()));
    assertIllegalArgumentException( () -> linearApproximationVarRangesAndValuesWithValuesPrecomputed(
        linearApproximationVarRanges(ImmutableList.of(0.0, 10.0, 20.0)),
        ImmutableList.of(0.0, 100.0),
        quadraticFunctionDescriptor()));
  }

  @Test
  public void testApproximatesThisFunction() {
    LinearApproximationVarRangesAndValues quadraticApproximation = linearApproximationVarRangesAndValuesWithValuesPrecomputed(
        linearApproximationVarRanges(ImmutableList.of(
            0.0, 0.01,   0.03,   0.09,   0.27,   0.81,   1.00)),
        ImmutableList.of(
            0.0, 0.0001, 0.0009, 0.0081, 0.0729, 0.6561, 1.00),
        quadraticFunctionDescriptor());
    assertFalse(quadraticApproximation.approximatesThisFunction(x -> Math.pow(x, 1.9), 1e-8));
    assertTrue( quadraticApproximation.approximatesThisFunction(x -> Math.pow(x, 2),   1e-8));
    assertFalse(quadraticApproximation.approximatesThisFunction(x -> Math.pow(x, 2.1), 1e-8));
  }

  @Test
  public void testGetAllSlopesInPiecewiseLinearApproximations_hasNoMultiplier() {
    DoubleUnaryOperator f = x -> Math.sqrt(1 + x * x) - 1;

    assertThat(
        linearApproximationVarRangesAndValues(
            linearApproximationVarRanges(ImmutableList.of(0.0, 0.2, 0.5, 1.0)),
            unscaledWaterSlideFunctionDescriptor(2, 1)) // sqrt(1 + x^2) - 1
            .getAllSlopesInPiecewiseLinearApproximations(),
        doubleListMatcher(
            ImmutableList.of(
                doubleExplained(0.0990195135,
                    (doubleExplained(0.0198039027, f.applyAsDouble(0.2)) - doubleExplained(0, f.applyAsDouble(0.0))) / (0.2 - 0.0)),
                doubleExplained(0.327433621,
                    (doubleExplained(0.118033989, f.applyAsDouble(0.5)) - doubleExplained(0.0198039027, f.applyAsDouble(0.2))) / (0.5 - 0.2)),
                doubleExplained(0.592359146,
                    (doubleExplained(0.414213562, f.applyAsDouble(1.0)) - doubleExplained(0.118033989, f.applyAsDouble(0.5))) / (1.0 - 0.5))),
            1e-8));
  }

  @Test
  public void testGetAllSlopesInPiecewiseLinearApproximations_hasMultiplierOfThree() {
    DoubleUnaryOperator f = x -> 3 * (Math.sqrt(1 + x * x) - 1);

    assertThat(
        "All slopes are 3x those in testGetAllSlopesInPiecewiseLinearApproximations_hasNoMultiplier",
        linearApproximationVarRangesAndValues(
            linearApproximationVarRanges(ImmutableList.of(0.0, 0.2, 0.5, 1.0)),
            waterSlideFunctionDescriptor(2, 1, positiveMultiplier(3))) // 3 * (sqrt(1 + x^2) - 1)
            .getAllSlopesInPiecewiseLinearApproximations(),
        doubleListMatcher(
            ImmutableList.of(
                doubleExplained(
                    0.297058541,
                    3 * 0.0990195135,
                    (doubleExplained(3 * 0.0198039027, f.applyAsDouble(0.2)) - doubleExplained(0, f.applyAsDouble(0.0))) / (0.2 - 0.0)),
                doubleExplained(
                    0.982300863,
                    3 * 0.327433621,
                    (doubleExplained(3 * 0.118033989, f.applyAsDouble(0.5)) - doubleExplained(3 * 0.0198039027, f.applyAsDouble(0.2))) / (0.5 - 0.2)),
                doubleExplained(
                    1.77707744,
                    3 * 0.592359146,
                    (doubleExplained(3 * 0.414213562, f.applyAsDouble(1.0)) - doubleExplained(3 * 0.118033989, f.applyAsDouble(0.5))) / (1.0 - 0.5))),
            1e-8));
  }

  @Test
  public void testThatAnyWaterslideApproximationWithoutScalingTrendsTo45degrees() {
    for (double divisor : ImmutableList.of(0.1, 0.9, 1.0, 1.1, 2.0)) {
      assertEquals(
          Iterables.getLast(
              linearApproximationVarRangesAndValues(
                  linearApproximationVarRanges(ImmutableList.of(0.0, 10.0, 100.0)),
                  unscaledWaterSlideFunctionDescriptor(2, divisor)) // d * (sqrt(1 + (x / d) ^ 2) - 1)
                  .getAllSlopesInPiecewiseLinearApproximations()),
          1,
          0.01); // close enough, for purposes of this test
    }
  }

  @Test
  public void testThatAnyWaterslideApproximationWithScalingTrendsToSlopeEqualToScaler() {
    for (double divisor : ImmutableList.of(0.1, 0.9, 1.0, 1.1, 2.0)) {
      for (double scaler: ImmutableList.of(0.98, 1.0, 1.02)) {
        WaterSlideFunctionDescriptor functionDescriptor = waterSlideFunctionDescriptor(2, divisor, positiveMultiplier(scaler));
        assertEquals(
            Iterables.getLast(
                linearApproximationVarRangesAndValues(
                    linearApproximationVarRanges(ImmutableList.of(0.0, 10.0, 100.0)),
                    functionDescriptor) // scaler * d * (sqrt(1 + (x / d) ^ 2) - 1)
                    .getAllSlopesInPiecewiseLinearApproximations()),
            scaler,
            0.01); // close enough, for purposes of this test
        assertEquals(
            functionDescriptor.getSlopeAtInfiniteX(),
            scaler,
            1e-8);
      }
    }
  }

  @Override
  public LinearApproximationVarRangesAndValues makeTrivialObject() {
    return linearApproximationVarRangesAndValuesForQuadratic(0.0, 1.0);
  }

  @Override
  public LinearApproximationVarRangesAndValues makeNontrivialObject() {
    return testLinearApproximationVarRangesAndValuesWithSeed(ZERO_SEED);
  }

  @Override
  public LinearApproximationVarRangesAndValues makeMatchingNontrivialObject() {
    return testLinearApproximationVarRangesAndValuesWithSeed(EPSILON_SEED);
  }

  @Override
  protected boolean willMatch(LinearApproximationVarRangesAndValues expected,
                              LinearApproximationVarRangesAndValues actual) {
    return linearApproximationVarRangesAndValuesMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<LinearApproximationVarRangesAndValues> linearApproximationVarRangesAndValuesMatcher(
      LinearApproximationVarRangesAndValues expected) {
    return makeMatcher(expected,
        match(v -> v.getLinearApproximationVarRanges(),   f -> linearApproximationVarRangesMatcher(f)),
        match(v -> v.getFunctionValuesAtRangeEndpoints(), f -> doubleListMatcher(f, 1e-8)),
        match(v -> v.getFunctionDescriptor(),             f -> functionDescriptorMatcher(f)));
  }

}
