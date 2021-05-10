package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.math.optimization.highlevel.GeometricallyIncreasingRangesGenerationInstructions.GeometricallyIncreasingRangesGenerationInstructionsBuilder.geometricallyIncreasingRangesGenerationInstructionsBuilder;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.doubleListMatcher;

public class GeometricallyIncreasingRangesRawGeneratorTest extends RBTest<GeometricallyIncreasingRangesRawGenerator> {

  @Test
  public void lowerBoundIsZero() {
    Consumer<UnaryOperator<GeometricallyIncreasingRangesGenerationInstructionsBuilder>> asserter = f ->
        assertResult(
            f.apply(geometricallyIncreasingRangesGenerationInstructionsBuilder()
                .setInitialStep(0.01)
                .setStepMultiplier(2.0)
                .setBoundsForOriginalExpression(closedRange(0.0, 1.0)))
                .doNotStopMultiplyingStepsUntilEndOfInterval()
                .build(),
            ImmutableList.of(0.0, 0.01, 0.03, 0.07, 0.15, 0.31, 0.63, 1.00));
    asserter.accept(f -> f.approximationStartsAt0());
    asserter.accept(f -> f.approximationStartsAtLowerBound());
  }

  @Test
  public void lowerBoundIsExactlyOnBoundary_approximationStartsAt0() {
    assertResult(
        geometricallyIncreasingRangesGenerationInstructionsBuilder()
            .setInitialStep(0.01)
            .setStepMultiplier(2.0)
            .setBoundsForOriginalExpression(closedRange(0.03, 1.0))
            .approximationStartsAt0()
            .doNotStopMultiplyingStepsUntilEndOfInterval()
            .build(),
        ImmutableList.of(0.03, 0.07, 0.15, 0.31, 0.63, 1.00));
  }

  @Test
  public void approximationStartsAtLowerBound_lastSegmentDoesNotFallOnUpperBound() {
    assertResult(
        geometricallyIncreasingRangesGenerationInstructionsBuilder()
            .setInitialStep(0.01)
            .setStepMultiplier(2.0)
            .setBoundsForOriginalExpression(closedRange(0.03, 1.0))
            .approximationStartsAtLowerBound()
            .doNotStopMultiplyingStepsUntilEndOfInterval()
            .build(),
        ImmutableList.of(0.03, 0.04, 0.06, 0.10, 0.18, 0.34, 0.66, 1.00));
  }

  @Test
  public void approximationStartsAtLowerBound_lastSegmentFallsOnUpperBound() {
    assertResult(
        geometricallyIncreasingRangesGenerationInstructionsBuilder()
            .setInitialStep(0.01)
            .setStepMultiplier(2.0)
            .setBoundsForOriginalExpression(closedRange(0.03, 0.66))
            .approximationStartsAtLowerBound()
            .doNotStopMultiplyingStepsUntilEndOfInterval()
            .build(),
        ImmutableList.of(0.03, 0.04, 0.06, 0.10, 0.18, 0.34, 0.66));
  }

  /**
   * This test looks equivalent to other ones, but actually uncovered a case where
   * the very 1st range is tiny (like 0.20 to 0.20 + tiny epsilon)
   */
  @Test
  public void lowerBoundIsExactlyOnBoundary_noMultiplier() {
    Consumer<UnaryOperator<GeometricallyIncreasingRangesGenerationInstructionsBuilder>> asserter = f ->
        assertResult(
            f.apply(
                geometricallyIncreasingRangesGenerationInstructionsBuilder()
                    .setInitialStep(0.01)
                    .setStepMultiplier(1.0)
                    .setBoundsForOriginalExpression(closedRange(0.2, 0.25)))
                .doNotStopMultiplyingStepsUntilEndOfInterval()
                .build(),
            ImmutableList.of(0.20, 0.21, 0.22, 0.23, 0.24, 0.25));

    asserter.accept(f -> f.approximationStartsAt0());
    asserter.accept(f -> f.approximationStartsAtLowerBound());
  }

