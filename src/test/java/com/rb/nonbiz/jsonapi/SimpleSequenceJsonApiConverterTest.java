package com.rb.nonbiz.jsonapi;

import com.rb.biz.types.Money;
import com.rb.nonbiz.collections.Pair;
import com.rb.nonbiz.math.sequence.SimpleSequence;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

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
import static com.rb.nonbiz.math.sequence.ArithmeticProgression.arithmeticProgression;
import static com.rb.nonbiz.math.sequence.ConstantSequence.constantSequence;
import static com.rb.nonbiz.math.sequence.GeometricProgression.geometricProgression;
import static com.rb.nonbiz.math.sequence.SimpleSequenceTest.simpleSequenceMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;

public class SimpleSequenceJsonApiConverterTest
    extends RBCommonsIntegrationTest<SimpleSequenceJsonApiConverter> {

  @Test
  public void handlesAllSubclasses() {
    JsonApiTestData<SimpleSequence<Pair<String, Money>>> jsonApiTestData = jsonApiTestData(
        f -> simpleSequenceMatcher(f,
            f2 -> pairMatcher(f2, f3 -> typeSafeEqualTo(f3), f4 -> preciseValueMatcher(f4, DEFAULT_EPSILON_1e_8))),

        jsonApiPair(
            arithmeticProgression(
                pair("x", money(100.0)),
                1.1,
                v -> pair(v.getLeft(), sumMoney(v.getRight(), money(1.1)))),
            jsonObject(
                "type", jsonString("arithmeticProgression"),
                "initialValue", jsonDouble(111.22),
                "commonDifference", jsonDouble(0.33))),

        jsonApiPair(
            geometricProgression(
                pair("x", money(100.1)),
                positiveMultiplier(1.1),
                v -> pair(v.getLeft(), money(1.1 * v.getRight().doubleValue()))),
            jsonObject(
                "type", jsonString("geometricProgression"),
                "initialValue", jsonObject(
                    "left", jsonString("x"),
                    "right", jsonDouble(100.1)),
                "commonRatio", jsonDouble(1.1))),

        jsonApiPair(
            constantSequence(pair("y", money(12.34))),
            jsonObject(
                "type", jsonString("constantSequence"),
                "constantValue", jsonObject(
                    "left", jsonString("x"),
                    "right", jsonDouble(12.34)))));

    jsonApiTestData.testRoundTripConversions(
        v -> makeRealObject().toJsonObject(
            v,
            pair -> jsonObject(
                "left",  jsonString(pair.getLeft()),
                "right", jsonBigDecimal(pair.getRight()))),
        v -> makeRealObject().fromJsonObject(
            v,
            // deserializer
            jsonElement -> pair(
                getJsonStringOrThrow(jsonElement.getAsJsonObject(), "left"),
                money(getJsonBigDecimalOrThrow(jsonElement.getAsJsonObject(), "right"))),
            // next item generator, for case of arithmetic progression
            (pair, commonDifference) -> pair(
                pair.getLeft(),
                money(pair.getRight().doubleValue() + commonDifference)),

            // next item generator, for case of geometric progression
            (pair, commonRatio) -> pair(
                pair.getLeft(),
                money(pair.getRight().doubleValue() * commonRatio.doubleValue()))));
  }

  @Test
  public void hasJsonApiDocumentation_runAllTests() {
    SimpleSequenceJsonApiConverter simpleSequenceJsonApiConverter = makeRealObject();
    hasJsonApiDocumentationTestHelper().runAllTests(
        simpleSequenceJsonApiConverter,
        v -> simpleSequenceJsonApiConverter.fromJsonObject(
            v,
            v1 -> v1.getAsDouble(),
            (v2, commonDifference) -> v2 + commonDifference,
            (v3, commonRatio ) -> v3 * commonRatio.doubleValue()));
  }

  @Override
  protected Class<SimpleSequenceJsonApiConverter> getClassBeingTested() {
    return SimpleSequenceJsonApiConverter.class;
  }

}
