package com.rb.nonbiz.functional;

import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;
import java.util.function.BiFunction;

/**
 * f( {x1, x2, ..., xN}, xx)
 * where the x_i and xx are of the same type, and f is a boolean.
 *
 * @see NPlusOneAryFunction
 */
public class NPlusOneAryPredicate<X> extends NPlusOneAryFunction<X, Boolean> {

  protected NPlusOneAryPredicate(int N, BiFunction<List<X>, X, Boolean> rawPredicate, HumanReadableLabel label) {
    super(N, rawPredicate, label);
  }

  public static <X> NPlusOneAryPredicate<X> nPlusOneAryPredicate(
      int N, BiFunction<List<X>, X, Boolean> rawPredicate, HumanReadableLabel label) {
    RBPreconditions.checkArgument(
        N >= 0,
        // since we always have the 'lone' X passed in, the function will always be at least unary.
        // if N == 0, this is a unary function.
        "%s : N in the N+1-ary predicate must be non-negative, so that it's at least a unary function; was %s",
        label, N);
    return new NPlusOneAryPredicate<>(N, rawPredicate, label);
  }

}