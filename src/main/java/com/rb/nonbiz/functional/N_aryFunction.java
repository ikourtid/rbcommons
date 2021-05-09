package com.rb.nonbiz.functional;

import com.rb.nonbiz.text.HasHumanReadableLabel;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.List;
import java.util.function.Function;

/**
 * A function {@code f(x1, x2, ..., xN)}
 * where the {@code x_i} are all of the same type, but {@code f()} doesn't have to return a
 * value of the same type as the {@code x_i}.
 *
 * <p> There are cases where we want a function that can take <i>N</i> arguments of the same type.
 * If <i>N</i> is known ahead of time, we can use a specialized class, such as {@code BiFunction}
 * or {@code TriFunction}. However, for cases where <i>N</i> is not known ahead of time,
 * the least bad solution is to accept a collection (we like {@code List<>}) of items of the same type. </p>
 *
 * <p> By storing the "N" inside this class, we can at least check at runtime that the size of the list of items passed
 * is indeed <i>N</i>. </p>
 */
public class N_aryFunction<X, Y> implements HasHumanReadableLabel {

  private final int N;
  private final Function<List<X>, Y> rawFunction;
  private final HumanReadableLabel label;

  protected N_aryFunction(int N, Function<List<X>, Y> rawFunction, HumanReadableLabel label) {
    this.N = N;
    this.rawFunction = rawFunction;
    this.label = label;
  }

  public static <X, Y> N_aryFunction<X, Y> n_aryFunction(int N, Function<List<X>, Y> rawFunction, HumanReadableLabel label) {
    RBPreconditions.checkArgument(
        N >= 1,
        "%s : N in the N-ary function must be positive, so that it's at least a unary function; was %s",
        label, N);
    return new N_aryFunction<>(N, rawFunction, label);
  }

  public int getN() {
    return N;
  }

  public Y evaluate(List<X> tuple) {
    RBSimilarityPreconditions.checkBothSame(
        N,
        tuple.size(),
        "%s : Tuple size for N-ary function is not correct",
        label);
    return rawFunction.apply(tuple);
  }

  @Override
  public HumanReadableLabel getHumanReadableLabel() {
    return label;
  }

}
