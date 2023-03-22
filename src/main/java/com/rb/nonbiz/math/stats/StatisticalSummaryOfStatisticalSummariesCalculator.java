package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.collections.RBEnumMap;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.EnumMap;
import java.util.Iterator;

import static com.rb.nonbiz.math.stats.StatisticalSummaryAspect.getStatisticalSummaryField;
import static com.rb.nonbiz.util.RBEnumMaps.transformRBEnumMap;

/**
 * @see StatisticalSummaryOfStatisticalSummaries
 */
public class StatisticalSummaryOfStatisticalSummariesCalculator {

  /**
   * This is useful if we have a collection of StatisticalSummary (e.g. one per day in a backtest)
   * and we want to store a StatisticalSummary for each of the attributes of a StatisticalSummary.
   * That is, this allows us to store the min of the averages (over the collection), the max of the mins, etc.
   *
   * It is a static inner class so that we can prevent its instantiation from any other place,
   * which saves this data class from having to worry about having preconditions, of which we'd need many
   * if we were able to build this object arbitrarily by setting each of its constituents separately.
   */
  public static class StatisticalSummaryOfStatisticalSummaries {

    private final RBEnumMap<StatisticalSummaryAspect, StatisticalSummary> byStatisticalSummaryAspect;
    private final int numStatisticalSummaries;

    private StatisticalSummaryOfStatisticalSummaries(
        RBEnumMap<StatisticalSummaryAspect, StatisticalSummary> byStatisticalSummaryAspect,
        int numStatisticalSummaries) {
      this.byStatisticalSummaryAspect = byStatisticalSummaryAspect;
      this.numStatisticalSummaries = numStatisticalSummaries;
    }

    /**
     * Avoid using this when you can use the clearer getters below.
     */
    public RBEnumMap<StatisticalSummaryAspect, StatisticalSummary> getRawEnumMap() {
      return byStatisticalSummaryAspect;
    }

    /**
     * The number of {@link StatisticalSummary} objects (1 or more) that were used in constructing this.
     *
     * We could have called this size() but that's a bit more ambiguous.
     */
    public int getNumStatisticalSummaries() {
      return numStatisticalSummaries;
    }

    public StatisticalSummary getStatisticalSummary(StatisticalSummaryAspect statisticalSummaryAspect) {
      return byStatisticalSummaryAspect.getOrThrow(statisticalSummaryAspect);
    }

    /**
     * E.g. passing in STATISTICAL_SUMMARY_MIN, STATISTICAL_SUMMARY_MEAN gives you the min of averages.
     */
    public double get(StatisticalSummaryAspect field1, StatisticalSummaryAspect ofField2) {
      return getStatisticalSummaryField(getStatisticalSummary(ofField2), field1);
    }

  }


  public StatisticalSummaryOfStatisticalSummaries calculate(Iterator<StatisticalSummary> iterator) {
    RBPreconditions.checkArgument(
        iterator.hasNext(),
        "You cannot call StatisticalSummaryOfStatisticalSummaries#calculate on an empty iterator");
    EnumMap<StatisticalSummaryAspect, SummaryStatistics> byStatisticalSummaryAspect =
        new EnumMap<>(StatisticalSummaryAspect.class);
    for (StatisticalSummaryAspect statisticalSummaryAspect : StatisticalSummaryAspect.values()) {
      byStatisticalSummaryAspect.put(statisticalSummaryAspect, new SummaryStatistics());
    }
    int numStatisticalSummaries = 0;
    while (iterator.hasNext()) {
      StatisticalSummary inputStatisticalSummary = iterator.next();
      numStatisticalSummaries++;
      // The naming is supposed to reflect e.g. 'min of averages'.
      for (StatisticalSummaryAspect ofStatisticalSummaryAspect : StatisticalSummaryAspect.values()) {
        // This is e.g. for all the mins
        SummaryStatistics statisticalSummaryForAspect = byStatisticalSummaryAspect.get(ofStatisticalSummaryAspect);
        double inputValueForThisItem = getStatisticalSummaryField(inputStatisticalSummary, ofStatisticalSummaryAspect);
        statisticalSummaryForAspect.addValue(inputValueForThisItem);
      }
    }
    // Unfortunately we have to 'transform' the values here, even though it's just a cast from SummaryStatistics
    // to its interface, StatisticalSummary.
    return new StatisticalSummaryOfStatisticalSummaries(
        transformRBEnumMap(
            byStatisticalSummaryAspect, summaryStatistics -> (StatisticalSummary) summaryStatistics),
        numStatisticalSummaries);
  }

}
