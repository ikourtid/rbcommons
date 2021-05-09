package com.rb.nonbiz.util;

import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.biz.types.asset.HasInvestable;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.math.eigen.Investable;

import java.util.function.Function;

public class RBMapPreconditions {

  /**
   * Throws if the instrument ID doesn't match between the key and the value (which implements HasInstrumentId).
   *
   * E.g. in a map of InstrumentId to BuyOrder (where BuyOrder implements HasInstrumentId),
   * the instrumentId in the key must match the one that the value has.
   */
  public static <T extends HasInstrumentId> IidMap<T> checkMatchingMapInstrumentIds(IidMap<T> map) {
    return checkIidMapKeysMatchValues(map, v -> v.getInstrumentId());
  }

  public static <K extends Investable, T extends HasInvestable<K>>
  RBMap<K, T> checkMatchingMapInvestables(RBMap<K, T> map) {
    return checkMapKeysMatchValues(map, v -> v.getInvestable());
  }

  public static <K, V> RBMap<K, V> checkMapKeysMatchValues(
      RBMap<K, V> map, Function<V, K> keyFromValueExtractor) {
    map.forEachEntry( (key, value) -> {
      K inValue = keyFromValueExtractor.apply(value);
      RBPreconditions.checkArgument(
          key.equals(inValue),
          "key %s != %s key in value",
          key, inValue);
    });
    return map;
  }

  public static <V> IidMap<V> checkIidMapKeysMatchValues(
      IidMap<V> map, Function<V, InstrumentId> instrumentIdFromValueExtractor) {
    map.forEach( (longId, value) -> {
      long idInValue = instrumentIdFromValueExtractor.apply(value).asLong();
      RBPreconditions.checkArgument(
          longId == idInValue,
          "key %s != %s key in value",
          longId, idInValue);
    });
    return map;
  }

}
