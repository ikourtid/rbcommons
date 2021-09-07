package com.rb.nonbiz.types;

/**
 * <p> An item with a {@link SubObjectiveTreeWeight} weight. </p>
 *
 * <p> This is useful for describing a term in a linear combination of items. </p>
 *
 * @see Weighted
 */
public class WeightedBySubObjectiveTreeWeight<T> extends WeightedBy<SubObjectiveTreeWeight, T> {

  private WeightedBySubObjectiveTreeWeight(T item, SubObjectiveTreeWeight weight) {
    super(item, weight);
  }

  public static <T> WeightedBySubObjectiveTreeWeight<T> weightedBySubObjectiveTreeWeight(
      T item, SubObjectiveTreeWeight weight) {
    return new WeightedBySubObjectiveTreeWeight<T>(item, weight);
  }

}
