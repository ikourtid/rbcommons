package com.rb.nonbiz.math.sequence;

import com.google.common.collect.Iterators;
import com.rb.biz.types.Money;
import com.rb.nonbiz.collections.Pair;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.PairTest.pairMatcher;
import static com.rb.nonbiz.math.sequence.ConstantSequence.constantSequence;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

// This test class is not generic, but the publicly exposed static matcher is.
public class SequenceTest extends RBTestMatcher<Sequence<Pair<String, Money>>> {

  @Override
  public Sequence<Pair<String, Money>> makeTrivialObject() {
    return constantSequence(pair("", ZERO_MONEY));
  }

  @Override
  public Sequence<Pair<String, Money>> makeNontrivialObject() {
    return new GeometricProgressionTest().makeNontrivialObject();
  }

  @Override
  public Sequence<Pair<String, Money>> makeMatchingNontrivialObject() {
    return new GeometricProgressionTest().makeMatchingNontrivialObject();
  }

  @Override
  protected boolean willMatch(Sequence<Pair<String, Money>> expected, Sequence<Pair<String, Money>> actual) {
    return sequenceIncompleteMatcher(
        expected,
        f -> pairMatcher(f, f2 -> typeSafeEqualTo(f2), f3 -> preciseValueMatcher(f3, DEFAULT_EPSILON_1e_8)))
        .matches(actual);
  }

  public static <T> TypeSafeMatcher<Sequence<T>> sequenceEqualityIncompleteMatcher(Sequence<T> expected) {
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
