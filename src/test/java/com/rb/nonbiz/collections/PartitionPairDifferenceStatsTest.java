package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.PartitionPairDifferenceStats.PartitionPairDifferenceStatsBuilder;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.collections.PartitionPairDifferenceStats.PartitionPairDifferenceStatsBuilder.partitionPairDifferenceStatsBuilder;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRestDoubles;
import static com.rb.nonbiz.math.stats.StatisticalSummaryTest.statisticalSummaryMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInPct;

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
  public void noItems_throws() {
    assertIllegalArgumentException( () -> partitionPairDifferenceStatsBuilder()
        .build());
    PartitionPairDifferenceStats doesNotThrow;
    doesNotThrow = partitionPairDifferenceStatsBuilder().addDifference(UNIT_FRACTION_0,     UNIT_FRACTION_0).build();
    doesNotThrow = partitionPairDifferenceStatsBuilder().addDifference(unitFraction(0.123), unitFraction(0.123)).build();
    doesNotThrow = partitionPairDifferenceStatsBuilder().addDifference(UNIT_FRACTION_1,     UNIT_FRACTION_1).build();
  }

  @Test
  public void tooMuchTotalOverweightnessAndUnderweightness_throws() {
    // There's no way to check each one individually, because if they're not the same, there's another precondition
    // that will trigger here.
    assertIllegalArgumentException( () -> partitionPairDifferenceStatsBuilder()
        .addDifference(unitFraction(0.1), unitFraction(0.9)) // we have one item with a 80% higher weight in the 2nd partition
        .addDifference(unitFraction(0.2), unitFraction(0.7)) // .. and 50%

        .addDifference(unitFraction(0.9), unitFraction(0.1)) // now doing the opposite, so that we'll hit the right precondition
        .addDifference(unitFraction(0.7), unitFraction(0.2))
        .build());

    PartitionPairDifferenceStats doesNotThrow = partitionPairDifferenceStatsBuilder()
        .addDifference(unitFraction(0.1), unitFraction(0.9))  // 80% overweight
        .addDifference(unitFraction(0.2), unitFraction(0.19)) // 1% overweight

        .addDifference(unitFraction(0.9), unitFraction(0.1))
        .addDifference(unitFraction(0.19), unitFraction(0.2))
        .build();
  }

  @Test
  public void totalOverweightnessMustBeSameAsUnderweightness() {
    // Note that the only thing that matters is the difference between the two args in addDifference;
    // the code does not assert that all the first args ever passed in will sum to 100% (same with 2nd args).
    Function<UnitFraction, PartitionPairDifferenceStats> maker = fraction -> partitionPairDifferenceStatsBuilder()
        .addDifference(unitFraction(0.1), fraction)          // 60% higher weight in 2nd partition when using unitFraction(0.7)
        .addDifference(unitFraction(0.2), unitFraction(0.3)) // 10% higher weight
        .addDifference(UNIT_FRACTION_1,   unitFraction(0.3))   // 70% underweight = 60 + 10.
        .build();

    PartitionPairDifferenceStats doesNotThrow;
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.69)));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.7 - 1e-7)));
    doesNotThrow = maker.apply(unitFraction(0.7 - 1e-9));
    doesNotThrow = maker.apply(unitFraction(0.7));
    doesNotThrow = maker.apply(unitFraction(0.7 + 1e-9));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.7 + 1e-7)));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.71)));
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
