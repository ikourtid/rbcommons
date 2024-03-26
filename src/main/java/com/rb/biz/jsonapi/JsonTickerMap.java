package com.rb.biz.jsonapi;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;

import java.util.Optional;

/**
 * This is a {@link JsonTicker} {@code <-->} {@link InstrumentId} bidirectional map.
 *
 * <p> We aren't calling this SymbolMap because it doesn't use our {@link Symbol} class (which has restrictions on length,
 * characters allowed, etc.) </p>
 *
 * <p> It is similar to an {@link InstrumentMaster}, except that: </p>
 * <ol type="a">
 *   <li> It is only relevant during the JSON API conversions. We want clients to specify instrument keys as
 *        strings, NOT as numeric (long) instrument IDs, because instrument IDs are only relevant within the rb engine. </li>
 *   <li> It does not deal with {@link Symbol}, but just String, which can be more general. </li>
 *   <li> It has no concept of history (e.g. what was the symbol for this instrument ID last year?) </li>
 * </ol>
 *
 * <p> When tickers are used, we need to ensure there is no ambiguity about date. A ticker is unique for a date,
 * but not across dates.
 * For example, if a client wants to specify a risk model, the factor loadings for a stock must be keyed by
 * a ticker. </p>
 *
 * <p> The Rowboat engine JSON API is stateless, so it generates its own consecutive instrument IDs based on the string
 * tickers that appear as keys in the MarketInfo part of the JSON API inputs. </p>
 *
 * @see InstrumentId
 * @see JsonTicker
 */
public interface JsonTickerMap extends PrintsInstruments {

  InstrumentId getInstrumentIdOrThrow(JsonTicker ticker);

  Optional<InstrumentId> getOptionalInstrumentId(JsonTicker ticker);

  JsonTicker getJsonTickerOrThrow(InstrumentId instrumentId);

  Optional<JsonTicker> getOptionalJsonTicker(InstrumentId instrumentId);

}
