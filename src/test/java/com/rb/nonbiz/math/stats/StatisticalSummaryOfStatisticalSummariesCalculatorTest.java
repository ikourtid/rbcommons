package com.rb.nonbiz.math.stats;

import com.google.common.collect.ImmutableList;
import com.google.inject.multibindings.MapKey;
import com.rb.nonbiz.math.stats.StatisticalSummaryOfStatisticalSummariesCalculator.StatisticalSummaryOfStatisticalSummaries;
import com.rb.nonbiz.testutils.RBTest;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.junit.Test;

import java.util.Collections;
import java.util.function.BiConsumer;

import static com.rb.nonbiz.math.stats.StatisticalSummaryAspect.STATISTICAL_SUMMARY_MAX;
import static com.rb.nonbiz.math.stats.StatisticalSummaryAspect.STATISTICAL_SUMMARY_MEAN;
import static com.rb.nonbiz.math.stats.StatisticalSummaryAspect.STATISTICAL_SUMMARY_MIN;
import static com.rb.nonbiz.math.stats.StatisticalSummaryAspect.STATISTICAL_SUMMARY_N;
import static com.rb.nonbiz.math.stats.StatisticalSummaryAspect.STATISTICAL_SUMMARY_STANDARD_DEVIATION;
import static com.rb.nonbiz.math.stats.StatisticalSummaryAspect.STATISTICAL_SUMMARY_SUM;
import static com.rb.nonbiz.math.stats.StatisticalSummaryAspect.STATISTICAL_SUMMARY_VARIANCE;
import static com.rb.nonbiz.math.stats.StatisticalSummaryAspect.getStatisticalSummaryField;
import static com.rb.nonbiz.math.stats.StatisticalSummaryTest.makeTestStatisticalSummary;
import static com.rb.nonbiz.math.stats.StatisticalSummaryTest.statisticalSummaryMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static java.util.Collections.emptyIterator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class StatisticalSummaryOfStatisticalSummariesCalculatorTest
    extends RBTest<StatisticalSummaryOfStatisticalSummariesCalculator> {

  @Test
  public void generalTest() {
    StatisticalSummary statisticalSummary1 = makeTestStatisticalSummary(10, 11, 15);
    StatisticalSummary statisticalSummary2 = makeTestStatisticalSummary(20, 21, 24, 27);
    StatisticalSummary statisticalSummary3 = makeTestStatisticalSummary(30, 32);

    StatisticalSummaryOfStatisticalSummaries result = makeTestObject().calculate(
        ImmutableList.of(statisticalSummary1, statisticalSummary2, statisticalSummary3).iterator());

    BiConsumer<StatisticalSummaryAspect, StatisticalSummary> asserter =
        (statisticalSummaryAspect, expectedStatisticalSummaryForThisAspect) ->
            assertThat(
                result.getStatisticalSummary(statisticalSummaryAspect),
                statisticalSummaryMatcher(
                    expectedStatisticalSummaryForThisAspect,
                    1e-8));
    asserter.accept(STATISTICAL_SUMMARY_MEAN, makeTestStatisticalSummary(
        doubleExplained(12, (10 + 11 + 15) / 3.0),
        doubleExplained(23, (20 + 21 + 24 + 27) / 4.0),
        doubleExplained(31, (30 + 32) / 2.0)));

    asserter.accept(STATISTICAL_SUMMARY_VARIANCE, makeTestStatisticalSummary(
        doubleExplained( 7, 1 / 2.0 * ((10 - 12) * (10 - 12) + (11 - 12) * (11 - 12) + (15 - 12) * (15 - 12))),
        doubleExplained(10, 1 / 3.0 * ((20 - 23) * (20 - 23) + (21 - 23) * (21 - 23) + (24 - 23) * (24 - 23) + (27 - 23) * (27 - 23))),
        doubleExplained( 2, 1 / 1.0 * ((30 - 31) * (30 - 31) + (32 - 31) * (32 - 31)))));

    asserter.accept(STATISTICAL_SUMMARY_STANDARD_DEVIATION, makeTestStatisticalSummary(
        doubleExplained(2.64575131, Math.sqrt(7)),
        doubleExplained(3.16227766, Math.sqrt(10)),
        doubleExplained(1.41421356, Math.sqrt(2))));

    asserter.accept(STATISTICAL_SUMMARY_MAX, makeTestStatisticalSummary(15, 27, 32));
    asserter.accept(STATISTICAL_SUMMARY_MIN, makeTestStatisticalSummary(10, 20, 30));
    asserter.accept(STATISTICAL_SUMMARY_N,   makeTestStatisticalSummary(3, 4, 2)); // the count of numbers in each
    asserter.accept(STATISTICAL_SUMMARY_SUM, makeTestStatisticalSummary(
        doubleExplained(36, 10 + 11 + 15),
        doubleExplained(92, 20 + 21 + 24 + 27),
        doubleExplained(62, 30 + 32)));

    // Let's also test a few flavors of getStatisticalSummaryField and StatisticalSummaryOfStatisticalSummaries#get,
    // since it's convenient to do so here
    StatisticalSummary statsForMeans = result.getStatisticalSummary(STATISTICAL_SUMMARY_MEAN); // i.e. 12, 23, 31
    assertEquals(12, getStatisticalSummaryField(statsForMeans, STATISTICAL_SUMMARY_MIN), 1e-8);
    assertEquals(12, result.get(STATISTICAL_SUMMARY_MIN, STATISTICAL_SUMMARY_MEAN), 1e-8); // reads as 'min of means'

    assertEquals(31, getStatisticalSummaryField(statsForMeans, STATISTICAL_SUMMARY_MAX), 1e-8);
    assertEquals(31, result.get(STATISTICAL_SUMMARY_MAX, STATISTICAL_SUMMARY_MEAN), 1e-8); // reads as 'max of means'

    assertEquals(
        doubleExplained(22, 1 / 3.0 * (12 + 23 + 31)), // mean of means
        getStatisticalSummaryField(statsForMeans, STATISTICAL_SUMMARY_MEAN),
        1e-8);
    assertEquals(22, result.get(STATISTICAL_SUMMARY_MEAN, STATISTICAL_SUMMARY_MEAN), 1e-8);

    assertEquals(3, result.getNumStatisticalSummaries());
  }

  @Test
  public void emptyIterator_throws() {
    assertIllegalArgumentException( () -> makeTestObject().calculate(emptyIterator()));
  }

  @Override
  protected StatisticalSummaryOfStatisticalSummariesCalculator makeTestObject() {
    return new StatisticalSummaryOfStatisticalSummariesCalculator();
  }

}
