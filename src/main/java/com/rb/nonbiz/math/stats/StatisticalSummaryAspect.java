package com.rb.nonbiz.math.stats;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

/**
 * This is a way to refer to the fields of {@link StatisticalSummary} programmatically.
 */
public enum StatisticalSummaryAspect {

  STATISTICAL_SUMMARY_MEAN("mean", "means"),
  STATISTICAL_SUMMARY_VARIANCE("variance", "variances"),
  STATISTICAL_SUMMARY_STANDARD_DEVIATION("stdev", "stdevs"),
  STATISTICAL_SUMMARY_MAX("max", "maxes"),
  STATISTICAL_SUMMARY_MIN("min", "mins"),
  STATISTICAL_SUMMARY_N("count", "counts"),
  STATISTICAL_SUMMARY_SUM("sum", "sums");

  private final String singular;
  private final String plural;

  StatisticalSummaryAspect(String singular, String plural) {
    this.singular = singular;
    this.plural = plural;
  }

  public String getSingular() {
    return singular;
  }

  public String getPlural() {
    return plural;
  }

  public static double getStatisticalSummaryField(
      StatisticalSummary statisticalSummary,
      StatisticalSummaryAspect statisticalSummaryAspect) {
    switch (statisticalSummaryAspect) {
      case STATISTICAL_SUMMARY_MEAN:
        return statisticalSummary.getMean();
      case STATISTICAL_SUMMARY_VARIANCE:
        return statisticalSummary.getVariance();
      case STATISTICAL_SUMMARY_STANDARD_DEVIATION:
        return statisticalSummary.getStandardDeviation();
      case STATISTICAL_SUMMARY_MAX:
        return statisticalSummary.getMax();
      case STATISTICAL_SUMMARY_MIN:
        return statisticalSummary.getMin();
      case STATISTICAL_SUMMARY_N:
        return statisticalSummary.getN();
      case STATISTICAL_SUMMARY_SUM:
        return statisticalSummary.getSum();
      default:
        throw new IllegalArgumentException("Unsupported StatisticalSummaryField of " + statisticalSummaryAspect);
    }
  }

}
