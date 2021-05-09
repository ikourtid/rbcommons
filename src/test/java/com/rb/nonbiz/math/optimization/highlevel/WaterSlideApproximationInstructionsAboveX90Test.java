package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructionsAboveX90.waterSlideApproximationInstructionsAboveX90;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class WaterSlideApproximationInstructionsAboveX90Test
    extends RBTestMatcher<WaterSlideApproximationInstructionsAboveX90> {

  public static WaterSlideApproximationInstructionsAboveX90 superSafeWaterSlideApproximationInstructionsAboveX90() {
    return waterSlideApproximationInstructionsAboveX90(
        // 50 = 5000% = max misallocation we could ever have even with synthetic cash and crazy high betas.
        50.0,
        // a reasonable step multiplier so we can get geometrically increasing intervals in the
        // linear approximation from x90 to maxX.
        1.6);
  }

  public static WaterSlideApproximationInstructionsAboveX90 defaultWaterSlideApproximationInstructionsAboveX90() {
    return waterSlideApproximationInstructionsAboveX90(
        // 3 = 300% = max misallocation we could ever have even with synthetic cash and reasonably high
        // (but not crazy high) betas.
        3.0,
        // a reasonable step multiplier so we can get geometrically increasing intervals in the
        // linear approximation from x90 to maxX.
        1.6);
  }

  @Test
  public void stepMultiplier_mustBeGreaterThanOne() {
    Function<Double, WaterSlideApproximationInstructionsAboveX90> maker =
        stepMultiplier -> waterSlideApproximationInstructionsAboveX90(34.567, stepMultiplier);
    WaterSlideApproximationInstructionsAboveX90 doesNotThrow = maker.apply(1.01);
    assertIllegalArgumentException( () -> maker.apply(1.0));
    assertIllegalArgumentException( () -> maker.apply(0.99));
    assertIllegalArgumentException( () -> maker.apply(0.01));
    assertIllegalArgumentException( () -> maker.apply(0.0));
    assertIllegalArgumentException( () -> maker.apply(-0.01));
    assertIllegalArgumentException( () -> maker.apply(-3.33));
  }

  @Test
  public void maximumX_mustBePositive() {
    Function<Double, WaterSlideApproximationInstructionsAboveX90> maker =
        maxX -> waterSlideApproximationInstructionsAboveX90(maxX, 1.1); // 1.1 is some valid step multiplier
    WaterSlideApproximationInstructionsAboveX90 doesNotThrow = maker.apply(0.1);
    assertIllegalArgumentException( () -> maker.apply(0.0));
    assertIllegalArgumentException( () -> maker.apply(-0.1));
  }

  @Override
  public WaterSlideApproximationInstructionsAboveX90 makeTrivialObject() {
    return waterSlideApproximationInstructionsAboveX90(1.0, 2.0);
  }

  @Override
  public WaterSlideApproximationInstructionsAboveX90 makeNontrivialObject() {
    return waterSlideApproximationInstructionsAboveX90(12.34, 1.1);
  }

  @Override
  public WaterSlideApproximationInstructionsAboveX90 makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return waterSlideApproximationInstructionsAboveX90(12.34 + e, 1.1 + e);
  }

  @Override
  protected boolean willMatch(WaterSlideApproximationInstructionsAboveX90 expected,
                              WaterSlideApproximationInstructionsAboveX90 actual) {
    return waterSlideApproximationInstructionsAboveX90Matcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<WaterSlideApproximationInstructionsAboveX90> waterSlideApproximationInstructionsAboveX90Matcher(
      WaterSlideApproximationInstructionsAboveX90 expected) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getMaxX(),           1e-8),
        matchUsingDoubleAlmostEquals(v -> v.getStepMultiplier(), 1e-8));
  }

}
