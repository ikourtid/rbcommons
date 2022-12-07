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

  default boolean isEmpty() {
    // In practice, the classes implementing this interface should never allow situations where we somehow
    // have 0 rows but 1+ columns, or vice versa. However, this is not the place to have preconditions for that.
    // Since we trust that all our data classes have very restrictive preconditions, we don't have to worry about
    // that here.
    return getNumRows() == 0 || getNumColumns() == 0;
  }

}
