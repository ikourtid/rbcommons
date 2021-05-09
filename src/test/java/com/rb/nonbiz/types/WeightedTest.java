package com.rb.nonbiz.types;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_STRING;
import static com.rb.nonbiz.types.Weighted.negativeWeighted;
import static com.rb.nonbiz.types.Weighted.positiveWeighted;
import static com.rb.nonbiz.types.Weighted.weighted;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class WeightedTest extends RBTestMatcher<Weighted<String>> {

  @Test
  public void selectivelyAllowsNegativePositiveOrZeroWeights() {
    Weighted<String> doesNotThrow;
    doesNotThrow = weighted(DUMMY_STRING, -1.1);
    doesNotThrow = weighted(DUMMY_STRING, 0);
    doesNotThrow = weighted(DUMMY_STRING, 1.1);

    assertIllegalArgumentException( () -> positiveWeighted(DUMMY_STRING, -1.1));
    assertIllegalArgumentException( () -> positiveWeighted(DUMMY_STRING, 0));
    doesNotThrow = positiveWeighted(DUMMY_STRING, 1.1);
    
    assertIllegalArgumentException( () -> negativeWeighted(DUMMY_STRING, 1.1));
    assertIllegalArgumentException( () -> negativeWeighted(DUMMY_STRING, 0));
    doesNotThrow = negativeWeighted(DUMMY_STRING, -1.1);
  }

  @Override
  public Weighted<String> makeTrivialObject() {
    return weighted("", 0);
  }

  @Override
  public Weighted<String> makeNontrivialObject() {
    return weighted("abc", -12.34);
  }

  @Override
  public Weighted<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return weighted("abc", -12.34 + e);
  }

  @Override
  protected boolean willMatch(Weighted<String> expected, Weighted<String> actual) {
    return weightedMatcher(expected, v -> typeSafeEqualTo(v)).matches(actual);
  }

  public static <T> TypeSafeMatcher<Weighted<T>> weightedMatcher(Weighted<T> expected,
                                                                 MatcherGenerator<T> itemMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getItem(), itemMatcherGenerator),
        matchUsingDoubleAlmostEquals(v -> v.getWeight(), 1e-8));
  }

}
