package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.math.sequence.GeometricProgression;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.HasJsonApiDocumentationTestHelper.hasJsonApiDocumentationTestHelper;
import static com.rb.nonbiz.jsonapi.JsonApiPair.jsonApiPair;
import static com.rb.nonbiz.jsonapi.JsonApiTestData.jsonApiTestData;
import static com.rb.nonbiz.math.sequence.GeometricProgression.GeometricProgressionBuilder.geometricProgressionBuilder;
import static com.rb.nonbiz.math.sequence.GeometricProgressionTest.geometricProgressionMatcher;

public class GeometricProgressionJsonApiConverterTest
    extends RBCommonsIntegrationTest<GeometricProgressionJsonApiConverter> {

  // Check that the "sample" JSON element in the JsonApiDocumentation can be transformed
  // to a valid Java object via #fromJsonObject.
  // We don't want to display any JSON that can't be converted.
  @Test
  public void hasJsonApiDocumentation_runAllTests() {
    GeometricProgressionJsonApiConverter geometricProgressionJsonApiConverter = makeRealObject();
    hasJsonApiDocumentationTestHelper().runAllTests(
        geometricProgressionJsonApiConverter,
        jsonObject -> geometricProgressionJsonApiConverter.fromJsonObject(jsonObject));
  }

  @Test
  public void generalCase() {
    JsonApiTestData<GeometricProgression> jsonApiTestData = jsonApiTestData(
        f -> geometricProgressionMatcher(f),

        jsonApiPair(
            geometricProgressionBuilder()
                .setInitialValue(111.22)
                .setCommonRatio(1.08)
                .build(),
            jsonObject(
                "type",         jsonString("geometricProgression"),
                "initialValue", jsonDouble(111.22),
                "commonRatio",  jsonDouble(1.08))),

        jsonApiPair(
            geometricProgressionBuilder()
                .setInitialValue(111.22)
                .setCommonRatio(1)
                .build(),
            jsonObject(
                "type",         jsonString("geometricProgression"),
                "initialValue", jsonDouble(111.22),
                "commonRatio",  jsonDouble(1))), // trivial, but still works: see singleValueGeometricProgression

        jsonApiPair(
            geometricProgressionBuilder()
                .setInitialValue(-111.22)
                .setCommonRatio(0.98)
                .build(),
            jsonObject(
                "type",         jsonString("geometricProgression"),
                "initialValue", jsonDouble(-111.22),
                "commonRatio",  jsonDouble(0.98))));

    jsonApiTestData.testRoundTripConversions(
        v -> makeRealObject().toJsonObject(v),
        v -> makeRealObject().fromJsonObject(v));
  }

  @Override
  protected Class<GeometricProgressionJsonApiConverter> getClassBeingTested() {
    return GeometricProgressionJsonApiConverter.class;
  }

}
