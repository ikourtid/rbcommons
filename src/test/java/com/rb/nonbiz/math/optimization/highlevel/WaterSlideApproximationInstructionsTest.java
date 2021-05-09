package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructions.waterSlideApproximationInstructions;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructionsAboveX90Test.defaultWaterSlideApproximationInstructionsAboveX90;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructionsAboveX90Test.superSafeWaterSlideApproximationInstructionsAboveX90;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructionsAboveX90Test.waterSlideApproximationInstructionsAboveX90Matcher;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructionsBelowX90Test.defaultWaterSlideApproximationInstructionsBelowX90;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructionsBelowX90Test.waterSlideApproximationInstructionsBelowX90Matcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class WaterSlideApproximationInstructionsTest
  extends RBTestMatcher<WaterSlideApproximationInstructions> {

  public static WaterSlideApproximationInstructions defaultWaterSlideApproximationInstructions() {
    return waterSlideApproximationInstructions(
        defaultWaterSlideApproximationInstructionsBelowX90(),
        defaultWaterSlideApproximationInstructionsAboveX90());
  }

  public static WaterSlideApproximationInstructions superSafeWaterSlideApproximationInstructions() {
    return waterSlideApproximationInstructions(
        defaultWaterSlideApproximationInstructionsBelowX90(),
        superSafeWaterSlideApproximationInstructionsAboveX90());
  }

  @Override
  public WaterSlideApproximationInstructions makeTrivialObject() {
    return waterSlideApproximationInstructions(
        new WaterSlideApproximationInstructionsBelowX90Test().makeTrivialObject(),
        new WaterSlideApproximationInstructionsAboveX90Test().makeTrivialObject());
  }

  @Override
  public WaterSlideApproximationInstructions makeNontrivialObject() {
    return waterSlideApproximationInstructions(
        new WaterSlideApproximationInstructionsBelowX90Test().makeNontrivialObject(),
        new WaterSlideApproximationInstructionsAboveX90Test().makeNontrivialObject());
  }

  @Override
  public WaterSlideApproximationInstructions makeMatchingNontrivialObject() {
    return waterSlideApproximationInstructions(
        new WaterSlideApproximationInstructionsBelowX90Test().makeMatchingNontrivialObject(),
        new WaterSlideApproximationInstructionsAboveX90Test().makeMatchingNontrivialObject());
  }

  @Override
  protected boolean willMatch(WaterSlideApproximationInstructions expected,
                              WaterSlideApproximationInstructions actual) {
    return waterSlideFunctionLinearApproximationInstructionsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<WaterSlideApproximationInstructions>
      waterSlideFunctionLinearApproximationInstructionsMatcher(WaterSlideApproximationInstructions expected) {
    return makeMatcher(expected,
        match(v -> v.getForBelowX90(), f -> waterSlideApproximationInstructionsBelowX90Matcher(f)),
        match(v -> v.getForAboveX90(), f -> waterSlideApproximationInstructionsAboveX90Matcher(f)));
  }

}
