package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.math.vectorspaces.RBMatrix;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;

import static com.rb.nonbiz.collections.RBOptionals.getIntOrThrow;
import static com.rb.nonbiz.math.vectorspaces.RBMatrix.rbMatrix;

/**
 * See {@link MutableIndexableArray2D}.
 *
 * This is a specialized, more memory-efficient alternative to a {@code MutableIndexableArray2D<Double>}.
 * It's a bit more efficient because it uses unboxed lowercase-d doubles.
 */
public class MutableDoubleIndexableArray2D<R, C> implements IndexableDoubleDataStore2D<R, C> {

  private final double[][] rawArray;
  private final ArrayIndexMapping<R> rowMapping;
  private final ArrayIndexMapping<C> columnMapping;

  private MutableDoubleIndexableArray2D(
      double[][] rawArray,
      ArrayIndexMapping<R> rowMapping,
      ArrayIndexMapping<C> columnMapping) {
    this.rawArray = rawArray;
    this.rowMapping = rowMapping;
    this.columnMapping = columnMapping;
  }

  public static <R, C> MutableDoubleIndexableArray2D<R, C> mutableDoubleIndexableArray2D(
      double[][] rawArray,
      ArrayIndexMapping<R> rowMapping,
      ArrayIndexMapping<C> columnMapping) {
    RBPreconditions.checkArgument(
        rawArray.length == rowMapping.size(),
        "Array has %s rows but mapping has %s",
        rawArray.length, rowMapping.size());
    if (rawArray.length == 0) {
      RBPreconditions.checkArgument(
          columnMapping.isEmpty(),
          "An empty array must have 0 column mappings but this one has %s",
          columnMapping.size());
    } else {
      int numColumns = rawArray[0].length;
      RBPreconditions.checkArgument(
          numColumns == columnMapping.size(),
          "A non-empty array with %s columns had %s column mappings",
          numColumns, columnMapping.size());
    }
    return new MutableDoubleIndexableArray2D<>(rawArray, rowMapping, columnMapping);
  }

  @Override
  public double getByIndex(int rowIndex, int columnIndex) {
    return rawArray[rowIndex][columnIndex];
  }

  public void set(R rowKey, C columnKey, double value) {
    rawArray[rowMapping.getIndexOrThrow(rowKey)][columnMapping.getIndexOrThrow(columnKey)] = value;
  }

  public RBMatrix toRBMatrix() {
    return rbMatrix(rawArray);
  }

  @Override
  public ArrayIndexMapping<C> getColumnMapping() {
    return columnMapping;
  }

  @Override
  public ArrayIndexMapping<R> getRowMapping() {
    return rowMapping;
  }

  @VisibleForTesting // Don't use this; it helps the matcher code be simpler
  double[][] getRawArray() {
    return rawArray;
  }

  public Iterator<Double> singleRowIterator(R rowKey) {
    int rowIndex = getIntOrThrow(
        rowMapping.getOptionalIndex(rowKey),
        "Row key %s does not exist in mapping %s",
        rowKey, rowMapping);
    return IntStream.range(0, getNumColumns())
        .mapToDouble(columnIndex -> rawArray[rowIndex][columnIndex])
        .iterator();
  }

  public Iterator<Double> singleColumnIterator(C columnKey) {
    int columnIndex = getIntOrThrow(
        columnMapping.getOptionalIndex(columnKey),
        "Column key %s does not exist in mapping %s",
        columnKey, columnMapping);
    return IntStream.range(0, getNumRows())
        .mapToDouble(rowIndex -> rawArray[rowIndex][columnIndex])
        .iterator();
  }

  public Iterator<Double> rowMajorIterator() {
    return new Iterator<Double>() {
      int row = 0;
      int column = 0;

      @Override
      public boolean hasNext() {
        return row < getNumRows() && column < getNumColumns();
      }

      @Override
      public Double next() {
        double nextItem = rawArray[row][column];
        column++;
        if (column == getNumColumns()) {
          column = 0;
          row++;
        }
        return nextItem;
      }
    };
  }

  public boolean isEmpty() {
    return getRowMapping().isEmpty() && getColumnMapping().isEmpty();
  }

  @Override
  public String toString() {
    return Strings.format("%s %s %s", rowMapping, columnMapping, Arrays.deepToString(rawArray));
  }

}
