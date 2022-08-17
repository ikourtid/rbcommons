package com.rb.nonbiz.text;

import com.rb.nonbiz.testutils.RBTestMatcher;

import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.text.HumanReadableDocumentation.emptyDocumentation;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class HumanReadableDocumentationTest extends RBTestMatcher<HumanReadableDocumentation> {

  @Override
  public HumanReadableDocumentation makeTrivialObject() {
    return emptyDocumentation();
  }

  @Override
  public HumanReadableDocumentation makeNontrivialObject() {
    return documentation("some\ndocumentation");
  }

  @Override
  public HumanReadableDocumentation makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return documentation("some\ndocumentation");
  }

  @Override
  protected boolean willMatch(HumanReadableDocumentation expected, HumanReadableDocumentation actual) {
    return humanReadableDocumentationMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<HumanReadableDocumentation> humanReadableDocumentationMatcher(
      HumanReadableDocumentation expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getAsString()));
  }

}
