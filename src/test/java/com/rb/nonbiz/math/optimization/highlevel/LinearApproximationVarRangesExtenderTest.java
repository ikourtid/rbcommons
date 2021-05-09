package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRanges.linearApproximationVarRanges;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesTest.linearApproximationVarRangesMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.hamcrest.MatcherAssert.assertThat;

public class LinearApproximationVarRangesExtenderTest extends RBTest<LinearApproximationVarRangesExtender> {

  private final LinearApproximationVarRanges ORIGINAL_LINEAR_APPROXIMATION_VAR_RANGES =
      linearApproximationVarRanges(ImmutableList.of(0.0, 0.01, 0.03, 0.07, 0.15, 0.31, 0.63, 1.00));

  @Test
  public void triesToExtendWithFirstPointSmallerThanLastPointInOriginal_throws() {
    assertIllegalArgumentException( () -> makeTestObject().extend(ORIGINAL_LINEAR_APPROXIMATION_VAR_RANGES, 0.99));
    assertIllegalArgumentException( () -> makeTestObject().extend(ORIGINAL_LINEAR_APPROXIMATION_VAR_RANGES, 1.0));
    LinearApproximationVarRanges doesNotThrow = makeTestObject().extend(ORIGINAL_LINEAR_APPROXIMATION_VAR_RANGES, 1.01);
  }

  @Test
  public void pointsToExtendWithAreNotInOrder_throws() {
    // This currently (Jan 2018) gets checked by the LinearApproximationVarRanges static constructor, not this class,
    // but it's good to test it here anyway.
    assertIllegalArgumentException( () -> makeTestObject().extend(ORIGINAL_LINEAR_APPROXIMATION_VAR_RANGES, 1.05, 1.01));
    LinearApproximationVarRanges doesNotThrow = makeTestObject().extend(ORIGINAL_LINEAR_APPROXIMATION_VAR_RANGES, 1.01, 1.05);
  }

  @Test
  public void happyPath() {
    assertThat(
        makeTestObject().extend(ORIGINAL_LINEAR_APPROXIMATION_VAR_RANGES, 5.0, 50.0),
        linearApproximationVarRangesMatcher(
            linearApproximationVarRanges(ImmutableList.of(0.0, 0.01, 0.03, 0.07, 0.15, 0.31, 0.63, 1.00, 5.0, 50.0))));
  }

  @Override
  protected LinearApproximationVarRangesExtender makeTestObject() {
    return new LinearApproximationVarRangesExtender();
  }

}