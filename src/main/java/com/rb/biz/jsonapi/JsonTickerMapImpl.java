package com.rb.biz.jsonapi;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidBiMap;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;
import java.util.Optional;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;

/**
 * A simple concrete implementation of the {@link JsonTickerMap} interface, using an {@link IidBiMap}.
 */
public class JsonTickerMapImpl implements JsonTickerMap {

  private final IidBiMap<JsonTicker> rawBiMap;

  private JsonTickerMapImpl(IidBiMap<JsonTicker> rawBiMap) {
    this.rawBiMap = rawBiMap;
  }

  public static JsonTickerMapImpl jsonTickerMap(IidBiMap<JsonTicker> rawBiMap) {
    RBPreconditions.checkArgument(
        !rawBiMap.isEmpty(),
        "A JsonTickerMap cannot be empty; that would probably indicate a problem");
    return new JsonTickerMapImpl(rawBiMap);
  }

  public IidBiMap<JsonTicker> getRawBiMap() {
    return rawBiMap;
  }

  @Override
  public InstrumentId getInstrumentIdOrThrow(JsonTicker ticker) {
    return rawBiMap.getInstrumentIdFromItem().getOrThrow(ticker);
  }

  @Override
  public Optional<InstrumentId> getOptionalInstrumentId(JsonTicker ticker) {
    return rawBiMap.getInstrumentIdFromItem().getOptional(ticker);
  }

  @Override
  public JsonTicker getJsonTickerOrThrow(InstrumentId instrumentId) {
    return rawBiMap.getItemFromInstrumentId().getOrThrow(instrumentId);
  }

  @Override
  public Optional<JsonTicker> getOptionalJsonTicker(InstrumentId instrumentId) {
    return rawBiMap.getItemFromInstrumentId().getOptional(instrumentId);
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[JTM %s JTM]", rawBiMap.toString(instrumentMaster, date));
  }

}
