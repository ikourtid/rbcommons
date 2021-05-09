package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.rb.biz.types.SignedMoney;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapPreciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.hamcrest.MatcherAssert.assertThat;

public class PreciseValueRBMapJsonConverterTest extends RBTest<PreciseValueRBMapJsonConverter> {

  @Test
  public void testRoundTripConversions_empty() {
    testRoundTripConversionsHelper(
        emptyRBMap(),
        emptyJsonObject());
  }

  @Test
  public void testRoundTripConversions_nonEmpty() {
    testRoundTripConversionsHelper(
        rbMapOf(
            instrumentId(1), signedMoney(1.1),
            instrumentId(2), signedMoney(-2.2)),
        jsonObject(
            "1", jsonDouble(1.1),
            "2", jsonDouble(-2.2)));
  }

  @Test
  public void jsonKeyDeserializerFunctionIsNotOneToOne_throws() {
    assertIllegalArgumentException( () -> makeTestObject().<InstrumentId, SignedMoney>fromJsonObject(
        jsonObject(
            "1", jsonDouble(1.1),
            "2", jsonDouble(-2.2)),
        s -> instrumentId(123), // fixed key
        v -> signedMoney(v)));
  }

  @Test
  public void rbMapKeyDeserializerFunctionIsNotOneToOne_throws() {
    assertIllegalArgumentException( () -> makeTestObject().toJsonObject(
        rbMapOf(
            instrumentId(1), signedMoney(1.1),
            instrumentId(2), signedMoney(-2.2)),
        instrumentId -> "fixedKey"));
  }

  private void testRoundTripConversionsHelper(RBMap<InstrumentId, SignedMoney> map, JsonObject json) {
    assertThat(
        makeTestObject().toJsonObject(map, instrumentId -> Long.toString(instrumentId.asLong())),
        jsonObjectEpsilonMatcher(json));
    assertThat(
        makeTestObject().fromJsonObject(json, s -> instrumentId(Long.parseLong(s)), v -> signedMoney(v)),
        rbMapPreciseValueMatcher(map, 1e-14));
  }

  @Override
  protected PreciseValueRBMapJsonConverter makeTestObject() {
    return new PreciseValueRBMapJsonConverter();
  }

}
