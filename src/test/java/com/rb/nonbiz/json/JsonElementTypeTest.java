package com.rb.nonbiz.json;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.json.JsonElementType.JSON_NULL;
import static com.rb.nonbiz.json.JsonElementType.JSON_NUMBER;
import static com.rb.nonbiz.testmatchers.Match.matchEnum;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class JsonElementTypeTest extends RBTestMatcher<JsonElementType> {

  @Override
  public JsonElementType makeTrivialObject() {
    return JSON_NULL;
  }

  @Override
  public JsonElementType makeNontrivialObject() {
    return JSON_NUMBER;
  }

  @Override
  public JsonElementType makeMatchingNontrivialObject() {
    return JSON_NUMBER;
  }

  @Override
  protected boolean willMatch(JsonElementType expected, JsonElementType actual) {
    return jsonElementTypeMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonElementType> jsonElementTypeMatcher(JsonElementType expected) {
    // This is trivial, but it's good to make it general in case this enum gets changed to a class.
    return makeMatcher(expected,
        matchEnum(v -> v));
  }

}
