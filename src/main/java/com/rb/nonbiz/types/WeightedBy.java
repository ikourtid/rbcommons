package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;

/**
 * <p> An item with a {@link SignedFraction} weight. </p>
 *
 * <p> This is useful for describing a term in a linear combination of items. </p>
 *
 * @see Weighted
 * @see SignedFraction
 */
public class WeightedBy<T, W extends RBNumeric<W>> {

  private final T item;
  private final W weight;

  private WeightedBy(T item, W weight) {
    this.item = item;
    this.weight = weight;
  }

  public static <T, W extends RBNumeric<W>> WeightedBy<T, W> weightedBy(T item, W weight) {
    return new WeightedBy<>(item, weight);
  }

  public T getItem() {
    return item;
  }

  public W getWeight() {
    return weight;
  }

  @Override
  public String toString() {
    return Strings.format("[WBSF %s %s WBSF]", weight, item);
  }

}