  @Test
  public void lowerBoundIsNotOnBoundary() {
    assertResult(
        geometricallyIncreasingRangesGenerationInstructionsBuilder()
            .setInitialStep(0.01)
            .setStepMultiplier(2.0)
            .setBoundsForOriginalExpression(closedRange(0.12, 1.0))
            .approximationStartsAt0()
            .doNotStopMultiplyingStepsUntilEndOfInterval()
            .build(),
        ImmutableList.of(0.12, 0.15, 0.31, 0.63, 1.00));
  }

  @Test
  public void upperBoundIsExactlyOnBoundary_increasingLineSegments() {
    assertResult(
        geometricallyIncreasingRangesGenerationInstructionsBuilder()
            .setInitialStep(0.01)
            .setStepMultiplier(2.0)
            .setBoundsForOriginalExpression(closedRange(0.12, 0.63))
            .approximationStartsAt0()
            .doNotStopMultiplyingStepsUntilEndOfInterval()
            .build(),
        ImmutableList.of(0.12, 0.15, 0.31, 0.63));
  }

  @Test
  public void upperBoundIsExactlyOnBoundary_constantLineSegments() {
    Consumer<UnaryOperator<GeometricallyIncreasingRangesGenerationInstructionsBuilder>> asserter = f ->
        assertResult(
            f.apply(geometricallyIncreasingRangesGenerationInstructionsBuilder()
                .setInitialStep(0.1)
                .setStepMultiplier(1.0)
                .setBoundsForOriginalExpression(closedRange(0.0, 1.0)))
                .doNotStopMultiplyingStepsUntilEndOfInterval()
                .build(),
            ImmutableList.of(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0));
    asserter.accept(f -> f.approximationStartsAt0());
    asserter.accept(f -> f.approximationStartsAtLowerBound());
  }

  @Test
  public void stopsIntervalsPastSpecifiedPoint() {
    BiConsumer<GeometricallyIncreasingRangesGenerationInstructionsBuilder, List<Double>> asserter =
        (builder, expected) -> assertResult(
            builder
                .setInitialStep(0.01)
                .setStepMultiplier(2.0)
                .setBoundsForOriginalExpression(closedRange(0.0, 1.0))
                .approximationStartsAt0()
                .build(),
            expected);

    asserter.accept(
        geometricallyIncreasingRangesGenerationInstructionsBuilder()
            .doNotStopMultiplyingStepsUntilEndOfInterval(),
        ImmutableList.of(0.0, 0.01, 0.03, 0.07, 0.15, 0.31, 0.63, 1.0));
    asserter.accept(
        geometricallyIncreasingRangesGenerationInstructionsBuilder()
            .stopMultiplyingStepsAfterThis(0.64),
        ImmutableList.of(0.0, 0.01, 0.03, 0.07, 0.15, 0.31, 0.63, 0.64, 1.0));
    asserter.accept(
        geometricallyIncreasingRangesGenerationInstructionsBuilder()
            .stopMultiplyingStepsAfterThis(0.25),
        ImmutableList.of(0.0, 0.01, 0.03, 0.07, 0.15, 0.25, 1.0));
    asserter.accept(
        geometricallyIncreasingRangesGenerationInstructionsBuilder()
            .stopMultiplyingStepsAfterThis(0.017),
        ImmutableList.of(0.0, 0.01, 0.017, 1.0));
  }

  private void assertResult(
      GeometricallyIncreasingRangesGenerationInstructions geometricallyIncreasingRangesGenerationInstructions,
      List<Double> expectedResult) {
    assertThat(
        makeTestObject().generateRangeEndpoints(geometricallyIncreasingRangesGenerationInstructions),
        doubleListMatcher(expectedResult, 1e-8));
  }


  @Override
  protected GeometricallyIncreasingRangesRawGenerator makeTestObject() {
    return new GeometricallyIncreasingRangesRawGenerator();
  }

}
