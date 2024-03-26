package com.rb.nonbiz.types;

import com.rb.nonbiz.util.RBPreconditions;

import java.util.Collection;

/**
 * A simple class to hold a non-negative number meant to be used as a multiplier
 * of some other numeric quantity.
 *
 * @see PositiveMultiplier
 */
public class NonNegativeMultiplier extends ImpreciseValue<NonNegativeMultiplier> {

  public static NonNegativeMultiplier NON_NEGATIVE_MULTIPLIER_0 = nonNegativeMultiplier(0.0);
  public static NonNegativeMultiplier NON_NEGATIVE_MULTIPLIER_1 = nonNegativeMultiplier(1.0);

  protected NonNegativeMultiplier(double multiplier) {
    super(multiplier);
  }

  public static NonNegativeMultiplier nonNegativeMultiplier(double multiplier) {
    RBPreconditions.checkArgument(
        multiplier >= 0,
        "Encountered negative multiplier %s",
        multiplier);
    return new NonNegativeMultiplier(multiplier);
  }

  public NonNegativeMultiplier multiply(NonNegativeMultiplier other) {
    return nonNegativeMultiplier(this.doubleValue() * other.doubleValue());
  }

  public static NonNegativeMultiplier sum(Collection<NonNegativeMultiplier> collection) {
    return nonNegativeMultiplier(collection.stream().mapToDouble(v -> v.doubleValue()).sum());
  }

}
