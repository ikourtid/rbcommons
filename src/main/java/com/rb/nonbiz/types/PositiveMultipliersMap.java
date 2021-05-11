package com.rb.nonbiz.types;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.collections.DoubleMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;

/**
 * A map of items {@code ->} positive multiplier.
 *
 * @see DoubleMap
 */
public class PositiveMultipliersMap<K> {

  private final RBMap<K, PositiveMultiplier> rawMap;

  private PositiveMultipliersMap(RBMap<K, PositiveMultiplier> rawMap) {
    this.rawMap = rawMap;
  }

  public static <K> PositiveMultipliersMap<K> positiveMultipliersMap(RBMap<K, PositiveMultiplier> rawMap) {
    return new PositiveMultipliersMap<>(rawMap);
  }

  public static <K> PositiveMultipliersMap<K> singletonPositiveMultipliersMap(K key, PositiveMultiplier positiveMultiplier) {
    return new PositiveMultipliersMap<>(singletonRBMap(key, positiveMultiplier));
  }

  @VisibleForTesting
  public static <K> PositiveMultipliersMap<K> emptyPositiveMultipliersMap() {
    return positiveMultipliersMap(emptyRBMap());
  }

  public RBMap<K, PositiveMultiplier> getRawMap() {
    return rawMap;
  }

  public int size() {
    return rawMap.size();
  }

  public boolean isEmpty() {
    return rawMap.isEmpty();
  }

  @Override
  public String toString() {
    return Strings.format("[PMM %s PMM]", rawMap.toString());
  }

}
