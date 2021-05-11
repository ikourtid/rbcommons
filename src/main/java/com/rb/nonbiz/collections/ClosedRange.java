package com.rb.nonbiz.collections;

import com.google.common.collect.Range;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Objects;

/**
 * A simple wrapper around a Range that ensures that the range is closed.
 * There are many places in the code where we want to pass around a closed range, so instead of passing a Range
 * and asserting at the callee that it is a closed range, it is clearer to use a ClosedRange.
 */
public class ClosedRange<T extends Comparable<? super T>> {

  private final Range<T> rawRange;

  private ClosedRange(Range<T> rawRange) {
    this.rawRange = rawRange;
  }

  public static <T extends Comparable<? super T>> ClosedRange<T> closedRange(Range<T> range) {
    RBPreconditions.checkArgument(
        RBRanges.rangeIsClosed(range),
        "For a ClosedRange, the range passed in must be closed, but I got %s",
        range);
    return new ClosedRange<>(range);
  }

  public static <T extends Comparable<? super T>> ClosedRange<T> closedRange(
      T lowerEndpointInclusive, T upperEndpointInclusive) {
    RBPreconditions.checkArgument(
        lowerEndpointInclusive.compareTo(upperEndpointInclusive) <= 0,
        "Values in ClosedRange are not ordered: %s %s",
        lowerEndpointInclusive, upperEndpointInclusive);
    return new ClosedRange<>(Range.closed(lowerEndpointInclusive, upperEndpointInclusive));
  }

  public static <T extends Comparable<? super T>> ClosedRange<T> singletonClosedRange(T singleValue) {
    return new ClosedRange<>(Range.singleton(singleValue));
  }

  public Range<T> asRange() {
    return rawRange;
  }

  /**
   * With a regular Range, we can't just call lowerEndpoint without confirming it exists. However, this is a ClosedRange,
   * so that's guaranteed to be the case.
   */
  public T lowerEndpoint() {
    return rawRange.lowerEndpoint();
  }

  /**
   * With a regular Range, we can't just call upperEndpoint without confirming it exists. However, this is a ClosedRange,
   * so that's guaranteed to be the case.
   */
  public T upperEndpoint() {
    return rawRange.upperEndpoint();
  }

  public boolean contains(T value) {
    return rawRange.contains(value);
  }

  // We don't normally implement #equals and #hashCode, but we want ClosedRange to do it because some preconditions need that.
  // Plus, since this is really just a special type of Range, and Range itself implements #equals and #hashCode,
  // it can't hurt too much to do it here as well.
  // IDE-generated
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ClosedRange<?> that = (ClosedRange<?>) o;
    return rawRange.equals(that.rawRange);
  }

  // IDE-generated
  @Override
  public int hashCode() {
    return Objects.hash(rawRange);
  }

  @Override
  public String toString() {
    // Because a ClosedRange is a special case of a Range, and it prints in several places, we should avoid the
    // return Strings.format("[CR %s CR]", rawRange);
    // and just print the raw range.
    return rawRange.toString();
  }

}
