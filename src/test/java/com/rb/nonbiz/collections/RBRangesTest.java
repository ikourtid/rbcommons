package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.biz.types.Money;
import com.rb.biz.types.SignedMoney;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.math.stats.ZScore;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.PositiveMultiplier;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static com.google.common.collect.BoundType.CLOSED;
import static com.google.common.collect.BoundType.OPEN;
import static com.google.common.collect.Iterators.singletonIterator;
import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.SignedMoney.ZERO_SIGNED_MONEY;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.ClosedRange.singletonClosedRange;
import static com.rb.nonbiz.collections.ClosedRangeTest.closedRangeEqualityMatcher;
import static com.rb.nonbiz.collections.ClosedRangeTest.closedRangeMatcher;
import static com.rb.nonbiz.collections.PairOfSameType.pairOfSameType;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.collections.RBRanges.*;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.math.stats.ZScore.Z_SCORE_0;
import static com.rb.nonbiz.math.stats.ZScore.zScore;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.doubleRangeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.preciseValueClosedRangeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.preciseValueRangeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.rangeEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_SIGNED_MONEY;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.ZERO_EPSILON;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.parseDouble;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBRangesTest {

  private final ZScore DUMMY_Z_SCORE = zScore(DUMMY_DOUBLE);

  public static <V extends Comparable<? super V>> List<Range<V>> allRanges(V min, V max) {
    return ImmutableList.of(
        Range.singleton(min),
        Range.singleton(max),
        Range.atLeast(min),
        Range.greaterThan(min),
        Range.atMost(max),
        Range.lessThan(max),
        Range.open(min, max),
        Range.openClosed(min, max),
        Range.closedOpen(min, max),
        Range.closed(min, max),
        Range.all());
  }

  /**
   * Return a List of all the types of ranges that have both endpoints present.
   */
  public static <V extends Comparable<? super V>> List<Range<V>> allRangesWithBothEndpoints(V min, V max) {
    return ImmutableList.of(
        Range.open(min, max),
        Range.openClosed(min, max),
        Range.closedOpen(min, max),
        Range.closed(min, max));
  }
  /**
   * Return a List of all the types of ranges that have at least one exclusive bound. That is, ranges
   * for which at least one boundary point is itself not part of the range. In terms of the Range class,
   * a range with at least one boundary of type BoundType.OPEN.
   */
  public static <V extends Comparable<? super V>> List<Range<V>> allRangesWithAnExlusiveBound(V min, V max) {
    return ImmutableList.of(
        Range.greaterThan(min),
        Range.lessThan(max),
        Range.open(min, max),
        Range.openClosed(min, max),
        Range.closedOpen(min, max));
  }

  public static <V extends Comparable<? super V>> List<Range<V>> allNonClosedRanges(V min, V max) {
    return ImmutableList.of(
        Range.atLeast(min),
        Range.greaterThan(min),
        Range.atMost(max),
        Range.lessThan(max),
        Range.open(min, max),
        Range.openClosed(min, max),
        Range.closedOpen(min, max),
        Range.all());
  }

  public static <V extends Comparable<? super V>> List<Range<V>> allNonEmptyOrSingletonRanges(
      V minWhenApplicable, V maxWhenApplicable) {
    return ImmutableList.of(
        Range.atLeast(minWhenApplicable),
        Range.greaterThan(minWhenApplicable),
        Range.atMost(maxWhenApplicable),
        Range.lessThan(maxWhenApplicable),
        Range.open(minWhenApplicable, maxWhenApplicable),
        Range.openClosed(minWhenApplicable, maxWhenApplicable),
        Range.closedOpen(minWhenApplicable, maxWhenApplicable),
        Range.closed(minWhenApplicable, maxWhenApplicable));
  }

  @Test
  public void testClosedDoubleRangeEpsilonContains_zeroEpsilon() {
    double e = 0; // epsilon
    assertFalse(closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7 - 1e-9, e));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7, e));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7.5, e));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 8, e));
    assertFalse(closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 8 + 1e-9, e));

    assertFalse(closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6 - 1e-9, e));
    assertTrue( closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6, e));
    assertFalse(closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6 + 1e-9, e));
  }

  @Test
  public void testClosedDoubleRangeContainsWellWithinBounds_zeroEpsilon() {
    double e = 0; // epsilon
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7 - 1e-9, e));
    assertTrue( closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7, e));
    assertTrue( closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7.5, e));
    assertTrue( closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 8, e));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 8 + 1e-9, e));

    assertFalse(closedDoubleRangeContainsWellWithinBounds(singletonClosedRange(6.0), 6 - 1e-9, e));
    assertTrue( closedDoubleRangeContainsWellWithinBounds(singletonClosedRange(6.0), 6, e));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(singletonClosedRange(6.0), 6 + 1e-9, e));
    
    // These are the same as above, but using a plain Range that has both endpoints present, not a ClosedRange object.
    allRangesWithBothEndpoints(7.0, 8.0).forEach(doubleRange -> {
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7 - 1e-9, e));
      assertTrue( doubleRangeContainsWellWithinBounds(doubleRange, 7, e));
      assertTrue( doubleRangeContainsWellWithinBounds(doubleRange, 7.5, e));
      assertTrue( doubleRangeContainsWellWithinBounds(doubleRange, 8, e));
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 8 + 1e-9, e));
    });

    assertFalse(doubleRangeContainsWellWithinBounds(Range.singleton(6.0), 6 - 1e-9, e));
    assertTrue( doubleRangeContainsWellWithinBounds(Range.singleton(6.0), 6, e));
    assertFalse(doubleRangeContainsWellWithinBounds(Range.singleton(6.0), 6 + 1e-9, e));
  }

  @Test
  public void testClosedDoubleRangeWellWithinBounds_usualEpsilon() {
    double e = 1e-8; // epsilon
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7 - 1e-7, e));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7 - 1e-9, e));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7, e));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7 + 1e-9, e));
    assertTrue( closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7 + 1e-7, e));
    assertTrue( closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7.5, e));
    assertTrue( closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 8 - 1e-7, e));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 8 - 1e-9, e));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 8, e));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 8 + 1e-9, e));

    assertFalse(closedDoubleRangeContainsWellWithinBounds(singletonClosedRange(6.0), 6 - 1e-7, e));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(singletonClosedRange(6.0), 6 - 1e-9, e));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(singletonClosedRange(6.0), 6, e));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(singletonClosedRange(6.0), 6 + 1e-9, e));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(singletonClosedRange(6.0), 6 + 1e-7, e));

    // These are the same as above, but using a plain Range that has both endpoints present, not a ClosedRange object.
    allRangesWithBothEndpoints(7.0, 8.0).forEach(doubleRange -> {
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7 - 1e-7, e));
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7 - 1e-9, e));
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7, e));
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7 + 1e-9, e));
      assertTrue( doubleRangeContainsWellWithinBounds(doubleRange, 7 + 1e-7, e));
      assertTrue( doubleRangeContainsWellWithinBounds(doubleRange, 7.5, e));
      assertTrue( doubleRangeContainsWellWithinBounds(doubleRange, 8 - 1e-7, e));
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 8 - 1e-9, e));
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 8, e));
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 8 + 1e-9, e));
    });

    assertFalse(doubleRangeContainsWellWithinBounds(Range.singleton(6.0), 6 - 1e-7, e));
    assertFalse(doubleRangeContainsWellWithinBounds(Range.singleton(6.0), 6 - 1e-9, e));
    assertFalse(doubleRangeContainsWellWithinBounds(Range.singleton(6.0), 6, e));
    assertFalse(doubleRangeContainsWellWithinBounds(Range.singleton(6.0), 6 + 1e-9, e));
    assertFalse(doubleRangeContainsWellWithinBounds(Range.singleton(6.0), 6 + 1e-7, e));
  }

  @Test
  public void testClosedDoubleRangeWellWithinBounds_defaultEpsilon() {
    // this overload uses the default epsilon of 1e-8
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7 - 1e-7));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7 - 1e-9));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7 + 1e-9));
    assertTrue( closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7 + 1e-7));
    assertTrue( closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 7.5));
    assertTrue( closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 8 - 1e-7));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 8 - 1e-9));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 8));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(closedRange(7.0, 8.0), 8 + 1e-9));

    assertFalse(closedDoubleRangeContainsWellWithinBounds(singletonClosedRange(6.0), 6 - 1e-7));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(singletonClosedRange(6.0), 6 - 1e-9));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(singletonClosedRange(6.0), 6));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(singletonClosedRange(6.0), 6 + 1e-9));
    assertFalse(closedDoubleRangeContainsWellWithinBounds(singletonClosedRange(6.0), 6 + 1e-7));

    // These are the same as above, but using a plain Range that has both endpoints present, not a ClosedRange object.
    allRangesWithBothEndpoints(7.0, 8.0).forEach(doubleRange -> {
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7 - 1e-7));
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7 - 1e-9));
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7));
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7 + 1e-9));
      assertTrue( doubleRangeContainsWellWithinBounds(doubleRange, 7 + 1e-7));
      assertTrue( doubleRangeContainsWellWithinBounds(doubleRange, 7.5));
      assertTrue( doubleRangeContainsWellWithinBounds(doubleRange, 8 - 1e-7));
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 8 - 1e-9));
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 8));
      assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 8 + 1e-9));
    });

    assertFalse(doubleRangeContainsWellWithinBounds(Range.singleton(6.0), 6 - 1e-7));
    assertFalse(doubleRangeContainsWellWithinBounds(Range.singleton(6.0), 6 - 1e-9));
    assertFalse(doubleRangeContainsWellWithinBounds(Range.singleton(6.0), 6));
    assertFalse(doubleRangeContainsWellWithinBounds(Range.singleton(6.0), 6 + 1e-9));
    assertFalse(doubleRangeContainsWellWithinBounds(Range.singleton(6.0), 6 + 1e-7));
  }

  @Test
  public void doubleRangeContainsWellWithinBounds_fewerThanTwoEndpointsArePresent_usingDefaultEpsilon() {
    // These tests use the default epsilon of 1e-8
    rbSetOf(
        Range.atLeast(7.0),
        Range.greaterThan(7.0))
        .forEach(doubleRange -> {
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7.0 - 1e-7));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7.0 - 1e-9));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7.0));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7.0 + 1e-9));
          assertTrue( doubleRangeContainsWellWithinBounds(doubleRange, 7.0 + 1e-7));
        });
    rbSetOf(
        Range.atMost(7.0),
        Range.lessThan(7.0))
        .forEach(doubleRange -> {
          assertTrue( doubleRangeContainsWellWithinBounds(doubleRange, 7 - 1e-7));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7 - 1e-9));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7 + 1e-9));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7 + 1e-7));
        });
    assertTrue(doubleRangeContainsWellWithinBounds(Range.all(), -12.3));
    assertTrue(doubleRangeContainsWellWithinBounds(Range.all(),   0.0));
    assertTrue(doubleRangeContainsWellWithinBounds(Range.all(), +12.3));
  }

  @Test
  public void doubleRangeContainsWellWithinBounds_fewerThanTwoEndpointsArePresent_usingLargerEpsilon() {
    double eps = 1e-4; // epsilon used in this test, instead of the default 1e-8
    rbSetOf(
        Range.atLeast(7.0),
        Range.greaterThan(7.0))
        .forEach(doubleRange -> {
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7.0 - 1e-3, eps));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7.0 - 1e-5, eps));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7.0,        eps));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7.0 + 1e-5, eps));
          assertTrue( doubleRangeContainsWellWithinBounds(doubleRange, 7.0 + 1e-3, eps));
        });
    rbSetOf(
        Range.atMost(7.0),
        Range.lessThan(7.0))
        .forEach(doubleRange -> {
          assertTrue( doubleRangeContainsWellWithinBounds(doubleRange, 7 - 1e-3, eps));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7 - 1e-5, eps));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7,        eps));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7 + 1e-5, eps));
          assertFalse(doubleRangeContainsWellWithinBounds(doubleRange, 7 + 1e-3, eps));
        });
    assertTrue(doubleRangeContainsWellWithinBounds(Range.all(), -12.3, eps));
    assertTrue(doubleRangeContainsWellWithinBounds(Range.all(),   0.0, eps));
    assertTrue(doubleRangeContainsWellWithinBounds(Range.all(), +12.3, eps));
  }

  @Test
  public void testDoubleRangeEpsilonContains_zeroEpsilon_rangeIsClosed() {
    double e = 0; // epsilon
    assertFalse(closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7 - 1e-9, e));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7, e));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7.5, e));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 8, e));
    assertFalse(closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 8 + 1e-9, e));

    assertFalse(closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6 - 1e-9, e));
    assertTrue( closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6, e));
    assertFalse(closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6 + 1e-9, e));
  }

  @Test
  public void testClosedDoubleRangeEpsilonContains_usualEpsilon() {
    double e = 1e-8; // epsilon
    assertFalse(closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7 - 1e-7, e));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7 - 1e-9, e));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7, e));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7.5, e));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 8, e));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 8 + 1e-9, e));
    assertFalse(closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 8 + 1e-7, e));

    assertFalse(closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6 - 1e-7, e));
    assertTrue( closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6 - 1e-9, e));
    assertTrue( closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6, e));
    assertTrue( closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6 + 1e-9, e));
    assertFalse(closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6 + 1e-7, e));
  }

  @Test
  public void testClosedDoubleRangeEpsilonContains_defaultEpsilon() {
    // this overload uses the default epsilon of 1e-8
    assertFalse(closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7 - 1e-7));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7 - 1e-9));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 7.5));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 8));
    assertTrue( closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 8 + 1e-9));
    assertFalse(closedDoubleRangeEpsilonContains(closedRange(7.0, 8.0), 8 + 1e-7));

    assertFalse(closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6 - 1e-7));
    assertTrue( closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6 - 1e-9));
    assertTrue( closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6));
    assertTrue( closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6 + 1e-9));
    assertFalse(closedDoubleRangeEpsilonContains(singletonClosedRange(6.0), 6 + 1e-7));
  }

  @Test
  public void testPreciseValueRangeEpsilonContains_zeroEpsilon() {
    double e = 0; // epsilon = 0

    assertFalse(preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(7 - 1e-7), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(7 - 1e-9), e));
    assertTrue( preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(7),        e));
    assertTrue( preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(7.5),      e));
    assertTrue( preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(8),        e));
    assertFalse(preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(8 + 1e-9), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(8 + 1e-7), e));

    // open vs closed can matter for epsilon = 0
    assertFalse(preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(7 - 1e-7), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(7 - 1e-9), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(7),        e));
    assertTrue( preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(7.5),      e));
    assertFalse(preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(8),        e));
    assertFalse(preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(8 + 1e-9), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(8 + 1e-7), e));

    // singleton ranges
    assertFalse(preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6 - 1e-7), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6 - 1e-9), e));
    assertTrue( preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6),        e));
    assertFalse(preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6 + 1e-9), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6 + 1e-7), e));

    // single bound open ranges
    assertFalse(preciseValueRangeEpsilonContains(Range.greaterThan(money(7.0)), money(7 - 1e-7), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.greaterThan(money(7.0)), money(7 - 1e-9), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.greaterThan(money(7.0)), money(7),        e));
    assertTrue( preciseValueRangeEpsilonContains(Range.greaterThan(money(7.0)), money(7 + 1e-9), e));
    assertTrue( preciseValueRangeEpsilonContains(Range.lessThan(money(8.0)),    money(8 - 1e-9), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.lessThan(money(8.0)),    money(8),        e));
    assertFalse(preciseValueRangeEpsilonContains(Range.lessThan(money(8.0)),    money(8 + 1e-9), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.lessThan(money(8.0)),    money(8 + 1e-7), e));
  }

  @Test
  public void testPreciseValueRangeEpsilonContains_usualEpsilon() {
    double e = 1e-8; // epsilon

    // closed ranges
    assertFalse(preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(7 - 1e-7), e));
    assertTrue( preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(7 - 1e-9), e));
    assertTrue( preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(7),        e));
    assertTrue( preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(7.5),      e));
    assertTrue( preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(8),        e));
    assertTrue( preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(8 + 1e-9), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(8 + 1e-7), e));

    // open vs closed doesn't matter (much); still have a buffer of epsilon > 0 around the range
    assertFalse(preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(7 - 1e-7), e));
    assertTrue( preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(7 - 1e-9), e));
    assertTrue( preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(7),        e));
    assertTrue( preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(7.5),      e));
    assertTrue( preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(8),        e));
    assertTrue( preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(8 + 1e-9), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(8 + 1e-7), e));

    // singleton ranges
    assertFalse(preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6 - 1e-7), e));
    assertTrue( preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6 - 1e-9), e));
    assertTrue( preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6),        e));
    assertTrue( preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6 + 1e-9), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6 + 1e-7), e));

    // single bound open ranges
    assertFalse(preciseValueRangeEpsilonContains(Range.greaterThan(money(7.0)), money(7 - 1e-7), e));
    assertTrue( preciseValueRangeEpsilonContains(Range.greaterThan(money(7.0)), money(7 - 1e-9), e));
    assertTrue( preciseValueRangeEpsilonContains(Range.greaterThan(money(7.0)), money(7),        e));
    assertTrue( preciseValueRangeEpsilonContains(Range.greaterThan(money(7.0)), money(7 + 1e-9), e));
    assertTrue( preciseValueRangeEpsilonContains(Range.lessThan(money(8.0)),    money(8 - 1e-9), e));
    assertTrue( preciseValueRangeEpsilonContains(Range.lessThan(money(8.0)),    money(8),        e));
    assertTrue( preciseValueRangeEpsilonContains(Range.lessThan(money(8.0)),    money(8 + 1e-9), e));
    assertFalse(preciseValueRangeEpsilonContains(Range.lessThan(money(8.0)),    money(8 + 1e-7), e));
  }

  @Test
  public void testPreciseValueRangeEpsilonContains_defaultEpsilon() {
    // this overload uses the default epsilon of 1e-8

    // closed ranges
    assertFalse(preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(7 - 1e-7)));
    assertTrue( preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(7 - 1e-9)));
    assertTrue( preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(7)));
    assertTrue( preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(7.5)));
    assertTrue( preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(8)));
    assertTrue( preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(8 + 1e-9)));
    assertFalse(preciseValueRangeEpsilonContains(Range.closed(money(7.0), money(8.0)), money(8 + 1e-7)));

    // open vs closed doesn't matter (much); still have a buffer of epsilon > 0 around the range
    assertFalse(preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(7 - 1e-7)));
    assertTrue( preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(7 - 1e-9)));
    assertTrue( preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(7)));
    assertTrue( preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(7.5)));
    assertTrue( preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(8)));
    assertTrue( preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(8 + 1e-9)));
    assertFalse(preciseValueRangeEpsilonContains(Range.open(money(7.0), money(8.0)), money(8 + 1e-7)));

    // singleton ranges
    assertFalse(preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6 - 1e-7)));
    assertTrue( preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6 - 1e-9)));
    assertTrue( preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6)));
    assertTrue( preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6 + 1e-9)));
    assertFalse(preciseValueRangeEpsilonContains(Range.singleton(money(6.0)), money(6 + 1e-7)));

    // single bound open ranges
    assertFalse(preciseValueRangeEpsilonContains(Range.greaterThan(money(7.0)), money(7 - 1e-7)));
    assertTrue( preciseValueRangeEpsilonContains(Range.greaterThan(money(7.0)), money(7 - 1e-9)));
    assertTrue( preciseValueRangeEpsilonContains(Range.greaterThan(money(7.0)), money(7)));
    assertTrue( preciseValueRangeEpsilonContains(Range.greaterThan(money(7.0)), money(7 + 1e-9)));
    assertTrue( preciseValueRangeEpsilonContains(Range.lessThan(money(8.0)),    money(8 - 1e-9)));
    assertTrue( preciseValueRangeEpsilonContains(Range.lessThan(money(8.0)),    money(8)));
    assertTrue( preciseValueRangeEpsilonContains(Range.lessThan(money(8.0)),    money(8 + 1e-9)));
    assertFalse(preciseValueRangeEpsilonContains(Range.lessThan(money(8.0)),    money(8 + 1e-7)));
  }

  @Test
  public void testDoubleRangeIsSinglePoint() {
    assertTrue(doubleRangeIsAlmostSinglePoint(Range.closed(-1.1, -1.1), 1e-8));
    assertTrue(doubleRangeIsAlmostSinglePoint(Range.closed(0.0, 0.0), 1e-8));
    assertTrue(doubleRangeIsAlmostSinglePoint(Range.closed(1.1, 1.1), 1e-8));
    double e = 1e-9; // epsilon
    assertTrue( doubleRangeIsAlmostSinglePoint(Range.closed(-1.1, -1.1 + e), 1e-8));
    assertTrue( doubleRangeIsAlmostSinglePoint(Range.closed(0.0, 0.0 + e), 1e-8));
    assertTrue( doubleRangeIsAlmostSinglePoint(Range.closed(1.1, 1.1 + e), 1e-8));
    assertFalse(doubleRangeIsAlmostSinglePoint(Range.closed(-1.1, -1.0), 1e-8));
    assertFalse(doubleRangeIsAlmostSinglePoint(Range.closed(0.0, 0.1), 1e-8));
    assertFalse(doubleRangeIsAlmostSinglePoint(Range.closed(1.1, 1.2), 1e-8));
  }

  @Test
  public void testDoubleRangeIsSinglePoint_defaultEpsilon() {
    assertTrue(doubleRangeIsAlmostSinglePoint(Range.closed(-1.1, -1.1)));
    assertTrue(doubleRangeIsAlmostSinglePoint(Range.closed(0.0, 0.0)));
    assertTrue(doubleRangeIsAlmostSinglePoint(Range.closed(1.1, 1.1)));
    double e = 1e-9; // epsilon
    assertTrue( doubleRangeIsAlmostSinglePoint(Range.closed(-1.1, -1.1 + e)));
    assertTrue( doubleRangeIsAlmostSinglePoint(Range.closed(0.0, 0.0 + e)));
    assertTrue( doubleRangeIsAlmostSinglePoint(Range.closed(1.1, 1.1 + e)));
    assertFalse(doubleRangeIsAlmostSinglePoint(Range.closed(-1.1, -1.0)));
    assertFalse(doubleRangeIsAlmostSinglePoint(Range.closed(0.0, 0.1)));
    assertFalse(doubleRangeIsAlmostSinglePoint(Range.closed(1.1, 1.2)));
  }

  @Test
  public void testPreciseValueRangeIsAlmostSinglePoint() {
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1)), 1e-8));
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY), 1e-8));
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1)), 1e-8));
    double e = 1e-9; // epsilon
    assertTrue( rbNumericRangeIsAlmostSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1 + e)), 1e-8));
    assertTrue( rbNumericRangeIsAlmostSinglePoint(Range.closed(ZERO_SIGNED_MONEY, signedMoney(e)), 1e-8));
    assertTrue( rbNumericRangeIsAlmostSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1 + e)), 1e-8));
    assertFalse(rbNumericRangeIsAlmostSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.0)), 1e-8));
    assertFalse(rbNumericRangeIsAlmostSinglePoint(Range.closed(ZERO_SIGNED_MONEY, signedMoney(0.1)), 1e-8));
    assertFalse(rbNumericRangeIsAlmostSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.2)), 1e-8));
  }

  @Test
  public void testPreciseValueRangeIsAlmostSinglePoint_defaultEpsilon() {
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1))));
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY)));
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1))));
    double e = 1e-9; // epsilon
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1 + e))));
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(ZERO_SIGNED_MONEY, signedMoney(e))));
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1 + e))));
    assertFalse(rbNumericRangeIsAlmostSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.0))));
    assertFalse(rbNumericRangeIsAlmostSinglePoint(Range.closed(ZERO_SIGNED_MONEY, signedMoney(0.1))));
    assertFalse(rbNumericRangeIsAlmostSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.2))));
  }

  @Test
  public void testImpreciseValueRangeIsAlmostSinglePoint() {
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(zScore(-1.1), zScore(-1.1)), 1e-8));
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(Z_SCORE_0, Z_SCORE_0), 1e-8));
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(zScore(1.1), zScore(1.1)), 1e-8));
    double e = 1e-9; // epsilon
    assertTrue( rbNumericRangeIsAlmostSinglePoint(Range.closed(zScore(-1.1), zScore(-1.1 + e)), 1e-8));
    assertTrue( rbNumericRangeIsAlmostSinglePoint(Range.closed(Z_SCORE_0, zScore(e)), 1e-8));
    assertTrue( rbNumericRangeIsAlmostSinglePoint(Range.closed(zScore(1.1), zScore(1.1 + e)), 1e-8));
    assertFalse(rbNumericRangeIsAlmostSinglePoint(Range.closed(zScore(-1.1), zScore(-1.0)), 1e-8));
    assertFalse(rbNumericRangeIsAlmostSinglePoint(Range.closed(Z_SCORE_0, zScore(0.1)), 1e-8));
    assertFalse(rbNumericRangeIsAlmostSinglePoint(Range.closed(zScore(1.1), zScore(1.2)), 1e-8));
  }

  @Test
  public void testImpreciseValueRangeIsAlmostSinglePoint_defaultEpsilon() {
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(zScore(-1.1), zScore(-1.1))));
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(Z_SCORE_0, Z_SCORE_0)));
    assertTrue(rbNumericRangeIsAlmostSinglePoint(Range.closed(zScore(1.1), zScore(1.1))));
    double e = 1e-9; // epsilon
    assertTrue( rbNumericRangeIsAlmostSinglePoint(Range.closed(zScore(-1.1), zScore(-1.1 + e))));
    assertTrue( rbNumericRangeIsAlmostSinglePoint(Range.closed(Z_SCORE_0, zScore(e))));
    assertTrue( rbNumericRangeIsAlmostSinglePoint(Range.closed(zScore(1.1), zScore(1.1 + e))));
    assertFalse(rbNumericRangeIsAlmostSinglePoint(Range.closed(zScore(-1.1), zScore(-1.0))));
    assertFalse(rbNumericRangeIsAlmostSinglePoint(Range.closed(Z_SCORE_0, zScore(0.1))));
    assertFalse(rbNumericRangeIsAlmostSinglePoint(Range.closed(zScore(1.1), zScore(1.2))));
  }

  @Test
  public void testDoubleRangeIsThisSinglePoint_usualEpsilon() {
    double e = 1e-9; // epsilon
    assertTrue(doubleRangeIsAlmostThisSinglePoint(Range.closed(-1.1, -1.1), -1.1 - e, 1e-8));
    assertTrue(doubleRangeIsAlmostThisSinglePoint(Range.closed(-1.1, -1.1), -1.1, 1e-8));
    assertTrue(doubleRangeIsAlmostThisSinglePoint(Range.closed(-1.1, -1.1), -1.1 + e, 1e-8));
    assertFalse(doubleRangeIsAlmostThisSinglePoint(Range.closed(-1.1, -1.1), 1.1, 1e-8));

    assertTrue(doubleRangeIsAlmostThisSinglePoint(Range.closed(0.0, 0.0), 0.0 - e, 1e-8));
    assertTrue(doubleRangeIsAlmostThisSinglePoint(Range.closed(0.0, 0.0), 0.0, 1e-8));
    assertTrue(doubleRangeIsAlmostThisSinglePoint(Range.closed(0.0, 0.0), 0.0 + e, 1e-8));
    assertFalse(doubleRangeIsAlmostThisSinglePoint(Range.closed(0.0, 0.0), 123.456, 1e-8));

    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(1.1, 1.1), 1.1 - e, 1e-8));
    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(1.1, 1.1), 1.1, 1e-8));
    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(1.1, 1.1), 1.1 + e, 1e-8));
    assertFalse(doubleRangeIsAlmostThisSinglePoint(Range.closed(1.1, 1.1), -1.1, 1e-8));

    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(-1.1, -1.1 + e), -1.1, 1e-8));
    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(0.0, 0.0 + e), 0.0, 1e-8));
    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(1.1, 1.1 + e), 1.1, 1e-8));
    assertFalse(doubleRangeIsAlmostThisSinglePoint(Range.closed(-1.1, -1.0), -1.1, 1e-8));
    assertFalse(doubleRangeIsAlmostThisSinglePoint(Range.closed(0.0, 0.1), 0.0, 1e-8));
    assertFalse(doubleRangeIsAlmostThisSinglePoint(Range.closed(1.1, 1.2), 1.1, 1e-8));
  }

  @Test
  public void testDoubleRangeIsThisSinglePoint_defaultEpsilon() {
    double e = 1e-9; // epsilon
    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(-1.1, -1.1), -1.1 - e));
    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(-1.1, -1.1), -1.1));
    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(-1.1, -1.1), -1.1 + e));
    assertFalse(doubleRangeIsAlmostThisSinglePoint(Range.closed(-1.1, -1.1), 1.1));

    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(0.0, 0.0), 0.0 - e));
    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(0.0, 0.0), 0.0));
    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(0.0, 0.0), 0.0 + e));
    assertFalse(doubleRangeIsAlmostThisSinglePoint(Range.closed(0.0, 0.0), 123.456));

    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(1.1, 1.1), 1.1 - e));
    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(1.1, 1.1), 1.1));
    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(1.1, 1.1), 1.1 + e));
    assertFalse(doubleRangeIsAlmostThisSinglePoint(Range.closed(1.1, 1.1), -1.1));

    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(-1.1, -1.1 + e), -1.1));
    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(0.0, 0.0 + e), 0.0));
    assertTrue( doubleRangeIsAlmostThisSinglePoint(Range.closed(1.1, 1.1 + e), 1.1));
    assertFalse(doubleRangeIsAlmostThisSinglePoint(Range.closed(-1.1, -1.0), -1.1));
    assertFalse(doubleRangeIsAlmostThisSinglePoint(Range.closed(0.0, 0.1), 0.0));
    assertFalse(doubleRangeIsAlmostThisSinglePoint(Range.closed(1.1, 1.2), 1.1));
  }

  @Test
  public void testPreciseValueRangeIsThisSinglePoint() {
    double e = 1e-9; // epsilon
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1)), signedMoney(-1.1 - e), 1e-8));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1)), signedMoney(-1.1), 1e-8));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1)), signedMoney(-1.1 + e), 1e-8));
    assertFalse(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1)), signedMoney(1.1), 1e-8));

    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY), signedMoney(-e), 1e-8));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY), ZERO_SIGNED_MONEY, 1e-8));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY), signedMoney(e), 1e-8));
    assertFalse(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY), signedMoney(123.456), 1e-8));

    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1)), signedMoney(1.1 - e), 1e-8));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1)), signedMoney(1.1), 1e-8));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1)), signedMoney(1.1 + e), 1e-8));
    assertFalse(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1)), signedMoney(-1.1), 1e-8));

    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1 + e)), signedMoney(-1.1), 1e-8));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(ZERO_SIGNED_MONEY, signedMoney(e)), ZERO_SIGNED_MONEY, 1e-8));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1 + e)), signedMoney(1.1), 1e-8));
    assertFalse(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.0)), signedMoney(-1.1), 1e-8));
    assertFalse(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(ZERO_SIGNED_MONEY, signedMoney(0.1)), ZERO_SIGNED_MONEY, 1e-8));
    assertFalse(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.2)), signedMoney(1.1), 1e-8));
  }

  @Test
  public void testPreciseValueRangeIsThisSinglePoint_defaultEpsilon() {
    double e = 1e-9; // epsilon
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1)), signedMoney(-1.1 - e)));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1)), signedMoney(-1.1)));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1)), signedMoney(-1.1 + e)));
    assertFalse(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1)), signedMoney(1.1)));

    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY), signedMoney(-e)));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY), ZERO_SIGNED_MONEY));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY), signedMoney(e)));
    assertFalse(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY), signedMoney(123.456)));

    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1)), signedMoney(1.1 - e)));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1)), signedMoney(1.1)));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1)), signedMoney(1.1 + e)));
    assertFalse(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1)), signedMoney(-1.1)));

    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.1 + e)), signedMoney(-1.1)));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(ZERO_SIGNED_MONEY, signedMoney(e)), ZERO_SIGNED_MONEY));
    assertTrue(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.1 + e)), signedMoney(1.1)));
    assertFalse(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(-1.1), signedMoney(-1.0)), signedMoney(-1.1)));
    assertFalse(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(ZERO_SIGNED_MONEY, signedMoney(0.1)), ZERO_SIGNED_MONEY));
    assertFalse(rbNumericRangeIsAlmostThisSinglePoint(Range.closed(signedMoney(1.1), signedMoney(1.2)), signedMoney(1.1)));
  }

  @Test
  public void testRangeIsClosed() {
    allNonClosedRanges(1, 2).forEach(nonClosedRange -> assertFalse(rangeIsClosed(nonClosedRange)));
    assertTrue(rangeIsClosed(Range.closed(1, 2)));
  }

  @Test
  public void testRangeIsBounded() {
    for (Range<Integer> invalidRange : rbSetOf(
        Range.atLeast(1),
        Range.greaterThan(1),
        Range.lessThan(1),
        Range.atMost(1),
        Range.<Integer>all())) {
      assertFalse(rangeIsBounded(invalidRange));
    }
    for (Range<Integer> validRange : rbSetOf(
        Range.closedOpen(1, 2),
        Range.openClosed(1, 2),
        Range.open(1, 2),
        Range.closed(1, 2))) {
      assertTrue(rangeIsBounded(validRange));
    }
  }

  @Test
  public void testRangeHasAtLeastOneClosedBound() {
    for (Range<Integer> invalidRange : rbSetOf(
        Range.greaterThan(1),
        Range.lessThan(1),
        Range.closedOpen(1, 2),
        Range.openClosed(1, 2),
        Range.open(1, 2),
        Range.<Integer>all())) {
      assertFalse(rangeHasAtLeastOneClosedBound(invalidRange));
    }
    for (Range<Integer> validRange : rbSetOf(
        Range.atLeast(1),
        Range.atMost(1),
        Range.singleton(1),
        Range.closed(1, 2))) {
      assertTrue(rangeHasAtLeastOneClosedBound(validRange));
    }
  }

  @Test
  public void testRangeIsAtLeast() {
    for (Range<Integer> invalidRange : rbSetOf(
        Range.closedOpen(1, 2),
        Range.openClosed(1, 2),
        Range.open(1, 2),
        Range.closed(1, 2),
        Range.greaterThan(1),
        Range.lessThan(1),
        Range.atMost(1),
        Range.<Integer>all())) {
      assertFalse(rangeIsAtLeast(invalidRange));
    }
    assertTrue(rangeIsAtLeast(Range.atLeast(1)));
  }

  @Test
  public void testGetMinMaxClosedRange() {
    assertIllegalArgumentException( () -> getMinMaxClosedRange(Collections.<Integer>emptyList()));
    assertIllegalArgumentException( () -> getMinMaxClosedRange(Stream.<Integer>empty()));
    assertThat(
        getMinMaxClosedRange(singleton(-1)),
        closedRangeEqualityMatcher(
            closedRange(-1, -1)));
    assertThat(
        getMinMaxClosedRange(Stream.of(-1)),
        closedRangeEqualityMatcher(
            closedRange(-1, -1)));
    List<Integer> numbers = ImmutableList.of(-1, 2, -3, 4, 5, -6);
    assertThat(
        getMinMaxClosedRange(numbers),
        closedRangeEqualityMatcher(
            closedRange(-6, 5)));
    assertThat(
        getMinMaxClosedRange(numbers.stream()),
        closedRangeEqualityMatcher(
            closedRange(-6, 5)));
  }

  @Test
  public void testIsLowerBoundOpen() {
    assertFalse(hasOpenLowerBound(Range.<Integer>all()));
    assertFalse(hasOpenLowerBound(Range.closed(     1, 10)));
    assertFalse(hasOpenLowerBound(Range.closedOpen( 1, 10)));
    assertFalse(hasOpenLowerBound(Range.lessThan(      10)));

    assertTrue( hasOpenLowerBound(Range.open(       1, 10)));
    assertTrue( hasOpenLowerBound(Range.openClosed( 1, 10)));
    assertTrue( hasOpenLowerBound(Range.greaterThan(1)));
  }

  @Test
  public void testIsUpperBoundOpen() {
    assertFalse(hasOpenUpperBound(Range.<Integer>all()));
    assertFalse(hasOpenUpperBound(Range.closed(     1, 10)));
    assertFalse(hasOpenUpperBound(Range.openClosed( 1, 10)));
    assertFalse(hasOpenUpperBound(Range.greaterThan(1)));

    assertTrue(hasOpenUpperBound(Range.open(        1, 10)));
    assertTrue(hasOpenUpperBound(Range.closedOpen(  1, 10)));
    assertTrue(hasOpenUpperBound(Range.lessThan(       10)));
  }

  @Test
  public void testIsEitherBoundOpen() {
    assertTrue(hasEitherBoundOpen(Range.open(       1, 10)));
    assertTrue(hasEitherBoundOpen(Range.openClosed( 1, 10)));
    assertTrue(hasEitherBoundOpen(Range.closedOpen( 1, 10)));

    assertTrue(hasEitherBoundOpen(Range.greaterThan(1)));
    assertTrue(hasEitherBoundOpen(Range.lessThan(      10)));

    assertFalse(hasEitherBoundOpen(Range.<Integer>all()));
    assertFalse(hasEitherBoundOpen(Range.closed(    1, 10)));

    assertFalse(hasEitherBoundOpen(Range.<Integer>all()));
    assertFalse(hasEitherBoundOpen(Range.closed(    1, 10)));
  }

  @Test
  public void testGetClosedRangeFromSorted() {
    Function<Integer, String> extractor = i -> Strings.format("_%s", i);

    assertIllegalArgumentException( () -> getClosedRangeFromSorted(emptyList(), extractor));
    assertIllegalArgumentException( () -> getClosedRangeFromSorted(Collections.emptyIterator(), extractor));

    assertThat(
        getClosedRangeFromSorted(singleton(-1), extractor),
        closedRangeEqualityMatcher(
            closedRange("_-1", "_-1")));
    assertThat(
        getClosedRangeFromSorted(singleton(-1).iterator(), extractor),
        closedRangeEqualityMatcher(
            closedRange("_-1", "_-1")));

    List<Integer> sortedNumbers = ImmutableList.of(1, 2, 3, 4);
    assertThat(
        getClosedRangeFromSorted(sortedNumbers, extractor),
        closedRangeEqualityMatcher(
            closedRange("_1", "_4")));
    assertThat(
        getClosedRangeFromSorted(sortedNumbers.iterator(), extractor),
        closedRangeEqualityMatcher(
            closedRange("_1", "_4")));

    // not sorted; throws
    assertIllegalArgumentException( () -> getClosedRangeFromSorted(ImmutableList.of(2, 1), extractor));
    assertIllegalArgumentException( () -> getClosedRangeFromSorted(ImmutableList.of(2, 1).iterator(), extractor));

    // Not sorted, even though endpoints are sorted; throws
    assertIllegalArgumentException( () -> getClosedRangeFromSorted(ImmutableList.of(1, 5, 2), extractor));
    assertIllegalArgumentException( () -> getClosedRangeFromSorted(ImmutableList.of(1, -5, 2).iterator(), extractor));
  }

  @Test
  public void testFlipRange() {
    for (PairOfSameType<Range<Double>> pair : ImmutableList.of(
        pairOfSameType(Range.closedOpen(1.1, 2.2),   Range.openClosed(-2.2, -1.1)),
        pairOfSameType(Range.closedOpen(-1.1, 2.2),  Range.openClosed(-2.2, 1.1)),
        pairOfSameType(Range.closedOpen(-2.2, -1.1), Range.openClosed(1.1, 2.2)),
        pairOfSameType(Range.openClosed(1.1, 2.2),   Range.closedOpen(-2.2, -1.1)),
        pairOfSameType(Range.openClosed(-1.1, 2.2),  Range.closedOpen(-2.2, 1.1)),
        pairOfSameType(Range.openClosed(-2.2, -1.1), Range.closedOpen(1.1, 2.2)),
        pairOfSameType(Range.open(1.1, 2.2),         Range.open(-2.2, -1.1)),
        pairOfSameType(Range.open(-1.1, 2.2),        Range.open(-2.2, 1.1)),
        pairOfSameType(Range.open(-2.2, -1.1),       Range.open(1.1, 2.2)),
        pairOfSameType(Range.atLeast(1.1),           Range.atMost(-1.1)),
        pairOfSameType(Range.atLeast(-1.1),          Range.atMost(1.1)),
        pairOfSameType(Range.greaterThan(1.1),       Range.lessThan(-1.1)),
        pairOfSameType(Range.greaterThan(-1.1),      Range.lessThan(1.1)),
        pairOfSameType(Range.lessThan(1.1),          Range.greaterThan(-1.1)),
        pairOfSameType(Range.lessThan(-1.1),         Range.greaterThan(1.1)),
        pairOfSameType(Range.atMost(1.1),            Range.atLeast(-1.1)),
        pairOfSameType(Range.atMost(-1.1),           Range.atLeast(1.1)),
        pairOfSameType(Range.<Double>all(),          Range.<Double>all()))) {
      Range<Double> range1 = pair.getLeft();
      Range<Double> range2 = pair.getRight();
      assertThat(flipRange(range1), doubleRangeMatcher(range2, DEFAULT_EPSILON_1e_8));
      assertThat(flipRange(range2), doubleRangeMatcher(range1, DEFAULT_EPSILON_1e_8));
    }
  }

  @Test
  public void testShiftRange() {
    for (PairOfSameType<Range<Double>> pair : ImmutableList.of(
        pairOfSameType(Range.closed(-1.1, 2.2),      Range.closed(-1.09, 2.21)),
        pairOfSameType(Range.closedOpen(-1.1, 2.2),  Range.closedOpen(-1.09, 2.21)),
        pairOfSameType(Range.openClosed(-1.1, 2.2),  Range.openClosed(-1.09, 2.21)),
        pairOfSameType(Range.open(-1.1, 2.2),        Range.open(-1.09, 2.21)),
        pairOfSameType(Range.atLeast(1.1),           Range.atLeast(1.11)),
        pairOfSameType(Range.greaterThan(1.1),       Range.greaterThan(1.11)),
        pairOfSameType(Range.lessThan(1.1),          Range.lessThan(1.11)),
        pairOfSameType(Range.atMost(1.1),            Range.atMost(1.11)),
        pairOfSameType(Range.<Double>all(),               Range.<Double>all()))) {
      Range<Double> range1 = pair.getLeft();
      Range<Double> range2 = pair.getRight();
      assertThat(shiftDoubleRange(range1, 0.01), doubleRangeMatcher(range2, DEFAULT_EPSILON_1e_8));
      assertThat(shiftDoubleRange(range2, -0.01), doubleRangeMatcher(range1, DEFAULT_EPSILON_1e_8));
      assertThat(shiftDoubleRange(range1, 0), doubleRangeMatcher(range1, DEFAULT_EPSILON_1e_8));
    }
  }

  @Test
  public void testExtendDoubleRangeBiDirectionally() {
    TriConsumer<Range<Double>, Double, Range<Double>> asserter = (initialRange, extension, expectedRange) ->
        assertThat(
            extendDoubleRangeBiDirectionally(initialRange, extension),
            doubleRangeMatcher(expectedRange, DEFAULT_EPSILON_1e_8));

    asserter.accept(Range.closed(-1.1, 2.2),  0.0, Range.closed( -1.1,  2.2));
    asserter.accept(Range.closed(-1.1, 2.2),  1.0, Range.closed( -2.1,  3.2));
    asserter.accept(Range.closed(-1.1, 2.2), 10.0, Range.closed(-11.1, 12.2));

    asserter.accept(Range.atLeast(-1.1),  0.0, Range.atLeast( -1.1));
    asserter.accept(Range.atLeast(-1.1),  1.0, Range.atLeast( -2.1));
    asserter.accept(Range.atLeast(-1.1), 10.0, Range.atLeast(-11.1));
    asserter.accept(Range.atMost(  2.2),  0.0, Range.atMost(   2.2));
    asserter.accept(Range.atMost(  2.2),  1.0, Range.atMost(   3.2));
    asserter.accept(Range.atMost(  2.2), 10.0, Range.atMost(  12.2));

    // open ranges supported as well
    asserter.accept(Range.open(-1.1, 2.2),  0.0, Range.open( -1.1,  2.2));
    asserter.accept(Range.open(-1.1, 2.2),  1.0, Range.open( -2.1,  3.2));
    asserter.accept(Range.open(-1.1, 2.2), 10.0, Range.open(-11.1, 12.2));

    asserter.accept(Range.greaterThan(-1.1),  0.0, Range.greaterThan( -1.1));
    asserter.accept(Range.greaterThan(-1.1),  1.0, Range.greaterThan( -2.1));
    asserter.accept(Range.greaterThan(-1.1), 10.0, Range.greaterThan(-11.1));
    asserter.accept(Range.lessThan(    2.2),  0.0, Range.lessThan(     2.2));
    asserter.accept(Range.lessThan(    2.2),  1.0, Range.lessThan(     3.2));
    asserter.accept(Range.lessThan(    2.2), 10.0, Range.lessThan(    12.2));

    // extending Range.all() doesn't change it
    asserter.accept(Range.all(),  0.0, Range.all());
    asserter.accept(Range.all(),  1.0, Range.all());
    asserter.accept(Range.all(), 10.0, Range.all());

    // can't extend by a negative amount
    assertIllegalArgumentException( () -> extendDoubleRangeBiDirectionally(Range.closed(-1.1, 2.2), -0.2));
  }

  @Test
  public void testOptionalIntersection() {
    assertOptionalEmpty(optionalIntersection(Range.closed(1, 3), Range.closed(5, 7)));
    assertOptionalEmpty(optionalIntersection(Range.closed(5, 7), Range.closed(1, 3)));
    assertOptionalEquals(Range.atLeast(33.33), optionalIntersection(Range.atLeast(33.33), Range.all()));
    assertOptionalEquals(Range.closed(33.33, 44.44), optionalIntersection(Range.atLeast(33.33), Range.atMost(44.44)));
    assertOptionalEmpty(optionalIntersection(Range.atLeast(33.33), Range.atMost(22.22)));
  }

  @Test
  public void testOptionalIntersection_endsMeetButBothAreOpen() {
    assertOptionalEmpty(optionalIntersection(Range.closedOpen(1.1, 2.2), Range.openClosed(2.2, 3.3)));
    assertOptionalEmpty(optionalIntersection(Range.closed(1.1, 2.2), Range.openClosed(2.2, 3.3)));
    assertOptionalEmpty(optionalIntersection(Range.closedOpen(1.1, 2.2), Range.closed(2.2, 3.3)));
    assertOptionalEquals(
        Range.singleton(2.2),
        optionalIntersection(Range.closed(1.1, 2.2), Range.closed(2.2, 3.3)));

    assertOptionalEmpty(optionalIntersection(Range.closedOpen(-11.1, -2.2), Range.openClosed(-2.2, 3.3)));
    assertOptionalEmpty(optionalIntersection(Range.closed(-11.1, -2.2), Range.openClosed(-2.2, 3.3)));
    assertOptionalEmpty(optionalIntersection(Range.closedOpen(-11.1, -2.2), Range.closed(-2.2, 3.3)));
    assertOptionalEquals(
        Range.singleton(-2.2),
        optionalIntersection(Range.closed(-11.1, -2.2), Range.closed(-2.2, 3.3)));

    assertOptionalEmpty(optionalIntersection(Range.lessThan(2.2), Range.greaterThan(2.2)));
    assertOptionalEmpty(optionalIntersection(Range.atMost(2.2), Range.greaterThan(2.2)));
    assertOptionalEmpty(optionalIntersection(Range.lessThan(2.2), Range.atLeast(2.2)));
    assertOptionalEquals(
        Range.singleton(2.2),
        optionalIntersection(Range.atMost(2.2), Range.atLeast(2.2)));

    assertOptionalEmpty(optionalIntersection(Range.lessThan(-2.2), Range.greaterThan(-2.2)));
    assertOptionalEmpty(optionalIntersection(Range.atMost(-2.2), Range.greaterThan(-2.2)));
    assertOptionalEmpty(optionalIntersection(Range.lessThan(-2.2), Range.atLeast(-2.2)));
    assertOptionalEquals(
        Range.singleton(-2.2),
        optionalIntersection(Range.atMost(-2.2), Range.atLeast(-2.2)));
  }

  @Test
  public void testOptionalIntersection_intersectionIsOpenOnBothEndsButNotSingleton_returnsNonEmptyIntersection() {
    assertOptionalEquals(
        Range.open(2.2, 3.3),
        optionalIntersection(Range.closedOpen(1.1, 3.3), Range.openClosed(2.2, 4.4)));
    assertOptionalEquals(
        Range.openClosed(2.2, 3.3),
        optionalIntersection(Range.closed(1.1, 3.3), Range.openClosed(2.2, 4.4)));
    assertOptionalEquals(
        Range.closedOpen(2.2, 3.3),
        optionalIntersection(Range.closedOpen(1.1, 3.3), Range.closed(2.2, 4.4)));
  }

  @Test
  public void testRangeForAbsValue() {
    Range<Double> unrestricted = Range.atLeast(0.0);

    assertAbs(Range.closed(-1.1, 2.2),     Range.closed(0.0, 2.2));
    assertAbs(Range.closed(-4.4, 3.3),     Range.closed(0.0, 4.4));
    assertAbs(Range.closed(-5.5, 5.5),     Range.closed(0.0, 5.5));

    assertAbs(Range.closedOpen(-1.1, 2.2), Range.closedOpen(0.0, 2.2));
    assertAbs(Range.closedOpen(-4.4, 3.3), Range.closed(0.0, 4.4));
    assertAbs(Range.closedOpen(-5.5, 5.5), Range.closed(0.0, 5.5));

    assertAbs(Range.openClosed(-1.1, 2.2), Range.closed(0.0, 2.2));
    assertAbs(Range.openClosed(-4.4, 3.3), Range.closedOpen(0.0, 4.4));
    assertAbs(Range.openClosed(-5.5, 5.5), Range.closed(0.0, 5.5));

    assertAbs(Range.atLeast(1.1),          Range.atLeast(1.1));
    assertAbs(Range.greaterThan(1.1),      Range.greaterThan(1.1));
    assertAbs(Range.lessThan(1.1),         unrestricted);
    assertAbs(Range.atMost(1.1),           unrestricted);

    assertAbs(Range.atLeast(0.0),          unrestricted);
    assertAbs(Range.greaterThan(0.0),      Range.greaterThan(0.0));
    assertAbs(Range.lessThan(0.0),         Range.greaterThan(0.0));
    assertAbs(Range.atMost(0.0),           unrestricted);

    assertAbs(Range.atLeast(-1.1),         unrestricted);
    assertAbs(Range.greaterThan(-1.1),     unrestricted);
    assertAbs(Range.lessThan(-1.1),        Range.greaterThan(1.1));
    assertAbs(Range.atMost(-1.1),          Range.atLeast(1.1));

    assertAbs(Range.singleton(-1.1),       Range.singleton(1.1));
    assertAbs(Range.singleton(0.0),        Range.singleton(0.0));
    assertAbs(Range.singleton(1.1),        Range.singleton(1.1));

    assertAbs(Range.<Double>all(),         unrestricted);
  }

  private void assertAbs(Range<Double> originalRange, Range<Double> expected) {
    assertThat(rangeForAbsValue(originalRange), doubleRangeMatcher(expected, DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testGetMinMaxOfClosedRanges_throwsOnEmpty() {
    assertIllegalArgumentException( () -> getMinMaxOfClosedRanges(emptyIterator()));
  }

  @Test
  public void testGetMinMaxOfClosedRanges_throwsOnNonClosedRanges() {
    for (Range<ZScore> range : ImmutableList.of(
        Range.atLeast(DUMMY_Z_SCORE),
        Range.greaterThan(DUMMY_Z_SCORE),
        Range.lessThan(DUMMY_Z_SCORE),
        Range.atMost(DUMMY_Z_SCORE),
        Range.open(zScore(1), zScore(3)),
        Range.closedOpen(zScore(1), zScore(3)),
        Range.openClosed(zScore(1), zScore(3)))) {
      // After adding class ClosedRange (March 2020) the exceptions are inside the closedRange static constructor now,
      // so we don't really have to check for that, but let's just keep the test anyway.
      assertIllegalArgumentException( () -> getMinMaxOfClosedRanges(singletonIterator(closedRange(range))));
      assertIllegalArgumentException( () -> getMinMaxOfClosedRanges(ImmutableList.of(closedRange(zScore(1.5), zScore(2.5)), closedRange(range)).iterator()));
      assertIllegalArgumentException( () -> getMinMaxOfClosedRanges(ImmutableList.of(closedRange(zScore(1.5), zScore(2.5)), closedRange(range)).iterator()));
    }
  }

  @Test
  public void testGetMinMaxOfClosedRanges_oneRange() {
    assertThat(
        getMinMaxOfClosedRanges(singletonIterator(singletonClosedRange(ZERO_MONEY))),
        preciseValueClosedRangeMatcher(
            singletonClosedRange(ZERO_MONEY), DEFAULT_EPSILON_1e_8));
    assertThat(
        getMinMaxOfClosedRanges(singletonIterator(singletonClosedRange(money(1.1)))),
        preciseValueClosedRangeMatcher(
            singletonClosedRange(money(1.1)), DEFAULT_EPSILON_1e_8));
    assertThat(
        getMinMaxOfClosedRanges(singletonIterator(closedRange(money(1.1), money(3.3)))),
        preciseValueClosedRangeMatcher(
            closedRange(money(1.1), money(3.3)), DEFAULT_EPSILON_1e_8));
    assertThat(
        getMinMaxOfClosedRanges(singletonIterator(closedRange(ZERO_MONEY, money(3.3)))),
        preciseValueClosedRangeMatcher(
            closedRange(ZERO_MONEY, money(3.3)), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testGetMinMaxOfClosedRanges_multipleRanges_noGaps() {
    assertThat(
        getMinMaxOfClosedRanges(ImmutableList.of(
            closedRange(money(1), money(3)),
            closedRange(money(2), money(4)),
            closedRange(money(3), money(5)))
            .iterator()),
        preciseValueClosedRangeMatcher(
            closedRange(money(1), money(5)), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testGetMinMaxOfClosedRanges_multipleRanges_withGaps() {
    assertThat(
        getMinMaxOfClosedRanges(ImmutableList.of(
            closedRange(money( 1), money( 3)),
            closedRange(money(12), money(14)),
            closedRange(money(23), money(25)))
            .iterator()),
        preciseValueClosedRangeMatcher(
            closedRange(money(1), money(25)), DEFAULT_EPSILON_1e_8));

    assertThat(
        getMinMaxOfClosedRanges(ImmutableList.of(
            closedRange(money(23), money(25)),
            closedRange(money(12), money(14)),
            closedRange(money( 1), money( 3)))
            .iterator()),
        preciseValueClosedRangeMatcher(
            closedRange(money(1), money(25)), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testGetMinMaxOfClosedRanges_varArgs_multipleRanges_noGaps() {
    assertThat(
        getMinMaxOfClosedRanges(
            closedRange(money(1), money(3)),
            closedRange(money(2), money(4)),
            closedRange(money(3), money(5))),
        preciseValueClosedRangeMatcher(
            closedRange(money(1), money(5)), DEFAULT_EPSILON_1e_8));

    assertThat(
        getMinMaxOfClosedRanges(
            closedRange(money(3), money(5)),
            closedRange(money(2), money(4)),
            closedRange(money(1), money(3))),
        preciseValueClosedRangeMatcher(
            closedRange(money(1), money(5)), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testGetMinMaxOfClosedRanges_varArgs_multipleRanges_withGaps() {
    assertThat(
        getMinMaxOfClosedRanges(
            closedRange(money( 1), money( 3)),
            closedRange(money(12), money(14)),
            closedRange(money(23), money(25))),
        preciseValueClosedRangeMatcher(
            closedRange(money(1), money(25)), DEFAULT_EPSILON_1e_8));

    assertThat(
        getMinMaxOfClosedRanges(
            closedRange(money(23), money(25)),
            closedRange(money(12), money(14)),
            closedRange(money( 1), money( 3))),
        preciseValueClosedRangeMatcher(
            closedRange(money(1), money(25)), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testNewLowerBoundFloor() {
    Money NEW_LOWER_BOUND_10 = money(10);

    BiConsumer<Range<Money>, Range<Money>> asserter = (startingRange, expectedResult) ->
        assertThat(
            withNewLowerEndpointFloor(startingRange, NEW_LOWER_BOUND_10),
            rangeEqualityMatcher(expectedResult));

    // Range.all() becomes a range with the new lower bound, but no upper bound:  (-inf, inf) -> [newLower, inf)
    asserter.accept(Range.all(), Range.atLeast(NEW_LOWER_BOUND_10));

    // Ranges with existing lower bounds, but no upper bounds:
    // the new range should have a lower bound that is the max of the existing lower bound or the supplied new one
    asserter.accept(Range.atLeast(ZERO_MONEY),           Range.atLeast(NEW_LOWER_BOUND_10));            // [  0, inf) -> [ 10, inf)
    asserter.accept(Range.atLeast(money(123)),           Range.atLeast(money(123)));                    // [123, inf) -> [123, inf)

    // ranges with 2 bounds: the lower is the max of the existing and the new
    asserter.accept(Range.closed(ZERO_MONEY, money(20)), Range.closed(NEW_LOWER_BOUND_10, money(20)));  // [ 0, 20] -> [10, 20]
    asserter.accept(Range.closed(money(15),  money(20)), Range.closed(money(15), money(20)));           // [15, 20] -> [15, 20]

    // special case: if the existing lower bound matches the new lower bound,
    // but the input range is OPEN, it will become CLOSED after applying the new lower bound
    asserter.accept(Range.greaterThan(NEW_LOWER_BOUND_10),      Range.atLeast(NEW_LOWER_BOUND_10));                // (10, inf) -> [10, inf)
    asserter.accept(Range.open(NEW_LOWER_BOUND_10, money(123)), Range.closedOpen(NEW_LOWER_BOUND_10, money(123))); // (10, 123) -> [10, 123)

    // if a range has no lower bound, the returned range will have the new lower bound, as a CLOSED bound.
    asserter.accept(Range.atMost(money(123)), Range.closed(NEW_LOWER_BOUND_10, money(123)));  // (-inf, 123] -> [10, 123]

    // if the new lower bound is above the existing upper bound, and exception will be thrown
    assertIllegalArgumentException( () -> withNewLowerEndpointFloor(     // [1, 9] -> [10, 9] inverted; exception
        Range.closed(money(1), money(9)),
        NEW_LOWER_BOUND_10));
  }

  @Test
  public void testNewUpperBoundCeiling() {
    Money NEW_UPPER_BOUND_10 = money(10);

    BiConsumer<Range<Money>, Range<Money>> asserter = (startingRange, expectedResult) ->
        assertThat(
            withNewUpperEndpointCeiling(startingRange, NEW_UPPER_BOUND_10),
            rangeEqualityMatcher(expectedResult));

    // Range.all() becomes a range with the new upper bound, but no lower bound:  (-inf, inf) -> (-inf, newLower]
    asserter.accept(Range.all(), Range.atMost(NEW_UPPER_BOUND_10));

    // Ranges with existing upper bounds, but no lower bounds:
    // the new range should have an upper bound that is the main (tighter) of the existing upper bound or the supplied new one
    asserter.accept(Range.atMost(ZERO_MONEY),           Range.atMost(ZERO_MONEY));         // (-inf, 0]   -> (-inf, 0]
    asserter.accept(Range.atMost(money(123)),           Range.atMost(NEW_UPPER_BOUND_10)); // (-inf, 123] -> (-inf, 10]

    // ranges with 2 bounds: the upper is the min(tighter) of the existing and the new
    asserter.accept(Range.closed(money(7), money(12)), Range.closed(money(7), NEW_UPPER_BOUND_10));  // [7, 12] -> [7, 12]
    asserter.accept(Range.closed(money(7), money(8)),  Range.closed(money(7), money(8)));            // [7,  8] -> [7,  8]

    // special case: if the existing upper bound matches the new upper bound,
    // but the input range is OPEN, it will become CLOSED after applying the new lower bound
    asserter.accept(Range.lessThan(NEW_UPPER_BOUND_10),       Range.atMost(NEW_UPPER_BOUND_10));
    asserter.accept(Range.open(money(7), NEW_UPPER_BOUND_10), Range.openClosed(money(7), NEW_UPPER_BOUND_10));

    // if a range has no upper bound, the returned range will have the new upper bound, as a CLOSED bound.
    asserter.accept(Range.atLeast(money(7)), Range.closed(money(7), NEW_UPPER_BOUND_10));

    // if the new upper bound is below the existing lower bound, and exception will be thrown
    assertIllegalArgumentException( () -> withNewUpperEndpointCeiling(     // [11, 19] -> [11, 10] inverted; exception
        Range.closed(money(11), money(19)),
        NEW_UPPER_BOUND_10));
  }

  @Test
  public void testToClosedDoubleRange() {
    for (Range<SignedMoney> nonClosedRange : ImmutableList.of(
        Range.atLeast(DUMMY_SIGNED_MONEY),
        Range.greaterThan(DUMMY_SIGNED_MONEY),
        Range.lessThan(DUMMY_SIGNED_MONEY),
        Range.atMost(DUMMY_SIGNED_MONEY),
        Range.open(signedMoney(-1), signedMoney(3)),
        Range.closedOpen(signedMoney(-1), signedMoney(3)),
        Range.openClosed(signedMoney(-1), signedMoney(3)))) {
      // After the introduction of the class ClosedRange, the exception gets thrown inside the closedRange
      // static constructor, not toClosedDoubleRange, but let's keep this test anyway.
      assertIllegalArgumentException( () -> toClosedDoubleRange(closedRange(nonClosedRange)));
    }
    assertThat(
        toClosedDoubleRange(closedRange(signedMoney(-1), signedMoney(3))),
        closedRangeMatcher(
            closedRange(-1.0, 3.0),
            f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8)));
  }

  @Test
  public void testToClosedDoubleRangeFromImpreciseValue() {
    for (Range<ZScore> nonClosedRange : ImmutableList.of(
        Range.atLeast(DUMMY_Z_SCORE),
        Range.greaterThan(DUMMY_Z_SCORE),
        Range.lessThan(DUMMY_Z_SCORE),
        Range.atMost(DUMMY_Z_SCORE),
        Range.open(zScore(-1), zScore(3)),
        Range.closedOpen(zScore(-1), zScore(3)),
        Range.openClosed(zScore(-1), zScore(3)))) {
      // After the introduction of the class ClosedRange, the exception gets thrown inside the closedRange
      // static constructor, not toClosedDoubleRangeFromImpreciseValue, but let's keep this test anyway.
      assertIllegalArgumentException( () -> toClosedDoubleRangeFromImpreciseValue(closedRange(nonClosedRange)));
    }

    assertThat(
        toClosedDoubleRangeFromImpreciseValue(closedRange(positiveMultiplier(1.1), positiveMultiplier(3.3))),
        closedRangeMatcher(
            closedRange(1.1, 3.3),
            f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8)));
  }

  @Test
  public void testToDoubleRange() {
    BiConsumer<Range<Money>, Range<Double>> asserter = (moneyRange, doubleRange) ->
        assertThat(
            toDoubleRange(moneyRange),
            doubleRangeMatcher(doubleRange, DEFAULT_EPSILON_1e_8));

    asserter.accept(Range.all(),                                  Range.all());
    asserter.accept(Range.atLeast(money(100.0)),                  Range.atLeast(100.0));
    asserter.accept(Range.greaterThan(money(100.0)),              Range.greaterThan(100.0));
    asserter.accept(Range.lessThan(money(100.0)),                 Range.lessThan(100.0));
    asserter.accept(Range.atMost(money(100.0)),                   Range.atMost(100.0));
    asserter.accept(Range.open(money(100.0), money(200.0)),       Range.open(100.0, 200.0));
    asserter.accept(Range.closedOpen(money(100.0), money(200.0)), Range.closedOpen(100.0, 200.0));
    asserter.accept(Range.openClosed(money(100.0), money(200.0)), Range.openClosed(100.0, 200.0));
    asserter.accept(Range.closed(money(100.0), money(200.0)),     Range.closed(100.0, 200.0));
  }

  @Test
  public void testBigDecimalToDoubleRange() {
    BiConsumer<Range<BigDecimal>, Range<Double>> asserter = (bigDecimalRange, doubleRange) ->
        assertThat(
            bigDecimalToDoubleRange(bigDecimalRange),
            doubleRangeMatcher(doubleRange, DEFAULT_EPSILON_1e_8));

    asserter.accept(Range.all(),                                  Range.all());
    asserter.accept(Range.atLeast(    BigDecimal.valueOf(100.0)), Range.atLeast(    100.0));
    asserter.accept(Range.greaterThan(BigDecimal.valueOf(100.0)), Range.greaterThan(100.0));
    asserter.accept(Range.lessThan(   BigDecimal.valueOf(100.0)), Range.lessThan(   100.0));
    asserter.accept(Range.atMost(     BigDecimal.valueOf(100.0)), Range.atMost(     100.0));

    asserter.accept(
        Range.open(       BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0)),
        Range.open(100.0, 200.0));
    asserter.accept(
        Range.closedOpen( BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0)),
        Range.closedOpen(100.0, 200.0));
    asserter.accept(
        Range.openClosed( BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0)),
        Range.openClosed(100.0, 200.0));
    asserter.accept(
        Range.closed(     BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0)),
        Range.closed(100.0, 200.0));
  }

  @Test
  public void testToDoubleRangeFromImpreciseValue() {
    BiConsumer<Range<PositiveMultiplier>, Range<Double>> asserter = (positiveMultiplierRange, doubleRange) ->
        assertThat(
            toDoubleRange(positiveMultiplierRange),
            doubleRangeMatcher(doubleRange, DEFAULT_EPSILON_1e_8));

    asserter.accept(Range.all(),                                                             Range.all());
    asserter.accept(Range.atLeast(    positiveMultiplier(100.0)),                            Range.atLeast(100.0));
    asserter.accept(Range.greaterThan(positiveMultiplier(100.0)),                            Range.greaterThan(100.0));
    asserter.accept(Range.lessThan(   positiveMultiplier(100.0)),                            Range.lessThan(100.0));
    asserter.accept(Range.atMost(     positiveMultiplier(100.0)),                            Range.atMost(100.0));
    asserter.accept(Range.open(       positiveMultiplier(100.0), positiveMultiplier(200.0)), Range.open(100.0, 200.0));
    asserter.accept(Range.closedOpen( positiveMultiplier(100.0), positiveMultiplier(200.0)), Range.closedOpen(100.0, 200.0));
    asserter.accept(Range.openClosed( positiveMultiplier(100.0), positiveMultiplier(200.0)), Range.openClosed(100.0, 200.0));
    asserter.accept(Range.closed(     positiveMultiplier(100.0), positiveMultiplier(200.0)), Range.closed(100.0, 200.0));
  }

  @Test
  public void testTransformClosedRange() {
    for (Range<String> nonClosedRange : ImmutableList.of(
        Range.atLeast("1.1"),
        Range.greaterThan("1.1"),
        Range.lessThan("1.1"),
        Range.atMost("1.1"),
        Range.open("1.1", "2.2"),
        Range.closedOpen("1.1", "2.2"),
        Range.openClosed("1.1", "2.2"))) {
      // After the introduction of the class ClosedRange, the exception gets thrown inside the closedRange
      // static constructor, not toClosedDoubleRange, but let's keep this test anyway.
      assertIllegalArgumentException( () -> transformClosedRange(closedRange(nonClosedRange), v -> parseDouble(v)));
    }
    assertThat(
        transformClosedRange(closedRange("1.1", "2.2"), v -> parseDouble(v)),
        closedRangeMatcher(
            closedRange(1.1, 2.2),
            f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8)));
  }

  @Test
  public void testWithNewLowerEndpoint() {
    BiConsumer<Range<Double>, Range<Double>> asserter = (startingRange, expectedResult) ->
        assertThat(
            RBRanges.withNewLowerEndpoint(
                startingRange,
                existingLowerBound -> transformOptional(existingLowerBound, v -> v - 8).orElse(7.0)),
            doubleRangeMatcher(expectedResult, DEFAULT_EPSILON_1e_8));

    asserter.accept(Range.all(),                    Range.atLeast(7.0));
    asserter.accept(Range.atLeast(100.0),           Range.atLeast(doubleExplained(92, 100 - 8)));
    asserter.accept(Range.greaterThan(100.0),       Range.greaterThan(92.0));
    asserter.accept(Range.lessThan(100.0),          Range.closedOpen(7.0, 100.0));
    asserter.accept(Range.atMost(100.0),            Range.closed(7.0, 100.0));
    asserter.accept(Range.open(100.0, 200.0),       Range.open(92.0, 200.0));
    asserter.accept(Range.closedOpen(100.0, 200.0), Range.closedOpen(92.0, 200.0));
    asserter.accept(Range.openClosed(100.0, 200.0), Range.openClosed(92.0, 200.0));
    asserter.accept(Range.closed(100.0, 200.0),     Range.closed(92.0, 200.0));

    UnaryOperator<Range<Double>> maker = startingRange ->
        RBRanges.withNewLowerEndpoint(
            startingRange,
            existingLowerBound -> transformOptional(existingLowerBound, v -> v + 88).orElse(77.0));
    // all of these will become inverted ranges of 100.01 , 100
    assertIllegalArgumentException( () -> maker.apply(Range.open(doubleExplained(12.01, 100 - 88 + 0.01), 100.0)));
    assertIllegalArgumentException( () -> maker.apply(Range.closedOpen(12.01, 100.0)));
    assertIllegalArgumentException( () -> maker.apply(Range.openClosed(12.01, 100.0)));
    assertIllegalArgumentException( () -> maker.apply(Range.closed(12.01, 100.0)));
    // all of these will become inverted ranges of 77, 76.99
    assertIllegalArgumentException( () -> maker.apply(Range.atMost(doubleExplained(76.99, 77 - 0.01))));
    assertIllegalArgumentException( () -> maker.apply(Range.lessThan(doubleExplained(76.99, 77 - 0.01))));

    assertThat(
        maker.apply(Range.closed(12.0, 100.0)),
        doubleRangeMatcher(Range.singleton(100.0), DEFAULT_EPSILON_1e_8));
    assertThat(
        maker.apply(Range.atMost(77.0)),
        doubleRangeMatcher(Range.singleton(77.0), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testWithNewDecreasedClosedLowerEndpoint() {
    BiConsumer<Range<Double>, Range<Double>> asserter = (startingRange, expectedResult) ->
        assertThat(
            RBRanges.withNewDecreasedClosedLowerEndpoint(
                startingRange,
                existingLowerBound -> existingLowerBound - 8.0),
            doubleRangeMatcher(expectedResult, DEFAULT_EPSILON_1e_8));

    asserter.accept(Range.atLeast(    100.0),        Range.atLeast(doubleExplained(92, 100 - 8)));
    asserter.accept(Range.greaterThan(100.0),        Range.atLeast(    92.0));        // open lower bound becomes closed
    asserter.accept(Range.open(       100.0, 200.0), Range.closedOpen( 92.0, 200.0));
    asserter.accept(Range.closedOpen( 100.0, 200.0), Range.closedOpen( 92.0, 200.0));
    asserter.accept(Range.openClosed( 100.0, 200.0), Range.closed(     92.0, 200.0));
    asserter.accept(Range.closed(     100.0, 200.0), Range.closed(     92.0, 200.0));
    asserter.accept(Range.singleton(  100.0),        Range.closed(     92.0, 100.0));

    // the starting range must have a lower endpoint
    assertIllegalArgumentException( () -> RBRanges.withNewDecreasedClosedLowerEndpoint(Range.lessThan(100.0), v -> v - 8.0));
    assertIllegalArgumentException( () -> RBRanges.withNewDecreasedClosedLowerEndpoint(Range.atMost(  100.0), v -> v - 8.0));
    assertIllegalArgumentException( () -> RBRanges.<Double>withNewDecreasedClosedLowerEndpoint(Range.all(),   v -> v - 8.0));

    // an open endpoint can be "extended" to a closed endpoint
    assertThat(
        RBRanges.withNewDecreasedClosedLowerEndpoint(Range.open(100.0, 200.0), v -> v),
        doubleRangeMatcher(Range.closedOpen(100.0, 200.0), DEFAULT_EPSILON_1e_8));

    // the new lower endpoint must be strictly less than the starting lower endpoint
    Range<Double> doesNotThrow;
    doesNotThrow = RBRanges.withNewDecreasedClosedLowerEndpoint(Range.closed(100.0, 200.0), v -> v - 1);
    doesNotThrow = RBRanges.withNewDecreasedClosedLowerEndpoint(Range.closed(100.0, 200.0), v -> v - 1e-9);

    assertIllegalArgumentException( () -> RBRanges.withNewDecreasedClosedLowerEndpoint(Range.closed(100.0, 200.0), v -> v + 0.0));
    assertIllegalArgumentException( () -> RBRanges.withNewDecreasedClosedLowerEndpoint(Range.closed(100.0, 200.0), v -> v + 1e-9));
    assertIllegalArgumentException( () -> RBRanges.withNewDecreasedClosedLowerEndpoint(Range.closed(100.0, 200.0), v -> v + 1.0));
  }

  @Test
  public void testWithNewIncreasedClosedUpperEndpoint() {
    BiConsumer<Range<Double>, Range<Double>> asserter = (startingRange, expectedResult) ->
        assertThat(
            RBRanges.withNewIncreasedClosedUpperEndpoint(
                startingRange,
                existingUpperBound -> existingUpperBound + 8.0),
            doubleRangeMatcher(expectedResult, DEFAULT_EPSILON_1e_8));

    asserter.accept(Range.atMost(            200.0), Range.atMost(doubleExplained(208, 200 + 8)));
    asserter.accept(Range.lessThan(          200.0), Range.atMost(            208.0)); // open upper bound becomes closed
    asserter.accept(Range.open(       100.0, 200.0), Range.openClosed( 100.0, 208.0));
    asserter.accept(Range.closedOpen( 100.0, 200.0), Range.closed(     100.0, 208.0));
    asserter.accept(Range.openClosed( 100.0, 200.0), Range.openClosed( 100.0, 208.0));
    asserter.accept(Range.closed(     100.0, 200.0), Range.closed(     100.0, 208.0));
    asserter.accept(Range.singleton(  100.0),        Range.closed(     100.0, 108.0));

    // the starting range must have a upper endpoint
    assertIllegalArgumentException( () -> RBRanges.withNewIncreasedClosedUpperEndpoint(Range.greaterThan(100.0), v -> v + 8.0));
    assertIllegalArgumentException( () -> RBRanges.withNewIncreasedClosedUpperEndpoint(Range.atLeast(    100.0), v -> v + 8.0));
    assertIllegalArgumentException( () -> RBRanges.<Double>withNewIncreasedClosedUpperEndpoint(Range.all(),      v -> v + 8.0));

    // an open endpoint can be "extended" to a closed endpoint
    assertThat(
        RBRanges.withNewIncreasedClosedUpperEndpoint(Range.open(100.0, 200.0), v -> v + 0),
        doubleRangeMatcher(Range.openClosed(100.0, 200.0), DEFAULT_EPSILON_1e_8));

    // the new upper endpoint must be strictly greater than the starting range
    Range<Double> doesNotThrow;
    doesNotThrow = RBRanges.withNewIncreasedClosedUpperEndpoint(Range.closed(100.0, 200.0), v -> v + 1);
    doesNotThrow = RBRanges.withNewIncreasedClosedUpperEndpoint(Range.closed(100.0, 200.0), v -> v + 1e-9);

    assertIllegalArgumentException( () -> RBRanges.withNewIncreasedClosedUpperEndpoint(Range.closed(100.0, 200.0), v -> v - 0.0));
    assertIllegalArgumentException( () -> RBRanges.withNewIncreasedClosedUpperEndpoint(Range.closed(100.0, 200.0), v -> v - 1e-9));
    assertIllegalArgumentException( () -> RBRanges.withNewIncreasedClosedUpperEndpoint(Range.closed(100.0, 200.0), v -> v - 1.0));
  }

  @Test
  public void testWithPossiblyNewLowerEndpoint() {
    BiConsumer<Range<Double>, Range<Double>> asserter = (startingRange, expectedResult) ->
        assertThat(
            RBRanges.withPossiblyNewLowerEndpoint(
                startingRange,
                existingLowerBound -> transformOptional(existingLowerBound, v -> v - 8)),
            doubleRangeMatcher(expectedResult, DEFAULT_EPSILON_1e_8));

    asserter.accept(Range.all(),                    Range.all());
    asserter.accept(Range.atLeast(100.0),           Range.atLeast(doubleExplained(92, 100 - 8)));
    asserter.accept(Range.greaterThan(100.0),       Range.greaterThan(92.0));
    asserter.accept(Range.lessThan(100.0),          Range.lessThan(100.0));
    asserter.accept(Range.atMost(100.0),            Range.atMost(100.0));
    asserter.accept(Range.open(100.0, 200.0),       Range.open(92.0, 200.0));
    asserter.accept(Range.closedOpen(100.0, 200.0), Range.closedOpen(92.0, 200.0));
    asserter.accept(Range.openClosed(100.0, 200.0), Range.openClosed(92.0, 200.0));
    asserter.accept(Range.closed(100.0, 200.0),     Range.closed(92.0, 200.0));

    Consumer<Range<Double>> assertThrows = startingRange ->
        assertIllegalArgumentException( () -> RBRanges.withPossiblyNewLowerEndpoint(
            startingRange,
            existingLowerBound -> transformOptional(existingLowerBound, v -> v + 88)));
    assertThrows.accept(Range.open(doubleExplained(12.01, 100 - 88 + 0.01), 100.0));
    assertThrows.accept(Range.closedOpen(doubleExplained(12.01, 100 - 88 + 0.01), 100.0));
    assertThrows.accept(Range.openClosed(doubleExplained(12.01, 100 - 88 + 0.01), 100.0));

    UnaryOperator<Range<Double>> maker = startingRange ->
        RBRanges.withPossiblyNewLowerEndpoint(
            startingRange,
            existingLowerBound -> transformOptional(existingLowerBound, v -> v + 88));
    // all of these will become inverted ranges of 100.01 , 100
    assertIllegalArgumentException( () -> maker.apply(Range.open(doubleExplained(12.01, 100 - 88 + 0.01), 100.0)));
    assertIllegalArgumentException( () -> maker.apply(Range.closedOpen(12.01, 100.0)));
    assertIllegalArgumentException( () -> maker.apply(Range.openClosed(12.01, 100.0)));
    assertIllegalArgumentException( () -> maker.apply(Range.closed(12.01, 100.0)));

    assertThat(
        maker.apply(Range.closed(12.0, 100.0)),
        doubleRangeMatcher(Range.singleton(100.0), DEFAULT_EPSILON_1e_8));
    assertThat(
        maker.apply(Range.atLeast(1.0)),
        doubleRangeMatcher(Range.atLeast(89.0), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testWithNewUpperEndpoint() {
    BiConsumer<Range<Double>, Range<Double>> asserter = (startingRange, expectedResult) ->
        assertThat(
            RBRanges.withNewUpperEndpoint(
                startingRange,
                existingUpperBound -> transformOptional(existingUpperBound, v -> v + 8).orElse(777.0)),
            doubleRangeMatcher(expectedResult, DEFAULT_EPSILON_1e_8));

    asserter.accept(Range.all(),                    Range.atMost(777.0));
    asserter.accept(Range.atLeast(100.0),           Range.closed(100.0, 777.0));
    asserter.accept(Range.greaterThan(100.0),       Range.openClosed(100.0, 777.0));
    asserter.accept(Range.lessThan(100.0),          Range.lessThan(doubleExplained(108, 100 + 8)));
    asserter.accept(Range.atMost(100.0),            Range.atMost(108.0));
    asserter.accept(Range.open(100.0, 200.0),       Range.open(100.0, 208.0));
    asserter.accept(Range.closedOpen(100.0, 200.0), Range.closedOpen(100.0, 208.0));
    asserter.accept(Range.openClosed(100.0, 200.0), Range.openClosed(100.0, 208.0));
    asserter.accept(Range.closed(100.0, 200.0),     Range.closed(100.0, 208.0));

    UnaryOperator<Range<Double>> maker = startingRange ->
        RBRanges.withNewUpperEndpoint(
            startingRange,
            existingUpperBound -> transformOptional(existingUpperBound, v -> v - 88).orElse(77.0));
    // all of these will become inverted ranges of 12.01 , 12
    assertIllegalArgumentException( () -> maker.apply(Range.open(12.01, 100.0)));
    assertIllegalArgumentException( () -> maker.apply(Range.closedOpen(12.01, 100.0)));
    assertIllegalArgumentException( () -> maker.apply(Range.openClosed(12.01, 100.0)));
    assertIllegalArgumentException( () -> maker.apply(Range.closed(12.01, 100.0)));
    // all of these will become inverted ranges of 77.01, 77
    assertIllegalArgumentException( () -> maker.apply(Range.atLeast(doubleExplained(77.01, 77 + 0.01))));
    assertIllegalArgumentException( () -> maker.apply(Range.greaterThan(77.01)));

    assertThat(
        maker.apply(Range.closed(12.0, 100.0)),
        doubleRangeMatcher(Range.singleton(12.0), DEFAULT_EPSILON_1e_8));
    assertThat(
        maker.apply(Range.atLeast(77.0)),
        doubleRangeMatcher(Range.singleton(77.0), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testWithPossiblyNewUpperEndpoint() {
    BiConsumer<Range<Double>, Range<Double>> asserter = (startingRange, expectedResult) ->
        assertThat(
            RBRanges.withPossiblyNewUpperEndpoint(
                startingRange,
                existingUpperBound -> transformOptional(existingUpperBound, v -> v + 8)),
            doubleRangeMatcher(expectedResult, DEFAULT_EPSILON_1e_8));

    asserter.accept(Range.all(),                    Range.all());
    asserter.accept(Range.atLeast(100.0),           Range.atLeast(100.0));
    asserter.accept(Range.greaterThan(100.0),       Range.greaterThan(100.0));
    asserter.accept(Range.lessThan(100.0),          Range.lessThan(doubleExplained(108, 100 + 8)));
    asserter.accept(Range.atMost(100.0),            Range.atMost(108.0));
    asserter.accept(Range.open(100.0, 200.0),       Range.open(100.0, 208.0));
    asserter.accept(Range.closedOpen(100.0, 200.0), Range.closedOpen(100.0, 208.0));
    asserter.accept(Range.openClosed(100.0, 200.0), Range.openClosed(100.0, 208.0));
    asserter.accept(Range.closed(100.0, 200.0),     Range.closed(100.0, 208.0));


    UnaryOperator<Range<Double>> maker = startingRange ->
        RBRanges.withPossiblyNewUpperEndpoint(
            startingRange,
            existingUpperBound -> transformOptional(existingUpperBound, v -> v - 88));
    // all of these will become inverted ranges of 12.01 , 12
    assertIllegalArgumentException( () -> maker.apply(Range.open(12.01, 100.0)));
    assertIllegalArgumentException( () -> maker.apply(Range.closedOpen(12.01, 100.0)));
    assertIllegalArgumentException( () -> maker.apply(Range.openClosed(12.01, 100.0)));
    assertIllegalArgumentException( () -> maker.apply(Range.closed(12.01, 100.0)));
  }

  @Test
  public void testMinimallyLoosenToOverlapRange() {
    TriConsumer<Range<Double>, Range<Double>, Range<Double>> asserter = (initial, other, expected) ->
        assertThat(
            minimallyLoosenRangeToTouchOtherRange(initial, other),
            doubleRangeMatcher(expected, DEFAULT_EPSILON_1e_8));

    // if the ranges overlap, the initial range is returned
    asserter.accept(Range.closed( 3.0, 5.0), Range.singleton(4.0),    Range.closed( 3.0, 5.0));
    asserter.accept(Range.closed( 3.0, 5.0), Range.closed( 4.0, 4.1), Range.closed( 3.0, 5.0));
    asserter.accept(Range.closed( 3.0, 5.0), Range.closed( 1.0, 9.0), Range.closed( 3.0, 5.0));

    asserter.accept(Range.closed( 3.0, 5.0), Range.closed( 4.0, 9.0), Range.closed( 3.0, 5.0));
    asserter.accept(Range.closed( 3.0, 5.0), Range.closed( 0.0, 4.0), Range.closed( 3.0, 5.0));

    asserter.accept(Range.closed( 3.0, 5.0), Range.closed( 5.0, 9.0), Range.closed( 3.0, 5.0));
    asserter.accept(Range.closed( 3.0, 5.0), Range.closed(-2.0, 3.0), Range.closed( 3.0, 5.0));

    // an open range will ramain open if the 'other' range overlaps it
    asserter.accept(Range.open(3.0, 5.0), Range.singleton(4.0),    Range.open( 3.0, 5.0));
    asserter.accept(Range.open(3.0, 5.0), Range.closed( 4.0, 4.1), Range.open( 3.0, 5.0));
    asserter.accept(Range.open(3.0, 5.0), Range.closed( 1.0, 9.0), Range.open( 3.0, 5.0));

    // if the ranges would overlap at one point except for an open/closed combination, the range is extended to that point
    asserter.accept(Range.open(3.0, 5.0), Range.closedOpen( 5.0, 9.0), Range.openClosed(3.0, 5.0));
    asserter.accept(Range.open(3.0, 5.0), Range.openClosed(-2.0, 3.0), Range.closedOpen(3.0, 5.0));

    // If the ranges would overlap except that both are open, we extend the inital to include the boundary point.
    // The name is not quite accurate since the new range does not overlap 'other', but we need to keep
    // names manageably short.
    asserter.accept(Range.open(3.0, 5.0), Range.open( 5.0, 9.0), Range.openClosed(3.0, 5.0));
    asserter.accept(Range.open(3.0, 5.0), Range.open(-2.0, 3.0), Range.closedOpen(3.0, 5.0));

    // if they don't overlap, the initial range is extended just enough to overlap the other by one point
    asserter.accept(Range.closed( 3.0, 5.0), Range.closed( 7.0, 9.0), Range.closed( 3.0, 7.0));
    asserter.accept(Range.closed( 7.0, 9.0), Range.closed( 3.0, 5.0), Range.closed( 5.0, 9.0));

    asserter.accept(Range.closed( 3.0, 5.0), Range.closed(-2.0, 1.0), Range.closed( 1.0, 5.0));
    asserter.accept(Range.closed(-2.0, 1.0), Range.closed( 3.0, 5.0), Range.closed(-2.0, 3.0));
  }

  @Test
  public void testGetNearestValueInRange() {
    TriConsumer<Range<Double>, Double, Double> asserter = (range, startingValue, expectedResult) ->
        assertEquals(
            getNearestValueInRange(range, startingValue),
            expectedResult,
            1e-8);
    asserter.accept(Range.all(),                     12.3, 12.3);

    asserter.accept(Range.atLeast(100.0),            99.9, 100.0);
    asserter.accept(Range.atLeast(100.0),           100.0, 100.0);
    asserter.accept(Range.atLeast(100.0),           100.1, 100.1);

    asserter.accept(Range.greaterThan(100.0),        99.9, 100.0);
    // technically 100 is not greater than 100, but there's no well-defined way next point above 100,
    // so we define the correct result in this case to be 100.
    asserter.accept(Range.greaterThan(100.0),       100.0, 100.0);
    asserter.accept(Range.greaterThan(100.0),       100.1, 100.1);

    asserter.accept(Range.atMost(100.0),             99.9,  99.9);
    asserter.accept(Range.atMost(100.0),            100.0, 100.0);
    asserter.accept(Range.atMost(100.0),            100.1, 100.0);

    asserter.accept(Range.lessThan(100.0),           99.9,  99.9);
    // technically 100 is not less than 100, but there's no well-defined way next point below 100,
    // so we define the correct result in this case to be 100.
    asserter.accept(Range.lessThan(100.0),          100.0, 100.0);
    asserter.accept(Range.lessThan(100.0),          100.1, 100.0);

    rbSetOf(
        Range.open(100.0, 200.0),
        Range.closedOpen(100.0, 200.0),
        Range.openClosed(100.0, 200.0),
        Range.closed(100.0, 200.0)).forEach(range -> {
      asserter.accept(range,  99.9, 100.0);
      asserter.accept(range, 100.0, 100.0);
      asserter.accept(range, 150.0, 150.0);
      asserter.accept(range, 200.0, 200.0);
      asserter.accept(range, 200.1, 200.0);
    });

    asserter.accept(Range.singleton(100.0),  99.9, 100.0);
    asserter.accept(Range.singleton(100.0), 100.0, 100.0);
    asserter.accept(Range.singleton(100.0), 100.1, 100.0);
  }

  @Test
  public void testTransformToBigDecimalRange() {
    BiConsumer<Range<Money>, Range<BigDecimal>> asserter = (initialRange, expectedResult) ->
        assertThat(
            transformToBigDecimalRange(initialRange),
            rangeEqualityMatcher(expectedResult));

    asserter.accept(
        Range.closed(money(1.1), money(3.3)),
        Range.closed(BigDecimal.valueOf(1.1), BigDecimal.valueOf(3.3)));
    asserter.accept(
        Range.closedOpen(money(1.1), money(3.3)),
        Range.closedOpen(BigDecimal.valueOf(1.1), BigDecimal.valueOf(3.3)));
    asserter.accept(
        Range.openClosed(money(1.1), money(3.3)),
        Range.openClosed(BigDecimal.valueOf(1.1), BigDecimal.valueOf(3.3)));
    asserter.accept(
        Range.open(money(1.1), money(3.3)),
        Range.open(BigDecimal.valueOf(1.1), BigDecimal.valueOf(3.3)));

    asserter.accept(
        Range.atLeast(money(1.1)),
        Range.atLeast(BigDecimal.valueOf(1.1)));
    asserter.accept(
        Range.atMost(money(3.3)),
        Range.atMost(BigDecimal.valueOf(3.3)));

    asserter.accept(
        Range.all(),
        Range.all());
  }

  @Test
  public void testTransformRange() {
    BiConsumer<Range<Integer>, Range<Integer>> asserter = (initialRange, expectedResult) ->
        assertThat(
            transformRange(initialRange, v -> v + 7),
            rangeEqualityMatcher(expectedResult));
    asserter.accept(Range.all(),                Range.all());
    asserter.accept(Range.singleton(100),       Range.singleton(107));
    asserter.accept(Range.atLeast(100),         Range.atLeast(107));
    asserter.accept(Range.greaterThan(100),     Range.greaterThan(107));
    asserter.accept(Range.lessThan(100),        Range.lessThan(107));
    asserter.accept(Range.atMost(100),          Range.atMost(107));
    asserter.accept(Range.open(100, 200),       Range.open(107, 207));
    asserter.accept(Range.closedOpen(100, 200), Range.closedOpen(107, 207));
    asserter.accept(Range.openClosed(100, 200), Range.openClosed(107, 207));
    asserter.accept(Range.closed(100, 200),     Range.closed(107, 207));
  }

  @Test
  public void testTransformRange_invertsOrdering_throwsOnlyWhenBothLowerAndUpperExist() {
    BiConsumer<Range<Integer>, Range<Integer>> asserter = (initialRange, expectedResult) ->
        assertThat(
            transformRange(initialRange, v -> -(v + 7)),
            rangeEqualityMatcher(expectedResult));
    asserter.accept(Range.all(),            Range.all());
    asserter.accept(Range.singleton(  100), Range.singleton(  -107));
    asserter.accept(Range.atLeast(    100), Range.atLeast(    -107));
    asserter.accept(Range.greaterThan(100), Range.greaterThan(-107));
    asserter.accept(Range.lessThan(   100), Range.lessThan(   -107));
    asserter.accept(Range.atMost(     100), Range.atMost(     -107));

    assertIllegalArgumentException( () -> transformRange(Range.open(      100, 200), v -> -(v + 7)));
    assertIllegalArgumentException( () -> transformRange(Range.closedOpen(100, 200), v -> -(v + 7)));
    assertIllegalArgumentException( () -> transformRange(Range.openClosed(100, 200), v -> -(v + 7)));
    assertIllegalArgumentException( () -> transformRange(Range.closed(    100, 200), v -> -(v + 7)));
  }

  @Test
  public void testConstructRange() {
    BiConsumer<Range<Integer>, Range<Integer>> asserter = (constructedRange, expectedResult) ->
        assertThat(
            constructedRange,
            rangeEqualityMatcher(expectedResult));
    Optional<Integer> NO_LOWER = Optional.empty();
    Optional<Integer> NO_UPPER = Optional.empty();
    asserter.accept(constructRange(NO_LOWER, CLOSED, NO_UPPER, CLOSED), Range.all());
    asserter.accept(constructRange(NO_LOWER, CLOSED, NO_UPPER, OPEN),   Range.all());
    asserter.accept(constructRange(NO_LOWER, OPEN,   NO_UPPER, CLOSED), Range.all());
    asserter.accept(constructRange(NO_LOWER, OPEN,   NO_UPPER, OPEN),   Range.all());

    asserter.accept(constructRange(Optional.of(100), CLOSED, Optional.of(100), CLOSED), Range.singleton(100));
    asserter.accept(constructRange(Optional.of(100), CLOSED, NO_UPPER,         OPEN),   Range.atLeast(100));
    asserter.accept(constructRange(Optional.of(100), OPEN,   NO_UPPER,         OPEN),   Range.greaterThan(100));
    asserter.accept(constructRange(NO_LOWER,         OPEN,   Optional.of(100), OPEN),   Range.lessThan(100));
    asserter.accept(constructRange(NO_LOWER,         OPEN,   Optional.of(100), CLOSED), Range.atMost(100));
    asserter.accept(constructRange(Optional.of(100), OPEN,   Optional.of(200), OPEN),   Range.open(100, 200));
    asserter.accept(constructRange(Optional.of(100), CLOSED, Optional.of(200), OPEN),   Range.closedOpen(100, 200));
    asserter.accept(constructRange(Optional.of(100), OPEN,   Optional.of(200), CLOSED), Range.openClosed(100, 200));
    asserter.accept(constructRange(Optional.of(100), CLOSED, Optional.of(200), CLOSED), Range.closed(100, 200));
  }

  @Test
  public void testRangeIsUnrestricted() {
    assertTrue(rangeIsUnrestricted(Range.<Double>all()));
    rbSetOf(
        Range.singleton(100.0),
        Range.atLeast(100.0),
        Range.greaterThan(100.0),
        Range.lessThan(100.0),
        Range.atMost(100.0),
        Range.open(100.0, 200.0),
        Range.closedOpen(100.0, 200.0),
        Range.openClosed(100.0, 200.0),
        Range.closed(100.0, 200.0))
        .forEach(range -> assertFalse(rangeIsUnrestricted(range)));
  }

  @Test
  public void testValidateAgainstExtremes() {
    // Instead of having a bunch of one-liner assertions, we first instantiate all the ranges, so that when there is
    // an exception, we'll know it's not coming from the constructors of Range, and is instead coming from
    // validateAgainstExtremes, as expected.
    rbSetOf(
        Range.singleton(NaN),
        Range.singleton(NEGATIVE_INFINITY),
        Range.singleton(POSITIVE_INFINITY),
        Range.openClosed(NEGATIVE_INFINITY, DUMMY_DOUBLE),
        Range.openClosed(NEGATIVE_INFINITY, NEGATIVE_INFINITY),
        Range.closed(NEGATIVE_INFINITY, DUMMY_DOUBLE),
        Range.openClosed(DUMMY_DOUBLE, POSITIVE_INFINITY),
        Range.openClosed(POSITIVE_INFINITY, POSITIVE_INFINITY),
        Range.closed(DUMMY_DOUBLE, POSITIVE_INFINITY))
        .forEach(range -> assertIllegalArgumentException( () -> validateAgainstExtremes(range)));

    allRanges(1.23, 4.56).forEach(range -> validateAgainstExtremes(range)); // i.e. does not throw
  }

  @Test
  public void testRangeIsProperSubsetOf() {
    Range<Double> closedRange1to10 = Range.closed(1.0, 10.0);

    assertTrue( rangeIsProperSubsetOnBothEnds(closedRange1to10, Range.closed(0.0,        11.0)));
    assertTrue( rangeIsProperSubsetOnBothEnds(closedRange1to10, Range.closed(1.0 - 1e-9, 10.0 + 1e-9)));

    assertFalse(rangeIsProperSubsetOnBothEnds(closedRange1to10, Range.closed(1.0 + 1e-9, 10.0       )));
    assertFalse(rangeIsProperSubsetOnBothEnds(closedRange1to10, Range.closed(1.0,        10.0 - 1e-9)));
    assertFalse(rangeIsProperSubsetOnBothEnds(closedRange1to10, Range.closed(1.0 + 1e-9, 10.0 - 1e-9)));

    // a range is not a proper subset of itself
    assertFalse(rangeIsProperSubsetOnBothEnds(closedRange1to10, closedRange1to10));

    // singleton ranges are not proper subsets of themselves
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.singleton(10.0), Range.singleton(10.0)));

    // open ranges are supported
    assertTrue( rangeIsProperSubsetOnBothEnds(Range.open(      2.0,  9.0), closedRange1to10));
    assertTrue( rangeIsProperSubsetOnBothEnds(Range.open(      1.0, 10.0), closedRange1to10));
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.openClosed(1.0, 10.0), closedRange1to10));
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.closedOpen(1.0, 10.0), closedRange1to10));

    assertFalse(rangeIsProperSubsetOnBothEnds(Range.greaterThan(1.0),      closedRange1to10));
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.lessThan(       10.0), closedRange1to10));

    assertTrue( rangeIsProperSubsetOnBothEnds(Range.open(      1.0, 10.0), closedRange1to10));
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.openClosed(1.0, 10.0), closedRange1to10));
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.closedOpen(1.0, 10.0), closedRange1to10));

    assertTrue( rangeIsProperSubsetOnBothEnds(closedRange1to10, Range.open(0.0, 11.0)));

    // singleton ranges are supported
    assertTrue( rangeIsProperSubsetOnBothEnds(Range.singleton( 5.0), closedRange1to10));
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.singleton( 1.0), closedRange1to10));
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.singleton(10.0), closedRange1to10));

    // 'unrestricted' ranges are supported. E.g. (-inf, a], [b, inf), (-inf, inf)
    assertTrue( rangeIsProperSubsetOnBothEnds(closedRange1to10, Range.greaterThan(0.0)));
    assertTrue( rangeIsProperSubsetOnBothEnds(closedRange1to10, Range.greaterThan(1.0 - 1e-9)));
    assertTrue( rangeIsProperSubsetOnBothEnds(closedRange1to10, Range.lessThan(  10.0 + 1e-9)));
    assertTrue( rangeIsProperSubsetOnBothEnds(closedRange1to10, Range.lessThan(  11.0)));
    assertTrue( rangeIsProperSubsetOnBothEnds(closedRange1to10, Range.all()));

    // if both ranges are 'unrestricted' on the same end, we compare only the other end
    assertTrue( rangeIsProperSubsetOnBothEnds(Range.greaterThan(1.0), Range.greaterThan(0.0)));
    assertTrue( rangeIsProperSubsetOnBothEnds(Range.greaterThan(1.0), Range.atLeast(    1.0)));
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.atLeast(    1.0), Range.greaterThan(1.0)));
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.greaterThan(1.0), Range.greaterThan(2.0)));
    assertTrue( rangeIsProperSubsetOnBothEnds(Range.greaterThan(1.0), Range.all()));
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.lessThan(  10.0), Range.lessThan(   9.0)));
    assertTrue( rangeIsProperSubsetOnBothEnds(Range.lessThan(  10.0), Range.atMost(    10.0)));
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.atMost(    10.0), Range.lessThan(  10.0)));
    assertTrue( rangeIsProperSubsetOnBothEnds(Range.lessThan(  10.0), Range.lessThan(  11.0)));
    assertTrue( rangeIsProperSubsetOnBothEnds(Range.lessThan(  10.0), Range.all()));

    // of course, being unrestricted on opposite ends cannot be a proper subset
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.atLeast(1.0), Range.atMost(10.0)));
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.atMost(10.0), Range.atLeast(1.0)));

    // if both ranges are fully unrestricted, then it's not a proper subset
    assertFalse(rangeIsProperSubsetOnBothEnds(Range.<Double>all(), Range.<Double>all()));
  }

  @Test
  public void testRangeIsSafelyProperSubsetOf() {
    Range<Money> closedRange1to10 = Range.closed(money(1), money(10));

    assertTrue( rangeIsSafelyProperSubsetOf(closedRange1to10, Range.closed(ZERO_MONEY, money(11)), DEFAULT_EPSILON_1e_8));
    assertFalse(rangeIsSafelyProperSubsetOf(closedRange1to10, Range.closed(ZERO_MONEY, money(10)), DEFAULT_EPSILON_1e_8));
    assertFalse(rangeIsSafelyProperSubsetOf(closedRange1to10, Range.closed(money(1.0), money(11)), DEFAULT_EPSILON_1e_8));
    assertFalse(rangeIsSafelyProperSubsetOf(closedRange1to10, closedRange1to10, DEFAULT_EPSILON_1e_8));

    // the size of epsilon can determine whether one range is "safely" a proper subset of another
    assertTrue( rangeIsSafelyProperSubsetOf(closedRange1to10, Range.closed(ZERO_MONEY, money(10.01)), DEFAULT_EPSILON_1e_8));
    assertTrue( rangeIsSafelyProperSubsetOf(closedRange1to10, Range.closed(ZERO_MONEY, money(10.01)), epsilon(0.009)));
    assertFalse(rangeIsSafelyProperSubsetOf(closedRange1to10, Range.closed(ZERO_MONEY, money(10.01)), epsilon(0.02)));

    assertTrue( rangeIsSafelyProperSubsetOf(closedRange1to10, Range.closed(money(0.99), money(11)), DEFAULT_EPSILON_1e_8));
    assertTrue( rangeIsSafelyProperSubsetOf(closedRange1to10, Range.closed(money(0.99), money(11)), epsilon(0.009)));
    assertFalse(rangeIsSafelyProperSubsetOf(closedRange1to10, Range.closed(money(0.99), money(11)), epsilon(0.02)));

    // zero epsilon is valid
    assertTrue(rangeIsSafelyProperSubsetOf(closedRange1to10, Range.closed(ZERO_MONEY, money(11)), ZERO_EPSILON));

    // an open range is not a  "safely" proper subset of the equivalent closed range
    assertFalse(rangeIsSafelyProperSubsetOf(
        Range.open(  money(1), money(10)),
        Range.closed(money(1), money(10)),
        DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testToRangeWithoutTrivialEndpoints() {
    BiConsumer<ClosedRange<UnitFraction>, Range<UnitFraction>> asserter = (inputRange, expectedResult) ->
        assertThat(
            toRangeWithoutTrivialEndpoints(inputRange, closedRange(UNIT_FRACTION_0, UNIT_FRACTION_1)),
            // There are no epsilons here; toRangeWithoutTrivialEndpoints uses compareTo for exact comparison.
            preciseValueRangeMatcher(expectedResult, epsilon(1e-12)));

    asserter.accept(closedRange(UNIT_FRACTION_0, UNIT_FRACTION_1), Range.all());
    asserter.accept(
        closedRange(UNIT_FRACTION_0, unitFraction(1 - 1e-9)),
        Range.atMost(unitFraction(1 - 1e-9)));
    asserter.accept(
        closedRange(unitFraction(1e-9), unitFraction(1 - 1e-9)),
        Range.closed(unitFraction(1e-9), unitFraction(1 - 1e-9)));
    asserter.accept(
        closedRange(unitFraction(1e-9), UNIT_FRACTION_1),
        Range.atLeast(unitFraction(1e-9)));

    asserter.accept(
        closedRange(UNIT_FRACTION_0, unitFraction(0.8)),
        Range.atMost(unitFraction(0.8)));
    asserter.accept(
        closedRange(unitFraction(0.3), unitFraction(0.8)),
        Range.closed(unitFraction(0.3), unitFraction(0.8)));
    asserter.accept(
        closedRange(unitFraction(0.3), UNIT_FRACTION_1),
        Range.atLeast(unitFraction(0.3)));

    asserter.accept(singletonClosedRange(UNIT_FRACTION_0), Range.singleton(unitFraction(0)));
    asserter.accept(singletonClosedRange(unitFraction(1e-9)), Range.singleton(unitFraction(1e-9)));
    asserter.accept(singletonClosedRange(unitFraction(0.3)), Range.singleton(unitFraction(0.3)));
    asserter.accept(singletonClosedRange(unitFraction(1 - 1e-9)), Range.singleton(unitFraction(1 - 1e-9)));
    asserter.accept(singletonClosedRange(UNIT_FRACTION_1), Range.singleton(unitFraction(1)));
  }

  @Test
  public void test_makeClosedRangesForDiscreteValues() {
    assertThat(
        "2 starting points",
        makeClosedRangesForDiscreteValues(
            ImmutableList.of(
                LocalDate.of(2020, 1, 20),
                LocalDate.of(2020, 1, 25)),
            LocalDate.of(2020, 1, 30),
            date -> date.minusDays(1)),
        orderedListMatcher(
            ImmutableList.of(
                Range.closed(LocalDate.of(2020, 1, 20), LocalDate.of(2020, 1, 24)),
                Range.closed(LocalDate.of(2020, 1, 25), LocalDate.of(2020, 1, 30))),
            f -> rangeEqualityMatcher(f)));

    assertThat(
        "1 starting point",
        makeClosedRangesForDiscreteValues(
            ImmutableList.of(
                LocalDate.of(2020, 1, 20)),
            LocalDate.of(2020, 1, 30),
            date -> date.minusDays(1)),
        orderedListMatcher(
            ImmutableList.of(
                Range.closed(LocalDate.of(2020, 1, 20), LocalDate.of(2020, 1, 30))),
            f -> rangeEqualityMatcher(f)));

    assertIllegalArgumentException( () -> makeClosedRangesForDiscreteValues(
        emptyList(),
        LocalDate.of(2020, 1, 30),
        date -> date.minusDays(1)));
  }

}
