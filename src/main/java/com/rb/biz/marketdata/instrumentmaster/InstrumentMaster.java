package com.rb.biz.marketdata.instrumentmaster;

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

/**
 * Answers two questions: for a {@link Symbol} / date combination, what is the (numeric) {@link InstrumentId}?
 * What was the valid {@link Symbol} for an {@link InstrumentId} on a given date?
 */
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

  /**
   * WARNING - only use this if your other code uses the {@link EncodedIdGenerator} as a mechanism for generating
   * intuitive numeric {@link InstrumentId}.
   */
  default Symbol getLatestValidSymbolOrBestGuess(InstrumentId instrumentId, LocalDate effectiveDate) {
    InstrumentIdGenerator instrumentIdGenerator = new InstrumentIdGenerator();
    instrumentIdGenerator.encodedIdGenerator = new EncodedIdGenerator();
    return getLatestValidSymbol(instrumentId, effectiveDate)
        .orElseGet( () -> instrumentIdGenerator.getBestGuessSymbol(instrumentId));
  }

  default Symbol getLatestValidSymbolOrAssetIdAsSymbol(AssetId assetId, LocalDate effectiveDate) {
    return assetId.visit(new AssetIdVisitor<Symbol>() {
      @Override
      public Symbol visitInstrumentId(InstrumentId instrumentId) {
        return getLatestValidSymbol(instrumentId, effectiveDate)
            .orElseGet( () -> instrumentIdAsSymbol(instrumentId));
      }

      @Override
      public Symbol visitCash(CashId cash) {
        return cashSymbol();
      }
    });
  }

}
