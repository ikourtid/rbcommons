package com.rb.nonbiz.math.sequence;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Iterator;

import static com.rb.nonbiz.math.sequence.ConstantSequence.constantSequence;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static org.junit.Assert.assertEquals;

// This test class is not generic, but the publicly exposed static matcher is.
public class ConstantSequenceTest extends RBTestMatcher<ConstantSequence<String>> {

  @Test
  public void testBasicCalls() {
    Iterator<String> stringSequence = constantSequence("x").iterator();
    assertEquals("x", stringSequence.next());
    assertEquals("x", stringSequence.next());
    assertEquals("x", stringSequence.next());
  }

  @Override
  public ConstantSequence<String> makeTrivialObject() {
    return constantSequence("");
  }

  @Override
  public ConstantSequence<String> makeNontrivialObject() {
    return constantSequence("abc");
  }

  @Override
  public ConstantSequence<String> makeMatchingNontrivialObject() {
    return constantSequence("abc");
  }

  @Override
  protected boolean willMatch(ConstantSequence<String> expected, ConstantSequence<String> actual) {
    return constantSequenceEqualityMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<ConstantSequence<T>> constantSequenceEqualityMatcher(ConstantSequence<T> expected) {
    return constantSequenceMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <T> TypeSafeMatcher<ConstantSequence<T>> constantSequenceMatcher(
      ConstantSequence<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getConstantValue(), matcherGenerator));
  }

}
