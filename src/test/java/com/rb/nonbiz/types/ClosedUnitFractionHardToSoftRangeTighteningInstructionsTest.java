package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.ClosedUnitFractionHardToSoftRangeTighteningInstructions.closedUnitFractionHardToSoftRangeTighteningInstructions;
import static com.rb.nonbiz.types.ClosedUnitFractionHardToSoftRangeTighteningInstructions.setClosedUnitFractionSoftRangeToSameAsHard;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class ClosedUnitFractionHardToSoftRangeTighteningInstructionsTest
    extends RBTestMatcher<ClosedUnitFractionHardToSoftRangeTighteningInstructions> {

  @Test
  public void throwsForZeroOrAlmostZero() {
    assertIllegalArgumentException( () -> closedUnitFractionHardToSoftRangeTighteningInstructions(UNIT_FRACTION_0));
    assertIllegalArgumentException( () -> closedUnitFractionHardToSoftRangeTighteningInstructions(unitFraction(1e-9)));
    ClosedUnitFractionHardToSoftRangeTighteningInstructions doesNotThrow =
        closedUnitFractionHardToSoftRangeTighteningInstructions(unitFraction(1e-7));
  }


  @Override
  public ClosedUnitFractionHardToSoftRangeTighteningInstructions makeTrivialObject() {
    return setClosedUnitFractionSoftRangeToSameAsHard();
  }

  @Override
  public ClosedUnitFractionHardToSoftRangeTighteningInstructions makeNontrivialObject() {
    return closedUnitFractionHardToSoftRangeTighteningInstructions(unitFraction(0.123));
  }

  @Override
  public ClosedUnitFractionHardToSoftRangeTighteningInstructions makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return closedUnitFractionHardToSoftRangeTighteningInstructions(unitFraction(0.123 + e));
  }

  @Override
  protected boolean willMatch(ClosedUnitFractionHardToSoftRangeTighteningInstructions expected,
                              ClosedUnitFractionHardToSoftRangeTighteningInstructions actual) {
    return closedUnitFractionHardToSoftRangeTighteningInstructionsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<ClosedUnitFractionHardToSoftRangeTighteningInstructions>
  closedUnitFractionHardToSoftRangeTighteningInstructionsMatcher(
      ClosedUnitFractionHardToSoftRangeTighteningInstructions expected) {
    return makeMatcher(expected,
        matchUsingAlmostEquals(v -> v.getRawMultiplier(), DEFAULT_EPSILON_1e_8));
  }

}
