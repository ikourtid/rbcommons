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
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.PairTest.pairMatcher;
import static com.rb.nonbiz.math.sequence.GeometricProgression.doubleGeometricProgression;
import static com.rb.nonbiz.math.sequence.GeometricProgression.geometricProgression;
import static com.rb.nonbiz.math.sequence.GeometricProgression.constantValueGeometricProgression;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingImpreciseAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

// This test class is not generic, but the publicly exposed static matcher is
public class GeometricProgressionTest extends RBTestMatcher<GeometricProgression<Pair<String, Money>>> {

  @Test
  public void testGet_simpleDouble() {
    Iterator<Double> iterator = doubleGeometricProgression(100.0, positiveMultiplier(2.0)).iterator();

    assertEquals(100, iterator.next(), 1e-8);
    assertEquals(200, iterator.next(), 1e-8);
    assertEquals(400, iterator.next(), 1e-8);
    assertEquals(800, iterator.next(), 1e-8);
  }

  @Test
  public void testGet_nonTrivialClassWithNumberInIt() {
    Iterator<Pair<String, Money>> iterator = geometricProgression(
        pair("x", money(100)),
        positiveMultiplier(1.5),
        v -> pair(v.getLeft(), v.getRight().multiply(1.5)))
        .iterator();

    Consumer<Pair<String, Money>> asserter = expectedPair ->
        assertThat(
            iterator.next(),
            pairMatcher(expectedPair, f2 -> typeSafeEqualTo(f2), f3 -> preciseValueMatcher(f3, DEFAULT_EPSILON_1e_8)));

    // Note that, each time 'asserter' gets invoked, we advance the iterator.
    asserter.accept(pair("x", money(100)));
    asserter.accept(pair("x", money(doubleExplained(150, 100 * 1.5))));
    asserter.accept(pair("x", money(doubleExplained(225, 150 * 1.5))));
  }

  @Override
  public GeometricProgression<Pair<String, Money>> makeTrivialObject() {
    return constantValueGeometricProgression(pair("", ZERO_MONEY));
  }

  @Override
  public GeometricProgression<Pair<String, Money>> makeNontrivialObject() {
    return geometricProgression(
        pair("x", money(100)),
        positiveMultiplier(1.08),
        v -> pair(v.getLeft(), v.getRight().multiply(1.08)));
  }

  @Override
  public GeometricProgression<Pair<String, Money>> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return geometricProgression(
        pair("x", money(100 + e)),
        positiveMultiplier(1.08 + e),
        v -> pair(v.getLeft(), v.getRight().multiply(1.08 + e)));
  }

  @Override
  protected boolean willMatch(GeometricProgression<Pair<String, Money>> expected,
                              GeometricProgression<Pair<String, Money>> actual) {
    return geometricProgressionMatcher(
        expected,
        f -> pairMatcher(f, f2 -> typeSafeEqualTo(f2), f3 -> preciseValueMatcher(f3, DEFAULT_EPSILON_1e_8)))
        .matches(actual);
  }

  public static <T> TypeSafeMatcher<GeometricProgression<T>> geometricProgressionMatcher(
      GeometricProgression<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(                          v -> v.getInitialValue(), matcherGenerator),
        matchUsingImpreciseAlmostEquals(v -> v.getCommonRatio(),  DEFAULT_EPSILON_1e_8));
    // We can't match on the lambda, of course.
  }

}
