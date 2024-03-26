package com.rb.biz.marketdata.instrumentmaster;

import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;

import java.time.LocalDate;
import java.util.Optional;

/**
 * A simple implementation of {@link InstrumentMaster} for cases when we want to say that we don't have an
 * instrument master, but don't want to pass null because our codebase (as do most modern Java codebases)
 * avoids nulls.
 *
 * <p> See {@link PrintsInstruments} for a description of where this is most commonly used. </p>
 */
public class NullInstrumentMaster implements InstrumentMaster {

  public static final NullInstrumentMaster NULL_INSTRUMENT_MASTER = new NullInstrumentMaster();

  private NullInstrumentMaster() {}

  @Override
  public Optional<InstrumentId> getInstrumentId(Symbol symbol, LocalDate effectiveDate) {
    return Optional.empty();
  }

  @Override
  public Optional<Symbol> getSymbol(InstrumentId instrumentId, LocalDate date) {
    return Optional.empty();
  }

  @Override
  public Optional<Symbol> getLatestValidSymbol(InstrumentId instrumentId, LocalDate effectiveDate) {
    return Optional.empty();
  }

}
