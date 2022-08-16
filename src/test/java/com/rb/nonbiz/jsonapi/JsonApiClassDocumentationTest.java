package com.rb.nonbiz.jsonapi;

import com.rb.biz.jsonapi.JsonTicker;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.RBMapWithDefault;
import com.rb.nonbiz.json.JsonValidationInstructionsTest;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.json.JsonValidationInstructionsTest.jsonValidationInstructionsMatcher;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.jsonapi.HasJsonApiDocumentationTest.hasJsonApiDocumentationMatcher;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonElementMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.text.HumanReadableLabelTest.humanReadableLabelMatcher;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

public class JsonApiClassDocumentationTest extends RBTestMatcher<JsonApiClassDocumentation> {

  public static JsonApiClassDocumentation testJsonApiClassDocumentationWithSeed(Class<?> clazz, String seed) {
    return jsonApiClassDocumentationBuilder()
        .setClass(clazz)
        .setSingleLineSummary(label("summary" + seed))
        .setLongDocumentation("documentation" + seed)
        .setJsonValidationInstructions(new JsonValidationInstructionsTest().makeNontrivialObject())
        .hasNoChildNodes() // hard to set this here
        .setTrivialSampleJson(singletonJsonObject(
            "key" + seed, "value" + seed))
        .setNontrivialSampleJson(jsonObject(
            "key1" + seed, jsonString("value1" + seed),
            "key2" + seed, jsonString("value2" + seed)))
        .build();
  }

  @Test
  public void classIsEnum_mustUseJsonApiEnumDocumentationInstead_throws() {
    assertIllegalArgumentException( () ->
        testJsonApiClassDocumentationWithSeed(TestEnumXYZ.class, DUMMY_STRING));
    JsonApiClassDocumentation doesNotThrow =
        testJsonApiClassDocumentationWithSeed(ClosedRange.class, DUMMY_STRING);
  }

  @Override
  public JsonApiClassDocumentation makeTrivialObject() {
    return jsonApiClassDocumentationBuilder()
        .setClass(JsonTicker.class)
        .setSingleLineSummary(label("x"))
        .setLongDocumentation("y")
        .hasNoJsonValidationInstructions()
        .hasNoChildNodes()
        .noTrivialSampleJsonSupplied()
        .noNontrivialSampleJsonSupplied()
        .build();
  }

  @Override
  public JsonApiClassDocumentation makeNontrivialObject() {
    return testJsonApiClassDocumentationWithSeed(RBMapWithDefault.class, "");
  }

  @Override
  public JsonApiClassDocumentation makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return testJsonApiClassDocumentationWithSeed(RBMapWithDefault.class, "");
  }

  @Override
  protected boolean willMatch(JsonApiClassDocumentation expected, JsonApiClassDocumentation actual) {
    return jsonApiClassDocumentationMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonApiClassDocumentation> jsonApiClassDocumentationMatcher(
      JsonApiClassDocumentation expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getClazz()),
        match(           v -> v.getSingleLineSummary(),          f -> humanReadableLabelMatcher(f)),
        matchUsingEquals(v -> v.getLongDocumentation()),
        match(           v -> v.getJsonValidationInstructions(), f -> jsonValidationInstructionsMatcher(f)),
        matchList(       v -> v.getChildNodes(),                 f -> hasJsonApiDocumentationMatcher(f)),
        matchOptional(   v -> v.getTrivialSampleJson(),          f -> jsonElementMatcher(f, 1e-8)),
        matchOptional(   v -> v.getNontrivialSampleJson(),       f -> jsonElementMatcher(f, 1e-8)));
  }

}
