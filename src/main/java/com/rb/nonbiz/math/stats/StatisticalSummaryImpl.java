package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * An implementation of Apache's {@code StatisticalSummary} interface.
 *
 * <p> This simply holds the results; it does not calculate them. </p>
 *
 * <p> The purpose is to be able to write {@code StatisticalSummary}s to/from JSON.
 * We aren't able to read in Apache's {@code StatisticalSummary} from JSON
 * since they are built up by adding one point after the other. </p>
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
    // We could have more checks, but we don't want to make it too hard to construct test data.
    RBPreconditions.checkArgument(
        n >= 0,
        "Can't have negative number of points 'n': %s",
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
        "Can't have a negative standard deviation: %s",
        stdDev);
    RBPreconditions.checkArgument(
        variance >= 0,
        "Can't have a negative variance: %s",
        variance);
    return new StatisticalSummaryImpl(n, mean, min, max, stdDev, variance, sum);
  }

  /**
   * A constructor given an Apache {@code SummaryStatistics}. Just copy the summary statistics.
   */
  public static StatisticalSummaryImpl statisticalSummaryImpl(SummaryStatistics summaryStatistics) {
    return statisticalSummaryImpl(
        summaryStatistics.getN(),
        summaryStatistics.getMean(),
        summaryStatistics.getMin(),
        summaryStatistics.getMax(),
        summaryStatistics.getStandardDeviation(),
        summaryStatistics.getVariance(),
        summaryStatistics.getSum());
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
