package com.rb.biz.marketdata.instrumentmaster;

import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidBiMap;
import com.rb.nonbiz.collections.IidMap;

import java.time.LocalDate;
import java.util.Optional;

import static com.rb.biz.types.Symbol.symbol;
import static com.rb.nonbiz.collections.IidBiMap.iidBiMap;
import static com.rb.nonbiz.collections.IidBiMaps.iidBiMapOf;

/**
 * Similar to {@link HardCodedInstrumentMaster}, except that it allows missing items, instead of throwing.
 */
public class HardCodedAllowingEmptyInstrumentMaster implements InstrumentMaster {

  private final IidBiMap<Symbol> hardCodedSymbolBiMap;

  private HardCodedAllowingEmptyInstrumentMaster(IidBiMap<Symbol> hardCodedSymbolBiMap) {
    this.hardCodedSymbolBiMap = hardCodedSymbolBiMap;
  }

  public static HardCodedAllowingEmptyInstrumentMaster hardCodedAllowingEmptyInstrumentMaster(
      IidMap<Symbol> hardCodedSymbolMap) {
    return new HardCodedAllowingEmptyInstrumentMaster(iidBiMap(hardCodedSymbolMap));
  }

  public static HardCodedAllowingEmptyInstrumentMaster hardCodedAllowingEmptyInstrumentMaster(
      InstrumentId instrumentId1, String symbol1,
      InstrumentId instrumentId2, String symbol2) {
    return new HardCodedAllowingEmptyInstrumentMaster(iidBiMapOf(
        instrumentId1, symbol(symbol1),
        instrumentId2, symbol(symbol2)));
  }

  @Override
  public Optional<InstrumentId> getInstrumentId(Symbol symbol, LocalDate ignoredDate) {
    return hardCodedSymbolBiMap.getInstrumentIdFromItem().getOptional(symbol);
  }

  @Override
  public Optional<Symbol> getSymbol(InstrumentId instrumentId, LocalDate ignoredEffectiveDate) {
    return hardCodedSymbolBiMap.getItemFromInstrumentId().getOptional(instrumentId);
  }

  @Override
  public Optional<Symbol> getLatestValidSymbol(InstrumentId instrumentId, LocalDate ignoredEffectiveDate) {
    return getSymbol(instrumentId, ignoredEffectiveDate);
  }

}
