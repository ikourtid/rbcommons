package com.rb.biz.types.asset;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.biz.types.asset.HasListTest.hasListMatcher;
import static com.rb.biz.types.asset.TestHasStringList.emptyTestHasStringList;
import static com.rb.biz.types.asset.TestHasStringList.testHasStringListOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;

public class TestHasStringListTest extends RBTestMatcher<TestHasStringList> {

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

  public static TypeSafeMatcher<TestHasStringList> testHasStringListMatcher(TestHasStringList expected) {
    return makeMatcher(expected,
        match(v -> v.getList(), f -> typeSafeEqualTo(f)));
  }

}
