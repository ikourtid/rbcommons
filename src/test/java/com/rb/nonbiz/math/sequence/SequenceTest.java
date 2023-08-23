package com.rb.nonbiz.math.sequence;

import com.google.common.collect.Iterators;
import com.rb.biz.types.Money;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.math.sequence.ArithmeticProgression.ArithmeticProgressionBuilder.arithmeticProgressionBuilder;
import static com.rb.nonbiz.math.sequence.ConstantSequence.constantSequence;
import static com.rb.nonbiz.math.sequence.Sequences.transformedSequence;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

// This test class is not generic, but the publicly exposed static matcher is.
public class SequenceTest extends RBTestMatcher<Sequence<Money>> {

  @Override
  public Sequence<Money> makeTrivialObject() {
    return constantSequence(ZERO_MONEY);
  }

  @Override
  public Sequence<Money> makeNontrivialObject() {
    return transformedSequence(
        arithmeticProgressionBuilder()
            .setCommonDifference(100)
            .setInitialValue(500)
            .build(),
        v -> money(v));
  }

  @Override
  public Sequence<Money> makeMatchingNontrivialObject() {
    double e = 1e-12; // epsilon; using a smaller one than the usual 1e-9, because the differences compound further down in the Sequence
    return transformedSequence(
        arithmeticProgressionBuilder()
            .setCommonDifference(100 + e)
            .setInitialValue(500 + e)
            .build(),
        v -> money(v));
  }

  @Override
  protected boolean willMatch(Sequence<Money> expected, Sequence<Money> actual) {
    return sequenceIncompleteMatcher(expected, f -> preciseValueMatcher(f, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <T>TypeSafeMatcher<Sequence<T>> sequenceEqualityIncompleteMatcher(Sequence<T> expected) {
    return sequenceIncompleteMatcher(expected, f -> typeSafeEqualTo(f));
  }

  /**
   * A {@link Sequence} is unusual in that we cannot match two sequences easily, because they are functions where the
   * domain is infinite. Usually what we do in the code is come up with some parametric representation of a function,
   * which allows us to compare different functions. However, a Sequence may have an arbitrary set of items, or
   * it can be an arithmetic progression (but only in the case of doubles), etc. - so it's not just some numeric
   * function that we can somehow represent.
   *
   * Therefore, the matching for Sequence objects will only compare 10 values, as specified. It's not a perfect
   * way to do matching, because it's better than not matching at all. Hence the name 'incomplete'. 10 items is large
   * enough that if there's ever a problem, we will likely catch it.
   *
   * Of course, if you are dealing with a specific implementation of Sequence (as of April 2020, that's only DoubleSequence)
   * then just use doubleSequenceMatcher. This matcher here is for the most general case.
   */
  public static <T> TypeSafeMatcher<Sequence<T>> sequenceIncompleteMatcher(
      Sequence<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        matchList(v -> newArrayList(Iterators.limit(v.iterator(), 10)), matcherGenerator));
  }

}
