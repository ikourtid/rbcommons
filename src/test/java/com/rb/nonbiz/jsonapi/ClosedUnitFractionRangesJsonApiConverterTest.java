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
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unitFractionAtLeast;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unitFractionAtMost;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unrestrictedClosedUnitFractionRange;
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
  public void testDefaultValues() {
    ClosedUnitFractionRanges<InstrumentId> closedUnitFractionRangesNoDefaults = closedUnitFractionRanges(rbMapOf(
        instrumentId(111L), unrestrictedClosedUnitFractionRange(),           // default min = 0 % and max = 100 %
        instrumentId(222L), unitFractionAtLeast(unitFractionInPct(10)),      // default max = 100 %
        instrumentId(333L), unitFractionAtMost( unitFractionInPct(90))));    // default min = 0 %

    // converting from Json: default min = 0 %; default max = 100 %
    testFromJsonHelper(
        closedUnitFractionRangesNoDefaults,
        jsonObject(
            "111", emptyJsonObject(),
            "222", singletonJsonObject(
                "min", jsonDouble(10)),    // no 'max'; 100 % is the default
            "333", singletonJsonObject(
                "max", jsonDouble(90))),   // no 'min'; 0 % is the default
        v -> instrumentId(parseLong(v)));

    // converting to Json: default 'min' and 'max' appear in the Json
    testToJsonHelper(
        closedUnitFractionRangesNoDefaults,
        jsonObject(
            "111", jsonObject(
                "min", jsonDouble(0),     // both entries are optional for fromJsonObject()
                "max", jsonDouble(100)),  // default min = 0 %; default max = 100 %
            "222", jsonObject(
                "min", jsonDouble(10),
                "max", jsonDouble(100)),  // this is optional for fromJsonObject() but appears for toJsonObject()
            "333", jsonObject(
                "min", jsonDouble(0),     // this is optional for fromJsonObject() but appears for toJsonObject()
                "max", jsonDouble(90))),
        iid -> iid.asString());
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
    testToJsonHelper(closedUnitFractionRanges, closedUnitFractionRangesJsonObject, keySerializer);

    // check the conversion fromJsonObject()
    testFromJsonHelper(closedUnitFractionRanges, closedUnitFractionRangesJsonObject, keyDeserializer);
  }

  private <C extends Comparable<? super C>> void testToJsonHelper(
      ClosedUnitFractionRanges<C> closedUnitFractionRanges,
      JsonObject closedUnitFractionRangesJsonObject,
      Function<C, String> keySerializer) {
    
    assertThat(
        makeTestObject().toJsonObject(
            closedUnitFractionRanges,
            v -> keySerializer.apply(v)),
        jsonObjectEpsilonMatcher(
            closedUnitFractionRangesJsonObject));
  }

  private <C extends Comparable<? super C>> void testFromJsonHelper(
      ClosedUnitFractionRanges<C> closedUnitFractionRanges,
      JsonObject closedUnitFractionRangesJsonObject,
      Function<String, C> keyDeserializer) {
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
