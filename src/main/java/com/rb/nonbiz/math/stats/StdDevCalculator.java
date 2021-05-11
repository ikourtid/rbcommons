package com.rb.nonbiz.math.stats;

import com.google.inject.Inject;
import com.rb.nonbiz.collections.PreciseValueWeighter;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import static com.rb.nonbiz.collections.RBStreams.sumNonNegativePreciseValuesToDouble;
import static java.math.MathContext.DECIMAL128;

/**
 * Calculates population standard deviation (weighted and unweighted).
 *
 * I'm not sure how to generalize this statement or make it rigorous, but:
 * you can't always instantiate a PreciseValue with the value of the standard deviation.
 * This won't work when the 'zero value' of V is not the BigDecimal 0, e.g. with OnesBasedReturn.
 * By 'zero value' I mean something like the 'additive identity' in a (math) ring.
 */

public class StdDevCalculator {

  @Inject PreciseValueWeighter preciseValueWeighter;

  // Two-pass algorithm; better numerical stability than sum-of-squares
  public <V extends PreciseValue<V>, W extends PreciseValue<W>>
  double calculateWeightedStandardDeviationForPopulation(List<V> values, List<W> weights) {
    RBPreconditions.checkArgument(
        values.size() == weights.size(),
        "There were %s values but %s weights",
        values.size(), weights.size());
    RBPreconditions.checkArgument(
        values.size() > 1,
        "There were %s values but need at least 2",
        values.size());
    double sumOfWeights = sumNonNegativePreciseValuesToDouble(weights);
    RBPreconditions.checkArgument(
        sumOfWeights > 0,
        "It's OK for some individual weights to be 0, but sum of weights must be >0 and was %s",
        sumOfWeights);

    // first pass; get the weighted average
    BigDecimal average = preciseValueWeighter.makeWeightedAverage(values, weights);

    // second pass; sum squares of deviation from the average
    BigDecimal wDiffSqSum = BigDecimal.ZERO;
    BigDecimal wSum = BigDecimal.ZERO;
    for (int i = 0; i < values.size(); ++i) {
      BigDecimal v = values.get(i).asBigDecimal();
      BigDecimal w = weights.get(i).asBigDecimal();

      BigDecimal diff = v.subtract(average, DECIMAL128);
      BigDecimal diffSq = diff.multiply(diff, DECIMAL128);
      BigDecimal wDiffSq = diffSq.multiply(w, DECIMAL128);
      wDiffSqSum = wDiffSqSum.add(wDiffSq, DECIMAL128);
      wSum = wSum.add(w, DECIMAL128);
    }

    BigDecimal variance = wDiffSqSum.divide(wSum, DECIMAL128);
    // Check for variance < 0 due to possible rounding issues
    // BigDecimal has no sqrt; return double
    return variance.doubleValue() < 0 ? 0.0 : Math.sqrt(variance.doubleValue());
  }

  public <V extends PreciseValue<V>> double calculateStandardDeviationForPopulationWithOnePass(Iterable<V> values) {
    return calculateStandardDeviationForPopulationWithOnePass(values.iterator());
  }

  // Welford algorithm for stable one-pass results
  public <V extends PreciseValue<V>> double calculateStandardDeviationForPopulationWithOnePass(Iterator<V> values) {
    BigDecimal n = BigDecimal.ZERO;
    BigDecimal mean = BigDecimal.ZERO;
    BigDecimal S = BigDecimal.ZERO;

    while (values.hasNext()) {
      n = n.add(BigDecimal.ONE);
      BigDecimal v = values.next().asBigDecimal();
      BigDecimal delta = v.subtract(mean, DECIMAL128);
      BigDecimal deltaReduced = delta.divide(n, DECIMAL128);  // this DECIMAL128 is required for the tests to pass
      mean = mean.add(deltaReduced, DECIMAL128);
      BigDecimal delta2 = v.subtract(mean, DECIMAL128);
      S = S.add(delta.multiply(delta2, DECIMAL128));
    }

    BigDecimal variance = S.divide(n, DECIMAL128);
    // check for variance < 0 due to possible rounding issues
    return variance.doubleValue() < 0 ? 0 : Math.sqrt(variance.doubleValue());
  }

}
