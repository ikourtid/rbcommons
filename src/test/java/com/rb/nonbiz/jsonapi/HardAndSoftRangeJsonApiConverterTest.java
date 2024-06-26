package com.rb.nonbiz.jsonapi;

import com.google.common.collect.Range;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rb.biz.types.Money;
import com.rb.nonbiz.collections.RBOptionals;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.HardAndSoftRange;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectMatcher;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;
import static com.rb.nonbiz.types.Correlation.correlation;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.HardAndSoftRange.hardAndSoftRange;
import static com.rb.nonbiz.types.HardAndSoftRangeTest.hardAndSoftRangeMatcher;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class HardAndSoftRangeJsonApiConverterTest extends RBTest<HardAndSoftRangeJsonApiConverter> {

  // convert a HardAndSoftRange of PreciseValues
  @Test
  public void testHardAndSoftRanges_preciseValues() {
    testRoundTripConversionHelper(
        hardAndSoftRange(
            Range.closed(signedFraction(0.1), signedFraction(0.4)),
            Range.closed(signedFraction(0.2), signedFraction(0.3))),
        jsonObject(
            "hardRange", jsonObject(
                "min", jsonDouble(0.1),
                "max", jsonDouble(0.4)),
            "softRange", jsonObject(
                "min", jsonDouble(0.2),
                "max", jsonDouble(0.3))),
        signedFraction -> jsonDouble(signedFraction),
        jsonPrimitive  -> signedFraction(jsonPrimitive.getAsDouble()));
  }

  // convert a HardAndSoftRange of ImpreciseValues
  @Test
  public void testHardAndSoftRanges_impreciseValues() {
    testRoundTripConversionHelper(
        hardAndSoftRange(
            Range.closed(correlation(0.111), correlation(0.444)),
            Range.closed(correlation(0.222), correlation(0.333))),
        jsonObject(
            "hardRange", jsonObject(
                "min", jsonDouble(0.111),
                "max", jsonDouble(0.444)),
            "softRange", jsonObject(
                "min", jsonDouble(0.222),
                "max", jsonDouble(0.333))),
        correlation   -> jsonDouble(correlation),
        jsonPrimitive -> correlation(jsonPrimitive.getAsDouble()));
  }

  @Test
  public void testOpenRanges_atMost() {
    // convert a HardAndSoftRange that has no lower bounds
    testMoneyRoundTripConversionHelper(
        hardAndSoftRange(
            Range.atMost(money(100)),
            Range.atMost(money(90))),
        jsonObject(
            "hardRange", singletonJsonObject(
                "max", jsonDouble(100)),
            "softRange", singletonJsonObject(
                "max", jsonDouble( 90))));
  }

  @Test
  public void testOpenRanges_atLeast() {
    // convert a HardAndSoftRange that has no upper bounds
    testMoneyRoundTripConversionHelper(
        hardAndSoftRange(
            Range.atLeast(money(10)),
            Range.atLeast(money(20))),
        jsonObject(
            "hardRange", singletonJsonObject(
                "min", jsonDouble(10)),
            "softRange", singletonJsonObject(
                "min", jsonDouble(20))));
  }

  // can't check for a hard range with no bounds;      HardAndSoftRange disallows that
  // can't check for a soft range with a single point; HardAndSoftRange disallows that
  // can't check for hard and soft ranges that have one common boundary; not allowed

  private void testMoneyRoundTripConversionHelper(
      HardAndSoftRange<Money> moneyHardAndSoftRange,
      JsonObject jsonObject) {
    testRoundTripConversionHelper(
        moneyHardAndSoftRange,
        jsonObject,
        v -> jsonDouble(v),
        jsonPrimitive -> money(jsonPrimitive.getAsDouble()));
  }

  private <T extends RBNumeric<? super T>> void testRoundTripConversionHelper(
      HardAndSoftRange<T> hardAndSoftRange,
      JsonObject rangeJsonObject,
      Function<T, JsonPrimitive> serializer,
      Function<JsonPrimitive, T> deserializer) {
    assertThat(
        makeTestObject().toJsonObject(
            hardAndSoftRange,
            v -> serializer.apply(v)),
        jsonObjectMatcher(
            rangeJsonObject,
            DEFAULT_EPSILON_1e_8));

    assertThat(
        makeTestObject().fromJsonObject(
            rangeJsonObject,
            jsonPrimitive -> deserializer.apply(jsonPrimitive)),
        hardAndSoftRangeMatcher(
            hardAndSoftRange));
  }

  @Test
  public void testValidSampleJson() {
    HardAndSoftRangeJsonApiConverter realObject =
        makeRealObject(HardAndSoftRangeJsonApiConverter.class);

    JsonElement sampleJson = RBOptionals.getOrThrow(
        // Cast to JsonApiClassDocumentation because not all implementers of HasJsonApiDocumentation
        // have optional sample JSON.
        ((JsonApiClassDocumentation) realObject.getJsonApiDocumentation())
            .getNontrivialSampleJson(),
        "Internal error - there should be sample JSON");

    // check that the sample JSON can be successfully processed by fromJsonObject()
    HardAndSoftRange<UnitFraction> doesNotThrow = realObject.fromJsonObject(
        sampleJson.getAsJsonObject(),
        jsonPrimitive -> unitFraction(jsonPrimitive.getAsDouble()));
  }

  @Override
  protected HardAndSoftRangeJsonApiConverter makeTestObject() {
    return makeRealObject(HardAndSoftRangeJsonApiConverter.class);
  }

}