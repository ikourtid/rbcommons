package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.Strings;
import org.apache.commons.math3.util.MultidimensionalCounter;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Java allows multi-dimensional arrays, but it's hard to use them unless you know the number of dimensions ahead of time.
 * This is an implementation that allows you to use any number of dimensions.
 *
 * <p> As elsewhere in the codebase, it is highly recommended that you use this immutable class instead of the mutable version,
 * {@link MutableMultiDimensionalArray}. The usual pattern is to create a {@link MutableMultiDimensionalArray} once,
 * wrap it in a MultiDimensionalArray, and then just pass around the MultiDimensionalArray. </p>
 *
 * <p> The iterator iterates in the order defined by the {@code MultidimensionalCounter}. </p>
 *
 * @see MutableMultiDimensionalArray
 */
public class MultiDimensionalArray<T> implements Iterable<T> {

  private final MutableMultiDimensionalArray<T> rawMutableArray;

  private MultiDimensionalArray(MutableMultiDimensionalArray<T> rawMutableArray) {
    this.rawMutableArray = rawMutableArray;
  }

  public static <T> MultiDimensionalArray<T> newMultiDimensionalArray(MutableMultiDimensionalArray<T> rawMutableArray) {
    return new MultiDimensionalArray<>(rawMutableArray);
  }

  public T get(Coordinates coordinates) {
    return rawMutableArray.get(coordinates);
  }

  @VisibleForTesting // don't use this; it's here to help the matcher
  MutableMultiDimensionalArray<T> getRawMutableArray() {
    return rawMutableArray;
  }

  public MultidimensionalCounter getMultidimensionalCounter() {
    return rawMutableArray.getMultidimensionalCounter();
  }

  @Override
  public String toString() {
    return Strings.format("[MDA %s MDA]", rawMutableArray.toString());
  }

  @Override
  public Iterator<T> iterator() {
    return rawMutableArray.iterator();
  }

  public Stream<T> stream() {
    return rawMutableArray.stream();
  }

}
