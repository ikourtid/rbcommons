package com.rb.nonbiz.search;

import com.rb.nonbiz.text.Strings;

/**
 * This is for the case where we won't run a binary search, because we can't find a valid initial lower bound,
 * and can only find a valid initial upper bound.
 *
 * <p> For example, let's say we want to find x1 <= 1 <= x2, such that f(x1) <= f(1) <= f(x2).
 * It's possible that there's no such x1. In that case, this class will represent the <em> tightest </em> (lowest)
 * upper bound that we could use. The hope is that, in most cases, this upper bound will be as close to 1 (in this
 * example) as possible, e.g. 1.0001, and that f(x2) will be close enough to f(1), even though we didn't run a proper
 * binary search to narrow down the value (since we didn't have both an initial lower and an initial upper bound). </p>
 */
public class ShortCircuitedBinarySearchResultForUpperBoundOnly<X, Y> {

  // X and Y do not necessarily implement Comparable; this is intentional. We don't like Comparable, because it
  // fixes the logic to only use one type of comparison. Instead, we use Comparators, of which there can be many.
  // Because X and Y do not implement Comparable, we cannot use Range here. In particular, with X,
  // there isn't even a guarantee that lowerBoundX <= upperBoundX; it could be the other way round.
  // For example, if f(x) = -x^2.
  private final X upperBoundX;
  private final Y upperBoundY;

  private ShortCircuitedBinarySearchResultForUpperBoundOnly(X upperBoundX, Y upperBoundY) {
    this.upperBoundX = upperBoundX;
    this.upperBoundY = upperBoundY;
  }

  public static <X, Y> ShortCircuitedBinarySearchResultForUpperBoundOnly<X, Y> shortCircuitedBinarySearchResultForUpperBoundOnly(
      X upperBoundX, Y upperBoundY) {
    return new ShortCircuitedBinarySearchResultForUpperBoundOnly<>(upperBoundX, upperBoundY);
  }

  public X getUpperBoundX() {
    return upperBoundX;
  }

  public Y getUpperBoundY() {
    return upperBoundY;
  }

  @Override
  public String toString() {
    return Strings.format("[SCBSRFUBO x= %s ; y= %s SCBSRFUBO]",
        upperBoundX, upperBoundY);
  }

}
