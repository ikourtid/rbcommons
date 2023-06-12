package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Epsilon;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSupplier.rbSupplierReturningValidValue;
import static com.rb.nonbiz.collections.RBSupplier.rbSupplierThrowingException;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

// This test class is not generic, but the prod class and static matcher are.
public class RBSupplierTest extends RBTestMatcher<RBSupplier<Double>> {

  @Test
  public void testPresentItem() {
    assertEquals("foo", rbSupplierReturningValidValue("foo").get());
  }

  @Test
  public void testThrows() {
    assertIllegalArgumentException( () -> rbSupplierThrowingException().get());
  }

  @Override
  public RBSupplier<Double> makeTrivialObject() {
    return rbSupplierReturningValidValue(0.0);
  }

  @Override
  public RBSupplier<Double> makeNontrivialObject() {
    return rbSupplierReturningValidValue(1.23);
  }

  @Override
  public RBSupplier<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbSupplierReturningValidValue(1.23 + e);
  }

  @Override
  protected boolean willMatch(RBSupplier<Double> expected, RBSupplier<Double> actual) {
    return rbSupplierMatcher(expected, f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <T> TypeSafeMatcher<RBSupplier<T>> rbSupplierMatcher(
      RBSupplier<T> expected,
      MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        matchOptional(v -> v.getRawOptionalUnsafe(), matcherGenerator));
  }

}
