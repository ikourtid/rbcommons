package com.rb.nonbiz.text;

import com.rb.nonbiz.collections.MutableRBMap;
import com.rb.nonbiz.collections.Pair;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.Pair.pair;

/**
 * Use this to build a RBMapOfHasUniqueId, which is immutable.
 *
 * This indirection is here to hide the internal implementation details of RBMapOfHasUniqueId
 * which stores a {@code RBMap<UniqueId<K>, Pair<K, V>>}.
 */
public class MutableRBMapOfHasUniqueId<K extends HasUniqueId<K>, V> {

  private final MutableRBMap<UniqueId<K>, Pair<K, V>> rawMap;

  private MutableRBMapOfHasUniqueId(MutableRBMap<UniqueId<K>, Pair<K, V>> rawMap) {
    this.rawMap = rawMap;
  }

  public static <K extends HasUniqueId<K>, V> MutableRBMapOfHasUniqueId<K, V> newMutableRBMapOfHasUniqueId() {
    return new MutableRBMapOfHasUniqueId<>(newMutableRBMap());
  }

  public static <K extends HasUniqueId<K>, V> MutableRBMapOfHasUniqueId<K, V> newMutableRBMapOfHasUniqueIdWithExpectedSize(int size) {
    return new MutableRBMapOfHasUniqueId<>(newMutableRBMapWithExpectedSize(size));
  }

  public void putAssumingAbsent(K key, V value) {
    rawMap.putAssumingAbsent(key.getUniqueId(), pair(key, value));
  }

  // This is intentionally package-private, because we really only want the RBMapOfHasUniqueId constructor
  // to be able to access this.
  MutableRBMap<UniqueId<K>, Pair<K, V>> getRawMap() {
    return rawMap;
  }

}
