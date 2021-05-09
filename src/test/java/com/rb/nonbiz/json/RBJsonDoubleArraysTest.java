package com.rb.nonbiz.json;

import com.rb.biz.investing.esg.EsgAttributeScore;
import com.rb.biz.types.Money;
import com.rb.biz.types.collections.ts.DailyTimeSeries;
import org.junit.Test;

import static com.rb.biz.investing.esg.EsgAttributeScore.esgAttributeScore;
import static com.rb.biz.market.MarketTest.REAL_MARKET;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.collections.ts.DailyTimeSeriesTest.dailyTestTimeSeries;
import static com.rb.biz.types.trading.RoundingScale.roundingScale;
import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.json.RBJsonDoubleArray.rbJsonDoubleArray;
import static com.rb.nonbiz.json.RBJsonDoubleArrayTest.rbJsonDoubleArrayMatcher;
import static com.rb.nonbiz.json.RBJsonDoubleArrays.convertClosedRangeToRBJsonDoubleArray;
import static com.rb.nonbiz.json.RBJsonDoubleArrays.rbJsonDoubleArrayFromDoubleTimeSeries;
import static com.rb.nonbiz.json.RBJsonDoubleArrays.rbJsonDoubleArrayFromImpreciseValueTimeSeries;
import static com.rb.nonbiz.json.RBJsonDoubleArrays.rbJsonDoubleArrayFromPreciseValueTimeSeries;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY0;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY2;
import static org.hamcrest.MatcherAssert.assertThat;

public class RBJsonDoubleArraysTest {

  @Test
  public void testRbJsonDoubleArrayFromPreciseValueTimeSeries() {
    DailyTimeSeries<Money> dailyTimeSeries = dailyTestTimeSeries(DAY0, DAY2, money(1.11), money(2.22), money(7.77));
    assertThat(
        rbJsonDoubleArrayFromPreciseValueTimeSeries(dailyTimeSeries, REAL_MARKET),
        rbJsonDoubleArrayMatcher(
            rbJsonDoubleArray(1.11, 2.22, 7.77),
            1e-8));
    assertThat(
        rbJsonDoubleArrayFromPreciseValueTimeSeries(roundingScale(1), dailyTimeSeries, REAL_MARKET),
        rbJsonDoubleArrayMatcher(
            rbJsonDoubleArray(1.1, 2.2, 7.8), // rounds up from 7.77
            1e-8));
  }

  @Test
  public void testRbJsonDoubleArrayFromImpreciseValueTimeSeries() {
    DailyTimeSeries<EsgAttributeScore> dailyTimeSeries = dailyTestTimeSeries(DAY0, DAY2,
        esgAttributeScore(1.11),
        esgAttributeScore(2.22),
        esgAttributeScore(7.77));
    assertThat(
        rbJsonDoubleArrayFromImpreciseValueTimeSeries(dailyTimeSeries, REAL_MARKET),
        rbJsonDoubleArrayMatcher(
            rbJsonDoubleArray(1.11, 2.22, 7.77),
            1e-8));
    assertThat(
        rbJsonDoubleArrayFromImpreciseValueTimeSeries(roundingScale(1), dailyTimeSeries, REAL_MARKET),
        rbJsonDoubleArrayMatcher(
            rbJsonDoubleArray(1.1, 2.2, 7.8), // rounds up from 7.77
            1e-8));
  }

  @Test
  public void testRbJsonDoubleArrayFromDoubleTimeSeries() {
    DailyTimeSeries<Double> dailyTimeSeries = dailyTestTimeSeries(DAY0, DAY2, 1.11, 2.22, 7.77);
    assertThat(
        rbJsonDoubleArrayFromDoubleTimeSeries(dailyTimeSeries, REAL_MARKET),
        rbJsonDoubleArrayMatcher(
            rbJsonDoubleArray(1.11, 2.22, 7.77),
            1e-8));
    assertThat(
        rbJsonDoubleArrayFromDoubleTimeSeries(roundingScale(1), dailyTimeSeries, REAL_MARKET),
        rbJsonDoubleArrayMatcher(
            rbJsonDoubleArray(1.1, 2.2, 7.8), // rounds up from 7.77
            1e-8));
  }

  @Test
  public void testConvertClosedRangeToJsonDoubleArray() {
    assertThat(
        convertClosedRangeToRBJsonDoubleArray(closedRange(signedMoney(-1.1), signedMoney(3.3))),
        rbJsonDoubleArrayMatcher(
            rbJsonDoubleArray(-1.1, 3.3), 1e-8));
  }

}
