package com.rb.nonbiz.math.sequence;

import com.rb.nonbiz.util.RBPreconditions;

/**
 * A functional representation of a sequence (the mathematical class of functions).
 * This is sort of an array, except that an array has finite elements.
 *
 * <p> In practice, this is identical to {@link Iterable}, except the semantics are clearer.
 * So it's a sequence, but without random access. </p>
 * @param <T>
 */
public interface Sequence<T> extends Iterable<T> {

}
