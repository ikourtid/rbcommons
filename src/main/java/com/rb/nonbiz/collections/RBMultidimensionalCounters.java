package com.rb.nonbiz.collections;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.math3.util.MultidimensionalCounter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.collect.Iterators.singletonIterator;
import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.Coordinates.coordinates;
import static com.rb.nonbiz.collections.RBArrays.cutFromArray;
import static com.rb.nonbiz.collections.RBArrays.spliceIntoArray;
import static java.util.Collections.singletonList;

public class RBMultidimensionalCounters {

  public static Iterator<Coordinates> flatMultidimensionalIterator(MultidimensionalCounter counter) {
    return Iterators.transform(counter.iterator(), flatIndex -> coordinates(counter.getCounts(flatIndex)));
  }

  /**
   * This name is not great, but it is meant to contrast with the 'as we vary dimension'
   * iteration, which is all over the place (currently used in group sanity checks).
   *
   * <p> Given an N-dimensional slice, this will return an iterator that will iterate over k N-1-dimensional slices
   * of the array (effectively), where k is the size of the dimension we pass in. </p>
   *
   * <p> See very extensive comments in
   * {@link MultiDimensionalArrays#getSliceOfMultiDimensionalArray(MultiDimensionalArray, int, int)}
   * and the corresponding tests. </p>
   */
  public static Iterator<Iterator<Coordinates>> asWeFixDimensionIterator(
      MultidimensionalCounter counter, int dimension) {
    RBPreconditions.checkValidArrayElement(dimension, counter.getDimension());
    int[] originalDimensions = counter.getSizes();
    final int sizeOfDimension = originalDimensions[dimension];
    if (counter.getDimension() == 1) {
      // special case; the counter for a 1-d array was passed in
      return singletonIterator(
          IntStream.range(0, sizeOfDimension)
              .mapToObj(coordinateInVaryingDimension ->
                  coordinates(coordinateInVaryingDimension))
              .iterator());
    }

    // If the original counter is for an N-dimensional array,
    // this will be for N-1 dimensions after excluding the dimension we're varying.
    int[] decrementedDimensions = cutFromArray(originalDimensions, dimension);
    MultidimensionalCounter counterInDecremented = new MultidimensionalCounter(decrementedDimensions);

    return IntStream.range(0, sizeOfDimension)
        .mapToObj(coordinateInDimension ->
            Iterators.transform(
                counterInDecremented.iterator(), flatIndexInDecremented -> {
                  Coordinates coordinatesInDecremented = coordinates(counterInDecremented.getCounts(flatIndexInDecremented));
                  return coordinates(
                      spliceIntoArray(coordinatesInDecremented.getRawCoordinatesArray(), dimension, coordinateInDimension));
                }))
        .iterator();
  }

  /**
   * <p> See very extensive comments in
   * {@link MultiDimensionalArrays#getSliceOfMultiDimensionalArray(MultiDimensionalArray, int, int)}
   * and the corresponding tests. </p>
   *
   * See also {@link #asWeFixDimensionIterator(MultidimensionalCounter, int)}.
   */
  public static List<List<Coordinates>> asWeFixDimension(
      MultidimensionalCounter counter, int dimensionToVary) {
    return newArrayList(Iterators.transform(
        asWeFixDimensionIterator(counter, dimensionToVary),
        coordinatesIterator -> newArrayList(coordinatesIterator)));
  }

  /**
   * <p> See very extensive comments in
   * {@link MultiDimensionalArrays#getSliceOfMultiDimensionalArray(MultiDimensionalArray, int, int)}
   * and the corresponding tests. </p>
   */
  public static Iterator<Iterator<Coordinates>> asWeVaryDimensionIterator(MultidimensionalCounter counter, int dimensionToVary) {
    RBPreconditions.checkValidArrayElement(dimensionToVary, counter.getDimension());
    int[] originalDimensions = counter.getSizes();
    final int sizeOfVaryingDimension = originalDimensions[dimensionToVary];
    if (counter.getDimension() == 1) {
      // special case; the counter for a 1-d array was passed in
      return IntStream.range(0, sizeOfVaryingDimension)
          .mapToObj(coordinateInVaryingDimension ->
              singletonList(coordinates(coordinateInVaryingDimension)).iterator())
          .iterator();
    }

    // If the original counter is for an N-dimensional array,
    // this will be for N-1 dimensions after excluding the dimension we're varying.
    int[] decrementedDimensions = cutFromArray(originalDimensions, dimensionToVary);
    MultidimensionalCounter counterInDecrementedDimensions = new MultidimensionalCounter(decrementedDimensions);
    return Iterators.transform(
        counterInDecrementedDimensions.iterator(),
        flatIndexInDecremented -> {
          Coordinates coordinatesInDecremented = coordinates(counterInDecrementedDimensions.getCounts(flatIndexInDecremented));
          return IntStream.range(0, sizeOfVaryingDimension)
              .mapToObj(coordinateInVaryingDimension ->
                  coordinates(
                      spliceIntoArray(coordinatesInDecremented.getRawCoordinatesArray(), dimensionToVary, coordinateInVaryingDimension)))
              .iterator();
        });
  }

  /**
   * See very extensive comments in #getSliceOfMultiDimensionalArray
   */
  public static List<List<Coordinates>> asWeVaryDimension(MultidimensionalCounter counter, int dimensionToVary) {
    return newArrayList(Iterators.transform(
        asWeVaryDimensionIterator(counter, dimensionToVary),
        coordinatesIterator -> newArrayList(coordinatesIterator)));
  }

  public static String multidimensionalCounterToString(MultidimensionalCounter counter) {
    return Strings.format("[ %s -dimensional counter for sizes %s ]",
        counter.getDimension(),
        Joiner.on(' ').join(Arrays.stream(counter.getSizes()).iterator()));
  }

  /**
   * Returns true if the coordinates supplied have the correct number of dimensions,
   * and all of the coordinates are contained within the grid formed by the multidimensional counter.
   */
  public static boolean multidimensionalCounterCoordinatesAreValid(
      MultidimensionalCounter counter,
      Coordinates coordinates) {
    if (coordinates.getNumDimensions() != counter.getDimension()) {
      return false;
    }
    int[] dimensionSizes = counter.getSizes();
    for (int dimension = 0; dimension < dimensionSizes.length; dimension++) {
      if (coordinates.getNthCoordinate(dimension) >= dimensionSizes[dimension]) {
        return false;
      }
    }
    return true;
  }

}
