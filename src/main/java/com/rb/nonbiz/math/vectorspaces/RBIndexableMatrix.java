package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleMatrix2D;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.IndexableDoubleDataStore2D;
import com.rb.nonbiz.text.Strings;

public class RBIndexableMatrix<R, C> implements IndexableDoubleDataStore2D<R, C> {

  private final DoubleMatrix2D rawMatrix;
  private final ArrayIndexMapping<R> rowMapping;
  private final ArrayIndexMapping<C> columnMapping;

  private RBIndexableMatrix(
      DoubleMatrix2D rawMatrix,
      ArrayIndexMapping<R> rowMapping,
      ArrayIndexMapping<C> columnMapping) {
    this.rawMatrix = rawMatrix;
    this.rowMapping = rowMapping;
    this.columnMapping = columnMapping;
  }

  public static <R, C> RBIndexableMatrix<R, C> rbIndexableMatrix(
      DoubleMatrix2D rawMatrix,
      ArrayIndexMapping<R> rowMapping,
      ArrayIndexMapping<C> columnMapping) {
    // FIXME IAK MATRIX add preconditions
    return new RBIndexableMatrix<>(rawMatrix, rowMapping, columnMapping);
  }

  @Override
  public double getByIndex(int rowIndex, int columnIndex) {
    return rawMatrix.get(rowIndex, columnIndex);
  }

  @Override
  public ArrayIndexMapping<R> getRowMapping() {
    return rowMapping;
  }

  @Override
  public ArrayIndexMapping<C> getColumnMapping() {
    return columnMapping;
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
    return Strings.format("[RBIM matrix with %s rows= %s and %s columns = %s: %s RBIM]",
        getNumRows(),
        rowMapping,
        getNumColumns(),
        getColumnMapping(),
        rawMatrix);
  }

}
