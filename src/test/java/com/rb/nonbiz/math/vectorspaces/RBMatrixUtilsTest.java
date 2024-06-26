package com.rb.nonbiz.math.vectorspaces;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.types.Epsilon;
import org.junit.Test;

import java.util.function.DoubleFunction;

import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.math.vectorspaces.RBMatrix.rbMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbDiagonalMatrix2by2;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbDiagonalMatrix3by3;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbMatrix2by2;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbMatrix3by3;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.singletonRBMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixUtils.computeVariance;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixUtils.isAlmostIdentityMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixUtils.isOrthoNormalTransformationMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixUtils.isPositiveSemiDefiniteSymmetricMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixUtils.isSymmetricMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrix.identityRBSquareMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrix.rbSquareMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrixTest.rbSquareMatrix2by2;
import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrixTest.rbSquareMatrix3by3;
import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrixTest.rbSquareMatrixDiagonal3by3;
import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrixTest.singletonRBSquareMatrix;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_DOUBLE;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.epsilon;
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
    Epsilon epsilon = epsilon(1e-4);
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
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2( 0.57737, -1,  0.57737,  1), covMat, epsilon));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2( 0.57737,  1,  0.57737, -1), covMat, epsilon));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(-0.57737, -1, -0.57737,  1), covMat, epsilon));
    // Switching top and bottom is the same as well, even if we negate one side
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737, -1, 0.57737,  1), covMat, epsilon));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737,  1, 0.57737, -1), covMat, epsilon));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737, -1, 0.57737,  1), covMat, epsilon));

    // Making a large change to the transformation makes in invalid
    for (double delta : newRBSet(-0.1, 0.1)) {
      assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737, -1, 0.57737, 1),
          covMat, epsilon));
      assertFalse(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737 + delta, -1, 0.57737, 1),
          covMat, epsilon));
      assertFalse(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737, -1 + delta, 0.57737, 1),
          covMat, epsilon));
      assertFalse(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737, -1, 0.57737 + delta, 1),
          covMat, epsilon));
      assertFalse(isOrthoNormalTransformationMatrix(rbMatrix2by2(0.57737, -1, 0.57737, 1 + delta),
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
    assertEquals(
        1.0,
        computeVariance(covMat, rbMatrix2by2(
            0.57737, -1,
            0.57737,  1).multiply(matrix2by1(1, 0))),
        epsilon.doubleValue());
    assertEquals(
        1.0,
        computeVariance(covMat, rbMatrix2by2(
            0.57737, -1,
            0.57737,  1).multiply(matrix2by1(0, 1))),
        epsilon.doubleValue());
    assertEquals(
        2.0,
        computeVariance(covMat, rbMatrix2by2(
            0.57737, -1,
            0.57737,  1).multiply(matrix2by1(1, 1))),
        epsilon.doubleValue());
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
    assertTrue(isOrthoNormalTransformationMatrix(singletonRBMatrix(1), singletonRBMatrix(1), epsilon(1e-4)));
    /**
     * Stretch transformation matrix one way, covariance the other, so OK.  Flip sign is also OK.
     * There are two ways to think about why we need square-root of the difference in variances.
     * The practical, but not satisfying one, is 'it makes the numbers work, and other transformations don't".
     * The intuitive one is that, when we compute variance, we do w' * COV * w, where w is raw exposure.
     * But remember raw exposures are T * w_orth, where T is the transformation matrix and w_orth is orth. exposures.
     * So you can re-write variance as: (T * w_orth)' * COV * (T * w_orth).  Elements of the transformation matrix
     * are getting squared in the variance calculation.  So if the covariance matrix has a number N, adding one
     * over the square root of this number in the transformation matrix will multiply it by 1 / N, bringing it back to 1.
     */

    assertTrue(isOrthoNormalTransformationMatrix(singletonRBMatrix( 1 / Math.sqrt(2.0)), singletonRBMatrix(2), epsilon(1e-4)));
    assertTrue(isOrthoNormalTransformationMatrix(singletonRBMatrix(-1 / Math.sqrt(2.0)), singletonRBMatrix(2), epsilon(1e-4)));
    // Mismatch but epsilon is huge so OK
    assertTrue(isOrthoNormalTransformationMatrix(singletonRBMatrix(5.0), singletonRBMatrix(1), epsilon(99)));

    // Mismatch.  Flip sign is OK but stretches don't match
    assertFalse(isOrthoNormalTransformationMatrix(singletonRBMatrix(-1), singletonRBMatrix(2), epsilon(1e-4)));

    // Identity covariance matrix and transformation matrix are always orthonormal
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2( 1, 0,  0,  1), identityRBSquareMatrix(2), epsilon(1e-4)));
    // Flipping a factor is ortho-normal if covariance is identity
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2( 0, 1,  1,  0), identityRBSquareMatrix(2), epsilon(1e-4)));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2( 0, 1, -1,  0), identityRBSquareMatrix(2), epsilon(1e-4)));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(-1, 0,  0,  1), identityRBSquareMatrix(2), epsilon(1e-4)));
    assertTrue(isOrthoNormalTransformationMatrix(rbMatrix2by2(-1, 0,  0, -1), identityRBSquareMatrix(2), epsilon(1e-4)));
    // Now covariance matrix is not identity, so a transformation matrix that switches factors or is identity is not orthonormal
    assertFalse(
        isOrthoNormalTransformationMatrix(
            rbMatrix2by2(0, 1, 1, 0),
            rbMatrix2by2(4.0, 1.0, 1.0, 0.5),
            epsilon(1e-4)));
    assertFalse(
        isOrthoNormalTransformationMatrix(
            rbMatrix2by2(1, 0, 0, 1),
            rbMatrix2by2(4.0, 1.0, 1.0, 0.5),
            epsilon(1e-4)));

    // Mismatch...if the covariance matrix stretches one factor, the transformation has to undo the stretch
    assertFalse(isOrthoNormalTransformationMatrix(
        rbMatrix2by2(1.0, 0, 0, 1),
        rbMatrix2by2(4.0, 0, 0, 1),
        epsilon(1e-4)));
    // OK...transformation matrix stretches first factor to compensate for higher variance.
    assertTrue(isOrthoNormalTransformationMatrix(
        rbMatrix2by2(0.5, 0, 0, 1),
        rbMatrix2by2(4.0, 0, 0, 1),
        epsilon(1e-4)));
    // OK...same as above but flip a sign
    assertTrue(isOrthoNormalTransformationMatrix(
        rbMatrix2by2(-0.5, 0, 0, 1),
        rbMatrix2by2(4.0, 0, 0, 1),
        epsilon(1e-4)));
  }

  @Test
  public void testComputeVarianceBadArgs() {
    // The loadings should have one column.
    assertIllegalArgumentException( () -> computeVariance(identityRBSquareMatrix(2), identityRBSquareMatrix(2)));
    double doesNotThrow = computeVariance(singletonRBMatrix(1.0), identityRBSquareMatrix(1));
  }

  @Test
  public void testComputeVariance() {
    // Singleton matrix.  Correct answer is loading^2 * variance.
    assertEquals(1.0, computeVariance(singletonRBMatrix(1),       singletonRBMatrix(1)), 1e-4);
    assertEquals(4.0, computeVariance(singletonRBMatrix(1),       singletonRBMatrix(2)),   1e-4);
    assertEquals(4.0, computeVariance(singletonRBMatrix(1),       singletonRBMatrix(-2)),  1e-4);
    assertEquals(3.0, computeVariance(singletonRBMatrix(3),       singletonRBMatrix(-1)),  1e-4);
    assertEquals(3.0, computeVariance(singletonRBMatrix(3),       singletonRBMatrix(1)),   1e-4);
    assertEquals(3.0 / 4.0, computeVariance(singletonRBMatrix(3), singletonRBMatrix(0.5)), 1e-4);

    // Simple diagonal covariance checks of variance computation
    // Without explaining each one, the correct answer is the top-left diagonal times the first loading squared
    // plus the bottom-right diagonal times the second loading squared
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

    // Now check with diagonals in covariance, positive correlation.
    RBMatrix covmat2x2PositiveCorrelation = rbMatrix2by2(
        4,  1,
        1,  1.5);
    RBMatrix covmat2x2NegativeCorrelation = rbMatrix2by2(
        4,  -1,
        -1,  1.5);
    assertEquals(4.0, computeVariance(covmat2x2PositiveCorrelation, matrix2by1 (1,  0)), 1e-4);
    assertEquals(1.5, computeVariance(covmat2x2PositiveCorrelation, matrix2by1 (0,  1)), 1e-4);
    assertEquals(
        doubleExplained(7.5, 4 + 1 + 1 + 1.5),
        computeVariance(covmat2x2PositiveCorrelation, matrix2by1 ( 1, 1)), 1e-4);
    assertEquals(
        doubleExplained(7.5, 4 + 1 + 1 + 1.5),
        computeVariance(covmat2x2PositiveCorrelation, matrix2by1 (-1,-1)), 1e-4);
    assertEquals(
        doubleExplained(3.5, 4 - 1 - 1 + 1.5),
        computeVariance(covmat2x2PositiveCorrelation, matrix2by1 (1, -1)), 1e-4);
    assertEquals(
        doubleExplained(3.5, 4 - 1 - 1 + 1.5),
        computeVariance(covmat2x2PositiveCorrelation, matrix2by1 (-1, 1)), 1e-4);
    assertEquals(
        doubleExplained(13.5, 16 - 2 - 2 + 1.5),
        computeVariance(covmat2x2PositiveCorrelation, matrix2by1 (-2, 1)), 1e-4);
    assertEquals(
        doubleExplained(21.5, 16 + 2 + 2 + 1.5),
        computeVariance(covmat2x2PositiveCorrelation, matrix2by1 (2,  1)), 1e-4);
    // Check with negative correlation
    assertEquals(
        doubleExplained(3.5, 4 - 1 - 1 + 1.5),
        computeVariance(covmat2x2NegativeCorrelation, matrix2by1 ( 1,  1)), 1e-4);
    assertEquals(
        doubleExplained(3.5, 4 - 1 - 1 + 1.5),
        computeVariance(covmat2x2NegativeCorrelation, matrix2by1 (-1, -1)), 1e-4);
    assertEquals(
        doubleExplained(7.5, 4 + 1 + 1 + 1.5),
        computeVariance(covmat2x2NegativeCorrelation, matrix2by1 ( 1, -1)), 1e-4);
    assertEquals(
        doubleExplained(7.5, 4 + 1 + 1 + 1.5),
        computeVariance(covmat2x2NegativeCorrelation, matrix2by1 (-1,  1)), 1e-4);
    assertEquals(
        doubleExplained(21.5, 16 + 2 + 2 + 1.5),
        computeVariance(covmat2x2NegativeCorrelation, matrix2by1 (-2,  1)), 1e-4);
    assertEquals(
        doubleExplained(13.5, 16 - 2 - 2 + 1.5),
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
    assertEquals(
        doubleExplained(29.98, 3 * 3 * 3 + 2 * 1 * 1 + 0.7 * 0.7 * 2),
        computeVariance(rbDiagonalMatrix3by3(3, 2, 2), matrix3by1(3, 1, 0.7)), 1e-4);

    // 3 x 3 non-diagonal matrix.
    // Remember we don't care if this is a valid covariance matrix...we are testing the multiplication.
    RBMatrix covmat3x3 = rbMatrix3by3(
        3.0,  1.0, -1.0,
        1.0,  1.5,  0.6,
        -1.0, 0.6,  0.4);
    assertEquals(
        3.0,
        computeVariance(covmat3x3, matrix3by1(-1, 0, 0)), 1e-4);
    // Check a few cases with only 2 loadings
    assertEquals(
        doubleExplained(13, 3 * -1 * -1 + 1.5 * -2 * -2 + -2 * 1 * -1 + -2 * 1 * -1),
        computeVariance(covmat3x3, matrix3by1(-1, -2, 0)), 1e-4);
    assertEquals(
        doubleExplained(13, 3 + 1.5 * 2 * 2 + 2 + 2),
        computeVariance(covmat3x3, matrix3by1(1, 2, 0)), 1e-4);
    assertEquals(
        doubleExplained(5, 3 + 1.5 * 2 * 2 - 2 - 2),
        computeVariance(covmat3x3, matrix3by1(1, -2, 0)), 1e-4);
    assertEquals(
        doubleExplained(3.1, 1.5 + 0.6 + 0.6 + 0.4),
        computeVariance(covmat3x3, matrix3by1(0, -1, -1)), 1e-4);
    assertEquals(
        doubleExplained(0.7, 1.5 - 0.6 - 0.6 + 0.4),
        computeVariance(covmat3x3, matrix3by1(0, 1, -1)), 1e-4);
    // Check cases with 3 loadings
    // This should be the sum of all matrix elements
    assertEquals(6.1, computeVariance(covmat3x3, matrix3by1(1, 1, 1)), 1e-4);
    assertEquals(6.1, computeVariance(covmat3x3, matrix3by1(-1, -1, -1)), 1e-4);
    assertEquals(
        doubleExplained(20.7, 12 + 2 + 2 + 2 + 1.5 - 0.6 + 2 - 0.6 + 0.4),
        computeVariance(covmat3x3, matrix3by1(2.0, 1, -1)), 1e-4);
  }

  @Test
  public void testIsAlmostIdentityMatrix() {
    double largeEpsilon = 1e-4;
    double smallEpsilon = 1e-8;

    // Identity equals identity
    assertTrue(isAlmostIdentityMatrix(identityRBSquareMatrix(1), epsilon(smallEpsilon)));
    assertTrue(isAlmostIdentityMatrix(identityRBSquareMatrix(2), epsilon(smallEpsilon)));
    assertTrue(isAlmostIdentityMatrix(identityRBSquareMatrix(4), epsilon(smallEpsilon)));
    assertTrue(isAlmostIdentityMatrix(identityRBSquareMatrix(10), epsilon(smallEpsilon)));

    // epsilon tests
    assertTrue(isAlmostIdentityMatrix(
        rbMatrix2by2(1.0 + smallEpsilon, 0.0, 0.0, 1.0 - smallEpsilon),
        epsilon(largeEpsilon)));
    assertTrue(isAlmostIdentityMatrix(
        rbMatrix2by2(3.0, -4.0, 8.0, 0.0),
        epsilon(10.0))); // Huge epsilon is forgiving
    assertFalse(isAlmostIdentityMatrix(
        rbMatrix2by2(1.0 + smallEpsilon, 0.0, 0.0, 1.0 - largeEpsilon),
        epsilon(smallEpsilon)));
    assertFalse(isAlmostIdentityMatrix(
        rbMatrix2by2(1.0 + 2 * smallEpsilon, 0.0, 0.0, 1.0 - smallEpsilon),
        epsilon(smallEpsilon)));

    // Identity matrix
    assertTrue(isAlmostIdentityMatrix(rbMatrix2by2(1.0, 0.0, 0.0, 1.0), epsilon(largeEpsilon)));
    // Lots of non-identity matrices
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2( 1.0,  0.0,  2 * largeEpsilon, 1.0), epsilon(largeEpsilon)));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2( 2.0,  0.0,  0.0,  2.0), epsilon(largeEpsilon)));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0,  0.0,  0.0, -1.0), epsilon(largeEpsilon)));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0,  0.0,  0.0, -2.0), epsilon(largeEpsilon)));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0,  0.1,  0.1,  1.0), epsilon(largeEpsilon)));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0, -0.1, -0.1,  1.0), epsilon(largeEpsilon)));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0,  1.0,  1.0,  1.0), epsilon(largeEpsilon)));

    // not square
    assertFalse(isAlmostIdentityMatrix(
        rbMatrix(new double[][] {
            { 1.0, 2.0 } }),
        epsilon(largeEpsilon)));
  }

  @Test
  public void testIsSymmetric() {
    // a 1x1 matrix is symmetric
    assertTrue(isSymmetricMatrix(singletonRBMatrix(DUMMY_DOUBLE), DEFAULT_EPSILON_1e_8));
    // a diagonal matrix is symmetric
    assertTrue(isSymmetricMatrix(rbDiagonalMatrix3by3(123, -234, 567), DEFAULT_EPSILON_1e_8));
    // general case - symmetric
    assertTrue(isSymmetricMatrix(
        rbMatrix(new double[][] {
            { 1,  2 },
            { 2, -3 } }),
        DEFAULT_EPSILON_1e_8));
    // general case - epsilon symmetric
    assertTrue(isSymmetricMatrix(
        rbMatrix(new double[][] {
            { 1,  2 + 1e-9 },
            { 2, -3 } }),
        DEFAULT_EPSILON_1e_8));
    // general case - asymmetric
    assertFalse(isSymmetricMatrix(
        rbMatrix(new double[][] {
            { 1,   2 },
            { 567, 3 } }),
        DEFAULT_EPSILON_1e_8));

    // a 2 x 3 matrix is not symmetric
    assertFalse(isSymmetricMatrix(
        rbMatrix(new double[][] {
            { 1, 2, 3 },
            { 4, 5, 6 } }),
        DEFAULT_EPSILON_1e_8));

    // a 3 x 2 matrix is not symmetric
    assertFalse(isSymmetricMatrix(
        rbMatrix(new double[][] {
            { 1, 2 },
            { 3, 4 },
            { 5, 6 } }),
        DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testIsPositiveSemiDefiniteSymmetric() {
    // square but not symmetric
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(rbSquareMatrix(new double[][] {
        { DUMMY_POSITIVE_DOUBLE,                 123.4 },
        { 567.8,                 DUMMY_POSITIVE_DOUBLE }
    })));

    // simple cases
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(singletonRBSquareMatrix(DUMMY_POSITIVE_DOUBLE)));
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(identityRBSquareMatrix(3)));
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(rbSquareMatrixDiagonal3by3(123, 456, 789)));

    // having one zero diagonal entry is OK; it will introduce 1 eigenvalue = 0, but none will be negative
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(rbSquareMatrixDiagonal3by3(123, 456, 0)));
    // even all zeros are OK; all eigenvalues will be zero, but none will be negative
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(rbSquareMatrixDiagonal3by3(0, 0, 0)));

    // can't have a negative diagonal element (even epsilon negative)
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(rbSquareMatrixDiagonal3by3(123, 456, -1e-9)));

    // make a matrix whose diagonal elements are perfect squares, for clarity in the following checks
    DoubleFunction<RBSquareMatrix> maker2x2 = offDiagonal -> rbSquareMatrix2by2(
        4.0,         offDiagonal,
        offDiagonal,        9.0);
    // Any off-diagonal element with abs(off-diagonal) <= sqrt(4) * sqrt(9) = 6 is fine.
    // This is because abs(e_ij) <= sqrt(e_ii) * sqrt(e_jj); the abs(covariance of variables i and j)
    // must be less than or equal to the sqrt(variance(i)) * sqrt(variance(j)).
    // We use a tiny (1e-14) tolerance for the off-diagonal element being slightly too large.
    for (double offDiagonal : ImmutableList.of(-6.0 - 1e-15, -6.0, -1.0, 0.0, 0.5, 1.0, 6.0, 6.0 + 1e-15)) {
      assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker2x2.apply(offDiagonal)));
    }

    // Here, the off-diagonal elements are larger (in absolute value) than sqrt(diag_i) * sqrt(diag_j).
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker2x2.apply(-6 - 1e-13)));
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker2x2.apply( 6 + 1e-13)));

    // Now investigate off-diagonal elements that follow the above limit of
    // abs(e_ij) <= sqrt(e_ii) * sqrt(e_jj), but still may be non-positive-semi-definite.
    TriFunction<Double, Double, Double, RBSquareMatrix> maker3x3 = (elem12, elem13, elem23) -> rbSquareMatrix3by3(
        4,      elem12, elem13,
        elem12,      9, elem23,
        elem13, elem23,     16);

    // In all the following examples, the off-diagonal elements e_ij <= sqrt(e_ii) * sqrt(e_jj),
    // so that condition is satisfied.

    // In the following, the single non-zero off-diagonal element is as large as possible,
    // e.g. e_ij = sqrt(e_ii) * sqrt(e_jj), and the matrix is positive semi-definite.
    // Each matrix will have one eigenvalue of 0, so they barely pass the "no negative eigenvalues" test.
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(
        doubleExplained(6.0, Math.sqrt(4) * Math.sqrt(9)), 0.0,  0.0)));
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(
        0.0, doubleExplained(8.0, Math.sqrt(4) * Math.sqrt(16)), 0.0)));
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(
        0.0, 0.0, doubleExplained(12.0, Math.sqrt(9) * Math.sqrt(16)))));

    // As above, but with a second non-zero off-diagonal element e_ij. Even though the second
    // non-zero element is small, they still push the smallest eigenvalue below 0.0
    // and make the matrix non-positive-semi-definite.
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(6.0, 0.1,  0.0)));
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(6.0, 0.0,  0.1)));
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(0.1, 8.0,  0.0)));
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(0.0, 8.0,  0.1)));
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(0.1, 0.0, 12.0)));
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(0.0, 0.1, 12.0)));

    // For "smallish" non-zero off-diagonals, all the eigenvalues are still non-negative,
    // which guarantees that the matrix is positive semi-definite. Here we check off-diagonal
    // elements up to 40% of their maximum magnitude.
    // The goal here is not to figure out the maximum allowable ratio that can still be PSD,
    // but to show that *some* fractions result in PSD matrices.
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(0.0, 0.0, 0.0)));
    for (double ratio : ImmutableList.of(-0.4, -0.2, -0.1, 0.0, 0.1, 0.2, 0.4)) {
      assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply( 6.0 * ratio,  8.0 * ratio,  12.0 * ratio)));
      assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply( 6.0 * ratio,  8.0 * ratio, -12.0 * ratio)));
      assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply( 6.0 * ratio, -8.0 * ratio,  12.0 * ratio)));
      assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(-6.0 * ratio,  8.0 * ratio,  12.0 * ratio)));
    }

    // With a single large off-diagonal element, the other off-diagonal elements must be small.
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(5.9, 1.0,  1.0)));
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(1.0, 7.9,  1.0)));
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(1.0, 1.0, 11.9)));

    // Having 2 off-diagonal elements close to their max does not work.
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(5.9, 7.9,  0.0)));
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(5.9, 0.0, 11.9)));
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(0.0, 7.9, 11.9)));

    // With 1 off-diagonal element close to its max, the others can't be too large. Positive
    // signs for the small off-diagonal elements yield positive semi-definite matrices.
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply( 5.9,  1.0,   1.0)));
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply( 1.0,  7.9,   1.0)));
    assertTrue(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply( 1.0,  1.0,  11.9)));

    // However, changing the signs of the "small" off-diagonal elements is enough to make
    // the matrices non-positive-semi-definite.
    // The point of these checks is not to come up with rules for how large the off-diagonal elements can be.
    // Rather, the point is to demonstrate that it is not enough to rely on the simple test of
    // abs(e_ij) <= sqrt(e_ii) * sqrt(i_jj).
    // It's a simple check to do, but it doesn't catch all non-PSD matrices. For that, we need
    // to do the (more computationally intensive) check that all eigenvalues are non-negative.
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply( 5.9,  1.0, -1.0)));
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply( 5.9, -1.0,  1.0)));
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply( 1.0,  7.9, -1.0)));
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(-1.0,  7.9,  1.0)));
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply( 1.0, -1.0, 11.9)));
    assertFalse(isPositiveSemiDefiniteSymmetricMatrix(maker3x3.apply(-1.0,  1.0, 11.9)));
  }

}
