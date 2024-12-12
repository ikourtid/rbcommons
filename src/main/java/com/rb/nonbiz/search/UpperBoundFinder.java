package com.rb.nonbiz.search;

import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.rb.nonbiz.text.RBLog.rbLog;

/**
 * Find a valid upper bound for the X value in a binary search.
 *
 * <p> Returns empty optional if no valid value can be found after a few iterations (maxIterations). </p>
 */
public class UpperBoundFinder {

  private final static RBLog log = rbLog(UpperBoundFinder.class);

  public <X extends Comparable<? super X>, Y extends Comparable<? super Y>> Optional<X> findPossiblyIncreasedUpperBound(
      Function<X, Y> evaluateInput,
      X startingPointForSearchUpper,
      Y targetY,
      UnaryOperator<X> increaseUpperBound,
      int maxIterations) {
    X upperBoundX = startingPointForSearchUpper;
    Y upperBoundY = evaluateInput.apply(startingPointForSearchUpper);

    if (upperBoundY.compareTo(targetY) >= 0) {
      log.debug("No need to increase upX %s", upperBoundX);
      return Optional.of(upperBoundX);
    }

    // possibly increase the upper bound
    // The initial upper X-bound has a Y-value below the targetY.
    // Keep increasing upperBoundY until we get a Y above (or at) targetY, i.e. it becomes a real upper bound.
    int iIteration = 0;
    while (iIteration < maxIterations) {
      upperBoundX = increaseUpperBound.apply(upperBoundX);
      Y yUpperBoundPrev = upperBoundY;
      upperBoundY = evaluateInput.apply(upperBoundX);
      log.debug("i=%s increase upX to %s ; upY %s", iIteration, upperBoundX, upperBoundY);
      RBPreconditions.checkArgument(
          yUpperBoundPrev.compareTo(upperBoundY) <= 0,
          "new upperBoundY %s must not be less than previous upperBoundY %s",
          upperBoundY, yUpperBoundPrev);
      if (upperBoundY.compareTo(targetY) >= 0) {
        // Found a valid upper bound
        return Optional.of(upperBoundX);
      }
      iIteration++;
    }
    // If we get to this point, we've run too many iterations and still can't get a valid lower bound
    RBPreconditions.checkArgument(
        iIteration < maxIterations,
        "After %s iterations, our upper bound of %s produces a Y-value %s that's still below the targetY of %s",
        iIteration, upperBoundX, upperBoundY, targetY);
    return Optional.empty();
  }

}
