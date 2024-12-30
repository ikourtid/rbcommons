package com.rb.nonbiz.text;

import com.rb.nonbiz.collections.Pair;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static java.util.Comparator.comparing;

/**
 * An immutable map from {@link HasUniqueId} to any value.
 *
 * <p> Sometimes we want an RBMap, but the map key does not implement #equals and #hashCode.
 * We can just use {@code RBMap<K, V>} in the case where we can rely on pointer comparison, e.g. if K does not implement
 * hashCode or equals, but we don't instantiate more than one K that would ever count as intuitively 'equal'. </p>
 *
 * <p> Essentially, this behaves like an {@code RBMap<K, V>} but by storing two separate maps to avoid the fact that K
 * does not implement #equals and #hashCode, as it should in an {@code RBMap<K, V>}. </p>
 *
 * <p> The initial use case scenario for this was a map of BacktestResultField to some other type.
 * BacktestResultField has a uniqueId, but does not implement #equals and #hashCode. So we shouldn't be keying off of
 * a BacktestResultField. We were doing this (June 2019) but it was working because we never have more than one
 * instance of a BacktestResultField with the same uniqueId, so everything worked, but only by accident. </p>
 */
public class RBMapOfHasUniqueId<K extends HasUniqueId<K>, V> {

  private final RBMap<UniqueId<K>, Pair<K, V>> rawMap;

  private RBMapOfHasUniqueId(RBMap<UniqueId<K>, Pair<K, V>> rawMap) {
    this.rawMap = rawMap;
  }

  public static <K extends HasUniqueId<K>, V> RBMapOfHasUniqueId<K, V> rbMapOfHasUniqueId(
      MutableRBMapOfHasUniqueId<K, V> mutableMap) {
    return new RBMapOfHasUniqueId<>(newRBMap(mutableMap.getRawMap()));
  }

  public RBSet<UniqueId<K>> uniqueIdKeySet() {
    return newRBSet(rawMap.keySet());
  }

  public RBSet<K> keySet() {
    return newRBSet(rawMap.values()
        .stream()
        .map(pair -> pair.getLeft())
        .collect(Collectors.toSet()));
  }

  public Stream<K> keyStream() {
    return rawMap.values()
        .stream()
        .map(pair -> pair.getLeft());
  }

  public Stream<K> sortedKeyStream() {
    return rawMap.values()
        .stream()
        .map(pair -> pair.getLeft())
        .sorted(comparing(v -> v.getUniqueId()));
  }

  public RBSet<V> values() {
    return newRBSet(rawMap.values()
        .stream()
        .map(pair -> pair.getRight())
        .collect(Collectors.toSet()));
  }

  public <V2> RBMapOfHasUniqueId<K, V2> transformValuesCopy(Function<V, V2> transformer) {
    return new RBMapOfHasUniqueId<>(rawMap.transformValuesCopy(
        v -> pair(v.getLeft(), transformer.apply(v.getRight()))));
  }

  public int size() {
    return rawMap.size();
  }

  public V getOrThrow(K hasUniqueId) {
    return rawMap.getOrThrow(hasUniqueId.getUniqueId()).getRight();
  }

  public Optional<V> getOptional(K hasUniqueId) {
    return transformOptional(rawMap.getOptional(hasUniqueId.getUniqueId()), v -> v.getRight());
  }

  public boolean containsKey(UniqueId<K> key) {
    return rawMap.containsKey(key);
  }

  /**
   * Returns the value under the key (if present), otherwise throws.
   */
  public V getOrThrow(UniqueId<K> uniqueId) {
    if (uniqueId == null) {
      throw new IllegalArgumentException("An RBMapOfHasUniqueId does not allow null keys");
    }
    return rawMap.getOrThrow(uniqueId).getRight();
  }

  /**
   * Returns the value under the key (if present), otherwise the defaultValue.
   */
  public V getOrDefault(K key, V defaultValue) {
    if (key == null) {
      throw new IllegalArgumentException("An RBMapOfHasUniqueId does not allow null keys");
    }
    return transformOptional(
        rawMap.getOptional(key.getUniqueId()),
        pair -> pair.getRight())
        .orElse(defaultValue);
  }

  public RBMap<UniqueId<K>, Pair<K, V>> getRawMap() {
    return rawMap;
  }

  public void forEachPair(BiConsumer<K, V> biConsumer) {
    rawMap.forEachEntry( (uniqueId, entireKeyAndValue) ->
        biConsumer.accept(entireKeyAndValue.getLeft(), entireKeyAndValue.getRight()));
  }

  public boolean isEmpty() {
    return rawMap.isEmpty();
  }

  @Override
  public String toString() {
    return Strings.format("[RMOHUI %s %s RMOHUI]", rawMap.size(), rawMap);
  }

}
