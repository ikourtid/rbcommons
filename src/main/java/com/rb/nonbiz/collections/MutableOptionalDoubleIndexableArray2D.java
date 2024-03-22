package com.rb.nonbiz.collections;

import com.rb.nonbiz.util.RBPreconditions;

import java.util.OptionalDouble;

import static com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2D.immutableDoubleIndexableArray2D;
import static com.rb.nonbiz.collections.MutableDoubleIndexableArray2D.mutableDoubleIndexableArray2D;

/**
 * See {@link MutableIndexableArray2D}.
 *
 * <p> This is a specialized, more memory-efficient alternative to a {@code MutableIndexableArray2D<OptionalDouble>}.
 * See {@link MutableOptionalDoubleRawArray2D} about why. </p>
 */
public class MutableOptionalDoubleIndexableArray2D<R, C> extends MutableOptionalDoubleRawArray2D {

  private final ArrayIndexMapping<R> rowMapping;
  private final ArrayIndexMapping<C> columnMapping;

  private MutableOptionalDoubleIndexableArray2D(ArrayIndexMapping<R> rowMapping, ArrayIndexMapping<C> columnMapping) {
    super(rowMapping.size(), columnMapping.size());
    this.rowMapping = rowMapping;
    this.columnMapping = columnMapping;
  }

  public static <R, C> MutableOptionalDoubleIndexableArray2D<R, C> mutableOptionalDoubleIndexableArray2D(
      ArrayIndexMapping<R> rowMapping, ArrayIndexMapping<C> columnMapping) {
    return new MutableOptionalDoubleIndexableArray2D<>(rowMapping, columnMapping);
  }

  public OptionalDouble get(R rowKey, C columnKey) {
    return get(rowMapping.getIndexOrThrow(rowKey), columnMapping.getIndexOrThrow(columnKey));
  }

  public void set(R rowKey, C columnKey, double value) {
    set(rowMapping.getIndexOrThrow(rowKey), columnMapping.getIndexOrThrow(columnKey), value);
  }

  public void setOnlyOnce(R rowKey, C columnKey, double value) {
    setOnlyOnce(rowMapping.getIndexOrThrow(rowKey), columnMapping.getIndexOrThrow(columnKey), value);
  }

  public int numValuesPresentInRow(R rowKey) {
    return getNumValuesPresentInRow(rowMapping.getIndexOrThrow(rowKey));
  }

  public int numValuesPresentInColumn(C columnKey) {
    return getNumValuesPresentInColumn(columnMapping.getIndexOrThrow(columnKey));
  }

  public R getRowKey(int row) {
    return rowMapping.getKey(row);
  }

  public C getColumnKey(int column) {
    return columnMapping.getKey(column);
  }

  /**
   * Note that this does not make a copy of the 2D array. That's intentional, for performance reasons.
   * So tread carefully.
   */
  public ImmutableDoubleIndexableArray2D<R, C> toUnmodifiableDoubleIndexedArray2D() {
    RBPreconditions.checkArgument(
        getNumValuesPresent() == getNumRows() * getNumColumns(),
        "Can't convert to plain double (not optional) array; only %s of the total %s values in the %s x %s are there",
        getNumValuesPresent(), getNumRows() * getNumColumns(), getNumRows(), getNumColumns());
    return toUnmodifiableDoubleIndexedArray2DIgnoringEmpties();
  }

  /**
   * Note that this does not make a copy of the 2D array. That's intentional, for performance reasons.
   * So tread carefully.
   */
  public ImmutableDoubleIndexableArray2D<R, C> toUnmodifiableDoubleIndexedArray2DIgnoringEmpties() {
    return immutableDoubleIndexableArray2D(
        mutableDoubleIndexableArray2D(values, rowMapping, columnMapping));
  }

}
