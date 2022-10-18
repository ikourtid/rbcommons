package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.RBOptionals;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.WeightedBySignedFraction;
import org.junit.Test;

import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.WeightedBySignedFraction.weightedBySignedFraction;
import static com.rb.nonbiz.types.WeightedBySignedFractionTest.weightedBySignedFractionMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

public class WeightedBySignedFractionJsonApiConverterTest
    extends RBTest<WeightedBySignedFractionJsonApiConverter> {

  @Test
  public void testZeroWeight() {
    testRoundTripConversionHelper(
        weightedBySignedFraction("ZERO", SIGNED_FRACTION_0),
        jsonObject(
            "weight", jsonDouble(0),
            "item",   jsonString("ZERO")));
  }

  @Test
  public void testPositiveWeight() {
    testRoundTripConversionHelper(
        weightedBySignedFraction("POSITIVE", signedFraction(0.123)),
        jsonObject(
            "weight", jsonDouble(0.123),
            "item",   jsonString("POSITIVE")));

    // SignedFractions can be greater than 1.0
    testRoundTripConversionHelper(
        weightedBySignedFraction("POSITIVE_LARGE", signedFraction(123_456.0)),
        jsonObject(
            "weight", jsonDouble(123_456.0),
            "item",   jsonString("POSITIVE_LARGE")));
  }

  @Test
  public void testNegativeWeight() {
    testRoundTripConversionHelper(
        weightedBySignedFraction("NEGATIVE", signedFraction(-0.123)),
        jsonObject(
            "weight", jsonDouble(-0.123),
            "item",   jsonString("NEGATIVE")));

    testRoundTripConversionHelper(
        weightedBySignedFraction("NEGATIVE_LARGE", signedFraction(-123_456.0)),
        jsonObject(
            "weight", jsonDouble(-123_456.0),
            "item",   jsonString("NEGATIVE_LARGE")));
  }

  private void testRoundTripConversionHelper(
      WeightedBySignedFraction<String> weightedBySignedFraction,
      JsonObject weightedBySignedFractionJsonObject) {
    assertThat(
        makeTestObject().toJsonObject(
            weightedBySignedFraction,
            string -> jsonString(string)),
        jsonObjectMatcher(
            weightedBySignedFractionJsonObject,
            DUMMY_EPSILON));   // no epsilon needed for String comparisons
    assertThat(
        makeTestObject().fromJsonObject(
            weightedBySignedFractionJsonObject,
            jsonElement -> jsonElement.getAsString()),
        weightedBySignedFractionMatcher(
            weightedBySignedFraction,
            v -> typeSafeEqualTo(v)));
  }

  @Test
  public void testValidSampleJson() {
    WeightedBySignedFractionJsonApiConverter realObject =
        makeRealObject(WeightedBySignedFractionJsonApiConverter.class);

    JsonElement sampleJson = RBOptionals.getOrThrow(
        // Cast to JsonApiClassDocumentation because not all implementers of HasJsonApiDocumentation
        // have optional sample JSON.
        ((JsonApiClassDocumentation) realObject.getJsonApiDocumentation())
            .getNontrivialSampleJson(),
        "Internal error - there should be sample JSON");

    // check that the sample JSON can be successfully processed by fromJsonArray()
    WeightedBySignedFraction<String> doesNotThrow = realObject.fromJsonObject(
        sampleJson.getAsJsonObject(),
        jsonElement -> jsonElement.getAsString());
  }

  @Override
  protected WeightedBySignedFractionJsonApiConverter makeTestObject() {
    return makeRealObject(WeightedBySignedFractionJsonApiConverter.class);
  }

}