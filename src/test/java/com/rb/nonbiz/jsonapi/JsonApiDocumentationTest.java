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

  @Override
  public JsonApiDocumentation makeTrivialObject() {
    return jsonApiDocumentationBuilder()
        .setClass(JsonTicker.class)
        .setSingleLineSummary(label("x"))
        .setDocumentationHtml("y")
        .hasNoJsonValidationInstructions()
        .hasNoChildNodes()
        .noTrivialSampleJsonSupplied()
        .noNontrivialSampleJsonSupplied()
        .build();
  }

  @Override
  public JsonApiDocumentation makeNontrivialObject() {
    return jsonApiDocumentationBuilder()
        .setClass(RBMapWithDefault.class)
        .setSingleLineSummary(label("summary"))
        .setDocumentationHtml("documentation")
        .setJsonValidationInstructions(new JsonValidationInstructionsTest().makeNontrivialObject())
        .hasNoChildNodes() // hard to set this here
        .setTrivialSampleJson(singletonJsonObject(
            "key", "value"))
        .setNontrivialSampleJson(jsonObject(
            "key1", jsonString("value1"),
            "key2", jsonString("value2")))
        .build();
  }

  @Override
  public JsonApiDocumentation makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return jsonApiDocumentationBuilder()
        .setClass(RBMapWithDefault.class)
        .setSingleLineSummary(label("summary"))
        .setDocumentationHtml("documentation")
        .setJsonValidationInstructions(new JsonValidationInstructionsTest().makeNontrivialObject())
        .hasNoChildNodes() // hard to set this here
        .setTrivialSampleJson(singletonJsonObject(
            "key", "value"))
        .setNontrivialSampleJson(jsonObject(
            "key1", jsonString("value1"),
            "key2", jsonString("value2")))
        .build();
  }

  @Override
  protected boolean willMatch(JsonApiDocumentation expected, JsonApiDocumentation actual) {
    return jsonApiDocumentationMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonApiDocumentation> jsonApiDocumentationMatcher(JsonApiDocumentation expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getClazz()),
        match(           v -> v.getSingleLineSummary(),          f -> humanReadableLabelMatcher(f)),
        matchUsingEquals(v -> v.getDocumentationHtml()),
        match(           v -> v.getJsonValidationInstructions(), f -> jsonValidationInstructionsMatcher(f)),
        matchList(       v -> v.getChildNodes(),                 f -> hasJsonApiDocumentationMatcher(f)),
        matchOptional(   v -> v.getTrivialSampleJson(),          f -> jsonElementMatcher(f, 1e-8)),
        matchOptional(   v -> v.getNontrivialSampleJson(),       f -> jsonElementMatcher(f, 1e-8)));
  }

}
