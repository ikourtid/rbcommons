package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

/**
 * This is effectively a generalization of a boolean.
 *
 * <p> There are often cases where there is a number that we <i>could</i> use in a calculation,
 * and we want to decide to either use it, use a default value (whatever the default happens to be - not our job here),
 * or just use something in between. </p>
 *
 * This is effectively a {@link UnitFraction}, but with more explicit semantics.
 *
 * @see Interpolator
 */
public class InterpolationPreference {

  public static InterpolationPreference USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED = preferSuppliedValueBy(UNIT_FRACTION_0);
  public static InterpolationPreference USE_SUPPLIED_VALUE_INSTEAD_OF_DEFAULT = preferSuppliedValueBy(UNIT_FRACTION_1);

  private final UnitFraction rawPreferenceForSuppliedValue;

  private InterpolationPreference(UnitFraction rawPreferenceForSuppliedValue) {
    this.rawPreferenceForSuppliedValue = rawPreferenceForSuppliedValue;
  }

  public static InterpolationPreference preferSuppliedValueBy(UnitFraction rawPreference) {
    return new InterpolationPreference(rawPreference);
  }

  public static InterpolationPreference preferSuppliedValueBy(double rawPreference) {
    return new InterpolationPreference(unitFraction(rawPreference));
  }

  public UnitFraction getRawPreferenceForSuppliedValue() {
    return rawPreferenceForSuppliedValue;
  }

  @Override
  public String toString() {
    return Strings.format("[IP %s IP]", rawPreferenceForSuppliedValue);
  }

  public boolean ignoresSuppliedValue() {
    return rawPreferenceForSuppliedValue.isZero();
  }

}
