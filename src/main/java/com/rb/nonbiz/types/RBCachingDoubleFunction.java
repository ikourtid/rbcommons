package com.rb.nonbiz.types;

import com.rb.nonbiz.collections.MutableRBDoubleKeyedMap;

import java.util.Optional;
import java.util.function.DoubleFunction;

import static com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.BehaviorWhenTwoDoubleKeysAreClose.USE_NEAREST_OR_FLOOR;
import static com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.newMutableRBDoubleKeyedMap;

/**
 * Creates a wrapper around an existing {@link DoubleFunction}
 *
 * <p> This is useful for expensive calculations, where we want to avoid invoking the supplied function again
 * if we have already done it before. However, the complexity is that the parameter of the function is a double,
 * so checking whether we've called this function on the same x uses an epsilon comparison. </p>
 *
 * <p> Obviously you should avoid using this in cases where two x values that are less than epsilon apart
 * produce very different results. Say f(x1) = y1 and f(x2) = y2, and x1 and x2 are less than {@link #epsilonForX} apart.
 * Normally, y1 and y2 would also be very near each other. If not, then if we first call f(x1) and get back y1, the next
 * time we call f(x2) we will also get y1, because of caching. So if y1 is very different than y2, we should not
 * use this class. </p>
 */
public class RBCachingDoubleFunction<V> implements DoubleFunction<V> {

  private final DoubleFunction<V> function;
  private final MutableRBDoubleKeyedMap<V> mutableAlreadyCalculatedResults;
  private final Epsilon epsilonForX;

  private RBCachingDoubleFunction(
      DoubleFunction<V> function,
      MutableRBDoubleKeyedMap<V> mutableAlreadyCalculatedResults,
      Epsilon epsilonForX) {
    this.function = function;
    this.mutableAlreadyCalculatedResults = mutableAlreadyCalculatedResults;
    this.epsilonForX = epsilonForX;
  }

  public static <V> RBCachingDoubleFunction<V> rbCachingDoubleFunction(
      DoubleFunction<V> function,
      Epsilon epsilonForX) {
    return new RBCachingDoubleFunction<>(function, newMutableRBDoubleKeyedMap(), epsilonForX);
  }


  @Override
  public V apply(double x) {
    // First, check to see if a result has already been calculated. That's the whole point of using
    // RBCachingDoubleFunction instead of a plain DoubleFunction.

    // We could also have used USE_NEAREST_OR_CEILING here; the difference is only for the rare few cases where the
    // key is exactly in the middle of two other valid keys.
    Optional<V> existingY = mutableAlreadyCalculatedResults.getOptional(x, epsilonForX, USE_NEAREST_OR_FLOOR);
    if (existingY.isPresent()) {
      return existingY.get();
    }

    V y = function.apply(x);
    mutableAlreadyCalculatedResults.put(x, y);
    return y;
  }

}
