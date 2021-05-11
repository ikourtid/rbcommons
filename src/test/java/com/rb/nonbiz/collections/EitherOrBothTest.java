package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Optional;

import static com.rb.nonbiz.collections.EitherOrBoth.both;
import static com.rb.nonbiz.collections.EitherOrBoth.eitherOrBoth;
import static com.rb.nonbiz.collections.EitherOrBoth.rightOnly;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static junit.framework.TestCase.assertEquals;

/**
 * This test class is not generic, but the publicly exposed matcher is.
 */
public class EitherOrBothTest extends RBTestMatcher<EitherOrBoth<UnitFraction, Double>> {

  @Test
  public void implementsEquals() {
    assertEquals(
        eitherOrBoth(Optional.of("a"), Optional.of(1)),
        eitherOrBoth(Optional.of("a"), Optional.of(1)));
    assertEquals(
        eitherOrBoth(Optional.of("a"), Optional.empty()),
        eitherOrBoth(Optional.of("a"), Optional.empty()));
    assertEquals(
        eitherOrBoth(Optional.empty(), Optional.of(1)),
        eitherOrBoth(Optional.empty(), Optional.of(1)));
  }

  @Test
  public void generalConstructor_mustHaveAtLeastOne() {
    assertIllegalArgumentException( () -> eitherOrBoth(Optional.empty(), Optional.empty()));
    EitherOrBoth<String, Integer> doesNotThrow;
    doesNotThrow = eitherOrBoth(Optional.of("a"), Optional.of(1));
    doesNotThrow = eitherOrBoth(Optional.of("a"), Optional.empty());
    doesNotThrow = eitherOrBoth(Optional.empty(), Optional.of(1));
  }

  @Override
  public EitherOrBoth<UnitFraction, Double> makeTrivialObject() {
    return rightOnly(0.0);
  }

  @Override
  public EitherOrBoth<UnitFraction, Double> makeNontrivialObject() {
    return both(unitFraction(0.11), 2.22);
  }

  @Override
  public EitherOrBoth<UnitFraction, Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return both(unitFraction(0.11 + e), 2.22 + e);
  }

  @Override
  protected boolean willMatch(EitherOrBoth<UnitFraction, Double> expected, EitherOrBoth<UnitFraction, Double> actual) {
    return eitherOrBothMatcher(expected, v -> preciseValueMatcher(v, 1e-8), v -> doubleAlmostEqualsMatcher(v, 1e-8))
        .matches(actual);
  }

  public static <L, R> TypeSafeMatcher<EitherOrBoth<L, R>> eitherOrBothMatcher(
      EitherOrBoth<L, R> expected,
      MatcherGenerator<L> leftMatcherGenerator,
      MatcherGenerator<R> rightMatcherGenerator) {
    return makeMatcher(expected,
        matchOptional(v -> v.getLeft(),  leftMatcherGenerator),
        matchOptional(v -> v.getRight(), rightMatcherGenerator));
  }

}
