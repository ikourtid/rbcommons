package com.rb.nonbiz.math.stats;

import com.google.common.collect.Range;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import java.text.NumberFormat;
import java.util.function.DoubleFunction;

import static com.rb.nonbiz.math.stats.RBStats.formatStatisticalSummary;

/**
 * This is a typesafe wrapper around an Apache {@code StatisticalSummary}, which we created for the same reasons why we like
 * type-safety elsewhere: it allows us to talk about stats of e.g. Money. For example, getMin() should really return Money,
 * not double.
 *
 * <p> We can't easily enforce it statically, other than by requiring 'instantiator' to be passed in,
 * but T has to be a number-like class. Unfortunately, we didn't think about this hard enough early on, so
 * ImpreciseValue and PreciseValue don't extend Number, which would have been a good base class.
 * That would have allowed us to use {@code <T extends Number>} below. </p>
 *
 * <p> At a minimum, we can require that T extends Comparable; it's not quite the same, but it's still useful to do
 * that. </p>
 *
 * <p> Note that the standard deviation will be returned as a double, NOT as a T. It is possible that T's constructor
 * enforces that e.g. T &gt; 0, but standard deviation could be 0, which would throw an exception.
 * Variance has the same reason as above for not being a T. </p>
 */
public class RBStatisticalSummary<T extends Comparable<? super T>> {

  protected final StatisticalSummary rawStatisticalSummary;
  private final DoubleFunction<T> instantiator;

  protected RBStatisticalSummary(StatisticalSummary rawStatisticalSummary, DoubleFunction<T> instantiator) {
    this.rawStatisticalSummary = rawStatisticalSummary;
    this.instantiator = instantiator;
  }

  public static <T extends Comparable<? super T>> RBStatisticalSummary<T> rbStatisticalSummary(
      StatisticalSummary rawStatisticalSummary, DoubleFunction<T> instantiator) {
    return new RBStatisticalSummary<>(rawStatisticalSummary, instantiator);
  }

  public StatisticalSummary getRawStatisticalSummary() {
    return rawStatisticalSummary;
  }

  public T getMean() {
    return instantiator.apply(rawStatisticalSummary.getMean());
  }

  public double getVariance() {
    return rawStatisticalSummary.getVariance();
  }

  public double getStandardDeviation() {
    return rawStatisticalSummary.getStandardDeviation();
  }

  public T getMax() {
    return instantiator.apply(rawStatisticalSummary.getMax());
  }

  public T getMin() {
    return instantiator.apply(rawStatisticalSummary.getMin());
  }

  public Range<T> getMinMaxRange() {
    return Range.closed(getMin(), getMax());
  }

  public long getN() {
    return rawStatisticalSummary.getN();
  }

  public T getSum() {
    return instantiator.apply(rawStatisticalSummary.getSum());
  }

  @Override
  public String toString() {
    return formatStatisticalSummary(rawStatisticalSummary);
  }

  public String toString(NumberFormat numberFormat) {
    return formatStatisticalSummary(rawStatisticalSummary, numberFormat);
  }

}
