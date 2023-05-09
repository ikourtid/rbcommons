package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.BitSet;

/**
 * This is effectively a space-optimized 2-dimensional boolean array.
 *
 * <p> This isn't truly a 2D bitset, but there's constant-time access for row/column. </p>
 */
public class BitSet2D {

  private final BitSet flatBitSet;
  private final int numRows;
  private final int numColumns;

  public BitSet2D(BitSet flatBitSet, int numRows, int numColumns) {
    this.flatBitSet = flatBitSet;
    this.numRows = numRows;
    this.numColumns = numColumns;
  }

  public static BitSet2D bitSet2D(int numRows, int numColumns) {
    RBPreconditions.checkArgument(
        numRows > 0,
        "You must have a positive number of rows, not %s",
        numRows);
    RBPreconditions.checkArgument(
        numColumns > 0,
        "You must have a positive number of columns, not %s",
        numColumns);
    BitSet flatBitSet = new BitSet(numRows * numColumns);
    return new BitSet2D(flatBitSet, numRows, numColumns);
  }

  public boolean get(int row, int column) {
    return flatBitSet.get(getFlatIndex(row, column));
  }

  public void set(int row, int column) {
    flatBitSet.set(getFlatIndex(row, column));
  }

  public void clear(int row, int column) {
    flatBitSet.clear(getFlatIndex(row, column));
  }

  public void setAssumingOff(int row, int column) {
    RBPreconditions.checkArgument(
        !get(row, column),
        "Bit for position %s , %s should not already have been set to ON",
        row, column);
    set(row, column);
  }

  public int getNumRows() {
    return numRows;
  }

  public int getNumColumns() {
    return numColumns;
  }

  public int rowCardinality(int row) {
    int numBitsTurnedOn = 0;
    for (int column = 0; column < getNumColumns(); column++) {
      if (get(row, column)) {
        numBitsTurnedOn++;
      }
    }
    return numBitsTurnedOn;
  }

  public int columnCardinality(int column) {
    int numBitsTurnedOn = 0;
    for (int row = 0; row < getNumRows(); row++) {
      if (get(row, column)) {
        numBitsTurnedOn++;
      }
    }
    return numBitsTurnedOn;
  }

  public int cardinality() {
    return flatBitSet.cardinality();
  }

  private int getFlatIndex(int row, int column) {
    RBPreconditions.checkArgument(
        0 <= row && row < getNumRows(),
        "Row must be between 0 and %s , inclusive, but was %s",
        getNumRows(), row);
    RBPreconditions.checkArgument(
        0 <= column && column < getNumColumns(),
        "Column must be between 0 and %s , inclusive, but was %s",
        getNumColumns(), column);
    return row * numColumns + column;
  }

  @VisibleForTesting // this is only here to help the test matcher
  BitSet getFlatBitSet() {
    return flatBitSet;
  }

}
