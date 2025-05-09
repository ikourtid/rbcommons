package com.rb.nonbiz.search;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Pointer;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.rb.nonbiz.search.Filter.filter;
import static com.rb.nonbiz.search.Filter.noFilter;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Pointer.uninitializedPointer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilterTest extends RBTestMatcher<Filter<String>> {

  @Test
  public void testIfPresentOrElse() {
    Pointer<String> state = uninitializedPointer();

    Filter.<String>noFilter().ifPresentOrElse(
        v -> state.set(v),
        () -> state.set("?"));
    assertEquals("?", state.getOrThrow());

    filter("abc").ifPresentOrElse(
        v -> state.set(v),
        () -> state.set("?"));
    assertEquals("abc", state.getOrThrow());
  }

  @Test
  public void testIfPresent() {
    Pointer<String> state = uninitializedPointer();

    Filter.<String>noFilter().ifPresent(
        v -> state.set(v));
    assertFalse(state.isInitialized());

    filter("abc").ifPresent(
        v -> state.set(v));
    assertEquals("abc", state.getOrThrow());
  }

  @Override
  public Filter<String> makeTrivialObject() {
    return noFilter();
  }

  @Override
  public Filter<String> makeNontrivialObject() {
    return filter("abc");
  }

  @Override
  public Filter<String> makeMatchingNontrivialObject() {
    return filter("abc");
  }

  @Override
  protected boolean willMatch(Filter<String> expected, Filter<String> actual) {
    return filterEqualsMatcher(expected).matches(actual);
  }

  // A filter in production can only be used if T implements non-trivial hashCode/equals
  // (i.e. not a simple pointer comparison). Therefore, there's no point in passing a MatcherGenerator here.
  public static <T> TypeSafeMatcher<Filter<T>> filterEqualsMatcher(Filter<T> expected) {
    return makeMatcher(expected,
        matchOptional(v -> v.getRawFilter(), f -> typeSafeEqualTo(f)));
  }

}
