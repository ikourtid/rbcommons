package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

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
      RBPreconditions.checkArgument(
          statsForAbsoluteValueDifferences.getMean() >= 0
              && statsForAbsoluteValueDifferences.getMin() >= 0
              && statsForAbsoluteValueDifferences.getSum() >= 0,
          "Absolute differences must have a non-negative mean, min, and sum: %s",
          statsForAbsoluteValueDifferences);
      RBPreconditions.checkArgument(
          statsForOverweight.getMean() >= 0
              && statsForOverweight.getMin() >= 0
              && statsForOverweight.getSum() >= 0,
          "Overweight differences must have a non-negative mean, min, and sum: %s",
          statsForOverweight);
      RBPreconditions.checkArgument(
          statsForUnderweight.getMean() <= 0
              && statsForUnderweight.getMax() <= 0
              && statsForUnderweight.getSum() <= 0,
          "Underweight differences must have a non-positive mean, max, and sum: %s",
          statsForUnderweight);
    }

    @Override
    public PartitionPairDifferenceStats buildWithoutPreconditions() {
      return new PartitionPairDifferenceStats(statsForOverweight, statsForUnderweight, statsForAbsoluteValueDifferences);
    }

  }

}
