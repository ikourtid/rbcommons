package com.rb.biz.types.asset;

import com.rb.biz.types.asset.HasListTest.TestHasStringList;
import com.rb.nonbiz.collections.RBLists;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

import static com.rb.biz.types.asset.HasListTest.TestHasStringList.emptyTestHasStringList;
import static com.rb.biz.types.asset.HasListTest.TestHasStringList.testHasStringList;
import static com.rb.biz.types.asset.HasListTest.TestHasStringList.testHasStringListMatcher;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class HasListTest extends RBTestMatcher<TestHasStringList> {


  public static class TestHasStringList implements HasList<String> {

    private final List<String> listOfStrings;

    private TestHasStringList(List<String> listOfStrings) {
      this.listOfStrings = listOfStrings;
    }

    public static TestHasStringList emptyTestHasStringList() {
      return new TestHasStringList(emptyList());
    }

    public static TestHasStringList singletonTestHasStringList(String onlyItem) {
      return new TestHasStringList(singletonList(onlyItem));
    }

    public static TestHasStringList testHasStringList(String first, String second, String ... rest) {
      return new TestHasStringList(concatenateFirstSecondAndRest(first, second, rest));
    }

    @Override
    public List<String> getList() {
      return listOfStrings;
    }

    public static TypeSafeMatcher<TestHasStringList> testHasStringListMatcher(TestHasStringList expected) {
      return hasListMatcher(expected, f -> typeSafeEqualTo(f));
    }

  }


  @Override
  public TestHasStringList makeTrivialObject() {
    return emptyTestHasStringList();
  }

  @Override
  public TestHasStringList makeNontrivialObject() {
    return testHasStringList("a", "b");
  }

  @Override
  public TestHasStringList makeMatchingNontrivialObject() {
    return testHasStringList("a", "b");
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
