package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedUnitFractionRanges;
import com.rb.nonbiz.collections.RBOptionals;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.ClosedUnitFractionRanges.closedUnitFractionRanges;
import static com.rb.nonbiz.collections.ClosedUnitFractionRanges.emptyClosedUnitFractionRanges;
import static com.rb.nonbiz.collections.ClosedUnitFractionRangesTest.closedUnitFractionRangesMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectMatcher;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInPct;
import static java.lang.Long.parseLong;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClosedUnitFractionRangesJsonApiConverterTest extends RBTest<ClosedUnitFractionRangesJsonApiConverter> {

  @Test
  public void testEmptyMap() {
    testInstrumentIdRoundTripConversionHelper(
        emptyClosedUnitFractionRanges(),
        emptyJsonObject());
  }

  @Test
  public void testRoundTrip_instrumentIds() {
    testInstrumentIdRoundTripConversionHelper(
        closedUnitFractionRanges(rbMapOf(
            instrumentId(111L), closedUnitFractionRange(
                unitFractionInPct(10),
                unitFractionInPct(11)),
            instrumentId(222L), closedUnitFractionRange(
                unitFractionInPct(20),
                unitFractionInPct(21)))),
        jsonObject(
            "111", jsonObject(
                "min", jsonDouble(10.0),
                "max", jsonDouble(11.0)),
            "222", jsonObject(
                "min", jsonDouble(20.0),
                "max", jsonDouble(21.0))));
  }

  @Test
  public void testRoundTrip_strings() {
    testRoundTripConversionHelper(
        closedUnitFractionRanges(rbMapOf(
            "US_Financials", closedUnitFractionRange(
                unitFractionInPct(10),
                unitFractionInPct(11)),
            "US_Software", closedUnitFractionRange(
                unitFractionInPct(20),
                unitFractionInPct(21)))),
        jsonObject(
            "US_Financials", jsonObject(
                "min", jsonDouble(10.0),
                "max", jsonDouble(11.0)),
            "US_Software", jsonObject(
                "min", jsonDouble(20.0),
                "max", jsonDouble(21.0))),
        key -> key,   // trivial key serializer
        key -> key);  // trivial key deserializer
  }

  private void testInstrumentIdRoundTripConversionHelper(
      ClosedUnitFractionRanges<InstrumentId> closedUnitFractionRanges,
      JsonObject jsonObject) {
    testRoundTripConversionHelper(
        closedUnitFractionRanges,
        jsonObject,
        iid -> iid.asString(),
        v -> instrumentId(parseLong(v)));
  }

  private <C extends Comparable<? super C>> void testRoundTripConversionHelper(
      ClosedUnitFractionRanges<C> closedUnitFractionRanges,
      JsonObject closedUnitFractionRangesJsonObject,
      Function<C, String> keySerializer,
      Function<String, C> keyDeserializer) {
    // check the conversion toJsonObject()
    assertThat(
        makeTestObject().toJsonObject(
            closedUnitFractionRanges,
            v -> keySerializer.apply(v)),
        jsonObjectMatcher(
            closedUnitFractionRangesJsonObject,
            DEFAULT_EPSILON_1e_8));

    // check the conversion fromJsonObject()
    assertThat(
        makeTestObject().fromJsonObject(
            closedUnitFractionRangesJsonObject,
            jsonElement -> keyDeserializer.apply(jsonElement)),
        closedUnitFractionRangesMatcher(
            closedUnitFractionRanges));
  }

  @Test
  public void testValidSampleJson() {
    ClosedUnitFractionRangesJsonApiConverter realObject =
        makeRealObject(ClosedUnitFractionRangesJsonApiConverter.class);

    JsonElement sampleJson = RBOptionals.getOrThrow(
        // have to cast because not all JsonApiDocumentation classes have NontrivialSampleJson
        ((JsonApiClassWithNonFixedPropertiesDocumentation) realObject.getJsonApiDocumentation()).getNontrivialSampleJson(),
        "Internal error - should have a sample JSON");

    // Check that the sample JSON can be successfully processed by fromJsonObject().
    ClosedUnitFractionRanges<String> doesNotThrow = realObject.fromJsonObject(
        sampleJson.getAsJsonObject(),
        v -> v);
  }

  @Override
  protected ClosedUnitFractionRangesJsonApiConverter makeTestObject() {
    return makeRealObject(ClosedUnitFractionRangesJsonApiConverter.class);
  }

}
