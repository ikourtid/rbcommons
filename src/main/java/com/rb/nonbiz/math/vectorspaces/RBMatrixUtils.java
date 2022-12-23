package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.linalg.EigenvalueDecomposition;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.stream.DoubleStream;

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
    RBPreconditions.checkArgument(epsilon >= 0);
    for (int i = 0; i < matrix.getNumRows(); ++i) {
      for (int j = 0; j < matrix.getNumColumns(); ++j) {
        double correctValue = (i == j) ? 1.0 : 0.0;
        if (Math.abs(matrix.getRawMatrixUnsafe().get(i, j) - correctValue) > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Check if a matrix is symmetric (to within epsilon).
   */
  public static boolean isSymmetricMatrix(RBMatrix rbMatrix, double epsilon) {
    RBPreconditions.checkArgument(
        epsilon >= 0 && epsilon <= 100.0,
        "Epsilon should be non-negative and probably less than 100; found %s ",
        epsilon);
    if (!rbMatrix.isSquare()) {
      return false;
    }
    int sharedSize = rbMatrix.getNumRows();

    // We usually like to use Streams and fluent code, but this could be a big operation (for a large matrix),
    // plus it's clear enough to look at for loops when iterating over a matrix.
    for (int i = 0; i < sharedSize; i++) {
      for (int j = i + 1; j < sharedSize; j++) {
        double aboveDiagonal = rbMatrix.get(i, j);
        double belowDiagonal = rbMatrix.get(j, i);
        if (Math.abs(aboveDiagonal - belowDiagonal) > epsilon) {
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
   * <p> Covariance matrices are symmetric and positive semi-definite. This check
   * ascertains whether a given matrix could be a covariance matrix. </p>
   */
  public static boolean isPositiveSemiDefiniteSymmetricMatrix(RBMatrix rbMatrix) {
    // Do various matrix checks, from easiest to complex, in order to "fail fast".
    if (!rbMatrix.isSquare()) {
      // A matrix M can't have x_transpose * M * x >= 0 unless it's square;
      // the matrix multiplication wouldn't be properly specified.
      return false;
    }

    if (!isSymmetricMatrix(rbMatrix, 1e-8)) {
      // Non-symmetric matrices can be positive semi-definite, but we're only interested in symmetric ones.
      // In particular, we want to use the property that symmetric matrices have real eigenvalues.
      // Also, covariance matrices are symmetric, and they're what we're mostly interested in here.
      return false;
    }

    // We have already checked that the matrix is symmetric, so it must be square. So # rows = # columns.
    int numRowsOrColumns = rbMatrix.getNumRows();
    double[] sqrtDiagonal = new double[numRowsOrColumns];

    // Check that all diagonal elements are non-negative.
    // The covariance of a variable with itself is just that variable's variance. Variances must be
    // non-negative. That is cov[i, i] = var[i] >= 0.
    for (int i = 0; i < numRowsOrColumns; ++i) {
      double diagonalElement = rbMatrix.get(i, i);
      // no epsilon for this check; we're going to take the square root of each diagonal element
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
        // have been checked. If this turns out to be too tight, we can loosen it.
        if (Math.abs(rbMatrix.get(i, j)) > sqrtDiagonal[i] * sqrtDiagonal[j] + 1e-14) {
          // the matrix isn't symmetric
          return false;
        }
      }
    }

    // Now check if all eigenvalues are non-negative (or at most epsilon negative). If they
    // are non-negative, then the matrix will be positive semi-definite.
    //
    // Why should non-negative eigenvalues imply a positive semi-definite matrix?
    // A matrix M is positive semi-definite if x' * M * x >= 0 for all vectors x.
    // Consider an eigenvector v such that M * v = lambda * v.
    // Then v' * M * v >= 0. Therefore v' * lambda * v >= 0, or lambda * v' * v >= 0.
    // Since v' * v >= 0, it must be true that lambda >= 0. The converse is also true.
    //
    // The downside of this check is that its complexity is O(n^3) for matrix of size n x n.

    // The documenation for Colt EigenvalueDecomposition() says it will only throw an exception
    // if the matrix isn't square, which we checked at the beginning of this method. Therefore,
    // we don't use "try/catch" when evaluating it.
    EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(
        rbMatrix.getRawMatrixUnsafe());
    // No need to worry about complex eigenvalues; this is a symmetric matrix.
    double[] sortedEigenValues = DoubleStream.of(eigenvalueDecomposition.getRealEigenvalues().toArray())
        .sorted()
        .toArray();
    double smallestEigenvalue = sortedEigenValues[0];
    // For numerical reasons, allow the smallest eigenvalue to be slightly negative.
    // Zero and epsilon-negative eigenvalues may be discarded if we're using SVD.
    if (smallestEigenvalue < -1e-8) {
      return false;
    }

    // All tests pass; the matrix must be symmetric and positive semi-definite.
    return true;
  }

}

