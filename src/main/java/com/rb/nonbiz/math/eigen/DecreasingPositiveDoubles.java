package com.rb.nonbiz.math.eigen;

import com.google.common.collect.Iterables;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBOrderingPreconditions;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;

/**
 * A typesafe wrapper around a list of Doubles, guaranteeing that they are positive and in decreasing order.
 *
 * <p> One example usage is storing eigenvalues. </p>
 */
public class DecreasingPositiveDoubles {

  private final List<Double> decreasingPositiveDoubles;

  private DecreasingPositiveDoubles(List<Double> decreasingPositiveDoubles) {
    this.decreasingPositiveDoubles = decreasingPositiveDoubles;
  }

  public static DecreasingPositiveDoubles decreasingPositiveDoubles(List<Double> decreasingPositiveDoubles) {
    RBPreconditions.checkArgument(
        !decreasingPositiveDoubles.isEmpty(),
        "Must have at least one element");
    RBOrderingPreconditions.checkNotIncreasing(
        decreasingPositiveDoubles,
        "elements must be monotonically decreasing, but found %s",
        decreasingPositiveDoubles);
    // Check that the last element is positive.
    // Since the elements are not increasing (previous check), all most be positive if the last one is.
    double lastElement = Iterables.getLast(decreasingPositiveDoubles);
    RBPreconditions.checkArgument(
        lastElement > 0,
        "all elements must be > 0, but element %s is %s",
        decreasingPositiveDoubles.size() - 1,
        lastElement);
    return new DecreasingPositiveDoubles(decreasingPositiveDoubles);
  }

  public int size() {
    return decreasingPositiveDoubles.size();
  }

  public List<Double> getRawList() {
    return decreasingPositiveDoubles;
  }

  @Override
  public String toString() {
    return Strings.format("[DPD %s DPD]", decreasingPositiveDoubles);
  }

}
