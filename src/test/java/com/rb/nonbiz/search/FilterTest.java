package com.rb.nonbiz.search;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.search.Filter.filter;
import static com.rb.nonbiz.search.Filter.noFilter;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;

public class FilterTest extends RBTestMatcher<Filter<String>> {

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
