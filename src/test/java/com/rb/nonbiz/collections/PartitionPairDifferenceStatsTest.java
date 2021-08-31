package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.PartitionPairDifferenceStats.PartitionPairDifferenceStatsBuilder.partitionPairDifferenceStatsBuilder;
import static com.rb.nonbiz.math.stats.StatisticalSummaryTest.makeTestStatisticalSummary;
import static com.rb.nonbiz.math.stats.StatisticalSummaryTest.statisticalSummaryMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static org.junit.Assert.fail;

public class PartitionPairDifferenceStatsTest extends RBTestMatcher<PartitionPairDifferenceStats> {

  public static PartitionPairDifferenceStats testPartitionPairDifferenceStatsWithSeed(double seed) {
    return partitionPairDifferenceStatsBuilder()
        .setStatsForOverweight(
            makeTestStatisticalSummary(0.05 + seed, 0.06 + seed, 0))
        .setStatsForUnderweight(
            makeTestStatisticalSummary(-0.01 + seed, -0.03 + seed, -0.07 + seed, 0))
        .setStatsForAbsoluteValueDifferences(
            makeTestStatisticalSummary(0.05 + seed, 0.06 + seed, 0, 0.01 + seed, 0.03 + seed, 0.07 + seed))
        .build();
  }

  @Test
  public void reminder() {
    fail("FIXME IAK ESGSTATS test preconditions");
  }

  @Override
  public PartitionPairDifferenceStats makeTrivialObject() {
    return partitionPairDifferenceStatsBuilder()
        .setStatsForOverweight(makeTestStatisticalSummary(0))
        .setStatsForUnderweight(makeTestStatisticalSummary(0))
        .setStatsForAbsoluteValueDifferences(makeTestStatisticalSummary(0))
        .build();
  }

  @Override
  public PartitionPairDifferenceStats makeNontrivialObject() {
    return testPartitionPairDifferenceStatsWithSeed(ZERO_SEED);
  }

  @Override
  public PartitionPairDifferenceStats makeMatchingNontrivialObject() {
    return testPartitionPairDifferenceStatsWithSeed(EPSILON_SEED);
  }

  @Override
  protected boolean willMatch(PartitionPairDifferenceStats expected, PartitionPairDifferenceStats actual) {
    return partitionPairDifferenceStatsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<PartitionPairDifferenceStats> partitionPairDifferenceStatsMatcher(
      PartitionPairDifferenceStats expected) {
    return makeMatcher(expected,
        match(v -> v.getStatsForOverweight(),               f -> statisticalSummaryMatcher(f, 1e-8)),
        match(v -> v.getStatsForUnderweight(),              f -> statisticalSummaryMatcher(f, 1e-8)),
        match(v -> v.getStatsForAbsoluteValueDifferences(), f -> statisticalSummaryMatcher(f, 1e-8)));
  }

}
