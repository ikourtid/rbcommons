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
 * This is a {@link JsonTicker} {@code <-->} {@link InstrumentId} bidirectional map ({@link IidBiMap}).
 *
 * <p> We aren't calling this {@code SymbolMap} because it doesn't use our {@link Symbol} class
 * (which has restrictions on length, characters allowed, etc.) </p>
 *
 * It is sort of like an {@link InstrumentMaster}, except that:
 * <ol type="a">
     <li> It is only relevant during the JSON API conversions. We want clients to specify strings as
 *    instrument keys, NOT {@link InstrumentId}s, because instrument IDs are only relevant within
 *    the {@code rb} engine. </li>
 *   <li> It does not deal with {@link Symbol}s, just String, which can be more general </li>
 *   <li> It has no concept of history (e.g. what was the symbol for this {@code InstrumentId} last year?) </li>
 </ol>
 *
 * <p> At some point, we will need to agree with the caller about which instrument is which.
 * For example, if a client wants to use the betas that we generate, then there has to be some identifier that
 * we both know about.
 * When that happens, we will expand the input JSON API to also have a string {@code ->} {@link InstrumentId} mapping. </p>
 *
 * <p> Until that happens, the rb code will just generate its own consecutive {@code InstrumentId}s based on the string
 * tickers that appear as keys in the PriceMap part of the JSON API inputs. This will not have anything to do with
 * our default 'uuencoding-like' method of assigning instrument IDs. </p>
 *
 * @see JsonTickerMap
 * @see JsonTicker
 * @see InstrumentId
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
