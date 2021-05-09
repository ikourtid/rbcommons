package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.rb.nonbiz.collections.Either.Visitor;
import com.rb.nonbiz.text.Strings;

import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.RBSet.rbSet;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;

/**
 * The name is not super-intuitive, but it's meant to contrast with the (well-established in the codebase by now)
 * Partition class. A {@code Partition<K>} is indexable by K. A {@code LinearCombination<N, L>} is not;
 * you can just iterate over its items.
 *
 * Use this whenever you want to represent having N things with proportions that sum to 1 (hence 'normalized')
 * and all the weights are positive, but there are no indexing semantics.
 *
 * This does not have to have ordered semantics. However, I'll do that, because
 * a) it results in deterministic printing
 * b) it's easier to check for epsilon-based comparison in test. E.g. if we have
 * 0.499999999 {@code ->}  "A"
 * 0.500000001 {@code ->}  "B"
 *
 * and
 *
 * 0.499999999 {@code ->}  "A"
 * 0.500000001 {@code ->}  "B"
 *
 * we want those to be equivalent. If we don't have ordered semantics, it's hard to tell what's first and what's second.
 *
 * In the generics below, N = node and L = leaf. This class allows you to use a different type for the nodes and
 * leaves, although you don't have to.
 *
 * #see Partition
 * #see SignedPartition
 * #see FlatLinearCombination
 */
public class LinearCombination<N, L> {

  public interface NodeOrLeafVisitor<T, N, L> {

    T visitNodeWithWeightedChildren(NodeWithWeightedChildren<N, L> nodeWithWeightedChildren);
    T visitLeaf(L leaf);

  }

  private final Either<NodeWithWeightedChildren<N, L>, L> nodeWithChildrenOrLeaf;

  private LinearCombination(Either<NodeWithWeightedChildren<N, L>, L> nodeWithChildrenOrLeaf) {
    this.nodeWithChildrenOrLeaf = nodeWithChildrenOrLeaf;
  }

  public static <N, L> LinearCombination<N, L> linearCombination(NodeWithWeightedChildren<N, L> nodeWithWeightedChildren) {
    return new LinearCombination<N, L>(Either.left(nodeWithWeightedChildren));
  }

  public static <N, L> LinearCombination<N, L> trivialLinearCombinationOfOneItem(L leaf) {
    return new LinearCombination<N, L>(Either.right(leaf));
  }

  public RBSet<L> getLeaves() {
    return visit(new NodeOrLeafVisitor<RBSet<L>, N, L>() {
      @Override
      public RBSet<L> visitNodeWithWeightedChildren(NodeWithWeightedChildren<N, L> nodeWithWeightedChildren) {
        return rbSet(nodeWithWeightedChildren.getWeightedChildren()
            .stream()
            .map(weightedLC -> weightedLC.getItem())
            .flatMap(linearCombination -> linearCombination.getLeaves().stream())
            .collect(Collectors.toSet()));
      }

      @Override
      public RBSet<L> visitLeaf(L leaf) {
        return singletonRBSet(leaf);
      }
    });
  }

  // This is better than exposing nodeWithChildrenOrLeaf and having it accessed via an Either.Visitor
  public <T> T visit(NodeOrLeafVisitor<T, N, L> visitor) {
    return nodeWithChildrenOrLeaf.<T>visit(new Visitor<NodeWithWeightedChildren<N, L>, L, T>() {
      @Override
      public T visitLeft(NodeWithWeightedChildren<N, L> nodeWithWeightedChildren) {
        return visitor.visitNodeWithWeightedChildren(nodeWithWeightedChildren);
      }

      @Override
      public T visitRight(L leaf) {
        return visitor.visitLeaf(leaf);
      }
    });
  }

  // This is here to help the test matcher. Instead, you should use visit to look at its value.
  @VisibleForTesting
  Either<NodeWithWeightedChildren<N, L>, L> getNodeWithChildrenOrLeaf() {
    return nodeWithChildrenOrLeaf;
  }

  @Override
  public String toString() {
    return toString(4);
  }

  public String toString(int precision) {
    return nodeWithChildrenOrLeaf.visit(new Visitor<NodeWithWeightedChildren<N, L>, L, String>() {
      @Override
      public String visitLeft(NodeWithWeightedChildren<N, L> nodeWithWeightedChildren) {
        return Strings.format("%s : %s",
            nodeWithWeightedChildren.getWeightedChildren(),
            Joiner.on(" ; ").join(nodeWithWeightedChildren.getWeightedChildren()
            .stream()
            .map(iww -> String.format("%s %s", iww.getWeight().toPercentString(precision), iww.getItem()))
            .iterator()));
      }

      @Override
      public String visitRight(L leaf) {
        return leaf.toString();
      }
    });
  }

}
