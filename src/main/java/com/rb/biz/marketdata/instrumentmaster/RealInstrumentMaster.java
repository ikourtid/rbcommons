package com.rb.biz.marketdata.instrumentmaster;

import com.google.inject.Inject;
import com.rb.biz.guice.RBClock;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.NonContiguousRangeMap;

import java.time.LocalDate;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;

public class RealInstrumentMaster implements InstrumentMaster {

  @Inject AllInstrumentMasterDataSupplier allInstrumentMasterDataSupplier;
  @Inject RBClock rbClock;

  @Override
  public Optional<InstrumentId> getInstrumentId(Symbol symbol, LocalDate effectiveDate) {
    allInstrumentMasterDataSupplier.getAllMasterDataAsOf(rbClock.today());
    Optional<NonContiguousRangeMap<LocalDate, InstrumentId>> rangeMap =
        getAllMasterData().getInstrumentMasterIndexBySymbol().getSymbolToInstrumentIdByDate().getOptional(symbol);
    if (!rangeMap.isPresent()) {
      return Optional.empty();
    }
    return rangeMap.get().getOptional(effectiveDate);
  }

  @Override
  public Optional<Symbol> getSymbol(InstrumentId instrumentId, LocalDate effectiveDate) {
    Optional<SingleInstrumentMasterData> data = getAllMasterData()
        .getAllUnindexedInstrumentMasterData()
        .getMasterDataMap()
        .getOptional(instrumentId);
    if (!data.isPresent()) {
      return Optional.empty();
    }
    return transformOptional(
        data.get().getDateToData().getOptional(effectiveDate),
        spd -> spd.getSymbol());
  }

  @Override
  public Optional<Symbol> getLatestValidSymbol(InstrumentId instrumentId, LocalDate effectiveDate) {
    Optional<SingleInstrumentMasterData> data = getAllMasterData()
        .getAllUnindexedInstrumentMasterData()
        .getMasterDataMap()
        .getOptional(instrumentId);
    if (!data.isPresent()) {
      return Optional.empty();
    }
    return transformOptional(
        data.get().getDateToData().getOptionalWithHighestKeyBelow(effectiveDate),
        spd -> spd.getSymbol());
  }

  private AllIndexedInstrumentMasterData getAllMasterData() {
    return allInstrumentMasterDataSupplier.getAllMasterDataAsOf(rbClock.today());
  }

}
