package com.rb.nonbiz.search;

import com.google.common.collect.Range;
import com.google.inject.Inject;
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

  @Inject LowerBoundFinder lowerBoundFinder;
  @Inject UpperBoundFinder upperBoundFinder;

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

    Optional<X> lowerBoundX = lowerBoundFinder.findPossiblyReducedLowerBound(
        evaluateInput, startingPointForSearchLower, targetY, reduceLowerBound, maxIterations);

    Optional<X> upperBoundX = upperBoundFinder.findPossiblyIncreasedUpperBound(
        evaluateInput, startingPointForSearchUpper, targetY, increaseUpperBound, maxIterations);

    // FIXME IAK Issue #1527 change this once we support this case
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
    // FIXME IAK Issue #1527 change to the above; for now let's keep this backwards-compatible
    return Range.closed(lowerBoundX.get(), upperBoundX.get());
  }

}
