package com.rb.nonbiz.search;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.OneOf3;
import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.collections.OneOf3.only1stOf3;
import static com.rb.nonbiz.collections.OneOf3.only2ndOf3;
import static com.rb.nonbiz.collections.OneOf3.only3rdOf3;

/**
 * The result of trying to find some valid initial lower and upper bounds for a {@link BinarySearch}.
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
 *
 * <p> Important note: in the event that we can't find both valid bounds (lower and upper), the single bound
 * stored here will be just some valid lower (or upper) bound. It will not necessarily be the tightest bound possible.
 * This is symmetric to the the normal case where we are able to find valid lower AND upper bounds; those are just
 * initial values, and aren't guaranteed to be the tightest possible. It's the job of the binary search to find
 * those. The job of the {@link LowerAndUpperBoundsFinder} is to just find some valid initial bounds that the
 * binary search can start off of. </p>
 *
 * <p> In the event where only the lower or upper bound exists, the caller can't exactly run a binary search.
 * However, there should be a step that's somewhat similar to a binary search, where we keep tightening the initial
 * bound. The binary search effectively keeps tightening the bounds until a solution is found between the bounds.
 * This 'one-sided binary search' will be similar, except that it will only be tightening a single bound. </p>
 */
public class BinarySearchInitialXBoundsResult<X extends Comparable<? super X>> {

  public interface Visitor<T, X extends Comparable<? super X>> {

    T visitXBoundsCanBracketTargetY(ClosedRange<X> lowerAndUpperBounds);
    T visitOnlyHasValidUpperBound(X someValidUpperBound);
    T visitOnlyHasValidLowerBound(X someValidLowerBound);

  }

  private final OneOf3<ClosedRange<X>, X, X> rawOneOf3;

  public BinarySearchInitialXBoundsResult(OneOf3<ClosedRange<X>, X, X> rawOneOf3) {
    this.rawOneOf3 = rawOneOf3;
  }

  public static <X extends Comparable<? super X>> BinarySearchInitialXBoundsResult<X> binarySearchBoundsCanBracketTargetY(
      ClosedRange<X> lowerAndUpperBounds) {
    return new BinarySearchInitialXBoundsResult<>(only1stOf3(lowerAndUpperBounds));
  }

  public static <X extends Comparable<? super X>> BinarySearchInitialXBoundsResult<X> onlyHasValidUpperBoundForX(
      X someValidUpperBound) {
    return new BinarySearchInitialXBoundsResult<>(only2ndOf3(someValidUpperBound));
  }

  public static <X extends Comparable<? super X>> BinarySearchInitialXBoundsResult<X> onlyHasValidLowerBoundForX(
      X someValidLowerBound) {
    return new BinarySearchInitialXBoundsResult<>(only3rdOf3(someValidLowerBound));
  }

  public <T> T visit(Visitor<T, X> visitor) {
    return rawOneOf3.visit(new OneOf3.Visitor<ClosedRange<X>, X, X, T>() {
      @Override
      public T visitOnly1stOf3(ClosedRange<X> lowerAndUpperBounds) {
        return visitor.visitXBoundsCanBracketTargetY(lowerAndUpperBounds);
      }

      @Override
      public T visitOnly2ndOf3(X someValidUpperBound) {
        return visitor.visitOnlyHasValidUpperBound(someValidUpperBound);
      }

      @Override
      public T visitOnly3rdOf3(X someValidLowerBound) {
        return visitor.visitOnlyHasValidLowerBound(someValidLowerBound);
      }
    });
  }

  public ClosedRange<X> getLowerAndUpperBoundOrThrow() {
    return visit(new Visitor<ClosedRange<X>, X>() {
      @Override
      public ClosedRange<X> visitXBoundsCanBracketTargetY(ClosedRange<X> lowerAndUpperBounds) {
        return lowerAndUpperBounds;
      }

      @Override
      public ClosedRange<X> visitOnlyHasValidUpperBound(X someValidUpperBound) {
        throw new IllegalArgumentException(Strings.format(
            "Expected both lower and upper bounds, but only have upper= %s", someValidUpperBound));
      }

      @Override
      public ClosedRange<X> visitOnlyHasValidLowerBound(X someValidLowerBound) {
        throw new IllegalArgumentException(Strings.format(
            "Expected both lower and upper bounds, but only have lower= %s", someValidLowerBound));
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
      public String visitOnlyHasValidUpperBound(X someValidUpperBound) {
        return Strings.format("OnlyHasValidUpperBound: %s", someValidUpperBound);
      }

      @Override
      public String visitOnlyHasValidLowerBound(X someValidLowerBound) {
      return Strings.format("OnlyHasValidLowerBound: %s", someValidLowerBound);
      }
    }));
  }

}
