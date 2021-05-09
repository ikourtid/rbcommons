package com.rb.biz.marketdata.instrumentmaster;

import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.NonContiguousRangeMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;

public class InstrumentMasterIndexBySymbol {

  private final RBMap<Symbol, NonContiguousRangeMap<LocalDate, InstrumentId>> symbolToInstrumentIdByDate;

  private InstrumentMasterIndexBySymbol(
      RBMap<Symbol, NonContiguousRangeMap<LocalDate, InstrumentId>> symbolToInstrumentIdByDate) {
    this.symbolToInstrumentIdByDate = symbolToInstrumentIdByDate;
  }

  public static InstrumentMasterIndexBySymbol instrumentMasterIndexBySymbol(
      RBMap<Symbol, NonContiguousRangeMap<LocalDate, InstrumentId>> symbolToInstrumentIdByDate) {
    RBPreconditions.checkArgument(
        !symbolToInstrumentIdByDate.isEmpty(),
        "You probably don't want to have an empty instrument master symbol -> instruments index");
    return new InstrumentMasterIndexBySymbol(symbolToInstrumentIdByDate);
  }

  public RBMap<Symbol, NonContiguousRangeMap<LocalDate, InstrumentId>> getSymbolToInstrumentIdByDate() {
    return symbolToInstrumentIdByDate;
  }

  @Override
  public String toString() {
    return Strings.format("[IMIFS %s IMIFS]", symbolToInstrumentIdByDate);
  }

}
