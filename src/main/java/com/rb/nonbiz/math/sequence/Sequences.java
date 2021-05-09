package com.rb.nonbiz.math.sequence;

import java.util.function.Function;

/**
 * Static methods related to {@link Sequence}
 */
public class Sequences {

  /**
   * Transforms a Sequence of T1 into a Sequence of T2.
   */
  public static <T1, T2> Sequence<T2> transformedSequence(Sequence<T1> initialSequence, Function<T1, T2> transformer) {
    return new Sequence<T2>() {
      @Override
      public T2 getUnsafe(int nonNegativeN) {
        return transformer.apply(initialSequence.getUnsafe(nonNegativeN));
      }
    };
  }

}
