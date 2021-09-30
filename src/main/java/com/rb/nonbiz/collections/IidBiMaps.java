package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;

import static com.rb.nonbiz.collections.IidBiMap.iidBiMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;

public class IidBiMaps {

  public static <V> IidBiMap<V> emptyIidBiMap() {
    return iidBiMap(emptyIidMap());
  }

  public static <V> IidBiMap<V> singletonIidBiMap(InstrumentId instrumentId, V item) {
    return iidBiMap(singletonIidMap(instrumentId, item));
  }

  public static <V> IidBiMap<V> iidBiMapOf(InstrumentId k1, V v1,
                                           InstrumentId k2, V v2) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(2);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    return iidBiMap(newIidMap(map));
  }

  public static <V> IidBiMap<V> iidBiMapOf(InstrumentId k1, V v1,
                                           InstrumentId k2, V v2,
                                           InstrumentId k3, V v3) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(3);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    return iidBiMap(newIidMap(map));
  }

  public static <V> IidBiMap<V> iidBiMapOf(InstrumentId k1, V v1,
                                           InstrumentId k2, V v2,
                                           InstrumentId k3, V v3,
                                           InstrumentId k4, V v4) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(4);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    return iidBiMap(newIidMap(map));
  }

  public static <V> IidBiMap<V> iidBiMapOf(InstrumentId k1, V v1,
                                           InstrumentId k2, V v2,
                                           InstrumentId k3, V v3,
                                           InstrumentId k4, V v4,
                                           InstrumentId k5, V v5) {
    MutableIidMap<V> map = newMutableIidMapWithExpectedSize(5);
    map.putAssumingAbsent(k1, v1);
    map.putAssumingAbsent(k2, v2);
    map.putAssumingAbsent(k3, v3);
    map.putAssumingAbsent(k4, v4);
    map.putAssumingAbsent(k5, v5);
    return iidBiMap(newIidMap(map));
  }

}
