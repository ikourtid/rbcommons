package com.rb.biz.marketdata.instrumentmaster;

import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;

import java.time.LocalDate;
import java.util.Optional;

/**
 * A wrapper around an existing {@link InstrumentMaster} that overrides any date to be a fixed date.
 * It's sometimes useful in tests.
 */
public class OverridingToConstantDateInstrumentMaster implements InstrumentMaster {

  private final InstrumentMaster innerInstrumentMaster;
  private final LocalDate constantDateOverrideToUse;

  private OverridingToConstantDateInstrumentMaster(
      InstrumentMaster innerInstrumentMaster, LocalDate constantDateOverrideToUse) {
    this.innerInstrumentMaster = innerInstrumentMaster;
    this.constantDateOverrideToUse = constantDateOverrideToUse;
  }

  public static OverridingToConstantDateInstrumentMaster overridingToConstantDateInstrumentMaster(
      InstrumentMaster instrumentMaster, LocalDate constantDateOverrideToUse) {
    return new OverridingToConstantDateInstrumentMaster(instrumentMaster, constantDateOverrideToUse);
  }

  @Override
  public Optional<InstrumentId> getInstrumentId(Symbol symbol, LocalDate ignoredEffectiveDate) {
    return innerInstrumentMaster.getInstrumentId(symbol, constantDateOverrideToUse);
  }

  @Override
  public Optional<Symbol> getSymbol(InstrumentId instrumentId, LocalDate ignoredEffectiveDate) {
    return innerInstrumentMaster.getSymbol(instrumentId, constantDateOverrideToUse);
  }

  @Override
  public Optional<Symbol> getLatestValidSymbol(InstrumentId instrumentId, LocalDate ignoredEffectiveDate) {
    return innerInstrumentMaster.getLatestValidSymbol(instrumentId, constantDateOverrideToUse);
  }

}
