package com.rb.nonbiz.types;

import com.rb.nonbiz.util.RBPreconditions;

/**
 * Represents an epsilon (small number) used to compare two numbers for 'rough equality',
 * which must be strictly positive.
 * i.e. if their values are 'close enough' subject to the epsilon.
 */
public class PositiveEpsilon extends Epsilon {

  public static final PositiveEpsilon DEFAULT_POSITIVE_EPSILON_1e_8 = positiveEpsilon(1e-8);

  protected PositiveEpsilon(double value) {
    super(value);
  }

  public static PositiveEpsilon positiveEpsilon(double value) {
    RBPreconditions.checkArgument(value > 0.0);

    // Model after pre-conditions in pseudo-constructor for Epsilon class, don't allow anything 100 or over.
    RBPreconditions.checkArgument(value <= MAX_ALLOWED_EPSILON_VALUE);
    return new PositiveEpsilon(value);
  }

  /**
   * In certain rare cases, we want to have a larger epsilon. An example is net traded notional in an optimization
   * of a portfolio with a very large value. In those cases, we can use this one to get around the limitation around
   * the magnitude of the epsilon. The name is explicit enough, so there's a very low chance of messing up.
   * We can't normally just remove preconditions from static constructors, but note that the size limitation here
   * is really just a sanity check; it does not affect any behavior. That is, no code will look at the value of this
   * and fail because it's > MAX_ALLOWED_EPSILON_VALUE.
   */
  public static PositiveEpsilon largePositiveEpsilon(double value) {
    return new PositiveEpsilon(value);
  }

}
