package com.rb.biz.types.asset;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class HasListTest {

  public static <T, L extends HasList<T>> TypeSafeMatcher<L> hasListMatcher(
      L expected, MatcherGenerator<T> itemMatcherGenerator) {
    return makeMatcher(expected,
        matchList(v -> expected.getList(), itemMatcherGenerator));
  }

}
