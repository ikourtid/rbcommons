package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;

import static com.rb.nonbiz.collections.IidBiMap.iidBiMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;

public class IidBiMaps {

  public static <V> IidBiMap<V> emptyIidBiMap() {
    return iidBiMap(emptyIidMap());
  }

  public static <V> IidBiMap<V> singletonIidBiMap(InstrumentId instrumentId, V item) {
    return iidBiMap(singletonIidMap(instrumentId, item));
  }

}
