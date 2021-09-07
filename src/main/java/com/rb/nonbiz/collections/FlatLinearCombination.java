package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.types.WeightedBy;
import com.rb.nonbiz.types.WeightedByUnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Iterator;
import java.util.List;

import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.WeightedBy.weightedBy;
import static com.rb.nonbiz.types.WeightedByUnitFraction.weightedByUnitFraction;
import static java.util.Collections.singletonList;

/**
 * A collection of items with positive weights. There used to be a restriction that they sum to 1, but not anymore.
 *
 * FlatLinearCombination is more general than #see Partition ; the items here are not guaranteed to be keyable. You can still iterate
 * over each (item, weight) pair of course.
 * Also, the weights do not need to sum to 1, unlike Partition - although, like Partition, they are UnitFractions,
 * so the weights are in (0, 1].
 *
 * FlatLinearCombination is less general than #see LinearCombination; here, we do not preserve any sort of tree structure.
 *
 * You can choose not to care about the order that the items appear here. However, having a deterministic order
 * makes testing easier, so we will assume that by default.
 *
 * @see Partition
 * @see LinearCombination
 * @see SignedPartition
 * @see FlatSignedLinearCombination
 */
public class FlatLinearCombination<W extends RBNumeric<W>, T> implements Iterable<WeightedBy<W, T>> {

  private final List<WeightedBy<W, T>> weightedItems;

  private FlatLinearCombination(List<WeightedBy<W, T>> weightedItems) {
    this.weightedItems = weightedItems;
  }

  public static <W extends RBNumeric<W>, T> FlatLinearCombination<W, T> flatLinearCombination(
      List<WeightedBy<W, T>> weightedItems) {
    RBPreconditions.checkArgument(!weightedItems.isEmpty());
    weightedItems
        .forEach(weightedItem -> RBPreconditions.checkArgument(
            Math.abs(weightedItem.getWeight().doubleValue()) > 1e-8,
            "Weights in a FlatLinearCombination must be non-0. If you don't want something, just don't put it in. %s",
            weightedItem));
    return new FlatLinearCombination<>(weightedItems);
  }

  public static <W extends RBNumeric<W>, T> FlatLinearCombination<W, T> singletonFlatLinearCombination(
      T singleItem, W unitWeight) {
    return new FlatLinearCombination<>(singletonList(weightedBy(singleItem, unitWeight)));
  }

  public static <W extends RBNumeric<W>, T> FlatLinearCombination<W, T> flatLinearCombination(
      T t1, W f1,
      T t2, W f2) {
    return flatLinearCombination(ImmutableList.of(
        weightedBy(t1, f1),
        weightedBy(t2, f2)));
  }

  public static <W extends RBNumeric<W>, T> FlatLinearCombination<W, T> flatLinearCombination(
      T t1, W f1,
      T t2, W f2,
      T t3, W f3) {
    return flatLinearCombination(ImmutableList.of(
        weightedBy(t1, f1),
        weightedBy(t2, f2),
        weightedBy(t3, f3)));
  }

  @Override
  public Iterator<WeightedBy<W, T>> iterator() {
    return weightedItems.iterator();
  }

  @VisibleForTesting // this is here for the test matcher; keep it hidden unless we have to expose it
  List<WeightedBy<W, T>> getWeightedItems() {
    return weightedItems;
  }

  @Override
  public String toString() {
    return Strings.format("[FLC %s : %s FLC]",
        weightedItems.size(),
        Joiner.on(' ').join(Iterables.transform(
            weightedItems,
            wi -> Strings.format("%s * %s", wi.getWeight(), wi.getItem()))));
  }

}
