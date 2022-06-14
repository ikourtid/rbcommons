package com.rb.nonbiz.jsonapi;

import com.google.common.collect.Range;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rb.biz.types.Money;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.HardAndSoftRange;
import com.rb.nonbiz.types.RBNumeric;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectMatcher;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;
import static com.rb.nonbiz.types.Correlation.correlation;
import static com.rb.nonbiz.types.HardAndSoftRange.hardAndSoftRange;
import static com.rb.nonbiz.types.HardAndSoftRangeTest.hardAndSoftRangeMatcher;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
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
        signedFraction -> jsonDouble(signedFraction.doubleValue()),
        jsonPrimitive -> signedFraction(jsonPrimitive.getAsDouble()));
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
        correlation -> jsonDouble(correlation.doubleValue()),
        jsonPrimitive -> correlation(jsonPrimitive.getAsDouble()));
  }

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
            1e-8));

    assertThat(
        makeTestObject().fromJsonObject(
            rangeJsonObject,
            jsonPrimitive -> deserializer.apply(jsonPrimitive)),
        hardAndSoftRangeMatcher(
            hardAndSoftRange));
  }

  @Override
  protected HardAndSoftRangeJsonApiConverter makeTestObject() {
    return makeRealObject(HardAndSoftRangeJsonApiConverter.class);
  }

}