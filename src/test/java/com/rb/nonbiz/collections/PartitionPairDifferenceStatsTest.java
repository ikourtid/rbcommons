package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.PartitionPairDifferenceStats.PartitionPairDifferenceStatsBuilder;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.PartitionPairDifferenceStats.PartitionPairDifferenceStatsBuilder.partitionPairDifferenceStatsBuilder;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRestDoubles;
import static com.rb.nonbiz.math.stats.StatisticalSummaryTest.statisticalSummaryMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInPct;
import static org.junit.Assert.fail;

public class PartitionPairDifferenceStatsTest extends RBTestMatcher<PartitionPairDifferenceStats> {

  public static PartitionPairDifferenceStats testPartitionPairDifferenceStatsWithSeed(double seed) {
    // This simulates diffing two partitions with 6 keys; the overweight and underweight % sum up to the same amount.
    return partitionPairDifferenceStatsBuilder()
        .addDifference(unitFractionInPct(10), unitFractionInPct(15 + seed)) // this key in partition B is 5% overweight
        .addDifference(unitFractionInPct(10), unitFractionInPct(16 + seed)) // +6%
        .addDifference(unitFractionInPct(50), unitFractionInPct(50)) // same
        .addDifference(unitFractionInPct(10), unitFractionInPct(9 - seed)) // -1%
        .addDifference(unitFractionInPct(10), unitFractionInPct(7 - seed)) // -3%
        // This does not take in a seed, because we need 2 items to be overweight by 'seed' and 2 to be underweight,
        // otherwise a precondition will throw.
        .addDifference(unitFractionInPct(10), unitFractionInPct(3)) // - 7%
        .build();
  }

  public static PartitionPairDifferenceStats singletonPartitionPairDifferenceStats(double netOverweightness) {
    // This looks a bit weird because the way we call addDifference is by specifying the unit fraction in the two
    // partitions, not the actual difference.
    return netOverweightness > 0
        ? partitionPairDifferenceStatsBuilder().addDifference(UNIT_FRACTION_0, unitFraction(netOverweightness)).build()
        : partitionPairDifferenceStatsBuilder().addDifference(unitFraction(-1 * netOverweightness), UNIT_FRACTION_0).build();
  }

  public static PartitionPairDifferenceStats testPartitionPairDifferenceStats(
      double netOverweightness1, double netOverweightnes2, double ... netOverweightnessRest) {
    PartitionPairDifferenceStatsBuilder builder = partitionPairDifferenceStatsBuilder();
    concatenateFirstSecondAndRestDoubles(netOverweightness1, netOverweightnes2, netOverweightnessRest)
        .forEach(netOverweightness -> {
          // This looks a bit weird because the way we call addDifference is by specifying the unit fraction in the two
          // partitions, not the actual difference.
          if (netOverweightness > 0) {
            builder.addDifference(UNIT_FRACTION_0, unitFraction(netOverweightness));
          } else {
            builder.addDifference(unitFraction(-1 * netOverweightness), UNIT_FRACTION_0);
          }
        });
    return builder.build();
  }

  public static PartitionPairDifferenceStats partitionPairDifferenceStatsWhenNoDifferences(double numItems) {
    PartitionPairDifferenceStatsBuilder builder = partitionPairDifferenceStatsBuilder();
    for (int i = 0; i < numItems; i++) {
      builder.addDifference(UNIT_FRACTION_0, UNIT_FRACTION_0);
    }
    return builder.build();
  }

  @Test
  public void reminder() {
    fail("FIXME IAK ESGSTATS test preconditions");
  }

  @Override
  public PartitionPairDifferenceStats makeTrivialObject() {
    return partitionPairDifferenceStatsBuilder()
        .addDifference(UNIT_FRACTION_1, UNIT_FRACTION_1)
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
        match(v -> v.getStatsForSignedDifferences(),        f -> statisticalSummaryMatcher(f, 1e-8)),
        match(v -> v.getStatsForAbsoluteValueDifferences(), f -> statisticalSummaryMatcher(f, 1e-8)));
  }

}
