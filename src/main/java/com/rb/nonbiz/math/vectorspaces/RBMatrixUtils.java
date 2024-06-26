package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.math.vectorspaces.RBMatrix.RBEigenvalueDecomposition;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;
import static com.rb.nonbiz.math.vectorspaces.MatrixRowIndex.matrixRowIndex;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

/**
 * Various static utility methods related to {@link RBMatrix}.
 */
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
      Epsilon epsilon) {
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
  public static boolean isAlmostIdentityMatrix(RBMatrix matrix, Epsilon epsilon) {
    // Non-square matrices are never similar to identity.
    if (!matrix.isSquare()) {
      return false;
    }
    return matrix.matrixRowIndexStream().allMatch(matrixRowIndex ->
        matrix.matrixColumnIndexStream().allMatch(matrixColumnIndex -> {
          double correctValue = (matrixRowIndex.intValue() == matrixColumnIndex.intValue()) ? 1.0 : 0.0;
          return epsilon.valuesAreWithin(matrix.get(matrixRowIndex, matrixColumnIndex), correctValue);
        }));
  }

  /**
   * Check if a matrix is symmetric (to within epsilon).
   */
  public static boolean isSymmetricMatrix(RBMatrix rbMatrix, Epsilon epsilon) {
    if (!rbMatrix.isSquare()) {
      return false;
    }
    int sharedSize = rbMatrix.getNumRows();

    // We usually like to use Streams and fluent code, but this could be a big operation (for a large matrix),
    // plus it's clear enough to look at for loops when iterating over a matrix.
    for (int i = 0; i < sharedSize; i++) {
      for (int j = i + 1; j < sharedSize; j++) {
        double aboveDiagonal = rbMatrix.get(matrixRowIndex(i), matrixColumnIndex(j));
        double belowDiagonal = rbMatrix.get(matrixRowIndex(j), matrixColumnIndex(i));
        if (!epsilon.valuesAreWithin(aboveDiagonal, belowDiagonal)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Check whether a matrix is symmetric and 'positive semi-definite'.
   *
   * <p> A matrix M is 'positive semi-definite' if for any vector x: </p>
   * {@code x_transpose * M * x >= 0}
   *
   * <p> 'Positive semi-definite' is the matrix equivalent of 'non-negative'. A 'PSD' matrix
   * defines a non-negative vector length, i.e. sqrt(x' * M * x). </p>
   *
   * <p> Covariance matrices are symmetric and positive semi-definite. This check
   * ascertains whether a given matrix could be a covariance matrix, at least insofar
   * as a covariance matrix must be PSD. </p>
   *
   * <p> This is meant to be a comprehensive check, not an "80/20" check to catch most problems. </p>
   */
  public static boolean isPositiveSemiDefiniteSymmetricMatrix(RBSquareMatrix rbSquareMatrix) {
    // Do various matrix checks, from easiest to complex, in order to "fail fast".

    if (!isSymmetricMatrix(rbSquareMatrix, DEFAULT_EPSILON_1e_8)) {
      // Non-symmetric matrices can be positive semi-definite, but we're only interested in symmetric ones.
      // In particular, we want to use the property that symmetric matrices have real eigenvalues.
      // Also, covariance matrices are symmetric, and they're what we're mostly interested in here.
      return false;
    }

    // We know the matrix is square. So # rows = # columns.
    int numRowsOrColumns = rbSquareMatrix.getNumRowsOrColumns();
    double[] sqrtDiagonal = new double[numRowsOrColumns];

    // Check that all diagonal elements are non-negative.
    // The covariance of a variable with itself is just that variable's variance. Variances must be
    // non-negative. That is cov[i, i] = var[i] >= 0.
    for (int i = 0; i < numRowsOrColumns; ++i) {
      double diagonalElement = rbSquareMatrix.get(matrixRowIndex(i), matrixColumnIndex(i));
      // no epsilon tolerance for this check; we're going to take the square root of each diagonal element
      if (diagonalElement < 0.0) {
        // can't have a negative diagonal element
        return false;
      }
      sqrtDiagonal[i] = Math.sqrt(diagonalElement);
    }

    // Check that no off-diagonal elements is too large.
    // The covariance[i, j] must be <= sqrt(variance[i]) * sqrt(variance[j]).
    for (int i = 0; i < numRowsOrColumns; ++i) {
      for (int j = i + 1; j < numRowsOrColumns; ++j) {
        // Use a very small tolerance; presumably these matrices are being read in from a vendor and
        // have been checked before we get them. If this turns out to be too tight, we can loosen it.
        double offDiagonalElement = rbSquareMatrix.get(matrixRowIndex(i), matrixColumnIndex(j));
        if (Math.abs(offDiagonalElement) > sqrtDiagonal[i] * sqrtDiagonal[j] + 1e-14) {
          // this off-diagonal term is too big
          return false;
        }
      }
    }

    // Now check if all eigenvalues are non-negative (or at most epsilon negative). If all
    // are non-negative, then the matrix will be positive semi-definite.
    //
    // Why should non-negative eigenvalues imply a positive semi-definite matrix?
    // A matrix M is positive semi-definite if x' * M * x >= 0 for all vectors x.
    // Consider an eigenvector e such that M * e = lambda * e.
    // Then e' * M * e >= 0. Therefore e' * lambda * e >= 0, or lambda * e' * e >= 0.
    // Since e' * e >= 0, it must be true that lambda >= 0.
    //
    // The converse is also true. Any vector x can be expanded as x = sum_i(a_i * e_i)
    // since the eigenvectors e_i "span" the vector space.
    // Then x' * M * x = sum_i(a_i * e_i') * M * sum_j(a_j * e_j)
    // Each term with i != j will be (a_i * e_i') * M * (a_j * e_j) = a_i * a_j * e_i' * (lamba_j * e_j)
    //        = (a_i * a_j * lambda_j) * (e_i' * e_j) = 0 since the eigenvectors are orthogonal.
    // Keeping only terms with i == j, we have x' * M * x = sum_i((a_i * x_i') * M * (a_i * x_i))
    //        = sum_i(a_i * x_i' * lambda_i * a_i * x_i) = sum_i((a_i)^2 * lambda_i * e_i' * e_i) >= 0
    // since (a_i)^2 is non-negative, each eigenvalue lambda_i is non-negative, and e_i' * e_i is non-negative.
    //
    // The downside of the "eigenvalues are non-negative" check is that the Colt routine has complexity
    // O(n^3) for matrix of size n x n.

    RBEigenvalueDecomposition rbEigenvalueDecomposition = rbSquareMatrix.calculateEigendecomposition();

    // No need to worry about complex eigenvalues; this is a symmetric matrix.
    double[] sortedEigenValues = rbEigenvalueDecomposition.getRealEigenvaluesAscending()
        .doubleStream()
        .sorted()
        .toArray();
    double smallestEigenvalue = sortedEigenValues[0];
    // For numerical reasons, allow the smallest eigenvalue to be slightly negative.
    // Zero and epsilon-negative eigenvalues may be discarded anyway if we're using SVD.
    if (smallestEigenvalue < -1e-8) {
      return false;
    }

    // All tests pass; the matrix must be symmetric and positive semi-definite.
    return true;
  }

}
