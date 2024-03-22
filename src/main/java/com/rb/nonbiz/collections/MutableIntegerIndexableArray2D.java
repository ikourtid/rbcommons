package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.Strings;

import java.util.Arrays;
import java.util.Iterator;

/**
 * See #MutableIndexableArray2D.
 *
 * <p> This is a specialized, more memory-efficient alternative to a {@code MutableIndexableArray2D<Integer>}.
 * It's a bit more efficient because it uses unboxed lowercase-i ints. </p>
 */
public class MutableIntegerIndexableArray2D<R, C> {

  private final int[][] rawArray;
  private final ArrayIndexMapping<R> rowMapping;
  private final ArrayIndexMapping<C> columnMapping;

  private MutableIntegerIndexableArray2D(int[][] rawArray,
                                         ArrayIndexMapping<R> rowMapping, ArrayIndexMapping<C> columnMapping) {
    this.rawArray = rawArray;
    this.rowMapping = rowMapping;
    this.columnMapping = columnMapping;
  }

  public static <R, C> MutableIntegerIndexableArray2D<R, C> mutableIntegerIndexableArray2D(
      int[][] rawArray, ArrayIndexMapping<R> rowMapping, ArrayIndexMapping<C> columnMapping) {
    return new MutableIntegerIndexableArray2D<>(rawArray, rowMapping, columnMapping);
  }

  public int get(R rowKey, C columnKey) {
    return rawArray[rowMapping.getIndexOrThrow(rowKey)][columnMapping.getIndexOrThrow(columnKey)];
  }

  public int getByIndex(int rowIndex, int columnIndex) {
    return rawArray[rowIndex][columnIndex];
  }

  public void set(R rowKey, C columnKey, int value) {
    rawArray[rowMapping.getIndexOrThrow(rowKey)][columnMapping.getIndexOrThrow(columnKey)] = value;
  }

  public R getRowKey(int row) {
    return rowMapping.getKey(row);
  }

  public C getColumnKey(int column) {
    return columnMapping.getKey(column);
  }

  public int getNumRows() {
    return rowMapping.size();
  }

  public int getNumColumns() {
    return columnMapping.size();
  }

  @VisibleForTesting // Don't use this; it helps the matcher code be simpler
  int[][] getRawArrayUnsafe() {
    return rawArray;
  }

  public ArrayIndexMapping<C> getColumnMapping() {
    return this.columnMapping;
  }

  public ArrayIndexMapping<R> getRowMapping() {
    return rowMapping;
  }

  public Iterator<Integer> rowMajorIterator() {
    return new Iterator<Integer>() {
      int row = 0;
      int column = 0;

      @Override
      public boolean hasNext() {
        return row < getNumRows() && column < getNumColumns();
      }

      @Override
      public Integer next() {
        int nextItem = rawArray[row][column];
        column++;
        if (column == getNumColumns()) {
          column = 0;
          row++;
        }
        return nextItem;
      }
    };
  }

  @Override
  public String toString() {
    return Strings.format("%s %s %s", rowMapping, columnMapping, Arrays.deepToString(rawArray));
  }

}
