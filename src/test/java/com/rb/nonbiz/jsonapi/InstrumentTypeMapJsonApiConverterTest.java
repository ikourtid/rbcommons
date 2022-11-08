package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.biz.types.asset.InstrumentTypeMap;
import com.rb.biz.types.asset.InstrumentTypeMap.InstrumentTypeMapBuilder;
import com.rb.nonbiz.collections.RBOptionals;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.biz.types.asset.InstrumentTypeMapTest.instrumentTypeMapMatcher;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;
import static org.hamcrest.MatcherAssert.assertThat;

public class InstrumentTypeMapJsonApiConverterTest
    extends RBTest<InstrumentTypeMapJsonApiConverter> {

  @Test
  public void testRoundTrip_noDefaultValuesNeeded() {
    InstrumentTypeMap<Double> instrumentTypeMap = InstrumentTypeMapBuilder.<Double>instrumentTypeMapBuilder()
        .setValueForEtfs(              1.1)
        .setValueForStocks(            2.2)
        .setValueForMutualFunds(       3.3)
        .setValueForStructuredProducts(4.4)
        .build();

    JsonObject JSON_OBJECT = jsonObject(
        "etf",               jsonDouble(1.1),
        "stock",             jsonDouble(2.2),
        "mutualFund",        jsonDouble(3.3),
        "structuredProduct", jsonDouble(4.4));

    assertThat(
        makeTestObject().toJsonObject(
            instrumentTypeMap,
            d -> jsonDouble(d)),
        jsonObjectMatcher(
            JSON_OBJECT,
            1e-8));

    assertThat(
        makeTestObject().fromJsonObject(
            JSON_OBJECT,
            jsonElement -> jsonElement.getAsDouble(),
            DUMMY_DOUBLE),           // the default value is not needed; no missing values
        instrumentTypeMapMatcher(
            instrumentTypeMap, f -> typeSafeEqualTo(f)));
  }

  @Test
  public void testRoundTrip_useDefault() {
    double valueIfMissing = -123.45;
    assertThat(
        makeTestObject().fromJsonObject(
            jsonObject(
                "etf",               jsonDouble(1.1),
                // no "stock" entry
                // no "mutualFund" entry
                "structuredProduct", jsonDouble(4.4)),
            jsonElement -> jsonElement.getAsDouble(),
            valueIfMissing),
        instrumentTypeMapMatcher(
            InstrumentTypeMapBuilder.<Double>instrumentTypeMapBuilder()
                .setValueForEtfs(              1.1)
                .setValueForStocks(            valueIfMissing)
                .setValueForMutualFunds(       valueIfMissing)
                .setValueForStructuredProducts(4.4)
                .build(),
            f -> typeSafeEqualTo(f)));
  }

  @Test
  public void testValidSampleJson() {
    InstrumentTypeMapJsonApiConverter realObject = makeTestObject();

    JsonElement sampleJson = RBOptionals.getOrThrow(
        // have to cast because not all JsonApiDocumentation classes have NontrivialSampleJson
        ((JsonApiClassDocumentation) realObject.getJsonApiDocumentation()).getNontrivialSampleJson(),
        "Internal error - should have a sample JSON");

    // Check that the sample JSON can be successfully processed by fromJsonObject().
    InstrumentTypeMap<Double> doesNotThrow = realObject.fromJsonObject(
        sampleJson.getAsJsonObject(),
        v -> v.getAsDouble(),
        DUMMY_DOUBLE);
  }

  @Override
  protected InstrumentTypeMapJsonApiConverter makeTestObject() {
    return makeRealObject(InstrumentTypeMapJsonApiConverter.class);
  }

}