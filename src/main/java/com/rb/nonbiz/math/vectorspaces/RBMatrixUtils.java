package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.linalg.CholeskyDecomposition;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Arrays;
import java.util.OptionalDouble;

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


  /** Given raw loadings and a matrix, compute variance.  variance = loadings' * COVMAT * loadings.
   * This helper function is not taking on the responsibility of making sure any properties of the
   *  covariance matrix are true.  It simply does a multiplication.
   *  If the loadings and the covariance matrices passed in are incompatible sizes, this will
   *  throw an exception.  The only check applied is that rawLoadings has 1 column.
   */
  public static double computeVariance(RBMatrix covarianceMatrix, RBMatrix rawLoadings) {
    RBPreconditions.checkArgument(rawLoadings.getNumColumns() == 1);
    // We are getting variance as a double, not a 1 x 1 matrix, hence using .get(0, 0).
    return rawLoadings.transpose().multiply(covarianceMatrix.multiply(rawLoadings)).getRawMatrixUnsafe().get(0, 0);
  }

  /**
   * Returns true iff a matrix is similar to the identity matrix, to within epsilon.
   */
  public static boolean isAlmostIdentityMatrix(RBMatrix matrix, double epsilon) {
    // Non-square matrices are never similar to identity.
    if (!matrix.isSquare()) {
      return false;
    }
    checkArgument(epsilon > 0);
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

  public static boolean isPositiveSemiDefiniteMatrix(RBMatrix rbMatrix) {
    // Do various matrix checks, from the easiest to most complex, in order to "fail fast".
    if (!rbMatrix.isSquare()) {
      return false;
    }

    if (!isSymmetricMatrix(rbMatrix, 1e-8)) {
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
      if (diagonalElement < 0.0) {
        // can't have a negative diagonal element
        return false;
      }
      sqrtDiagonal[i] = Math.sqrt(diagonalElement);
    }

    // Check that all off-diagonal elements are not too large.
    // The covariance[i, j] must be <= sqrt(variance[i]) * sqrt(variance[j]).
    for (int i = 0; i < numRowsOrColumns; ++i) {
      for (int j = i + 1; j < numRowsOrColumns; ++j) {
        if (Math.abs(rbMatrix.get(i, j)) > sqrtDiagonal[i] * sqrtDiagonal[j] + 1e-14) {
          return false;
        }
      }
    }

    // Now check if we can do a Choleskey decomposition. Apparently, this is only possible for
    // positive semi-definite matrices.
    // The downside is that this operation scales as N^3 for matrix size N.

    try {
      System.out.format("rbMat %s\n", rbMatrix.getRawMatrixUnsafe());
      CholeskyDecomposition choleskyDecomposition = new CholeskyDecomposition(rbMatrix.getRawMatrixUnsafe());
      System.out.format("chol %s\n", choleskyDecomposition);
      if (choleskyDecomposition.isSymmetricPositiveDefinite()) {
        return true;
      }
      EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(rbMatrix.getRawMatrixUnsafe());
      DoubleMatrix1D realEigenvalues = eigenvalueDecomposition.getRealEigenvalues();
      System.out.format("eigenvalues %s\n", eigenvalueDecomposition.getRealEigenvalues());
      OptionalDouble first = Arrays.stream(realEigenvalues.toArray()).sorted().findFirst();
      System.out.format("first %s\n", first);
      if (first.isPresent() && first.getAsDouble() < 0) {
        return false;
      }
    } catch (IllegalArgumentException e) {
      // Don't throw an exception; we just wanted to see if a Cholesky decomposition was possible.
      return false;
    }

    return true;
  }
}

