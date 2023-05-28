package com.rb.nonbiz.search;

import com.google.common.collect.Range;
import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.rb.nonbiz.text.RBLog.rbLog;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

/**
 * Find a Range of upper and lower <i>X</i>-value bounds whose <i>Y</i> values bracket a target <i>Y</i> value.
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
  public <X extends Comparable<? super X>, Y extends Comparable<? super Y>> Range<X> findLowerAndUpperBounds(
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
  public <X extends Comparable<? super X>, Y extends Comparable<? super Y>> Range<X> findLowerAndUpperBounds(
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

    int comparisonLower = lowerBoundY.compareTo(targetY);
    int comparisonUpper = upperBoundY.compareTo(targetY);
    log.debug(smartFormat("lowX %s upX %s ; lowY %s upY %s ; tgtY %s",
        startingPointForSearchLower, startingPointForSearchUpper, lowerBoundY, upperBoundY, targetY));

    // check for early exit; no bound changes needed?
    if (comparisonLower < 0 && comparisonUpper > 0) {
      log.debug("returning no changes: [%s, %s]", startingPointForSearchLower, startingPointForSearchUpper);
      return Range.closed(startingPointForSearchLower, startingPointForSearchUpper) ;
    }

    X lowerBoundX = startingPointForSearchLower;
    X upperBoundX = startingPointForSearchUpper;

    // possibly reduce the lower bound
    if (comparisonLower >= 0) {
      // The initial lower X-bound does not have a Y-value below the targetY.
      // Keep reducing lowerBoundX until we get a Y below targetY, i.e. it becomes a real lower bound.
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
        if (lowerBoundY.compareTo(targetY) < 0) {
          break;
        }
        iIteration++;
      }
      RBPreconditions.checkArgument(
          iIteration < maxIterations,
          "After %s iterations, our lower bound of %s produces a Y-value %s that's still above the targetY of %s",
          maxIterations, lowerBoundX, lowerBoundY, targetY);
    } else {
      log.debug("No need to reduce lowX %s", lowerBoundX);
    }

    // possibly increase the upper bound
    if (comparisonUpper <= 0) {
      // The initial upper X-bound does not have a Y-value above the targetY.
      // Keep increasing upperBoundY until we get a Y above targetY, i.e. it becomes a real upper bound.
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
        if (upperBoundY.compareTo(targetY) > 0) {
          break;
        }
        iIteration++;
      }
      RBPreconditions.checkArgument(
          iIteration < maxIterations,
          "After %s iterations, our upper bound of %s produces a Y-value %s that's still below the targetY of %s",
          maxIterations, upperBoundX, upperBoundY, targetY);
    } else {
      log.debug("No need to increase upX %s", upperBoundX);
    }

    log.debug("returning [%s, %s]", lowerBoundX, upperBoundX);
    return Range.closed(lowerBoundX, upperBoundX);
  }

}
