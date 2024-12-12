package com.rb.nonbiz.search;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;
import com.rb.nonbiz.text.Strings;

import java.util.Optional;

/**
 * FIXME IAK comment and test
 */
public class BinarySearchXBoundsResult<X extends Comparable<? super X>> {

  public interface Visitor<T, X extends Comparable<? super X>> {

    T visitXBoundsCanBracketTargetY(Range<X> lowerAndUpperBounds);
    T visitXUpperBoundEvaluatesToBelowTargetY(X highestPossibleUpperBound);
    T visitXLowerBoundEvaluatesToAboveTargetY(X lowestPossibleLowerBound);

  }

  // Exactly 1 of these 3 will be non-empty. We enforce this by only allowing
  private Optional<Range<X>> lowerAndUpperBounds;
  private Optional<X> highestPossibleUpperBound;
  private Optional<X> lowestPossibleLowerBound;

  public BinarySearchXBoundsResult(
      Optional<Range<X>> lowerAndUpperBounds,
      Optional<X> highestPossibleUpperBound,
      Optional<X> lowestPossibleLowerBound) {
    this.lowerAndUpperBounds = lowerAndUpperBounds;
    this.highestPossibleUpperBound = highestPossibleUpperBound;
    this.lowestPossibleLowerBound = lowestPossibleLowerBound;
  }

  public static <X extends Comparable<? super X>> BinarySearchXBoundsResult<X> binarySearchBoundsCanBracketTargetY(
      Range<X> lowerAndUpperBounds) {
    return new BinarySearchXBoundsResult<>(Optional.of(lowerAndUpperBounds), Optional.empty(), Optional.empty());
  }

  public static <X extends Comparable<? super X>> BinarySearchXBoundsResult<X> xUpperBoundEvalutesToBelowTargetY(
      X highestPossibleUpperBound) {
    return new BinarySearchXBoundsResult<>(Optional.empty(), Optional.of(highestPossibleUpperBound), Optional.empty());
  }

  public static <X extends Comparable<? super X>> BinarySearchXBoundsResult<X> xLowerBoundEvalutesToAboveTargetY(
      X lowestPossibleLowerBound) {
    return new BinarySearchXBoundsResult<>(Optional.empty(), Optional.empty(), Optional.of(lowestPossibleLowerBound));
  }

  public <T> T visit(Visitor<T, X> visitor) {
    if (lowerAndUpperBounds.isPresent()) {
      return visitor.visitXBoundsCanBracketTargetY(lowerAndUpperBounds.get());
    }
    if (highestPossibleUpperBound.isPresent()) {
      return visitor.visitXUpperBoundEvaluatesToBelowTargetY(highestPossibleUpperBound.get());
    }
    if (lowestPossibleLowerBound.isPresent()) {
      return visitor.visitXLowerBoundEvaluatesToAboveTargetY(lowestPossibleLowerBound.get());
    }
    throw new IllegalArgumentException("Internal error! Exactly 1 of 3 cases here must be non-empty");
  }

  /**
   * Do not use this; it's here to help the test-only matcher. Instead, use the visitor.
   */
  @VisibleForTesting
  Optional<Range<X>> getLowerAndUpperBounds() {
    return lowerAndUpperBounds;
  }

  /**
   * Do not use this; it's here to help the test-only matcher. Instead, use the visitor.
   */
  @VisibleForTesting
  Optional<X> getHighestPossibleUpperBound() {
    return highestPossibleUpperBound;
  }

  /**
   * Do not use this; it's here to help the test-only matcher. Instead, use the visitor.
   */
  @VisibleForTesting
  Optional<X> getLowestPossibleLowerBound() {
    return lowestPossibleLowerBound;
  }

  @Override
  public String toString() {
    return Strings.format("[BSXBR %s BSXBR]", visit(new Visitor<String, X>() {
      @Override
      public String visitXBoundsCanBracketTargetY(Range<X> lowerAndUpperBounds) {
        return Strings.format("XBoundsCanBracketTargetY: %s", Strings.formatRange(lowerAndUpperBounds));
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
