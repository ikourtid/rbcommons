package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * Just a thin typesafe wrapper around an int, which denotes the row index to a matrix.
 */
public class MatrixColumnIndex implements IsArrayIndex {

  private final int rawIndex;

  private MatrixColumnIndex(int rawIndex) {
    this.rawIndex = rawIndex;
  }

  public static MatrixColumnIndex matrixColumnIndex(int rawIndex) {
    RBPreconditions.checkArgument(
        rawIndex >= 0,
        "A column index cannot be negative at %s",
        rawIndex);
    return new MatrixColumnIndex(rawIndex);
  }

  @Override
  public int asInt() {
    return rawIndex;
  }

  @Override
  public String toString() {
    return Strings.format("[MCI %s MCI]", rawIndex);
  }

}
