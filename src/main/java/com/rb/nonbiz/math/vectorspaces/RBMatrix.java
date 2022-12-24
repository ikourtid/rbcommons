package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;
import static com.rb.nonbiz.math.vectorspaces.MatrixRowIndex.matrixRowIndex;
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
        matrixColumnIndex.intValue() < rawMatrix.columns(),
        "Matrix column index %s is not within the range of valid matrix columns 0 to %s",
        matrixColumnIndex, rawMatrix.columns());
    return rbVector(rawMatrix.viewColumn(matrixColumnIndex.intValue()));
  }

  public int getNumRows() {
    return rawMatrix.rows();
  }

  public int getNumColumns() {
    return rawMatrix.columns();
  }

  public MatrixRowIndex getLastRowIndex() {
    return matrixRowIndex(getNumRows() - 1);
  }

  public MatrixColumnIndex getLastColumnIndex() {
    return matrixColumnIndex(getNumColumns() - 1);
  }

  /**
   * Returns all valid row indices, from 0 to the last valid row, as a stream of {@link MatrixRowIndex}.
   */
  public Stream<MatrixRowIndex> matrixRowIndexStream() {
    return IntStream.range(0, getNumRows())
        .mapToObj(i -> matrixRowIndex(i));
  }

  /**
   * Returns all valid column indices, from 0 to the last valid column, as a stream of {@link MatrixColumnIndex}.
   */
  public Stream<MatrixColumnIndex> matrixColumnIndexStream() {
    return IntStream.range(0, getNumColumns())
        .mapToObj(i -> matrixColumnIndex(i));
  }

  public double get(MatrixRowIndex matrixRowIndex, MatrixColumnIndex matrixColumnIndex) {
    return rawMatrix.get(matrixRowIndex.intValue(), matrixColumnIndex.intValue());
  }
  
  /**
   * Apply an arbitrary transform for every element based on its position in the matrix (row & column),
   * and return a copy of this matrix.
   *
   * <p> The first two arguments in the supplied {@link TriFunction} give the position of the matrix element.
   * The third is the value in the original (not the transformed copy) matrix. </p>
   */
  public RBMatrix transformCopy(TriFunction<MatrixRowIndex, MatrixColumnIndex, Double, Double> transformer) {
    DoubleMatrix2D transformedMatrix = DoubleFactory2D.dense.make(getNumRows(), getNumColumns());

    for (int i = 0; i < getNumRows(); i++) {
      for (int j = 0; j < getNumColumns(); j++) {
        transformedMatrix.setQuick(i, j,
            transformer.apply(matrixRowIndex(i), matrixColumnIndex(j), rawMatrix.get(i, j)));
      }
    }
    return rbMatrix(transformedMatrix);
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
    return rbMatrix(new Algebra().mult(rawMatrix, other.rawMatrix));
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
   * Implements the function DoubleMatrix2D, viewPart, from Colt.
   * This returns a new matrix which is a rectangle subset of the current matrix.
   * Note that the colt function itself returns a copy, as of December 2022.
   */
  public RBMatrix copyPart(ClosedRange<MatrixRowIndex> rowRange, ClosedRange<MatrixColumnIndex> columnRange) {
    int firstRow    = rowRange.lowerEndpoint().intValue();
    int lastRow     = rowRange.upperEndpoint().intValue();
    int firstColumn = columnRange.lowerEndpoint().intValue();
    int lastColumn  = columnRange.upperEndpoint().intValue();
    return rbMatrix(rawMatrix.viewPart(
        firstRow, firstColumn, lastRow - firstRow + 1, lastColumn - firstColumn + 1));
  }

  /**
   * Returns a new matrix which is a subset of the current matrix whose top-left item is also (0, 0).
   */
  public RBMatrix copyTopLeftPart(MatrixRowIndex lastRowInclusive, MatrixColumnIndex lastColumnInclusive) {
    return copyPart(
        closedRange(matrixRowIndex(0), lastRowInclusive),
        closedRange(matrixColumnIndex(0), lastColumnInclusive));
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
   * Returns whether or not this matrix is square.
   */
  public boolean isSquare() {
    return getNumRows() == getNumColumns();
  }

  /**
   * If this is a 1 x 1 matrix, returns the only element. Otherwise, throws an exception.
   *
   * <p> The name parallels {@link Iterables#getOnlyElement(Iterable)}, which has similar behavior. </p>
   */
  public double getOnlyElementOrThrow() {
    RBPreconditions.checkArgument(
        getNumRows() == 1 && getNumColumns() == 1,
        "getOnlyElementOrThrow needs a 1x1 matrix, but was %s x %s : %s",
        getNumRows(), getNumColumns(), rawMatrix);
    return rawMatrix.get(0, 0);
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
