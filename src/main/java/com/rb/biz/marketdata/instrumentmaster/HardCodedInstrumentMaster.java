package com.rb.biz.marketdata.instrumentmaster;

import com.google.common.annotations.VisibleForTesting;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidBiMap;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBSet;

import java.time.LocalDate;
import java.util.Optional;

import static com.rb.biz.types.Symbol.symbol;
import static com.rb.nonbiz.collections.IidBiMap.iidBiMap;
import static com.rb.nonbiz.collections.IidBiMaps.emptyIidBiMap;
import static com.rb.nonbiz.collections.IidBiMaps.iidBiMapOf;
import static com.rb.nonbiz.collections.IidBiMaps.singletonIidBiMap;
import static com.rb.nonbiz.collections.RBSet.newRBSet;

/**
 * This is useful in test when we want to create an {@link InstrumentMaster} with some mappings hardcoded,
 * e.g. STOCK_A1 {@code <->} "A1", etc.
 *
 * <p> HardCodedInstrumentMaster is used in tests, usually for a small number of stocks. It's not general enough
 * for production use because ticker / symbol is not necessarily a bi-map. For example, a ticker change would cause one
 * InstrumentId to map to two (or more) symbols, depending on the date. </p>
 */
public class HardCodedInstrumentMaster implements InstrumentMaster {

  private final IidBiMap<Symbol> hardCodedSymbolBiMap;

  public HardCodedInstrumentMaster() {
    this.hardCodedSymbolBiMap = emptyIidBiMap();
  }

  private HardCodedInstrumentMaster(IidBiMap<Symbol> hardCodedSymbolBiMap) {
    this.hardCodedSymbolBiMap = hardCodedSymbolBiMap;
  }

  public static HardCodedInstrumentMaster hardCodedInstrumentMaster(IidBiMap<Symbol> hardCodedSymbolBiMap) {
    return new HardCodedInstrumentMaster(hardCodedSymbolBiMap);
  }

  public static HardCodedInstrumentMaster hardCodedInstrumentMaster(IidMap<Symbol> hardCodedSymbolMap) {
    return new HardCodedInstrumentMaster(iidBiMap(hardCodedSymbolMap));
  }

  public static HardCodedInstrumentMaster singletonHardCodedInstrumentMaster(InstrumentId instrumentId, String symbol) {
    return hardCodedInstrumentMaster(singletonIidBiMap(instrumentId, symbol(symbol)));
  }

  public static HardCodedInstrumentMaster hardCodedInstrumentMaster(
      InstrumentId instrumentId1, String symbol1,
      InstrumentId instrumentId2, String symbol2) {
    return hardCodedInstrumentMaster(iidBiMapOf(
        instrumentId1, symbol(symbol1),
        instrumentId2, symbol(symbol2)));
  }

  public static HardCodedInstrumentMaster hardCodedInstrumentMaster(
      InstrumentId instrumentId1, String symbol1,
      InstrumentId instrumentId2, String symbol2,
      InstrumentId instrumentId3, String symbol3) {
    return hardCodedInstrumentMaster(iidBiMapOf(
        instrumentId1, symbol(symbol1),
        instrumentId2, symbol(symbol2),
        instrumentId3, symbol(symbol3)));
  }

  public static HardCodedInstrumentMaster hardCodedInstrumentMaster(
      InstrumentId instrumentId1, String symbol1,
      InstrumentId instrumentId2, String symbol2,
      InstrumentId instrumentId3, String symbol3,
      InstrumentId instrumentId4, String symbol4) {
    return hardCodedInstrumentMaster(iidBiMapOf(
        instrumentId1, symbol(symbol1),
        instrumentId2, symbol(symbol2),
        instrumentId3, symbol(symbol3),
        instrumentId4, symbol(symbol4)));
  }

  public static HardCodedInstrumentMaster hardCodedInstrumentMaster(
      InstrumentId instrumentId1, String symbol1,
      InstrumentId instrumentId2, String symbol2,
      InstrumentId instrumentId3, String symbol3,
      InstrumentId instrumentId4, String symbol4,
      InstrumentId instrumentId5, String symbol5) {
    return hardCodedInstrumentMaster(iidBiMapOf(
        instrumentId1, symbol(symbol1),
        instrumentId2, symbol(symbol2),
        instrumentId3, symbol(symbol3),
        instrumentId4, symbol(symbol4),
        instrumentId5, symbol(symbol5)));
  }

  public RBSet<InstrumentId> getAllInstrumentIdsAsRBSet() {
    return newRBSet(hardCodedSymbolBiMap.getInstrumentIdFromItem().values());
  }

  @Override
  public Optional<InstrumentId> getInstrumentId(Symbol symbol, LocalDate ignoredDate) {
    // use getOrThrow() to fail fast; this is test, not prod
    return Optional.of(hardCodedSymbolBiMap.getInstrumentIdFromItem().getOrThrow(symbol));
  }

  @Override
  public Optional<Symbol> getSymbol(InstrumentId instrumentId, LocalDate ignoredEffectiveDate) {
    // use getOrThrow() to fail fast; this is test, not prod
    return Optional.of(hardCodedSymbolBiMap.getItemFromInstrumentId().getOrThrow(instrumentId));
  }

  @Override
  public Optional<Symbol> getLatestValidSymbol(InstrumentId instrumentId, LocalDate ignoredEffectiveDate) {
    return getSymbol(instrumentId, ignoredEffectiveDate);
  }

  // Do not use this; it's here to help the test matcher.
  @VisibleForTesting
  IidBiMap<Symbol> getHardCodedSymbolBiMapDoNotUse() {
    return hardCodedSymbolBiMap;
  }

  /**
   * Ideally callers will only treat this class as a plain {@link InstrumentMaster}, but sometimes it's convenient
   * in tests to confirm that we loaded the right number of instruments.
   */
  public int size() {
    return hardCodedSymbolBiMap.size();
  }

}
