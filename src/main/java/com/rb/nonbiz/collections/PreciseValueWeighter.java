package com.rb.nonbiz.collections;

import com.google.common.collect.Iterators;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.Iterator;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static java.math.MathContext.DECIMAL128;

/**
 * Creates weighted averages where the weights scalars are PreciseValue instances.
 *
 * Use RBStreams#unweightedAverage if you're looking for basic doubles-based functionality similar to this.
 *
 * @see DoublesWeighter
 */
public class PreciseValueWeighter {

  public <T extends PreciseValue<T>> BigDecimal makeUnweightedAverage(Iterable<T> values) {
    return makeUnweightedAverage(values.iterator());
  }

  public <T extends PreciseValue<T>> BigDecimal makeUnweightedAverage(Iterator<T> values) {
    RBPreconditions.checkArgument(
        values.hasNext(),
        "Cannot make an unweighted average of 0 values");
    BigDecimal v0 = values.next().asBigDecimal();
    BigDecimal sum = v0;
    int size = 1;
    while (values.hasNext()) {
      sum = sum.add(values.next().asBigDecimal());
      size++;
    }
    return (size == 1)
        ? v0
        : sum.divide(BigDecimal.valueOf(size), DEFAULT_MATH_CONTEXT);
  }

  public <T extends PreciseValue<T>, W extends PreciseValue<W>> BigDecimal makeWeightedAverage(
      Iterable<T> values, Iterable<W> weights) {
    return makeWeightedAverage(values.iterator(), weights.iterator());
  }

  public <T extends PreciseValue<T>, W extends PreciseValue<W>> BigDecimal makeWeightedAverage(
      Iterator<T> values, Iterator<W> weights) {
    return makeWeightedAverageWithBigDecimalWeights(
        values,
        Iterators.transform(weights, w -> w.asBigDecimal()));
  }

  public <T extends PreciseValue<T>> BigDecimal makeWeightedAverageWithBigDecimalWeights(
      Iterable<T> values, Iterable<BigDecimal> weights) {
    return makeWeightedAverageWithBigDecimalWeights(values.iterator(), weights.iterator());
  }

  public <T extends PreciseValue<T>> BigDecimal makeWeightedAverageWithBigDecimalWeights(
      Iterator<T> values, Iterator<BigDecimal> weights) {
    RBPreconditions.checkArgument(
        values.hasNext() && weights.hasNext(),
        "Neither values nor weights can be empty");
    BigDecimal v0 = values.next().asBigDecimal();
    BigDecimal w0 = weights.next();
    BigDecimal sumOfTerms = v0.multiply(w0, DECIMAL128);
    BigDecimal sumOfWeights = w0;
    int size = 1;
    while (values.hasNext() && weights.hasNext()) {
      BigDecimal v = values.next().asBigDecimal();
      BigDecimal w = weights.next();
      sumOfTerms = sumOfTerms.add(v.multiply(w, DECIMAL128));
      sumOfWeights = sumOfWeights.add(w, DECIMAL128);
      size++;
    }
    RBPreconditions.checkArgument(
        !values.hasNext() && !weights.hasNext(),
        "We did not have the same number of values and weights");
    RBPreconditions.checkArgument(
        sumOfWeights.signum() == 1,
        "Individual weights can be 0, but not all can be 0. sumOfWeights= %s",
        sumOfWeights);
    return size == 1
        ? v0
        : sumOfTerms.divide(sumOfWeights, DEFAULT_MATH_CONTEXT);
  }

}
