package com.rb.nonbiz.math.stats;

import com.rb.biz.types.Money;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.DoubleFunction;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.math.stats.RBStatisticalSummary.rbStatisticalSummary;
import static com.rb.nonbiz.math.stats.RBStats.toStatisticalSummary;
import static com.rb.nonbiz.math.stats.StatisticalSummaryTest.statisticalSummaryMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static junit.framework.TestCase.assertEquals;

public class RBStatisticalSummaryTest extends RBTestMatcher<RBStatisticalSummary<Money>> {

  @Test
  public void testStats() {
    RBStatisticalSummary<Money> summary = simpleRBStatisticalSummary(v -> money(v), 1, 2, 3, 4, 5);

    assertEquals(      summary.getN(),    5);
    assertAlmostEquals(summary.getMin(),  money( 1.0), 1e-8);
    assertAlmostEquals(summary.getMax(),  money( 5.0), 1e-8);
    assertAlmostEquals(summary.getMean(), money( 3.0), 1e-8);
    assertAlmostEquals(summary.getSum(),  money(15.0), 1e-8);
    assertEquals(summary.getVariance(),          2.5, 1e-8);
    assertEquals(summary.getStandardDeviation(), 1.58113883, 1e-8);

    assertAlmostEquals(summary.getMinMaxRange().lowerEndpoint(), money(1), 1e-8);
    assertAlmostEquals(summary.getMinMaxRange().upperEndpoint(), money(5), 1e-8);
  }

  public static <T extends Comparable<?>> RBStatisticalSummary<T> simpleRBStatisticalSummary(
      DoubleFunction<T> instantiator, double first, double ... rest) {
    return rbStatisticalSummary(toStatisticalSummary(first, rest), instantiator);
  }

  @SafeVarargs
  public static <P extends PreciseValue<? super P>> RBStatisticalSummary<P> simpleRBStatisticalSummary(
      DoubleFunction<P> instantiator, P first, P ... rest) {
    return rbStatisticalSummary(toStatisticalSummary(first, rest), instantiator);
  }

  @SafeVarargs
  public static <V extends ImpreciseValue<? super V>> RBStatisticalSummary<V> simpleRBStatisticalSummary(
      DoubleFunction<V> instantiator, V first, V ... rest) {
    return rbStatisticalSummary(toStatisticalSummary(first, rest), instantiator);
  }

  @Override
  public RBStatisticalSummary<Money> makeTrivialObject() {
    return rbStatisticalSummary(new StatisticalSummaryTest().makeTrivialObject(), v -> money(v));
  }

  @Override
  public RBStatisticalSummary<Money> makeNontrivialObject() {
    return rbStatisticalSummary(new StatisticalSummaryTest().makeNontrivialObject(), v -> money(v));
  }

  @Override
  public RBStatisticalSummary<Money> makeMatchingNontrivialObject() {
    return rbStatisticalSummary(new StatisticalSummaryTest().makeMatchingNontrivialObject(), v -> money(v));
  }

  @Override
  protected boolean willMatch(RBStatisticalSummary<Money> expected, RBStatisticalSummary<Money> actual) {
    return rbStatisticalSummaryMatcher(expected, 1e-8).matches(actual);
  }

  public static <T extends Comparable<?>> TypeSafeMatcher<RBStatisticalSummary<T>> rbStatisticalSummaryMatcher(
      RBStatisticalSummary<T> expected, double epsilon) {
    return makeMatcher(expected,
        match(v -> v.getRawStatisticalSummary(), f -> statisticalSummaryMatcher(f, epsilon)));
  }

}
