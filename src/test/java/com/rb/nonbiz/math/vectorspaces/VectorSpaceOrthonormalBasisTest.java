package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.vectorspaces.VectorSpaceBasisTest.vectorSpaceBasis1d;
import static com.rb.nonbiz.math.vectorspaces.VectorSpaceBasisTest.vectorSpaceBasis2d;
import static com.rb.nonbiz.math.vectorspaces.VectorSpaceBasisTest.vectorSpaceBasis3d;
import static com.rb.nonbiz.math.vectorspaces.VectorSpaceBasisTest.vectorSpaceBasisMatcher;
import static com.rb.nonbiz.math.vectorspaces.VectorSpaceOrthonormalBasis.vectorSpaceOrthonormalBasis;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.junit.Assert.assertEquals;

public class VectorSpaceOrthonormalBasisTest extends RBTestMatcher<VectorSpaceOrthonormalBasis> {

  public static VectorSpaceOrthonormalBasis vectorSpaceOrthonormalBasis1d(double onlyValue) {
    return vectorSpaceOrthonormalBasis(vectorSpaceBasis1d(onlyValue));
  }

  public static VectorSpaceOrthonormalBasis vectorSpaceOrthonormalBasis2d(double a11, double a12, double a21, double a22) {
    return vectorSpaceOrthonormalBasis(vectorSpaceBasis2d(a11, a12, a21, a22));
  }

  public static VectorSpaceOrthonormalBasis vectorSpaceOrthonormalBasis3d(
      double a11, double a12, double a13,
      double a21, double a22, double a23,
      double a31, double a32, double a33) {
    return vectorSpaceOrthonormalBasis(vectorSpaceBasis3d(
        a11, a12, a13,
        a21, a22, a23,
        a31, a32, a33));
  }

  @Test
  public void testSize() {
    assertEquals(1, makeTrivialObject().getNumDimensions());
    assertEquals(3, makeNontrivialObject().getNumDimensions());
  }

  @Test
  public void orthonormalButNotCanonical_2d_works() {
    double e = 1e-9; // epsilon
    VectorSpaceOrthonormalBasis doesNotThrow;
    double v = doubleExplained(0.707106781, Math.sqrt(2) / 2);
    doesNotThrow = vectorSpaceOrthonormalBasis2d(
        v,  v,
        v, -v);
    doesNotThrow = vectorSpaceOrthonormalBasis2d(
        v + e,  v + e,
        v + e, -v + e);
  }

  @Test
  public void orthonormalButNotCanonical_3d_works() {
    double e = 1e-9; // epsilon
    VectorSpaceOrthonormalBasis doesNotThrow;
    double v = doubleExplained(0.707106781, Math.sqrt(2) / 2);
    doesNotThrow = vectorSpaceOrthonormalBasis3d(
        v,  v, 0,
        v, -v, 0,
        0,  0, 1);
    doesNotThrow = vectorSpaceOrthonormalBasis3d(
        v + e,  v + e, 0 + e,
        v + e, -v + e, 0 + e,
        0 + e,  0 + e, 1 + e);
  }

  @Test
  public void nonOrthonormal_2d_throws() {
    double v = doubleExplained(0.707106781, Math.sqrt(2) / 2);
    assertIllegalArgumentException( () -> vectorSpaceOrthonormalBasis2d(
        v, 1,
        v, 0));
    VectorSpaceOrthonormalBasis doesNotThrow = vectorSpaceOrthonormalBasis2d(
        1, 0,
        0, 1);
  }

  @Test
  public void nonOrthonormal_3d_throws() {
    double v = doubleExplained(0.57735026919, 1 / Math.sqrt(3));
    assertIllegalArgumentException( () -> vectorSpaceOrthonormalBasis3d(
        v, 1, 0,
        v, 0, 1,
        v, 0, 0));
    VectorSpaceOrthonormalBasis doesNotThrow = vectorSpaceOrthonormalBasis3d(
        0, 1, 0,
        0, 0, 1,
        1, 0, 0);
  }

  @Override
  public VectorSpaceOrthonormalBasis makeTrivialObject() {
    return vectorSpaceOrthonormalBasis1d(1);
  }

  @Override
  public VectorSpaceOrthonormalBasis makeNontrivialObject() {
    double v = doubleExplained(0.707106781, Math.sqrt(2) / 2);
    return vectorSpaceOrthonormalBasis3d(
        v,  v, 0,
        v, -v, 0,
        0,  0, 1);
  }

  @Override
  public VectorSpaceOrthonormalBasis makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    double v = doubleExplained(0.707106781, Math.sqrt(2) / 2);
    return vectorSpaceOrthonormalBasis3d(
        v + e,  v + e, 0 + e,
        v + e, -v + e, 0 + e,
        0 + e,  0 + e, 1 + e);
  }

  @Override
  protected boolean willMatch(VectorSpaceOrthonormalBasis expected, VectorSpaceOrthonormalBasis actual) {
    return vectorSpaceOrthonormalBasisMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<VectorSpaceOrthonormalBasis> vectorSpaceOrthonormalBasisMatcher(
      VectorSpaceOrthonormalBasis expected) {
    return vectorSpaceOrthonormalBasisMatcher(expected, DEFAULT_EPSILON_1e_8);
  }

  public static TypeSafeMatcher<VectorSpaceOrthonormalBasis> vectorSpaceOrthonormalBasisMatcher(
      VectorSpaceOrthonormalBasis expected, Epsilon epsilon) {
    return makeMatcher(expected,
        match(v -> v.getVectorSpaceBasis(), f -> vectorSpaceBasisMatcher(f, epsilon)));
  }

}
