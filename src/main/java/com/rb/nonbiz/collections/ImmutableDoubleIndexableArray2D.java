package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.functional.TriFunction;

import java.util.Iterator;

import static com.rb.nonbiz.collections.ImmutableIndexableArray2D.immutableIndexableArray2D;
import static com.rb.nonbiz.collections.MutableDoubleIndexableArray2D.mutableDoubleIndexableArray2D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;

/**
 * Just like {@link MutableDoubleIndexableArray2D}, except this is immutable.
 *
 * <p> Note that someone can modify the underlying object if they have a handle to it via getRawArrayUnsafe()
 * but at least they can't modify it through THIS object; there's no set() method here. </p>
 */
public class ImmutableDoubleIndexableArray2D<R, C> {

  private final MutableDoubleIndexableArray2D<R, C> mutableArray2D;

  private ImmutableDoubleIndexableArray2D(MutableDoubleIndexableArray2D<R, C> mutableArray2D) {
    this.mutableArray2D = mutableArray2D;
  }

  public static <R, C> ImmutableDoubleIndexableArray2D<R, C> immutableDoubleIndexableArray2D(
      MutableDoubleIndexableArray2D<R, C> mutableArray2D) {
    return new ImmutableDoubleIndexableArray2D<>(mutableArray2D);
  }

  public static <R, C> ImmutableDoubleIndexableArray2D<R, C> immutableDoubleIndexableArray2D(
      double[][] rawArray, ArrayIndexMapping<R> rowMapping, ArrayIndexMapping<C> columnMapping) {
    return immutableDoubleIndexableArray2D(mutableDoubleIndexableArray2D(
        rawArray, rowMapping, columnMapping));
  }

  public static <R, C> ImmutableDoubleIndexableArray2D<R, C> emptyImmutableDoubleIndexableArray2D() {
    return immutableDoubleIndexableArray2D(
        new double[][] { },
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping());
  }

  public double get(R rowKey, C columnKey) {
    return mutableArray2D.get(rowKey, columnKey);
  }

  public double getByIndex(int rowIndex, int columnIndex) {
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

  /** As should be obvious by the name, only use this if you know what you're doing.
   * We're breaking an abstraction here in order to improve performance in some cases.
   */
  public double[][] getRawArrayUnsafe() {
    return mutableArray2D.getRawArrayUnsafe();
  }

  public ArrayIndexMapping<R> getRowMapping() {
    return mutableArray2D.getRowMapping();
  }

  public ArrayIndexMapping<C> getColumnMapping() {
    return mutableArray2D.getColumnMapping();
  }

  @VisibleForTesting // Don't use this; it helps the matcher code be simpler
  MutableDoubleIndexableArray2D<R, C> getMutableArray2D() {
    return mutableArray2D;
  }

  @SuppressWarnings("unchecked")
  public <V> ImmutableIndexableArray2D<R, C, V> transform(
      TriFunction<R, C, Double, V> transformer) {
    ArrayIndexMapping<R> rowKeysMapping = mutableArray2D.getRowMapping();
    ArrayIndexMapping<C> columnKeysMapping = mutableArray2D.getColumnMapping();
    // Unfortunately I haven't found a good alternative to instantiating a 2d array.
    V[][] newRawArray = (V[][]) new Object[rowKeysMapping.size()][columnKeysMapping.size()];
    for (int r = 0; r < rowKeysMapping.size(); r++) {
      R rowKey = rowKeysMapping.getKey(r);
      for (int c = 0; c < columnKeysMapping.size(); c++) {
        C columnKey = columnKeysMapping.getKey(c);
        newRawArray[r][c] = transformer.apply(rowKey, columnKey, mutableArray2D.getByIndex(r, c));
      }
    }
    return immutableIndexableArray2D(newRawArray, rowKeysMapping, columnKeysMapping);
  }

  @Override
  public String toString() {
    return mutableArray2D.toString();
  }

  public Iterator<Double> singleRowIterator(R rowKey) {
    return mutableArray2D.singleRowIterator(rowKey);
  }

  public Iterator<Double> singleColumnIterator(C columnKey) {
    return mutableArray2D.singleColumnIterator(columnKey);
  }

  public Iterator<Double> rowMajorIterator() {
    return mutableArray2D.rowMajorIterator();
  }

  public boolean isEmpty() {
    return mutableArray2D.isEmpty();
  }

}
