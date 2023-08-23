package com.rb.nonbiz.jsonapi;

import com.rb.biz.types.Money;
import com.rb.nonbiz.collections.Pair;
import com.rb.nonbiz.math.sequence.ArithmeticProgression;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
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
import static com.rb.nonbiz.math.sequence.ArithmeticProgression.arithmeticProgression;
import static com.rb.nonbiz.math.sequence.ArithmeticProgressionTest.arithmeticProgressionMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

public class ArithmeticProgressionJsonApiConverterTest
    extends RBCommonsIntegrationTest<ArithmeticProgressionJsonApiConverter> {

  @Test
  public void hasJsonApiDocumentation_runAllTests() {
    ArithmeticProgressionJsonApiConverter arithmeticProgressionJsonApiConverter = makeRealObject();
    hasJsonApiDocumentationTestHelper().runAllTests(
        arithmeticProgressionJsonApiConverter,
        jsonObject -> arithmeticProgressionJsonApiConverter.fromJsonObject(
            jsonObject,
            v -> v.getAsDouble(),
            (v, commonDifference) -> v + commonDifference));
  }

  @Test
  public void generalCase() {
    JsonApiTestData<ArithmeticProgression<Pair<String, Money>>> jsonApiTestData = jsonApiTestData(
        f -> arithmeticProgressionMatcher(f,
            f2 -> pairMatcher(f2, f3 -> typeSafeEqualTo(f3), f4 -> preciseValueMatcher(f4, DEFAULT_EPSILON_1e_8))),

        jsonApiPair(
            arithmeticProgression(
                pair("x", money(100.0)),
                1.1,
                v -> pair(v.getLeft(), sumMoney(v.getRight(), money(1.1)))),
            jsonObject(
                "type",             jsonString("arithmeticProgression"),
                "initialValue", jsonObject(
                    "left", jsonString("x"),
                    "right", jsonDouble(100.0)),
                "commonDifference", jsonDouble(1.1))),

        jsonApiPair(
            arithmeticProgression(
                pair("x", money(100.0)),
                0.0,
                v -> pair(v.getLeft(), sumMoney(v.getRight(), ZERO_MONEY))),
            jsonObject(
                "type",             jsonString("arithmeticProgression"),
                "initialValue", jsonObject(
                    "left", jsonString("x"),
                    "right", jsonDouble(100.0)),
                "commonDifference", jsonDouble(0))),

        jsonApiPair(
            arithmeticProgression(
                pair("x", money(100.0)),
                -1.1,
                v -> pair(v.getLeft(), money(v.getRight().doubleValue() - 1.1))),
            jsonObject(
                "type",             jsonString("arithmeticProgression"),
                "initialValue", jsonObject(
                    "left", jsonString("x"),
                    "right", jsonDouble(100.0)),
                "commonDifference", jsonDouble(-1.1))));

    jsonApiTestData.testRoundTripConversions(
        v -> makeRealObject().toJsonObject(
            v,
            // serializer
            pair -> jsonObject(
                "left", jsonString(pair.getLeft()),
                "right", jsonBigDecimal(pair.getRight()))),
        v -> makeRealObject().fromJsonObject(
            v,
            // deserializer
            jsonElement -> pair(
                getJsonStringOrThrow(jsonElement.getAsJsonObject(), "left"),
                money(getJsonBigDecimalOrThrow(jsonElement.getAsJsonObject(), "right"))),
            // next item generator
            (pair, commonDifference) -> pair(
                pair.getLeft(),
                money(pair.getRight().doubleValue() + commonDifference))));
  }

  @Override
  protected Class<ArithmeticProgressionJsonApiConverter> getClassBeingTested() {
    return ArithmeticProgressionJsonApiConverter.class;
  }

}
