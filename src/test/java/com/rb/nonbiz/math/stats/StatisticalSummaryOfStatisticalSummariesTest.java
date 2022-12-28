package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.math.stats.StatisticalSummaryOfStatisticalSummariesCalculator.StatisticalSummaryOfStatisticalSummaries;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiConsumer;

import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.math.stats.StatisticalSummaryTest.makeTestStatisticalSummary;
import static com.rb.nonbiz.math.stats.StatisticalSummaryTest.statisticalSummaryMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.enumMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class StatisticalSummaryOfStatisticalSummariesTest extends RBTestMatcher<StatisticalSummaryOfStatisticalSummaries> {

  public static StatisticalSummaryOfStatisticalSummaries testStatisticalSummaryOfStatisticalSummariesWithSeed(double seed) {
    // We do not use a seed on the last item, because shifting every number by the same seed would produce
    // the same standard deviation and variance across all objects constructed by this method, which goes contrary
    // to what we intend when we use a double seed.
    return testStatisticalSummaryOfStatisticalSummaries(
        makeTestStatisticalSummary(10 + seed, 11 + seed, 15),
        makeTestStatisticalSummary(20 + seed, 21 + seed, 24 + seed, 27),
        makeTestStatisticalSummary(30 + seed, 32));
  }

  public static StatisticalSummaryOfStatisticalSummaries testStatisticalSummaryOfStatisticalSummaries(
      StatisticalSummary first,
      StatisticalSummary second,
      StatisticalSummary ... rest) {
    // Doing this via a verb class is a bit unorthodox, but we prevent the construction of
    // StatisticalSummaryOfStatisticalSummaries from any place other than the
    // StatisticalSummaryOfStatisticalSummariesCalculator itself, to save ourselves from having to write a ton of
    // preconditions. Anyway, this is test-only, so it's OK.
    return makeRealObject(StatisticalSummaryOfStatisticalSummariesCalculator.class)
        .calculate(concatenateFirstSecondAndRest(first, second, rest).iterator());
  }

  public static StatisticalSummaryOfStatisticalSummaries singletonStatisticalSummaryOfStatisticalSummaries(
      StatisticalSummary onlyItem) {
    // Doing this via a verb class is a bit unorthodox, but we prevent the construction of
    // StatisticalSummaryOfStatisticalSummaries from any place other than the
    // StatisticalSummaryOfStatisticalSummariesCalculator itself, to save ourselves from having to write a ton of
    // preconditions. Anyway, this is test-only, so it's OK.
    return makeRealObject(StatisticalSummaryOfStatisticalSummariesCalculator.class)
        .calculate(singletonList(onlyItem).iterator());
  }

  @Test
  public void testNumStatisticalSummaries() {
    BiConsumer<Integer, StatisticalSummaryOfStatisticalSummaries> asserter =
        (expectedSize, statisticalSummaryOfStatisticalSummaries) ->
            assertEquals(
                expectedSize.intValue(),
                statisticalSummaryOfStatisticalSummaries.getNumStatisticalSummaries());

    StatisticalSummary singleStatisticalSummary = makeTestStatisticalSummary(DUMMY_DOUBLE);
    asserter.accept(1, singletonStatisticalSummaryOfStatisticalSummaries(singleStatisticalSummary));
    asserter.accept(2, testStatisticalSummaryOfStatisticalSummaries(singleStatisticalSummary, singleStatisticalSummary));
    asserter.accept(3, testStatisticalSummaryOfStatisticalSummaries(
        singleStatisticalSummary, singleStatisticalSummary, singleStatisticalSummary));
  }

  @Override
  public StatisticalSummaryOfStatisticalSummaries makeTrivialObject() {
    return singletonStatisticalSummaryOfStatisticalSummaries(makeTestStatisticalSummary(0.0));
  }

  @Override
  public StatisticalSummaryOfStatisticalSummaries makeNontrivialObject() {
    return testStatisticalSummaryOfStatisticalSummariesWithSeed(ZERO_SEED);
  }

  @Override
  public StatisticalSummaryOfStatisticalSummaries makeMatchingNontrivialObject() {
    // The 0.1 is unusual, but if we just use EPSILON_SEED, even though some aspects of the statistical summary
    // (such as min and max) are off by EPSILON_SEED, some others (such as the standard deviation and sum) could be off
    // by more than 1e-8. So we will use a smaller epsilon here. This way, statisticalSummaryOfStatisticalSummariesMatcher,
    // which gets used outside this class way more than this method, can still use 1e-8 which is the standard epsilon.
    return testStatisticalSummaryOfStatisticalSummariesWithSeed(0.1 * EPSILON_SEED);
  }

  @Override
  protected boolean willMatch(StatisticalSummaryOfStatisticalSummaries expected,
                              StatisticalSummaryOfStatisticalSummaries actual) {
    return statisticalSummaryOfStatisticalSummariesMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<StatisticalSummaryOfStatisticalSummaries> statisticalSummaryOfStatisticalSummariesMatcher(
      StatisticalSummaryOfStatisticalSummaries expected) {
    return makeMatcher(expected,
        match(v -> v.getRawEnumMap(), f -> enumMapMatcher(f, f2 -> statisticalSummaryMatcher(f2, DEFAULT_EPSILON_1e_8))));
  }

}
