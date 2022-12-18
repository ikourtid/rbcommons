package com.rb.nonbiz.math.vectorspaces;

import static com.rb.nonbiz.util.RBPreconditions.checkArgument;

public class RBMatrixUtils {

  /**
   * Returns true iff a matrix is a valid transformation matrix from orthonormal factor space to raw factor space.
   * A transformation matrix is orthonormal iff, given any two exposure vectors A and B in orthonormal space,
   * it's true that: Variance(A+B) = Variance(A) + Variance(B)
   */
  public static boolean isOrthoNormalTransformationMatrix(
      RBMatrix transformationMatrixOrthToRaw,
      RBMatrix covarianceMatrix,
      double epsilon) {

    /** Where T is the transformation matrix from orth. space to raw space, it should always be true that
     * T' * COVARIANCE * T is equal to the identity matrix.
     * Intuitively, this means that the covariance matrix in ortho-space is the identity matrix
     * If the above is true, then given any two exposures in orthogonal space, it's also true that:
     * Variance(Exposure1) + Variance(Exposure2) = Variance(Exposure1 + Exposure2)
     */
    RBMatrix shouldBeIdentity = transformationMatrixOrthToRaw.transpose().multiply(
        covarianceMatrix.multiply(transformationMatrixOrthToRaw));
    return isAlmostIdentityMatrix(shouldBeIdentity, epsilon);
  }

  /**
   * Returns true iff a matrix is similar to the identity matrix, to within epsilon.
    */
  public static boolean isAlmostIdentityMatrix(RBMatrix matrix, double epsilon) {
    // Non-square matrices are never similar to identity.
    if (matrix.getNumColumns() != matrix.getNumRows()) {
      return false;
    }
    checkArgument(epsilon > 0);
    for( int i = 0; i < matrix.getNumRows(); ++i) {
      for( int j = 0; j < matrix.getNumColumns(); ++j) {
        double correctValue = (i == j) ? 1.0 : 0.0;
        if (Math.abs(matrix.getRawMatrixUnsafe().get(i, j) - correctValue) > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

}
