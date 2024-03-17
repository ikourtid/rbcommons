package com.rb.biz.marketdata.instrumentmaster;

import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.AssetId;
import com.rb.biz.types.asset.AssetId.AssetIdVisitor;
import com.rb.biz.types.asset.CashId;
import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.biz.types.asset.InstrumentId;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Various utility methods for an {@link InstrumentMaster}
 */
public class InstrumentMasters {

  public static String displaySymbol(
      HasInstrumentId hasInstrumentId, InstrumentMaster instrumentMaster, LocalDate effectiveDate) {
    return displaySymbol(hasInstrumentId.getInstrumentId(), instrumentMaster, effectiveDate);
  }

  public static String displaySymbol(
      InstrumentId instrumentId, InstrumentMaster instrumentMaster, LocalDate effectiveDate,
      boolean printInstrumentIds) {
    Optional<Symbol> symbol = instrumentMaster.getSymbol(instrumentId, effectiveDate);
    return !symbol.isPresent() ? instrumentId.toString()
        : printInstrumentIds ? Symbol.getDisplaySymbol(symbol.get(), instrumentId)
        : symbol.get().toString();
  }

  public static String displaySymbol(
      InstrumentId instrumentId, InstrumentMaster instrumentMaster, LocalDate effectiveDate) {
    return displaySymbol(instrumentId, instrumentMaster, effectiveDate, false);
  }

  /**
   * Just like #displaySymbol , except that it can also return the string "cash"
   */
  public static String displaySymbolForAssetId(
      AssetId assetId, InstrumentMaster instrumentMaster, LocalDate effectiveDate, boolean printInstrumentIds) {
    return assetId.visit(new AssetIdVisitor<String>() {
      @Override
      public String visitInstrumentId(InstrumentId instrumentId) {
        Optional<Symbol> symbol = instrumentMaster.getSymbol(instrumentId, effectiveDate);
        return !symbol.isPresent() ? instrumentId.toString()
            : printInstrumentIds ? Symbol.getDisplaySymbol(symbol.get(), instrumentId)
            : symbol.get().toString();
      }

      @Override
      public String visitCash(CashId cash) {
        return "cash";
      }
    });
  }

}
