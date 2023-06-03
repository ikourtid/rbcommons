package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

/**
 * An implementation of Apache's {@code StatisticalSummary}. It holds the results,
 * but does not have an addPoint() method.
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

  public static StatisticalSummaryImpl statisticalSummaryImpl(
      long   n,
      double mean,
      double min,
      double max,
      double stdDev,
      double variance,
      double sum) {
    RBPreconditions.checkArgument(
        n >= 0,
        "Can't have negative n: %s",
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
        stdDev >= 0,
        "Can't have negative stdDev %s",
        stdDev);
    RBPreconditions.checkArgument(
        variance >= 0,
        "Can't have negative variance %s",
        variance);
    return new StatisticalSummaryImpl(n, mean, min, max, stdDev, variance, sum);
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

}
