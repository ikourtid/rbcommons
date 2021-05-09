package com.rb.nonbiz.math.optimization.general;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.general.ScalingInstructionsForImprovedLpAccuracy.scalingInstructionsForImprovedLpAccuracy;
import static com.rb.nonbiz.testmatchers.Match.matchUsingImpreciseAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.types.PositiveMultiplier.POSITIVE_MULTIPLIER_1;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;

public class ScalingInstructionsForImprovedLpAccuracyTest
    extends RBTestMatcher<ScalingInstructionsForImprovedLpAccuracy> {

  @Override
  public ScalingInstructionsForImprovedLpAccuracy makeTrivialObject() {
    return scalingInstructionsForImprovedLpAccuracy(POSITIVE_MULTIPLIER_1);
  }

  @Override
  public ScalingInstructionsForImprovedLpAccuracy makeNontrivialObject() {
    return scalingInstructionsForImprovedLpAccuracy(positiveMultiplier(0.123));
  }

  @Override
  public ScalingInstructionsForImprovedLpAccuracy makeMatchingNontrivialObject() {
    double e = 1e-9;
    return scalingInstructionsForImprovedLpAccuracy(positiveMultiplier(0.123 + e));
  }

  @Override
  protected boolean willMatch(ScalingInstructionsForImprovedLpAccuracy expected,
                              ScalingInstructionsForImprovedLpAccuracy actual) {
    return scalingInstructionsForImprovedLpAccuracyMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<ScalingInstructionsForImprovedLpAccuracy>
      scalingInstructionsForImprovedLpAccuracyMatcher(ScalingInstructionsForImprovedLpAccuracy expected) {
    return makeMatcher(expected,
        matchUsingImpreciseAlmostEquals(v -> v.getRawMultiplier(), 1e-8));
  }

}
