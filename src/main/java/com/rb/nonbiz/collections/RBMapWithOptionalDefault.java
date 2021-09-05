package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;

/**
 * Basically just an RBMap, but it also stores an optional default value, with the semantics that the default value applies
 * when a value for a given key (K) is missing AND the default value is present. Otherwise, a missing key in the map
 * is supposed to get some default behavior based on the semantics of its caller.
 *
 * @see RBMap
 * @see RBCategoryMap
 * @see RBMapWithDefault
 */
public class RBMapWithOptionalDefault<K, V> {

  private final Optional<V> optionalDefaultValue;
  private final RBMap<K, V> rawRBMap;

  private RBMapWithOptionalDefault(Optional<V> optionalDefaultValue, RBMap<K, V> rawRBMap) {
    this.optionalDefaultValue = optionalDefaultValue;
    this.rawRBMap = rawRBMap;
  }

  public static <K, V> RBMapWithOptionalDefault<K, V> rbMapWithOptionalDefault(
      Optional<V> defaultValue, RBMap<K, V> rawRBMap) {
    return new RBMapWithOptionalDefault<>(defaultValue, rawRBMap);
  }

  public static <K, V> RBMapWithOptionalDefault<K, V> rbMapWithOptionalDefaultPresent(
      V defaultValue, RBMap<K, V> rawRBMap) {
    return rbMapWithOptionalDefault(Optional.of(defaultValue), rawRBMap);
  }

  public static <K, V> RBMapWithOptionalDefault<K, V> rbMapWithOptionalDefaultMissing(RBMap<K, V> rawRBMap) {
    return rbMapWithOptionalDefault(Optional.empty(), rawRBMap);
  }

  public static <K, V> RBMapWithOptionalDefault<K, V> emptyRBMapWithOptionalDefault(Optional<V> defaultValue) {
    return new RBMapWithOptionalDefault<>(defaultValue, emptyRBMap());
  }

  public static <K, V> RBMapWithOptionalDefault<K, V> emptyRBMapWithOptionalDefaultPresent(V defaultValue) {
    return emptyRBMapWithOptionalDefault(Optional.of(defaultValue));
  }

  public static <K, V> RBMapWithOptionalDefault<K, V> emptyRBMapWithOptionalDefaultMissing() {
    return emptyRBMapWithOptionalDefault(Optional.empty());
  }

  public Optional<V> getOptionalDefaultValue() {
    return optionalDefaultValue;
  }

  public RBMap<K, V> getRawRBMap() {
    return rawRBMap;
  }

  public boolean hasNoDefaultValueOrOverrides() {
    return !optionalDefaultValue.isPresent() && rawRBMap.isEmpty();
  }

  /**
   * If the key is present in the map, it returns the value as a present optional.
   * Otherwise, it returns the default value, which again is an optional.
   */
  public Optional<V> getOrDefault(K key) {
    Optional<V> valueInMap = rawRBMap.getOptional(key);
    return valueInMap.isPresent() ? valueInMap : optionalDefaultValue;
  }

  @Override
  public String toString() {
    return Strings.format("[RBWOD default= %s ; %s RBMOD]", optionalDefaultValue, rawRBMap);
  }

}
