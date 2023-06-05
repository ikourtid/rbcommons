package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import static com.rb.nonbiz.math.stats.StatisticalSummaryImpl.StatisticalSummaryImplBuilder.statisticalSummaryImplBuilder;

/**
 * An implementation of Apache's {@code StatisticalSummary} interface.
 *
 * <p> This simply holds the results; it does not calculate them. </p>
 *
 * <p> The initial use case (June 2023) is to be able to write {@code StatisticalSummary}s
 * to/from JSON. We cannot construct an Apache {@code StatisticalSummary} from JSON
 * summary entries since the Apache class is built up by adding one data point at at time. </p>
 */
public class StatisticalSummaryImpl implements StatisticalSummary {

  private final long   n;
  private final double mean;
  private final double min;
  private final double max;
  private final double stdDev;
  private final double variance;
  private final double sum;

  private StatisticalSummaryImpl(
      long   n,
      double mean,
      double min,
      double max,
      double standardDeviation,
      double variance,
      double sum) {
    this.n        = n;
    this.mean     = mean;
    this.min      = min;
    this.max      = max;
    this.stdDev   = standardDeviation;
    this.variance = variance;
    this.sum      = sum;
  }

  /**
   * A convenience constructor given an Apache {@code SummaryStatistics}.
   * Just copy the summary statistics.
   */
  public static StatisticalSummaryImpl statisticalSummaryImpl(StatisticalSummary statisticalSummary) {
    return statisticalSummaryImplBuilder()
        .setN(                statisticalSummary.getN())
        .setMean(             statisticalSummary.getMean())
        .setMin(              statisticalSummary.getMin())
        .setMax(              statisticalSummary.getMax())
        .setStandardDeviation(statisticalSummary.getStandardDeviation())
        .build();
  }

  @Override
  public long getN() {
    return n;
  }

  @Override
  public double getMean() {
    return mean;
  }

  @Override
  public double getMin() {
    return min;
  }

  @Override
  public double getMax() {
    return max;
  }

  @Override
  public double getStandardDeviation() {
    return stdDev;
  }

  @Override
  public double getVariance() {
    return variance;
  }

  @Override
  public double getSum() {
    return sum;
  }

  @Override
  public String toString() {
    return Strings.format("[SSI n= %s ; mean= %s ; min= %s ; max= %s ; stdDev= %s ; variance= %s ; sum= %s SSI]",
        n, mean, min, max, stdDev, variance, sum);
  }


  /**
   * An {@link RBBuilder} for a {@link StatisticalSummaryImpl}.
   */
  public static class StatisticalSummaryImplBuilder implements RBBuilder<StatisticalSummaryImpl> {

    // use Long/Double instead of long/double so that we can use checkNotAlreadySet() below
    private Long   n;
    private Double mean;
    private Double min;
    private Double max;
    private Double standardDeviation;

    private StatisticalSummaryImplBuilder() {}

    public static StatisticalSummaryImplBuilder statisticalSummaryImplBuilder() {
      return new StatisticalSummaryImplBuilder();
    }

    public StatisticalSummaryImplBuilder setN(long n) {
      this.n = checkNotAlreadySet(this.n, n);
      return this;
    }

    public StatisticalSummaryImplBuilder setMean(double mean) {
      this.mean = checkNotAlreadySet(this.mean, mean);
      return this;
    }

    public StatisticalSummaryImplBuilder setMin(double min) {
      this.min = checkNotAlreadySet(this.min, min);
      return this;
    }

    public StatisticalSummaryImplBuilder setMax(double max) {
      this.max = checkNotAlreadySet(this.max, max);
      return this;
    }

    public StatisticalSummaryImplBuilder setStandardDeviation(double standardDeviation) {
      this.standardDeviation = checkNotAlreadySet(this.standardDeviation, standardDeviation);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkArgument(
          n >= 1L,
          "Must have at least one data point, but n= %s",
          n);
      RBPreconditions.checkArgument(
          min <= max,
          "Can't have min %s > max %s",
          min, max);
      RBPreconditions.checkArgument(
          min <= mean && mean <= max,
          "Can't have mean %s < min %s or > max %s",
          mean, min, max);
      RBPreconditions.checkArgument(
          standardDeviation >= 0,
          "Can't have a negative standard deviation: %s",
          standardDeviation);
    }

    @Override
    public StatisticalSummaryImpl buildWithoutPreconditions() {
      return new StatisticalSummaryImpl(
          n,
          mean,
          min,
          max,
          standardDeviation,
          standardDeviation * standardDeviation,  // variance = stdDev * stdDev
          n * mean);                              // sum = n * mean

    }
  }

}
