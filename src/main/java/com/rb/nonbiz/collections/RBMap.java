package com.rb.nonbiz.collections;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static java.util.Comparator.comparing;

/**
 * Similar to java.util.Map. However, it is meant to be immutable.
 *
 * Always prefer RBMap to a java.util.Map, especially on an interface, but even inside a method's body, when possible.
 *
 * Guava ImmutableMap implements the Map interface, but its put() method will throw at runtime.
 * However, RBMap intentionally has NO methods to modify it. That offers compile-time safety.
 *
 * Another advantage: #get on a regular Map returns null if the value is not there. We don't like nulls in the codebase,
 * plus that behavior is confusing to someone new to Java.
 * Instead, RBMap has:
 * #getOptional (which will return an Optional.empty() if there is no value for the specified key),
 * #getOrThrow, which assumes the value is there, and returns Optional.of(...)
 *
 * @see RBMaps for some handy static methods.
 * @see MutableRBMap for a class that helps you initialize an RBMap.
 */
public class RBMap<K, V> {

  private final Map<K, V> rawMap;

  protected RBMap(Map<K, V> rawMap) {
    this.rawMap = Collections.unmodifiableMap(rawMap);
  }

  /**
   * Use this if you want the standard java.util.Map interface, e.g. to use it in some library that
   * can manipulate maps.
   *
   * Be careful with this, because you are exposing the guts of the RBMap, and you have no guarantee that the
   * caller won't modify the map, which would break the convention that RBMap is immutable.
   */
  public Map<K, V> asMap() {
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

  public boolean containsOnlyKey(K key) {
    return rawMap.containsKey(key)
        && rawMap.size() == 1;
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
      throw new IllegalArgumentException("An RBMap does not allow null keys");
    }
    return Optional.ofNullable(rawMap.get(key));
  }

  /**
   * Returns the value under the key (if present), otherwise the defaultValue.
   */
  public V getOrDefault(K key, V defaultValue) {
    if (key == null) {
      throw new IllegalArgumentException("An RBMap does not allow null keys");
    }

    return rawMap.getOrDefault(key, defaultValue);
  }

  /**
   * Returns the value under the key (if present), otherwise the defaultValue.
   */
  public V getOrDefault(K key, Supplier<V> defaultValueSupplier) {
    if (key == null) {
      throw new IllegalArgumentException("An RBMap does not allow null keys");
    }
    V value = rawMap.get(key);
    return value == null ? defaultValueSupplier.get() : value;
  }

