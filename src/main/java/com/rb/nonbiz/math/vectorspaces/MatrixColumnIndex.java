package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.IntegerValue;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * Just a thin typesafe wrapper around an int, which denotes the row index to a matrix.
 */
public class MatrixColumnIndex extends IntegerValue<MatrixColumnIndex> implements IsArrayIndex {

  private MatrixColumnIndex(int rawIndex) {
    super(rawIndex);
  }

  public static MatrixColumnIndex matrixColumnIndex(int rawIndex) {
    RBPreconditions.checkArgument(
        rawIndex >= 0,
        "A column index cannot be negative at %s",
        rawIndex);
    return new MatrixColumnIndex(rawIndex);
  }

  @Override
  public String toString() {
    return Strings.format("[MCI %s MCI]", intValue());
  }

}
