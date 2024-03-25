package com.rb.nonbiz.functional;

import com.rb.nonbiz.text.HasHumanReadableLabel;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.List;
import java.util.function.BiFunction;

/**
 * A descriptor for a function f( {x1, x2, ..., xN}, xx)
 * where the x_i and xx are all of the same type, but f doesn't have to return a value of the same type as the x_i.
 *
 * <p> This is semantically almost the same as a function that takes N+1 values of type X, except that there are cases
 * where it is more convenient to break them up into a group of N plus a single value of 1. </p>
 *
 * @see N_aryFunction
 */
public class NPlusOneAryFunction<X, Y> implements HasHumanReadableLabel {

  private final int N;
  private final BiFunction<List<X>, X, Y> rawFunction;
  private final HumanReadableLabel label;

  protected NPlusOneAryFunction(int N, BiFunction<List<X>, X, Y> rawFunction, HumanReadableLabel label) {
    this.N = N;
    this.rawFunction = rawFunction;
    this.label = label;
  }

  public static <X, Y> NPlusOneAryFunction<X, Y> nPlusOneAryFunction(
      int N, BiFunction<List<X>, X, Y> rawFunction, HumanReadableLabel label) {
    RBPreconditions.checkArgument(
        N >= 0,
        // since we always have the 'lone' X passed in, the function will always be at least unary.
        // if N == 0, this is a unary function.
        "%s : N in the N+1-ary function must be non-negative, so that it's at least a unary function; was %s",
        label, N);
    return new NPlusOneAryFunction<>(N, rawFunction, label);
  }

  public int getN() {
    return N;
  }

  public Y evaluate(List<X> firstList, X secondItem) {
    RBSimilarityPreconditions.checkBothSame(
        N,
        firstList.size(),
        "%s : Tuple size for N-ary function is not correct",
        label);
    return rawFunction.apply(firstList, secondItem);
  }

  @Override
  public HumanReadableLabel getHumanReadableLabel() {
    return label;
  }

}
