package com.rb.nonbiz.math.sequence;

import com.rb.biz.types.Money;
import com.rb.nonbiz.collections.Pair;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Iterator;
import java.util.function.Consumer;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.Money.sumMoney;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.PairTest.pairMatcher;
import static com.rb.nonbiz.math.sequence.ArithmeticProgression.arithmeticProgression;
import static com.rb.nonbiz.math.sequence.ArithmeticProgression.doubleArithmeticProgression;
import static com.rb.nonbiz.math.sequence.ArithmeticProgression.singleValueArithmeticProgression;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

// This test class is not generic, but the publicly exposed static matcher is
public class ArithmeticProgressionTest extends RBTestMatcher<ArithmeticProgression<Pair<String, Money>>> {

  public static ArithmeticProgression<Integer> allZeroesArithmeticProgression() {
    return singleValueArithmeticProgression(0);
  }

  /**
   * Returns a sequence with 0, 1, 2, etc. This is especially useful if you want to construct a Sequence of other
   * number-like items, like PreciseValue (Money etc.), external gains by term, etc.
   */
  public static ArithmeticProgression<Integer> naturalNumbersAsArithmeticProgression() {
    return arithmeticProgression(0, 1, v -> v + 1);
  }

  @Test
  public void testGet_simpleDouble() {
    Iterator<Double> iterator = doubleArithmeticProgression(100.0, 1.1).iterator();

    assertEquals(100.0, iterator.next(), 1e-8);
    assertEquals(101.1, iterator.next(), 1e-8);
    assertEquals(102.2, iterator.next(), 1e-8);
    assertEquals(103.3, iterator.next(), 1e-8);
  }

  @Test
  public void testGet_nonTrivialClassWithNumberInIt() {
    Iterator<Pair<String, Money>> iterator = arithmeticProgression(
        pair("x", money(100)),
        1.1,
        v -> pair(v.getLeft(), sumMoney(v.getRight(), money(1.1))))
        .iterator();

    Consumer<Pair<String, Money>> asserter = expectedPair ->
        assertThat(
            iterator.next(),
            pairMatcher(expectedPair, f2 -> typeSafeEqualTo(f2), f3 -> preciseValueMatcher(f3, DEFAULT_EPSILON_1e_8)));

    // Note that, each time 'asserter' gets invoked, we advance the iterator.
    asserter.accept(pair("x", money(100)));
    asserter.accept(pair("x", money(doubleExplained(101.1, 100   + 1.1))));
    asserter.accept(pair("x", money(doubleExplained(102.2, 101.1 + 1.1))));
  }

  @Override
  public ArithmeticProgression<Pair<String, Money>> makeTrivialObject() {
    return singleValueArithmeticProgression(pair("", ZERO_MONEY));
  }

  @Override
  public ArithmeticProgression<Pair<String, Money>> makeNontrivialObject() {
    return arithmeticProgression(
        pair("x", money(100)),
        1.1,
        v -> pair(v.getLeft(), sumMoney(v.getRight(), money(1.1))));
  }

  @Override
  public ArithmeticProgression<Pair<String, Money>> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return arithmeticProgression(
        pair("x", money(100 + e)),
        1.1 + e,
        v -> pair(v.getLeft(), sumMoney(v.getRight(), money(1.1 + e))));
  }

  @Override
  protected boolean willMatch(ArithmeticProgression<Pair<String, Money>> expected,
                              ArithmeticProgression<Pair<String, Money>> actual) {
    return arithmeticProgressionMatcher(
        expected,
        f -> pairMatcher(f, f2 -> typeSafeEqualTo(f2), f3 -> preciseValueMatcher(f3, DEFAULT_EPSILON_1e_8)))
        .matches(actual);
  }

  public static <T> TypeSafeMatcher<ArithmeticProgression<T>> arithmeticProgressionMatcher(
      ArithmeticProgression<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getInitialValue(),     matcherGenerator),
        matchUsingDoubleAlmostEquals(v -> v.getCommonDifference(), DEFAULT_EPSILON_1e_8));
    // We can't match on the lambda, of course.
  }

}
