package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.types.WeightedByUnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;

import static com.rb.nonbiz.collections.LinearCombination.trivialLinearCombinationOfOneItem;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.WeightedByUnitFraction.weightedByUnitFraction;

/**
 * Represents an intermediate node (i.e. NOT a leaf) in the linear combination tree, together with its weighted children.
 */
public class NodeWithWeightedChildren<N, L> {

  private final N node;
  private final List<WeightedByUnitFraction<LinearCombination<N, L>>> weightedChildren;

  private NodeWithWeightedChildren(N node, List<WeightedByUnitFraction<LinearCombination<N, L>>> weightedChildren) {
    this.node = node;
    this.weightedChildren = weightedChildren;
  }

  public static <N, L> NodeWithWeightedChildren<N, L> nodeWithWeightedChildren(
      N node, List<WeightedByUnitFraction<LinearCombination<N, L>>> weightedChildren) {
    double sum = 0;
    for (WeightedByUnitFraction<LinearCombination<N, L>> weightedChild : weightedChildren) {
      RBPreconditions.checkArgument(
          !weightedChild.getWeight().isAlmostZero(DEFAULT_EPSILON_1e_8),
          "Weight can't be 0 (or almost 0) for %s",
          weightedChild);
      double weight = weightedChild.getWeight().doubleValue();
      sum += weight;
    }
    if (Math.abs(sum - 1) > 1e-8) {
      throw new IllegalArgumentException(
          smartFormat("Weights should add to EXACTLY 1 but add to %.30f", sum));
    }
    return new NodeWithWeightedChildren<>(node, weightedChildren);
  }

  public static <N, L> NodeWithWeightedChildren<N, L> nodeWithTwoWeightedLeafChildren(
      N node,
      UnitFraction weight1, L leafChild1,
      UnitFraction weight2, L leafChild2) {
    return nodeWithWeightedChildren(node, ImmutableList.of(
        weightedByUnitFraction(trivialLinearCombinationOfOneItem(leafChild1), weight1),
        weightedByUnitFraction(trivialLinearCombinationOfOneItem(leafChild2), weight2)));
  }

  public N getNode() {
    return node;
  }

  public List<WeightedByUnitFraction<LinearCombination<N, L>>> getWeightedChildren() {
    return weightedChildren;
  }
}
