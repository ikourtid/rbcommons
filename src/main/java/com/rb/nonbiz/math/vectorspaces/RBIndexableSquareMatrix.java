package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleMatrix2D;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.IndexableDoubleDataStore2D;
import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;
import static com.rb.nonbiz.math.vectorspaces.MatrixRowIndex.matrixRowIndex;
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

  private final RBSquareMatrix rawSquareMatrix;
  private final ArrayIndexMapping<K> mappingForBothRowsAndColumns;

  private RBIndexableSquareMatrix(
      RBSquareMatrix rawSquareMatrix,
      ArrayIndexMapping<K> mappingForBothRowsAndColumns) {
    this.rawSquareMatrix = rawSquareMatrix;
    this.mappingForBothRowsAndColumns = mappingForBothRowsAndColumns;
  }

  public static <K> RBIndexableSquareMatrix<K> rbIndexableSquareMatrix(
      RBSquareMatrix rbSquareMatrix,
      ArrayIndexMapping<K> mappingForBothRowsAndColumns) {
    checkBothSame(
        rbSquareMatrix.getNumRowsOrColumns(),
        mappingForBothRowsAndColumns.size(),
        "# of matrix rows / columns = %s , but # of rows we have a mapping for is %s : %s %s",
        rbSquareMatrix.getNumRowsOrColumns(), mappingForBothRowsAndColumns.size(),
        rbSquareMatrix, mappingForBothRowsAndColumns);
    return new RBIndexableSquareMatrix<>(rbSquareMatrix, mappingForBothRowsAndColumns);
  }

  @Override
  public double getByIndex(int rowIndexAsInt, int columnIndexAsInt) {
    return rawSquareMatrix.get(matrixRowIndex(rowIndexAsInt), matrixColumnIndex(columnIndexAsInt));
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

  public RBSquareMatrix getRawSquareMatrix() {
    return rawSquareMatrix;
  }

  /**
   * This lets you use clearer semantics for this case where you know you're dealing with a square matrix.
   */
  public int getNumRowsOrColumns() {
    return mappingForBothRowsAndColumns.size();
  }

  @Override
  public String toString() {
    return Strings.format("[RBISM %s x %s square matrix; mapping for rows & columns is: %s ; contents: %s RBISM]",
        getNumRowsOrColumns(),
        getNumRowsOrColumns(),
        mappingForBothRowsAndColumns,
        rawSquareMatrix);
  }

}
