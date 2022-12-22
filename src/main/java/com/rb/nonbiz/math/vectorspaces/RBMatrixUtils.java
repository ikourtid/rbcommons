package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.util.RBPreconditions.checkArgument;

public class RBMatrixUtils {

  /**
   * Returns true iff a matrix is a valid transformation matrix from orthonormal factor space to raw factor space.
   * A transformation matrix is orthonormal iff, given any two exposure vectors A and B in orthonormal space,
   * it's true that: Variance(A+B) = Variance(A) + Variance(B).
   * Also, where T is the transformation matrix from orth. space to raw space, it should always be true that
   * T' * COVARIANCE * T is equal to the identity matrix.
   * Intuitively, this means that the covariance matrix in ortho-space is the identity matrix
   * If the above is true, then given any two exposures in orthogonal space, it's also true that:
   * Variance(Exposure1) + Variance(Exposure2) = Variance(Exposure1 + Exposure2)
   */
  public static boolean isOrthoNormalTransformationMatrix(
      RBMatrix transformationMatrixOrthToRaw,
      RBMatrix covarianceMatrix,
      double epsilon) {
    RBMatrix shouldBeIdentity = transformationMatrixOrthToRaw.transpose().multiply(
        covarianceMatrix.multiply(transformationMatrixOrthToRaw));
    return isAlmostIdentityMatrix(shouldBeIdentity, epsilon);
  }


  /**
   * Given raw loadings and a matrix, compute variance.  variance = loadings' * COVMAT * loadings.
   * This helper function is not taking on the responsibility of making sure any properties of the
   *  covariance matrix are true.  It simply does a multiplication.
   *  If the loadings and the covariance matrices passed in are incompatible sizes, this will
   *  throw an exception.  The only check applied is that rawLoadings has 1 column.
   */
  public static double computeVariance(RBMatrix covarianceMatrix, RBMatrix rawLoadings) {
    RBPreconditions.checkArgument(rawLoadings.getNumColumns() == 1);
    // We are getting variance as a double, not a 1 x 1 matrix, hence using .get(0, 0).
    return rawLoadings.transpose().multiply(covarianceMatrix.multiply(rawLoadings)).getOnlyElementOrThrow();
  }

  /**
   * Returns true iff a matrix is similar to the identity matrix, to within epsilon.
    */
  public static boolean isAlmostIdentityMatrix(RBMatrix matrix, double epsilon) {
    // Non-square matrices are never similar to identity.
    if (!matrix.isSquare()) {
      return false;
    }
    checkArgument(epsilon >= 0);
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
