package com.rb.nonbiz.search;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.OneOf3;
import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.collections.OneOf3.only1stOf3;
import static com.rb.nonbiz.collections.OneOf3.only2ndOf3;
import static com.rb.nonbiz.collections.OneOf3.only3rdOf3;

/**
 * The result of trying to find initial lower and upper bounds for a {@link BinarySearch}.
 *
 * <p> Let's say we're trying to do a binary search on values of X such that Y = f(X) will have a value of 7.
 * Ideally, we'll find two bounds: a lower bound X1 and an upper bound X2 such that f(X1) &lt; 7 &lt; f(X2).
 *
 * <p> However, in some cases, it's also possible that the lowest valid lower bound X1 we can find
 * is such that f(X1) > 7. In that case, we don't care about X2, because 7 &lt; f(X1) &lt; f(X2).
 * So X1 is the best we can do. We need to specially flag that case, because the caller should be less likely
 * to accept the result. Here's why: </p>
 *
 * <p> Although this depends on the problem domain, we're more likely to trust a solution that is achieved by
 * X1 and X2 that 'bracket' the target, i.e. f(X1) &lt; 7 &lt; f(X2). That's for cases where we trust that a binary
 * search will converge to a solution that's very close to what we want. An example is rounding shares for orders:
 * when the binary search terminates, the difference between X1 and X2 will be a single share for a single stock;
 * e.g. we're buying 53 IBM in the case of X1, and 54 in the case of X2. So, again depending on the domain,
 * we're more likely to trust a solution in the 'normal' case of being able to find lower and upper bounds that
 * bracket the solution. </p>
 *
 * <p> On the other hand, if we can't find initial lower & upper bounds that 'bracket' the ideal solution
 * (that is, we can't find X1 and X2 such that f(X1) &lt; 7 &lt; f(X2)), we need to flag this, so that the caller
 * can decide whether or not to accept the result. In some cases, the highest upper bound X2 could be such that e.g.
 * f(X2) = 6.99 (in this example), which is close enough to 7, so perhaps this is a close enough solution.
 * This code is too low-level to know what counts as 'close enough'. Therefore, here we will just flag
 * those special cases, and the caller can decide. </p>
 */
public class BinarySearchInitialXBoundsResult<X extends Comparable<? super X>> {

  public interface Visitor<T, X extends Comparable<? super X>> {

    T visitXBoundsCanBracketTargetY(ClosedRange<X> lowerAndUpperBounds);
    T visitXUpperBoundEvaluatesToBelowTargetY(X highestPossibleUpperBound);
    T visitXLowerBoundEvaluatesToAboveTargetY(X lowestPossibleLowerBound);

  }

  private final OneOf3<ClosedRange<X>, X, X> rawOneOf3;

  public BinarySearchInitialXBoundsResult(
      OneOf3<ClosedRange<X>, X, X> rawOneOf3) {
    this.rawOneOf3 = rawOneOf3;
  }

  public static <X extends Comparable<? super X>> BinarySearchInitialXBoundsResult<X> binarySearchBoundsCanBracketTargetY(
      ClosedRange<X> lowerAndUpperBounds) {
    return new BinarySearchInitialXBoundsResult<>(only1stOf3(lowerAndUpperBounds));
  }

  public static <X extends Comparable<? super X>> BinarySearchInitialXBoundsResult<X> xUpperBoundEvaluatesToBelowTargetY(
      X highestPossibleUpperBound) {
    return new BinarySearchInitialXBoundsResult<>(only2ndOf3(highestPossibleUpperBound));
  }

  public static <X extends Comparable<? super X>> BinarySearchInitialXBoundsResult<X> xLowerBoundEvaluatesToAboveTargetY(
      X lowestPossibleLowerBound) {
    return new BinarySearchInitialXBoundsResult<>(only3rdOf3(lowestPossibleLowerBound));
  }

  public <T> T visit(Visitor<T, X> visitor) {
    return rawOneOf3.visit(new OneOf3.Visitor<ClosedRange<X>, X, X, T>() {
      @Override
      public T visitOnly1stOf3(ClosedRange<X> lowerAndUpperBounds) {
        return visitor.visitXBoundsCanBracketTargetY(lowerAndUpperBounds);
      }

      @Override
      public T visitOnly2ndOf3(X highestPossibleUpperBound) {
        return visitor.visitXUpperBoundEvaluatesToBelowTargetY(highestPossibleUpperBound);
      }

      @Override
      public T visitOnly3rdOf3(X lowestPossibleLowerBound) {
        return visitor.visitXLowerBoundEvaluatesToAboveTargetY(lowestPossibleLowerBound);
      }
    });
  }

  /**
   * Do not use this; it's here to help the test-only matcher. Instead, use the visitor.
   */
  @VisibleForTesting
  OneOf3<ClosedRange<X>, X, X> getRawOneOf3() {
    return rawOneOf3;
  }

  @Override
  public String toString() {
    return Strings.format("[BSIXBR %s BSIXBR]", visit(new Visitor<String, X>() {
      @Override
      public String visitXBoundsCanBracketTargetY(ClosedRange<X> lowerAndUpperBounds) {
        return Strings.format("XBoundsCanBracketTargetY: %s", Strings.formatRange(lowerAndUpperBounds.asRange()));
      }

      @Override
      public String visitXUpperBoundEvaluatesToBelowTargetY(X highestPossibleUpperBound) {
        return Strings.format("XUpperBoundEvaluatesToBelowTargetY: %s", highestPossibleUpperBound);
      }

      @Override
      public String visitXLowerBoundEvaluatesToAboveTargetY(X lowestPossibleLowerBound) {
        return Strings.format("XLowerBoundEvaluatesToAboveTargetY: %s", lowestPossibleLowerBound);
      }
    }));
  }

}
