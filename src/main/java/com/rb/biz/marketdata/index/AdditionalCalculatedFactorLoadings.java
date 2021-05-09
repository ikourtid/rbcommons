package com.rb.biz.marketdata.index;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.math.eigen.FactorLoadings;
import com.rb.nonbiz.math.eigen.Investable;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

/**
 * Factor loadings for all fake ETFs that track a fake stock index,
 * for a single day and eigendecomposition (risk model descriptor, actually).
 */
public class AdditionalCalculatedFactorLoadings<K extends Investable> {

  // K is currently InstrumentId, but we can't use IidMap, because in theory K can be another investable
  // (e.g. asset class).
  private final RBMap<K, FactorLoadings> rawMap;
  private final int sharedFactorLoadingsSize;

  private AdditionalCalculatedFactorLoadings(
      RBMap<K, FactorLoadings> rawMap,
      int sharedFactorLoadingsSize) {
    this.rawMap = rawMap;
    this.sharedFactorLoadingsSize = sharedFactorLoadingsSize;
  }

  public static <K extends Investable> AdditionalCalculatedFactorLoadings<K> additionalCalculatedFactorLoadings(
      RBMap<K, FactorLoadings> rawMap) {
    RBPreconditions.checkArgument(
        !rawMap.isEmpty(),
        "We don't allow empty AdditionalCalculatedFactorLoadings");
    int sharedFactorLoadingsSize = RBSimilarityPreconditions.checkAllSame(
        rawMap.values(),
        factorLoadings -> factorLoadings.size(),
        "All factor loadings must have the same size");
    return new AdditionalCalculatedFactorLoadings<>(rawMap, sharedFactorLoadingsSize);
  }

  public static AdditionalCalculatedFactorLoadings<InstrumentId> additionalCalculatedInstrumentIdFactorLoadings(
      RBMap<InstrumentId, FactorLoadings> rawMap) {
    return additionalCalculatedFactorLoadings(rawMap);
  }

  public RBMap<K, FactorLoadings> getRawMap() {
    return rawMap;
  }

  public int getSharedFactorLoadingsSize() {
    return sharedFactorLoadingsSize;
  }

  @Override
  public String toString() {
    return Strings.format("[ACFL %s %s ACFL]", sharedFactorLoadingsSize, rawMap);
  }

}
