package com.rb.nonbiz.collections;

import org.apache.commons.math3.util.MultidimensionalCounter;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.rb.nonbiz.collections.Coordinates.coordinates;
import static com.rb.nonbiz.collections.MultiDimensionalArray.newMultiDimensionalArray;
import static com.rb.nonbiz.collections.MutableMultiDimensionalArray.mutableMultiDimensionalArray;

public class MultiDimensionalArrays {

  public static <T> MultiDimensionalArray<T> multiDimensionalArray(
      MultidimensionalCounter counter,
      BiFunction<Integer, Coordinates, T> generator) {
    MutableMultiDimensionalArray<T> mutableArray = mutableMultiDimensionalArray(counter.getSizes());
    counter.iterator().forEachRemaining(flatIndex -> {
      Coordinates coordinates = coordinates(counter.getCounts(flatIndex));
      mutableArray.setAssumingAbsent(generator.apply(flatIndex, coordinates), coordinates);
    });
    return newMultiDimensionalArray(mutableArray);
  }

  // This logic is also repeated in test, in multidimensionalCounterMatcher -
  // but we can't access it from here (non-test).
  public static <T1, T2> boolean multiDimensionalArraysHaveSameSize(
      MultiDimensionalArray<T1> arr1,
      MultiDimensionalArray<T2> arr2) {
    return multiDimensionalCountersAreEqual(arr1.getMultidimensionalCounter(), arr2.getMultidimensionalCounter());
  }

  public static boolean multiDimensionalCountersAreEqual(
      MultidimensionalCounter counter1,
      MultidimensionalCounter counter2) {
    int[] sizes1 = counter1.getSizes();
    int[] sizes2 = counter2.getSizes();
    if (sizes1.length != sizes2.length) {
      return false;
    }
    return IntStream.range(0, sizes1.length)
        .allMatch(i -> sizes1[i] == sizes2[i]);
  }

  /**
   * Create a (n-1)-dimensional slice of an n-dimensional array by fixing the 'dimensionToHoldConstant'-th dimension
   * to the value 'coordinateInDimension'. Tests may be clearer than an explanation.
   *
   * The best analogy I can think of is this: imagine a bunch of pencils on a table (with rubber erasers on the tip).
   * You place one layer of 4 pencils which are all aligned.
   * x axis is from the pencil eraser towards its tip ("left-right")
   * y axis is on the table: as it changes, we're looking at different pencils ("away from us - towards us")
   * Then, you add more layers on top for a total of 3. That would be the z axis ("up-down")
   *
   * If you were to saw off only the pencil erasers, that would be a slice where you hold the x axis constant
   * (it's where x=0, so it's a bit special, but it could be anywhere).
   * If you were to look at one of 4 vertical 'walls' of pencils, that's y.
   * If you were to look at one of the 3 layers of pencils on the table, that's z.
   *
   * #getSliceOfMultiDimensionalArray can generate one of the aforementioned slices.
   *
   * RBMultidimensionalCounters#asWeVaryDimensionIterator, however, is different. It's the opposite, in a way.
   * Using the same example, it would return you
   * all 12 pencils, if you specify the x axis.
   * The equivalent for y and z axes isn't as easy to explain, but for completeness's sake, let me try:
   * specifying the y axis would give you all 'away from us - towards us' 1-d chunks of the box we formed with the
   * pencils. E.g. one of those would be the shape formed by the 4 pencil erasers that are touching the table,
   * another by the 4 above it, etc.
   * And if we specify the z axis, you can imagine getting all vertical columns that stand on the table and which
   * together comprise the entire box.
   */
  public static <T> MultiDimensionalArray<T> getSliceOfMultiDimensionalArray(
      MultiDimensionalArray<T> originalArray, int sliceDimension, int sliceCoordinate) {
    MultidimensionalCounter originalDimensions = originalArray.getMultidimensionalCounter();
    int[] sliceSizes = originalDimensions.getSizes();
    sliceSizes[sliceDimension] = 1; // the resulting array is a copy, so this is OK to do in place
    MutableMultiDimensionalArray<T> slicedArray = mutableMultiDimensionalArray(sliceSizes);
    MultidimensionalCounter slicedDimensions = slicedArray.getMultidimensionalCounter();
    slicedDimensions.iterator().forEachRemaining(flatIndexInSliced -> {
      int[] coordinatesInSliced = slicedDimensions.getCounts(flatIndexInSliced);
      int[] coordinatesInOriginal = Arrays.copyOf(coordinatesInSliced, coordinatesInSliced.length);
      coordinatesInOriginal[sliceDimension] = sliceCoordinate;
      slicedArray.setAssumingAbsent(
          originalArray.get(coordinates(coordinatesInOriginal)),
          coordinates(coordinatesInSliced));
    });
    return newMultiDimensionalArray(slicedArray);
  }

  /**
   * Transforms a multidimensional array into another one of the same size, but with values transformed.
   * For any given coordinates in the final array, that value depends only on the value in the same coordinates
   * of the original multidimensional array.
   */
  public static <X, Y> MultiDimensionalArray<Y> transformMultiDimensionalArray(
      MultiDimensionalArray<X> initial, Function<X, Y> transformer) {
    return multiDimensionalArray(
        initial.getMultidimensionalCounter(),
        (ignoredInteger, coordinates) -> transformer.apply(initial.getRawMutableArray().get(coordinates)));
  }

}
