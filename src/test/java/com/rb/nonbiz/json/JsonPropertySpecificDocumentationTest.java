package com.rb.nonbiz.json;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.HumanReadableDocumentationTest.humanReadableDocumentationMatcher;

public class JsonPropertySpecificDocumentationTest extends RBTestMatcher<JsonPropertySpecificDocumentation> {

  @Override
  public JsonPropertySpecificDocumentation makeTrivialObject() {
    return jsonPropertySpecificDocumentation(documentation("x"));
  }

  @Override
  public JsonPropertySpecificDocumentation makeNontrivialObject() {
    return jsonPropertySpecificDocumentation(documentation("a b c"));
  }

  @Override
  public JsonPropertySpecificDocumentation makeMatchingNontrivialObject() {
    return jsonPropertySpecificDocumentation(documentation("a b c"));
  }

  @Override
  protected boolean willMatch(JsonPropertySpecificDocumentation expected, JsonPropertySpecificDocumentation actual) {
    return jsonPropertySpecificDocumentationMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentationMatcher(
      JsonPropertySpecificDocumentation expected) {
    return makeMatcher(expected,
        match(v ->v.getRawDocumentation(), f -> humanReadableDocumentationMatcher(f)));
  }

}
