package com.rb.nonbiz.math.sequence;


import com.google.common.collect.Iterators;

import java.util.function.Function;

/**
 * Static methods related to {@link Sequence}
 */
public class Sequences {

  /**
   * Transforms a Sequence of T1 into a Sequence of T2.
   */
  public static <T1, T2> Sequence<T2> transformedSequence(Sequence<T1> initialSequence, Function<T1, T2> transformer) {
    return () -> Iterators.transform(
        initialSequence.iterator(),
        // we can't just use 'transformer' as is, because it's a java.util.function.Function,
        // whereas Iterators.transform takes a com.google.common.base.Function.
        v -> transformer.apply(v));
  }

}
