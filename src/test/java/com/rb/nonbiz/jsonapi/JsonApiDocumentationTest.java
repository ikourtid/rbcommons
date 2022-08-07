package com.rb.nonbiz.jsonapi;

import com.rb.biz.jsonapi.JsonTicker;
import com.rb.nonbiz.collections.RBMapWithDefault;
import com.rb.nonbiz.json.JsonValidationInstructionsTest;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.json.JsonValidationInstructionsTest.jsonValidationInstructionsMatcher;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.jsonapi.HasJsonApiDocumentationTest.hasJsonApiDocumentationMatcher;
import static com.rb.nonbiz.jsonapi.JsonApiDocumentation.JsonApiDocumentationBuilder.jsonApiDocumentationBuilder;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonElementMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.text.HumanReadableLabelTest.humanReadableLabelMatcher;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

public class JsonApiDocumentationTest extends RBTestMatcher<JsonApiDocumentation> {

  public static JsonApiDocumentation testJsonApiDocumentationWithSeed(Class<?> clazz, String seed) {
    return jsonApiDocumentationBuilder()
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

  @Override
  public JsonApiDocumentation makeTrivialObject() {
    return jsonApiDocumentationBuilder()
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
  public JsonApiDocumentation makeNontrivialObject() {
    return testJsonApiDocumentationWithSeed(RBMapWithDefault.class, "");
  }

  @Override
  public JsonApiDocumentation makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return testJsonApiDocumentationWithSeed(RBMapWithDefault.class, "");
  }

  @Override
  protected boolean willMatch(JsonApiDocumentation expected, JsonApiDocumentation actual) {
    return jsonApiDocumentationMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonApiDocumentation> jsonApiDocumentationMatcher(JsonApiDocumentation expected) {
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
