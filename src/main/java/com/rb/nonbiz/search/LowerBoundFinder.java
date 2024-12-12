package com.rb.nonbiz.search;

import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.rb.nonbiz.text.RBLog.rbLog;

/**
 * Find a valid lower bound for the X value in a binary search.
 *
 * <p> Returns empty optional if no valid value can be found after a few iterations (maxIterations). </p>
 */
public class LowerBoundFinder {

  private final static RBLog log = rbLog(LowerBoundFinder.class);

  public <X extends Comparable<? super X>, Y extends Comparable<? super Y>> Optional<X> findPossiblyReducedLowerBound(
      Function<X, Y> evaluateInput,
      X startingPointForSearchLower,
      Y targetY,
      UnaryOperator<X> reduceLowerBound,
      int maxIterations) {
    X lowerBoundX = startingPointForSearchLower;
    Y lowerBoundY = evaluateInput.apply(startingPointForSearchLower);

    // If the starting point X1 is already a valid lower bound, i.e. f(X1) <= Y, return that.
    if (lowerBoundY.compareTo(targetY) <= 0) {
      log.debug("No need to loosen (reduce) lowX= %s any further", lowerBoundX);
      return Optional.of(lowerBoundX);
    }

    // possibly reduce the lower bound
    // The initial lower X-bound has a Y-value above the targetY, i.e. Y < f(X1)
    // Keep reducing lowerBoundX until we get a Y below or at targetY, i.e. it becomes a real lower bound.
    int iIteration = 0;
    while (iIteration < maxIterations) {
      lowerBoundX = reduceLowerBound.apply(lowerBoundX);
      Y lowerBoundYPrev = lowerBoundY;
      lowerBoundY = evaluateInput.apply(lowerBoundX);
      log.debug("i=%s reduce lowX to %s ; lowY %s", iIteration, lowerBoundX, lowerBoundY);
      RBPreconditions.checkArgument(
          lowerBoundYPrev.compareTo(lowerBoundY) >= 0,
          "new lowerBoundY %s must not be greater than previous lowerBoundY %s",
          lowerBoundY, lowerBoundYPrev);
      if (lowerBoundY.compareTo(targetY) <= 0) {
        // Found a valid lower bound
        return Optional.of(lowerBoundX);
      }
      iIteration++;
    }

    // If we get to this point, we've run too many iterations and still can't get a valid lower bound
    log.info("After %s iterations, our lower bound of %s produces a Y-value %s that's still above the targetY of %s",
        iIteration, lowerBoundX, lowerBoundY, targetY);
    return Optional.empty();
  }

}
