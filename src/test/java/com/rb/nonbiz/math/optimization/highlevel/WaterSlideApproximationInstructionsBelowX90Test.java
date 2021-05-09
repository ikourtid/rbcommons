package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideApproximationInstructionsBelowX90.waterSlideApproximationInstructionsBelowX90;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class WaterSlideApproximationInstructionsBelowX90Test
    extends RBTestMatcher<WaterSlideApproximationInstructionsBelowX90> {

  public static WaterSlideApproximationInstructionsBelowX90 defaultWaterSlideApproximationInstructionsBelowX90() {
    return waterSlideApproximationInstructionsBelowX90(
        unitFraction(0.9),
        // We use 10 hinge points up to & including x90: 0, x10, x20, ..., x90.
        10);
  }

  @Test
  public void mustHaveAtLeast3HingePoints() {
    UnitFraction valid = unitFraction(0.9);
    ImmutableList.of(-999, -4, -3, -2, -1, 0, 1).forEach(numHingePoints ->
        assertIllegalArgumentException( () ->
            waterSlideApproximationInstructionsBelowX90(valid, numHingePoints)));
    WaterSlideApproximationInstructionsBelowX90 doesNotThrow;
    doesNotThrow = waterSlideApproximationInstructionsBelowX90(valid, 2);
    doesNotThrow = waterSlideApproximationInstructionsBelowX90(valid, 3);
    doesNotThrow = waterSlideApproximationInstructionsBelowX90(valid, 999);
  }

  @Test
  public void fractionOfFinalSlope_cannotBeExtreme() {
    Function<UnitFraction, WaterSlideApproximationInstructionsBelowX90> maker =
        lastCutoff -> waterSlideApproximationInstructionsBelowX90(lastCutoff, DUMMY_POSITIVE_INTEGER);
    WaterSlideApproximationInstructionsBelowX90 doesNotThrow;
    assertIllegalArgumentException( () -> maker.apply(UNIT_FRACTION_0));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(1e-9)));
    doesNotThrow = maker.apply(unitFraction(1e-7));
    doesNotThrow = maker.apply(unitFraction(0.5));
    doesNotThrow = maker.apply(unitFraction(1 - 1e-7));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(1 - 1e-9)));
    assertIllegalArgumentException( () -> maker.apply(UNIT_FRACTION_1));
  }

  @Override
  public WaterSlideApproximationInstructionsBelowX90 makeTrivialObject() {
    return waterSlideApproximationInstructionsBelowX90(unitFraction(0.9), 3);
  }

  @Override
  public WaterSlideApproximationInstructionsBelowX90 makeNontrivialObject() {
    return waterSlideApproximationInstructionsBelowX90(unitFraction(0.88), 5);
  }

  @Override
  public WaterSlideApproximationInstructionsBelowX90 makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return waterSlideApproximationInstructionsBelowX90(unitFraction(0.88 + e), 5);
  }

  @Override
  protected boolean willMatch(WaterSlideApproximationInstructionsBelowX90 expected,
                              WaterSlideApproximationInstructionsBelowX90 actual) {
    return waterSlideApproximationInstructionsBelowX90Matcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<WaterSlideApproximationInstructionsBelowX90> waterSlideApproximationInstructionsBelowX90Matcher(
      WaterSlideApproximationInstructionsBelowX90 expected) {
    return makeMatcher(expected,
        matchUsingAlmostEquals(v -> v.getFractionOfFinalSlope(), 1e-8),
        matchUsingEquals(v -> v.getNumHingePointsUpToAndIncludingX90()));
  }

}
