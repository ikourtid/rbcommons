package com.rb.nonbiz.math.sequence;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.Strings;

/**
 * A {@link Sequence} (function of non-negative integer to T) where each value of the function is always the same.
 *
 * This is clearer to use in lieu of an ArithmeticProgression or GeometricProgression for cases where the value
 * does not change. Moreover, it is applicable to Sequence of any type, not just Double.
 */
public class ConstantSequence<T> implements Sequence<T> {

  private final T constantValue;

  private ConstantSequence(T constantValue) {
    this.constantValue = constantValue;
  }

  public static <T> ConstantSequence<T> constantSequence(T constantValue) {
    return new ConstantSequence<>(constantValue);
  }

  @Override
  public T getUnsafe(int ignoredNonNegativeN) {
    return constantValue;
  }

  @VisibleForTesting // ideally you should use the Sequence interface; this is here for the test matcher.
  T getConstantValue() {
    return constantValue;
  }

  @Override
  public String toString() {
    return Strings.format("[CS %s CS]", constantValue);
  }

}
