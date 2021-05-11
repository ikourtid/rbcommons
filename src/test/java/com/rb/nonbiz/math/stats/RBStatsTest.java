package com.rb.nonbiz.math.stats;

import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.math.stats.RBStats.formatStatisticalSummary;
import static com.rb.nonbiz.math.stats.RBStats.getZScore;
import static com.rb.nonbiz.math.stats.RBStats.toStatisticalSummary;
import static com.rb.nonbiz.math.stats.ZScore.Z_SCORE_0;
import static com.rb.nonbiz.math.stats.ZScore.zScore;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static java.util.Collections.emptyIterator;
import static org.junit.Assert.assertEquals;

public class RBStatsTest {

  @Test
  public void toStatisticalSummary_noData_throws() {
    assertIllegalArgumentException( () -> toStatisticalSummary(emptyIterator()));
  }

  @Test
  public void toStatisticalSummary_hasData_returnsProperStatistics() {
    for (StatisticalSummary statisticalSummary : rbSetOf(
        toStatisticalSummary(ImmutableList.of(7.7, -3.3, 1.1).iterator()),
        RBStats.toStatisticalSummary(ImmutableList.of(7.7, -3.3, 1.1)),
        RBStats.toStatisticalSummary(7.7, -3.3, 1.1))) {
      assertEquals(3, statisticalSummary.getN());
      assertEquals(-3.3, statisticalSummary.getMin(), 1e-8);
      assertEquals(7.7, statisticalSummary.getMax(), 1e-8);
      assertEquals(doubleExplained(1.8333333333, (7.7 - 3.3 + 1.1) / 3), statisticalSummary.getMean(), 1e-8);
    }
  }

  @Test
  public void testFormatStatisticalSummary() {
    SummaryStatistics stats = new SummaryStatistics();
    stats.addValue(3.0);
    stats.addValue(4.0);
    stats.addValue(5.0);
    // bin/stats1 shows me
    // [ 3.0000  2.8453  4.0000  5.1547  5.0000  ] 1.0000  0.6667  3
    // I'm guessing the difference is due to that population standard deviation distinction? not sure.
    // Anyway, the underlying library is Apache, so I'm sure it's not wrong.
    {
      NumberFormat format = new DecimalFormat();
      format.setMinimumFractionDigits(4);
      format.setMaximumFractionDigits(4);
      assertEquals(
          "[ 3.0000 2.8453 4.0000 5.1547 5.0000 ] 1.0000 3",
          formatStatisticalSummary(stats, format));
    }
    {
      NumberFormat format = new DecimalFormat();
      format.setMinimumFractionDigits(2);
      format.setMaximumFractionDigits(2);
      assertEquals(
          "[ 3.00 2.85 4.00 5.15 5.00 ] 1.00 3",
          formatStatisticalSummary(stats, format));
    }
  }

  @Test
  public void doNotPrintNegativeZeros() {
    double e = 1e-13; // epsilon
    Function<Double, SummaryStatistics> maker = mean -> {
      SummaryStatistics stats = new SummaryStatistics();
      // stddev is sqrt(3); stderr is 1.0
      stats.addValue(mean - e - Math.sqrt(3));
      stats.addValue(mean - e);
      stats.addValue(mean - e + Math.sqrt(3));
      return stats;
    };

    double sqrt3 = Math.sqrt(3);
    // sample stddev; divide by (n - 1)
    double stddev = doubleExplained(sqrt3, Math.sqrt(((-sqrt3) * (-sqrt3) + 0 * 0 + sqrt3 * sqrt3) / (3 - 1)));
    for (double mean : rbSetOf(-2.0, 0.0, 2.0)) {
      SummaryStatistics summaryStatistics = maker.apply(mean);
      assertEquals(mean,         summaryStatistics.getMean(), 1e-8);
      assertEquals(mean - sqrt3, summaryStatistics.getMin(),  1e-8);
      assertEquals(mean + sqrt3, summaryStatistics.getMax(),  1e-8);
      assertEquals(sqrt3,        summaryStatistics.getStandardDeviation(), 1e-8);
    }

    NumberFormat format = new DecimalFormat();
    format.setMinimumFractionDigits(4);
    format.setMaximumFractionDigits(4);

    // mean plus 2 * stderr is -epsilon, but prints as 0.0
    assertEquals(
        "[ -3.7321 -4.0000 -2.0000 0.0000 -0.2679 ] 1.7321 3",
        formatStatisticalSummary(maker.apply(-2.0), format));
    // mean is -epsilon, but prints as 0.0
    assertEquals(
        "[ -1.7321 -2.0000 0.0000 2.0000 1.7321 ] 1.7321 3",
        formatStatisticalSummary(maker.apply(0.0), format));
    // mean minus 2 * stderr is -epsilon, but prints as 0.0
    assertEquals(
        "[ 0.2679 0.0000 2.0000 4.0000 3.7321 ] 1.7321 3",
        formatStatisticalSummary(maker.apply(2.0), format));
  }

  @Test
  public void testGetZScore() {
    SummaryStatistics stats = new SummaryStatistics();
    stats.addValue(1.0);
    stats.addValue(2.0);
    stats.addValue(3.0);
    stats.addValue(4.0);
    stats.addValue(5.0);
    double stdDev = doubleExplained(1.58113883008419, stats.getStandardDeviation());
    assertAlmostEquals(zScore(-2), getZScore(doubleExplained(3.0, stats.getMean())
                                               - 2 * stdDev, stats), 1e-8);
    assertAlmostEquals(zScore(-1), getZScore(3 - 1 * stdDev, stats), 1e-8);
    assertAlmostEquals(Z_SCORE_0,  getZScore(3,              stats), 1e-8);
    assertAlmostEquals(zScore(1),  getZScore(3 + 1 * stdDev, stats), 1e-8);
    assertAlmostEquals(zScore(2),  getZScore(3 + 2 * stdDev, stats), 1e-8);

    assertAlmostEquals(zScore(doubleExplained(-0.6324555320336758, (2 - 3.0) / stdDev)), getZScore(2.0, stats), 1e-8);
    assertAlmostEquals(zScore(doubleExplained( 0.6324555320336758, (4 - 3.0) / stdDev)), getZScore(4.0, stats), 1e-8);
  }

  @Test
  public void getZScore_throwsWhenStandardDeviationIsZero() {
    SummaryStatistics stats = new SummaryStatistics();
    stats.addValue(DUMMY_DOUBLE);
    stats.addValue(DUMMY_DOUBLE);
    stats.addValue(DUMMY_DOUBLE);
    assertEquals(0, stats.getStandardDeviation(), 1e-8);
    assertIllegalArgumentException( () -> getZScore(-1.23, stats));
    assertIllegalArgumentException( () -> getZScore(0, stats));
    assertIllegalArgumentException( () -> getZScore(1.23, stats));
  }

}
