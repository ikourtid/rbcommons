package com.rb.nonbiz.collections;

import com.google.common.base.Joiner;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.math3.util.MultidimensionalCounter;

import java.util.Arrays;

/**
 * These are coordinates in an N-dimensional space.
 *
 * <p> We keep the underlying collection as an array, so that this can interoperate easily with
 * {@link MultidimensionalCounter}. </p>
 */
public class Coordinates {

  private final int[] rawCoordinatesArray;

  private Coordinates(int[] rawCoordinatesArray) {
    this.rawCoordinatesArray = rawCoordinatesArray;
  }

  public static Coordinates coordinates(int ... rawCoordinatesArray) {
    RBPreconditions.checkArgument(rawCoordinatesArray.length > 0);
    for (int i = 0; i < rawCoordinatesArray.length; i++) {
      RBPreconditions.checkArgument(rawCoordinatesArray[i] >= 0);
    }
    return new Coordinates(rawCoordinatesArray);
  }

  public static Coordinates coordinatesAllZero(int numberOfDimensions) {
    RBPreconditions.checkArgument(numberOfDimensions >= 1);
    // int has a default value of zero.
    return coordinates(new int[numberOfDimensions]);
  }

  public int[] getRawCoordinatesArray() {
    return rawCoordinatesArray;
  }

  public int getNthCoordinate(int dimension) {
    return rawCoordinatesArray[dimension];
  }

  public int getNumDimensions() {
    return rawCoordinatesArray.length;
  }

  public Coordinates getSubset(int startInclusive, int endExclusive) {
    RBPreconditions.checkArgument(
        0 < endExclusive && endExclusive <= rawCoordinatesArray.length);
    return coordinates(Arrays.copyOfRange(rawCoordinatesArray, startInclusive, endExclusive));
  }

  public Coordinates copyWithChangedNthItem(int index, int newValue) {
    RBPreconditions.checkValidArrayElement(index, rawCoordinatesArray.length);
    RBPreconditions.checkArgument(newValue >= 0);
    int[] newArray = rawCoordinatesArray.clone();
    newArray[index] = newValue;
    return coordinates(newArray);
  }

  @Override
  public String toString() {
    return Strings.format("[Coordinates: %s ]", Joiner.on(' ').join(Arrays.stream(rawCoordinatesArray).iterator()));
  }

}
