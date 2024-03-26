package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

/**
 * An immutable square matrix.
 *
 * <p> We went back-and-forth between using inheritance and object composition, and settled for the latter.
 * Even though this is clearly an "is a" relationship, composition is useful because it saves us from having to
 * duplicate some preconditions. Plus, it hides some methods:
 * {@link RBMatrix#getNumRows()} and {@link RBMatrix#getNumColumns()} were replaced with the
 * more explicit {@link #getNumRowsOrColumns()}. </p>
 *
 * @see RBMatrix
 */
public class RBSquareMatrix extends RBMatrix {

  private final int numRowsOrColumns;

  private RBSquareMatrix(DoubleMatrix2D rawMatrix, int numRowsOrColumns) {
    super(rawMatrix);
    this.numRowsOrColumns = numRowsOrColumns;
  }

  public static RBSquareMatrix rbSquareMatrix(double[][] doubleMatrixArray) {
    DoubleMatrix2D rawMatrix = DoubleFactory2D.dense.make(doubleMatrixArray);
    RBPreconditions.checkArgument(
        rawMatrix.size() > 0,
        "We do not allow an empty RBSquareMatrix, just to be safe");
    int numRowsOrColumns = RBSimilarityPreconditions.checkBothSame(
        rawMatrix.rows(),
        rawMatrix.columns(),
        "In a square matrix, we have %s rows but %s columns",
        rawMatrix.rows(), rawMatrix.columns());
    return new RBSquareMatrix(rawMatrix, numRowsOrColumns);
  }

  public static RBSquareMatrix identityRBSquareMatrix(int n) {
    RBPreconditions.checkArgument(
        n > 0,
        "We do not allow an empty RBSquareMatrix, just to be safe");
    return new RBSquareMatrix(DoubleFactory2D.dense.identity(n), n);
  }

  public static RBSquareMatrix diagonalRBSquareMatrix(RBVector rbVector) {
    // Note that RBVector can't be empty, so we don't have to check for that.
    return new RBSquareMatrix(
        DoubleFactory2D.dense.diagonal(new DenseDoubleMatrix1D(rbVector.toArray())),
        rbVector.size());
  }

  /**
   * An n x n matrix is officially called a square matrix of order n.
   * However, because the word 'order' can mean ordering, or (OK, that's a stretch) even buy/sell order,
   * let's just have a clearer name, even if it's not official.
   */
  public int getNumRowsOrColumns() {
    return numRowsOrColumns;
  }

  @Override
  public String toString() {
    return Strings.format("[RBSM %s RBSM]", super.toString());
  }

}
