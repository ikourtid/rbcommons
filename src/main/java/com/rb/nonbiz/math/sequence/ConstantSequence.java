package com.rb.nonbiz.math.sequence;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.Strings;

/**
 * A {@link Sequence} (function of non-negative integer to T) where each value of the function is always the same.
 *
 * <p> This is clearer to use in lieu of an {@link ArithmeticProgression} or {@link GeometricProgression}
 * for cases where the value does not change. </p>
 */
public class ConstantSequence<T> extends SimpleSequence<T> {

  private ConstantSequence(T constantValue) {
    super(constantValue, v -> constantValue);
  }

  public static <T> ConstantSequence<T> constantSequence(T constantValue) {
    return new ConstantSequence<>(constantValue);
  }

  @Override
  public <T2> T2 visit(Visitor<T, T2> visitor) {
    return visitor.visitConstantSequence(this);
  }

  @VisibleForTesting // ideally you should use the Sequence interface, when possible.
  public T getConstantValue() {
    return getInitialValue();
  }

  @Override
  public String toString() {
    return Strings.format("[CS %s CS]", getConstantValue());
  }

}
