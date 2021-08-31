package com.rb.nonbiz.collections;

import com.google.common.collect.Range;
import com.rb.nonbiz.math.stats.RBStatisticalSummary;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.function.BiConsumer;

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
 * However, every item is on target, even if we decide to categorize "exactly on target" as overweight (pick one),
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
  private final StatisticalSummary statsForAbsoluteValueDifferences;

  private PartitionPairDifferenceStats(
      StatisticalSummary statsForOverweight,
      StatisticalSummary statsForUnderweight,
      StatisticalSummary statsForAbsoluteValueDifferences) {
    this.statsForOverweight = statsForOverweight;
    this.statsForUnderweight = statsForUnderweight;
    this.statsForAbsoluteValueDifferences = statsForAbsoluteValueDifferences;
  }

  public StatisticalSummary getStatsForOverweight() {
    return statsForOverweight;
  }

  public StatisticalSummary getStatsForUnderweight() {
    return statsForUnderweight;
  }

  public StatisticalSummary getStatsForAbsoluteValueDifferences() {
    return statsForAbsoluteValueDifferences;
  }

  @Override
  public String toString() {
    return Strings.format("[PPDS %s %s %s PPDS]",
        statsForOverweight, statsForUnderweight, statsForAbsoluteValueDifferences);
  }


  public static class PartitionPairDifferenceStatsBuilder implements RBBuilder<PartitionPairDifferenceStats> {

    private StatisticalSummary statsForOverweight;
    private StatisticalSummary statsForUnderweight;
    private StatisticalSummary statsForAbsoluteValueDifferences;

    private PartitionPairDifferenceStatsBuilder() {}

    public static PartitionPairDifferenceStatsBuilder partitionPairDifferenceStatsBuilder() {
      return new PartitionPairDifferenceStatsBuilder();
    }

    public PartitionPairDifferenceStatsBuilder setStatsForOverweight(StatisticalSummary statsForOverweight) {
      this.statsForOverweight = checkNotAlreadySet(this.statsForOverweight, statsForOverweight);
      return this;
    }

    public PartitionPairDifferenceStatsBuilder setStatsForUnderweight(StatisticalSummary statsForUnderweight) {
      this.statsForUnderweight = checkNotAlreadySet(this.statsForUnderweight, statsForUnderweight);
      return this;
    }

    public PartitionPairDifferenceStatsBuilder setStatsForAbsoluteValueDifferences(
        StatisticalSummary statsForAbsoluteValueDifferences) {
      this.statsForAbsoluteValueDifferences = checkNotAlreadySet(this.statsForAbsoluteValueDifferences,
          statsForAbsoluteValueDifferences);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(statsForOverweight);
      RBPreconditions.checkNotNull(statsForUnderweight);
      RBPreconditions.checkNotNull(statsForAbsoluteValueDifferences);

      // Any partition item that's exactly on target will count as both overweight and underweight, which is
      // expedient, as per class comment.
      RBPreconditions.checkArgument(
          statsForOverweight.getN() + statsForUnderweight.getN() >= statsForAbsoluteValueDifferences.getN(),
          "Too many absolute value differences: %s %s %s",
          statsForOverweight, statsForUnderweight, statsForAbsoluteValueDifferences);

      BiConsumer<StatisticalSummary, Range<Double>> allInRange = (statisticalSummary, allowableRange) ->
          RBPreconditions.checkArgument(
              allowableRange.contains(statisticalSummary.getMin())
                  && allowableRange.contains(statisticalSummary.getMax())
                  && allowableRange.contains(statisticalSummary.getMean())
                  && allowableRange.contains(statisticalSummary.getSum()),
              "Partition pair statistics must be [0, 1] for overweight, [-1, 0] for underweight, and [0, 2] for sum of abs: %s %s %s",
              statsForOverweight, statsForUnderweight, statsForAbsoluteValueDifferences);

      allInRange.accept(statsForOverweight,  Range.closed( 0.0, 1.0));
      allInRange.accept(statsForUnderweight, Range.closed(-1.0, 0.0));

      Range<Double> rangeForAbsDiffs = Range.closed(0.0, 1.0);
      // 2 sum of abs diffs is valid in some extreme cases, like partition X is 100% A and partition Y is 100% B.
      RBPreconditions.checkArgument(
          rangeForAbsDiffs.contains(statsForAbsoluteValueDifferences.getMin())
              && rangeForAbsDiffs.contains(statsForAbsoluteValueDifferences.getMax())
              && rangeForAbsDiffs.contains(statsForAbsoluteValueDifferences.getMean())
              && Range.closed(0.0, 2.0).contains(statsForAbsoluteValueDifferences.getSum()),
          "Partition pair statistics must be [0, 1] for overweight, [-1, 0] for underweight, and [0, 2] for sum of abs: %s %s %s",
          statsForOverweight, statsForUnderweight, statsForAbsoluteValueDifferences);

      RBPreconditions.checkArgument(
          Math.abs(statsForOverweight.getSum() + statsForUnderweight.getSum()) < 1e-8,
          "The total % percentage of 'overweightness' and 'underweightness' must sum up to 0: %s %s %s",
          statsForOverweight, statsForUnderweight, statsForAbsoluteValueDifferences);
    }

    @Override
    public PartitionPairDifferenceStats buildWithoutPreconditions() {
      return new PartitionPairDifferenceStats(statsForOverweight, statsForUnderweight, statsForAbsoluteValueDifferences);
    }

  }

}
