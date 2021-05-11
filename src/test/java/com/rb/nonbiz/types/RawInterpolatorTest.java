package com.rb.nonbiz.types;

import org.junit.Test;

import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.RawInterpolator.interpolateWithLeftPreference;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.junit.Assert.assertEquals;

public class RawInterpolatorTest {

  @Test
  public void simpleInterpolations() {
    assertEquals(2, interpolateWithLeftPreference(UNIT_FRACTION_1).from(2).to(12), 1e-8);
    assertEquals(2, interpolateWithLeftPreference(UNIT_FRACTION_1).from(2).to(2), 1e-8);
    assertEquals(2, interpolateWithLeftPreference(UNIT_FRACTION_1).from(2).to(-12), 1e-8);

    assertEquals(12,  interpolateWithLeftPreference(UNIT_FRACTION_0).from(2).to(12), 1e-8);
    assertEquals(2,   interpolateWithLeftPreference(UNIT_FRACTION_0).from(2).to(2), 1e-8);
    assertEquals(-12, interpolateWithLeftPreference(UNIT_FRACTION_0).from(2).to(-12), 1e-8);

    assertEquals(
        doubleExplained(9.5, 2 + 0.75 * (12 - 2)),
        interpolateWithLeftPreference(unitFraction(0.25)).from(2).to(12),
        1e-8);
    assertEquals(
        doubleExplained(2, 2 + 0.75 * (2 - 2)),
        interpolateWithLeftPreference(unitFraction(0.25)).from(2).to(2),
        1e-8);
    assertEquals(
        doubleExplained(-13, 2 + 0.75 * (-18 - 2)),
        interpolateWithLeftPreference(unitFraction(0.25)).from(2).to(-18),
        1e-8);
  }


}
