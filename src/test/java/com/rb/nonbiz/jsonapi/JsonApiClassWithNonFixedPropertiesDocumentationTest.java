package com.rb.nonbiz.jsonapi;

import com.rb.biz.jsonapi.JsonTicker;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.Partition;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.HasJsonApiDocumentationTest.hasJsonApiDocumentationMatcher;
import static com.rb.nonbiz.jsonapi.JsonApiClassWithNonFixedPropertiesDocumentation.JsonApiClassWithNonFixedPropertiesDocumentationBuilder.jsonApiClassWithNonFixedPropertiesDocumentationBuilder;
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

public class JsonApiClassWithNonFixedPropertiesDocumentationTest
    extends RBTestMatcher<JsonApiClassWithNonFixedPropertiesDocumentation> {

  public static JsonApiClassWithNonFixedPropertiesDocumentation testJsonApiClassWithNonFixedPropertiesDocumentationWithSeed(
      Class<?> classBeingDocumented, Class<?> keyClass, Class<?> valueClass, String seed) {
    return jsonApiClassWithNonFixedPropertiesDocumentationBuilder()
        .setClassBeingDocumented(classBeingDocumented)
        .setKeyClass(keyClass)
        .setValueClass(valueClass)
        .setSingleLineSummary(documentation("summary" + seed))
        .setLongDocumentation(documentation("documentation" + seed))
        .hasNoChildJsonApiConverters() // hard to set this here
        .setNontrivialSampleJson(jsonObject(
            "key1" + seed, jsonString("value1" + seed),
            "key2" + seed, jsonString("value2" + seed)))
        .build();
  }

  @Test
  public void classIsEnum_mustUseJsonApiEnumDocumentationInstead_throws() {
    // JsonTicker and UnitFraction are dummy classes.
    Class<JsonTicker> dummyKeyClass = JsonTicker.class;
    Class<UnitFraction> dummyValueClass = UnitFraction.class;

    assertIllegalArgumentException( () ->
        testJsonApiClassWithNonFixedPropertiesDocumentationWithSeed(
            TestEnumXYZ.class, dummyKeyClass, dummyValueClass, DUMMY_STRING));

    Class<?> dummyNonEnumClass = ClosedRange.class;
    JsonApiClassWithNonFixedPropertiesDocumentation doesNotThrow =
        testJsonApiClassWithNonFixedPropertiesDocumentationWithSeed(
            dummyNonEnumClass, dummyKeyClass, dummyValueClass, DUMMY_STRING);
  }

  @Override
  public JsonApiClassWithNonFixedPropertiesDocumentation makeTrivialObject() {
    return jsonApiClassWithNonFixedPropertiesDocumentationBuilder()
        .setClassBeingDocumented(Partition.class)
        .setKeyClass(JsonTicker.class)
        .setValueClass(Double.class)
        .setSingleLineSummary(documentation("x"))
        .setLongDocumentation(documentation("y"))
        .hasNoChildJsonApiConverters()
        .noNontrivialSampleJsonSupplied()
        .build();
  }

  @Override
  public JsonApiClassWithNonFixedPropertiesDocumentation makeNontrivialObject() {
    return testJsonApiClassWithNonFixedPropertiesDocumentationWithSeed(
        Partition.class, JsonTicker.class, UnitFraction.class, "");
  }

  @Override
  public JsonApiClassWithNonFixedPropertiesDocumentation makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return testJsonApiClassWithNonFixedPropertiesDocumentationWithSeed(
        Partition.class, JsonTicker.class, UnitFraction.class, "");
  }

  @Override
  protected boolean willMatch(JsonApiClassWithNonFixedPropertiesDocumentation expected,
                              JsonApiClassWithNonFixedPropertiesDocumentation actual) {
    return jsonApiClassWithNonFixedPropertiesDocumentationMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonApiClassWithNonFixedPropertiesDocumentation>
  jsonApiClassWithNonFixedPropertiesDocumentationMatcher(
      JsonApiClassWithNonFixedPropertiesDocumentation expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getClassBeingDocumented()),
        matchUsingEquals(v -> v.getKeyClass()),
        matchUsingEquals(v -> v.getValueClass()),
        match(           v -> v.getSingleLineSummary(),          f -> humanReadableDocumentationMatcher(f)),
        match(           v -> v.getLongDocumentation(),          f -> humanReadableDocumentationMatcher(f)),
        matchList(       v -> v.getChildJsonApiConverters(),     f -> hasJsonApiDocumentationMatcher(f)),
        matchOptional(   v -> v.getNontrivialSampleJson(),       f -> jsonElementMatcher(f, 1e-8)));
  }

}
