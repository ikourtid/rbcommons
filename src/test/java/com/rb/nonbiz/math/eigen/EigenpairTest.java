package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.eigen.Eigenpair.eigenpair;
import static com.rb.nonbiz.math.eigen.Eigenvalue.eigenvalue;
import static com.rb.nonbiz.math.eigen.EigenvectorTest.eigenvector;
import static com.rb.nonbiz.math.eigen.EigenvectorTest.eigenvectorMatcher;
import static com.rb.nonbiz.math.eigen.EigenvectorTest.singletonEigenvector;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingImpreciseAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class EigenpairTest extends RBTestMatcher<Eigenpair> {

  @Test
  public void happyPath() {
    Eigenpair doesNotThrow = eigenpair(eigenvalue(10.0), eigenvector(1.1, 2.2));
  }

  @Test
  public void hasZeroEigenvalue_throws() {
    assertIllegalArgumentException( () -> eigenpair(eigenvalue(0.0), eigenvector(1.1, 2.2)));
  }

  @Test
  public void hasNegativeEigenvalue_throws() {
    assertIllegalArgumentException( () -> eigenpair(eigenvalue(-10.0), eigenvector(1.1, 2.2)));
  }

  @Test
  public void hasNoElementsInEigenvector_throws() {
    assertIllegalArgumentException( () -> eigenpair(eigenvalue(10.0), Eigenvector.eigenvector(new double[] {})));
  }

  @Test
  public void testToString() {
    assertEquals(
        "eigenvalue= 10.98765432 ; eigenvector= [  0.70710678,  0.70710678 ]",
        eigenpair(eigenvalue(10.987654321), eigenvector(10.987654321, 10.987654321)).toString());
  }

  @Override
  public Eigenpair makeTrivialObject() {
    return eigenpair(eigenvalue(1.0), singletonEigenvector(1.0));
  }

  @Override
  public Eigenpair makeNontrivialObject() {
    return eigenpair(eigenvalue(10.0), eigenvector(1.1, 2.2));
  }

  @Override
  public Eigenpair makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return eigenpair(eigenvalue(10.0 + e), eigenvector(1.1 + e, 2.2 + e));
  }

  @Override
  protected boolean willMatch(Eigenpair expected, Eigenpair actual) {
    return eigenpairMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<Eigenpair> eigenpairMatcher(Eigenpair expected) {
    return makeMatcher(expected,
        matchUsingImpreciseAlmostEquals(v -> v.getEigenvalue(), 1e-8),
        match(v -> v.getEigenvector(), f -> eigenvectorMatcher(f)));
  }

}
