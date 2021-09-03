package com.rb.nonbiz.collections;

import com.rb.nonbiz.math.stats.RBStatisticalSummary;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.RBDoubles.EpsilonComparisonVisitor;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import static com.rb.nonbiz.collections.RBVoid.rbVoid;
import static com.rb.nonbiz.math.stats.RBStats.formatStatisticalSummary;
import static com.rb.nonbiz.types.PreciseValues.epsilonComparePreciseValuesAsDoubles;

/**
 * This is useful for storing information about how two partitions are different.
 * Example: target is 10% A, 40% B, 50% C; actual partition is 12% A, 37% B, 51% C.
 * The differences are +2, -3, +1.
 * {@link #statsForOverweight} will be a {@link StatisticalSummary} for the +2 and +1 (obviously there will be many more numbers in
 * general.
 * {@link #statsForUnderweight} will be a {@link StatisticalSummary} for the -3.
 * {@link #statsForAbsoluteValueDifferences} will be a {@link StatisticalSummary} for +2, +3, and +1 (absolute values).
 *
 * One nice thing about this is that we don't have to worry about one of the 3 fields not existing:
 * if any partition item is overweight, then at least one has to be underweight, and vice versa. That is, partition X
 * cannot have every single item overweight vs. partition Y.
 * However, if every item is on target, even if we decide to categorize "exactly on target" as overweight (pick one),
 * then there will be 0 items in statsForUnderweight, which is an invalid {@link SummaryStatistics}; there has to be
 * at least one item. Therefore, we will adopt the expedient semantics that a 0 difference (exactly on target)
 * counts as both overweight and underweight.
 *
 * We won't use a {@link RBStatisticalSummary} here, because we can't find a proper generic to use. If we use
 * {@code RBStatisticalSummary<Partition<AssetClass>>} then it's not really clear; the stats are for the difference
 * between two partitions. Then, if we use {@code RBStatisticalSummary<UnitFraction>}, then there can't be a sum
 * defined, because the sum of UnitFractions can easily exceed 1 (e.g. {@code statsForAbsoluteValueDifferences.getSum()}).
 *
 * This means we'll have to know to interpret these correctly, i.e. that they are in the UnitFraction range of 0 to 1
 * for overweight, and -1 to 0 for underweight.
 */
public class PartitionPairDifferenceStats {

  private final StatisticalSummary statsForOverweight;
  private final StatisticalSummary statsForUnderweight;
  private final StatisticalSummary statsForSignedDifferences;
  private final StatisticalSummary statsForAbsoluteValueDifferences;

  private PartitionPairDifferenceStats(
      StatisticalSummary statsForOverweight,
      StatisticalSummary statsForUnderweight,
      StatisticalSummary statsForSignedDifferences,
      StatisticalSummary statsForAbsoluteValueDifferences) {
    this.statsForOverweight = statsForOverweight;
    this.statsForUnderweight = statsForUnderweight;
    this.statsForSignedDifferences = statsForSignedDifferences;
    this.statsForAbsoluteValueDifferences = statsForAbsoluteValueDifferences;
  }

  public StatisticalSummary getStatsForOverweight() {
    return statsForOverweight;
  }

  public StatisticalSummary getStatsForUnderweight() {
    return statsForUnderweight;
  }

  public StatisticalSummary getStatsForSignedDifferences() {
    return statsForSignedDifferences;
  }

  public StatisticalSummary getStatsForAbsoluteValueDifferences() {
    return statsForAbsoluteValueDifferences;
  }

  @Override
  public String toString() {
    return Strings.format("[PPDS overweight= %s ; underweight= %s ; signedDiffs= %s ; |diffs|= %s PPDS]",
        formatStatisticalSummary(statsForOverweight, 6),
        formatStatisticalSummary(statsForUnderweight, 6),
        formatStatisticalSummary(statsForSignedDifferences, 6),
        formatStatisticalSummary(statsForAbsoluteValueDifferences, 6));
  }


  public static class PartitionPairDifferenceStatsBuilder implements RBBuilder<PartitionPairDifferenceStats> {

    private final SummaryStatistics statsForOverweight;
    private final SummaryStatistics statsForUnderweight;
    private final SummaryStatistics statsForSignedDifferences;
    private final SummaryStatistics statsForAbsoluteValueDifferences;

