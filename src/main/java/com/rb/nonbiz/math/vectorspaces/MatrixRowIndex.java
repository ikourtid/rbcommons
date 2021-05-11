package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * Just a thin typesafe wrapper around an int, which denotes the row index to a matrix.
 */
public class MatrixRowIndex {

  private final int rawIndex;

  private MatrixRowIndex(int rawIndex) {
    this.rawIndex = rawIndex;
  }

  public static MatrixRowIndex matrixRowIndex(int rawIndex) {
    RBPreconditions.checkArgument(
        rawIndex >= 0,
        "A row index cannot be negative at %s",
        rawIndex);
    return new MatrixRowIndex(rawIndex);
  }

  public int asInt() {
    return rawIndex;
  }

  @Override
  public String toString() {
    return Strings.format("[MRI %s MRI]", rawIndex);
  }

}
