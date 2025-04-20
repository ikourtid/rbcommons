package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * A simple representation of a normal distribution; just the mean and stdev.
 *
 * <p> {@link MutableNormalDistributionGenerator} generates random numbers from this distribution. </p>
 */
public class NormalDistribution {

  private final double mean;
  private final double standardDeviation;

  private NormalDistribution(double mean, double standardDeviation) {
    this.mean = mean;
    this.standardDeviation = standardDeviation;
  }

  public double getMean() {
    return mean;
  }

  public double getStandardDeviation() {
    return standardDeviation;
  }

  @Override
  public String toString() {
    return Strings.format("[ND mean= %s ; stdev= %s ND]");
  }


  public static class NormalDistributionBuilder implements RBBuilder<NormalDistribution> {

    private Double mean;
    private Double standardDeviation;

    private NormalDistributionBuilder() {}

    public static NormalDistributionBuilder normalDistributionBuilder() {
      return new NormalDistributionBuilder();
    }

    public NormalDistributionBuilder setMean(double mean) {
      this.mean = checkNotAlreadySet(this.mean, mean);
      return this;
    }

    public NormalDistributionBuilder setStandardDeviation(double standardDeviation) {
      this.standardDeviation = checkNotAlreadySet(this.standardDeviation, standardDeviation);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(mean);
      RBPreconditions.checkNotNull(standardDeviation);

      RBPreconditions.checkArgument(
          standardDeviation >= 0,
          "Standard deviation cannot be negative; was %s",
          standardDeviation);
    }

    @Override
    public NormalDistribution buildWithoutPreconditions() {
      return new NormalDistribution(mean, standardDeviation);
    }

  }

}
