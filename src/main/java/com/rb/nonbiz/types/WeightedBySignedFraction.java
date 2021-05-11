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
public class WeightedBySignedFraction<T> {

  private final T item;
  private final SignedFraction weight;

  private WeightedBySignedFraction(T item, SignedFraction weight) {
    this.item = item;
    this.weight = weight;
  }

  public static <T> WeightedBySignedFraction<T> weightedBySignedFraction(T item, SignedFraction weight) {
    return new WeightedBySignedFraction<T>(item, weight);
  }

  public T getItem() {
    return item;
  }

  public SignedFraction getWeight() {
    return weight;
  }

  @Override
  public String toString() {
    return Strings.format("[WBSF %s %s WBSF]", weight, item);
  }

}
