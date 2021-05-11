package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.OptionalDouble;

import static com.rb.nonbiz.collections.BitSet2D.bitSet2D;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

/**
 * This is more memory-efficient than a 2D array of {@code OptionalDouble},
 * because the double[][] is more space-efficient than either a Double[][] (boxed double)
 * or a {@code OptionalDouble[][]}, so we save a lot.
 * The BitSet2D is pretty compact, so we don't lose as much there.
 */
public class MutableOptionalDoubleRawArray2D {

  protected final double[][] values;
  private final BitSet2D isPresent;

  protected MutableOptionalDoubleRawArray2D(int numRows, int numColumns) {
    this.values = new double[numRows][numColumns];
    this.isPresent = bitSet2D(numRows, numColumns);
  }

  public static MutableOptionalDoubleRawArray2D mutableOptionalDoubleRawArray2D(int numRows, int numColumns) {
    RBPreconditions.checkArgument(
        numRows > 0 && numColumns > 0,
        "There must be positive # of rows and columns, but we have %s x %s",
        numRows, numColumns);
    return new MutableOptionalDoubleRawArray2D(numRows, numColumns);
  }

  public int getNumRows() {
    return isPresent.getNumRows();
  }

  public int getNumColumns() {
    return isPresent.getNumColumns();
  }

  public OptionalDouble get(int row, int column) {
    checkRowAndColumn(row, column);
    return isPresent.get(row, column)
        ? OptionalDouble.of(values[row][column])
        : OptionalDouble.empty();
  }

  public void set(int row, int column, double value) {
    checkRowAndColumn(row, column);
    values[row][column] = value;
    isPresent.set(row, column);
  }

  public void setOnlyOnce(int row, int column, double value) {
    checkRowAndColumn(row, column);
    values[row][column] = value;
    isPresent.setAssumingOff(row, column);
  }

  public int getNumValuesPresentInRow(int row) {
    return isPresent.rowCardinality(row);
  }

  public int getNumValuesPresentInColumn(int column) {
    return isPresent.columnCardinality(column);
  }

  public int getNumValuesPresent() {
    return isPresent.cardinality();
  }

  public UnitFraction getFractionPresent() {
    return unitFraction(getNumValuesPresent(), getNumRows() * getNumColumns());
  }

  private void checkRowAndColumn(int row, int column) {
    RBPreconditions.checkArgument(
        0 <= row && row < getNumRows() && 0 <= column && column < getNumColumns(),
        "Row, column pair of ( %s , %s ) is not applicable for a %s x %s array",
        row, column, getNumRows(), getNumColumns());
  }

}
