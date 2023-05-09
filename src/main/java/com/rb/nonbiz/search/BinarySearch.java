package com.rb.nonbiz.search;

import com.rb.nonbiz.search.BinarySearchResult.BinarySearchResultBuilder;
import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Comparator;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBComparables.monotonic;
import static com.rb.nonbiz.collections.RBComparators.nonDecreasingPerComparator;
import static com.rb.nonbiz.text.RBLog.rbLog;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

/**
 * Generalized code for doing binary search.
 *
 * @see BinarySearchParameters
 */
public class BinarySearch {

  private static final RBLog log = rbLog(BinarySearch.class);

  public <X, Y> BinarySearchResult<X, Y> performBinarySearch(BinarySearchParameters<X, Y> parameters) {
    X lowerBoundX = parameters.getLowerBoundX();
    X upperBoundX = parameters.getUpperBoundX();
    Comparator<? super X> comparatorForX = parameters.getComparatorForX();
    Comparator<? super Y> comparatorForY = parameters.getComparatorForY();
    Y targetY = parameters.getTargetY();
    Function<X, Y> evaluatorOfX = parameters.getEvaluatorOfX();
    int maxIterations = parameters.getMaxIterations();

    Y lowerBoundY = evaluatorOfX.apply(lowerBoundX);
    Y upperBoundY = evaluatorOfX.apply(upperBoundX);
    int numIterations = 0;
    while (numIterations++ < maxIterations) {
      if (parameters.getTerminationPredicate().test(lowerBoundX, upperBoundX, lowerBoundY, upperBoundY)) {
        return BinarySearchResultBuilder.<X, Y>binarySearchResultBuilder()
            .setLowerBoundX(lowerBoundX)
            .setUpperBoundX(upperBoundX)
            .setLowerBoundY(lowerBoundY)
            .setUpperBoundY(upperBoundY)
            .setNumIterationsUsed(numIterations)
            .setTargetY(targetY)
            .setComparatorForY(comparatorForY)
            .build();
      }
      X midpointX = parameters.getMidpointGenerator().apply(lowerBoundX, upperBoundX);
      RBPreconditions.checkArgument(
          monotonic(comparatorForX, lowerBoundX, midpointX, upperBoundX),
          "Midpoint generator is probably bad: lower / initial mid / upper should be monotonic (not strictly) but were %s %s %s",
          lowerBoundX, upperBoundX, upperBoundX);
      Y midpointY = evaluatorOfX.apply(midpointX);
      boolean isBetweenLowerAndUpperInclusive = nonDecreasingPerComparator(
          comparatorForY, lowerBoundY, midpointY, upperBoundY);
      if (!isBetweenLowerAndUpperInclusive) {
        throw new IllegalArgumentException(smartFormat(
            "Using midpoint of %s (between %s and %s ) we got value %s which is not between %s and %s , inclusive",
            midpointX, lowerBoundX, upperBoundX, midpointY, lowerBoundY, upperBoundY));
      }
      int comparisonY = comparatorForY.compare(midpointY, targetY);
      log.debug("compY %s midX %s low %s up %s", comparisonY, midpointX, lowerBoundX, upperBoundX);
      if (comparisonY < 0) {
        lowerBoundX = midpointX;
        lowerBoundY = midpointY;
      } else if (comparisonY > 0) {
        upperBoundX = midpointX;
        upperBoundY = midpointY;
      } else {
        // unlikely with doubles, but can't hurt to have this here.
        // This is when the binary search step that generates the midpoint happens to find the exact y = f(x)
        // that we are searching for.
        return BinarySearchResultBuilder.<X, Y>binarySearchResultBuilder()
            .setLowerBoundX(midpointX)
            .setUpperBoundX(midpointX)
            .setLowerBoundY(midpointY)
            .setUpperBoundY(midpointY)
            .setNumIterationsUsed(numIterations)
            .setTargetY(targetY)
            .setComparatorForY(comparatorForY)
            .build();
      }
    }
    throw new IllegalArgumentException(smartFormat(
        "Binary search could not finish, even within %s iterations; lowerX %s ; upperX %s ; lowerY %s ; upper Y%s",
        maxIterations,
        lowerBoundX, upperBoundX,
        evaluatorOfX.apply(lowerBoundX),
        evaluatorOfX.apply(upperBoundX)));
  }

}
