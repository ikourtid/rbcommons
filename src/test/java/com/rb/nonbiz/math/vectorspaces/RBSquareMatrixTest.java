package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import com.rb.nonbiz.testutils.Epsilons;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.vectorspaces.RBMatrix.rbDiagonalMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrix.rbIdentityMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrix.rbMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbMatrix2by2;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbMatrixMatcher;
import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrix.rbDiagonalSquareMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrix.rbIdentitySquareMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBVectorTest.rbVector;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertIndexOutOfBoundsException;
import static com.rb.nonbiz.testutils.Epsilons.emptyEpsilons;
import static com.rb.nonbiz.testutils.Epsilons.useEpsilonEverywhere;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class RBSquareMatrixTest extends RBTestMatcher<RBSquareMatrix> {

  public static RBSquareMatrix singletonRBSquareMatrix(double onlyValue) {
    return rbSquareMatrix(new double[][] { { onlyValue } });
  }

  public static RBSquareMatrix rbSquareMatrixIdentity(int n) {
    return RBSquareMatrix.rbSquareMatrix(rbIdentityMatrix(n));
  }

  public static RBSquareMatrix rbSquareMatrixDiagonal(RBVector rbVector) {
    return RBSquareMatrix.rbSquareMatrix(rbDiagonalMatrix(rbVector));
  }

  public static RBSquareMatrix rbSquareMatrixDiagonal2by2(double a11, double a22) {
    return rbSquareMatrix2by2(
        a11,  0,
        0,  a22);
  }

  public static RBSquareMatrix rbSquareMatrixDiagonal3by3(double a11, double a22, double a33) {
    return rbSquareMatrix3by3(
        a11,  0,   0,
        0,  a22,   0,
        0,    0, a33);
  }

  public static RBSquareMatrix rbSquareMatrix2by2(double a11, double a12, double a21, double a22) {
    return rbSquareMatrix(new double[][] {
        { a11, a12 },
        { a21, a22 } });
  }

  public static RBSquareMatrix rbSquareMatrix3by3(
      double a11, double a12, double a13,
      double a21, double a22, double a23,
      double a31, double a32, double a33) {
    return rbSquareMatrix(new double[][] {
        { a11, a12, a13 },
        { a21, a22, a23 },
        { a31, a32, a33 } });
  }

  public static RBSquareMatrix rbSquareMatrix(double[][] values) {
    return RBSquareMatrix.rbSquareMatrix(rbMatrix(new DenseDoubleMatrix2D(values)));
  }

  @Test
  public void notSquare_throws() {
    assertIllegalArgumentException(() -> rbSquareMatrix(new double[][] {
        { 1.1 },
        { 3.1 } }));
    assertIllegalArgumentException(() -> rbSquareMatrix(new double[][] {
        { 1.1, 2.1 } }));
    assertIllegalArgumentException(() -> rbSquareMatrix(new double[][] {
        { 1.1, 2.1, 3.1 },
        { 4.1, 5.1, 6.1 } }));
    assertIllegalArgumentException(() -> rbSquareMatrix(new double[][] {
        { 1.1, 2.1 },
        { 3.1, 4.1 },
        { 5.1, 6.1 } }));

    RBSquareMatrix doesNotThrow;
    doesNotThrow = rbSquareMatrix(new double[][] {
        { 1.1, 2.1 },
        { 3.1, 4.1 } });
    doesNotThrow = rbSquareMatrix(new double[][] {
        { 1.1, 2.1, 3.1 },
        { 4.1, 5.1, 6.1 },
        { 7.1, 8.1, 9.1 } });
  }

  @Test
  public void testIdentityMatrix() {
    assertThat(
        rbIdentitySquareMatrix(2),
        rbSquareMatrixMatcher(rbSquareMatrix2by2(
            1, 0,
            0, 1)));

    assertThat(
        rbIdentitySquareMatrix(3),
        rbSquareMatrixMatcher(rbSquareMatrix3by3(
            1, 0, 0,
            0, 1, 0,
            0, 0, 1)));
  }

  @Test
  public void testDiagonalMatrix() {
    assertThat(
        rbDiagonalSquareMatrix(rbVector(77, 88, 99)),
        rbSquareMatrixMatcher(rbSquareMatrix3by3(
            77, 0, 0,
            0, 88, 0,
            0, 0, 99)));
  }

  @Test
  public void testGetIJ() {
    RBSquareMatrix matrix = RBSquareMatrix.rbSquareMatrix(rbMatrix2by2(
        1.0, 2.0,
        3.0, 4.0));
    assertEquals(1.0, matrix.get(0, 0), 1e-8);
    assertEquals(2.0, matrix.get(0, 1), 1e-8);
    assertEquals(3.0, matrix.get(1, 0), 1e-8);
    assertEquals(4.0, matrix.get(1, 1), 1e-8);

    // no negative indices
    assertIndexOutOfBoundsException( () -> matrix.get(-1,  0));
    assertIndexOutOfBoundsException( () -> matrix.get( 0, -1));
    // indices can't be too large
    assertIndexOutOfBoundsException( () -> matrix.get(0, 2));
    assertIndexOutOfBoundsException( () -> matrix.get(2, 0));
  }

  @Override
  public RBSquareMatrix makeTrivialObject() {
    return singletonRBSquareMatrix(0);
  }

  @Override
  public RBSquareMatrix makeNontrivialObject() {
    return rbSquareMatrix2by2(
        1.1, 2.1,
        3.1, 4.1);
  }

  @Override
  public RBSquareMatrix makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbSquareMatrix2by2(
        1.1 + e, 2.1 + e,
        3.1 + e, 4.1 + e);
  }

  @Override
  protected boolean willMatch(RBSquareMatrix expected, RBSquareMatrix actual) {
    return rbSquareMatrixMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<RBSquareMatrix> rbSquareMatrixMatcher(RBSquareMatrix expected) {
    return rbSquareMatrixMatcher(expected, emptyEpsilons());
  }

  public static TypeSafeMatcher<RBSquareMatrix> rbSquareMatrixMatcher(RBSquareMatrix expected, double epsilon) {
    return rbSquareMatrixMatcher(expected, useEpsilonEverywhere(epsilon));
  }

  public static TypeSafeMatcher<RBSquareMatrix> rbSquareMatrixMatcher(RBSquareMatrix expected, Epsilons e) {
    return makeMatcher(expected,
        match(v -> v.getRawMatrixUnsafe(), f -> rbMatrixMatcher(f, e)));
  }

}
