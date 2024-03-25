package com.rb.nonbiz.collections;

import com.google.common.collect.Range;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Collections;
import java.util.Iterator;
import java.util.stream.IntStream;

/**
 * Various static utility methods pertaining to Java arrays.
 */
public class RBArrays {

  /**
   * Remove one element from an array of length N, and return an array of length N - 1.
   */
  public static int[] cutFromArray(int[] original, int indexToRemove) {
    RBPreconditions.checkArgument(
        original.length >= 2,
        "You can only cut an element from an array of size 2+; was %s",
        original.length);
    RBPreconditions.checkValidArrayElement(
        indexToRemove,
        original.length);
    int[] newArray = new int[original.length - 1];
    for (int i = 0; i < indexToRemove; i++) {
      newArray[i] = original[i];
    }
    for (int i = indexToRemove; i < newArray.length; i++) {
      newArray[i] = original[i + 1];
    }
    return newArray;
  }

  /**
   * Insert one element to an array of length N, and return an array of length N + 1.
   */
  public static int[] spliceIntoArray(int[] original, int indexToInsertAt, int newValue) {
    RBPreconditions.checkInRange(
        indexToInsertAt,
        Range.closed(0, original.length));
    int[] newArray = new int[original.length + 1];
    for (int i = 0; i < indexToInsertAt; i++) {
      newArray[i] = original[i];
    }
    newArray[indexToInsertAt] = newValue;
    for (int i = indexToInsertAt + 1; i < newArray.length; i++) {
      newArray[i] = original[i - 1];
    }
    return newArray;
  }

  public static <T> Iterator<T> arrayIterator(T[] array) {
    return IntStream.range(0, array.length).mapToObj(i -> array[i]).iterator();
  }

  public double[] createUnitVectorDoubleArray(int size, int indexOfItemWithValue1) {
    double[] array = new double[size];
    array[indexOfItemWithValue1] = 1;
    return array;
  }

  public static int[] intArrayWithNCopies(int size, int sharedValue) {
    RBPreconditions.checkArgument(size > 0, "The size argument must be greater than zero.");
    return Collections.nCopies(size, sharedValue).stream().mapToInt(i -> i).toArray();
  }

}
