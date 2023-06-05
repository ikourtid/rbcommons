package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.math.stats.StatisticalSummaryImpl.StatisticalSummaryImplBuilder.statisticalSummaryImplBuilder;
import static com.rb.nonbiz.math.stats.StatisticalSummaryImpl.statisticalSummaryImpl;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static org.hamcrest.MatcherAssert.assertThat;

public class StatisticalSummaryImplTest extends RBTestMatcher<StatisticalSummaryImpl> {

  public StatisticalSummaryImpl testStatisticalSummaryImplWithSeed(double seed) {
    return statisticalSummaryImplBuilder()
        .setN(                  123L)
        .setMean(               5.1 + seed)
        .setMin(              -10.1 - seed)
        .setMax(               20.2 + seed)
        .setStandardDeviation( 2.34 + seed)
        .build();
  }

  @Test
  public void testPreconditions() {
    StatisticalSummaryImpl doesNotThrow;

    // must have at least one data point
    Function<Long, StatisticalSummaryImpl> makerNPoints = numPoints ->
        statisticalSummaryImplBuilder()
            .setN(numPoints)
            .setMean(2.0)
            .setMin( 1.0)
            .setMax( 3.0)
            .setStandardDeviation(1.0)
            .build();
    doesNotThrow =                        makerNPoints.apply( 1L);
    assertIllegalArgumentException( () -> makerNPoints.apply(-1L));  // n = -1
    assertIllegalArgumentException( () -> makerNPoints.apply( 0L));  // n =  0

    // must have min <= mean
    TriFunction<Double, Double, Double, StatisticalSummaryImpl> maker3 = (mean, min, max) ->
        statisticalSummaryImplBuilder()
            .setN(   3L)
            .setMean(mean)
            .setMin( min)
            .setMax( max)
            .setStandardDeviation(0.1)
            .build();
    doesNotThrow =                        maker3.apply(2.0, 1.0, 3.0);
    assertIllegalArgumentException( () -> maker3.apply(2.0, 3.0, 1.0));  // min > max
    assertIllegalArgumentException( () -> maker3.apply(2.0, 2.1, 3.0));  // min > mean
    assertIllegalArgumentException( () -> maker3.apply(2.0, 1.0, 1.9));  // max > mean

    Function<Double, StatisticalSummaryImpl> makerStdDev = stdDev ->
        statisticalSummaryImplBuilder()
            .setN(   3L)
            .setMean(2.0)
            .setMin( 1.0)
            .setMax( 3.0)
            .setStandardDeviation(stdDev)
            .build();
    // cannot have a negative standard deviation
    doesNotThrow =                        makerStdDev.apply(0.0);
    doesNotThrow =                        makerStdDev.apply(1.23);
    assertIllegalArgumentException( () -> makerStdDev.apply(-1e-9));  // stddev < 0
    assertIllegalArgumentException( () -> makerStdDev.apply(-1.0));   // stddev < 0
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
            statisticalSummaryImplBuilder()
                .setN(   3L)
                .setMean(2.0)
                .setMin( 0.0)
                .setMax( 4.0)
                .setStandardDeviation(2.0)
                .build()));
  }

  @Override
  public StatisticalSummaryImpl makeTrivialObject() {
    return statisticalSummaryImplBuilder()
        .setN(                1L)
        .setMean(             0.0)
        .setMin(              0.0)
        .setMax(              0.0)
        .setStandardDeviation(0.0)
        .build();
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
    // The derived quantities 'variance' and 'sum' won't use the same epsilons
    // as the other variables, since they are products.
    // We have to multiply their epsilons by their derivatives.

    // the derivative of the variance w.r.t. standard deviation
    double epsMultiplierVariance = 2.0 * expected.getStandardDeviation();
    // the derivative of the sum w.r.t. the mean
    double epsMultiplierSum = expected.getN();
    double defaultEpsilonValue = DEFAULT_EPSILON_1e_8.doubleValue();

    return makeMatcher(expected,
        matchUsingEquals(            v -> v.getN()),
        matchUsingDoubleAlmostEquals(v -> v.getMin(),               DEFAULT_EPSILON_1e_8),
        matchUsingDoubleAlmostEquals(v -> v.getMax(),               DEFAULT_EPSILON_1e_8),
        matchUsingDoubleAlmostEquals(v -> v.getMean(),              DEFAULT_EPSILON_1e_8),
        matchUsingDoubleAlmostEquals(v -> v.getStandardDeviation(), DEFAULT_EPSILON_1e_8),
        // The following are derived quantities, but let's check anyway.
        matchUsingDoubleAlmostEquals(v -> v.getVariance(),
            epsilon(epsMultiplierVariance * defaultEpsilonValue)),
        matchUsingDoubleAlmostEquals(v -> v.getSum(),
            epsilon(epsMultiplierSum      * defaultEpsilonValue)));
  }

}
