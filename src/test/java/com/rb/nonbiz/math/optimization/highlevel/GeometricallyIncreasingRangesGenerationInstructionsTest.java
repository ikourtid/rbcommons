package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.math.optimization.highlevel.GeometricallyIncreasingRangesGenerationInstructions.GeometricallyIncreasingRangesGenerationInstructionsBuilder.geometricallyIncreasingRangesGenerationInstructionsBuilder;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchOptionalDouble;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.doubleClosedRangeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertNullPointerException;

public class GeometricallyIncreasingRangesGenerationInstructionsTest
    extends RBTestMatcher<GeometricallyIncreasingRangesGenerationInstructions> {

  @Test
  public void mustIncludeAllValues_otherwiseThrows() {
    GeometricallyIncreasingRangesGenerationInstructions doesNotThrow = geometricallyIncreasingRangesGenerationInstructionsBuilder()
        .setInitialStep(0.1)
        .setStepMultiplier(2)
        .setBoundsForOriginalExpression(closedRange(0.0, 1.0))
        .approximationStartsAt0()
        .doNotStopMultiplyingStepsUntilEndOfInterval()
        .build();
    assertNullPointerException( () -> geometricallyIncreasingRangesGenerationInstructionsBuilder()
        .setStepMultiplier(2)
        .setBoundsForOriginalExpression(closedRange(0.0, 1.0))
        .approximationStartsAt0()
        .doNotStopMultiplyingStepsUntilEndOfInterval()
        .build());
    assertNullPointerException( () -> geometricallyIncreasingRangesGenerationInstructionsBuilder()
        .setInitialStep(0.1)
        .setBoundsForOriginalExpression(closedRange(0.0, 1.0))
        .approximationStartsAt0()
        .doNotStopMultiplyingStepsUntilEndOfInterval()
        .build());
    assertNullPointerException( () -> geometricallyIncreasingRangesGenerationInstructionsBuilder()
        .setInitialStep(0.1)
        .setStepMultiplier(2)
        .approximationStartsAt0()
        .doNotStopMultiplyingStepsUntilEndOfInterval()
        .build());
    assertNullPointerException( () -> geometricallyIncreasingRangesGenerationInstructionsBuilder()
        .setInitialStep(0.1)
        .setStepMultiplier(2)
        .setBoundsForOriginalExpression(closedRange(0.0, 1.0))
        .doNotStopMultiplyingStepsUntilEndOfInterval()
        .build());
  }

  @Test
  public void initialStepMustBePositive() {
    Function<Double, GeometricallyIncreasingRangesGenerationInstructions> maker = initialStep ->
        geometricallyIncreasingRangesGenerationInstructionsBuilder()
            .setInitialStep(initialStep)
            .setStepMultiplier(2)
            .setBoundsForOriginalExpression(closedRange(0.0, 1.0))
            .approximationStartsAt0()
            .doNotStopMultiplyingStepsUntilEndOfInterval()
            .build();
    GeometricallyIncreasingRangesGenerationInstructions doesNotThrow = maker.apply(0.01);
    assertIllegalArgumentException( () -> maker.apply(0.0));
    assertIllegalArgumentException( () -> maker.apply(-0.1));
  }

  @Test
  public void segmentsCannotShrink() {
    Function<Double, GeometricallyIncreasingRangesGenerationInstructions> maker = stepMultiplier ->
        geometricallyIncreasingRangesGenerationInstructionsBuilder()
            .setInitialStep(0.01)
            .setStepMultiplier(stepMultiplier)
            .setBoundsForOriginalExpression(closedRange(0.0, 1.0))
            .approximationStartsAt0()
            .doNotStopMultiplyingStepsUntilEndOfInterval()
            .build();
    GeometricallyIncreasingRangesGenerationInstructions doesNotThrow;
    doesNotThrow = maker.apply(1.01);
    doesNotThrow = maker.apply(1.0);
    assertIllegalArgumentException( () -> maker.apply(0.99));
  }

  @Test
  public void initialStepIsTooBig_resultsInNoSegments_throws() {
    Function<Double, GeometricallyIncreasingRangesGenerationInstructions> maker = initialStep ->
        geometricallyIncreasingRangesGenerationInstructionsBuilder()
            .setInitialStep(initialStep)
            .setStepMultiplier(2)
            .setBoundsForOriginalExpression(closedRange(0.0, 1.0))
            .approximationStartsAt0()
            .doNotStopMultiplyingStepsUntilEndOfInterval()
            .build();
    GeometricallyIncreasingRangesGenerationInstructions doesNotThrow;
    doesNotThrow = maker.apply(0.99);
    doesNotThrow = maker.apply(1.00);
    assertIllegalArgumentException( () -> maker.apply(1.01));
  }

  @Test
  public void lowerBoundIsNegative_throws() {
    Function<Double, GeometricallyIncreasingRangesGenerationInstructions> maker = lowerBound ->
        geometricallyIncreasingRangesGenerationInstructionsBuilder()
            .setInitialStep(0.01)
            .setStepMultiplier(2)
            .setBoundsForOriginalExpression(closedRange(lowerBound, 1.0))
            .approximationStartsAt0()
            .doNotStopMultiplyingStepsUntilEndOfInterval()
            .build();
    GeometricallyIncreasingRangesGenerationInstructions doesNotThrow;
    doesNotThrow = maker.apply(0.001);
    doesNotThrow = maker.apply(0.0);
    assertIllegalArgumentException( () -> maker.apply(-0.001));
  }

  @Override
  public GeometricallyIncreasingRangesGenerationInstructions makeTrivialObject() {
    return geometricallyIncreasingRangesGenerationInstructionsBuilder()
        .setInitialStep(0.1)
        .setStepMultiplier(2)
        .setBoundsForOriginalExpression(closedRange(0.0, 1.0))
        .approximationStartsAt0()
        .doNotStopMultiplyingStepsUntilEndOfInterval()
        .build();
  }

  @Override
  public GeometricallyIncreasingRangesGenerationInstructions makeNontrivialObject() {
    return geometricallyIncreasingRangesGenerationInstructionsBuilder()
        .setInitialStep(0.11)
        .setStepMultiplier(2.22)
        .setBoundsForOriginalExpression(closedRange(3.3, 4.4))
        .approximationStartsAt0()
        .stopMultiplyingStepsAfterThis(4.123)
        .build();
  }

  @Override
  public GeometricallyIncreasingRangesGenerationInstructions makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return geometricallyIncreasingRangesGenerationInstructionsBuilder()
        .setInitialStep(0.11 + e)
        .setStepMultiplier(2.22 + e)
        .setBoundsForOriginalExpression(closedRange(3.3 + e, 4.4 + e))
        .approximationStartsAt0()
        .stopMultiplyingStepsAfterThis(4.123 + e)
        .build();
  }

  @Override
  protected boolean willMatch(GeometricallyIncreasingRangesGenerationInstructions expected,
                              GeometricallyIncreasingRangesGenerationInstructions actual) {
    return geometricallyIncreasingGenerationInstructionsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<GeometricallyIncreasingRangesGenerationInstructions>
      geometricallyIncreasingGenerationInstructionsMatcher(GeometricallyIncreasingRangesGenerationInstructions expected) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getInitialStep(),    1e-8),
        matchUsingDoubleAlmostEquals(v -> v.getStepMultiplier(), 1e-8),
        match(                       v -> v.getBoundsForOriginalExpression(), f -> doubleClosedRangeMatcher(f, 1e-8)),
        matchUsingDoubleAlmostEquals(v -> v.getStartingPointForApproximation(), 1e-8),
        matchOptionalDouble(         v -> v.getStopMultiplyingStepsAfterThis(), 1e-8));
  }

}
