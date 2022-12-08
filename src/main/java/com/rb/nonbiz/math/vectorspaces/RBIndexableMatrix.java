package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleMatrix2D;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.IndexableDoubleDataStore2D;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import static com.rb.nonbiz.util.RBSimilarityPreconditions.checkBothSame;

/**
 * A 2d collection of doubles, which can be indexed by a row key and a column key, which can be of different types.
 *
 * <p> It's a bit like a 2-dimensional map where there are two keys, and the values are doubles.
 * The underlying data store is a Colt {@link DoubleMatrix2D}, so this class is particularly useful in case we
 * want to interact with the Colt linear algebra library. Of course, this means that the fact that we use a
 * {@link DoubleMatrix2D} is not hidden by this abstraction - but it's fine; this is intentional here. </p>
 */
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
    RBPreconditions.checkArgument(
        rawMatrix.size() > 0,
        "We do not allow an empty RBIndexableMatrix, just to be safe");
    checkBothSame(
        rawMatrix.rows(),
        rowMapping.size(),
        "# of matrix rows = %s , but # of rows we have a mapping for is %s : %s %s %s",
        rawMatrix.rows(), rowMapping.size(), rowMapping, columnMapping, rawMatrix);
    checkBothSame(
        rawMatrix.columns(),
        columnMapping.size(),
        "# of matrix columns = %s , but # of columns we have a mapping for is %s : %s %s %s",
        rawMatrix.columns(), columnMapping.size(), rowMapping, columnMapping, rawMatrix);
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
