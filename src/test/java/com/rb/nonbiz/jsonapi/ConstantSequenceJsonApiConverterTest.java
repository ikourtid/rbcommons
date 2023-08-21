package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.math.sequence.ConstantSequence;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.HasJsonApiDocumentationTestHelper.hasJsonApiDocumentationTestHelper;
import static com.rb.nonbiz.jsonapi.JsonApiPair.jsonApiPair;
import static com.rb.nonbiz.jsonapi.JsonApiTestData.jsonApiTestData;
import static com.rb.nonbiz.math.sequence.ConstantSequence.constantSequence;
import static com.rb.nonbiz.math.sequence.ConstantSequenceTest.constantSequenceEqualityMatcher;
import static com.rb.nonbiz.math.sequence.ConstantSequenceTest.constantSequenceMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

public class ConstantSequenceJsonApiConverterTest
    extends RBCommonsIntegrationTest<ConstantSequenceJsonApiConverter> {

  // Check that the "sample" JSON element in the JsonApiDocumentation can be transformed
  // to a valid Java object via #fromJsonObject.
  // We don't want to display any JSON that can't be converted.
  @Test
  public void testValidSampleJson() {
    ConstantSequenceJsonApiConverter constantSequenceJsonApiConverter = makeRealObject();
    hasJsonApiDocumentationTestHelper(
        constantSequenceJsonApiConverter,
        jsonObject -> constantSequenceJsonApiConverter.fromJsonObject(
            jsonObject, v -> v.getAsDouble()))
        .testValidSampleJson();
  }

  @Test
  public void testUsingDoubles_commonCase() {
    JsonApiTestData<ConstantSequence<Double>> jsonApiTestData = jsonApiTestData(
        f -> constantSequenceMatcher(f, f2 -> doubleAlmostEqualsMatcher(f2, DEFAULT_EPSILON_1e_8)),

        jsonApiPair(
            constantSequence(-12.34),
            jsonObject(
                "type", jsonString("constantSequence"),
                "constantValue", jsonDouble(-12.34))),

        jsonApiPair(
            constantSequence(0.0),
            jsonObject(
                "type", jsonString("constantSequence"),
                "constantValue", jsonDouble(0))),

        jsonApiPair(
            constantSequence(12.34),
            jsonObject(
                "type", jsonString("constantSequence"),
                "constantValue", jsonDouble(12.34))));

    jsonApiTestData.testRoundTripConversions(
        v -> makeRealObject().toJsonObject(v, v2 -> jsonDouble(v2)),
        v -> makeRealObject().fromJsonObject(v, v2 -> v2.getAsDouble()));
  }

  @Test
  public void testUsingStrings_general() {
    JsonApiTestData<ConstantSequence<String>> jsonApiTestData = jsonApiTestData(
        f -> constantSequenceEqualityMatcher(f),

        jsonApiPair(
            constantSequence("x"),
            jsonObject(
                "type", jsonString("constantSequence"),
                "constantValue", jsonString("x"))));

    jsonApiTestData.testRoundTripConversions(
        v -> makeRealObject().toJsonObject(v, v2 -> jsonString(v2)),
        v -> makeRealObject().fromJsonObject(v, v2 -> v2.getAsString()));
  }

  @Override
  protected Class<ConstantSequenceJsonApiConverter> getClassBeingTested() {
    return ConstantSequenceJsonApiConverter.class;
  }

}