  /**
   * Returns the value under the key (if present). Throws an exception otherwise.
   */
  public V getOrThrow(K key) {
    return getOrThrow(key, "Key %s does not exist in map", key);
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
      throw new IllegalArgumentException(Strings.format("%s : %s map keys are: %s",
          Strings.format(template, args), rawMap.size(), rawMap.keySet()));
    }
    return valueOrNull;
  }

  /**
   * Returns the value under the key (if present). Throws an exception otherwise.
   * Allows values to be null.
   */
  public V getOrThrowAllowingNull(K key) {
    return getOrThrowAllowingNull(key, "Key %s does not exist in map", key);
  }

  /**
   * Returns the value under the key (if present). Throws an exception otherwise, with the specified message.
   */
  public V getOrThrowAllowingNull(K key, String template, Object...args) {
    if (key == null) {
      throw new IllegalArgumentException("An RBMap does not allow null keys");
    }
    if (!rawMap.containsKey(key)) {
      throw new IllegalArgumentException(Strings.format("%s : %s map keys do not contain %s ; keys are %s",
          Strings.format(template, args), rawMap.size(), key, rawMap.keySet()));
    }
    return rawMap.get(key);
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

  public boolean allEntriesMatch(BiPredicate<K, V> biPredicate) {
    return entrySet()
        .stream()
        .allMatch(entry -> biPredicate.test(entry.getKey(), entry.getValue()));
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

  public RBMap<K, V> filterKeys(Predicate<K> mustKeepKey) {
    MutableRBMap<K, V> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (key, value) -> {
      if (mustKeepKey.test(key)) {
        mutableMap.putAssumingAbsent(key, value);
      }
    });
    return newRBMap(mutableMap);
  }

  public RBMap<K, V> filterValues(Predicate<V> mustKeepValue) {
    MutableRBMap<K, V> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (key, value) -> {
      if (mustKeepValue.test(value)) {
        mutableMap.putAssumingAbsent(key, value);
      }
    });
    return newRBMap(mutableMap);
  }

  public RBMap<K, V> filterEntries(BiPredicate<K, V> mustKeepEntry) {
    MutableRBMap<K, V> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (key, value) -> {
      if (mustKeepEntry.test(key, value)) {
        mutableMap.putAssumingAbsent(key, value);
      }
    });
    return newRBMap(mutableMap);
  }

  /**
   * Just like #filterKeysAndTransformValuesCopy, except that we filter on values instead of keys.
   */
  public <V1> RBMap<K, V1> filterValuesAndTransformValuesCopy(Function<V, V1> valueTransformer, Predicate<V> mustKeepValue) {
    MutableRBMap<K, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (key, originalValue) -> {
      if (!mustKeepValue.test(originalValue)) {
        return;
      }
      V1 transformedValue = valueTransformer.apply(originalValue);
      mutableMap.putAssumingAbsent(key, transformedValue);
    });
    return newRBMap(mutableMap);
  }

  /**
   * Creates a new map whose keys are the same, and whose values are a transformation
   * of the values of the original map. Additionally, it may filter on the transformed values.
   */
  public <V1> RBMap<K, V1> transformAndFilterValuesCopy(Function<V, V1> valueTransformer, Predicate<V1> mustKeepTransformedValue) {
    MutableRBMap<K, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (instrumentId, originalValue) -> {
      V1 transformedValue = valueTransformer.apply(originalValue);
      if (!mustKeepTransformedValue.test(transformedValue)) {
        return;
      }
      mutableMap.putAssumingAbsent(instrumentId, transformedValue);
    });
    return newRBMap(mutableMap);
  }

  public <V1> RBMap<K, V1> orderedTransformValuesCopy(Function<V, V1> valueTransformer, Comparator<K> keyComparator) {
    MutableRBMap<K, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachSortedEntry(
        comparing(entry -> entry.getKey(), keyComparator),
        (key, originalValue) ->
            mutableMap.putAssumingAbsent(key, valueTransformer.apply(originalValue)));
    return newRBMap(mutableMap);
  }

  /**
   * Transforms a map's values while keeping the same keys, but the transformations happen in a random order.
   * This only matters if the transformer function has any side effects;
   */
  public <V1> RBMap<K, V1> randomlyOrderedTransformValuesCopy(Function<V, V1> valueTransformer, Random random) {
    List<Entry<K, V>> entriesList = newArrayList(entrySet());

    // The performance isn't great here, because we need to have a list (not arrays or iterators), but I guess we have
    // to. Collections.shuffle implements some Knuth algorithm so it has to be good.
    Collections.shuffle(entriesList, random);
    MutableRBMap<K, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    entriesList.forEach(entry ->
        mutableMap.putAssumingAbsent(entry.getKey(), valueTransformer.apply(entry.getValue())));
    return newRBMap(mutableMap);
  }

  /**
   * Creates a new map whose keys are the same, and whose values are a transformation
   * of the values of the original map, when your transformation function cares about the key when transforming.
   */
  public <V1> RBMap<K, V1> transformEntriesCopy(BiFunction<K, V, V1> entryTransformer) {
    MutableRBMap<K, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (key, value) -> mutableMap.putAssumingAbsent(key, entryTransformer.apply(key, value)));
    return newRBMap(mutableMap);
  }

  /**
   * Just like transformEntriesCopy, but goes through the entries in some deterministic ordering
   */
  public <V1> RBMap<K, V1> orderedTransformEntriesCopy(
      BiFunction<K, V, V1> entryTransformer, Comparator<K> keyComparator) {
    MutableRBMap<K, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachSortedEntry(
        comparing(entry -> entry.getKey(), keyComparator),
        (key, value) ->
            mutableMap.putAssumingAbsent(key, entryTransformer.apply(key, value)));
    return newRBMap(mutableMap);
  }

  /**
   * Transform entries and then apply a filter to the transformed values
   */
  public <V1> RBMap<K, V1> transformEntriesAndFilterValuesCopy(
      BiFunction<K, V, V1> transformer,
      Predicate<V1> mustKeepTransformedValue) {
    MutableRBMap<K, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (entry, value) -> {
      V1 transformedValue = transformer.apply(entry, value);
      if (mustKeepTransformedValue.test(transformedValue)) {
        mutableMap.putAssumingAbsent(entry, transformer.apply(entry, value));
      }
    });
    return newRBMap(mutableMap);
  }

  /**
   * Transform entries to an Optional. If the optional is present, insert its value into the returned map.
   */
  public <V1> RBMap<K, V1> transformEntriesToOptionalAndKeepIfPresentCopy(
      BiFunction<K, V, Optional<V1>> transformer) {
    MutableRBMap<K, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (entry, value) -> {
      Optional<V1> transformedValue = transformer.apply(entry, value);
      transformedValue.ifPresent(v -> mutableMap.putAssumingAbsent(entry, v));
    });
    return newRBMap(mutableMap);
  }

  /**
   * Creates a new map whose values are the same, and whose keys are a transformation
   * of the keys of the original map, when you don't care about they value when doing the transformation.
   */
  public <K1> RBMap<K1, V> transformKeysCopy(Function<K, K1> keyTransformer) {
    MutableRBMap<K1, V> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (key, value) -> mutableMap.putAssumingAbsent(keyTransformer.apply(key), value));
    return newRBMap(mutableMap);
  }

  /**
   * Creates a new map whose keys AND values are a transformation of the original ones,
   * and the key transformation doesn't depend on the value in any particular entry, and also
   * the value transformation doesn't depend on the key in any particular entry.
   */
  public <K1, V1> RBMap<K1, V1> transformKeysAndValuesCopy(Function<K, K1> keyTransformer,
                                                           Function<V, V1> valuesTransformer) {
    MutableRBMap<K1, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (key, value) ->
        mutableMap.putAssumingAbsent(keyTransformer.apply(key), valuesTransformer.apply(value)));
    return newRBMap(mutableMap);
  }

  /**
   * Creates a new map whose keys AND values are a transformation of the original ones,
   * and the key transformation doesn't depend on the value in any particular entry, and also
   * the value transformation doesn't depend on the key in any particular entry.
   */
  public <K1, V1> RBMap<K1, V1> transformKeysAndValuesCopy(Function<K, K1> keyTransformer,
                                                           BiFunction<K1, V, V1> valuesTransformer) {
    MutableRBMap<K1, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (key, value) -> {
      K1 transformedKey = keyTransformer.apply(key);
      mutableMap.putAssumingAbsent(transformedKey, valuesTransformer.apply(transformedKey, value));
    });
    return newRBMap(mutableMap);
  }

  /**
   * Creates a new map whose keys AND values are a transformation of the original ones,
   * and the key transformation doesn't depend on the value in any particular entry, but the
   * the value transformation depends on the original value and the transformed key.
   * The value transformer function returns an optional, with the semantics that only if the optional is present
   * will we create a corresponding entry in the new map.
   */
  public <K1, V1> RBMap<K1, V1> filterValuesAndTransformKeysAndValuesCopy(
      Function<K, K1> keyTransformer,
      BiFunction<K1, V, Optional<V1>> valuesTransformer) {
    MutableRBMap<K1, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (key, value) -> {
      K1 transformedKey = keyTransformer.apply(key);
      valuesTransformer.apply(transformedKey, value)
              .ifPresent(transformedValue -> mutableMap.putAssumingAbsent(transformedKey, transformedValue));
    });
    return newRBMap(mutableMap);
  }

  /**
   * Like transformKeysAndValuesCopy, except that it goes through the entries in some deterministic ordering
   */
  public <K1, V1> RBMap<K1, V1> orderedTransformKeysAndValuesCopy(
      Function<K, K1> keyTransformer,
      Function<V, V1> valuesTransformer,
      Comparator<K> keyComparator) {
    MutableRBMap<K1, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachSortedEntry(
        comparing(entry -> entry.getKey(), keyComparator),
        (key, value) -> mutableMap.putAssumingAbsent(
            keyTransformer.apply(key), valuesTransformer.apply(value)));
    return newRBMap(mutableMap);
  }

  /**
   * Makes a copy of this map, applying the overrides passed in; all keys in the overrides map
   * must be present in the original map, for safety; this way, we really match the 'overrides' semantics.
   */
  public RBMap<K, V> copyWithOverridesApplied(RBMap<K, V> overrides) {
    MutableRBMap<K, V> mutableMap = newMutableRBMap(this);
    overrides.entrySet().forEach(entry ->
      mutableMap.putAssumingPresent(entry.getKey(), entry.getValue()));
    return newRBMap(mutableMap);
  }

  /**
   * This does NOT modify this RBMap! It creates a copy, but with one key removed.
   */
  public RBMap<K, V> copyWithKeyRemoved(K keyToRemove) {
    return copyWithKeysRemoved(singletonRBSet(keyToRemove));
  }

  /**
   * This does NOT modify this RBMap! It creates a copy, but with some keys removed.
   * Throws if one of the keysToRemove is not in the map.
   */
  public RBMap<K, V> copyWithKeysRemoved(RBSet<K> keysToRemove) {
    // We know the new size, since all keysToRemove must be present in the set.
    MutableRBMap<K, V> mutableMap = newMutableRBMapWithExpectedSize(size() - keysToRemove.size());
    int totalRemoved = 0;
    for (Map.Entry<K, V> entry : entrySet()) {
      K key = entry.getKey();
      V value = entry.getValue();
      if (keysToRemove.contains(key)) {
        totalRemoved++;
      } else {
        mutableMap.putAssumingAbsent(key, value);
      }
    }
    RBPreconditions.checkArgument(
        totalRemoved == keysToRemove.size(),
        "Not all keys requested to be removed are in the map: keysToRemove= %s ; map= %s",
        keysToRemove, this.toString());
    return newRBMap(mutableMap);
  }

  /**
   * This does NOT modify this RBMap! It creates a copy, but with an extra key/value pair added.
   * Throws if the key already exists in the map.
   */
  public RBMap<K, V> withItemAddedAssumingAbsent(K key, V value) {
    MutableRBMap<K, V> mutableMap = newMutableRBMap(this);
    mutableMap.putAssumingAbsent(key, value);
    return newRBMap(mutableMap);
  }

  /**
   * Throws if the values aren't unique, because that means the key {@code ->} value mapping is not one-to-one,
   * so the map is not invertible.
   *
   * So a map of {@code "a" -> 1.1}, {@code "b" -> 1.1 + e} cannot be inverted and have as keys 1.1 and 1.1 + e; it's not a good idea,
   * although it is possible. For the same reason why checking two doubles for equality is generally not good,
   * it's also not good to have doubles as keys, because if your (double) key is epsilon-different, it will look like
   * the map doesn't have a value for that key.
   */
  public BiMap<K, V> toBiMap() {
    Builder<K, V> builder = ImmutableBiMap.builder();
    forEachEntry( (key, value) -> builder.put(key, value));
    BiMap<K, V> biMap = builder.build();
    RBSimilarityPreconditions.checkBothSame(
        biMap.size(),
        size(),
        "We cannot build a bimap from this map, because the values are not unique, so the map is not invertible");
    return biMap;
  }

  /**
   * Convert an RBMap to an RBSet by applying a transformer to each entry.
   * If the transformed value is the same, we do not throw; it's just that the cardinality of the resulting RBSet
   * will be smaller than that of the RBMap.
   *
   * A bit like {@link #transformEntriesCopy}, except that the result is an {@link RBSet}.
   */
  public <V1> RBSet<V1> toRBSet(BiFunction<K, V, V1> transformer) {
    return newRBSet(entrySet()
        .stream()
        .map(entry -> {
          K key = entry.getKey();
          V value = entry.getValue();
          return transformer.apply(key, value);
        })
        .collect(Collectors.toSet()));
  }

  // IDE-generated
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RBMap<?, ?> rbMap = (RBMap<?, ?>) o;

    return rawMap.equals(rbMap.rawMap);

  }

  // IDE-generated
  @Override
  public int hashCode() {
    return rawMap.hashCode();
  }

  @Override
  public String toString() {
    return asMap().toString();
  }

}
