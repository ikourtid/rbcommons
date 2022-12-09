package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import com.rb.nonbiz.collections.SimpleArrayIndexMapping;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;
import static com.rb.nonbiz.math.vectorspaces.RBIndexableMatrix.rbIndexableMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBIndexableMatrixTest.rbIndexableMatrixMatcher;
import static com.rb.nonbiz.math.vectorspaces.RBMatrix.rbIdentityMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBVectorTest.rbVector;
import static com.rb.nonbiz.math.vectorspaces.RBVectorTest.rbVectorMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBColtMatchers.matrixMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class RBMatrixTest extends RBTestMatcher<RBMatrix> {

  public static RBMatrix singletonRBMatrix(double onlyValue) {
    return rbMatrix(new double[][] { { onlyValue } });
  }

  public static RBMatrix rbMatrix2by2(double a11, double a12, double a21, double a22) {
    return rbMatrix(new double[][] {
        { a11, a12 },
        { a21, a22 }});
  }

  public static RBMatrix rbMatrix3by3(
      double a11, double a12, double a13,
      double a21, double a22, double a23,
      double a31, double a32, double a33) {
    return rbMatrix(new double[][] {
        { a11, a12, a13 },
        { a21, a22, a23 },
        { a31, a32, a33 }});
  }

  public static RBMatrix rbMatrix(double[][] values) {
    return RBMatrix.rbMatrix(new DenseDoubleMatrix2D(values));
  }

  @Test
  public void emptyMatrix_throws() {
    DoubleMatrix2D emptyRawMatrix = new DenseDoubleMatrix2D(new double[][] { {} });
    assertIllegalArgumentException( () -> RBMatrix.rbMatrix(emptyRawMatrix));
  }

  @Test
  public void matrixMultiplyByIdentity_noChange() {
    RBMatrix matrix2by3 = rbMatrix(new double[][] {
        { 1.1, 2.1, 3.1 },
        { 1.2, 2.2, 3.2 } });
    assertThat(
        matrix2by3.multiply(rbIdentityMatrix(3)),
        rbMatrixMatcher(matrix2by3));
    assertThat(
        rbIdentityMatrix(2).multiply(matrix2by3),
        rbMatrixMatcher(matrix2by3));

    // multiplying an identity matrix by itself results in the same matrix
    assertThat(
        rbIdentityMatrix(3).multiply(rbIdentityMatrix(3)),
        rbMatrixMatcher(rbIdentityMatrix(3)));
  }

  @Test
  public void testMatrixMultiply() {
    RBMatrix matrix1 = rbMatrix2by2(
        1.0, 2.0,
        3.0, 4.0);
    RBMatrix matrix2 = rbMatrix2by2(
        5.0, 6.0,
        7.0, 8.0);
    assertThat(
        matrix1.multiply(matrix2),
        rbMatrixMatcher(rbMatrix(new double[][] {
            { doubleExplained(19, 1 * 5 + 2 * 7), doubleExplained(22, 1 * 6 + 2 * 8) },
            { doubleExplained(43, 3 * 5 + 4 * 7), doubleExplained(50, 3 * 6 + 4 * 8) }})));
  }

  @Test
  public void testMatrixDeterminant() {
    assertEquals(1.0, rbIdentityMatrix(2).determinant(), 1e-8);
    assertEquals(1.0, rbIdentityMatrix(3).determinant(), 1e-8);

    // recall that Det({{a, b}, {c, d}}) = ad - bc
    assertEquals(doubleExplained(-2, 1 * 4 - 2 * 3), rbMatrix2by2(1, 2, 3, 4).determinant(), 1e-8);
    assertEquals(doubleExplained( 7, 5 * 2 - 1 * 3), rbMatrix2by2(5, 1, 3, 2).determinant(), 1e-8);

    // an empty row or column will make the determinant zero
    assertEquals(0.0, rbMatrix2by2(1, 1, 0, 0).determinant(), 1e-8);
    assertEquals(0.0, rbMatrix2by2(0, 0, 1, 1).determinant(), 1e-8);
    assertEquals(0.0, rbMatrix2by2(1, 0, 1, 0).determinant(), 1e-8);
    assertEquals(0.0, rbMatrix2by2(0, 1, 0, 1).determinant(), 1e-8);

    // one row or column being equal to another, or a multiple of another, will cause the determinant to be zero
    assertEquals(0.0, rbMatrix2by2(1,  2,  1,  2).determinant(), 1e-8);
    assertEquals(0.0, rbMatrix2by2(1,  2, 10, 20).determinant(), 1e-8);
    assertEquals(0.0, rbMatrix2by2(1,  1,  2,  2).determinant(), 1e-8);
    assertEquals(0.0, rbMatrix2by2(1, 10,  2, 20).determinant(), 1e-8);
  }

  @Test
  public void matrixMultiplyDimensionsMismatched_throws() {
    RBMatrix matrix = rbMatrix2by2(
        1.0, 2.0,
        3.0, 4.0);
    assertIllegalArgumentException( () -> matrix.multiply(rbIdentityMatrix(3)));
    RBMatrix doesNotThrow = matrix.multiply(rbIdentityMatrix(2));
  }

  @Test
  public void testGetColumn() {
    RBMatrix matrix = rbMatrix2by2(
        1.0, 2.0,
        3.0, 4.0);

    assertThat(
        matrix.getColumnVector(matrixColumnIndex(0)),
        rbVectorMatcher(rbVector(1.0, 3.0)));

    assertThat(
        matrix.getColumnVector(matrixColumnIndex(1)),
        rbVectorMatcher(rbVector(2.0, 4.0)));

    // can't request the third column of a 2 x 2 matrix
    assertIllegalArgumentException( () -> matrix.getColumnVector(matrixColumnIndex(2)));
  }

  @Test
  public void testTranspose() {
    // the transposition of a 1x1 matrix is itself
    assertThat(
        singletonRBMatrix(123.45),
        rbMatrixMatcher(singletonRBMatrix(123.45)));

    // the transposition of an identity matrix is itself
    assertThat(
        rbIdentityMatrix(3).transpose(),
        rbMatrixMatcher(rbIdentityMatrix(3)));

    assertThat(
        rbMatrix2by2(
            1.0, 2.0,
            3.0, 4.0).transpose(),
        rbMatrixMatcher(
            rbMatrix2by2(
                1.0, 3.0,
                2.0, 4.0)));

    // one can transpose non-square matrices
    assertThat(
        rbMatrix(new double[][] {
            { 1.1, 2.1, 3.1 },
            { 1.2, 2.2, 3.2 } }).transpose(),
        rbMatrixMatcher(
            rbMatrix(new double[][] {
                { 1.1, 1.2 },
                { 2.1, 2.2 },
                { 3.1, 3.2 } })));
  }

  @Test
  public void testToIndexableMatrix() {
    assertThat(
        rbMatrix(new double[][] {
            { 1.1, 2.1, 3.1 },
            { 1.2, 2.2, 3.2 } })
            .toIndexableMatrix(simpleArrayIndexMapping(77, 88), simpleArrayIndexMapping("a", "b", "c")),
        rbIndexableMatrixMatcher(
            rbIndexableMatrix(
                new DenseDoubleMatrix2D(new double[][] {
                    { 1.1, 2.1, 3.1 },
                    { 1.2, 2.2, 3.2 } }),
                simpleArrayIndexMapping(77, 88),
                simpleArrayIndexMapping("a", "b", "c"))));
  }

  @Test
  public void testToIndexableMatrixWithTrivialRowMapping() {
    assertThat(
        rbMatrix(new double[][] {
            { 1.1, 2.1, 3.1 },
            { 1.2, 2.2, 3.2 } })
            .toIndexableMatrixWithTrivialRowMapping(simpleArrayIndexMapping("a", "b", "c")),
        rbIndexableMatrixMatcher(
            rbIndexableMatrix(
                new DenseDoubleMatrix2D(new double[][] {
                    { 1.1, 2.1, 3.1 },
                    { 1.2, 2.2, 3.2 } }),
                simpleArrayIndexMapping(0, 1),
                simpleArrayIndexMapping("a", "b", "c"))));
  }

  @Test
  public void testToIndexableMatrixWithTrivialColumnMapping() {
    assertThat(
        rbMatrix(new double[][] {
            { 1.1, 2.1, 3.1 },
            { 1.2, 2.2, 3.2 } })
            .toIndexableMatrixWithTrivialColumnMapping(simpleArrayIndexMapping(77, 88)),
        rbIndexableMatrixMatcher(
            rbIndexableMatrix(
                new DenseDoubleMatrix2D(new double[][] {
                    { 1.1, 2.1, 3.1 },
                    { 1.2, 2.2, 3.2 } }),
                simpleArrayIndexMapping(77, 88),
                simpleArrayIndexMapping(0, 1, 2))));
  }

  @Override
  public RBMatrix makeTrivialObject() {
    return singletonRBMatrix(0);
  }

  @Override
  public RBMatrix makeNontrivialObject() {
    return rbMatrix(new double[][] {
        { 1.1, 2.1, 3.1 },
        { 4.1, 5.1, 6.1 }});
  }

  @Override
  public RBMatrix makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbMatrix(new double[][] {
        { 1.1 + e, 2.1 + e, 3.1 + e },
        { 4.1 + e, 5.1 + e, 6.1 + e }});
  }

  @Override
  protected boolean willMatch(RBMatrix expected, RBMatrix actual) {
    return rbMatrixMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<RBMatrix> rbMatrixMatcher(RBMatrix expected) {
    return rbMatrixMatcher(expected, 1e-8);
  }

  public static TypeSafeMatcher<RBMatrix> rbMatrixMatcher(RBMatrix expected, double epsilon) {
    return makeMatcher(expected,
        match(v -> v.getRawMatrixUnsafe(), f -> matrixMatcher(f, epsilon)));
  }

}
