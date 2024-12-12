package com.rb.nonbiz.search;

import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Comparator;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBComparables.monotonic;
import static com.rb.nonbiz.text.RBLog.rbLog;

/**
 * Generalized code for doing binary search.
 *
 * @see BinarySearchParameters
 */
public class BinarySearchInitialUpperBoundTightener {

  private static final RBLog log = rbLog(BinarySearchInitialUpperBoundTightener.class);

  public <X, Y> X tighten(
      X initialUpperBoundX,
      Comparator<? super X> comparatorForX,
      Comparator<? super Y> comparatorForY,
      X targetX,
      Y targetY,
      Function<X, Y> evaluatorOfX,
      int maxIterations,
      BiPredicate<X, Y> terminationPredicate,
      BinaryOperator<X> midpointGenerator) {

    // FIXME IAK Issue #1525 package these into a single class and include it inside BinarySearchParameters

    X upperBoundX = initialUpperBoundX;
    Y upperBoundY = evaluatorOfX.apply(initialUpperBoundX);

    int numIterations = 0;
    while (numIterations++ < maxIterations) {
      if (terminationPredicate.test(upperBoundX, upperBoundY)) {
        return upperBoundX;
      }
      X midpointX = midpointGenerator.apply(upperBoundX, targetX);
      RBPreconditions.checkArgument(
          monotonic(comparatorForX, targetX, midpointX, upperBoundX),
          "Midpoint generator is probably bad: for X, target => initial mid => upper should be monotonic (not strictly) but were %s %s %s",
          upperBoundX, midpointX, targetX);

      Y midpointY = evaluatorOfX.apply(midpointX);
      RBPreconditions.checkArgument(
          monotonic(comparatorForY, targetY, midpointY, upperBoundY),
          "Midpoint generator is probably bad: for Y, lower => initial mid => target should be monotonic (not strictly) but were %s %s %s",
          upperBoundY, midpointY, targetY);

      // Push the lower bound closer to the target
      upperBoundX = midpointX;

      // unlikely with doubles, but can't hurt to have this here.
      // This is when a midpoint happens to find the exact y = f(x) that we are searching for.
      if (comparatorForY.compare(midpointY, targetY) == 0) {
        return upperBoundX;
      }
    }

    // By this point, we've run maxIterations.
    // The best we can do is return the tightened upper bound.
    return upperBoundX;
  }

}
