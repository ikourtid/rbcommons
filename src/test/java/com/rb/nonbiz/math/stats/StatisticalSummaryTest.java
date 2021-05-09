package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

// Unlike almost all cases of RBTestMatcher, in this case, StatisticalSummary is a 3rd party interface,
// not our code. FYI.
public class StatisticalSummaryTest extends RBTestMatcher<StatisticalSummary> {

  // apparently SummaryStatistics can't be empty
  public static StatisticalSummary makeTestStatisticalSummary(double first, double...rest) {
    SummaryStatistics summaryStatistics = new SummaryStatistics();
    summaryStatistics.addValue(first);
    for (double restValue : rest) {
      summaryStatistics.addValue(restValue);
    }
    return summaryStatistics;
  }
  
  @Override
  public StatisticalSummary makeTrivialObject() {
    return makeTestStatisticalSummary(0);
  }

  @Override
  public StatisticalSummary makeNontrivialObject() {
    return makeTestStatisticalSummary(1.1, 7.7, -3.3);
  }

  @Override
  public StatisticalSummary makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return makeTestStatisticalSummary(1.1 + e, 7.7 + e, -3.3 + e);
  }

  @Override
  protected boolean willMatch(StatisticalSummary expected, StatisticalSummary actual) {
    return statisticalSummaryMatcher(expected, 1e-8).matches(actual);
  }

  /**
   * There may be some pathological false positives (i.e. there is a match when there shouldn't be),
   * but this is 99.99% certainly good enough.
   */
  public static <T extends StatisticalSummary> TypeSafeMatcher<T> statisticalSummaryMatcher(T expected, double epsilon) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getMean(), epsilon),
        matchUsingDoubleAlmostEquals(v -> v.getVariance(), epsilon),
        matchUsingDoubleAlmostEquals(v -> v.getStandardDeviation(), epsilon),
        matchUsingDoubleAlmostEquals(v -> v.getMax(), epsilon),
        matchUsingDoubleAlmostEquals(v -> v.getMin(), epsilon),
        matchUsingEquals(v -> v.getN()),
        matchUsingDoubleAlmostEquals(v -> v.getSum(), epsilon));
  }

}
