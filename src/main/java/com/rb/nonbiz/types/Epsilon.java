package com.rb.nonbiz.types;

import com.rb.nonbiz.util.RBPreconditions;

/**
 * Represents an epsilon (small number) used to compare two numbers for 'rough equality',
 * i.e. if their values are 'close enough' subject to the epsilon.
 */
public class Epsilon extends ImpreciseValue<Epsilon> {

  public static final Epsilon DEFAULT_EPSILON_1e_8       = epsilon(1e-8);
  public static final Epsilon ZERO_EPSILON               = epsilon(0);
  public static final double MAX_ALLOWED_EPSILON_VALUE   = 99.9999;

  protected Epsilon(double value) {
    super(value);
  }

  public static Epsilon epsilon(double value) {
    // There's nothing special about 100, but it's rare to use an epsilon bigger than 1, let alone 100.
    // This is particularly useful for cases where one misspells e.g. 1e-6 to 1e6.
    RBPreconditions.checkArgument(
        0 <= value && value <= MAX_ALLOWED_EPSILON_VALUE,
        "Epsilon must be in [0, %s]; was %s", MAX_ALLOWED_EPSILON_VALUE, value);
    return new Epsilon(value);
  }

  /**
   * In certain rare cases, we want to have a larger epsilon. An example is net traded notional in an optimization
   * of a portfolio with a very large value. In those cases, we can use this one to get around the limitation around
   * the magnitude of the epsilon. The name is explicit enough, so there's a very low chance of messing up.
   * We can't normally just remove preconditions from static constructors, but note that the size limitation here
   * is really just a sanity check; it does not affect any behavior. That is, no code will look at the value of this
   * and fail because it's > MAX_ALLOWED_EPSILON_VALUE.
   */
  public static Epsilon largeEpsilon(double value) {
    return new Epsilon(value);
  }

  /**
   * Returns true if the two numbers are within this epsilon.
   */
  public boolean valuesAreWithin(double value1, double value2) {
    return Math.abs(value1 - value2) <= doubleValue();
  }

  /**
   * Returns true if the two {@link PreciseValue}s are within this epsilon.
   *
   * <p> {@link PreciseValue} already has a method for this, but having a method under {@link Epsilon} can sometimes
   * make the syntax easier to read. </p>
   */
  public <T extends PreciseValue<T>> boolean valuesAreWithin(T value1, T value2) {
    return value1.almostEquals(value2, this);
  }

  /**
   * Returns true if the two {@link PreciseValue}s are within this epsilon.
   *
   * <p> {@link ImpreciseValue} already has a method for this, but having a method under {@link Epsilon} can sometimes
   * make the syntax easier to read. </p>
   */
  public <T extends ImpreciseValue<T>> boolean valuesAreWithin(T value1, T value2) {
    return value1.almostEquals(value2, this);
  }

  /**
   * Returns true if the two numbers are within this epsilon.
   */
  public boolean isAlmostZero(double value) {
    return valuesAreWithin(value, 0);
  }

}
