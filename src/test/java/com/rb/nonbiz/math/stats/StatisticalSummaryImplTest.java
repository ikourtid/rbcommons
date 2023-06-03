package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.testmatchers.Match;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.stats.StatisticalSummaryImpl.statisticalSummaryImpl;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.ZERO_EPSILON;

public class StatisticalSummaryImplTest extends RBTestMatcher<StatisticalSummaryImpl> {

  public StatisticalSummaryImpl testStatisticalSummaryImplWithSeed(double seed) {
    return statisticalSummaryImpl(
        123,            // N
        5.1   + seed,   // mean
        -10.1 - seed,   // min
        20.2  + seed,   // max
        2.34  + seed,   // stdDev
        5.32  + seed,   // variance
        123 * 5.1 + seed);  // sum = mean * n
  }

  @Override
  public StatisticalSummaryImpl makeTrivialObject() {
    return statisticalSummaryImpl(
        1,
        1.0,
        1.0,
        1.0,
        0.0,
        0.0,
        1.0);
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
