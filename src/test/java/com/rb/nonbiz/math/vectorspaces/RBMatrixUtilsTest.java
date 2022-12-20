package com.rb.nonbiz.math.vectorspaces;

import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.math.vectorspaces.RBMatrix.rbIdentityMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbDiagonalMatrix2by2;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbDiagonalMatrix3by3;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbMatrix2by2;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbMatrix3by3;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.singletonRBMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixUtils.computeVariance;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixUtils.isAlmostIdentityMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixUtils.isOrthoNormalTransformationMatrix;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBMatrixUtilsTest {

  @Test
  public void testIsOrthoNormalTransformationMatrixWithCovariance() {
    // Covariance matrix with reasonably strong positive correlation
    RBMatrix covMat = rbMatrix2by2(
        1.0, 0.5,
        0.5, 1.0);
    double epsilon = 1e-4;
    // If interested, orthToRaw comes from the inverse of: rbMatrix2by2(0.866, 0.866, -0.5, 0.5));
    // For now this matrix may seem arbitrary, but we check it below
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737, -1, 0.57737, 1), covMat, epsilon));
    // Below the tests repeat the case above, and also assert that negating one side of the transformation is valid.
    // Intuitively, if T is a valid transformation from orthonormal to raw, then a transformation that is the same
    // as T but flips the sign of a vector in raw space is also valid.
    // For example, imagine that
    //     (1, 0) in orthonormal space is (-2 marketcap, 0.5 growth) in raw space.
    //     (0, 1) in orthonormal space is (0.5 marketcap, 2 growth) in raw space.
    // Then it's equally valid to (A) negate one of the transformation.  i.e.
    //       To say that (1, 0) in orthonormal space is (2 marketcap, -0.5 growth) in raw space.
    //     or to (B) flip the meaning of (1, 0) and (0, 1) in orth space.  i.e.
    //     (0, 1) in orthonormal space is (-2 marketcap, 0.5 growth) in raw space.
    //     (1, 0) in orthonormal space is (0.5 marketcap, 2 growth) in raw space.
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737,  -1,  0.57737,  1), covMat, epsilon));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737,   1,  0.57737, -1), covMat, epsilon));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(-0.57737, -1, -0.57737,  1), covMat, epsilon));
    // Switching top and bottom is the same as well, even if we negate one side
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737, -1,   0.57737,  1), covMat, epsilon));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737,  1,   0.57737, -1), covMat, epsilon));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737, -1,   0.57737,  1), covMat, epsilon));

    // Making a large change to the transormation makes in invalid
    for (double delta : newRBSet(-0.1, 0.1)) {
      assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737,          -1,         0.57737,         1),
          covMat, epsilon));
      assertFalse(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737 + delta, -1,         0.57737,         1),
          covMat, epsilon));
      assertFalse(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737,         -1 + delta, 0.57737,         1),
          covMat, epsilon));
      assertFalse(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737,         -1,         0.57737 + delta, 1),
          covMat, epsilon));
      assertFalse(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737,         -1,         0.57737,         1 + delta),
          covMat, epsilon));
    }


    // Now, use different method to check our orthogonal to raw matrix is actually reasonable.
    // Below we use the axiomatic definition: variance(A) + variance(B) = variance(A+B)
    // For 2x2, it's enough to check that:
    //   variance( [1, 0]) = variance( [0, 1] ) = 1.0
    //   variance( [1, 1]) = 2.0
    // The reason is that the first two checks make sure the covariance matrix in orthonormal space
    // has ones in each diagonal, and the third check makes sure the covariance matrix in orthonormal
    // space has a zero in the top-right.  Because covariances are symmetric, this means it must have a zero
    // in the bottom left as well.
    assertEquals(1.0, computeVariance(covMat, rbMatrix2by2(
        0.57737, -1,
        0.57737,  1).multiply(matrix2by1(1, 0))), epsilon);
    assertEquals(1.0, computeVariance(covMat, rbMatrix2by2(
        0.57737, -1,
        0.57737,  1).multiply(matrix2by1(0, 1))), epsilon);
    assertEquals(2.0, computeVariance(covMat, rbMatrix2by2(
        0.57737, -1,
        0.57737,  1).multiply(matrix2by1(1, 1))), epsilon);
  }


  private RBMatrix matrix2by1(double first, double second) {
    return rbMatrix(new double[][] { { first }, { second } });
  }
  private RBMatrix matrix3by1(double first, double second, double third) {
    return rbMatrix(new double[][] { { first }, { second }, { third } });
  }

  @Test
  public void testIsOrthoNormalTransformationMatrixNoCovariance() {
    // One-by-one matrices are easiest
    assertTrue(isOrthoNormalTransformationMatrix(singletonRBMatrix(1), singletonRBMatrix(1), 1e-4));
    /**
     * Stretch transformation matrix one way, covariance the other, so OK.  Flip sign is also OK.
     * There are two ways to think about why we need square-root of the difference in variances.
     * The practical, but not satisfying one, is 'it makes the numbers work, and other transformations don't".
     * The intuitive one is that, when we compute variance, we do w' * COV * w, where w is raw exposure.
     * But remember raw exposures are T * w_orth, where T is the transformation matrix and w_orth is orth. exposures.
     * So you can re-write variance as: (T * w_orth)' * COV * (T * w_orth).  Elements of the transformation matrix
     * are getting squared in the variance calculation.  So if the covariance matrix has a number N, adding one
     * over the squaroot of this number in the transformation matrix will multiply it by 1 / N, bringing it back to 1.
     */

    assertTrue(isOrthoNormalTransformationMatrix(singletonRBMatrix(1 / Math.sqrt(2.0)), singletonRBMatrix(2), 1e-4));
    assertTrue(isOrthoNormalTransformationMatrix(singletonRBMatrix(-1 / Math.sqrt(2.0)), singletonRBMatrix(2), 1e-4));
    // Mismatch but epsilon is huge so OK
    assertTrue(isOrthoNormalTransformationMatrix(singletonRBMatrix(5.0), singletonRBMatrix(1), 100));

    // Mismatch.  Flip sign is OK but stretches don't match
    assertFalse(isOrthoNormalTransformationMatrix(singletonRBMatrix(-1), singletonRBMatrix(2), 1e-4));

    // Identity covariance matrix and transformation matrix are always orthonormal
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(1, 0, 0, 1), rbIdentityMatrix(2), 1e-4));
    // Flipping a factor is ortho-normal if covariance is identity
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(0, 1, 1, 0), rbIdentityMatrix(2), 1e-4));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(0, 1, -1, 0), rbIdentityMatrix(2), 1e-4));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(-1, 0, 0, 1), rbIdentityMatrix(2), 1e-4));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(-1, 0, 0, -1), rbIdentityMatrix(2), 1e-4));
    // Now covariance matrix is not identity, so a transformation matrix that switches factors or is identity is not orthonormal
    assertFalse(
        isOrthoNormalTransformationMatrix(
            rbMatrix2by2(0, 1, 1, 0),
            rbMatrix2by2(4.0, 1.0, 1.0, 0.5),
            1e-4));
    assertFalse(
        isOrthoNormalTransformationMatrix(
            rbMatrix2by2(1, 0, 0, 1),
            rbMatrix2by2(4.0, 1.0, 1.0, 0.5),
            1e-4));

    // Mismatch...if the covariance matrix stretches one factor, the transformation has to undo the stretch
    assertFalse(isOrthoNormalTransformationMatrix(
        rbMatrix2by2(1.0, 0, 0, 1),
        rbMatrix2by2(4.0, 0, 0, 1),
        1e-4));
    // OK...transformation matrix stretches first factor to compensate for higher variance.
    assertTrue(isOrthoNormalTransformationMatrix(
        rbMatrix2by2(0.5, 0, 0, 1),
        rbMatrix2by2(4.0, 0, 0, 1),
        1e-4));
    // OK...same as above but flip a sign
    assertTrue(isOrthoNormalTransformationMatrix(
        rbMatrix2by2(-0.5, 0, 0, 1),
        rbMatrix2by2(4.0, 0, 0, 1),
        1e-4));
  }

  @Test
  public void testComputeVarianceBadArgs()
  {
    assertIllegalArgumentException(() -> computeVariance(rbIdentityMatrix(2), rbIdentityMatrix(2)));
    double doesNotThrow = computeVariance(singletonRBMatrix(1.0), rbIdentityMatrix(1));
  }

  @Test
  public void testComputeVariance()
  {
    // Singleton matrix.  Correct answer is loading^2 * variance.
    assertEquals(1.0, computeVariance(singletonRBMatrix(1),       singletonRBMatrix(1)),   1e-4);
    assertEquals(4.0, computeVariance(singletonRBMatrix(1),       singletonRBMatrix(2)),   1e-4);
    assertEquals(4.0, computeVariance(singletonRBMatrix(1),       singletonRBMatrix(-2)),  1e-4);
    assertEquals(3.0, computeVariance(singletonRBMatrix(3),       singletonRBMatrix(-1)),  1e-4);
    assertEquals(3.0, computeVariance(singletonRBMatrix(3),       singletonRBMatrix(1)),   1e-4);
    assertEquals(3.0 / 4.0, computeVariance(singletonRBMatrix(3), singletonRBMatrix(0.5)), 1e-4);

    // Simple diagonal covariance checks of variance computation
    // Without explaining each one, the correct answer is the top-left diagonal times the first factor squared
    // plus the bottom-right diagonal times the second factor squared
    assertEquals(4.0,  computeVariance(rbDiagonalMatrix2by2(4, 1),   matrix2by1 (1,  0)), 1e-4);
    assertEquals(4.0,  computeVariance(rbDiagonalMatrix2by2(1, 1),   matrix2by1( 2,  0)), 1e-4);
    assertEquals(16.0, computeVariance(rbDiagonalMatrix2by2(4, 1),   matrix2by1( 2,  0)), 1e-4);
    assertEquals(0.0,  computeVariance(rbDiagonalMatrix2by2(0, 1),   matrix2by1( 2,  0)), 1e-4);
    assertEquals(16.0, computeVariance(rbDiagonalMatrix2by2(0, 1),   matrix2by1( 2,  4)), 1e-4);
    assertEquals(2.25, computeVariance(rbDiagonalMatrix2by2(.25, 2), matrix2by1(-1,  1)), 1e-4);
    assertEquals(18.5, computeVariance(rbDiagonalMatrix2by2(2,0.5),  matrix2by1( 3,  1)), 1e-4);
    assertEquals(18.5, computeVariance(rbDiagonalMatrix2by2(2, 0.5), matrix2by1(-3, -1)), 1e-4);
    assertEquals(18.5, computeVariance(rbDiagonalMatrix2by2(2, 0.5), matrix2by1(-3,  1)), 1e-4);
    assertEquals(0.5,  computeVariance(rbDiagonalMatrix2by2(2, 0.5), matrix2by1(0.5, 0)), 1e-4);

    // Now check with diagonals in covariance, positive correlatation.
    RBMatrix covmat2x2PositiveCorrelation = rbMatrix2by2( 4,  1,
                                                          1,  1.5);
    RBMatrix covmat2x2NegativeCorrelation = rbMatrix2by2( 4, -1,
                                                          -1, 1.5);
    assertEquals(4.0,  computeVariance(rbMatrix2by2(4,  1,  1,  1.5), matrix2by1 (1,  0)), 1e-4);
    assertEquals(1.5,  computeVariance(rbMatrix2by2(4,  1,  1,  1.5), matrix2by1 (0,  1)), 1e-4);
    assertEquals(doubleExplained(7.5, 4 + 1 + 1 + 1.5),
        computeVariance(covmat2x2PositiveCorrelation, matrix2by1 ( 1, 1)), 1e-4);
    assertEquals(doubleExplained(7.5, 4 + 1 + 1 + 1.5),
        computeVariance(covmat2x2PositiveCorrelation, matrix2by1 (-1,-1)), 1e-4);
    assertEquals(doubleExplained(3.5, 4 - 1 - 1 + 1.5),
        computeVariance(covmat2x2PositiveCorrelation, matrix2by1 (1, -1)), 1e-4);
    assertEquals(doubleExplained(3.5, 4 - 1 - 1 + 1.5),
        computeVariance(covmat2x2PositiveCorrelation, matrix2by1 (-1, 1)), 1e-4);
    assertEquals(doubleExplained(13.5, 16 - 2 - 2 + 1.5),
        computeVariance(covmat2x2PositiveCorrelation, matrix2by1 (-2, 1)), 1e-4);
    assertEquals(doubleExplained(21.5, 16 + 2 + 2 + 1.5),
        computeVariance(covmat2x2PositiveCorrelation, matrix2by1 (2,  1)), 1e-4);
    // Check wth negative correlation
    assertEquals(doubleExplained(3.5, 4 - 1 - 1 + 1.5),
        computeVariance(covmat2x2NegativeCorrelation, matrix2by1 ( 1,  1)), 1e-4);
    assertEquals(doubleExplained(3.5, 4 - 1 - 1 + 1.5),
        computeVariance(covmat2x2NegativeCorrelation, matrix2by1 (-1, -1)), 1e-4);
    assertEquals(doubleExplained(7.5, 4 + 1 + 1 + 1.5),
        computeVariance(covmat2x2NegativeCorrelation, matrix2by1 ( 1, -1)), 1e-4);
    assertEquals(doubleExplained(7.5, 4 + 1 + 1 + 1.5),
        computeVariance(covmat2x2NegativeCorrelation, matrix2by1 (-1,  1)), 1e-4);
    assertEquals(doubleExplained(21.5, 16 + 2 + 2 + 1.5),
        computeVariance(covmat2x2NegativeCorrelation, matrix2by1 (-2,  1)), 1e-4);
    assertEquals(doubleExplained(13.5, 16 - 2 - 2 + 1.5),
        computeVariance(covmat2x2NegativeCorrelation, matrix2by1 ( 2,  1)), 1e-4);

    // Try 3x3 matrices.
    // We'll use doubleExplained where it's helpful but it's too wordy for each answer.
    // For diagonal matrices, it's just the exposures squared dot product with the diagonal
    assertEquals(3.0,  computeVariance(rbDiagonalMatrix3by3(1, 1, 1), matrix3by1( 1,  1,  1)), 1e-4);
    assertEquals(4.0,  computeVariance(rbDiagonalMatrix3by3(1, 1, 1), matrix3by1( 0,  2,  0)), 1e-4);
    assertEquals(7.0,  computeVariance(rbDiagonalMatrix3by3(3, 2, 2), matrix3by1( 1,  1,  1)), 1e-4);
    assertEquals(7.0,  computeVariance(rbDiagonalMatrix3by3(3, 2, 2), matrix3by1(-1, -1, -1)), 1e-4);
    assertEquals(13.0, computeVariance(rbDiagonalMatrix3by3(3, 2, 2), matrix3by1( 1,  1,  2)), 1e-4);
    assertEquals(13.0, computeVariance(rbDiagonalMatrix3by3(3, 2, 2), matrix3by1(-1, -1, -2)), 1e-4);
    assertEquals(doubleExplained(29.98, 3 * 3 * 3 + 2 + 0.7 * 0.7 * 2),
        computeVariance(rbDiagonalMatrix3by3(3, 2, 2), matrix3by1(3, 1, 0.7)), 1e-4);

    // 3 x 3 non-diagonal matrix.
    // Remember we don't care if this is a valid correlation matrix...we are testing the multiplication.
    RBMatrix covmat3x3 = rbMatrix3by3(   3, 1.0,-1.0,
                                       1.0, 1.5, 0.6,
                                      -1.0, 0.6, 0.4);
    assertEquals(3.0,
        computeVariance(covmat3x3, matrix3by1(-1, 0, 0)), 1e-4);
    // Check a few cases with only 2 loadings
    assertEquals(doubleExplained(13, 3 + 1.5 * 2 * 2 + 2 + 2),
        computeVariance(covmat3x3, matrix3by1(-1, -2, 0)), 1e-4);
    assertEquals(doubleExplained(13, 3 + 1.5 * 2 * 2 + 2 + 2),
        computeVariance(covmat3x3, matrix3by1(1, 2, 0)), 1e-4);
    assertEquals(doubleExplained(5, 3 + 1.5 * 2 * 2 - 2 - 2),
        computeVariance(covmat3x3, matrix3by1(1, -2, 0)), 1e-4);
    assertEquals(doubleExplained(3.1, 1.5 + 0.6 + 0.6 + 0.4),
        computeVariance(covmat3x3, matrix3by1(0, -1, -1)), 1e-4);
    assertEquals(doubleExplained(0.7, 1.5 - 0.6 - 0.6 + 0.4),
        computeVariance(covmat3x3, matrix3by1(0, 1, -1)), 1e-4);
    // Check cases with 3 loadings
    // This should be the sum of all matrix elements
    assertEquals(6.1, computeVariance(covmat3x3, matrix3by1(1, 1, 1)), 1e-4);
    assertEquals(6.1, computeVariance(covmat3x3, matrix3by1(-1, -1, -1)), 1e-4);
    assertEquals(doubleExplained(20.7, 12 + 2 + 2 + 2 + 1.5 - 0.6 + 2 - 0.6 + 0.4),
        computeVariance(covmat3x3, matrix3by1(2.0, 1, -1)), 1e-4);
  }

  @Test
  public void testIsSimilartoIdentityMatrix() {
    double largeEpsilon = 1e-4;
    double smallEpsilon = 1e-8;

    // Identity equals identity
    assertTrue(isAlmostIdentityMatrix(rbIdentityMatrix(1), smallEpsilon));
    assertTrue(isAlmostIdentityMatrix(rbIdentityMatrix(2), smallEpsilon));
    assertTrue(isAlmostIdentityMatrix(rbIdentityMatrix(4), smallEpsilon));
    assertTrue(isAlmostIdentityMatrix(rbIdentityMatrix(10), smallEpsilon));

    // epsilon tests
    assertTrue(isAlmostIdentityMatrix(
        rbMatrix2by2(1.0 + smallEpsilon, 0.0, 0.0, 1.0 - smallEpsilon),
        largeEpsilon));
    assertTrue(isAlmostIdentityMatrix(
        rbMatrix2by2(3.0, -4.0, 8.0, 0.0),
        10.0)); // Huge epsilon is forgiving
    assertFalse(isAlmostIdentityMatrix(
        rbMatrix2by2(1.0 + smallEpsilon, 0.0, 0.0, 1.0 - largeEpsilon),
        smallEpsilon));
    assertFalse(isAlmostIdentityMatrix(
        rbMatrix2by2(1.0 + 2 * smallEpsilon, 0.0, 0.0, 1.0 - smallEpsilon),
        smallEpsilon));

    // Identity matrix
    assertTrue(isAlmostIdentityMatrix(rbMatrix2by2(1.0, 0.0, 0.0, 1.0), largeEpsilon));
    // Lots of non-identity matrices
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(1.0, 0.0, 2 * largeEpsilon, 1.0), largeEpsilon));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(2.0, 0.0, 0.0, 2.0), largeEpsilon));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0, 0.0, 0.0, -1.0), largeEpsilon));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0, 0.0, 0.0, -2.0), largeEpsilon));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0, 0.1, 0.1, 1.0), largeEpsilon));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0, -0.1, -0.1, 1.0), largeEpsilon));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0, 1.0, 1.0, 1.0), largeEpsilon));
  }

}