package com.rb.nonbiz.collections;

public interface IndexableDoubleDataStore2D<R, C> {

  ArrayIndexMapping<R> getRowMapping();

  ArrayIndexMapping<C> getColumnMapping();

  double getByIndex(int rowIndex, int columnIndex);

  default double get(R rowKey, C columnKey) {
    return getByIndex(
        getRowMapping().getIndex(rowKey),
        getColumnMapping().getIndex(columnKey));
  }

  default R getRowKey(int rowIndex) {
    return getRowMapping().getKey(rowIndex);
  }

  default C getColumnKey(int columnIndex) {
    return getColumnMapping().getKey(columnIndex);
  }

  default int getNumRows() {
    return getRowMapping().size();
  }

  default int getNumColumns() {
    return getColumnMapping().size();
  }

}
