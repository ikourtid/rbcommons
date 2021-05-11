package com.rb.biz.types.asset;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.biz.types.asset.TestHasStringList.emptyTestHasStringList;
import static com.rb.biz.types.asset.TestHasStringList.testHasStringListOf;
import static com.rb.biz.types.asset.TestHasStringList.testHasStringListMatcher;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class HasListTest extends RBTestMatcher<TestHasStringList> {


  @Override
  public TestHasStringList makeTrivialObject() {
    return emptyTestHasStringList();
  }

  @Override
  public TestHasStringList makeNontrivialObject() {
    return testHasStringListOf("a", "b");
  }

  @Override
  public TestHasStringList makeMatchingNontrivialObject() {
    return testHasStringListOf("a", "b");
  }

  @Override
  protected boolean willMatch(TestHasStringList expected, TestHasStringList actual) {
    return testHasStringListMatcher(expected).matches(actual);
  }

  public static <T, L extends HasList<T>> TypeSafeMatcher<L> hasListMatcher(
      L expected, MatcherGenerator<T> itemMatcherGenerator) {
    return makeMatcher(expected,
        matchList(v -> expected.getList(), itemMatcherGenerator));
  }

}
