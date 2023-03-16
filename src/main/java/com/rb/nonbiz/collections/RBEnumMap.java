package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newEnumMap;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

/**
 * Similar to java.util.EnumMap. However, it is meant to be immutable.
 *
 * <p>Always prefer RBEnumMap to a java.util.Map, especially on an interface, but even inside a method's body, when possible.
 * </p>
 *
 * <p> Guava ImmutableMap implements the Map interface, but its put() method will throw at runtime.
 * However, RBMap intentionally has NO methods to modify it. That offers compile-time safety. </p>
 *
 * <p> Another advantage: #get on a regular Map returns null if the value is not there. We don't like nulls in the codebase,
 * plus that behavior is confusing to someone new to Java. </p>
 *
 * <p> Instead, RBEnumMap has: </p>
 * <ol>
 *   <li> #getOptional (which will return an Optional.empty() if there is no value for the specified key). </li>
 *   <li> #getOrThrow, which assumes the value is there, and returns {@link Optional}.of(...). </li>
 * </ol>
 *
 * @see RBMaps for some handy static methods.
 * @see MutableRBMap for a class that helps you initialize an RBMap.
 */
public class RBEnumMap<K extends Enum<K>, V> {

  private final EnumMap<K, V> rawMap;

  protected RBEnumMap(EnumMap<K, V> rawMap) {
    this.rawMap = newEnumMap(rawMap);
  }

  /**
   * Use this if you want the standard java.util.Map interface, e.g. to use it in some library that
   * can manipulate maps.
   *
   * <p> Be careful with this, because you are exposing the guts of the RBEnumMap, and you have no guarantee that the
   * caller won't modify the map, which would break the convention that RBEnumMap is immutable. </p>
   */
  public Map<K, V> asEnumMap() {
    return rawMap;
  }

  public int size() {
    return rawMap.size();
  }

  public boolean isEmpty() {
    return rawMap.isEmpty();
  }

  public boolean containsKey(K key) {
    return rawMap.containsKey(key);
  }

  public boolean containsValue(V value) {
    return rawMap.containsValue(value);
  }

  /**
   * Returns Optional.empty() if there is no value for the key,
   * otherwise Optional.of(value under key).
   */
  public Optional<V> getOptional(K key) {
    if (key == null) {
      throw new IllegalArgumentException("An RBEnumMap does not allow null keys");
    }
    return Optional.ofNullable(rawMap.get(key));
  }

  /**
   * Returns the value under the key (if present), otherwise the defaultValue.
   */
  public V getOrDefault(K key, V defaultValue) {
    if (key == null) {
      throw new IllegalArgumentException("An RBEnumMap does not allow null keys");
    }

    return rawMap.getOrDefault(key, defaultValue);
  }

  /**
   * Returns the value under the key (if present). Throws an exception otherwise.
   */
  public V getOrThrow(K key) {
    return getOrThrow(key, "Key %s does not exist in enumMap", key);
  }

  /**
   * Returns the value under the key (if present). Throws an exception otherwise, with the specified message.
   */
  public V getOrThrow(K key, String template, Object...args) {
    if (key == null) {
      throw new IllegalArgumentException("An RBMap does not allow null keys");
    }
    V valueOrNull = rawMap.get(key);
    if (valueOrNull == null) {
      throw new IllegalArgumentException(smartFormat("%s : %s map keys are: %s",
          Strings.format(template, args), rawMap.size(), rawMap.keySet()));
    }
    return valueOrNull;
  }

  public Set<K> keySet() {
    return rawMap.keySet();
  }

  public Collection<V> values() {
    return rawMap.values();
  }

  public Set<Map.Entry<K, V>> entrySet() {
    return rawMap.entrySet();
  }

  /**
   * This is a nice shorthand for iterating through an RBMap's entries.
   */
  public void forEachEntry(BiConsumer<K, V> biConsumer) {
    entrySet().forEach(entry -> biConsumer.accept(entry.getKey(), entry.getValue()));
  }

  /**
   * This is a nice shorthand for iterating through an RBMap's entries.
   */
  public void forEachSortedEntry(
      Comparator<Map.Entry<K, V>> comparator,
      BiConsumer<K, V> biConsumer) {
    entrySet()
        .stream()
        .sorted(comparator)
        .forEach(entry -> biConsumer.accept(entry.getKey(), entry.getValue()));
  }

  /**
   * Creates a new map whose keys are the same, and whose values are a transformation
   * of the values of the original map, when you don't care about they key when doing the transformation.
   */
  public <V1> RBMap<K, V1> transformValuesCopy(Function<V, V1> valueTransformer) {
    return filterKeysAndTransformValuesCopy(valueTransformer, v -> true);
  }

  /**
   * Creates a new map whose keys are the same, and whose values are a transformation
   * of the values of the original map, when you don't care about they key when doing the transformation.
   * ADDITIONALLY, it may filter keys so that the final map may be smaller than the original.
   */
  public <V1> RBMap<K, V1> filterKeysAndTransformValuesCopy(Function<V, V1> valueTransformer, Predicate<K> mustKeepKey) {
    return filterKeysAndTransformEntriesCopy( (ignoredKey, value) -> valueTransformer.apply(value), mustKeepKey);
  }

  /**
   * Creates a new map whose keys are the same, and whose values are a transformation
   * of the keys AND values of the original map - i.e. you do care about they key when doing the transformation.
   * Additionally, it may filter keys so that the final map may be smaller than the original.
   */
  public <V1> RBMap<K, V1> filterKeysAndTransformEntriesCopy(BiFunction<K, V, V1> valueTransformer, Predicate<K> mustKeepKey) {
    MutableRBMap<K, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (key, originalValue) -> {
      if (!mustKeepKey.test(key)) {
        return;
      }
      V1 transformedValue = valueTransformer.apply(key, originalValue);
      mutableMap.putAssumingAbsent(key, transformedValue);
    });
    return newRBMap(mutableMap);
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
    return asEnumMap().toString();
  }

}
