package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.rb.nonbiz.text.PrintsMultilineString;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.MultidimensionalCounter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Java allows multi-dimensional arrays, but it's hard to use them unless you know the # of dimensions ahead of time.
 * This is an implementation that allows you to use any number of dimensions.
 *
 * Just like with RBMap, RBSet, etc., you should not use a mutable version; once you add the data you want,
 * wrap the result around a plain {@link MultiDimensionalArray}{@code <T>}, and pass that around.
 *
 * The iterator iterates in the order defined by the MultidimensionalCounter.
 */
public class MutableMultiDimensionalArray<T> implements Iterable<T>, PrintsMultilineString {

  private final MultidimensionalCounter multidimensionalCounter;
  private final T[] values;

  private MutableMultiDimensionalArray(MultidimensionalCounter multidimensionalCounter, T[] values) {
    this.multidimensionalCounter = multidimensionalCounter;
    this.values = values;
  }

  @SuppressWarnings("unchecked")
  public static <T> MutableMultiDimensionalArray<T> mutableMultiDimensionalArray(int...sizes) {
    MultidimensionalCounter multidimensionalCounter = new MultidimensionalCounter(sizes);
    return new MutableMultiDimensionalArray<>(multidimensionalCounter, (T[]) new Object[multidimensionalCounter.getSize()]);
  }

  public T get(Coordinates coordinates) {
    return values[multidimensionalCounter.getCount(coordinates.getRawCoordinatesArray())];
  }

  @SuppressWarnings("unchecked")
  public MutableMultiDimensionalArray<T> setAssumingAbsent(T value, Coordinates coordinates) {
    try {
      int flatIndex = multidimensionalCounter.getCount(coordinates.getRawCoordinatesArray());
      RBPreconditions.checkArgument(
          values[flatIndex] == null,
          "Trying to set a value %s in location %s which already has in it the value %s",
          value, coordinates.getRawCoordinatesArray(), values[flatIndex]);
      RBSimilarityPreconditions.checkBothSame(
          coordinates.getNumDimensions(),
          multidimensionalCounter.getDimension(),
          "Invalid cardinality of coordinates of %s for multidimensional array with %s dimensions",
          coordinates.getNumDimensions(), multidimensionalCounter.getDimension());
      values[flatIndex] = value;
      return this;
    } catch (OutOfRangeException e) {
      // Sometimes we get an error message such as
      // org.apache.commons.math3.exception.OutOfRangeException: 3 out of [0, 0] range
      // I think it happens when we run out of memory.
      // The following will give us more details, even though it ends up rethrowing the exception.
      throw new RuntimeException(
          Strings.format("Coordinates were %s whereas multidimensional counter is %s", coordinates, multidimensionalCounter),
          e);
    }
  }

  public MultidimensionalCounter getMultidimensionalCounter() {
    return multidimensionalCounter;
  }

  @VisibleForTesting // This is to help the matcher; do not use it
  @SuppressWarnings("unchecked")
  public T[] getValues() {
    return (T[]) values;
  }

  @Override
  public String toString() {
    return Strings.format("[MMDA dimensions: %s ; %s MMDA]",
        Joiner.on(' ').join(IntStream.of(multidimensionalCounter.getSizes()).iterator()),
        Joiner.on(' ').join(Stream.of(values).iterator()));
  }

  @Override
  public String toMultilineString() {
    return Strings.format("[MMDA dimensions:\n%s\n ; \n%s\nMMDA]",
        Joiner.on('\n').join(IntStream.of(multidimensionalCounter.getSizes()).iterator()),
        Joiner.on('\n').join(Stream.of(values).iterator()));
  }

  @Override
  public Iterator<T> iterator() {
    return Arrays.asList(values).iterator();
  }

  public Stream<T> stream() {
    return Arrays.stream(values);
  }

}
