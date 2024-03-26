package com.rb.nonbiz.math.sequence;

import java.util.Iterator;
import java.util.function.UnaryOperator;

/**
 * A functional representation of a sequence (the mathematical class of functions).
 * This is sort of an array, except that an array has finite elements.
 *
 * <p> Note that the generic item T does not itself have to be numeric. For example, we could have an
 * arithmetic progression of the first numeric item in a pair class: ("a", 10), ("a", 12), ("a", 14), etc. </p>
 *
 * <p> In practice, this class identical to {@link Iterable}, except the semantics are clearer.
 * So it's a sequence, but without random access. </p>
 *
 * @param <T>
 */
public abstract class Sequence<T> implements Iterable<T> {

  private final T initialValue;
  private final UnaryOperator<T> nextItemGenerator;

  protected Sequence(T initialValue, UnaryOperator<T> nextItemGenerator) {
    this.initialValue = initialValue;
    this.nextItemGenerator = nextItemGenerator;
  }

  public T getInitialValue() {
    return initialValue;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      private T currentValue = initialValue;

      @Override
      public boolean hasNext() {
        // a Sequence is infinite, so it will always have a next item.
        // In theory, it's not truly infinite because we will eventually run into floating point overflow
        // if we keep e.g. adding a difference to a number, as is the case with an arithmetic progression.
        // However, we can treat it as infinite in practice, considering how we will be using it.
        return true;
      }

      @Override
      public T next() {
        T toReturn = currentValue;
        currentValue = nextItemGenerator.apply(currentValue);
        return toReturn;
      }
    };
  }

}
