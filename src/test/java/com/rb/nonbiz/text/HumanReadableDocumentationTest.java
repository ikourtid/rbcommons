package com.rb.nonbiz.text;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static org.junit.Assert.assertEquals;

public class HumanReadableDocumentationTest extends RBTestMatcher<HumanReadableDocumentation> {

  @Test
  public void implementsNonTrivialEqualsAndHashcode() {
    HumanReadableDocumentation objA = documentation("x");
    HumanReadableDocumentation objB = documentation("x");
    assertEquals(objA, objB);
    assertEquals(objA.hashCode(), objB.hashCode());
  }

  @Override
  public HumanReadableDocumentation makeTrivialObject() {
    return documentation("x");
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
