package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;

import java.util.Iterator;

import static com.rb.nonbiz.collections.MutableIndexableArray2D.mutableIndexableArray2D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;

/**
 * Just like {@link MutableIndexableArray2D}, except immutable.
 *
 * <p> Note that someone can modify the underlying object if they have a handle to it via getRawArrayUnsafe()
 * but at least they can't modify it through THIS object; there's no set() method here.
 * Plus, getRawArrayUnsafe is package-private, for extra safety. </p>
 */
public class ImmutableIndexableArray2D<R, C, V> {

  private final MutableIndexableArray2D<R, C, V> mutableArray2D;

  private ImmutableIndexableArray2D(MutableIndexableArray2D<R, C, V> mutableArray2D) {
    this.mutableArray2D = mutableArray2D;
  }

  public static <R, C, V> ImmutableIndexableArray2D<R, C, V> immutableIndexableArray2D(
      MutableIndexableArray2D<R, C, V> mutableArray2D) {
    return new ImmutableIndexableArray2D<>(mutableArray2D);
  }

  public static <R, C, V> ImmutableIndexableArray2D<R, C, V> immutableIndexableArray2D(
      V[][] rawArray, ArrayIndexMapping<R> rowMapping, ArrayIndexMapping<C> columnMapping) {
    return immutableIndexableArray2D(mutableIndexableArray2D(
        rawArray, rowMapping, columnMapping));
  }

  public static <R, C, V> ImmutableIndexableArray2D<R, C, V> emptyImmutableIndexableArray2D(V[][] emptyArray) {
    return immutableIndexableArray2D(
        emptyArray,
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping());
  }

  public V get(R rowKey, C columnKey) {
    return mutableArray2D.get(rowKey, columnKey);
  }

  public V getByIndex(int rowIndex, int columnIndex) {
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

  public boolean containsRowAndColumnKeys(R rowKey, C columnKey) {
    return mutableArray2D.containsRowAndColumnKeys(rowKey, columnKey);
  }

  @VisibleForTesting // Don't use this; it helps the matcher code be simpler
  V[][] getRawArrayUnsafe() {
    return mutableArray2D.getRawArrayUnsafe();
  }

  public ArrayIndexMapping<R> getRowMapping() {
    return mutableArray2D.getRowMapping();
  }

  public ArrayIndexMapping<C> getColumnMapping() {
    return mutableArray2D.getColumnMapping();
  }

  @VisibleForTesting // Don't use this; it helps the matcher code be simpler
  MutableIndexableArray2D<R, C, V> getMutableArray2D() {
    return mutableArray2D;
  }

  @Override
  public String toString() {
    return mutableArray2D.toString();
  }

  public Iterator<V> singleRowIterator(R rowKey) {
    return mutableArray2D.singleRowIterator(rowKey);
  }

  public Iterator<V> singleColumnIterator(C columnKey) {
    return mutableArray2D.singleColumnIterator(columnKey);
  }

  public Iterator<V> rowMajorIterator() {
    return mutableArray2D.rowMajorIterator();
  }

}
