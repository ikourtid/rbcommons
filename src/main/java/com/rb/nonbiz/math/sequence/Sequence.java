package com.rb.nonbiz.math.sequence;

import com.rb.nonbiz.util.RBPreconditions;

/**
 * A functional representation of a sequence (the mathematical class of functions).
 * This is sort of an array, except that an array has finite elements.
 * @param <T>
 */
public interface Sequence<T> {

  /**
   * This is a bit awkward, but Java does not have a primitive 'unsigned int' like C / C++, so we wrap the
   * precondition around #get below, and implementing classes should instead only implement getUnsafe.
   */
  T getUnsafe(int nonNegativeN);

  default T get(int n) {
    RBPreconditions.checkArgument(
        n >= 0,
        "A sequence (function) can only be evaluated at n = 0, 1, ... ; got %s",
        n);
    return getUnsafe(n);
  }

}
