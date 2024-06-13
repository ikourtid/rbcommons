package com.rb.nonbiz.math.stats;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import java.util.OptionalInt;

import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptionalInt;

/**
 * Generates random numbers from a normal distribution.
 *
 * <p> This is a special object, hence 'mutable' in the name. It contains a random number generator,
 * which we would normally want to seed: it makes tests deterministic, among other things.
 * Therefore, every time we ask it to generate a new number, its state will change. </p>
 *
 * <p> It's more like a stateful verb class instead of a mutable data class. </p>
 */
public class MutableNormalDistributionGenerator {

  // 'Normal' is a Colt object representing both the NormalDistribution, as well as the internal state (usually
  // seeded) used to generate the next value.
  private final Normal normal;
  private final NormalDistribution normalDistribution;

  private MutableNormalDistributionGenerator(Normal normal, NormalDistribution normalDistribution) {
    this.normal = normal;
    this.normalDistribution = normalDistribution;
  }

  public static MutableNormalDistributionGenerator mutableNormalDistributionGenerator(
      NormalDistribution normalDistribution, OptionalInt seed) {
    // Apparently this is a good pseudo-random generator.
    // https://dst.lbl.gov/ACSSoftware/colt/api/cern/jet/random/engine/MersenneTwister.html
    RandomEngine randomEngine = transformOptionalInt(
        seed,
        v -> new MersenneTwister(v))
        .orElse(new MersenneTwister());
    return new MutableNormalDistributionGenerator(
        new Normal(normalDistribution.getMean(), normalDistribution.getStandardDeviation(), randomEngine),
        normalDistribution);
  }

  public static MutableNormalDistributionGenerator mutableNormalDistributionGeneratorWithSeed(
      NormalDistribution normalDistribution,
      int seed) {
    return mutableNormalDistributionGenerator(normalDistribution, OptionalInt.of(seed));
  }

  public static MutableNormalDistributionGenerator mutableNormalDistributionGeneratorWithoutSeed(
      NormalDistribution normalDistribution) {
    return mutableNormalDistributionGenerator(normalDistribution, OptionalInt.empty());
  }

  /**
   * Returns an object representing the mean and stdev of this distribution (i.e. not a random number generator).
   */
  public NormalDistribution getNormalDistribution() {
    return normalDistribution;
  }

  /**
   * Returns the Colt random number generator object. You should not have to use this normally. In most cases you
   * probably just want to use {@link #nextDouble()}.
   */
  public Normal getNormal() {
    return normal;
  }

  /**
   * Generate a random number. If you cared about seeding the generator upon construction, this will modify
   * its internal state with respect to the seed.
   */
  public double nextDouble() {
    return normal.nextDouble();
  }

}
