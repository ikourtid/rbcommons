package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Iterator;
import java.util.stream.IntStream;

import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;

/**
 * A vector space basis is a set of linearly independent vectors that span the vector space,
 * i.e. every vector in the vector space can be expressed as a linear combination of the vectors in the basis.
 *
 * The vectors do not have to be pairwise orthogonal, or unit vectors; for that, see {@link VectorSpaceOrthonormalBasis}.
 *
 * Instead of an ordered list of vectors, we store them in matrix format, where column vectors are the
 * vectors in the basis. This is a standard math way of doing it.
 */
public class VectorSpaceBasis {

  private final RBSquareMatrix rawMatrix;
  private final double determinant;

  private VectorSpaceBasis(RBSquareMatrix rawMatrix, double determinant) {
    this.rawMatrix = rawMatrix;
    this.determinant = determinant;
  }

  public static VectorSpaceBasis vectorSpaceBasis(RBSquareMatrix rawMatrix) {
    double determinant = rawMatrix.calculateDeterminant();
    RBPreconditions.checkArgument(
        Math.abs(determinant) > 1e-8,
        "Determinant is %s ; can't be 0 or almost 0, because this means the vectors in the basis are not linearly independent",
        determinant);
    // Since we calculate the determinant here, we might as well save it
    return new VectorSpaceBasis(rawMatrix, determinant);
  }

  public RBVector getBasisVector(MatrixColumnIndex matrixColumnIndex) {
    return rawMatrix.getColumnVector(matrixColumnIndex);
  }

  public Iterator<RBVector> basisVectorIterator() {
    return IntStream
        .range(0, rawMatrix.getNumRowsOrColumns())
        .mapToObj(i -> rawMatrix.getColumnVector(matrixColumnIndex(i)))
        .iterator();
  }

  /**
   * This is here to help the test matcher, hence the 'Unsafe' in the name, and the package-private status.
   */
  RBSquareMatrix getRawMatrixUnsafe() {
    return rawMatrix;
  }

  public double getDeterminant() {
    return determinant;
  }

  public int getNumDimensions() {
    return rawMatrix.getNumRowsOrColumns();
  }

  @Override
  public String toString() {
    return Strings.format("[VSB determinant= %s ; %s VSB]", determinant, rawMatrix);
  }

}
