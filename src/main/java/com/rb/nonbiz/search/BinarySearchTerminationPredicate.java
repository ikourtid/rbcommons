package com.rb.nonbiz.search;

import com.rb.nonbiz.collections.EitherOrBoth;
import com.rb.nonbiz.collections.EitherOrBoth.EitherOrBothVisitor;

import java.util.function.BiPredicate;

/**
 * Say we are doing a binary search to find an x such that f(x) is very near 2,
 * but we don't know f. Let's say that f(x) = x^2, w.l.o.g., but assume we can't know its inverse function,
 * which is sqrt(x).
 * Note that sqrt(2) = 1.41421356. Let's say we start the binary search with x bounds [1.0, 2.0].
 *
 * We could terminate if the binary search is:
 * a) looking at an x interval that is very small, e.g. x in [1.41,  1.42]
 * b) looking at a  y interval that is very small, e.g. y in [1.9,   2.1]
 * c) a OR b
 * d) a AND b
 *
 * This data class abstracts away those 4 cases. Its inner state is intentionally not made available,
 * but the point is that you just call BinarySearchTerminationPredicate#test
 * (using 'test' in the 'test predicate' sense per Java naming convention, not in the unit test sense).
 */
public class BinarySearchTerminationPredicate<X, Y> {

  private final EitherOrBoth<BiPredicate<X, X>, BiPredicate<Y, Y>> rawEitherOrBoth;
  private final boolean useOrInsteadOfAndWhenBothPresent;

  private BinarySearchTerminationPredicate(
      EitherOrBoth<BiPredicate<X, X>, BiPredicate<Y, Y>> rawEitherOrBoth,
      boolean useOrInsteadOfAndWhenBothPresent) {
    this.rawEitherOrBoth = rawEitherOrBoth;
    this.useOrInsteadOfAndWhenBothPresent = useOrInsteadOfAndWhenBothPresent;
  }

  /**
   * Terminates the binary search when the predicate on X is true; we don't care about Y here.
   */
  public static <X, Y> BinarySearchTerminationPredicate<X, Y> onlyTerminateBasedOnX(BiPredicate<X, X> xPredicate) {
    return new BinarySearchTerminationPredicate<>(EitherOrBoth.leftOnly(xPredicate), false); // false is irrelevant here
  }

  /**
   * Terminates the binary search when the predicate on Y is true; we don't care about X here.
   */
  public static <X, Y> BinarySearchTerminationPredicate<X, Y> onlyTerminateBasedOnY(BiPredicate<Y, Y> yPredicate) {
    return new BinarySearchTerminationPredicate<>(EitherOrBoth.rightOnly(yPredicate), false); // false is irrelevant here
  }

  /**
   * Terminates the binary search when EITHER predicate on X or Y is true.
   */
  public static <X, Y> BinarySearchTerminationPredicate<X, Y> terminateBasedOnXandY(
      BiPredicate<X, X> xPredicate, BiPredicate<Y, Y> yPredicate) {
    return new BinarySearchTerminationPredicate<>(EitherOrBoth.both(xPredicate, yPredicate), false);
  }

  /**
   * Terminates the binary search when BOTH predicates on X and Y are true.
   */
  public static <X, Y> BinarySearchTerminationPredicate<X, Y> terminateBasedOnXorY(
      BiPredicate<X, X> xPredicate, BiPredicate<Y, Y> yPredicate) {
    return new BinarySearchTerminationPredicate<>(EitherOrBoth.both(xPredicate, yPredicate), true);
  }

  public boolean test(X x1, X x2, Y y1, Y y2) {
    return rawEitherOrBoth.visit(new EitherOrBothVisitor<BiPredicate<X, X>, BiPredicate<Y, Y>, Boolean>() {
      @Override
      public Boolean visitLeftOnly(BiPredicate<X, X> terminateOnX) {
        return terminateOnX.test(x1, x2);
      }

      @Override
      public Boolean visitRightOnly(BiPredicate<Y, Y> terminateOnY) {
        return terminateOnY.test(y1, y2);
      }

      @Override
      public Boolean visitBoth(BiPredicate<X, X> terminateOnX, BiPredicate<Y, Y> terminateOnY) {
        boolean resultX = terminateOnX.test(x1, x2);
        boolean resultY = terminateOnY.test(y1, y2);
        return useOrInsteadOfAndWhenBothPresent
            ? resultX || resultY
            : resultX && resultY;
      }
    });
  }

}
