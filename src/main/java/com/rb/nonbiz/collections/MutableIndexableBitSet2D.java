package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;

import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

/**
 * See #MutableIndexableArray2D.
 *
 * <p> This is a specialized, more memory-efficient alternative to a {@code MutableIndexableArray2D<Boolean>}.
 * It's a bit more efficient because it uses unboxed lowercase-b booleans. </p>
 */
public class MutableIndexableBitSet2D<R, C> {

  private final BitSet2D rawBitSet;
  private final ArrayIndexMapping<R> rowMapping;
  private final ArrayIndexMapping<C> columnMapping;

  private MutableIndexableBitSet2D(BitSet2D rawBitSet,
                                   ArrayIndexMapping<R> rowMapping, ArrayIndexMapping<C> columnMapping) {
    this.rawBitSet = rawBitSet;
    this.rowMapping = rowMapping;
    this.columnMapping = columnMapping;
  }

  public static <R, C> MutableIndexableBitSet2D<R, C> mutableIndexableBitSet2D(
      BitSet2D rawBitSet, ArrayIndexMapping<R> rowMapping, ArrayIndexMapping<C> columnMapping) {
    return new MutableIndexableBitSet2D(rawBitSet, rowMapping, columnMapping);
  }

  public boolean get(R rowKey, C columnKey) {
    return rawBitSet.get(rowMapping.getIndexOrThrow(rowKey), columnMapping.getIndexOrThrow(columnKey));
  }

  public boolean get(int rowIndex, int columnIndex) {
    return rawBitSet.get(rowIndex, columnIndex);
  }

  /**
   * Like a java BitSet, we use #set to turn on a bit and #clear to turn it off.
   */
  public void set(R rowKey, C columnKey) {
    rawBitSet.set(rowMapping.getIndexOrThrow(rowKey), columnMapping.getIndexOrThrow(columnKey));
  }

  public void setAssumingOff(R rowKey, C columnKey) {
    int rowIndex = rowMapping.getIndexOrThrow(rowKey);
    int columnIndex = columnMapping.getIndexOrThrow(columnKey);
    if (rawBitSet.get(rowIndex, columnIndex)) {
      throw new IllegalArgumentException(smartFormat(
          "You are trying to turn off the bit for ( %s , %s ) aka ( %s , %s ) but it's already on",
          rowKey, columnKey, rowIndex, columnIndex));
    }
    rawBitSet.set(rowIndex, columnIndex);
  }

  /**
   * Like a java BitSet, we use #set to turn on a bit and #clear to turn it off.
   */
  public void clear(R rowKey, C columnKey) {
    rawBitSet.clear(rowMapping.getIndexOrThrow(rowKey), columnMapping.getIndexOrThrow(columnKey));
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

  public int cardinality() {
    return rawBitSet.cardinality();
  }

  @VisibleForTesting // Don't use this; it helps the matcher code be simpler
  public BitSet2D getRawBitSet() {
    return rawBitSet;
  }

  @VisibleForTesting // Don't use this; it helps the matcher code be simpler
  ArrayIndexMapping<R> getRowMapping() {
    return rowMapping;
  }

  @VisibleForTesting // Don't use this; it helps the matcher code be simpler
  ArrayIndexMapping<C> getColumnMapping() {
    return columnMapping;
  }

  @Override
  public String toString() {
    return rawBitSet.toString();
  }

}
