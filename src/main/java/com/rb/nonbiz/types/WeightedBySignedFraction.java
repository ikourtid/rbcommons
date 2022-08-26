package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;

/**
 * An item with a {@link SignedFraction} weight.
 *
 * <p> This is useful for describing a term in a linear combination of items. </p>
 *
 * @see Weighted
 * @see SignedFraction
 */
public class WeightedBySignedFraction<T> extends WeightedBy<SignedFraction, T> {

  private WeightedBySignedFraction(T item, SignedFraction weight) {
    super(item, weight);
  }

  public static <T> WeightedBySignedFraction<T> weightedBySignedFraction(T item, SignedFraction weight) {
    return new WeightedBySignedFraction<T>(item, weight);
  }

  @Override
  public String toString() {
    return Strings.format("[WBSF %s %s WBSF]", getWeight(), getItem());
  }

}
