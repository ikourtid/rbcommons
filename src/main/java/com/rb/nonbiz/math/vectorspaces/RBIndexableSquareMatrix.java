package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleMatrix2D;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.IndexableDoubleDataStore2D;
import com.rb.nonbiz.text.Strings;

/**
 * A 2d collection of doubles, which can be indexed by a row key and a column key, which must be of the same type,
 * and where the number of rows is the same as the number of columns.
 *
 * <p> It's a bit like a 2-dimensional map where there are two keys, and the values are doubles.
 * The underlying data store is a Colt {@link DoubleMatrix2D}, so this class is particularly useful in case we
 * want to interact with the Colt linear algebra library. Of course, this means that the fact that we use a
 * {@link DoubleMatrix2D} is not hidden by this abstraction - but it's fine; this is intentional here. </p>
 */
public class RBIndexableSquareMatrix<T> implements IndexableDoubleDataStore2D<T, T> {

  private final DoubleMatrix2D rawMatrix;
  private final ArrayIndexMapping<T> mappingForBothRowsAndColumns;

  private RBIndexableSquareMatrix(
      DoubleMatrix2D rawMatrix,
      ArrayIndexMapping<T> mappingForBothRowsAndColumns) {
    this.rawMatrix = rawMatrix;
    this.mappingForBothRowsAndColumns = mappingForBothRowsAndColumns;
  }

  public static <T> RBIndexableSquareMatrix<T> rbIndexableSquareMatrix(
      DoubleMatrix2D rawMatrix,
      ArrayIndexMapping<T> mappingForBothRowsAndColumns) {
    // FIXME IAK MATRIX add preconditions
    return new RBIndexableSquareMatrix<>(rawMatrix, mappingForBothRowsAndColumns);
  }

  @Override
  public double getByIndex(int rowIndex, int columnIndex) {
    return rawMatrix.get(rowIndex, columnIndex);
  }

  @Override
  public ArrayIndexMapping<T> getRowMapping() {
    return mappingForBothRowsAndColumns;
  }

  @Override
  public ArrayIndexMapping<T> getColumnMapping() {
    return mappingForBothRowsAndColumns;
  }

  /**
   * This lets you use clearer semantics for this case where you know you're dealing with a square matrix.
   */
  public ArrayIndexMapping<T> getMappingForBothRowsAndColumns() {
    return mappingForBothRowsAndColumns;
  }

  /**
   * This lets you use clearer semantics for this case where you know you're dealing with a square matrix.
   */
  public int getNumRowsOrColumns() {
    return mappingForBothRowsAndColumns.size();
  }

  /**
   * The whole point of this class is for its callers to be able to convert it to a {@link DoubleMatrix2D} when the
   * need arises, such as when linear algebra functionality from the Colt package needs to be called. However,
   * ideally you can do most operations (like iterate over its contents) without having to be exposed to the fact that
   * the underlying data class is a Colt {@link DoubleMatrix2D}.
   *
   * <p> The method name has 'unsafe' so it's clear to the caller that this returns a mutable object, which in our
   * codebase is heavily discouraged. However, we can't control what Colt does. </p>
   */
  public DoubleMatrix2D getRawMatrixUnsafe() {
    return rawMatrix;
  }

  @Override
  public String toString() {
    return Strings.format("[RBISM %s x %s square matrix; mapping for rows & columns is: %s ; contents: %s RBISM]",
        getNumRowsOrColumns(),
        getNumRowsOrColumns(),
        mappingForBothRowsAndColumns,
        rawMatrix);
  }

}
