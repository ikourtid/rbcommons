package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.IndexableDoubleDataStore2D;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.stream.IntStream;

import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;
import static com.rb.nonbiz.math.vectorspaces.MatrixRowIndex.matrixRowIndex;
import static com.rb.nonbiz.math.vectorspaces.RBMatrix.rbMatrix;
import static com.rb.nonbiz.util.RBSimilarityPreconditions.checkBothSame;

/**
 * A 2d collection of doubles, which can be indexed by a row key and a column key, which can be of different types.
 *
 * <p> It's a bit like a 2-dimensional map where there are two keys, and the values are doubles.
 * The underlying data store is a {@link RBMatrix}, so this class is particularly useful in case we
 * want to interact with the Colt linear algebra library. Of course, this means that the fact that we use a
 * {@link DoubleMatrix2D} inside {@link RBMatrix} is not hidden by this abstraction -
 * but it's fine; this is intentional here. </p>
 */
public class RBIndexableMatrix<R, C> implements IndexableDoubleDataStore2D<R, C> {

  private final RBMatrix rbMatrix;
  private final ArrayIndexMapping<R> rowMapping;
  private final ArrayIndexMapping<C> columnMapping;

  private RBIndexableMatrix(
      RBMatrix rbMatrix,
      ArrayIndexMapping<R> rowMapping,
      ArrayIndexMapping<C> columnMapping) {
    this.rbMatrix = rbMatrix;
    this.rowMapping = rowMapping;
    this.columnMapping = columnMapping;
  }

  public static <R, C> RBIndexableMatrix<R, C> rbIndexableMatrix(
      RBMatrix rbMatrix,
      ArrayIndexMapping<R> rowMapping,
      ArrayIndexMapping<C> columnMapping) {
    checkBothSame(
        rbMatrix.getNumRows(),
        rowMapping.size(),
        "# of matrix rows = %s , but # of rows we have a mapping for is %s : %s %s %s",
        rbMatrix.getNumRows(), rowMapping.size(), rowMapping, columnMapping, rbMatrix);
    checkBothSame(
        rbMatrix.getNumColumns(),
        columnMapping.size(),
        "# of matrix columns = %s , but # of columns we have a mapping for is %s : %s %s %s",
        rbMatrix.getNumColumns(), columnMapping.size(), rowMapping, columnMapping, rbMatrix);
    return new RBIndexableMatrix<>(rbMatrix, rowMapping, columnMapping);
  }

  public static <R> RBIndexableMatrix<R, MatrixColumnIndex> rbIndexableMatrixWithTrivialColumnMapping(
      RBMatrix rbMatrix,
      ArrayIndexMapping<R> rowMapping) {
    return rbIndexableMatrix(
        rbMatrix,
        rowMapping,
        simpleArrayIndexMapping(rbMatrix.matrixColumnIndexStream()));
  }

  public static <C> RBIndexableMatrix<MatrixRowIndex, C> rbIndexableMatrixWithTrivialRowMapping(
      RBMatrix rbMatrix,
      ArrayIndexMapping<C> columnMapping) {
    return rbIndexableMatrix(
        rbMatrix,
        simpleArrayIndexMapping(rbMatrix.matrixRowIndexStream()),
        columnMapping);
  }

  /**
   * Retrieve this indexable matrix as a standard non-indexable {@link RBMatrix}.
   */
  public RBMatrix asRbMatrix() {
    return rbMatrix;
  }

  /**
   * When we multiply two matrixes, the # of columns in the left matrix must be the same as the # of rows in the right
   * matrix. Since this is a {@link RBIndexableMatrix}, the types must also match. However, just to be on the safe
   * side, we will also confirm that the mapping key for the n-th column of the left matrix is the same as the
   * mapping key of the n-th row of the right matrix.
   *
   * The types are: (R, C) x (C, C2), so the result is (R, C2).
   */
  public <C2> RBIndexableMatrix<R, C2> multiply(RBIndexableMatrix<C, C2> rightMatrix) {
    ArrayIndexMapping<C> leftColumnMapping = this.getColumnMapping();
    ArrayIndexMapping<C> rightRowMapping = rightMatrix.getRowMapping();
    int sharedSize = checkBothSame(
        leftColumnMapping.size(),
        rightRowMapping.size(),
        "We cannot multiply a %s x %s with a %s x %s matrix: %s %s",
        this.getNumRows(), this.getNumColumns(), rightMatrix.getNumRows(), rightMatrix.getNumColumns(), this, rightMatrix);

    for (int i = 0; i < sharedSize; i++) {
      C leftKey = leftColumnMapping.getKey(i);
      C rightKey = rightRowMapping.getKey(i);
      // This relies on #equals, which is normally a pointer comparison in our code because we rarely
      // implement a non-trivial equals / hashCode. However, for objects that become keys to an ArrayIndexMapping
      // (or maps and other map-like objects, in general), we always do because we have to.
      checkBothSame(
          leftKey,
          rightKey,
          "In matrix multiplication, sizes are OK, but the keys do not match: %s %s",
          this, rightMatrix);
    }
    return rbIndexableMatrix(
        this.rbMatrix.multiply(rightMatrix.rbMatrix),
        this.getRowMapping(),
        rightMatrix.getColumnMapping());
  }

  /**
   * Matrix inverse. This mostly calls the Colt matrix inverse() function.
   *
   * <p> Additionally, because this is an indexable matrix,
   * the row and column mappings of the original become the column and row mappings (respectively) of the inverse.
   * Although this is a loose explanation, this makes more sense if you think of the matrix as a transformation
   * from A to B; the inverse is a transformation from B to A. </p>
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
  public RBIndexableMatrix<C, R> inverse() {
    return rbIndexableMatrix(
        rbMatrix.inverse(),
        columnMapping,
        rowMapping);
  }

  /**
   * Matrix transpose.
   *
   * <p> Because this is an indexable matrix, the row and column mappings of the original
   * will become the column and row mappings (respectively) of the inverse. </p>
   */
  public RBIndexableMatrix<C, R> transpose() {
    return rbIndexableMatrix(
        rbMatrix.transpose(),
        columnMapping,
        rowMapping);
  }

  @Override
  public double getByIndex(int rowIndexAsInt, int columnIndexAsInt) {
    return rbMatrix.get(matrixRowIndex(rowIndexAsInt), matrixColumnIndex(columnIndexAsInt));
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
  public DoubleMatrix2D getRawMatrixUnsafe() { // FIXME IAK UNSAFE remove this method, as well as the RBMatrix one.
    return rbMatrix.getRawMatrixUnsafe();
  }

  @Override
  public String toString() {
    return Strings.format("[RBIM matrix with %s rows= %s and %s columns = %s: %s RBIM]",
        getNumRows(),
        rowMapping,
        getNumColumns(),
        getColumnMapping(),
        rbMatrix);
  }

}
