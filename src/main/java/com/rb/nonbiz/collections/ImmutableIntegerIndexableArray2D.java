package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;

import java.util.Iterator;

import static com.rb.nonbiz.collections.MutableIntegerIndexableArray2D.mutableIntegerIndexableArray2D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;

/**
 * See ImmutableIntegerIndexableArray2D for more.
 *
 * Note that someone can modify the underlying object if they have a handle to it via getRawArrayUnsafe()
 * but at least they can't modify it through THIS object; there's no set() method here.
 * Plus, getRawArrayUnsafe is package-private, for extra safety.
 */
public class ImmutableIntegerIndexableArray2D<R, C> {

  private final MutableIntegerIndexableArray2D<R, C> mutableArray2D;

  private ImmutableIntegerIndexableArray2D(MutableIntegerIndexableArray2D<R, C> mutableArray2D) {
    this.mutableArray2D = mutableArray2D;
  }

  public static <R, C> ImmutableIntegerIndexableArray2D<R, C> immutableIntegerIndexableArray2D(
      MutableIntegerIndexableArray2D<R, C> mutableArray2D) {
    return new ImmutableIntegerIndexableArray2D(mutableArray2D);
  }

  public static <R, C> ImmutableIntegerIndexableArray2D<R, C> immutableIntegerIndexableArray2D(
      int[][] rawArray, ArrayIndexMapping<R> rowMapping, ArrayIndexMapping<C> columnMapping) {
    return immutableIntegerIndexableArray2D(mutableIntegerIndexableArray2D(
        rawArray, rowMapping, columnMapping));
  }

  public static <R, C> ImmutableIntegerIndexableArray2D<R, C> emptyImmutableIntegerIndexableArray2D() {
    return immutableIntegerIndexableArray2D(
        new int[][] { },
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping());
  }

  public int get(R rowKey, C columnKey) {
    return mutableArray2D.get(rowKey, columnKey);
  }

  public int getByIndex(int rowIndex, int columnIndex) {
    return mutableArray2D.getByIndex(rowIndex, columnIndex);
  }

  public R getRowKey(int row) {
    return mutableArray2D.getRowKey(row);
  }

  public C getColumnKey(int column) {
    return mutableArray2D.getColumnKey(column);
  }

  public int getNumRows() {
    return mutableArray2D.getNumRows();
  }

  public int getNumColumns() {
    return mutableArray2D.getNumColumns();
  }

  @VisibleForTesting // Don't use this; it helps the matcher code be simpler
  int[][] getRawArrayUnsafe() {
    return mutableArray2D.getRawArrayUnsafe();
  }

  public ArrayIndexMapping<R> getRowMapping() {
    return mutableArray2D.getRowMapping();
  }

  public ArrayIndexMapping<C> getColumnMapping() {
    return mutableArray2D.getColumnMapping();
  }

  @VisibleForTesting // Don't use this; it helps the matcher code be simpler
  MutableIntegerIndexableArray2D<R, C> getMutableArray2D() {
    return mutableArray2D;
  }

  @Override
  public String toString() {
    return mutableArray2D.toString();
  }

  public Iterator<Integer> rowMajorIterator() {
    return mutableArray2D.rowMajorIterator();
  }

}
