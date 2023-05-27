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
 *   <li> <i>reduceLowerBound(x)</i> to reduce the lower bound. </li>
 *   <li> <i>increaseUpperBound(x)</i> to increase the upper bound. </li>
 * </ol>
 *
 * <p> The user must also specify the maximum number of bound increases/decreases to use. The same maximum
 * number will be applied for both increasing the upper bound as well as decreasing the lower bound. </p>
 *
 * <p> Note: these methods assume that the function to be bound is strictly increasing. </p>
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
      Y target,
      UnaryOperator<X> reduceLowerBound,
      UnaryOperator<X> increaseUpperBound,
      int maxIterations) {
    X lowerBound = startingPointForSearch;
    X upperBound = startingPointForSearch;
    Y startingPointY = evaluateInput.apply(startingPointForSearch);
    int comparison = startingPointY.compareTo(target);
    log.debug("initX %s initY %s tgtY %s compare %s", startingPointForSearch, startingPointY, target, comparison);

    if (comparison > 0) {
      upperBound = startingPointForSearch;
      // keep reducing lowerBound until we get below target, i.e. it becomes a real lower bound
      for (int i = 0; i < maxIterations; i++) {
        lowerBound = reduceLowerBound.apply(lowerBound);
        Y lowerBoundY = evaluateInput.apply(lowerBound);
        log.debug("i=%s reduce lowX to %s ; lowY %s", i, lowerBound, lowerBoundY);
        if (lowerBoundY.compareTo(target) < 0) {
          log.debug("returning [%s, %s]", lowerBound, upperBound);
          return Range.closed(lowerBound, upperBound);
        }
      }
      throw new IllegalArgumentException(smartFormat(
          "After %s iterations, our lower bound of %s produces a value %s that's still above the target of %s",
          maxIterations, lowerBound, evaluateInput.apply(lowerBound), target));
    } else if (comparison < 0) {
      // keep increasing upperBound until we get above target, i.e. it becomes a real upper bound
      lowerBound = startingPointForSearch;
      for (int i = 0; i < maxIterations; i++) {
        upperBound = increaseUpperBound.apply(upperBound);
        Y upperBoundY = evaluateInput.apply(upperBound);
        log.debug("i=%s increase upX to %s ; upY %s", i, upperBound, upperBoundY);
        if (upperBoundY.compareTo(target) > 0) {
          log.debug("returning [%s, %s]", lowerBound, upperBound);
          return Range.closed(lowerBound, upperBound);
        }
      }
      throw new IllegalArgumentException(smartFormat(
          "After %s iterations, our upper bound of %s produces a value %s that's still below the target of %s",
          maxIterations, upperBound, evaluateInput.apply(upperBound), target));
    } else {
      // relax bounds so that they are strictly not equal to target
      lowerBound = reduceLowerBound.apply(lowerBound);
      upperBound = increaseUpperBound.apply(upperBound);
      log.debug("returning [%s, %s]", lowerBound, upperBound);
      return Range.closed(lowerBound, upperBound);
    }
  }

  /**
   * Return a Range of <i>X</i>-values whose <i>Y</i>-values bracket an input target <i>Y</i>-value.
   *
   * <p> As above, but use this if you have separate initial lower and upper <i>X</i> guessed values
   * instead of a single value. </p>
   *
   * <p> Note: this method assumes that the function to be bound is strictly increasing. </p>
   */
  public <X extends Comparable<? super X>, Y extends Comparable<? super Y>> Range<X> findLowerAndUpperBounds(
      Function<X, Y> evaluateInput,
      X startingPointForSearchLower,
      X startingPointForSearchUpper,
      Y target,
      UnaryOperator<X> reduceLowerBound,
      UnaryOperator<X> increaseUpperBound,
      int maxIterations) {
    RBPreconditions.checkArgument(
        startingPointForSearchLower.compareTo(startingPointForSearchUpper) <= 0,
        "startingPointForSearchLower %s must not be greater than startingPointForSearchUpper %s",
        startingPointForSearchLower, startingPointForSearchUpper);

    Y yLowerBound = evaluateInput.apply(startingPointForSearchLower);
    Y yUpperBound = evaluateInput.apply(startingPointForSearchUpper);
    RBPreconditions.checkArgument(
        startingPointForSearchLower.compareTo(startingPointForSearchUpper) == 0 ||
            yLowerBound.compareTo(yUpperBound) < 0,
        "yLowerBound %s must be less than yUpperBound %s",
        yLowerBound, yUpperBound);

    int comparisonLower = yLowerBound.compareTo(target);
    int comparisonUpper = yUpperBound.compareTo(target);
    log.debug(smartFormat("lowX %s upX %s ; lowY %s upY %s ; tgtY %s",
        startingPointForSearchLower, startingPointForSearchUpper, yLowerBound, yUpperBound, target));

    // check for early exit; no bound changes needed?
    if (comparisonLower < 0 && comparisonUpper > 0) {
      log.debug("returning no changes: [%s, %s]", startingPointForSearchLower, startingPointForSearchUpper);
      return Range.closed(startingPointForSearchLower, startingPointForSearchUpper) ;
    }

    X lowerBound = startingPointForSearchLower;
    X upperBound = startingPointForSearchUpper;

    // possibly reduce the lower bound
    if (comparisonLower >= 0) {
      // The initial lower X-bound does not have a Y-value below the target.
      // Keep reducing lowerBound until we get below target, i.e. it becomes a real lower bound.
      int iIteration = 0;
      while (iIteration < maxIterations) {
        lowerBound = reduceLowerBound.apply(lowerBound);
        Y lowerBoundY = evaluateInput.apply(lowerBound);
        log.debug("i=%s reduce lowX to %s ; lowY %s", iIteration, lowerBound, lowerBoundY);
        if (lowerBoundY.compareTo(target) < 0) {
          break;
        }
        iIteration++;
      }
      if (iIteration == maxIterations) {
        throw new IllegalArgumentException(smartFormat(
            "After %s iterations, our lower bound of %s produces a value %s that's still above the target of %s",
            maxIterations, lowerBound, evaluateInput.apply(lowerBound), target));
      }
    } else {
      log.debug("No need to reduce lowX %s", lowerBound);
    }

    // possibly increase the upper bound
    if (comparisonUpper <= 0) {
      // The initial upper X-bound does not have a Y-value above the target.
      // Keep increasing upperBound until we get above target, i.e. it becomes a real upper bound.
      int iIteration = 0;
      while (iIteration < maxIterations) {
        upperBound = increaseUpperBound.apply(upperBound);
        Y upperBoundY = evaluateInput.apply(upperBound);
        log.debug("i=%s increase upX to %s ; upY %s", iIteration, upperBound, upperBoundY);
        if (upperBoundY.compareTo(target) > 0) {
          break;
        }
        iIteration++;
      }
      if (iIteration == maxIterations) {
        throw new IllegalArgumentException(smartFormat(
            "After %s iterations, our upper bound of %s produces a value %s that's still below the target of %s",
            maxIterations, upperBound, evaluateInput.apply(upperBound), target));
      }
    } else {
      log.debug("No need to increase upX %s", upperBound);
    }

    log.debug("returning [%s, %s]", lowerBound, upperBound);
    return Range.closed(lowerBound, upperBound);
  }

}
