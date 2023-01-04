package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.InterpolationPreference.USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED;
import static com.rb.nonbiz.types.InterpolationPreference.USE_SUPPLIED_VALUE_INSTEAD_OF_DEFAULT;
import static com.rb.nonbiz.types.InterpolationPreference.preferSuppliedValueBy;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class InterpolationPreferenceTest extends RBTestMatcher<InterpolationPreference> {

  @Test
  public void testIgnoresSuppliedValuePredicate() {
    assertTrue(USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED.ignoresSuppliedValue());
    assertFalse(preferSuppliedValueBy(unitFraction(0.123)).ignoresSuppliedValue());
    assertFalse(USE_SUPPLIED_VALUE_INSTEAD_OF_DEFAULT.ignoresSuppliedValue());
  }

  @Override
  public InterpolationPreference makeTrivialObject() {
    return USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED;
  }

  @Override
  public InterpolationPreference makeNontrivialObject() {
    return preferSuppliedValueBy(unitFraction(0.123));
  }

  @Override
  public InterpolationPreference makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return preferSuppliedValueBy(unitFraction(0.123 + e));
  }

  @Override
  protected boolean willMatch(InterpolationPreference expected, InterpolationPreference actual) {
    return interpolationPreferenceMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<InterpolationPreference> interpolationPreferenceMatcher(InterpolationPreference expected) {
    return makeMatcher(expected,
        matchUsingAlmostEquals(v -> v.getRawPreferenceForSuppliedValue(), DEFAULT_EPSILON_1e_8));
  }

}
