package com.rb.nonbiz.search;

import com.rb.nonbiz.text.Strings;

/**
 * This is for the case where we won't run a binary search, because we can't find a valid initial upper bound,
 * and can only find a valid initial lower bound.
 *
 * <p> For example, let's say we want to find x1 <= 1 <= x2, such that f(x1) <= f(1) <= f(x2).
 * It's possible that there's no such x2. In that case, this class will represent the <em> tightest </em> (largest)
 * lower bound that we could use. The hope is that, in most cases, this lower bound will be as close to 1 (in this
 * example) as possible, e.g. 0.9999, and that f(x1) will be close enough to f(1), even though we didn't run a proper
 * binary search to narrow down the value (since we didn't have both an initial upper and an initial lower bound). </p>
 */
public class ShortCircuitedBinarySearchResultForLowerBoundOnly<X, Y> {

  // X and Y do not necessarily implement Comparable; this is intentional. We don't like Comparable, because it
  // fixes the logic to only use one type of comparison. Instead, we use Comparators, of which there can be many.
  // Because X and Y do not implement Comparable, we cannot use Range here. In particular, with X,
  // there isn't even a guarantee that upperBoundX <= lowerBoundX; it could be the other way round.
  // For example, if f(x) = -x^2.
  private final X lowerBoundX;
  private final Y lowerBoundY;

  private ShortCircuitedBinarySearchResultForLowerBoundOnly(X lowerBoundX, Y lowerBoundY) {
    this.lowerBoundX = lowerBoundX;
    this.lowerBoundY = lowerBoundY;
  }

  public static <X, Y> ShortCircuitedBinarySearchResultForLowerBoundOnly<X, Y> shortCircuitedBinarySearchResultForLowerBoundOnly(
      X lowerBoundX, Y lowerBoundY) {
    return new ShortCircuitedBinarySearchResultForLowerBoundOnly<>(lowerBoundX, lowerBoundY);
  }

  public X getLowerBoundX() {
    return lowerBoundX;
  }

  public Y getLowerBoundY() {
    return lowerBoundY;
  }

  @Override
  public String toString() {
    return Strings.format("[SCBSRFLBO x= %s ; y= %s SCBSRFLBO]",
        lowerBoundX, lowerBoundY);
  }

}
