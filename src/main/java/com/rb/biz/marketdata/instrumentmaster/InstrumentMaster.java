package com.rb.biz.marketdata.instrumentmaster;

import com.google.inject.ImplementedBy;
import com.rb.biz.marketdata.EncodedIdGenerator;
import com.rb.biz.marketdata.InstrumentIdGenerator;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.AssetId;
import com.rb.biz.types.asset.AssetId.AssetIdVisitor;
import com.rb.biz.types.asset.CashId;
import com.rb.biz.types.asset.InstrumentId;

import java.time.LocalDate;
import java.util.Optional;

import static com.rb.biz.types.Symbol.instrumentIdAsSymbol;
import static com.rb.biz.types.asset.CashId.cashSymbol;

public interface InstrumentMaster {

  Optional<InstrumentId> getInstrumentId(Symbol symbol, LocalDate effectiveDate);

  Optional<Symbol> getSymbol(InstrumentId instrumentId, LocalDate effectiveDate);

  Optional<Symbol> getLatestValidSymbol(InstrumentId instrumentId, LocalDate effectiveDate);

  /**
   * This provides the most reasonable default symbol for cases where an instrument does not exist on a given day.
   * First, it tries to find the most recent available symbol, going backwards in time.
   * Then, if that fails, it creates a fake symbol that's just a wrapper around the (numeric) instrument ID.
   */
  default Symbol getLatestValidSymbolOrInstrumentIdAsSymbol(InstrumentId instrumentId, LocalDate effectiveDate) {
    // We don't use .orElse() because it evaluates its argument, and in test it is possible that
    // instrumentId has a hardcoded mapping to some symbol AND that instrumentIdAsSymbol would throw here.
    Optional<Symbol> latestValidSymbol = getLatestValidSymbol(instrumentId, effectiveDate);
    return latestValidSymbol.orElseGet( () -> instrumentIdAsSymbol(instrumentId));
  }

  default Symbol getLatestValidSymbolOrBestGuess(InstrumentId instrumentId, LocalDate effectiveDate) {
    InstrumentIdGenerator instrumentIdGenerator = new InstrumentIdGenerator();
    instrumentIdGenerator.encodedIdGenerator = new EncodedIdGenerator();
    return getLatestValidSymbol(instrumentId, effectiveDate).orElse(instrumentIdGenerator.getBestGuessSymbol(instrumentId));
  }

  default Symbol getLatestValidSymbolOrAssetIdAsSymbol(AssetId assetId, LocalDate effectiveDate) {
    return assetId.visit(new AssetIdVisitor<Symbol>() {
      @Override
      public Symbol visitInstrumentId(InstrumentId instrumentId) {
        return getLatestValidSymbol(instrumentId, effectiveDate).orElse(instrumentIdAsSymbol(instrumentId));
      }

      @Override
      public Symbol visitCash(CashId cash) {
        return cashSymbol();
      }
    });
  }

}
