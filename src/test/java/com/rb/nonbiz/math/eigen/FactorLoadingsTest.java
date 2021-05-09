package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.eigen.FactorLoadings.factorLoadings;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.doubleArrayMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class FactorLoadingsTest extends RBTestMatcher<FactorLoadings> {

  @Override
  public FactorLoadings makeTrivialObject() {
    return factorLoadings(1.1);
  }

  @Override
  public FactorLoadings makeNontrivialObject() {
    return factorLoadings(1.1, 2.2);
  }

  @Override
  public FactorLoadings makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return factorLoadings(1.1 + e, 2.2 + e);
  }

  @Override
  protected boolean willMatch(FactorLoadings expected, FactorLoadings actual) {
    return factorLoadingsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<FactorLoadings> factorLoadingsMatcher(FactorLoadings expected) {
    return makeMatcher(expected, actual ->
        doubleArrayMatcher(expected.getLoadings(), 1e-8).matches(actual.getLoadings()));
  }

}
