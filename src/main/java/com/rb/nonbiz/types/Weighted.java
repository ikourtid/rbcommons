package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * An item together with a scalar Double weight.
 *
 * <p> This is useful for describing a term in a linear combination of items. </p>
 *
 * @see WeightedBy
 */
public class Weighted<T> {

  private final T item;
  private final double weight;

  private Weighted(T item, double weight) {
    this.item = item;
    this.weight = weight;
  }

  public static <T> Weighted<T> weighted(T item, double weight) {
    return new Weighted<T>(item, weight);
  }

  public static <T> Weighted<T> positiveWeighted(T item, double positiveWeight) {
    RBPreconditions.checkArgument(positiveWeight > 0);
    return new Weighted<T>(item, positiveWeight);
  }

  public static <T> Weighted<T> negativeWeighted(T item, double negativeWeight) {
    RBPreconditions.checkArgument(negativeWeight < 0);
    return new Weighted<T>(item, negativeWeight);
  }

  public T getItem() {
    return item;
  }

  public double getWeight() {
    return weight;
  }

  @Override
  public String toString() {
    return Strings.format("[W %s %s W]", weight, item);
  }

}
