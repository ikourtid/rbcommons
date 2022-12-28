package com.rb.nonbiz.types;

import com.rb.nonbiz.util.RBPreconditions;

/**
 * Represents an epsilon (small number) used to compare two numbers for 'rough equality',
 * i.e. if their values are 'close enough' subject to the epsilon.
 */
public class Epsilon extends ImpreciseValue<Epsilon> {

  public static final Epsilon DEFAULT_EPSILON_1e_8 = epsilon(1e-8);
  public static final Epsilon ZERO_EPSILON         = epsilon(0);

  protected Epsilon(double value) {
    super(value);
  }

  public static Epsilon epsilon(double value) {
    // There's nothing special about 100, but it's rare to use an epsilon bigger than 1, let alone 100.
    // This is particularly useful for cases where one misspells e.g. 1e-6 to 1e6.
    RBPreconditions.checkArgument(
        0 <= value && value < 100,
        "Epsilon must be in [0, 100); was %s",
        value);
    return new Epsilon(value);
  }

  /**
   * Returns true if the two numbers are within this epsilon.
   */
  public boolean areWithin(double value1, double value2) {
    return Math.abs(value1 - value2) <= doubleValue();
  }

  /**
   * Returns true if the two numbers are within this epsilon.
   */
  public boolean isAlmostZero(double value) {
    return areWithin(value, 0);
  }

}
