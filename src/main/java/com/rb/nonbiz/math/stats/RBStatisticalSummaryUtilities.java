package com.rb.nonbiz.math.stats;

import com.google.common.collect.Iterators;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;

import java.util.Iterator;
import java.util.function.DoubleFunction;

import static com.rb.nonbiz.math.stats.RBStatisticalSummary.rbStatisticalSummary;
import static com.rb.nonbiz.math.stats.RBStats.toStatisticalSummary;

/**
 * Various static utility methods pertaining to {@link RBStatisticalSummary}.
 *
 * @see RBStats
 */
public class RBStatisticalSummaryUtilities {

  public static <V extends ImpreciseValue<V>> RBStatisticalSummary<V> toRBStatisticalSummary(
      Iterator<V> iterator, DoubleFunction<V> instantiator) {
    return rbStatisticalSummary(
        toStatisticalSummary(Iterators.transform(iterator, v -> v.doubleValue())),
        instantiator);
  }

  public static <V extends PreciseValue<V>> RBStatisticalSummary<V> preciseValuesToRBStatisticalSummary(
      Iterator<V> iterator, DoubleFunction<V> instantiator) {
    return rbStatisticalSummary(
        toStatisticalSummary(Iterators.transform(iterator, v -> v.doubleValue())),
        instantiator);
  }

  public static <V extends ImpreciseValue<V>> RBStatisticalSummary<V> impreciseValuesToRBStatisticalSummary(
      Iterator<V> iterator, DoubleFunction<V> instantiator) {
    return rbStatisticalSummary(
        toStatisticalSummary(Iterators.transform(iterator, v -> v.doubleValue())),
        instantiator);
  }

  public static <V extends ImpreciseValue<V>> RBStatisticalSummary<V> toRBStatisticalSummary(
      DoubleFunction<V> instantiator, V first, V ... rest) {
    return rbStatisticalSummary(
        toStatisticalSummary(first, rest),
        instantiator);
  }

}
