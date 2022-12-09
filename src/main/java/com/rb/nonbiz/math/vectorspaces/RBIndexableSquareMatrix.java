package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleMatrix2D;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.IndexableDoubleDataStore2D;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.util.RBSimilarityPreconditions.checkBothSame;

/**
 * A 2d square (i.e. same number of rows and columns) collection of doubles , which can be indexed by a row key and a
 * column key, and where the key for the i-th row is the same (and, consequently, of the same type) as that of the i-th
 * column.
 *
 * <p> It's a bit like a 2-dimensional map where there are two keys, and the values are doubles.
 * The underlying data store is a Colt {@link DoubleMatrix2D}, so this class is particularly useful in case we
 * want to interact with the Colt linear algebra library. Of course, this means that the fact that we use a
 * {@link DoubleMatrix2D} is not hidden by this abstraction - but it's fine; this is intentional here. </p>
 */
public class RBIndexableSquareMatrix<K> implements IndexableDoubleDataStore2D<K, K> {

  private final DoubleMatrix2D rawMatrix;
  private final ArrayIndexMapping<K> mappingForBothRowsAndColumns;

  private RBIndexableSquareMatrix(
      DoubleMatrix2D rawMatrix,
      ArrayIndexMapping<K> mappingForBothRowsAndColumns) {
    this.rawMatrix = rawMatrix;
    this.mappingForBothRowsAndColumns = mappingForBothRowsAndColumns;
  }

  public static <K> RBIndexableSquareMatrix<K> rbIndexableSquareMatrix(
      DoubleMatrix2D rawMatrix,
      ArrayIndexMapping<K> mappingForBothRowsAndColumns) {
    RBPreconditions.checkArgument(
        rawMatrix.size() > 0,
        "We do not allow an empty RBIndexableMatrix, just to be safe");
    int numRowsOrColumns = checkBothSame(
        rawMatrix.rows(),
        rawMatrix.columns(),
        "Not a square matrix: %s %s",
        rawMatrix, mappingForBothRowsAndColumns);
    checkBothSame(
        numRowsOrColumns,
        mappingForBothRowsAndColumns.size(),
        "# of matrix rows / columns = %s , but # of rows we have a mapping for is %s : %s %s",
        numRowsOrColumns, mappingForBothRowsAndColumns.size(), rawMatrix, mappingForBothRowsAndColumns);
    return new RBIndexableSquareMatrix<>(rawMatrix, mappingForBothRowsAndColumns);
  }

  @Override
  public double getByIndex(int rowIndex, int columnIndex) {
    return rawMatrix.get(rowIndex, columnIndex);
  }

  @Override
  public ArrayIndexMapping<K> getRowMapping() {
    return mappingForBothRowsAndColumns;
  }

  @Override
  public ArrayIndexMapping<K> getColumnMapping() {
    return mappingForBothRowsAndColumns;
  }

  /**
   * This lets you use clearer semantics for this case where you know you're dealing with a square matrix.
   */
  public ArrayIndexMapping<K> getMappingForBothRowsAndColumns() {
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
