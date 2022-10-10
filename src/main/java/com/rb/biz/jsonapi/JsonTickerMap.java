package com.rb.biz.jsonapi;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;

import java.util.Optional;

/**
 * This is a ticker {@code <-->} InstrumentId bidirectional map.
 *
 * <p> We aren't calling this SymbolMap because it doesn't use our Symbol class (which has restrictions on length,
 * characters allowed, etc.) </p>
 *
 * <p> It is sort of like an InstrumentMaster, except that: </p>
 * a) it is only relevant during the JSON API conversions. We want clients to specify strings as
 *    instrument keys, NOT instrument IDs, because instrument IDs are only relevant within the rb engine.
 * b) it does not deal with Symbols, just String, which can be more general
 * c) it has no concept of history (e.g. what was the symbol for this instrument ID last year?)
 *
 * <p> At some point, we will need to agree with the caller about which instrument is which.
 * For example, if a client wants to use the betas that we generate, then there has to be some identifier that
 * we both know about.
 * When that happens, we will expand the input JSON API to also have a string {@code ->} instrument ID mapping. </p>
 *
 * <p> Until that happens, the rb code will just generate its own consecutive instrument IDs based on the string
 * tickers that appear as keys in the PriceMap part of the JSON API inputs. This will not have anything to do with
 * our default 'uuencoding-like' method of assigning instrument IDs. </p>
 */
public interface JsonTickerMap extends PrintsInstruments {

  InstrumentId getInstrumentIdOrThrow(JsonTicker ticker);

  Optional<InstrumentId> getOptionalInstrumentId(JsonTicker ticker);

  JsonTicker getJsonTickerOrThrow(InstrumentId instrumentId);

  Optional<JsonTicker> getOptionalJsonTicker(InstrumentId instrumentId);

}
