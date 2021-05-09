package com.rb.nonbiz.search;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Comparator;
import java.util.function.BiFunction;

import static com.rb.nonbiz.collections.RBComparators.nonDecreasingPerComparator;

/**
 * As we narrow in on x to find the best f(x), the result will be an interval [x1, x2]
 * and a corresponding interval [f(x1), f(x2)] such that f(x1) &le; f(x) &le; f(x2),
 * where f(x) is the 'targetY' parameter in the BinarySearchParameters
 */
public class BinarySearchResult<X, Y> {

  // X and Y do not necessarily implement Comparable; this is intentional. We don't like Comparable, because it 
  // fixes the logic to only use one type of comparison. Instead, we use Comparators, of which there can be many.
  // Because X and Y do not implement Comparable, we cannot use Range here. In particular, with X,
  // there isn't even a guarantee that lowerBoundX <= upperBoundX; it could be the other way round.
  // For example, if f(x) = -x^2.
  private final X lowerBoundX;
  private final X upperBoundX;
  private final Y lowerBoundY;
  private final Y upperBoundY;

  private final int numIterationsUsed;
  private final Y targetY; // useful for getBestX and getBestY

  private BinarySearchResult(X lowerBoundX, X upperBoundX, Y lowerBoundY, Y upperBoundY, int numIterationsUsed, Y targetY) {
    this.lowerBoundX = lowerBoundX;
    this.upperBoundX = upperBoundX;
    this.lowerBoundY = lowerBoundY;
    this.upperBoundY = upperBoundY;
    this.numIterationsUsed = numIterationsUsed;
    this.targetY = targetY;
  }

  public X getLowerBoundX() {
    return lowerBoundX;
  }

  public X getUpperBoundX() {
    return upperBoundX;
  }

  public Y getLowerBoundY() {
    return lowerBoundY;
  }

  public Y getUpperBoundY() {
    return upperBoundY;
  }

  public int getNumIterationsUsed() {
    return numIterationsUsed;
  }

  // This is only here to help the matcher; callers should only care about targetY
  // because it allows getBestX and getBestY to return the right value.
  @VisibleForTesting
  Y getTargetY() {
    return targetY;
  }

  public <T extends Comparable<? super T>> X getBestX(BiFunction<Y, Y, T> distanceMetric) {
    return bestIsLowerNotUpper(distanceMetric) ? lowerBoundX : upperBoundX;
  }

  public <T extends Comparable<? super T>> Y getBestY(BiFunction<Y, Y, T> distanceMetric) {
    return bestIsLowerNotUpper(distanceMetric) ? lowerBoundY : upperBoundY;
  }

  private <T extends Comparable<? super T>> boolean bestIsLowerNotUpper(BiFunction<Y, Y, T> distanceMetric) {
    T distanceBetweenLowerAndTargetY = distanceMetric.apply(lowerBoundY, targetY);
    T distanceBetweenUpperAndTargetY = distanceMetric.apply(upperBoundY, targetY);
    return distanceBetweenLowerAndTargetY.compareTo(distanceBetweenUpperAndTargetY) < 0; // lower bound has smaller distance to target
  }

  @Override
  public String toString() {
    return Strings.format("[BSR: x between %s %s ; y between %s %s ; used %s iterations ; targetY %s BSR]",
        lowerBoundX, upperBoundX, lowerBoundY, upperBoundY, numIterationsUsed, targetY);
  }


  public static class BinarySearchResultBuilder<X, Y> implements RBBuilder<BinarySearchResult<X, Y>> {

    private X lowerBoundX;
    private X upperBoundX;
    private Y lowerBoundY;
    private Y upperBoundY;
    private Integer numIterationsUsed;
    private Y targetY;
    private Comparator<? super Y> comparatorForY; // for sanity checking only

    public static <X, Y> BinarySearchResultBuilder<X, Y> binarySearchResultBuilder() {
      return new BinarySearchResultBuilder<>();
    }

    public BinarySearchResultBuilder<X, Y> setLowerBoundX(X lowerBoundX) {
      this.lowerBoundX = checkNotAlreadySet(this.lowerBoundX, lowerBoundX);
      return this;
    }

    public BinarySearchResultBuilder<X, Y> setUpperBoundX(X upperBoundX) {
      this.upperBoundX = checkNotAlreadySet(this.upperBoundX, upperBoundX);
      return this;
    }

