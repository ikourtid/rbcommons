package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import static com.rb.nonbiz.math.vectorspaces.RBIndexableMatrix.rbIndexableMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBIndexableMatrix.rbIndexableMatrixWithTrivialColumnMapping;
import static com.rb.nonbiz.math.vectorspaces.RBIndexableMatrix.rbIndexableMatrixWithTrivialRowMapping;
import static com.rb.nonbiz.math.vectorspaces.RBVector.rbVector;

/**
 * An immutable wrapper around the Colt library's DoubleMatrix2D,
 * in the sense that this doesn't expose any methods that can result in mutating the object,
 * or even expose the matrix itself, where the caller could mutate it.
 *
 * <p> We don't have 'double' in the name, because we never use floats, and I don't even know if it ever makes sense
 * to have an int. And we certainly don't use complex numbers. So 'double' is implied here. </p>
 *
 * <p> Note that we do not allow an empty matrix, for error-checking purposes, since our use cases do not need that. </p>
 *
 * @see RBSquareMatrix
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

  public static RBMatrix rbDiagonalMatrix(RBVector rbVector) {
    return rbMatrix(DoubleFactory2D.dense.diagonal(
        rbVector.getRawDoubleMatrix1DUnsafe()));
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
   * Matrix inverse. This simply calls the Colt matrix inverse() function.
   *
   * <p> This will throw an exception for singular matrices, as it should; they don't have inverses. </p>
   *
   * <p> Warning: this will fail silently for nearly-singular matrices. That is, it will produce
   * an inverse matrix consisting of large almost-balancing positive and negative elements, but
   * the entries will depend very sensitively on the input. </p>
   *
   * <p> What you probably want in this situation is to use something like
   * singular value decomposition (SVD) to get a more robust estimate of the inverse. </p>
   *
   * <p> Before relying on this inverse, it would be wise to check the "condition number" of the matrix.
   * A condition number much greater than 1.0 indicates near-singularity. Conversely, rotation and
   * permutation matrices have condition numbers of 1.0. </p>
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
    return rbIndexableMatrixWithTrivialColumnMapping(rawMatrix, rowMapping);
  }

  /**
   * This is for cases where we only care about having column keys, but no row keys, i.e. the row keys are just
   * numeric indices for the row, starting at 0.
   */
  public <C> RBIndexableMatrix<MatrixRowIndex, C> toIndexableMatrixWithTrivialRowMapping(
      ArrayIndexMapping<C> columnMapping) {
    return rbIndexableMatrixWithTrivialRowMapping(rawMatrix, columnMapping);
  }

  /**
   * Returns whether this matrix is square or not.
   */
  public boolean isSquare() {
    return getNumRows() == getNumColumns();
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
