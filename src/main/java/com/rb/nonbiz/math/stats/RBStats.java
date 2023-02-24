package com.rb.nonbiz.math.stats;

import com.google.common.collect.Iterators;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.util.FastMath;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;

import static com.rb.nonbiz.collections.RBIterators.transformToDoubleIterator;
import static com.rb.nonbiz.math.stats.ZScore.zScore;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

public class RBStats {

  public static StatisticalSummary toBigDecimalStatisticalSummary(Iterable<BigDecimal> iterable) {
    return toBigDecimalStatisticalSummary(iterable.iterator());
  }

  public static StatisticalSummary toStatisticalSummary(double first, double ... rest) {
    SummaryStatistics summaryStatistics = new SummaryStatistics();
    summaryStatistics.addValue(first);
    for (double v : rest) {
      summaryStatistics.addValue(v);
    }
    return summaryStatistics;
  }

  /**
   * You should use {@link RBStatisticalSummary}{@code <T>} instead of StatisticalSummary, when possible.
   */
  @SafeVarargs
  public static <P extends PreciseValue<? super P>> StatisticalSummary toStatisticalSummary(P first, P ... rest) {
    SummaryStatistics summaryStatistics = new SummaryStatistics();
    summaryStatistics.addValue(first.doubleValue());
    for (P v : rest) {
      summaryStatistics.addValue(v.doubleValue());
    }
    return summaryStatistics;
  }

  /**
   * You should use {@link RBStatisticalSummary}{@code <T>} instead of StatisticalSummary, when possible.
   */
  @SafeVarargs
  public static <P extends ImpreciseValue<? super P>> StatisticalSummary toStatisticalSummary(P first, P ... rest) {
    SummaryStatistics summaryStatistics = new SummaryStatistics();
    summaryStatistics.addValue(first.doubleValue());
    for (P v : rest) {
      summaryStatistics.addValue(v.doubleValue());
    }
    return summaryStatistics;
  }

  public static StatisticalSummary toStatisticalSummary(Iterable<Double> iterable) {
    return toStatisticalSummary(iterable.iterator());
  }

  public static StatisticalSummary toIntStatisticalSummary(Iterable<Integer> iterable) {
    return toIntStatisticalSummary(iterable.iterator());
  }

  public static StatisticalSummary toBigDecimalStatisticalSummary(Iterator<BigDecimal> iterator) {
    SummaryStatistics summaryStatistics = new SummaryStatistics();
    RBPreconditions.checkArgument(
        iterator.hasNext(),
        "You cannot compute BigDecimal statistics if there are no data");
    while (iterator.hasNext()) {
      summaryStatistics.addValue(iterator.next().doubleValue());
    }
    return summaryStatistics;
  }

  public static <T extends ImpreciseValue<? super T>> StatisticalSummary toStatisticalSummaryFromImpreciseValues(Iterator<T> iterator) {
    return toStatisticalSummary(transformToDoubleIterator(iterator));
  }

  public static <T extends PreciseValue<? super T>> StatisticalSummary toStatisticalSummaryFromPreciseValues(Iterator<T> iterator) {
    return toStatisticalSummary(Iterators.transform(iterator, v -> v.doubleValue()));
  }

  public static ZScore getZScore(double value, StatisticalSummary statisticalSummary) {
    double stdDev = statisticalSummary.getStandardDeviation();
    // The standard deviation will either be 0, in the extreme case of all items being the same,
    // or - if it' tiny - it will be the result of calculating a std dev over very tightly spaced numbers,
    // in which case using an epsilon of e.g. 1e-8 will cause us to throw on calculating those z-scores.
    // Therefore, I'll be throwing on 0 (obviously - it's a divide by zero exception) but not on e.g. 1e-8.
    if (stdDev == 0) { // rare double equality, but might as well
      throw new IllegalArgumentException(smartFormat(
          "You are trying to take a z-score for value= %s when the std dev is 0 : %s",
          statisticalSummary, formatStatisticalSummary(statisticalSummary)));
    }
    return zScore((value - statisticalSummary.getMean()) / stdDev);
  }

  public static StatisticalSummary toStatisticalSummary(Iterator<Double> iterator) {
    SummaryStatistics summaryStatistics = new SummaryStatistics();
    RBPreconditions.checkArgument(
        iterator.hasNext(),
        "You cannot compute double statistics if there are no data");
    while (iterator.hasNext()) {
      summaryStatistics.addValue(iterator.next());
    }
    return summaryStatistics;
  }

  public static StatisticalSummary toIntStatisticalSummary(Iterator<Integer> iterator) {
    SummaryStatistics summaryStatistics = new SummaryStatistics();
    RBPreconditions.checkArgument(
        iterator.hasNext(),
        "You cannot compute int statistics if there are no data");
    while (iterator.hasNext()) {
      summaryStatistics.addValue(iterator.next());
    }
    return summaryStatistics;
  }

  public static String formatStatisticalSummary(StatisticalSummary statisticalSummary) {
    NumberFormat numberFormat = new DecimalFormat();
    numberFormat.setMinimumFractionDigits(4);
    numberFormat.setMaximumFractionDigits(4);
    return formatStatisticalSummary(statisticalSummary, numberFormat);
  }

  public static String formatStatisticalSummary(StatisticalSummary statisticalSummary, int nDigits) {
    NumberFormat numberFormat = new DecimalFormat();
    numberFormat.setMinimumFractionDigits(nDigits);
    numberFormat.setMaximumFractionDigits(nDigits);
    return formatStatisticalSummary(statisticalSummary, numberFormat);
  }

  public static String formatStatisticalSummary(
      StatisticalSummary statisticalSummary, NumberFormat numberFormat) {
    double stdDev = statisticalSummary.getStandardDeviation();
    double standardError = stdDev / FastMath.sqrt(statisticalSummary.getN());

    return Strings.format("[ %s %s %s %s %s ] %s %s",
        numberFormat.format(noNegativeZero(statisticalSummary.getMin())),
        numberFormat.format(noNegativeZero(statisticalSummary.getMean() - 2 * standardError)),
        numberFormat.format(noNegativeZero(statisticalSummary.getMean())),
        numberFormat.format(noNegativeZero(statisticalSummary.getMean() + 2 * standardError)),
        numberFormat.format(noNegativeZero(statisticalSummary.getMax())),
        numberFormat.format(noNegativeZero(stdDev)),
        statisticalSummary.getN());
  }

  public static <V extends Comparable<? super V>> String formatRBStatisticalSummary(
      RBStatisticalSummary<V> statisticalSummary) {
    return formatStatisticalSummary(statisticalSummary.getRawStatisticalSummary());
  }

  // avoid printing negative zeros "-0.0"
  private static double noNegativeZero(double x) {
    return (-1e-12 < x && x < 0) ? 0 : x;
  }

}

