package com.rb.nonbiz.types;

import com.rb.nonbiz.util.RBPreconditions;

/**
 * Just an int, but with the semantics that it will be used as a seed in a random number generator.
 *
 * <p> This may be overly restrictive, but we only allow non-negative seeds. </p>
 */
public class RandomNumberGeneratorSeed extends IntegerValue<RandomNumberGeneratorSeed> {

  protected RandomNumberGeneratorSeed(int seed) {
    super(seed);
  }

  public static RandomNumberGeneratorSeed randomNumberGeneratorSeed(int seed) {
    RBPreconditions.checkArgument(
        seed >= 0,
        "We restrict random number generator seeds to be positive; was %s",
        seed);
    return new RandomNumberGeneratorSeed(seed);
  }

  @Override
  public String toString() {
    return Integer.toString(intValue());
  }

}
