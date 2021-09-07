package com.rb.nonbiz.types;

/**
 * <p> An item with a {@link SignedFraction} weight. </p>
 *
 * <p> This is useful for describing a term in a linear combination of items. </p>
 *
 * @see Weighted
 * @see SignedFraction
 */
public class WeightedBySignedFraction<T> extends WeightedBy<T, SignedFraction> {

  private WeightedBySignedFraction(T item, SignedFraction weight) {
    super(item, weight);
  }

  public static <T> WeightedBySignedFraction<T> weightedBySignedFraction(T item, SignedFraction weight) {
    return new WeightedBySignedFraction<T>(item, weight);
  }

}
