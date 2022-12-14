package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.linalg.Algebra;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import static com.rb.nonbiz.math.vectorspaces.RBMatrix.rbIdentityMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrix.rbMatrix;

/**
 * An immutable square matrix.
 *
 * <p> I went back-and-forth between using inheritance and object composition, and settled for the latter.
 * Even though this is clearly an "is a" relationship, composition is useful because it saves us from having to
 * duplicate some preconditions. Plus, it hides some methods: getNumRows and getNumColumns were replaced with the
 * more explicit #getNumRowsOrColumns. </p>
 *
 * @see RBMatrix
 */
public class RBSquareMatrix {

  private final RBMatrix rawMatrix;
  private final int numRowsOrColumns;

  private RBSquareMatrix(RBMatrix rawMatrix, int numRowsOrColumns) {
    this.rawMatrix = rawMatrix;
    this.numRowsOrColumns = numRowsOrColumns;
  }

  public static RBSquareMatrix rbSquareMatrix(RBMatrix rawMatrix) {
    int numRowsOrColumns = RBSimilarityPreconditions.checkBothSame(
        rawMatrix.getNumRows(),
        rawMatrix.getNumColumns(),
        "In a square matrix, we have %s rows but %s columns",
        rawMatrix.getNumRows(), rawMatrix.getNumColumns());
    return new RBSquareMatrix(rawMatrix, numRowsOrColumns);
  }

  public static RBSquareMatrix rbIdentitySquareMatrix(int n) {
    return rbSquareMatrix(rbIdentityMatrix(n));
  }

  public static RBSquareMatrix rbDiagonalSquareMatrix(RBVector rbVector) {
    return rbSquareMatrix(rbMatrix(DoubleFactory2D.dense.diagonal(
        rbVector.getRawDoubleMatrix1DUnsafe())));
  }

  public RBVector getColumnVector(MatrixColumnIndex matrixColumnIndex) {
    return rawMatrix.getColumnVector(matrixColumnIndex);
  }

  /**
   * An n x n matrix is officially called a square matrix of order n.
   * However, because the word 'order' can mean ordering, or (OK, that's a stretch) even buy/sell order,
   * let's just have a clearer name, even if it's not official.
   */
  public int getNumRowsOrColumns() {
    return numRowsOrColumns;
  }

  /**
   * The name has 'calculate' so it's clear to the caller that the result isn't cached.
   */
  public double calculateDeterminant() {
    // You never see new() in the code, really; with verb classes, we use injection, and with data classes,
    // we use static constructors. However, in this case, new Algebra() is a Colt library way of doing things.
    // We can't inject one here (it's a data class), but it's also OK to instantiate it, because doing so is very
    // lightweight (I checked in the decompiler).
    return new Algebra().det(rawMatrix.getRawMatrixUnsafe());
  }

  /**
   * This is here to help the test matcher, hence the 'Unsafe' in the name, and the package-private status.
   */
  RBMatrix getRawMatrixUnsafe() {
    return rawMatrix;
  }

  @Override
  public String toString() {
    return Strings.format("[RBSM %s RBSM]", rawMatrix);
  }

}
