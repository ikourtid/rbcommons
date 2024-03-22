package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;

import static com.rb.nonbiz.collections.RBOptionals.getIntOrThrow;

/**
 * An indexable array is like a regular array, except that you can also access it based on some
 * more meaningful key - not just an integer index.
 *
 * <p> Why not use a map instead? This is a performance optimization for cases where 3rd party libraries
 * operate using arrays. This way, we won't have to convert back and forth from arrays (needed by the 3rd party library)
 * to our own implementation (e.g. maps of arrays, maps of maps, etc.) </p>
 *
 * <p> Also, if you know you're dealing with square data (all row/column combinations valid)
 * there's only 2 mappings that are stored. Contrast with storing an {@code RBMap<R, RBMap<C, Double>>}
 * where you have O(n) mappings separately stored in the N different submaps.
 * Finally, an IndexableArray2D makes the 'square data' semantics explicit,
 * unlike a map of maps which could be jagged. </p>
 *
 * <p> {@link MutableIndexableArray2D} is a general implementation that works on objects.
 * There are several specialized implementations for primitives; e.g. see {@link MutableDoubleIndexableArray2D}. </p>
 */
public class MutableIndexableArray2D<R, C, V> {

  private final V[][] rawArray;
  private final ArrayIndexMapping<R> rowMapping;
  private final ArrayIndexMapping<C> columnMapping;

  private MutableIndexableArray2D(V[][] rawArray,
                                  ArrayIndexMapping<R> rowMapping, ArrayIndexMapping<C> columnMapping) {
    this.rawArray = rawArray;
    this.rowMapping = rowMapping;
    this.columnMapping = columnMapping;
  }

  public static <R, C, V> MutableIndexableArray2D<R, C, V> mutableIndexableArray2D(
      V[][] rawArray, ArrayIndexMapping<R> rowMapping, ArrayIndexMapping<C> columnMapping) {
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
    return new MutableIndexableArray2D<>(rawArray, rowMapping, columnMapping);
  }

  public V get(R rowKey, C columnKey) {
    return rawArray[rowMapping.getIndexOrThrow(rowKey)][columnMapping.getIndexOrThrow(columnKey)];
  }

  public V getByIndex(int rowIndex, int columnIndex) {
    return rawArray[rowIndex][columnIndex];
  }

  public void set(R rowKey, C columnKey, V value) {
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

  public boolean containsRowAndColumnKeys(R rowKey, C columnKey) {
    return rowMapping.getOptionalIndex(rowKey).isPresent()
        && columnMapping.getOptionalIndex(columnKey).isPresent();
  }

  @VisibleForTesting // this makes the matcher easier
  V[][] getRawArrayUnsafe() {
    return rawArray;
  }

  public ArrayIndexMapping<C> getColumnMapping() {
    return this.columnMapping;
  }

  public ArrayIndexMapping<R> getRowMapping() {
    return rowMapping;
  }

  @VisibleForTesting // Don't use this; it helps the matcher code be simpler
  V[][] getRawArray() {
    return rawArray;
  }

  public Iterator<V> singleRowIterator(R rowKey) {
    int rowIndex = getIntOrThrow(
        rowMapping.getOptionalIndex(rowKey),
        "Row key %s does not exist in mapping %s",
        rowKey, rowMapping);
    return IntStream.range(0, getNumColumns())
        .mapToObj(columnIndex -> rawArray[rowIndex][columnIndex])
        .iterator();
  }

  public Iterator<V> singleColumnIterator(C columnKey) {
    int columnIndex = getIntOrThrow(
        columnMapping.getOptionalIndex(columnKey),
        "Column key %s does not exist in mapping %s",
        columnKey, columnMapping);
    return Iterators.transform(
        IntStream.range(0, getNumRows()).iterator(),
        rowIndex -> rawArray[rowIndex][columnIndex]);
  }

  public Iterator<V> rowMajorIterator() {
    return new Iterator<V>() {
      int row = 0;
      int column = 0;

      @Override
      public boolean hasNext() {
        return row < getNumRows() && column < getNumColumns();
      }

      @Override
      public V next() {
        V nextItem = rawArray[row][column];
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
