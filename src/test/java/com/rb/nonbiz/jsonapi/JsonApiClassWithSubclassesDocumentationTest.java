package com.rb.nonbiz.jsonapi;

import com.rb.biz.jsonapi.JsonTicker;
import com.rb.biz.types.asset.AssetId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassWithSubclassesDocumentation.JsonApiClassWithSubclassesDocumentationBuilder.jsonApiClassWithSubclassesDocumentationBuilder;
import static com.rb.nonbiz.jsonapi.JsonApiSubclassInfoTest.jsonApiSubclassInfoMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonElementMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.HumanReadableDocumentationTest.humanReadableDocumentationMatcher;

public class JsonApiClassWithSubclassesDocumentationTest extends RBTestMatcher<JsonApiClassWithSubclassesDocumentation> {

  public static JsonApiClassWithSubclassesDocumentation testJsonApiClassWithSubclassesDocumentationWithSeed(
      Class<?> clazz, String seed) {
    return jsonApiClassWithSubclassesDocumentationBuilder()
        .setClassBeingDocumented(clazz)
        .setSingleLineSummary(documentation("summary" + seed))
        .setLongDocumentation(documentation("documentation" + seed))
        .setJsonApiInfoOnMultipleSubclasses(
            new JsonApiSubclassInfoTest().makeTrivialObject(),
            new JsonApiSubclassInfoTest().makeNontrivialObject())
        .setDiscriminatorProperty("discr" + seed)
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
        testJsonApiClassWithSubclassesDocumentationWithSeed(TestEnumXYZ.class, DUMMY_STRING));
    JsonApiClassWithSubclassesDocumentation doesNotThrow =
        testJsonApiClassWithSubclassesDocumentationWithSeed(ClosedRange.class, DUMMY_STRING);
  }

  @Override
  public JsonApiClassWithSubclassesDocumentation makeTrivialObject() {
    return jsonApiClassWithSubclassesDocumentationBuilder()
        .setClassBeingDocumented(JsonTicker.class)
        .setSingleLineSummary(documentation("x"))
        .setLongDocumentation(documentation("y"))
        .setJsonApiInfoOnOnlySubclass(new JsonApiSubclassInfoTest().makeTrivialObject())
        .setDiscriminatorProperty("t")
        .noTrivialSampleJsonSupplied()
        .noNontrivialSampleJsonSupplied()
        .build();
  }

  @Override
  public JsonApiClassWithSubclassesDocumentation makeNontrivialObject() {
    return testJsonApiClassWithSubclassesDocumentationWithSeed(AssetId.class, "");
  }

  @Override
  public JsonApiClassWithSubclassesDocumentation makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return testJsonApiClassWithSubclassesDocumentationWithSeed(AssetId.class, "");
  }

  @Override
  protected boolean willMatch(
      JsonApiClassWithSubclassesDocumentation expected,
      JsonApiClassWithSubclassesDocumentation actual) {
    return jsonApiClassWithSubclassesDocumentationMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonApiClassWithSubclassesDocumentation> jsonApiClassWithSubclassesDocumentationMatcher(
      JsonApiClassWithSubclassesDocumentation expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getClassBeingDocumented()),
        match(           v -> v.getSingleLineSummary(),          f -> humanReadableDocumentationMatcher(f)),
        match(           v -> v.getLongDocumentation(),          f -> humanReadableDocumentationMatcher(f)),
        matchList(       v -> v.getJsonApiSubclassInfoList(),    f -> jsonApiSubclassInfoMatcher(f)),
        matchUsingEquals(v -> v.getDiscriminatorProperty()),
        matchOptional(   v -> v.getTrivialSampleJson(),          f -> jsonElementMatcher(f, 1e-8)),
        matchOptional(   v -> v.getNontrivialSampleJson(),       f -> jsonElementMatcher(f, 1e-8)));
  }

}
