package com.rb.nonbiz.functional;

import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

/**
 * An operator {@code f(x1, x2, ..., xN)}
 * where the {@code x_i} are all of the same type, as is {@code f()}.
 *
 * <p> There are cases where we want a function that can take <i>N</i> arguments of the same type.
 * If <i>N</i> is known ahead of time, we can use a specialized class, such as {@link BinaryOperator}.
 * However, for cases where <i>N</i> is not known ahead of time,
 * the least bad solution is to accept a collection (we like {@code List<>}) of items of the same type. </p>
 *
 * <p> By storing the "<i>N</i>" inside this class, we can at least check at runtime that the size of the list of items passed
 * is indeed <i>N</i>. This way, the underlying function inside N_aryFunction won't have to check for that scenario. </p>
 */
public class N_aryOperator<T> extends N_aryFunction<T, T> {

  private N_aryOperator(int N, Function<List<T>, T> rawFunction, HumanReadableLabel label) {
    super(N, rawFunction, label);
  }

  public static <T> N_aryOperator<T> n_aryOperator(int N, Function<List<T>, T> rawFunction, HumanReadableLabel label) {
    RBPreconditions.checkArgument(
        N >= 1,
        "%s : N in the N-ary function must be positive, so that it's at least a unary function; was %s",
        label, N);
    return new N_aryOperator<>(N, rawFunction, label);
  }

  // This is for the simplest case where i = 1 and f(x) = x
  public static <T> N_aryOperator<T> n_aryIdentityOperator() {
    return n_aryOperator(1, listOfOne -> listOfOne.get(0), label("n-ary identity"));
  }

}
