package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBEitherMatchers.eitherMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.types.AcceptableDifference.epsilonAsDiff;
import static com.rb.nonbiz.types.AcceptableDifference.epsilonAsFraction;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.ZERO_EPSILON;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.EpsilonTest.epsilonMatcher;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AcceptableDifferenceTest extends RBTestMatcher<AcceptableDifference> {

  @Test
  public void testEpsilonIsFraction() {
    assertFalse(epsilonAsFraction(unitFraction(0.1)).withinEpsilon(100, 88));
    assertTrue(epsilonAsFraction(unitFraction(0.1)).withinEpsilon(100, 92));
    assertTrue(epsilonAsFraction(unitFraction(0.1)).withinEpsilon(100, 92));
    assertTrue(epsilonAsFraction(unitFraction(0.1)).withinEpsilon(100, 108));
    assertFalse(epsilonAsFraction(unitFraction(0.1)).withinEpsilon(100, 112));
  }

  @Test
  public void testEpsilonIsDiff() {
    assertFalse(epsilonAsDiff(epsilon(2)).withinEpsilon(100, 97.9));
    assertTrue(epsilonAsDiff(epsilon(2)).withinEpsilon(100, 98.1));
    assertTrue(epsilonAsDiff(epsilon(2)).withinEpsilon(100, 100));
    assertTrue(epsilonAsDiff(epsilon(2)).withinEpsilon(100, 101.9));
    assertFalse(epsilonAsDiff(epsilon(2)).withinEpsilon(100, 102.1));
  }

  @Override
  public AcceptableDifference makeTrivialObject() {
    return epsilonAsDiff(ZERO_EPSILON);
  }

  @Override
  public AcceptableDifference makeNontrivialObject() {
    return epsilonAsFraction(unitFraction(0.12345));
  }

  @Override
  public AcceptableDifference makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return epsilonAsFraction(unitFraction(0.12345 + e));
  }

  @Override
  protected boolean willMatch(AcceptableDifference expected, AcceptableDifference actual) {
    return acceptableDifferenceMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<AcceptableDifference> acceptableDifferenceMatcher(AcceptableDifference expected) {
    return makeMatcher(expected,
        match(v -> v.getEpsilons(), f -> eitherMatcher(f,
            f2 -> preciseValueMatcher(f2, DEFAULT_EPSILON_1e_8),
            f3 -> epsilonMatcher(f3))));
  }

}
