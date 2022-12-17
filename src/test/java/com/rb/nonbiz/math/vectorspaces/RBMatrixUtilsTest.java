package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import com.rb.nonbiz.testutils.RBTest;
import junit.framework.TestCase;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.math.vectorspaces.RBMatrix.rbIdentityMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbMatrix2by2;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.singletonRBMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixUtils.isOrthoNormalTransformationMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixUtils.isAlmostIdentityMatrix;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;

public class RBMatrixUtilsTest extends TestCase {

  private DoubleMatrix2D matrix2by2(double first, double second, double third, double fourth) {
    return rbMatrix2by2(first, second, third, fourth).getRawMatrixUnsafe();
  }

  private DoubleMatrix2D matrix2by1(double first, double second) {
    return new DenseDoubleMatrix2D(new double[][] { { first }, { second } });
  }

  private DoubleMatrix2D singletonMatrix(double first) {
    return singletonRBMatrix(first).getRawMatrixUnsafe();
  }

  // Given raw loadings and a matrix, compute variance.  variance = loadings' * COVMAT * loadings
  public double computeVariance(DoubleMatrix2D covarianceMatrix, DoubleMatrix2D rawLoadings) {
    Algebra algebra = new Algebra();
    // We are getting variance as a double, not a 1 x 1 matrix, hence using .get(0, 0).
    return algebra.mult(algebra.transpose(rawLoadings), algebra.mult(covarianceMatrix, rawLoadings)).get(0,0);
  }

