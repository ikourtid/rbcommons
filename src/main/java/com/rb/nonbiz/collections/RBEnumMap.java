package com.rb.nonbiz.collections;

import com.rb.nonbiz.util.RBEnumMapSimpleConstructors;
import com.rb.nonbiz.util.RBEnumMaps;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.rb.nonbiz.collections.MutableRBEnumMap.newMutableRBEnumMap;
import static com.rb.nonbiz.collections.MutableRBEnumMap.newMutableRBEnumMapFromPlainRBMap;

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

  private final MutableRBEnumMap<E, V> mutableRBEnumMap;

  private RBEnumMap(MutableRBEnumMap<E, V> mutableRBEnumMap) {
    // Creating a new enum map ensures immutability.
    this.mutableRBEnumMap = mutableRBEnumMap;
  }

  public static <E extends Enum<E>, V> RBEnumMap<E, V> newRBEnumMap(MutableRBEnumMap<E, V> mutableMap) {
    return new RBEnumMap<E, V>(mutableMap);
  }

  public static <E extends Enum<E>, V> RBEnumMap<E, V> newRBEnumMap(Class<E> enumClass, RBMap<E, V> rbMap) {
    return newRBEnumMap(newMutableRBEnumMapFromPlainRBMap(enumClass, rbMap));
  }

  public static <E extends Enum<E>, V> RBEnumMap<E, V> emptyRBEnumMap(Class<E> enumClass) {
    return new RBEnumMap<E, V>(newMutableRBEnumMap(enumClass));
  }

  public Class<E> getEnumClass() {
    return mutableRBEnumMap.getEnumClass();
  }

  public int size() {
    return mutableRBEnumMap.size();
  }

  public boolean isEmpty() {
    return mutableRBEnumMap.isEmpty();
  }

  public boolean containsKey(E key) {
    return mutableRBEnumMap.containsKey(key);
  }

  public boolean containsValue(V value) {
    return mutableRBEnumMap.containsValue(value);
  }

  /**
   * Returns Optional.empty() if there is no value for the key,
   * otherwise Optional.of(value under key).
   */
  public Optional<V> getOptional(E key) {
    return mutableRBEnumMap.getOptional(key);
  }

  /**
   * Returns the value under the key (if present), otherwise the defaultValue.
   */
  public V getOrDefault(E key, V defaultValue) {
    return mutableRBEnumMap.getOrDefault(key, defaultValue);
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
    return mutableRBEnumMap.getOrThrow(key, template, args);
  }

  /**
   * Note that this returns items in enum declaration order.
   */
  public Set<E> keySet() {
    return mutableRBEnumMap.keySet();
  }

  /**
   * Note that this returns items in enum declaration order.
   */
  public Collection<V> values() {
    return mutableRBEnumMap.values();
  }

  /**
   * Note that this returns items in enum declaration order.
   */
  public Set<Map.Entry<E, V>> entrySet() {
    return mutableRBEnumMap.entrySet();
  }

  /**
   * This is a nice shorthand for iterating through an RBEnumMap's entries.
   * Note that this operates in enum key order.
   */
  public void forEachEntryInKeyOrder(BiConsumer<E, V> biConsumer) {
    mutableRBEnumMap.entrySet().forEach(entry -> biConsumer.accept(entry.getKey(), entry.getValue()));
  }

  // IDE-generated
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RBEnumMap<?, ?> rbEnumMap = (RBEnumMap<?, ?>) o;

    return mutableRBEnumMap.equals(rbEnumMap.mutableRBEnumMap);

  }

  // IDE-generated
  @Override
  public int hashCode() {
    return mutableRBEnumMap.hashCode();
  }

  @Override
  public String toString() {
    return mutableRBEnumMap.toString();
  }

}
