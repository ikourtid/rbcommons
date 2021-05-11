package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBIncreasingSet.emptyRBIncreasingSet;
import static com.rb.nonbiz.collections.RBIncreasingSet.rbIncreasingSetOf;
import static com.rb.nonbiz.collections.RBIncreasingSet.singletonRBIncreasingSet;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

// This test class is not generic, but the publicly exposed static matcher is.
public class RBIncreasingSetTest extends RBTestMatcher<RBIncreasingSet<Double>> {

  @Test
  public void hasDuplicates_throws() {
    assertIllegalArgumentException( () -> rbIncreasingSetOf(7, 7));
    assertIllegalArgumentException( () -> rbIncreasingSetOf(6, 7, 7));
    RBIncreasingSet<Integer> doesNotThrow;
    doesNotThrow = singletonRBIncreasingSet(7);
    doesNotThrow = rbIncreasingSetOf(7, 8);
    doesNotThrow = rbIncreasingSetOf(6, 7, 8);
  }

  @Test
  public void notIncreasing_throws() {
    assertIllegalArgumentException( () -> rbIncreasingSetOf(6, 8, 7));
    assertIllegalArgumentException( () -> rbIncreasingSetOf(8, 6, 7));
    assertIllegalArgumentException( () -> rbIncreasingSetOf(8, 7));
    RBIncreasingSet<Integer> doesNotThrow;
    doesNotThrow = singletonRBIncreasingSet(7);
    doesNotThrow = rbIncreasingSetOf(7, 8);
    doesNotThrow = rbIncreasingSetOf(6, 7, 8);
    doesNotThrow = rbIncreasingSetOf(6, 7, 8, 9);
    doesNotThrow = rbIncreasingSetOf(6, 7, 8, 9, 10);
    doesNotThrow = rbIncreasingSetOf(6, 7, 8, 9, 10, 11);
    doesNotThrow = rbIncreasingSetOf(6, 7, 8, 9, 10, 11, 12);
    doesNotThrow = rbIncreasingSetOf(6, 7, 8, 9, 10, 11, 12, 13);
    doesNotThrow = rbIncreasingSetOf(6, 7, 8, 9, 10, 11, 12, 13, 14);
    doesNotThrow = rbIncreasingSetOf(6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
  }

  @Override
  public RBIncreasingSet<Double> makeTrivialObject() {
    return emptyRBIncreasingSet();
  }

  @Override
  public RBIncreasingSet<Double> makeNontrivialObject() {
    return rbIncreasingSetOf(-20.0, 0.0, 10.0);
  }

  @Override
  public RBIncreasingSet<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbIncreasingSetOf(-20.0 + e, 0.0 + e, 10.0 + e);
  }

  @Override
  protected boolean willMatch(RBIncreasingSet<Double> expected, RBIncreasingSet<Double> actual) {
    return rbIncreasingSetMatcher(expected, f -> doubleAlmostEqualsMatcher(f, 1e-8)).matches(actual);
  }

  public static <T extends Comparable<? super T>> TypeSafeMatcher<RBIncreasingSet<T>> rbIncreasingSetEqualityMatcher(
      RBIncreasingSet<T> expected) {
    return rbIncreasingSetMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <T extends Comparable<? super T>> TypeSafeMatcher<RBIncreasingSet<T>> rbIncreasingSetMatcher(
      RBIncreasingSet<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.iterator(), f -> iteratorMatcher(f, matcherGenerator)));
  }
  
}