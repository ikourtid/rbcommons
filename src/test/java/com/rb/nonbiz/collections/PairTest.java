package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static junit.framework.TestCase.assertEquals;

public class PairTest {

  @Test
  public void implementsEquals() {
    assertEquals(pair(1, "a"), pair(1, "a"));
  }

  public static <L, R> TypeSafeMatcher<Pair<L, R>> pairEqualityMatcher(Pair<L, R> expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getLeft()),
        matchUsingEquals(v -> v.getRight()));
  }

  public static <L, R> TypeSafeMatcher<Pair<L, R>> pairMatcher(
      Pair<L, R> expected, MatcherGenerator<L> leftMatcherGenerator, MatcherGenerator<R> rightMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getLeft(), leftMatcherGenerator),
        match(v -> v.getRight(), rightMatcherGenerator));
  }

}
