package com.rb.nonbiz.json;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.JsonObjectPath.jsonObjectPath;
import static com.rb.nonbiz.json.JsonObjectPath.singletonJsonObjectPath;
import static com.rb.nonbiz.testmatchers.Match.matchListUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class JsonObjectPathTest extends RBTestMatcher<JsonObjectPath> {

  @Test
  public void isEmpty_throws() {
    assertIllegalArgumentException( () -> jsonObjectPath(emptyList()));
    JsonObjectPath doesNotThrow;
    doesNotThrow = jsonObjectPath(singletonList("x"));
    doesNotThrow = jsonObjectPath(ImmutableList.of("x", "y"));
  }

  @Test
  public void hasEmptyProperties_throws() {
    rbSetOf(
        "",
        " ",
        "\t",
        "\n",
        "  ").forEach(badJsonProperty -> {
      assertIllegalArgumentException( () -> singletonJsonObjectPath(badJsonProperty));
      assertIllegalArgumentException( () -> jsonObjectPath("x", badJsonProperty));
      assertIllegalArgumentException( () -> jsonObjectPath(badJsonProperty, "y"));
      assertIllegalArgumentException( () -> jsonObjectPath("x", badJsonProperty, "y"));
    });
  }

  @Override
  public JsonObjectPath makeTrivialObject() {
    return singletonJsonObjectPath("x");
  }

  @Override
  public JsonObjectPath makeNontrivialObject() {
    return jsonObjectPath("path", "to", "object");
  }

  @Override
  public JsonObjectPath makeMatchingNontrivialObject() {
    return jsonObjectPath("path", "to", "object");
  }

  @Override
  protected boolean willMatch(JsonObjectPath expected, JsonObjectPath actual) {
    return jsonObjectPathMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonObjectPath> jsonObjectPathMatcher(JsonObjectPath expected) {
    return makeMatcher(expected,
        matchListUsingEquals(v -> v.getJsonProperties()));
  }

}
