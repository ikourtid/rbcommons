package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBEnumMapSimpleConstructors;
import com.rb.nonbiz.util.RBEnumMaps;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.google.common.collect.Maps.newEnumMap;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

/**
 * Similar to {@link java.util.EnumMap}. However, it is meant to be immutable.
 *
 * <p>Always prefer {@link RBEnumMap} to a java.util.EnumMap, especially on an interface, but even inside a method's body, when possible.
 * </p>
 *
 * <p> Guava ImmutableMap implements the Map interface, but its put() method will throw at runtime.
 * However, {@link RBEnumMap} intentionally has NO methods to modify it. That offers compile-time safety. </p>
 *
 * <p> Another advantage: #get on a regular {@link Map} returns null if the value is not there. We don't like nulls in the codebase,
 * plus that behavior is confusing to someone new to Java. </p>
 *
 * <p> Instead, RBEnumMap has: </p>
 * <ol>
 *   <li> #getOptional (which will return an Optional.empty() if there is no value for the specified key). </li>
 *   <li> #getOrThrow, which assumes the value is there, and returns {@link Optional}.of(...). </li>
 * </ol>
 *
 * @see RBEnumMaps for some handy static methods.
 * @see RBEnumMapSimpleConstructors for some helpful constructors.
 */
public class RBEnumMap<E extends Enum<E>, V> {

  private final EnumMap<E, V> rawMap;

  private RBEnumMap(EnumMap<E, V> rawMap) {
    // Creating a new enum map ensures immutability.
    this.rawMap = newEnumMap(rawMap);
  }

  // Create an RBEnumMap from a raw map.
  public static <E extends Enum<E>, V> RBEnumMap<E, V> newRBEnumMap(EnumMap rawMap) {
    return new RBEnumMap<E, V>(rawMap);
  }

  // Get a copy of the raw map.  This is not efficient but it preserves immutability.
  public EnumMap getCopyOfRawMap() {
    return rawMap.clone();
  }

  public int size() {
    return rawMap.size();
  }

  public boolean isEmpty() {
    return rawMap.isEmpty();
  }

  public boolean containsKey(E key) {
    return rawMap.containsKey(key);
  }

  public boolean containsValue(V value) {
    return rawMap.containsValue(value);
  }

  /**
   * Returns Optional.empty() if there is no value for the key,
   * otherwise Optional.of(value under key).
   */
  public Optional<V> getOptional(E key) {
    if (key == null) {
      throw new IllegalArgumentException("An RBEnumMap does not allow null keys");
    }
    return Optional.ofNullable(rawMap.get(key));
  }

  /**
   * Returns the value under the key (if present), otherwise the defaultValue.
   */
  public V getOrDefault(E key, V defaultValue) {
    if (key == null) {
      throw new IllegalArgumentException("An RBEnumMap does not allow null keys");
    }

    return rawMap.getOrDefault(key, defaultValue);
  }

  /**
   * Returns the value under the key (if present). Throws an exception otherwise.
   */
  public V getOrThrow(E key) {
    return getOrThrow(key, "Key %s does not exist in enumMap", key);
  }

  /**
   * Returns the value under the key (if present). Throws an exception otherwise, with the specified message.
   */
  public V getOrThrow(E key, String template, Object...args) {
    if (key == null) {
      throw new IllegalArgumentException("An RBEnumMap does not allow null keys");
    }
    V valueOrNull = rawMap.get(key);
    if (valueOrNull == null) {
      throw new IllegalArgumentException(smartFormat("%s : %s map keys are: %s",
          Strings.format(template, args), rawMap.size(), rawMap.keySet()));
    }
    return valueOrNull;
  }

  /**
   * Note that this returns items in enum declaration order.
   */
  public Set<E> keySet() {
    return rawMap.keySet();
  }

  /**
   * Note that this returns items in enum declaration order.
   */
  public Collection<V> values() {
    return rawMap.values();
  }

  /**
   * Note that this returns items in enum declaration order.
   */
  public Set<Map.Entry<E, V>> entrySet() {
    return rawMap.entrySet();
  }

  /**
   * This is a nice shorthand for iterating through an RBEnumMap's entries.
   * Note that this operates in enum key order.
   */
  public void forEachEntry(BiConsumer<E, V> biConsumer) {
    rawMap.entrySet().forEach(entry -> biConsumer.accept(entry.getKey(), entry.getValue()));
  }

  // IDE-generated
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RBEnumMap<?, ?> rbEnumMap = (RBEnumMap<?, ?>) o;

    return rawMap.equals(rbEnumMap.rawMap);

  }

  // IDE-generated
  @Override
  public int hashCode() {
    return rawMap.hashCode();
  }

  @Override
  public String toString() {
    return rawMap.toString();
  }

}
