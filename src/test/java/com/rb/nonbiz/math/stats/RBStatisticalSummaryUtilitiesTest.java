package com.rb.nonbiz.math.stats;

import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

import static com.rb.biz.investing.strategy.optbased.di.BlendedRealizedVolatility.blendedRealizedVolatility;
import static com.rb.nonbiz.math.stats.RBStatisticalSummaryUtilities.toRBStatisticalSummary;
import static com.rb.nonbiz.math.stats.StatisticalSummaryTest.statisticalSummaryMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

public class RBStatisticalSummaryUtilitiesTest {

  @Test
  public void testToRBStatisticalSummary() {
    SummaryStatistics summaryStatistics = new SummaryStatistics();
    summaryStatistics.addValue(0.2);
    summaryStatistics.addValue(0.3);
    summaryStatistics.addValue(0.4);
    assertThat(
        toRBStatisticalSummary(v -> blendedRealizedVolatility(v),
            blendedRealizedVolatility(0.2),
            blendedRealizedVolatility(0.3),
            blendedRealizedVolatility(0.4))
        .getRawStatisticalSummary(),
        statisticalSummaryMatcher(summaryStatistics, 1e-8));
    assertThat(
        toRBStatisticalSummary(
            ImmutableList.of(
                blendedRealizedVolatility(0.2),
                blendedRealizedVolatility(0.3),
                blendedRealizedVolatility(0.4)).iterator(),
            v -> blendedRealizedVolatility(v))
            .getRawStatisticalSummary(),
        statisticalSummaryMatcher(summaryStatistics, 1e-8));
  }

}
