package com.rb.nonbiz.json;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.json.RBJsonStringArray.RBJsonStringArrayBuilder.rbJsonStringArrayBuilder;
import static com.rb.nonbiz.json.RBJsonStringArray.emptyRBJsonStringArray;
import static com.rb.nonbiz.testmatchers.Match.matchListUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class RBJsonStringArrayTest extends RBTestMatcher<RBJsonStringArray> {

  @Override
  public RBJsonStringArray makeTrivialObject() {
    return emptyRBJsonStringArray();
  }

  @Override
  public RBJsonStringArray makeNontrivialObject() {
    return rbJsonStringArrayBuilder().add("a").add("b").add("c").build();
  }

  @Override
  public RBJsonStringArray makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return rbJsonStringArrayBuilder().add("a").add("b").add("c").build();
  }

  @Override
  protected boolean willMatch(RBJsonStringArray expected, RBJsonStringArray actual) {
    return rbJsonStringArrayMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<RBJsonStringArray> rbJsonStringArrayMatcher(RBJsonStringArray expected) {
    return makeMatcher(expected,
        matchListUsingEquals(v -> v.getRawStringsList()));
  }

}
