package com.rb.biz.marketdata.instrumentmaster;

import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidBiMap;
import com.rb.nonbiz.collections.IidMap;

import java.time.LocalDate;
import java.util.Optional;

import static com.rb.biz.types.Symbol.symbol;
import static com.rb.nonbiz.collections.IidBiMap.iidBiMap;
import static com.rb.nonbiz.collections.IidBiMaps.emptyIidBiMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;

/**
 * This is useful in test when we want to create an InstrumentMaster with some mappings hardcoded,
 * e.g. STOCK_A1 {@code ->} "A1", etc.
 * It only works for cases where we don't want to do the reverse lookup of symbol {@code ->} instrument ID
 * (which we rarely want to do in tests), and where we are OK having the same symbol for a given instrument
 * on any date (i.e. no ticker changes supported, which we also don't really have a reason to do in a test).
 */
public class HardCodedInstrumentMaster implements InstrumentMaster {

  private final IidBiMap<Symbol> hardCodedSymbolBiMap;

  public HardCodedInstrumentMaster() {
    this.hardCodedSymbolBiMap = emptyIidBiMap();
  }

  private HardCodedInstrumentMaster(IidBiMap<Symbol> hardCodedSymbolBiMap) {
    this.hardCodedSymbolBiMap = hardCodedSymbolBiMap;
  }

  public static HardCodedInstrumentMaster hardCodedInstrumentMaster(IidMap<Symbol> hardCodedSymbolMap) {
    return new HardCodedInstrumentMaster(iidBiMap(hardCodedSymbolMap));
  }

  public static HardCodedInstrumentMaster singletonHardCodedInstrumentMaster(InstrumentId instrumentId, String symbol) {
    return hardCodedInstrumentMaster(singletonIidMap(instrumentId, symbol(symbol)));
  }

  public static HardCodedInstrumentMaster hardCodedInstrumentMaster(
      InstrumentId instrumentId1, String symbol1,
      InstrumentId instrumentId2, String symbol2) {
    return hardCodedInstrumentMaster(iidMapOf(
        instrumentId1, symbol(symbol1),
        instrumentId2, symbol(symbol2)));
  }

  public static HardCodedInstrumentMaster hardCodedInstrumentMaster(
      InstrumentId instrumentId1, String symbol1,
      InstrumentId instrumentId2, String symbol2,
      InstrumentId instrumentId3, String symbol3) {
    return hardCodedInstrumentMaster(iidMapOf(
        instrumentId1, symbol(symbol1),
        instrumentId2, symbol(symbol2),
        instrumentId3, symbol(symbol3)));
  }

  public static HardCodedInstrumentMaster hardCodedInstrumentMaster(
      InstrumentId instrumentId1, String symbol1,
      InstrumentId instrumentId2, String symbol2,
      InstrumentId instrumentId3, String symbol3,
      InstrumentId instrumentId4, String symbol4) {
    return hardCodedInstrumentMaster(iidMapOf(
        instrumentId1, symbol(symbol1),
        instrumentId2, symbol(symbol2),
        instrumentId3, symbol(symbol3),
        instrumentId4, symbol(symbol4)));
  }

  @Override
  public Optional<InstrumentId> getInstrumentId(Symbol symbol, LocalDate effectiveDate) {
    return Optional.of(hardCodedSymbolBiMap.getInstrumentIdFromItem().getOrThrow(symbol));
  }

  @Override
  public Optional<Symbol> getSymbol(InstrumentId instrumentId, LocalDate ignoredEffectiveDate) {
    return Optional.of(hardCodedSymbolBiMap.getItemFromInstrumentId().getOrThrow(instrumentId));
  }

  @Override
  public Optional<Symbol> getLatestValidSymbol(InstrumentId instrumentId, LocalDate ignoredEffectiveDate) {
    return Optional.of(hardCodedSymbolBiMap.getItemFromInstrumentId().getOrThrow(instrumentId));
  }

}
