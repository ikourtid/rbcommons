package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.PartitionPairDifferenceStats.PartitionPairDifferenceStatsBuilder;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.PartitionPairDifferenceStats.PartitionPairDifferenceStatsBuilder.partitionPairDifferenceStatsBuilder;
import static com.rb.nonbiz.math.stats.StatisticalSummaryTest.statisticalSummaryMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static org.junit.Assert.fail;

public class PartitionPairDifferenceStatsTest extends RBTestMatcher<PartitionPairDifferenceStats> {

  @Test
  public void reminder() {
    fail("");
  }


  @Override
  public PartitionPairDifferenceStats makeTrivialObject() {
    return null;
  }

  @Override
  public PartitionPairDifferenceStats makeNontrivialObject() {
    return partitionPairDifferenceStatsBuilder()
        .setStatsForOverweight()
        .setStatsForUnderweight()
        .setStatsForAbsoluteValueDifferences()
        .build();
  }

  @Override
  public PartitionPairDifferenceStats makeMatchingNontrivialObject() {
    return null;
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
