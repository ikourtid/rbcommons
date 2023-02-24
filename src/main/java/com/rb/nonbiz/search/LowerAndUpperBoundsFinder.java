package com.rb.nonbiz.search;

import com.google.common.collect.Range;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public class LowerAndUpperBoundsFinder {

  public <X extends Comparable, Y extends Comparable> Range<X>
  findLowerAndUpperBounds(Function<X, Y> evaluateInput, X startingPointForSearch, Y target,
                          UnaryOperator<X> reduceLowerBound,
                          UnaryOperator<X> increaseUpperBound,
                          int maxIterations) {
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
