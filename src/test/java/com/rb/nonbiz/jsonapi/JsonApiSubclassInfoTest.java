package com.rb.nonbiz.jsonapi;

import com.rb.biz.types.asset.CashId;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentationTest.jsonPropertySpecificDocumentationMatcher;
import static com.rb.nonbiz.jsonapi.HasJsonApiDocumentationTest.hasJsonApiDocumentationMatcher;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentationTest.testJsonApiClassDocumentationWithSeed;
import static com.rb.nonbiz.jsonapi.JsonApiSubclassInfo.JsonApiSubclassInfoBuilder.jsonApiSubclassInfoBuilder;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchOptionalUsingEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class JsonApiSubclassInfoTest extends RBTestMatcher<JsonApiSubclassInfo> {

  @Override
  public JsonApiSubclassInfo makeTrivialObject() {
    return jsonApiSubclassInfoBuilder()
        .setClassOfSubclass(CashId.class)
        .setDiscriminatorPropertyValue("cash_id")
        .hasNoSeparateJsonApiConverterForTraversing()
        .build();
  }

  @Override
  public JsonApiSubclassInfo makeNontrivialObject() {
    return jsonApiSubclassInfoBuilder()
        .setClassOfSubclass(InstrumentId.class)
        .setDiscriminatorPropertyValue("instrument_id")
        .setJsonApiConverterForTraversing( () -> testJsonApiClassDocumentationWithSeed(InstrumentId.class, "i"))
        .build();
  }

  @Override
  public JsonApiSubclassInfo makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return jsonApiSubclassInfoBuilder()
        .setClassOfSubclass(InstrumentId.class)
        .setDiscriminatorPropertyValue("instrument_id")
        .setJsonApiConverterForTraversing( () -> testJsonApiClassDocumentationWithSeed(InstrumentId.class, "i"))
        .build();
  }

  @Override
  protected boolean willMatch(JsonApiSubclassInfo expected, JsonApiSubclassInfo actual) {
    return jsonApiSubclassInfoMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonApiSubclassInfo> jsonApiSubclassInfoMatcher(JsonApiSubclassInfo expected) {
    return makeMatcher(expected,
        matchUsingEquals(        v -> v.getClassOfSubclass()),
        matchUsingEquals(        v -> v.getDiscriminatorPropertyValue()),
        matchOptional(           v -> v.getJsonApiConverterForTraversing(),     f -> hasJsonApiDocumentationMatcher(f)));
  }

}
