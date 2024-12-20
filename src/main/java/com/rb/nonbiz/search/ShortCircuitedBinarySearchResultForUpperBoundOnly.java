package com.rb.nonbiz.search;

import com.rb.nonbiz.text.Strings;

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
    return Strings.format();
  }

}
