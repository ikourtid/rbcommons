package com.rb.nonbiz.search;

import com.google.common.collect.Range;
import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.search.BinarySearchInitialXBoundsResult.binarySearchBoundsCanBracketTargetY;
import static com.rb.nonbiz.text.RBLog.rbLog;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

/**
 * Find a {@link Range} of upper and lower <i>X</i>-value bounds whose <i>Y</i> values bracket a target <i>Y</i> value.
 *
 * <p> The user must supply 3 functions: </p>
 * <ol>
 *   <li> <i>evaluateInput(x)</i> to evaluate any <i>X</i> value. </li>
 *   <li> <i>reduceLowerBound(x)</i> to reduce the lower bound, e.g. halving it. </li>
 *   <li> <i>increaseUpperBound(x)</i> to increase the upper bound, e.g. doubling it. </li>
 * </ol>
 *
 * <p> The user must also specify the maximum number of bound increases/decreases to use. The same maximum
 * number will be applied for both increasing the upper bound as well as decreasing the lower bound. </p>
 *
 * <p> Note: these methods assume that the function to be bound is monotonically increasing. </p>
 */
public class LowerAndUpperBoundsFinder {

  private final static RBLog log = rbLog(LowerAndUpperBoundsFinder.class);

  /**
   * Find a Range of upper and upper <i>X</i>-value bounds whose <i>Y</i> values bracket a target <i>Y</i> value.
   *
   * <p> This method should be used if there is a single starting <i>X</i> value for the search.
   * There is a similar method using an upper and a lower starting range. </p>
   */
  public <X extends Comparable<? super X>, Y extends Comparable<? super Y>>
  Range<X> findLowerAndUpperBounds(
      Function<X, Y> evaluateInput,
      X startingPointForSearch,
      Y targetY,
      UnaryOperator<X> reduceLowerBound,
      UnaryOperator<X> increaseUpperBound,
      int maxIterations) {
    return findLowerAndUpperBounds(
        evaluateInput,
        startingPointForSearch,   // call the next method with equal lower and upper bounds
        startingPointForSearch,
        targetY,
        reduceLowerBound,
        increaseUpperBound,
        maxIterations);
  }

  /**
   * Return a Range of <i>X</i>-values whose <i>Y</i>-values bracket an input target <i>Y</i>-value.
   *
   * <p> As above, but use this if you have separate initial lower and upper <i>X</i> guessed values
   * instead of a single value. </p>
   *
   * <p> Note: this method assumes that the function to be bound is monotonically increasing. </p>
   */
  public <X extends Comparable<? super X>, Y extends Comparable<? super Y>> Range<X>
  findLowerAndUpperBounds(
      Function<X, Y> evaluateInput,
      X startingPointForSearchLower,
      X startingPointForSearchUpper,
      Y targetY,
      UnaryOperator<X> reduceLowerBound,
      UnaryOperator<X> increaseUpperBound,
      int maxIterations) {
    RBPreconditions.checkArgument(
        startingPointForSearchLower.compareTo(startingPointForSearchUpper) <= 0,
        "startingPointForSearchLower %s must not be greater than startingPointForSearchUpper %s",
        startingPointForSearchLower, startingPointForSearchUpper);

    Y lowerBoundY = evaluateInput.apply(startingPointForSearchLower);
    Y upperBoundY = evaluateInput.apply(startingPointForSearchUpper);
    RBPreconditions.checkArgument(
        lowerBoundY.compareTo(upperBoundY) <= 0,
        "lowerBoundY %s must not be greater than upperBoundY %s",
        lowerBoundY, upperBoundY);

    Optional<X> lowerBoundX = calculatePossiblyReducedLowerBound(
        evaluateInput, startingPointForSearchLower, targetY, reduceLowerBound, maxIterations);

    Optional<X> upperBoundX = calculatePossiblyIncreasedUpperBound(
        evaluateInput, startingPointForSearchUpper, targetY, increaseUpperBound, maxIterations);

    RBPreconditions.checkArgument(
        lowerBoundX.isPresent(),
        "We could not find a valid lower X bound for the binary search, even after %s iterations",
        maxIterations);
    RBPreconditions.checkArgument(
        upperBoundX.isPresent(),
        "We could not find a valid upper X bound for the binary search, even after %s iterations",
        maxIterations);

    log.debug("returning [%s, %s]", lowerBoundX.get(), upperBoundX.get());
    // return binarySearchBoundsCanBracketTargetY(closedRange(lowerBoundX.get(), upperBoundX.get()));
    // FIXME IAK change to the above; for now let's keep this backwards-compatible
    return Range.closed(lowerBoundX.get(), upperBoundX.get());
  }

  private <X extends Comparable<? super X>, Y extends Comparable<? super Y>> Optional<X> calculatePossiblyReducedLowerBound(
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


  private <X extends Comparable<? super X>, Y extends Comparable<? super Y>> Optional<X> calculatePossiblyIncreasedUpperBound(
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
