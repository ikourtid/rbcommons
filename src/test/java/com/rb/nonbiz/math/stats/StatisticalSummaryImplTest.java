package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.stats.StatisticalSummaryImpl.statisticalSummaryImpl;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;

public class StatisticalSummaryImplTest extends RBTestMatcher<StatisticalSummaryImpl> {

  public StatisticalSummaryImpl testStatisticalSummaryImplWithSeed(double seed) {
    return statisticalSummaryImpl(
        123,                // n
        5.1   + seed,       // mean
        -10.1 - seed,       // min
        20.2  + seed,       // max
        2.34  + seed,       // stdDev
        5.32  + seed,       // variance
        // Yes, the following should be 123 * (5.1 + seed), but then makeNonTrivialObject() does not match
        // makeMatchingNonTrivialObject() because the epsilon is multiplied by 123.
        123 * 5.1 + seed);  // sum = n * mean.
  }

  @Test
  public void testPreconditions() {
    StatisticalSummaryImpl doesNotThrow;

    // must have at least one data point
    doesNotThrow =                        statisticalSummaryImpl(1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    assertIllegalArgumentException( () -> statisticalSummaryImpl(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));  // n = 0

    // Consider the set of data {1, 2, 3}. Then mean = 2, min = 1, max = 3, stddev = 1, var = 1, sum = 6

    // must have min <= mean
    doesNotThrow =                        statisticalSummaryImpl(3, 2.0, 1.0, 3.0, 1.0, 1.0, 6.0);
    assertIllegalArgumentException( () -> statisticalSummaryImpl(3, 2.0, 2.1, 3.0, 1.0, 1.0, 6.0));  // min > mean

    // must have mean <= max
    doesNotThrow =                        statisticalSummaryImpl(3, 2.0, 1.0, 3.0, 1.0, 1.0, 6.0);
    assertIllegalArgumentException( () -> statisticalSummaryImpl(3, 2.0, 1.1, 1.9, 1.0, 1.0, 6.0));  // max < mean

    // cannot have negative standard deviation
    doesNotThrow =                        statisticalSummaryImpl(3, 2.0, 1.0, 3.0,  1.0, 1.0, 6.0);
    assertIllegalArgumentException( () -> statisticalSummaryImpl(3, 2.0, 1.1, 1.9, -0.1, 1.0, 6.0));  // stddev < 0

    // cannot have negative variance
    doesNotThrow =                        statisticalSummaryImpl(3, 2.0, 1.0, 3.0, 1.0,  1.0, 6.0);
    assertIllegalArgumentException( () -> statisticalSummaryImpl(3, 2.0, 1.1, 1.9, 1.0, -0.1, 6.0));  // variance < 0
  }

  @Test
  public void testConstructFromSummaryStatistics() {
    SummaryStatistics summaryStatistics = new SummaryStatistics();
    summaryStatistics.addValue(0.0);
    summaryStatistics.addValue(2.0);
    summaryStatistics.addValue(4.0);

    assertThat(
        statisticalSummaryImpl(summaryStatistics),
        statisticalSummaryImplMatcher(
            statisticalSummaryImpl(
                3,       // n
                2.0,     // mean
                0.0,     // min
                4.0,     // max
                2.0,     // stddev
                4.0,     // variance
                6.0)));  // sum
  }

  @Override
  public StatisticalSummaryImpl makeTrivialObject() {
    return statisticalSummaryImpl(
        1,     // n
        0.0,   // mean
        0.0,   // min
        0.0,   // max
        0.0,   // stdDev
        0.0,   // variance
        0.0);  // sum
  }

  @Override
  public StatisticalSummaryImpl makeNontrivialObject() {
    return testStatisticalSummaryImplWithSeed(ZERO_SEED);
  }

  @Override
  public StatisticalSummaryImpl makeMatchingNontrivialObject() {
    return testStatisticalSummaryImplWithSeed(EPSILON_SEED);
  }

  @Override
  protected boolean willMatch(StatisticalSummaryImpl expected, StatisticalSummaryImpl actual) {
    return statisticalSummaryImplMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<StatisticalSummaryImpl> statisticalSummaryImplMatcher(
      StatisticalSummaryImpl expected) {
    return makeMatcher(expected,
        matchUsingEquals(            v -> v.getN()),
        matchUsingDoubleAlmostEquals(v -> v.getMin(),               DEFAULT_EPSILON_1e_8),
        matchUsingDoubleAlmostEquals(v -> v.getMax(),               DEFAULT_EPSILON_1e_8),
        matchUsingDoubleAlmostEquals(v -> v.getMean(),              DEFAULT_EPSILON_1e_8),
        matchUsingDoubleAlmostEquals(v -> v.getStandardDeviation(), DEFAULT_EPSILON_1e_8),
        matchUsingDoubleAlmostEquals(v -> v.getVariance(),          DEFAULT_EPSILON_1e_8),
        matchUsingDoubleAlmostEquals(v -> v.getSum(),               DEFAULT_EPSILON_1e_8));
  }

}
