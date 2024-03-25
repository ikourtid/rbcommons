package com.rb.nonbiz.collections;

import com.rb.nonbiz.util.RBEnumMapSimpleConstructors;
import com.rb.nonbiz.util.RBEnumMaps;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.rb.nonbiz.collections.MutableRBEnumMap.newMutableRBEnumMapFromPlainRBMap;

/**
 * Similar to {@link EnumMap}. However, it is meant to be immutable. So this is more like an {@link RBMap},
 * but specialized for enum keys.
 *
 * <p>Always prefer {@link RBEnumMap} to {@link EnumMap}, especially on an interface,
 * but even inside a method's body, when possible. Here's why: </p>
 *
 * <p> Guava ImmutableMap implements the Map interface, but its put() method will throw at runtime.
 * However, {@link RBEnumMap} intentionally has NO methods to modify it. That offers compile-time safety. </p>
 *
 * <p> Another advantage: a regular {@link Map#get(Object)} returns null if the value is not there.
 * We don't like nulls in the codebase, plus that behavior is confusing to someone new to Java. </p>
 *
 * <p> Instead, {@link RBEnumMap} has: </p>
 * <ol>
 *   <li> {@link #getOptional(Enum)} (which will return an Optional.empty() if there is no value for the specified key). </li>
 *   <li> {@link #getOrThrow(Enum)}, which assumes the value is there, and returns {@link Optional}.of(...). </li>
 * </ol>
 *
 * @see RBEnumMaps
 * @see RBEnumMapSimpleConstructors
 */
public class RBEnumMap<E extends Enum<E>, V> {

  /**
   * In theory, this is mutable, but the assumption is that once it’s passed to the constructor,
   * the caller will know not to modify it. There is no way to enforce this with a 100% guarantee, but in practice we
   * never pass {@link MutableRBEnumMap} as an argument, or return it from a method. So the code - whatever creates an
   * empty map, adds items to it, and then returns an immutable map - is contained enough for this not to happen.
   */
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
