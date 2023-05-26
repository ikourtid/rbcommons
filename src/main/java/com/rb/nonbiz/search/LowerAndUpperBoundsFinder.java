package com.rb.nonbiz.search;

import com.google.common.collect.Range;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

public class LowerAndUpperBoundsFinder {

  /**
   * Find a Range of upper and upper <i>X</i>-value bounds whose <i>Y</i> values bracket a target <i>Y</i> value.
   *
   * <p> The user must supply 3 functions: </p>
   * <ol>
   *   <li> <i>evaluateInput(x)</i> to evaluate any <i>X</i> values. </li>
   *   <li> <i>reduceLowerBound(x)</i> to reduce the lower bound. </li>
   *   <li> <i>increaseUpperBound(x)</i> to increase the upper bound. </li>
   * </ol>
   *
   * <p> The user must also specify the maximum number of bound increases/decreases to use. The same maximum
   * will be applied for both increasing the upper bound as well as decreasing the lower bound. </p>
   *
   * <p> This method should be used if there is a single starting <i>X</i> value for the search.
   * There is a similar method using an upper and a lower starting range. </p>
   *
   * <p> Note: this method assumes that the function to be bound is monotonically increasing. </p>
   */
  public <X extends Comparable<? super X>, Y extends Comparable<? super Y>> Range<X> findLowerAndUpperBounds(
      Function<X, Y> evaluateInput,
      X startingPointForSearch,
      Y target,
      UnaryOperator<X> reduceLowerBound,
      UnaryOperator<X> increaseUpperBound,
      int maxIterations) {
    {
      X lowerBound = startingPointForSearch;
      X upperBound = startingPointForSearch;
      int comparison = evaluateInput.apply(startingPointForSearch).compareTo(target);
      if (comparison > 0) {
        upperBound = startingPointForSearch;
        // keep reducing lowerBound until we get below target, i.e. it becomes a real lower bound
        for (int i = 0; i < maxIterations; i++) {
          lowerBound = reduceLowerBound.apply(lowerBound);
          if (evaluateInput.apply(lowerBound).compareTo(target) < 0) {
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
          if (evaluateInput.apply(upperBound).compareTo(target) > 0) {
            return Range.closed(lowerBound, upperBound);
          }
        }
        throw new IllegalArgumentException(smartFormat(
            "After %s iterations, our upper bound of %s produces a value %s that's still below the target of %s",
            maxIterations, upperBound, evaluateInput.apply(upperBound), target));
      } else {
        // relax bounds so that they are strictly not equal to target
        return Range.closed(reduceLowerBound.apply(lowerBound), increaseUpperBound.apply(upperBound));
      }
    }
  }

  /**
   * Return a Range of <i>X</i>-values whose <i>Y</i>-values bracket an input target <i>Y</i>-value.
   *
   * <p> As above, but use this if you have separate initial lower and upper <i>X</i> values instead of a single one. </p>
   *
   * <p> Note: this method assumes that the function to be bound is monotonically increasing. </p>
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

    int comparisonLower = evaluateInput.apply(startingPointForSearchLower).compareTo(target);
    int comparisonUpper = evaluateInput.apply(startingPointForSearchUpper).compareTo(target);
    // check for early exit; no bound changes needed?
    if (comparisonLower < 0 && comparisonUpper > 0) {
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
        if (evaluateInput.apply(lowerBound).compareTo(target) < 0) {
          break;
        }
        iIteration++;
      }
      if (iIteration == maxIterations) {
        throw new IllegalArgumentException(smartFormat(
            "After %s iterations, our lower bound of %s produces a value %s that's still above the target of %s",
            maxIterations, lowerBound, evaluateInput.apply(lowerBound), target));
      }
    }

    // possibly increase the upper bound
    if (comparisonUpper <= 0) {
      // The initial upper X-bound does not have a Y-value above the target.
      // Keep increasing upperBound until we get above target, i.e. it becomes a real upper bound.
      int iIteration = 0;
      while (iIteration < maxIterations) {
        upperBound = increaseUpperBound.apply(upperBound);
        if (evaluateInput.apply(upperBound).compareTo(target) > 0) {
          break;
        }
        iIteration++;
      }
      if (iIteration == maxIterations) {
        throw new IllegalArgumentException(smartFormat(
            "After %s iterations, our upper bound of %s produces a value %s that's still below the target of %s",
            maxIterations, upperBound, evaluateInput.apply(upperBound), target));
      }
    }

    return Range.closed(lowerBound, upperBound);
  }

}
