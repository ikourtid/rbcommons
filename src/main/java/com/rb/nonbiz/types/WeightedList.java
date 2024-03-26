package com.rb.nonbiz.types;

import com.rb.biz.types.asset.HasList;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.List;

/**
 * A list ({@link HasList}, to be precise), together with scalar Double weights.
 *
 * <p> This is more convenient than using {@link Weighted} for those cases where we already have an object
 * that has a list in it, and we just want to store weights externally.  </p>
 *
 * <p> Otherwise, for a list of size <i>N</i>, we'd have to create <i>N</i>
 * {@link Weighted} objects, each one wrapping a pair of double and <i>T</i>. </p>
 */
public class WeightedList<T, L extends HasList<T>> {

  private final L hasList;
  private final List<Double> weights;

  private WeightedList(L hasList, List<Double> weights) {
    this.hasList = hasList;
    this.weights = weights;
  }

  public static <T, L extends HasList<T>> WeightedList<T, L> possiblyEmptyWeightedList(L hasList, List<Double> weights) {
    RBSimilarityPreconditions.checkBothSame(
        hasList.getList().size(),
        weights.size(),
        "List has %s items but %s weights",
        hasList.getList().size(), weights.size());
    return new WeightedList<T, L>(hasList, weights);
  }

  public static <T, L extends HasList<T>> WeightedList<T, L> nonEmptyWeightedList(L hasList, List<Double> weights) {
    RBPreconditions.checkArgument(
        !weights.isEmpty(),
        "Calling nonEmptyWeightedList with non-empty inputs: %s %s",
        hasList, weights);
    return possiblyEmptyWeightedList(hasList, weights);
  }

  public L getHasList() {
    return hasList;
  }

  public List<Double> getWeights() {
    return weights;
  }

  @Override
  public String toString() {
    return Strings.format("[WL %s %s WL]", hasList, weights);
  }

}
