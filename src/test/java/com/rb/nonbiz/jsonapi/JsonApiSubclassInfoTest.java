package com.rb.nonbiz.jsonapi;

import com.rb.biz.types.asset.AssetId;
import com.rb.biz.types.asset.CashId;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.json.JsonPropertySpecificDocumentation;
import com.rb.nonbiz.json.JsonPropertySpecificDocumentationTest;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.junit.Test;

import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentationTest.jsonPropertySpecificDocumentationMatcher;
import static com.rb.nonbiz.jsonapi.HasJsonApiDocumentationTest.hasJsonApiDocumentationMatcher;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentationTest.testJsonApiClassDocumentationWithSeed;
import static com.rb.nonbiz.jsonapi.JsonApiSubclassInfo.jsonApiSubclassInfo;
import static com.rb.nonbiz.testmatchers.Match.matchClass;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class JsonApiSubclassInfoTest extends RBTestMatcher<JsonApiSubclassInfo> {

  @Override
  public JsonApiSubclassInfo makeTrivialObject() {
    return jsonApiSubclassInfo(
        CashId.class,
        "cash_id",
        () -> testJsonApiClassDocumentationWithSeed(CashId.class, "a"));
  }

  @Override
  public JsonApiSubclassInfo makeNontrivialObject() {
    return jsonApiSubclassInfo(
        InstrumentId.class,
        "instrument_id",
        () -> testJsonApiClassDocumentationWithSeed(InstrumentId.class, "i"),
        jsonPropertySpecificDocumentation("xyz"));
  }

  @Override
  public JsonApiSubclassInfo makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return jsonApiSubclassInfo(
        InstrumentId.class,
        "instrument_id",
        () -> testJsonApiClassDocumentationWithSeed(InstrumentId.class, "i"),
        jsonPropertySpecificDocumentation("xyz"));
  }

  @Override
  protected boolean willMatch(JsonApiSubclassInfo expected, JsonApiSubclassInfo actual) {
    return jsonApiSubclassInfoMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonApiSubclassInfo> jsonApiSubclassInfoMatcher(JsonApiSubclassInfo expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getClassOfSubclass()),
        matchUsingEquals(v -> v.getDiscriminatorPropertyValue()),
        match(           v -> v.getJsonApiConverterForTraversing(),     f -> hasJsonApiDocumentationMatcher(f)),
        matchOptional(   v -> v.getJsonPropertySpecificDocumentation(), f -> jsonPropertySpecificDocumentationMatcher(f)));
  }

}
