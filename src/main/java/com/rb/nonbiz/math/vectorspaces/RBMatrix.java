package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.stream.IntStream;

import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;
import static com.rb.nonbiz.math.vectorspaces.MatrixRowIndex.matrixRowIndex;
import static com.rb.nonbiz.math.vectorspaces.RBIndexableMatrix.rbIndexableMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBVector.rbVector;

/**
 * An immutable wrapper around the Colt library's DoubleMatrix2D,
 * in the sense that this doesn't expose any methods that can result in mutating the object,
 * or even expose the matrix itself, where the caller could mutate it.
 *
 * We don't have 'double' in the name, because we never use floats, and I don't even know if it ever makes sense
 * to have an int. And we certainly don't use complex numbers. So 'double' is implied here.
 *
 * Note that we do not allow an empty matrix, for error-checking purposes, since our use cases do not need that.
 */
public class RBMatrix {

  private final DoubleMatrix2D rawMatrix;

  protected RBMatrix(DoubleMatrix2D rawMatrix) {
    this.rawMatrix = rawMatrix;
  }

  public static RBMatrix rbMatrix(DoubleMatrix2D rawMatrix) {
    RBPreconditions.checkArgument(
        rawMatrix.size() > 0,
        "We do not allow an empty RBMatrix, just to be safe");
    return new RBMatrix(rawMatrix);
  }

  public static RBMatrix rbIdentityMatrix(int n) {
    return rbMatrix(DoubleFactory2D.dense.identity(n));
  }

  public RBVector getColumnVector(MatrixColumnIndex matrixColumnIndex) {
    RBPreconditions.checkArgument(
        matrixColumnIndex.asInt() < rawMatrix.columns(),
        "Matrix column index %s is not within the range of valid matrix columns 0 to %s",
        matrixColumnIndex, rawMatrix.columns());
    return rbVector(rawMatrix.viewColumn(matrixColumnIndex.asInt()));
  }

  public int getNumRows() {
    return rawMatrix.rows();
  }

  public int getNumColumns() {
    return rawMatrix.columns();
  }

  /**
   * Matrix multiplication
   */
  public RBMatrix multiply(RBMatrix other) {
    RBSimilarityPreconditions.checkBothSame(
        getNumColumns(),
        other.getNumRows(),
        "matrix multiplications: nColumns of first matrix %s must match nRows of second matrix %s",
        getNumColumns(), other.getNumRows());
    return rbMatrix(new Algebra().mult(rawMatrix, other.getRawMatrixUnsafe()));
  }

  /**
   * Matrix transposition
   */
  public RBMatrix transpose() {
    return rbMatrix(new Algebra().transpose(rawMatrix));
  }

  /**
   * Matrix inverse
   */
  public RBMatrix inverse() {
    return rbMatrix(new Algebra().inverse(rawMatrix));
  }

  /**
   * Matrix determinant
   */
  double determinant() {
    return new Algebra().det(rawMatrix);
  }

  public <R, C> RBIndexableMatrix<R, C> toIndexableMatrix(
      ArrayIndexMapping<R> rowMapping,
      ArrayIndexMapping<C> columnMapping) {
    return rbIndexableMatrix(rawMatrix, rowMapping, columnMapping);
  }

  /**
   * This is for cases where we only care about having row keys, but no column keys, i.e. the column keys are just
   * numeric indices for the column, starting at 0.
   */
  public <R> RBIndexableMatrix<R, MatrixColumnIndex> toIndexableMatrixWithTrivialColumnMapping(
      ArrayIndexMapping<R> rowMapping) {
    return rbIndexableMatrix(
        rawMatrix,
        rowMapping,
        simpleArrayIndexMapping(
            IntStream.range(0, getNumColumns())
                .mapToObj(i -> matrixColumnIndex(i))
                .iterator()));
  }

  /**
   * This is for cases where we only care about having column keys, but no row keys, i.e. the row keys are just
   * numeric indices for the row, starting at 0.
   */
  public <C> RBIndexableMatrix<MatrixRowIndex, C> toIndexableMatrixWithTrivialRowMapping(
      ArrayIndexMapping<C> columnMapping) {
    return rbIndexableMatrix(
        rawMatrix,
        simpleArrayIndexMapping(
            IntStream.range(0, getNumRows())
                .mapToObj(i -> matrixRowIndex(i))
                .iterator()),
        columnMapping);
  }

  /**
   * This is here to help the test matcher, hence the 'Unsafe' in the name, and the package-private status.
   */
  @VisibleForTesting
  public DoubleMatrix2D getRawMatrixUnsafe() {
    return rawMatrix;
  }

  @Override
  public String toString() {
    return Strings.format("[RBM %s x %s : %s RBM]", getNumRows(), getNumColumns(), rawMatrix);
  }

}
