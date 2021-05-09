package com.rb.nonbiz.math.eigen;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.util.RBPreconditions;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBColtMatchers.matrix1dMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;

public class EigenvectorTest extends RBTestMatcher<Eigenvector> {

  public static Eigenvector singletonEigenvector(double x) {
    return Eigenvector.eigenvector(new DenseDoubleMatrix1D(new double[] { x }));
  }

  public static Eigenvector dummyEigenvectorWithSize(int size) {
    RBPreconditions.checkArgument(size > 0);
    // make a vector [1, DUMMY_DOUBLE, DUMMY_DOUBLE^2, ...] and normalize to length 1
    double[] dummyVector = new double[size];
    for (int i=0; i < size; i++) {
      dummyVector[i] = Math.pow(DUMMY_DOUBLE, i);
    }
    double vectorMagnitude = Math.sqrt(Arrays.stream(dummyVector)
        .map(x -> x * x)
        .sum());
    for (int i = 0; i < size; i++) {
      dummyVector[i] /= vectorMagnitude;
    }
    return Eigenvector.eigenvector(new DenseDoubleMatrix1D(dummyVector));
  }

  public static double vectorNorm(double first, double second, double...rest) {
    double sumSq = first * first + second * second;
    for (int i = 0; i < rest.length; i++) {
      sumSq += rest[i] * rest[i];
    }
    return Math.sqrt(sumSq);
  }

  public static Eigenvector eigenvector(double first, double second, double...rest) {
    double[] values = new double[rest.length + 2];
    double magnitude = vectorNorm(first, second, rest);
    RBPreconditions.checkArgument(
        magnitude > 1e-8,
        "vector must have non-zero length; found %s",
        magnitude);

    values[0] = first / magnitude;
    values[1] = second / magnitude;
    for (int i = 0; i < rest.length; i++) {
      values[i + 2] = rest[i] / magnitude;
    }
    return Eigenvector.eigenvector(new DenseDoubleMatrix1D(values));
  }

  @Override
  public Eigenvector makeTrivialObject() {
    return singletonEigenvector(1.0);
  }

  @Override
  public Eigenvector makeNontrivialObject() {
    // The eigenvectors are normalized, i.e. have a magnitude of 1. I don't check that in the constructor
    // for performance reasons, but it's good to keep these tests realistic.
    doubleExplained(1.0, 0.9 * 0.9 + 0.4 * 0.4 + 3 * 0.1 * 0.1);
    return eigenvector(0.9, -0.4, 0.1, 0.1, -0.1);
  }

  @Override
  public Eigenvector makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return eigenvector(0.9 + e, -0.4 + e, 0.1 + e, 0.1 + e, -0.1 + e);
  }

  @Override
  protected boolean willMatch(Eigenvector expected, Eigenvector actual) {
    return eigenvectorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<Eigenvector> eigenvectorMatcher(Eigenvector expected) {
    return eigenvectorEpsilonMatcher(expected, 1e-8);
  }

  public static TypeSafeMatcher<Eigenvector> eigenvectorEpsilonMatcher(Eigenvector expected, double epsilon) {
    return makeMatcher(expected,
        match(v -> v.getRawMatrix1D(), f -> matrix1dMatcher(f, epsilon)));
  }

}
