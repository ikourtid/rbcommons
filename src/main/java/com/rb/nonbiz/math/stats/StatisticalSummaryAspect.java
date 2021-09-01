package com.rb.nonbiz.math.stats;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

/**
 * This is a way to refer to the fields of {@link StatisticalSummary} programmatically.
 */
public enum StatisticalSummaryAspect {

  STATISTICAL_SUMMARY_MEAN,
  STATISTICAL_SUMMARY_VARIANCE,
  STATISTICAL_SUMMARY_STANDARD_DEVIATION,
  STATISTICAL_SUMMARY_MAX,
  STATISTICAL_SUMMARY_MIN,
  STATISTICAL_SUMMARY_N,
  STATISTICAL_SUMMARY_SUM;

  // FIXME IAK ESGSTATS test this
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
