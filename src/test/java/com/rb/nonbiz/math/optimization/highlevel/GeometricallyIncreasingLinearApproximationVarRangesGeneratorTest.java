package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBIntegrationTest;
import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.math.optimization.highlevel.GeometricallyIncreasingRangesGenerationInstructions.GeometricallyIncreasingRangesGenerationInstructionsBuilder.geometricallyIncreasingRangesGenerationInstructionsBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRanges.linearApproximationVarRanges;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesTest.linearApproximationVarRangesMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

public class GeometricallyIncreasingLinearApproximationVarRangesGeneratorTest
    extends RBIntegrationTest<GeometricallyIncreasingLinearApproximationVarRangesGenerator> {

  @Test
  public void testRealisticValues() {
    // This is more of a 'data exploration' type of test; we want to find reasonable values for Issue #725.
    // We want to create a linear approximation for the section between the x90 (e.g. misallocation of 5%) and 100%.
    BiConsumer<Double, List<Double>> asserter = (x90, expectedHingePoints) -> assertThat(
        makeRealObject().calculateGeometricallyIncreasingRanges(
            geometricallyIncreasingRangesGenerationInstructionsBuilder()
                // x80 / x90 = 0.6457628064343259; #see WaterSlideFunctionDescriptorGeneratorTest.
                // Assuming the last line segment in the linear approximation between 0 and x90 is one that goes
                // from x80 to x90, the following will describe its length.
                .setInitialStep((1 - 0.6457628064343259)* x90)
                .setStepMultiplier(1.5)
                .setBoundsForOriginalExpression(closedRange(x90, 1.0))
                .approximationStartsAtLowerBound()
                .doNotStopMultiplyingStepsUntilEndOfInterval()
                .build()),
        linearApproximationVarRangesMatcher(
            linearApproximationVarRanges(expectedHingePoints)));

    // underscores are weirdly placed, but it helps view the percentages.
    // this would be for a case where x90 = 5%. This is reasonable. The code will first approximate
    // the misallocation from 0 to 5% using a breakdown as per the ratios mentioned in
    // WaterSlideFunctionDescriptorGeneratorTest:
    // 0, 24, 49, 76, 106, 140, 181, 237, 323, 500 bps
    // Then, we will separately create this geometrically-increasing misallocation (without regard to the
    // derivatives and/or the curvature of the function) up to 100% misallocation.
    // Note that the length of the first line segment in this geometric approximation is 177 bps,
    // same as the length of the last line segment in the 'water slide' approximation from 0 to 5%.
    // This doesn't *have* to be this way, but it's more reasonable. We don't want to have much more or much less
    // accuracy once we switch from one approximation to the other. Having the same accuracy is a good compromise.
    //
    // From that point on, we will approximate the misallocation from 100% to 5,000% with a single line segment.
    asserter.accept(0.05, ImmutableList.of(
        0.05,
        0.06_77118596782837,
        0.09_427964919570926,
        0.13_413133347184758,
        0.19_390885988605508,
        0.28_357514950736634,
        0.41_80745839393332,
        0.61_98237355872835,
        0.92_2447463059209,
        1.0));

    // The following would be for a case where x90 = 10%
    // Same comments as above apply here, except that the approximation from 0% to 10% (not shown in this test)
    // the applicable numbers are the previous numbers, but times 2:
    // 0, 48, 98, 152, 212, 280, 362, 474, 646, 1000 bps
    asserter.accept(0.1, ImmutableList.of(
        0.1,
        0.13_54237193565674,
        0.18_855929839141852,
        0.26_826266694369516,
        0.38_781771977211016,
        0.56_71502990147327,
        0.83_61491678786664,
        1.0));
  }

  @Test
  public void lowerBoundIsZero() {
    Consumer<UnaryOperator<GeometricallyIncreasingRangesGenerationInstructionsBuilder>> asserter = f ->
        assertThat(
            makeRealObject().calculateGeometricallyIncreasingRanges(
                f.apply(geometricallyIncreasingRangesGenerationInstructionsBuilder()
                    .setInitialStep(0.01)
                    .setStepMultiplier(2.0)
                    .setBoundsForOriginalExpression(closedRange(0.0, 1.0)))
                    .doNotStopMultiplyingStepsUntilEndOfInterval()
                    .build()),
            linearApproximationVarRangesMatcher(
                linearApproximationVarRanges(ImmutableList.of(
                    0.0, 0.01, 0.03, 0.07, 0.15, 0.31, 0.63, 1.00))));
    asserter.accept(f -> f.approximationStartsAt0());
    asserter.accept(f -> f.approximationStartsAtLowerBound());
  }

  @Test
  public void lowerBoundIsExactlyOnBoundary_approximationStartsAt0() {
    assertThat(
        makeRealObject().calculateGeometricallyIncreasingRanges(
            geometricallyIncreasingRangesGenerationInstructionsBuilder()
                .setInitialStep(0.01)
                .setStepMultiplier(2.0)
                .setBoundsForOriginalExpression(closedRange(0.03, 1.0))
                .approximationStartsAt0()
                .doNotStopMultiplyingStepsUntilEndOfInterval()
                .build()),
        linearApproximationVarRangesMatcher(
            linearApproximationVarRanges(ImmutableList.of(
                0.03, 0.07, 0.15, 0.31, 0.63, 1.00))));
  }

  @Test
  public void approximationStartsAtLowerBound_lastSegmentDoesNotFallOnUpperBound() {
    assertThat(
        makeRealObject().calculateGeometricallyIncreasingRanges(
            geometricallyIncreasingRangesGenerationInstructionsBuilder()
                .setInitialStep(0.01)
                .setStepMultiplier(2.0)
                .setBoundsForOriginalExpression(closedRange(0.03, 1.0))
                .approximationStartsAtLowerBound()
                .doNotStopMultiplyingStepsUntilEndOfInterval()
                .build()),
        linearApproximationVarRangesMatcher(
            linearApproximationVarRanges(ImmutableList.of(
                0.03, 0.04, 0.06, 0.10, 0.18, 0.34, 0.66, 1.00))));
  }

  @Test
  public void approximationStartsAtLowerBound_lastSegmentFallsOnUpperBound() {
    assertThat(
        makeRealObject().calculateGeometricallyIncreasingRanges(
            geometricallyIncreasingRangesGenerationInstructionsBuilder()
                .setInitialStep(0.01)
                .setStepMultiplier(2.0)
                .setBoundsForOriginalExpression(closedRange(0.03, 0.66))
                .approximationStartsAtLowerBound()
                .doNotStopMultiplyingStepsUntilEndOfInterval()
                .build()),
        linearApproximationVarRangesMatcher(
            linearApproximationVarRanges(ImmutableList.of(
                0.03, 0.04, 0.06, 0.10, 0.18, 0.34, 0.66))));
  }

  /**
   * This test looks equivalent to other ones, but actually uncovered a case where
   * the very 1st range is tiny (like 0.20 to 0.20 + tiny epsilon)
   */
  @Test
  public void lowerBoundIsExactlyOnBoundary_noMultiplier() {
    Consumer<UnaryOperator<GeometricallyIncreasingRangesGenerationInstructionsBuilder>> asserter = f ->
        assertThat(
            makeRealObject().calculateGeometricallyIncreasingRanges(
                f.apply(geometricallyIncreasingRangesGenerationInstructionsBuilder()
                    .setInitialStep(0.01)
                    .setStepMultiplier(1.0)
                    .setBoundsForOriginalExpression(closedRange(0.2, 0.25)))
                    .doNotStopMultiplyingStepsUntilEndOfInterval()
                    .build()),
            linearApproximationVarRangesMatcher(
                linearApproximationVarRanges(ImmutableList.of(
                    0.20, 0.21, 0.22, 0.23, 0.24, 0.25))));
    asserter.accept(f -> f.approximationStartsAt0());
    asserter.accept(f -> f.approximationStartsAtLowerBound());
  }

  @Test
  public void lowerBoundIsNotOnBoundary() {
    assertThat(
        makeRealObject().calculateGeometricallyIncreasingRanges(
            geometricallyIncreasingRangesGenerationInstructionsBuilder()
                .setInitialStep(0.01)
                .setStepMultiplier(2.0)
                .setBoundsForOriginalExpression(closedRange(0.12, 1.0))
                .approximationStartsAt0()
                .doNotStopMultiplyingStepsUntilEndOfInterval()
                .build()),
        linearApproximationVarRangesMatcher(
            linearApproximationVarRanges(ImmutableList.of(
                0.12, 0.15, 0.31, 0.63, 1.00))));
  }

  @Test
  public void upperBoundIsExactlyOnBoundary_increasingLineSegments() {
    assertThat(
        makeRealObject().calculateGeometricallyIncreasingRanges(
            geometricallyIncreasingRangesGenerationInstructionsBuilder()
                .setInitialStep(0.01)
                .setStepMultiplier(2.0)
                .setBoundsForOriginalExpression(closedRange(0.12, 0.63))
                .approximationStartsAt0()
                .doNotStopMultiplyingStepsUntilEndOfInterval()
                .build()),
        linearApproximationVarRangesMatcher(
            linearApproximationVarRanges(ImmutableList.of(
                0.12, 0.15, 0.31, 0.63))));
  }

  @Test
  public void upperBoundIsExactlyOnBoundary_constantLineSegments() {
    Consumer<UnaryOperator<GeometricallyIncreasingRangesGenerationInstructionsBuilder>> asserter = f ->
        assertThat(
            makeRealObject().calculateGeometricallyIncreasingRanges(
                f.apply(geometricallyIncreasingRangesGenerationInstructionsBuilder()
                    .setInitialStep(0.1)
                    .setStepMultiplier(1.0)
                    .setBoundsForOriginalExpression(closedRange(0.0, 1.0)))
                    .doNotStopMultiplyingStepsUntilEndOfInterval()
                    .build()),
            linearApproximationVarRangesMatcher(
                linearApproximationVarRanges(ImmutableList.of(
                    0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0))));
    asserter.accept(f -> f.approximationStartsAt0());
    asserter.accept(f -> f.approximationStartsAtLowerBound());
  }

  @Override
  protected Class<GeometricallyIncreasingLinearApproximationVarRangesGenerator> getClassBeingTested() {
    return GeometricallyIncreasingLinearApproximationVarRangesGenerator.class;
  }
  
}
