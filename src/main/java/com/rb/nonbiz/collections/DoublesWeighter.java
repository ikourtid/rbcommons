package com.rb.nonbiz.collections;

import com.rb.nonbiz.util.RBPreconditions;

import java.util.Iterator;

/**
 * Creates weighted averages where the weights scalars are PreciseValue instances.
 *
 * Use RBStreams#unweightedAverage if you're looking for basic doubles-based functionality similar to this.
 *
 * @see PreciseValueWeighter
 */
public class DoublesWeighter {

  public double makeWeightedAverage(Iterable<Double> values, Iterable<Double> weights) {
    return makeWeightedAverage(values.iterator(), weights.iterator());
  }

  public double makeWeightedAverage(
      Iterator<Double> values, Iterator<Double> weights) {
    RBPreconditions.checkArgument(
        values.hasNext() && weights.hasNext(),
        "Neither values nor weights can be empty");
    double v0 = values.next();
    double w0 = weights.next();
    double sumOfTerms = v0 * w0;
    double sumOfWeights = w0;
    int size = 1;
    while (values.hasNext() && weights.hasNext()) {
      double v = values.next();
      double w = weights.next();
      sumOfTerms += v * w;
      sumOfWeights += w;
      size++;
    }
    RBPreconditions.checkArgument(
        !values.hasNext() && !weights.hasNext(),
        "We did not have the same number of values and weights");
    RBPreconditions.checkArgument(
        sumOfWeights > 1e-8,
        "Individual weights can be 0, but not all can be 0 (actually epsilon, 1e-8). sumOfWeights= %s",
        sumOfWeights);
    return size == 1
        ? v0
        : sumOfTerms / sumOfWeights;
  }

}
