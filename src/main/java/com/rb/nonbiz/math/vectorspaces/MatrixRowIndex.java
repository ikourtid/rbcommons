package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.IntegerValue;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * Just a thin typesafe wrapper around an int, which denotes the row index to a matrix.
 */
public class MatrixRowIndex extends IntegerValue<MatrixRowIndex> implements IsArrayIndex {

  private MatrixRowIndex(int rawIndex) {
    super(rawIndex);
  }

  public static MatrixRowIndex matrixRowIndex(int rawIndex) {
    RBPreconditions.checkArgument(
        rawIndex >= 0,
        "A row index cannot be negative at %s",
        rawIndex);
    return new MatrixRowIndex(rawIndex);
  }

  @Override
  public int asInt() {
    return intValue();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return asInt() == ((MatrixRowIndex)o).asInt();
  }

  @Override
  public int hashCode() {
    return asInt();
  }

  @Override
  public String toString() {
    return Strings.format("[MRI %s MRI]", asInt());
  }

}
