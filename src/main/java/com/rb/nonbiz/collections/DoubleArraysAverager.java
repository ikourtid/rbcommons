package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;

import java.util.Iterator;
import java.util.Optional;

public class DoubleArraysAverager {

  public Optional<double[]> calculateAverageArray(Iterator<double[]> arraysIterator,
                                                  boolean ignoreAverageOfSingleItem) {
    if (!arraysIterator.hasNext()) {
      return Optional.empty();
    }
    double[] firstArray = arraysIterator.next();
    if (ignoreAverageOfSingleItem && !arraysIterator.hasNext()) {
      return Optional.empty();
    }
    double[] runningSum = new double[firstArray.length];
    addToLeft(runningSum, firstArray);
    int numArrays = 1;
    while (arraysIterator.hasNext()) {
      addToLeft(runningSum, arraysIterator.next());
      numArrays++;
    }
    for (int i = 0; i < runningSum.length; i++) {
      runningSum[i] /= numArrays;
    }
    // by this point it's the average, not a running sum
    return Optional.of(runningSum);
  }

  private void addToLeft(double[] left, double[] right) {
    if (left.length != right.length) {
      throw new IllegalArgumentException(smartFormat(
          "Not all arrays to be averaged have the same length - e.g. %s vs %s",
          left.length, right.length));
    }
    int sharedLength = left.length;
    if (sharedLength <= 0) {
      throw new IllegalArgumentException("You cannot be averaging empty arrays");
    }
    for (int i = 0; i < sharedLength; i++) {
      left[i] += right[i];
    }
  }

}
