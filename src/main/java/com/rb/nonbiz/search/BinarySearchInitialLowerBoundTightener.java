package com.rb.nonbiz.search;

import com.rb.nonbiz.util.RBPreconditions;

import java.util.Comparator;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBComparables.monotonic;

/**
 * Tightens (bumps up) the initial lower bound to be used in a binary search.
 *
 * <p> Normally, the binary search itself takes care of tightening the initial lower and upper bounds
 * towards the middle. However, sometimes we can't come up with valid lower and upper bounds that 'bracket'
 * the 'middle' / target. In those cases, we skip the binary search and instead return the tightest possible
 * lower bound or upper bound, whichever is present. </p>
 *
 * <p> This special case is described at more length in {@link BinarySearchInitialXBoundsResult}. </p>
 *
 * @see BinarySearchParameters
 */
public class BinarySearchInitialLowerBoundTightener {

  public <X, Y> X tighten(
      X initialLowerBoundX,
      BinarySearchRawParameters<X, Y> binarySearchRawParameters,
      X targetX,
      BiPredicate<X, Y> terminationPredicate) {

    Function<X, Y> evaluatorOfX          = binarySearchRawParameters.getEvaluatorOfX();
    BinaryOperator<X> midpointGenerator  = binarySearchRawParameters.getMidpointGenerator();
    Comparator<? super X> comparatorForX = binarySearchRawParameters.getComparatorForX();
    Comparator<? super Y> comparatorForY = binarySearchRawParameters.getComparatorForY();
    Y targetY                            = binarySearchRawParameters.getTargetY();

    X lowerBoundX = initialLowerBoundX;
    Y lowerBoundY = evaluatorOfX.apply(initialLowerBoundX);

    int numIterations = 0;
    while (numIterations++ < binarySearchRawParameters.getMaxIterations()) {
      if (terminationPredicate.test(lowerBoundX, lowerBoundY)) {
        return lowerBoundX;
      }
      X midpointX = midpointGenerator.apply(lowerBoundX, targetX);
      RBPreconditions.checkArgument(
          monotonic(comparatorForX, lowerBoundX, midpointX, targetX),
          "Midpoint generator is probably bad: for X, lower => initial mid => target should be monotonic (not strictly) but were %s %s %s",
          lowerBoundX, midpointX, targetX);

      Y midpointY = evaluatorOfX.apply(midpointX);
      RBPreconditions.checkArgument(
          monotonic(comparatorForY, lowerBoundY, midpointY, targetY),
          "Midpoint generator is probably bad: for Y, lower => initial mid => target should be monotonic (not strictly) but were %s %s %s",
          lowerBoundY, midpointY, targetY);

      // Push the lower bound closer to the target
      lowerBoundX = midpointX;

      // unlikely with doubles, but can't hurt to have this here.
      // This is when a midpoint happens to find the exact y = f(x) that we are searching for.
      if (comparatorForY.compare(midpointY, targetY) == 0) {
        return lowerBoundX;
      }
    }

    // By this point, we've run maxIterations.
    // The best we can do is return the tightened lower bound.
    return lowerBoundX;
  }

}
