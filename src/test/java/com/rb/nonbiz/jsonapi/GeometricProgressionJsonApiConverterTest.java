package com.rb.nonbiz.jsonapi;

import com.rb.biz.types.Money;
import com.rb.nonbiz.collections.Pair;
import com.rb.nonbiz.math.sequence.GeometricProgression;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import com.rb.nonbiz.types.PositiveMultiplier;
import org.junit.Test;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.Money.sumMoney;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.PairTest.pairMatcher;
import static com.rb.nonbiz.json.RBGson.jsonBigDecimal;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonBigDecimalOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonStringOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.HasJsonApiDocumentationTestHelper.hasJsonApiDocumentationTestHelper;
import static com.rb.nonbiz.jsonapi.JsonApiPair.jsonApiPair;
import static com.rb.nonbiz.jsonapi.JsonApiTestData.jsonApiTestData;
import static com.rb.nonbiz.math.sequence.GeometricProgression.geometricProgression;
import static com.rb.nonbiz.math.sequence.GeometricProgressionTest.geometricProgressionMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.PositiveMultiplier.POSITIVE_MULTIPLIER_1;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;

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
        jsonObject -> geometricProgressionJsonApiConverter.fromJsonObject(
            jsonObject,
            v -> v.getAsDouble(),
            (v, commonDifference) -> v * commonDifference.doubleValue()));
  }

  @Test
  public void generalCase() {
    JsonApiTestData<GeometricProgression<Pair<String, Money>>> jsonApiTestData = jsonApiTestData(
        f -> geometricProgressionMatcher(f,
            f2 -> pairMatcher(f2, f3 -> typeSafeEqualTo(f3), f4 -> preciseValueMatcher(f4, DEFAULT_EPSILON_1e_8))),

        jsonApiPair(
            geometricProgression(
                pair("x", money(100.1)),
                positiveMultiplier(1.1),
                v -> pair(v.getLeft(), money(1.1 * v.getRight().doubleValue()))),
            jsonObject(
                "type",         jsonString("geometricProgression"),
                "initialValue", jsonObject(
                    "left",  jsonString("x"),
                    "right", jsonDouble(100.1)),
                "commonRatio",  jsonDouble(1.1))),

        jsonApiPair(
            geometricProgression(
                pair("x", money(100.1)),
                POSITIVE_MULTIPLIER_1,
                v -> v),
            jsonObject(
                "type",         jsonString("geometricProgression"),
                "initialValue", jsonObject(
                    "left",  jsonString("x"),
                    "right", jsonDouble(100.1)),
                "commonRatio",  jsonDouble(1.0))),

        jsonApiPair(
            geometricProgression(
                pair("x", money(100.1)),
                positiveMultiplier(0.8),
                v -> pair(v.getLeft(), money(0.8 * v.getRight().doubleValue()))),
            jsonObject(
                "type",         jsonString("geometricProgression"),
                "initialValue", jsonObject(
                    "left",  jsonString("x"),
                    "right", jsonDouble(100.1)),
                "commonRatio",  jsonDouble(0.8))));

    jsonApiTestData.testRoundTripConversions(
        v -> makeRealObject().toJsonObject(
            v,
            // serializer
            pair -> jsonObject(
                "left",  jsonString(pair.getLeft()),
                "right", jsonBigDecimal(pair.getRight()))),
        v -> makeRealObject().fromJsonObject(
            v,
            // deserializer
            jsonElement -> pair(
                getJsonStringOrThrow(jsonElement.getAsJsonObject(), "left"),
                money(getJsonBigDecimalOrThrow(jsonElement.getAsJsonObject(), "right"))),
            // next item generator
            (pair, commonRatio) -> pair(
                pair.getLeft(),
                money(pair.getRight().doubleValue() * commonRatio.doubleValue()))));
  }


  @Override
  protected Class<GeometricProgressionJsonApiConverter> getClassBeingTested() {
    return GeometricProgressionJsonApiConverter.class;
  }

}
