package com.rb.nonbiz.types;

import com.rb.nonbiz.util.RBPreconditions;

/**
 * Represents an epsilon (small number) used to compare two numbers for 'rough equality',
 * which must be strictly positive.
 * i.e. if their values are 'close enough' subject to the epsilon.
 */
public class PositiveEpsilon extends Epsilon {

  public static final PositiveEpsilon DEFAULT_POSITIVE_EPSILON_1e_8       = positiveEpsilon(1e-8);

  protected PositiveEpsilon(double value) {
    super(value);
  }

  public static PositiveEpsilon positiveEpsilon(double value) {
    RBPreconditions.checkArgument(value > 0.0);

    // Model after pre-conditions in pseudo-constructor for Epsilon class, don't allow anything 100 or over.
    RBPreconditions.checkArgument(value <= MAX_ALLOWED_EPSILON_VALUE);
    return new PositiveEpsilon(value);
  }

}
