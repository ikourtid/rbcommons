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

  public static final JsonTickerMap TEST_JSON_TICKER_MAP = jsonTickerMap(iidMapOf(
      instrumentId(71), "eq1",
      instrumentId(81), "fi1",
      instrumentId(82), "fi2"));

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
    InstrumentId INSTRUMENT_ID_71 = instrumentId(71);
    InstrumentId MISSING_INSTRUMENT_ID = instrumentId(999);

    JsonTicker JSON_TICKER_EQ1 = jsonTicker("eq1");
    JsonTicker MISSING_JSON_TICKER = jsonTicker("missing");

    assertThat(
        TEST_JSON_TICKER_MAP.getJsonTickerOrThrow(INSTRUMENT_ID_71),
        jsonTickerMatcher(JSON_TICKER_EQ1));
    assertIllegalArgumentException( () -> TEST_JSON_TICKER_MAP.getJsonTickerOrThrow(MISSING_INSTRUMENT_ID));

    assertOptionalNonEmpty(
        TEST_JSON_TICKER_MAP.getOptionalJsonTicker(INSTRUMENT_ID_71),
        jsonTickerMatcher(JSON_TICKER_EQ1));
    assertOptionalEmpty(TEST_JSON_TICKER_MAP.getOptionalJsonTicker(MISSING_INSTRUMENT_ID));

    assertEquals(
        TEST_JSON_TICKER_MAP.getInstrumentIdOrThrow(JSON_TICKER_EQ1),
        INSTRUMENT_ID_71);
    assertIllegalArgumentException( () -> TEST_JSON_TICKER_MAP.getInstrumentIdOrThrow(MISSING_JSON_TICKER));

    assertOptionalEquals(
        INSTRUMENT_ID_71,
        TEST_JSON_TICKER_MAP.getOptionalInstrumentId(JSON_TICKER_EQ1));
    assertOptionalEmpty(TEST_JSON_TICKER_MAP.getOptionalInstrumentId(MISSING_JSON_TICKER));
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
