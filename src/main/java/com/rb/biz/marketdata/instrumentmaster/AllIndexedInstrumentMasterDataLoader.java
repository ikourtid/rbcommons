package com.rb.biz.marketdata.instrumentmaster;

import com.google.inject.Inject;

import java.time.LocalDate;

import static com.rb.biz.marketdata.instrumentmaster.AllIndexedInstrumentMasterData.allIndexedInstrumentMasterData;

public class AllIndexedInstrumentMasterDataLoader {

  @Inject AllUnindexedInstrumentMasterDataLoader allUnindexedInstrumentMasterDataLoader;
  @Inject InstrumentMasterIndexerBySymbol instrumentMasterIndexerBySymbol;

  public AllIndexedInstrumentMasterData load(LocalDate date) {
    AllUnindexedInstrumentMasterData allUnindexedInstrumentMasterData = allUnindexedInstrumentMasterDataLoader.load(date);
    return allIndexedInstrumentMasterData(
        allUnindexedInstrumentMasterData,
        instrumentMasterIndexerBySymbol.generateIndex(allUnindexedInstrumentMasterData));
  }

}
