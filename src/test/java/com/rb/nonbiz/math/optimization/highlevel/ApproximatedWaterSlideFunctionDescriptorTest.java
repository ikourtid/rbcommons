package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.highlevel.ApproximatedWaterSlideFunctionDescriptor.approximatedWaterSlideFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructionsTest.waterSlideFunctionLinearApproximationInstructionsMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptorTest.waterSlideFunctionDescriptorMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class ApproximatedWaterSlideFunctionDescriptorTest extends RBTestMatcher<ApproximatedWaterSlideFunctionDescriptor> {

  @Override
  public ApproximatedWaterSlideFunctionDescriptor makeTrivialObject() {
    return approximatedWaterSlideFunctionDescriptor(
        new WaterSlideFunctionDescriptorTest().makeTrivialObject(),
        new WaterSlideApproximationInstructionsTest().makeTrivialObject());
  }

  @Override
  public ApproximatedWaterSlideFunctionDescriptor makeNontrivialObject() {
    return approximatedWaterSlideFunctionDescriptor(
        new WaterSlideFunctionDescriptorTest().makeNontrivialObject(),
        new WaterSlideApproximationInstructionsTest().makeNontrivialObject());
  }

  @Override
  public ApproximatedWaterSlideFunctionDescriptor makeMatchingNontrivialObject() {
    return approximatedWaterSlideFunctionDescriptor(
        new WaterSlideFunctionDescriptorTest().makeMatchingNontrivialObject(),
        new WaterSlideApproximationInstructionsTest().makeMatchingNontrivialObject());
  }

  @Override
  protected boolean willMatch(ApproximatedWaterSlideFunctionDescriptor expected, ApproximatedWaterSlideFunctionDescriptor actual) {
    return approximatedWaterSlideFunctionDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<ApproximatedWaterSlideFunctionDescriptor> approximatedWaterSlideFunctionDescriptorMatcher(
      ApproximatedWaterSlideFunctionDescriptor expected) {
    return makeMatcher(expected,
        match(v -> v.getWaterSlideFunctionDescriptor(),        f -> waterSlideFunctionDescriptorMatcher(f)),
        match(v -> v.getWaterSlideApproximationInstructions(), f -> waterSlideFunctionLinearApproximationInstructionsMatcher(f)));
  }

}
