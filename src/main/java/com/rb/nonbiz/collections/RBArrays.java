package com.rb.nonbiz.collections;

import com.google.common.collect.Range;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Iterator;
import java.util.stream.IntStream;

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

}
