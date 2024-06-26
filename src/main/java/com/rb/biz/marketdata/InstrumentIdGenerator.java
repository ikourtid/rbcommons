package com.rb.biz.marketdata;

import com.google.inject.Inject;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.types.HasLongRepresentation;

import static com.rb.biz.types.asset.InstrumentId.instrumentId;

/**
 * Same behavior as an {@link EncodedIdGenerator}, but specialized for an {@link InstrumentId},
 * whereas {@link EncodedIdGenerator} is more general and supports {@link HasLongRepresentation}.
 */
public class InstrumentIdGenerator {

  // This is one of the rare cases where an injected member has to be public.
  // An interface default method InstrumentMaster#getLatestValidSymbolOrBestGuess)
  // has to construct an InstrumentIdGenerator, since it cannot inject anything.
  @Inject public EncodedIdGenerator encodedIdGenerator;

  public InstrumentId generate(Symbol symbol) {
    return instrumentId(encodedIdGenerator.generateLongId(symbol));
  }

  public Symbol getBestGuessSymbol(InstrumentId instrumentId) {
    return encodedIdGenerator.getBestGuessSymbol(instrumentId);
  }

}