    public BinarySearchResultBuilder<X, Y> setLowerBoundY(Y lowerBoundY) {
      this.lowerBoundY = checkNotAlreadySet(this.lowerBoundY, lowerBoundY);
      return this;
    }

    public BinarySearchResultBuilder<X, Y> setUpperBoundY(Y upperBoundY) {
      this.upperBoundY = checkNotAlreadySet(this.upperBoundY, upperBoundY);
      return this;
    }

    public BinarySearchResultBuilder<X, Y> setNumIterationsUsed(int numIterationsUsed) {
      this.numIterationsUsed = checkNotAlreadySet(this.numIterationsUsed, numIterationsUsed);
      return this;
    }

    public BinarySearchResultBuilder<X, Y> setTargetY(Y targetY) {
      this.targetY = checkNotAlreadySet(this.targetY, targetY);
      return this;
    }

    public BinarySearchResultBuilder<X, Y> setComparatorForY(Comparator<? super Y> comparatorForY) {
      // Can't use checkNotAlreadySet due to some generics-related reason, so I'm inlining it here.
      RBPreconditions.checkArgument(
          this.comparatorForY == null,
          "You are trying to set a value twice in a builder, which is probably a bug");
      this.comparatorForY = comparatorForY;
      return this;
    }

    // a special case: the targetY is not within the bounds [lowerBoundY, upperBoundY],
    // so no binary search can be done. This supports building a trivial "result" consisting of the lower and upper bounds.
    // Note that the initial Precondition for bounds checking is the opposite of that in sanityCheckContents().
    public BinarySearchResult<X, Y> buildForTargetOutsideOfBounds() {
      RBPreconditions.checkArgument(
          !nonDecreasingPerComparator(comparatorForY, lowerBoundY, targetY, upperBoundY),
          "[lowerBoundY, upperBoundY] should not contain targetY but x %s -> %s and y %s -> %s ; target= %s",
          lowerBoundX, upperBoundX, lowerBoundY, upperBoundY, targetY);
      // even though the targetY is outside of [lowerBoundY, upperBoundY], we still check that
      // lowerBoundY <= upperBoundY
      RBPreconditions.checkArgument(
          comparatorForY.compare(lowerBoundY, upperBoundY) <= 0,
          "lowerBoundY must be <= upperBoundY, but lowerBoundY = %s ; upperBoundY = %s", lowerBoundY, upperBoundY);
      // no binary search has been done; the number of iterations must be zero.
      RBPreconditions.checkArgument(
          numIterationsUsed == 0,
          "[lowerBoundY, upperBoundY] do not contain targetY; no search was done. numIterations = %s but should be 0", numIterationsUsed);
      sharedSanityChecks();
      return buildWithoutPreconditions();
    }

    @Override
    public void sanityCheckContents() {
      // Check that [lowerBoundY, targetY, upperBoundY] is a non-decreasing sequence here, instead of in sharedSanityChecks(),
      // because the alternative to build(), buildForTargetOutsideOfBounds(), cannot satisfy this condition.
      RBPreconditions.checkArgument(
          nonDecreasingPerComparator(comparatorForY, lowerBoundY, targetY, upperBoundY),
          "y = f(x) must be same or increasing in the last binary search interval: FYI, x %s -> %s and y %s -> %s ; target= %s",
          lowerBoundX, upperBoundX, lowerBoundY, upperBoundY, targetY);
      sharedSanityChecks();
    }

    private void sharedSanityChecks() {
      RBPreconditions.checkNotNull(lowerBoundX);
      RBPreconditions.checkNotNull(upperBoundX);
      RBPreconditions.checkNotNull(lowerBoundY);
      RBPreconditions.checkNotNull(upperBoundY);
      RBPreconditions.checkNotNull(numIterationsUsed);
      RBPreconditions.checkNotNull(comparatorForY);
      // It is possible to have 0 iterations if the binary search finds the target value before subdividing the interval.
      RBPreconditions.checkArgument(numIterationsUsed >= 0);
    }

    @Override
    public BinarySearchResult<X, Y> buildWithoutPreconditions() {
      return new BinarySearchResult<>(lowerBoundX, upperBoundX, lowerBoundY, upperBoundY, numIterationsUsed, targetY);
    }

  }

}
