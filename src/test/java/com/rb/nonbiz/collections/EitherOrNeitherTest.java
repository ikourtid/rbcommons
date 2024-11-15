package com.rb.nonbiz.collections;

import com.rb.biz.types.Money;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Optional;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.EitherOrNeither.eitherOrNeither;
import static com.rb.nonbiz.collections.EitherOrNeither.eitherOrNeitherFromOptionalEither;
import static com.rb.nonbiz.collections.EitherOrNeither.left;
import static com.rb.nonbiz.collections.EitherOrNeither.neither;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This test class is not generic, but the publicly exposed matcher is.
 */
public class EitherOrNeitherTest extends RBTestMatcher<EitherOrNeither<UnitFraction, Double>> {

  @Test
  public void generalConstructor_mustHaveAtMostOne() {
    assertIllegalArgumentException( () -> eitherOrNeither(Optional.of("a"), Optional.of(1)));
    EitherOrNeither<String, Integer> doesNotThrow;
    doesNotThrow = eitherOrNeither(Optional.empty(), Optional.empty());
    doesNotThrow = eitherOrNeither(Optional.of("a"), Optional.empty());
    doesNotThrow = eitherOrNeither(Optional.empty(), Optional.of(1));
  }

  @Test
  public void testConversionFromOptional() {
    MatcherGenerator<Double> leftMatcher = f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8);
    MatcherGenerator<Money> rightMatcher = f2 -> preciseValueMatcher(f2, DEFAULT_EPSILON_1e_8);
    assertThat(
        eitherOrNeitherFromOptionalEither(Optional.empty()),
        eitherOrNeitherMatcher(neither(), leftMatcher, rightMatcher));
    assertThat(
        eitherOrNeitherFromOptionalEither(Optional.of(Either.left(1.1))),
        eitherOrNeitherMatcher(EitherOrNeither.left(1.1), leftMatcher, rightMatcher));
    assertThat(
        eitherOrNeitherFromOptionalEither(Optional.of(Either.right(money(2.2)))),
        eitherOrNeitherMatcher(EitherOrNeither.right(money(2.2)), leftMatcher, rightMatcher));
  }

  @Test
  public void testHasNeither() {
    assertTrue(neither().hasNeither());
    assertFalse(EitherOrNeither.left(DUMMY_STRING).hasNeither());
    assertFalse(EitherOrNeither.right(DUMMY_STRING).hasNeither());
  }

  @Override
  public EitherOrNeither<UnitFraction, Double> makeTrivialObject() {
    return neither();
  }

  @Override
  public EitherOrNeither<UnitFraction, Double> makeNontrivialObject() {
    return left(unitFraction(0.11));
  }

  @Override
  public EitherOrNeither<UnitFraction, Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return left(unitFraction(0.11 + e));
  }

  @Override
  protected boolean willMatch(EitherOrNeither<UnitFraction, Double> expected,
                              EitherOrNeither<UnitFraction, Double> actual) {
    return eitherOrNeitherMatcher(
        expected,
        v -> preciseValueMatcher(v, DEFAULT_EPSILON_1e_8),
        v -> doubleAlmostEqualsMatcher(v, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <L, R> TypeSafeMatcher<EitherOrNeither<L, R>> eitherOrNeitherEqualityMatcher(
      EitherOrNeither<L, R> expected) {
    return makeMatcher(expected,
        matchOptional(v -> v.getLeft(),  f -> typeSafeEqualTo(f)),
        matchOptional(v -> v.getRight(), f -> typeSafeEqualTo(f)));
  }

  public static <L, R> TypeSafeMatcher<EitherOrNeither<L, R>> eitherOrNeitherMatcher(
      EitherOrNeither<L, R> expected,
      MatcherGenerator<L> leftMatcherGenerator,
      MatcherGenerator<R> rightMatcherGenerator) {
    return makeMatcher(expected,
        matchOptional(v -> v.getLeft(),  leftMatcherGenerator),
        matchOptional(v -> v.getRight(), rightMatcherGenerator));
  }

}
