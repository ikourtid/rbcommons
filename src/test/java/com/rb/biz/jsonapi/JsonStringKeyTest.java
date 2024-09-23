package com.rb.biz.jsonapi;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class JsonStringKeyTest {

  public static <T extends JsonStringKey> TypeSafeMatcher<T> jsonStringKeyMatcher(T expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getFreeFormString()));
  }

}
