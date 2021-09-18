package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;

/**
 * <p> An item with a {@link UnitFraction} weight. </p>
 *
 * <p> This is useful for describing a term in a linear combination of items. </p>
 *
 * @see Weighted
 * @see UnitFraction
 */
public class WeightedByUnitFraction<T> extends WeightedBy<UnitFraction, T> {

  private WeightedByUnitFraction(T item, UnitFraction weight) {
    super(item, weight);
  }

  public static <T> WeightedByUnitFraction<T> weightedByUnitFraction(T item, UnitFraction weight) {
    return new WeightedByUnitFraction<T>(item, weight);
  }

  @Override
  public String toString() {
    return Strings.format("[WBUF %s %s WBUF]", getWeight(), getItem());
  }

}