  @Test
  public void testIsOrthoNormalTransformationMatrixWithCovariance() {
    // Covariance matrix with reasonably strong positive correlation
    DoubleMatrix2D covMat = matrix2by2(
        1.0, 0.5,
        0.5, 1.0);
    double epsilon = 1e-4;
    // If interested, orthToRaw comes from:
    //   DoubleMatrix2D orthToRaw = new Algebra().inverse(matrix2by2(0.866, 0.866, -0.5, 0.5));
    // For now this matrix may seem arbitrary, but we check it below
    DoubleMatrix2D orthToRaw = matrix2by2(
        0.57737, -1,
        0.57737,1);
    assertTrue(isOrthoNormalTransformationMatrix(orthToRaw, covMat, epsilon));
    // Below is valid, along with equivelent transforms which negate one side
    assertTrue(isOrthoNormalTransformationMatrix(matrix2by2(0.57737, -1, 0.57737,1), covMat, epsilon));
    assertTrue(isOrthoNormalTransformationMatrix(matrix2by2(0.57737, 1, 0.57737,-1), covMat, epsilon));
    assertTrue(isOrthoNormalTransformationMatrix(matrix2by2(-0.57737, -1, -0.57737,1), covMat, epsilon));
    // Switching top and bottom is the same as well, even if we negate one side
    assertTrue(isOrthoNormalTransformationMatrix(matrix2by2(0.57737, -1, 0.57737,1), covMat, epsilon));
    assertTrue(isOrthoNormalTransformationMatrix(matrix2by2( 0.57737,1, 0.57737, -1), covMat, epsilon));
    assertTrue(isOrthoNormalTransformationMatrix(matrix2by2( 0.57737,-1, 0.57737, 1), covMat, epsilon));

    // Making a large change to the transormation makes in invalid
    for (double delta : newRBSet(-0.1, 0.1)) {
      assertTrue(isOrthoNormalTransformationMatrix(matrix2by2(0.57737, -1, 0.57737,1), covMat, epsilon));
      assertFalse(isOrthoNormalTransformationMatrix(matrix2by2(0.57737 + delta, -1, 0.57737, 1), covMat, epsilon));
      assertFalse(isOrthoNormalTransformationMatrix(matrix2by2(0.57737, -1 + delta, 0.57737, 1), covMat, epsilon));
      assertFalse(isOrthoNormalTransformationMatrix(matrix2by2(0.57737, -1, 0.57737 + delta, 1), covMat, epsilon));
      assertFalse(isOrthoNormalTransformationMatrix(matrix2by2(0.57737, -1, 0.57737, 1 + delta), covMat, epsilon));
    }

    //
    // Now, use different method to check our orthogonal to raw matrix is actually reasonable.
    // Below we use the axiomatic definition: variance(A) + variance(B) = variance(A+B)
    // For 2x2, it's enough to check that:
    //   variance( [1, 0]) = variance( [0, 1] ) = 1.0
    //   variance( [1, 1]) = 2.0
    assertEquals(computeVariance(covMat, new Algebra().mult(orthToRaw, matrix2by1(1,0))), 1.0, epsilon);
    assertEquals(computeVariance(covMat, new Algebra().mult(orthToRaw, matrix2by1(0,1))), 1.0, epsilon);
    assertEquals(computeVariance(covMat, new Algebra().mult(orthToRaw, matrix2by1(1,1))), 2.0, epsilon);
  }
  @Test
  public void testIsOrthoNormalTransformationMatrixNoCovariance() {
    DoubleMatrix2D identity2x2 = rbIdentityMatrix(2).getRawMatrixUnsafe();

    // One by one matrices are easiest
    assertTrue(isOrthoNormalTransformationMatrix(singletonMatrix(1), singletonMatrix(1), 1e-4));
    // Stretch transformation matrix one way, covariance the other, so OK.  Flip sign is also OK.
    assertTrue(isOrthoNormalTransformationMatrix(singletonMatrix(1 / Math.sqrt(2.0)), singletonMatrix(2), 1e-4));
    assertTrue(isOrthoNormalTransformationMatrix(singletonMatrix(-1 / Math.sqrt(2.0)), singletonMatrix(2), 1e-4));
    // Mismatch but epsilon is huge so OK
    assertTrue(isOrthoNormalTransformationMatrix(singletonMatrix(5.0), singletonMatrix(1), 100));

    // Mismatch.  Flip sign is OK but stretches don't match
    assertFalse(isOrthoNormalTransformationMatrix(singletonMatrix(-1), singletonMatrix(2), 1e-4));

    // Identity covariance matrix and transformation matrix are always orthonormal
    assertTrue(isOrthoNormalTransformationMatrix(matrix2by2(1, 0, 0, 1), identity2x2, 1e-4));
    // Flipping a factor is ortho-normal if covariance is identity
    assertTrue(isOrthoNormalTransformationMatrix(matrix2by2(0, 1, 1, 0), identity2x2, 1e-4));
    assertTrue(isOrthoNormalTransformationMatrix(matrix2by2(0, 1, -1, 0), identity2x2, 1e-4));
    assertTrue(isOrthoNormalTransformationMatrix(matrix2by2(-1, 0, 0, 1), identity2x2, 1e-4));
    assertTrue(isOrthoNormalTransformationMatrix(matrix2by2(-1, 0, 0, -1), identity2x2, 1e-4));
    // Now covariance matrix is not identity, so a transformation matrix that switches factors or is identity is not orthonormal
    assertFalse(
        isOrthoNormalTransformationMatrix(
            matrix2by2( 0,     1,   1,   0),
            matrix2by2( 4.0, 1.0, 1.0, 0.5),
            1e-4));
    assertFalse(
        isOrthoNormalTransformationMatrix(
            matrix2by2(   1,   0,   0,   1),
            matrix2by2( 4.0, 1.0, 1.0, 0.5),
            1e-4));

    // Mismatch...if the covariance matrix stretches one factor, the transformation has to undo the stretch
    assertFalse(isOrthoNormalTransformationMatrix(
        matrix2by2(1.0 ,0, 0, 1),
        matrix2by2(4.0, 0, 0, 1),
        1e-4));
    // OK...transformation matrix stretches first factor to compensate for higher variance.
    assertTrue(isOrthoNormalTransformationMatrix(
        matrix2by2(0.5, 0, 0, 1),
        matrix2by2(4.0, 0, 0, 1),
        1e-4));
    // OK...same as above but flip a sign
    assertTrue(isOrthoNormalTransformationMatrix(
        matrix2by2(-0.5, 0, 0, 1),
        matrix2by2(4.0,  0, 0, 1),
        1e-4));
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
        rbMatrix2by2(1.0+smallEpsilon, 0.0, 0.0, 1.0 - smallEpsilon),
        largeEpsilon));
    assertTrue(isAlmostIdentityMatrix(
        rbMatrix2by2(3.0, -4.0, 8.0, 0.0),
        10.0)); // Huge epsilon is forgiving
    assertFalse(isAlmostIdentityMatrix(
        rbMatrix2by2(1.0+smallEpsilon, 0.0, 0.0, 1.0 - largeEpsilon),
        smallEpsilon));
    assertFalse(isAlmostIdentityMatrix(
        rbMatrix2by2(1.0+2*smallEpsilon, 0.0, 0.0, 1.0-smallEpsilon),
        smallEpsilon));

    // Identity matrix
    assertTrue(isAlmostIdentityMatrix(rbMatrix2by2(1.0,    0.0, 0.0,            1.0),  largeEpsilon));
    // Lots of non-identity matrices
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(1.0,   0.0, 2*largeEpsilon, 1.0),  largeEpsilon));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(2.0,   0.0, 0.0,            2.0),  largeEpsilon));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0,  0.0, 0.0,            -1.0), largeEpsilon));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0,  0.0, 0.0,            -2.0), largeEpsilon));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0,  0.1, 0.1,            1.0),  largeEpsilon));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0, -0.1, -0.1,           1.0),  largeEpsilon));
    assertFalse(isAlmostIdentityMatrix(rbMatrix2by2(-1.0,  1.0, 1.0,            1.0),  largeEpsilon));
  }

}