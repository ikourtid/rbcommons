package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.util.RBPreconditions;

public class MultiItemQualityOfReturns<T extends Investable> {

  private final RBMap<T, SingleItemQualityOfReturns<T>> qualityOfReturnsMap;

  private MultiItemQualityOfReturns(RBMap<T, SingleItemQualityOfReturns<T>> qualityOfReturnsMap) {
    this.qualityOfReturnsMap = qualityOfReturnsMap;
  }

  public static <T extends Investable> MultiItemQualityOfReturns<T> multiItemQualityOfReturns(
      RBMap<T, SingleItemQualityOfReturns<T>> qualityOfReturnsMap) {
    RBPreconditions.checkArgument(!qualityOfReturnsMap.isEmpty());
    RBPreconditions.checkArgument(
        qualityOfReturnsMap.allEntriesMatch( (key, value) -> key.equals(value.getKey())));
    return new MultiItemQualityOfReturns<>(qualityOfReturnsMap);
  }

  public RBMap<T, SingleItemQualityOfReturns<T>> getQualityOfReturnsMap() {
    return qualityOfReturnsMap;
  }

  @Override
  public String toString() {
    return qualityOfReturnsMap.toString();
  }

}
