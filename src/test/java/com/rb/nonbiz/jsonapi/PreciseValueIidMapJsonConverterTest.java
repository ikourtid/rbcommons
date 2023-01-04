package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.rb.biz.jsonapi.JsonTickerMap;
import com.rb.biz.types.SignedMoney;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import static com.rb.biz.jsonapi.JsonTickerMapImplTest.jsonTickerMap;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapTest.iidMapPreciseValueMatcher;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static org.hamcrest.MatcherAssert.assertThat;

public class PreciseValueIidMapJsonConverterTest extends RBCommonsIntegrationTest<PreciseValueIidMapJsonConverter> {

  private final JsonTickerMap TICKER_MAP = jsonTickerMap(iidMapOf(
      instrumentId(1), "S1",
      instrumentId(2), "S2"));

  @Test
  public void testRoundTripConversions_empty() {
    testRoundTripConversionsHelper(
        emptyIidMap(),
        emptyJsonObject());
  }

  @Test
  public void testRoundTripConversions_nonEmpty() {
    testRoundTripConversionsHelper(
        iidMapOf(
            instrumentId(1), signedMoney(1.1),
            instrumentId(2), signedMoney(-2.2)),
        jsonObject(
            "S1", jsonDouble(1.1),
            "S2", jsonDouble(-2.2)));
  }

  private void testRoundTripConversionsHelper(IidMap<SignedMoney> map, JsonObject json) {
    assertThat(
        makeRealObject().toJsonObject(map, TICKER_MAP),
        jsonObjectEpsilonMatcher(json));
    assertThat(
        makeRealObject().fromJsonObject(json, TICKER_MAP, v -> signedMoney(v)),
        iidMapPreciseValueMatcher(map, epsilon(1e-14)));
  }

  @Override
  protected Class<PreciseValueIidMapJsonConverter> getClassBeingTested() {
    return PreciseValueIidMapJsonConverter.class;
  }

}