    private PartitionPairDifferenceStatsBuilder() {
      this.statsForOverweight = new SummaryStatistics();
      this.statsForUnderweight = new SummaryStatistics();
      this.statsForSignedDifferences = new SummaryStatistics();
      this.statsForAbsoluteValueDifferences = new SummaryStatistics();
    }

    public static PartitionPairDifferenceStatsBuilder partitionPairDifferenceStatsBuilder() {
      return new PartitionPairDifferenceStatsBuilder();
    }

    public PartitionPairDifferenceStatsBuilder addDifference(UnitFraction inPartitionA, UnitFraction inPartitionB) {
      epsilonComparePreciseValuesAsDoubles(
          inPartitionA,
          inPartitionB,
          // Don't use our usual 1e-8 here because many ignored entries just below 1e-8 would cause the
          // sums to be incorrect to within 1e-8, which we will check later.
          // Use an epsilon much less than 1e-8 in case we are using a large index (e.g. Russell 2000, Wilshire 5000)
          // and many stocks have tiny tilts.
          1e-12,
          new EpsilonComparisonVisitor<RBVoid>() {
            @Override
            public RBVoid visitRightIsGreater(double overweightness) {
              statsForOverweight.addValue(overweightness);
              statsForSignedDifferences.addValue(overweightness);
              statsForAbsoluteValueDifferences.addValue(overweightness);
              return rbVoid();
            }

            @Override
            public RBVoid visitAlmostEqual() {
              // See class comments on why it is expedient to have a 0 difference count as BOTH over- and underweight.
              statsForOverweight.addValue(0);
              statsForUnderweight.addValue(0);
              statsForSignedDifferences.addValue(0);
              statsForAbsoluteValueDifferences.addValue(0);
              return rbVoid();
            }

            @Override
            public RBVoid visitLeftIsGreater(double underweightnessAsNegative) {
              statsForUnderweight.addValue(underweightnessAsNegative);
              statsForSignedDifferences.addValue(underweightnessAsNegative);
              statsForAbsoluteValueDifferences.addValue(-1 * underweightnessAsNegative);
              return rbVoid();
            }
          }
      );
      return this;
    }

    @Override
    public void sanityCheckContents() {
      // Any partition item that's exactly on target will count as both overweight and underweight, which is
      // expedient, as per class comment. In case we only specify 1 underweight item but no overweight ones,
      // this will get called by the last check (sum of overweightness = sum of underweightness).
      // So this is a comprehensive check.
      RBPreconditions.checkArgument(
          statsForAbsoluteValueDifferences.getN() >= 1,
          "You must add at least one item to this builder");

      // We don't need to check min/mean/max, because of the way we control the addition via addDifference,
      // but the sum is worth checking.
      RBPreconditions.checkArgument(
          statsForOverweight.getSum() <= 1,
          "Sum of overweightness can't be >1: += %s ; -= %s ; |abs|= %s",
          statsForOverweight, statsForUnderweight, statsForAbsoluteValueDifferences);
      RBPreconditions.checkArgument(
          statsForUnderweight.getSum() <= 1,
          "Sum of underweightness can't be >1: += %s ; -= %s ; |abs|= %s",
          statsForOverweight, statsForUnderweight, statsForAbsoluteValueDifferences);

      RBPreconditions.checkArgument(
          Math.abs(statsForSignedDifferences.getMean()) < 1e-8,
          "The average signed difference must be 0, since sum(overweightness) = sum(underweightness). += %s ; -= %s ; |abs|= %s",
          statsForOverweight, statsForUnderweight, statsForAbsoluteValueDifferences);

      RBPreconditions.checkArgument(
          statsForAbsoluteValueDifferences.getSum() <= 2,
          "Sum of abs differences must be <= 2: += %s ; -= %s ; |abs|= %s",
          statsForOverweight, statsForUnderweight, statsForAbsoluteValueDifferences);

      RBPreconditions.checkArgument(
          Math.abs(statsForOverweight.getSum() + statsForUnderweight.getSum()) < 1e-8,
          "The total % percentage of 'overweightness' and 'underweightness' must sum up to 0: += %s ; -= %s ; |abs|= %s",
          statsForOverweight, statsForUnderweight, statsForAbsoluteValueDifferences);
    }

    @Override
    public PartitionPairDifferenceStats buildWithoutPreconditions() {
      return new PartitionPairDifferenceStats(
          statsForOverweight, statsForUnderweight, statsForSignedDifferences, statsForAbsoluteValueDifferences);
    }

  }

}
