package com.rb.nonbiz.types;

import com.rb.nonbiz.util.RBPreconditions;

/**
 * A builder object (and therefore mutable, despite our usual convention)
 * that allows for a fluent representation of an interpolation.
 */
public class Interpolator {

  private final InterpolationPreference interpolationPreference;
  private Double suppliedValue;

  private Interpolator(InterpolationPreference interpolationPreference) {
    this.interpolationPreference = interpolationPreference;
  }

  public static Interpolator interpolateUsingPreference(InterpolationPreference interpolationPreference) {
    return new Interpolator(interpolationPreference);
  }

  public Interpolator betweenSuppliedValue(double suppliedValue) {
    RBPreconditions.checkArgument(
        this.suppliedValue == null,
        "suppliedValue is %s and you are trying to reset it to %s , which is probably an error",
        this.suppliedValue, suppliedValue);
    this.suppliedValue = suppliedValue;
    return this;
  }

  public double andDefaultValue(double defaultValue) {
    RBPreconditions.checkNotNull(
        suppliedValue,
        "You forgot to specify a 'supplied value' in the Interpolator");
    double preferenceForSuppliedValue = interpolationPreference.getRawPreferenceForSuppliedValue().doubleValue();
    return preferenceForSuppliedValue * suppliedValue
        + (1 - preferenceForSuppliedValue) * defaultValue;
  }

}
