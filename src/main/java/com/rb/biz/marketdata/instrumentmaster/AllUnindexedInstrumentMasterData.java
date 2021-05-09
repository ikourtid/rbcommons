package com.rb.biz.marketdata.instrumentmaster;

import com.rb.nonbiz.collections.HasRoughIidCount;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBMapPreconditions;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * 'indexed' is when we build indexes for e.g. reverse lookup (symbol {@code ->} instrument ID).
 * This is the raw data - without the indexes.
 */
public class AllUnindexedInstrumentMasterData implements HasRoughIidCount {

  private final IidMap<SingleInstrumentMasterData> masterDataMap;

  private AllUnindexedInstrumentMasterData(IidMap<SingleInstrumentMasterData> masterDataMap) {
    this.masterDataMap = masterDataMap;
  }

  public static AllUnindexedInstrumentMasterData allUnindexedInstrumentMasterData(
      IidMap<SingleInstrumentMasterData> masterDataMap) {
    RBMapPreconditions.checkMatchingMapInstrumentIds(masterDataMap);
    RBPreconditions.checkArgument(
        !masterDataMap.isEmpty(),
        "Master data map cannot be empty; that would mean there are no instruments in the system");
    return new AllUnindexedInstrumentMasterData(masterDataMap);
  }

  public IidMap<SingleInstrumentMasterData> getMasterDataMap() {
    return masterDataMap;
  }

  @Override
  public int getRoughIidCount() {
    return masterDataMap.size();
  }

  @Override
  public String toString() {
    return Strings.format("[AUIMD %s AUIMD]", masterDataMap);
  }

}
