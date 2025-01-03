package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.util.RBPreconditions.checkArgument;

/**
 * A value and a {@link ClosedRange} that does not contain it.
 *
 * <p> This is useful for representing e.g. pre-optimization portfolio positions that are outside
 * their designated range (e.g. we're holding 2% but the range says we must hold between 4% and 5%). </p>
 */
public class ValueOutsideClosedRange<T extends Comparable<? super T>> {

  private final T value;
  private final ClosedRange<T> closedRange;

  private ValueOutsideClosedRange(T value, ClosedRange<T> closedRange) {
    this.value = value;
    this.closedRange = closedRange;
  }

  public static <T extends Comparable<? super T>> ValueOutsideClosedRange<T> valueOutsideClosedRange(
      T value, ClosedRange<T> closedRange) {
    checkArgument(
        !closedRange.contains(value),
        "Value %s is contained in closed range %s , but should not be in at this point in the code",
        value, closedRange);
    return new ValueOutsideClosedRange<>(value, closedRange);
  }

  public T getValue() {
    return value;
  }

  public ClosedRange<T> getClosedRange() {
    return closedRange;
  }

  @Override
  public String toString() {
    return Strings.format("[VOCR %s %s VOCR]", value, closedRange);
  }

}
