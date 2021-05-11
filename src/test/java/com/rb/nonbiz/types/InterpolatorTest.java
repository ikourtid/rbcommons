package com.rb.nonbiz.types;

import org.junit.Test;

import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.InterpolationPreference.USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED;
import static com.rb.nonbiz.types.InterpolationPreference.USE_SUPPLIED_VALUE_INSTEAD_OF_DEFAULT;
import static com.rb.nonbiz.types.InterpolationPreference.preferSuppliedValueBy;
import static com.rb.nonbiz.types.Interpolator.interpolateUsingPreference;
import static org.junit.Assert.assertEquals;

public class InterpolatorTest {

  @Test
  public void simpleInterpolations() {
    assertEquals(2, interpolateUsingPreference(USE_SUPPLIED_VALUE_INSTEAD_OF_DEFAULT).betweenSuppliedValue(2).andDefaultValue(12), 1e-8);
    assertEquals(2, interpolateUsingPreference(USE_SUPPLIED_VALUE_INSTEAD_OF_DEFAULT).betweenSuppliedValue(2).andDefaultValue(2), 1e-8);
    assertEquals(2, interpolateUsingPreference(USE_SUPPLIED_VALUE_INSTEAD_OF_DEFAULT).betweenSuppliedValue(2).andDefaultValue(-12), 1e-8);

    assertEquals(12,  interpolateUsingPreference(USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED).betweenSuppliedValue(2).andDefaultValue(12), 1e-8);
    assertEquals(2,   interpolateUsingPreference(USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED).betweenSuppliedValue(2).andDefaultValue(2), 1e-8);
    assertEquals(-12, interpolateUsingPreference(USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED).betweenSuppliedValue(2).andDefaultValue(-12), 1e-8);

    assertEquals(
        doubleExplained(9.5, 2 + 0.75 * (12 - 2)),
        interpolateUsingPreference(preferSuppliedValueBy(0.25)).betweenSuppliedValue(2).andDefaultValue(12),
        1e-8);
    assertEquals(
        doubleExplained(2, 2 + 0.75 * (2 - 2)),
        interpolateUsingPreference(preferSuppliedValueBy(0.25)).betweenSuppliedValue(2).andDefaultValue(2),
        1e-8);
    assertEquals(
        doubleExplained(-13, 2 + 0.75 * (-18 - 2)),
        interpolateUsingPreference(preferSuppliedValueBy(0.25)).betweenSuppliedValue(2).andDefaultValue(-18),
        1e-8);
  }

}
