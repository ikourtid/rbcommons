package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrixTest.rbSquareMatrix2by2;
import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrixTest.rbSquareMatrix3by3;
import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrixTest.rbSquareMatrixMatcher;
import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrixTest.singletonRBSquareMatrix;
import static com.rb.nonbiz.math.vectorspaces.VectorSpaceBasis.vectorSpaceBasis;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class VectorSpaceBasisTest extends RBTestMatcher<VectorSpaceBasis> {

  public static VectorSpaceBasis vectorSpaceBasis1d(double onlyValue) {
    return vectorSpaceBasis(singletonRBSquareMatrix(onlyValue));
  }

  public static VectorSpaceBasis vectorSpaceBasis2d(double a11, double a12, double a21, double a22) {
    return vectorSpaceBasis(rbSquareMatrix2by2(a11, a12, a21, a22));
  }

  public static VectorSpaceBasis vectorSpaceBasis3d(
      double a11, double a12, double a13,
      double a21, double a22, double a23,
      double a31, double a32, double a33) {
    return vectorSpaceBasis(rbSquareMatrix3by3(
        a11, a12, a13,
        a21, a22, a23,
        a31, a32, a33));
  }

  @Test
  public void notLinearlyIndependent_throws() {
    assertIllegalArgumentException( () -> vectorSpaceBasis2d(
        1, 10,
        2, 20));
    // "somewhat" linearly dependent
    assertIllegalArgumentException( () -> vectorSpaceBasis2d(
        1, 10 + 1e-9,
        2, 20));

    assertIllegalArgumentException( () -> vectorSpaceBasis3d(
        1, 10, 100,
        2, 20, 200,
        3, 30, 300));
    assertIllegalArgumentException( () -> vectorSpaceBasis3d(
        1, 10, 100 + 1e-9,
        2, 20, 200,
        3, 30, 300));
  }

  @Test
  public void hasZeroOrAlmostZeroVectors_throws() {
    assertIllegalArgumentException( () -> vectorSpaceBasis1d(0));
    assertIllegalArgumentException( () -> vectorSpaceBasis1d(1e-9));

    assertIllegalArgumentException( () -> vectorSpaceBasis1d(0));
    assertIllegalArgumentException( () -> vectorSpaceBasis1d(1e-9));

    assertIllegalArgumentException( () -> vectorSpaceBasis2d(
        1, 0,
        2, 0));
    assertIllegalArgumentException( () -> vectorSpaceBasis2d(
        0, 1,
        0, 2));
  }

  @Override
  public VectorSpaceBasis makeTrivialObject() {
    return vectorSpaceBasis1d(1);
  }

  @Override
  public VectorSpaceBasis makeNontrivialObject() {
    // These are actually linearly independent
    return vectorSpaceBasis2d(
        1.1, 2.1,
        3.1, 4.1);
  }

  @Override
  public VectorSpaceBasis makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return vectorSpaceBasis2d(
        1.1 + e, 2.1 + e,
        3.1 + e, 4.1 + e);
  }

  @Override
  protected boolean willMatch(VectorSpaceBasis expected, VectorSpaceBasis actual) {
    return vectorSpaceBasisMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<VectorSpaceBasis> vectorSpaceBasisMatcher(VectorSpaceBasis expected) {
    return vectorSpaceBasisMatcher(expected, 1e-8);
  }

  public static TypeSafeMatcher<VectorSpaceBasis> vectorSpaceBasisMatcher(VectorSpaceBasis expected, double epsilon) {
    return makeMatcher(expected,
        match(v -> v.getRbSquareMatrix(), f -> rbSquareMatrixMatcher(f, epsilon)));
  }

}
