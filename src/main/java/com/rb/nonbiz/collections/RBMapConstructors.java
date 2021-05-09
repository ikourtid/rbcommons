package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.HasUniqueId;
import com.rb.nonbiz.text.UniqueId;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;

public class RBMapConstructors {

  /**
   * This will also throw if there is more than 1 item with the same key.
   */
  public static <K, V, V2> RBMap<K, V2> rbMapFromIterator(
      Iterator<V> iterator, Function<V, K> keyExtractor, Function<V, V2> valueExtractor) {
    MutableRBMap<K, V2> mutableRBMap = newMutableRBMap();
    iterator.forEachRemaining(v -> mutableRBMap.putAssumingAbsent(keyExtractor.apply(v), valueExtractor.apply(v)));
    return newRBMap(mutableRBMap);
  }

  /**
   * This will also throw if there is more than 1 item with the same key.
   */
  public static <K, V> RBMap<K, V> rbMapFromCollection(Collection<V> collection, Function<V, K> keyExtractor) {
    MutableRBMap<K, V> mutableRBMap = newMutableRBMapWithExpectedSize(collection.size());
    collection.forEach(v -> mutableRBMap.putAssumingAbsent(keyExtractor.apply(v), v));
    return newRBMap(mutableRBMap);
  }

  /**
   * This will also throw if there is more than 1 item with the same key.
   */
  public static <K, V, V2> RBMap<K, V2> rbMapFromCollection(
      Collection<V> collection, Function<V, K> keyExtractor, Function<V, V2> valueExtractor) {
    MutableRBMap<K, V2> mutableRBMap = newMutableRBMapWithExpectedSize(collection.size());
    collection.forEach(v -> mutableRBMap.putAssumingAbsent(keyExtractor.apply(v), valueExtractor.apply(v)));
    return newRBMap(mutableRBMap);
  }

  /**
   * This will also throw if there is more than 1 item with the same key.
   */
  public static <V extends HasUniqueId<?>> RBMap<UniqueId<?>, V> rbMapFromCollectionOfHasInstrumentId(Collection<V> collection) {
    MutableRBMap<UniqueId<?>, V> mutableRBMap = newMutableRBMapWithExpectedSize(collection.size());
    collection.forEach(v -> mutableRBMap.putAssumingAbsent(v.getUniqueId(), v));
    return newRBMap(mutableRBMap);
  }

  /**
   * This will also throw if there is more than 1 item with the same key.
   */
  public static <K, V> RBMap<K, V> rbMapFromStream(Stream<V> stream, Function<V, K> keyExtractor) {
    MutableRBMap<K, V> mutableRBMap = newMutableRBMap();
    stream.forEach(v -> mutableRBMap.putAssumingAbsent(keyExtractor.apply(v), v));
    return newRBMap(mutableRBMap);
  }

  /**
   * Builds an RBMap based on a stream. Each key-value pair depends on a single stream item.
   *
   * This will also throw if there is more than 1 item with the same key.
   */
  public static <K, V, T> RBMap<K, V> rbMapFromStream(
      Stream<T> stream, Function<T, K> keyExtractor, Function<T, V> valueExtractor) {
    MutableRBMap<K, V> mutableRBMap = newMutableRBMap();
    stream.forEach(v -> mutableRBMap.putAssumingAbsent(keyExtractor.apply(v), valueExtractor.apply(v)));
    return newRBMap(mutableRBMap);
  }

  /**
   * Builds an RBMap based on a stream. Each key-value pair depends on a single stream item.
   *
   * This will also throw if there is more than 1 item with the same key.
   *
   * Similar to #rbMapFromStream, but the value transformer returns Optional.empty for values that shouldn't go
   * into the final map, and non-empty optional for the values that should go into the map.
   */
  public static <K, V, T> RBMap<K, V> rbMapFromStreamOfOptionals(
      Stream<T> stream, Function<T, K> keyExtractor, Function<K, Optional<V>> optionalValueExtractor) {
    MutableRBMap<K, V> mutableRBMap = newMutableRBMap();
    stream.forEach(v -> {
      K key = keyExtractor.apply(v);
      optionalValueExtractor.apply(key).ifPresent(transformedValue ->
          mutableRBMap.putAssumingAbsent(key, transformedValue));
    });
    return newRBMap(mutableRBMap);
  }

  /**
   * This will also throw if there is more than 1 item with the same key.
   */
  public static <K, V> RBMap<K, V> rbMapWithExpectedSizeFromStream(
      int expectedSize, Stream<V> stream, Function<V, K> keyExtractor) {
    MutableRBMap<K, V> mutableRBMap = newMutableRBMapWithExpectedSize(expectedSize);
    stream.forEach(v -> mutableRBMap.putAssumingAbsent(keyExtractor.apply(v), v));
    return newRBMap(mutableRBMap);
  }

  /**
   * Builds an RBMap based on a stream. Each key-value pair depends on a single stream item.
   *
   * This will also throw if there is more than 1 item with the same key.
   */
  public static <K, V, T> RBMap<K, V> rbMapWithExpectedSizeFromStream(
      int expectedSize, Stream<T> stream, Function<T, K> keyExtractor, Function<T, V> valueExtractor) {
    MutableRBMap<K, V> mutableRBMap = newMutableRBMapWithExpectedSize(expectedSize);
    stream.forEach(v -> mutableRBMap.putAssumingAbsent(keyExtractor.apply(v), valueExtractor.apply(v)));
    return newRBMap(mutableRBMap);
  }

}
