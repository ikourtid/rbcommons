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
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class ClosedUnitFractionHardToSoftRangeTighteningInstructionsTest
    extends RBTestMatcher<ClosedUnitFractionHardToSoftRangeTighteningInstructions> {

  @Test
  public void throwsForZeroOrAlmostZero() {
    // Lower
    assertIllegalArgumentException( () -> closedUnitFractionHardToSoftRangeTighteningInstructions(UNIT_FRACTION_0, UNIT_FRACTION_0));
    assertIllegalArgumentException( () -> closedUnitFractionHardToSoftRangeTighteningInstructions(UNIT_FRACTION_0, UNIT_FRACTION_1));
    assertIllegalArgumentException( () -> closedUnitFractionHardToSoftRangeTighteningInstructions(unitFraction(1e-9), UNIT_FRACTION_1));
    ClosedUnitFractionHardToSoftRangeTighteningInstructions doesNotThrow =
        closedUnitFractionHardToSoftRangeTighteningInstructions(unitFraction(1e-7), unitFraction(1e-7));
  }


  @Override
  public ClosedUnitFractionHardToSoftRangeTighteningInstructions makeTrivialObject() {
    return setClosedUnitFractionSoftRangeToSameAsHard();
  }

  @Override
  public ClosedUnitFractionHardToSoftRangeTighteningInstructions makeNontrivialObject() {
    return closedUnitFractionHardToSoftRangeTighteningInstructions(unitFraction(0.123), unitFraction(.456));
  }

  @Override
  public ClosedUnitFractionHardToSoftRangeTighteningInstructions makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return closedUnitFractionHardToSoftRangeTighteningInstructions(unitFraction(0.123 + e), unitFraction(0.456 + e));
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
        matchUsingAlmostEquals(v -> v.getRawMultiplierForLowerEndPoint(), DEFAULT_EPSILON_1e_8),
        matchUsingAlmostEquals(v -> v.getRawMultiplierForUpperEndPoint(), DEFAULT_EPSILON_1e_8));
  }

}
