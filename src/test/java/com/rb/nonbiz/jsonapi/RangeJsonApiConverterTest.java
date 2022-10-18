package com.rb.nonbiz.jsonapi;

import com.google.common.collect.Range;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rb.biz.types.Money;
import com.rb.nonbiz.collections.RBOptionals;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.RBRangesTest.allRangesWithAnExlusiveBound;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.rangeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.impreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;
import static com.rb.nonbiz.types.Correlation.correlation;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class RangeJsonApiConverterTest extends RBTest<RangeJsonApiConverter> {

  @Test
  public void convertRangeWithOpenBound_throws() {
    Function<Range<Money>, JsonObject> maker =
        range -> makeTestObject().toJsonObject(
            range,
            money -> jsonDouble(money.doubleValue()));

    JsonObject doesNotThrow;
    doesNotThrow = maker.apply(Range.atLeast(money(111)));
    doesNotThrow = maker.apply(Range.atMost( money(222)));
    doesNotThrow = maker.apply(Range.closed( money(111), money(222)));

    // can't convert ranges with open bounds, e.g. with a boundary point that is not part of the range
    for (Range<Money> exclusiveBoundRange: allRangesWithAnExlusiveBound(money(111), money(222))) {
      assertIllegalArgumentException( () -> maker.apply(exclusiveBoundRange));
    }
  }

  @Test
  public void testRanges_differentTypes() {
    // convert a Range of PreciseValues
    testRoundTripConversionHelper(
        Range.closed(SIGNED_FRACTION_0, signedFraction(0.1)),
        jsonObject(
            "min", jsonDouble(0.0),
            "max", jsonDouble(0.1)),
        signedFraction -> jsonDouble(signedFraction.doubleValue()),
        jsonPrimitive  -> signedFraction(jsonPrimitive.getAsDouble()),
        f -> preciseValueMatcher(f, 1e-8));

    // convert a Range of ImpreciseValues
    testRoundTripConversionHelper(
        Range.closed(correlation(0), correlation(1)),
        jsonObject(
            "min", jsonDouble(0),
            "max", jsonDouble(1)),
        correlation   -> jsonDouble(correlation.doubleValue()),
        jsonPrimitive -> correlation(jsonPrimitive.getAsDouble()),
        f -> impreciseValueMatcher(f, 1e-8));

    // convert a Range of Doubles
    testRoundTripConversionHelper(
        Range.closed(11.1, 22.2),
        jsonObject(
            "min", jsonDouble(11.1),
            "max", jsonDouble(22.2)),
        d -> jsonDouble(d),
        jsonPrimitive -> jsonPrimitive.getAsDouble(),
        f -> doubleAlmostEqualsMatcher(f, 1e-8));

    // convert a Range of Strings
    testRoundTripConversionHelper(
        Range.closed("aaa", "bbb"),
        jsonObject(
            "min", jsonString("aaa"),
            "max", jsonString("bbb")),
        s -> jsonString(s),
        jsonPrimitive -> jsonPrimitive.getAsString(),
        f -> typeSafeEqualTo(f));

    // convert a Range of Characters
    testRoundTripConversionHelper(
        //makeTestObject().toJsonObject(
        Range.closed('A', 'Z'),
        jsonObject(
            "min", jsonString("A"),
            "max", jsonString("Z")),
        c -> jsonString(c.toString()),
        jsonPrimitive -> jsonPrimitive.getAsCharacter(),
        f -> typeSafeEqualTo(f));
  }

  @Test
  public void testRoundTripClosedRange() {
    testMoneyRoundTripConversionHelper(
        Range.closed(money(111), money(222)),
        jsonObject(
            "min", jsonDouble(111),
            "max", jsonDouble(222)));
  }

  @Test
  public void testRoundTripOneSidedRange() {
    testMoneyRoundTripConversionHelper(
        Range.atLeast(money(111)),
        singletonJsonObject(
            "min", jsonDouble(111)));

    testMoneyRoundTripConversionHelper(
        Range.atMost(money(222)),
        singletonJsonObject(
            "max", jsonDouble(222)));
  }

  @Test
  public void testRoundTripOpenRange() {
    testMoneyRoundTripConversionHelper(
        Range.all(),  // the open range; no boundaries
        emptyJsonObject());
  }

  private void testMoneyRoundTripConversionHelper(
      Range<Money> moneyRange,
      JsonObject jsonObject) {
    testRoundTripConversionHelper(
        moneyRange,
        jsonObject,
        v -> jsonDouble(v),
        jsonPrimitive -> money(jsonPrimitive.getAsDouble()),
        f -> preciseValueMatcher(f, 1e-8));
  }

  private <C extends Comparable<? super C>> void testRoundTripConversionHelper(
      Range<C> range,
      JsonObject rangeJsonObject,
      Function<C, JsonPrimitive> serializer,
      Function<JsonPrimitive, C> deserializer,
      MatcherGenerator<C> matcherGenerator) {
    assertThat(
        makeTestObject().toJsonObject(
            range,
            v -> serializer.apply(v)),
        jsonObjectMatcher(
            rangeJsonObject,
            1e-8));

    assertThat(
        makeTestObject().fromJsonObject(
            rangeJsonObject,
            jsonPrimitive -> deserializer.apply(jsonPrimitive)),
        rangeMatcher(
            range,
            matcherGenerator));
  }

  @Test
  public void testValidSampleJson() {
    RangeJsonApiConverter realObject = makeRealObject(RangeJsonApiConverter.class);

    JsonElement sampleJson = RBOptionals.getOrThrow(
        // have to cast because not all JsonApiDocumentation classes have NontrivialSampleJson
        ((JsonApiClassDocumentation) realObject.getJsonApiDocumentation()).getNontrivialSampleJson(),
        "Internal error - should have a sample JSON");

    // Check that the sample JSON can be successfully processed by fromJsonObject().
    Range<Double> doesNotThrow = realObject.fromJsonObject(
        sampleJson.getAsJsonObject(),
        v -> v.getAsDouble());
  }

  @Override
  protected RangeJsonApiConverter makeTestObject() {
    return makeRealObject(RangeJsonApiConverter.class);
  }

}