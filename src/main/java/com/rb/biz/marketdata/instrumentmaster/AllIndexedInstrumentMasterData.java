package com.rb.biz.marketdata.instrumentmaster;

import com.rb.nonbiz.collections.HasRoughIidCount;
import com.rb.nonbiz.text.Strings;

public class AllIndexedInstrumentMasterData implements HasRoughIidCount {

  private final AllUnindexedInstrumentMasterData allUnindexedInstrumentMasterData;
  private final InstrumentMasterIndexBySymbol instrumentMasterIndexBySymbol;

  public AllIndexedInstrumentMasterData(AllUnindexedInstrumentMasterData allUnindexedInstrumentMasterData,
                                        InstrumentMasterIndexBySymbol instrumentMasterIndexBySymbol) {
    this.allUnindexedInstrumentMasterData = allUnindexedInstrumentMasterData;
    this.instrumentMasterIndexBySymbol = instrumentMasterIndexBySymbol;
  }

  public static AllIndexedInstrumentMasterData allIndexedInstrumentMasterData(
      AllUnindexedInstrumentMasterData allUnindexedInstrumentMasterData,
      InstrumentMasterIndexBySymbol instrumentMasterIndexBySymbol) {
    // InstrumentMasterIndexForSymbol can be generated off of AllUnindexedInstrumentMasterData;
    // it's just more efficient to compute it once and store it (here).
    // We could, in theory, confirm inside this static constructor
    // that the two inputs agree with each other, but it's too much work.
    // This is one instance where we'll have to rely not on a runtime check on data correctness
    // but on the unit tests for whatever class generates InstrumentMasterIndexForSymbol
    return new AllIndexedInstrumentMasterData(allUnindexedInstrumentMasterData, instrumentMasterIndexBySymbol);
  }

  public AllUnindexedInstrumentMasterData getAllUnindexedInstrumentMasterData() {
    return allUnindexedInstrumentMasterData;
  }

  /**
   * This enables the reverse lookup: symbol + date {@code ->} instrument ID
   */
  public InstrumentMasterIndexBySymbol getInstrumentMasterIndexBySymbol() {
    return instrumentMasterIndexBySymbol;
  }

  @Override
  public int getRoughIidCount() {
    return allUnindexedInstrumentMasterData.getRoughIidCount();
  }

  @Override
  public String toString() {
    return Strings.format("[AIIMD %s %s AIIMD]", allUnindexedInstrumentMasterData, instrumentMasterIndexBySymbol);
  }

}
