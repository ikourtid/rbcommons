package com.rb.nonbiz.math.eigen;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.eigen.Eigenvalue.eigenvalue;
import static com.rb.nonbiz.math.eigen.Eigenvalue.possiblyNegativeEigenvalue;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.impreciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class EigenvalueTest extends RBTestMatcher<Eigenvalue> {

  @Test
  public void mustBePositive() {
    assertIllegalArgumentException( () -> eigenvalue(-1));
    assertIllegalArgumentException( () -> eigenvalue(-1e-9));
    assertIllegalArgumentException( () -> eigenvalue(0));
    Eigenvalue doesNotThrow;
    doesNotThrow = eigenvalue(1e-9);
    doesNotThrow = eigenvalue(0.1);
    doesNotThrow = eigenvalue(1);
    doesNotThrow = eigenvalue(1_000);

    ImmutableList.of(-1.0, -1e-9, 0.0, 1e-9, 0.1, 1.0, 1_000.0)
        .forEach(v -> {
          Eigenvalue alsoDoesNotThrow = possiblyNegativeEigenvalue(v);
        });
  }

  @Override
  public Eigenvalue makeTrivialObject() {
    return eigenvalue(1.0);
  }

  @Override
  public Eigenvalue makeNontrivialObject() {
    return eigenvalue(5.678);
  }

  @Override
  public Eigenvalue makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return eigenvalue(5.678 + e);
  }

  @Override
  public boolean willMatch(Eigenvalue expected, Eigenvalue actual) {
    return eigenvalueMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<Eigenvalue> eigenvalueMatcher(Eigenvalue expected) {
    return impreciseValueMatcher(expected, 1e-8);
  }

}
