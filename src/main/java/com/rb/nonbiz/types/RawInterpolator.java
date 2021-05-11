package com.rb.nonbiz.types;

import com.rb.nonbiz.util.RBPreconditions;

/**
 * This is like a builder, so it's mutable.
 * It allows for a fluent representation of an interpolation.
 */
public class RawInterpolator {

  private final UnitFraction leftPreference;
  private Double from;

  private RawInterpolator(UnitFraction leftPreference) {
    this.leftPreference = leftPreference;
  }

  public static RawInterpolator interpolateWithLeftPreference(UnitFraction leftPreference) {
    return new RawInterpolator(leftPreference);
  }

  public RawInterpolator from(double from) {
    RBPreconditions.checkArgument(
        this.from == null,
        "from is %s and you are trying to reset it to %s , which is probably an error",
        this.from, from);
    this.from = from;
    return this;
  }

  public double to(double to) {
    RBPreconditions.checkNotNull(
        from,
        "You forgot to specify a 'from' value in the Interpolator");
    return leftPreference.doubleValue() * from
        + (1 - leftPreference.doubleValue()) * to;
  }

}
