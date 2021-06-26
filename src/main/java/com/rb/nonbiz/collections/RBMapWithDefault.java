package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;

import java.util.stream.Stream;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;

/**
 * Basically just an RBMap, but it also stores a default value, with the semantics that the default value applies
 * when a value for a given key (K) is missing.
 *
 * @see RBMap
 * @see RBCategoryMap
 * @see RBMapWithOptionalDefault
 */
public class RBMapWithDefault<K, V> {

  private final V defaultValue;
  private final RBMap<K, V> rawRBMap;

  private RBMapWithDefault(V defaultValue, RBMap<K, V> rawRBMap) {
    this.defaultValue = defaultValue;
    this.rawRBMap = rawRBMap;
  }

  public static <K, V> RBMapWithDefault<K, V> rbMapWithDefault(V defaultValue, RBMap<K, V> rawRBMap) {
    return new RBMapWithDefault<>(defaultValue, rawRBMap);
  }

  public static <K, V> RBMapWithDefault<K, V> emptyRBMapWithDefault(V defaultValue) {
    return new RBMapWithDefault<>(defaultValue, emptyRBMap());
  }

  public V getDefaultValue() {
    return defaultValue;
  }

  public RBMap<K, V> getRawRBMap() {
    return rawRBMap;
  }

  public V getOrDefault(K key) {
    return rawRBMap.getOptional(key).orElse(defaultValue);
  }

  public Stream<V> allValuesPlusDefaultAsStream() {
    return Stream.concat(
        Stream.of(defaultValue),
        rawRBMap.values().stream());
  }

  @Override
  public String toString() {
    return Strings.format("[RBMWD default= %s ; %s RBMWD]", defaultValue, rawRBMap);
  }

}
