package com.rb.nonbiz.types;

import com.rb.nonbiz.util.RBPreconditions;

/**
 * A simple class to hold a positive number meant to be used as positive multiplier to some other numeric quantity.
 */
public class PositiveMultiplier extends ImpreciseValue<PositiveMultiplier> {

  public static PositiveMultiplier POSITIVE_MULTIPLIER_1 = positiveMultiplier(1.0);

  protected PositiveMultiplier(double multiplier) {
    super(multiplier);
  }

  public static PositiveMultiplier positiveMultiplier(double multiplier) {
    RBPreconditions.checkArgument(
        multiplier > 0,
        "Encountered non-positive multiplier %s",
        multiplier);
    return new PositiveMultiplier(multiplier);
  }

  public PositiveMultiplier multiply(PositiveMultiplier other) {
    return positiveMultiplier(this.doubleValue() * other.doubleValue());
  }

}
