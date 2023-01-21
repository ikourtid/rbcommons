package com.rb.nonbiz.collections;

import cern.colt.matrix.DoubleMatrix2D;
import com.rb.nonbiz.types.Epsilon;

/**
 * A 2-dimensional collection of doubles that's not bound to a particular implementation: implementers of this
 * interface could be using e.g. a 2-d double array, or a Colt (linear algebra library) {@link DoubleMatrix2D}, or
 * anything else.
 */
public interface IndexableDoubleDataStore2D<R, C> {

  ArrayIndexMapping<R> getRowMapping();

  ArrayIndexMapping<C> getColumnMapping();

  double getByIndex(int rowIndex, int columnIndex);

  default double get(R rowKey, C columnKey) {
    return getByIndex(
        getRowMapping().getIndexOrThrow(rowKey),
        getColumnMapping().getIndexOrThrow(columnKey));
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
    // have 0 rows but 1+ columns, or vice versa. However, this default interface method is not the place to have
    // preconditions for that. Since we trust that all our data classes have very restrictive preconditions,
    // we don't have to worry about that here. So we could have also used && instead of || here.
    return getNumRows() == 0 || getNumColumns() == 0;
  }

  default boolean isSymmetric(Epsilon epsilon) {
    if (getNumRows() != getNumColumns()) {
      return false;
    }

    int sharedSize = getNumRows();

    // We usually like to use Streams and fluent code, but this could be a big operation (for a large matrix),
    // plus it's clear enough to look at for loops when iterating over a matrix.
    for (int i = 0; i < sharedSize; i++) {
      for (int j = i + 1; j < sharedSize; j++) {
        double aboveDiagonal = getByIndex(i, j);
        double belowDiagonal = getByIndex(j, i);
        if (!epsilon.valuesAreWithin(aboveDiagonal, belowDiagonal)) {
          return false;
        }
      }
    }
    return true;
  }


}
