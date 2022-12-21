package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * Just a thin typesafe wrapper around an int, which denotes the row index to a matrix.
 */
public class MatrixColumnIndex implements IsArrayIndex, Comparable<MatrixColumnIndex> {

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
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return rawIndex == ((MatrixColumnIndex)o).rawIndex;
  }

  @Override
  public int hashCode() {
    return rawIndex;
  }

  @Override
  public String toString() {
    return Strings.format("[MCI %s MCI]", rawIndex);
  }

  @Override
  public int compareTo(MatrixColumnIndex o) {
    return Integer.valueOf(rawIndex).compareTo(o.rawIndex);
  }
}
