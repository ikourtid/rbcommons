package com.rb.biz.jsonapi;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.jsonapi.JsonTicker.jsonTicker;
import static com.rb.biz.jsonapi.JsonTickerTest.jsonTickerMatcher;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.IidBiMap.iidBiMap;
import static com.rb.nonbiz.collections.IidBiMapTest.iidBiMapMatcher;
import static com.rb.nonbiz.collections.IidBiMaps.emptyIidBiMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalNonEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class JsonTickerMapImplTest extends RBTestMatcher<JsonTickerMapImpl> {

  // This is a test-only shorthand.
  public static JsonTickerMapImpl jsonTickerMap(IidMap<String> rawMap) {
    return JsonTickerMapImpl.jsonTickerMap(iidBiMap(rawMap.transformValuesCopy(v -> jsonTicker(v))));
  }

  @Test
  public void tickerMapIsEmpty_throws() {
    assertIllegalArgumentException( () -> JsonTickerMapImpl.jsonTickerMap(emptyIidBiMap()));
  }

  @Test
  public void testGetters() {
    InstrumentId instrumentId71 = instrumentId(71);
    InstrumentId missingInstrumentId = instrumentId(999);

    JsonTicker jsonTickerEq1 = jsonTicker("eq1");
    JsonTicker missingJsonTicker = jsonTicker("missing");

    JsonTickerMap jsonTickerMap = jsonTickerMap(iidMapOf(
        instrumentId(71), "eq1",
        instrumentId(81), "fi1",
        instrumentId(82), "fi2"));

    assertThat(
        jsonTickerMap.getJsonTickerOrThrow(instrumentId71),
        jsonTickerMatcher(jsonTickerEq1));
    assertIllegalArgumentException( () -> jsonTickerMap.getJsonTickerOrThrow(missingInstrumentId));

    assertOptionalNonEmpty(
        jsonTickerMap.getOptionalJsonTicker(instrumentId71),
        jsonTickerMatcher(jsonTickerEq1));
    assertOptionalEmpty(jsonTickerMap.getOptionalJsonTicker(missingInstrumentId));

    assertEquals(
        jsonTickerMap.getInstrumentIdOrThrow(jsonTickerEq1),
        instrumentId71);
    assertIllegalArgumentException( () -> jsonTickerMap.getInstrumentIdOrThrow(missingJsonTicker));

    assertOptionalEquals(
        instrumentId71,
        jsonTickerMap.getOptionalInstrumentId(jsonTickerEq1));
    assertOptionalEmpty(jsonTickerMap.getOptionalInstrumentId(missingJsonTicker));
  }

  @Override
  public JsonTickerMapImpl makeTrivialObject() {
    return jsonTickerMap(singletonIidMap(STOCK_A, "A"));
  }

  @Override
  public JsonTickerMapImpl makeNontrivialObject() {
    return jsonTickerMap(iidMapOf(
        STOCK_A, "AAA",
        STOCK_B, "BBB"));
  }

  @Override
  public JsonTickerMapImpl makeMatchingNontrivialObject() {
    // nothing to tweak here
    return jsonTickerMap(iidMapOf(
        STOCK_A, "AAA",
        STOCK_B, "BBB"));
  }

  @Override
  protected boolean willMatch(JsonTickerMapImpl expected, JsonTickerMapImpl actual) {
    return jsonTickerMapImplMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonTickerMapImpl> jsonTickerMapImplMatcher(JsonTickerMapImpl expected) {
    return makeMatcher(expected,
        match(v -> v.getRawBiMap(), f -> iidBiMapMatcher(f)));
  }

}
