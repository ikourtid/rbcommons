package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.SignedFraction;
import com.rb.nonbiz.types.WeightedBySignedFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.WeightedBySignedFraction.weightedBySignedFraction;
import static java.util.Collections.singletonList;

/**
 * A collection of weighted items, similar to {@link FlatLinearCombination},
 * except that it allows both positive and negative weights (but not zero).
 *
 * <p> Also, the weights are SignedFractions, which are not constrained to be &le; 1, as UnitFractions are. </p>
 *
 * @see FlatLinearCombination
 * @see Partition
 * @see LinearCombination
 * @see SignedPartition
 */
public class FlatSignedLinearCombination<T> implements Iterable<WeightedBySignedFraction<T>> {

  private final List<WeightedBySignedFraction<T>> weightedItems;

  private FlatSignedLinearCombination(List<WeightedBySignedFraction<T>> weightedItems) {
    this.weightedItems = weightedItems;
  }

  public static <T> FlatSignedLinearCombination<T> flatSignedLinearCombination(
      List<WeightedBySignedFraction<T>> weightedItems) {
    RBPreconditions.checkArgument(!weightedItems.isEmpty());
    weightedItems
        .forEach(weightedItem -> RBPreconditions.checkArgument(
            !weightedItem.getWeight().isAlmostZero(1e-8),
            "Fractions in a FlatSignedLinearCombination must be non-0. If you don't want something, just don't put it in. %s",
            weightedItem));
    return new FlatSignedLinearCombination<>(weightedItems);
  }

  public static <T> FlatSignedLinearCombination<T> singletonFlatSignedLinearCombination(T singleItem) {
    return new FlatSignedLinearCombination<>(singletonList(weightedBySignedFraction(singleItem, SIGNED_FRACTION_1)));
  }

  public static <T> FlatSignedLinearCombination<T> flatSignedLinearCombination(
      T t1, SignedFraction f1,
      T t2, SignedFraction f2) {
    return flatSignedLinearCombination(ImmutableList.of(
        weightedBySignedFraction(t1, f1),
        weightedBySignedFraction(t2, f2)));
  }

  public static <T> FlatSignedLinearCombination<T> flatSignedLinearCombination(
      T t1, SignedFraction f1,
      T t2, SignedFraction f2,
      T t3, SignedFraction f3) {
    return flatSignedLinearCombination(ImmutableList.of(
        weightedBySignedFraction(t1, f1),
        weightedBySignedFraction(t2, f2),
        weightedBySignedFraction(t3, f3)));
  }

  @Override
  public Iterator<WeightedBySignedFraction<T>> iterator() {
    return weightedItems.iterator();
  }

  public Stream<WeightedBySignedFraction<T>> stream() {
    return weightedItems.stream();
  }

  @VisibleForTesting // this is here for the test matcher; keep it hidden unless we have to expose it
  List<WeightedBySignedFraction<T>> getWeightedItems() {
    return weightedItems;
  }

  @Override
  public String toString() {
    return Strings.format("[FSLC %s : %s FSLC]",
        weightedItems.size(),
        Joiner.on(' ').join(Iterables.transform(
            weightedItems,
            wi -> Strings.format("%s * %s", wi.getWeight(), wi.getItem()))));
  }

}
