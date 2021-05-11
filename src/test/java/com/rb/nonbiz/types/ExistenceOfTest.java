package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.types.ExistenceOf.existenceOf;

// This test class is not generic, but the publicly exposed static matcher is.
public class ExistenceOfTest extends RBTestMatcher<ExistenceOf<String>> {

  @Override
  public ExistenceOf<String> makeTrivialObject() {
    return existenceOf(false);
  }

  @Override
  public ExistenceOf<String> makeNontrivialObject() {
    // This is no more or less trivial than makeTrivialObject, but we had to pick some way to do this arbitrarily.
    return existenceOf(true);
  }

  @Override
  public ExistenceOf<String> makeMatchingNontrivialObject() {
    return existenceOf(true);
  }

  @Override
  protected boolean willMatch(ExistenceOf<String> expected, ExistenceOf<String> actual) {
    return existenceOfMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<ExistenceOf<T>> existenceOfMatcher(ExistenceOf<T> expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.asBoolean()));
  }

}
