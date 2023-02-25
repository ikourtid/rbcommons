package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;

/**
 * Various static methods pertaining to RBMap objects.
 *
 * @see RBMap
 */
public class RBMaps {

  /**
   * Use this when you have a mutable map where the values themselves are mutable maps,
   * and you want to 'lock' the values (i.e. go from MutableRBMap to RBMap)
   * so that you end up with an RBMap where the keys are RBMaps.
   */
  public static <K1, K2, V> RBMap<K1, RBMap<K2, V>> lockValues(MutableRBMap<K1, MutableRBMap<K2, V>> sourceMap) {
    MutableRBMap<K1, RBMap<K2, V>> lockedMap = newMutableRBMap();
    for (Entry<K1, MutableRBMap<K2, V>> entry : sourceMap.entrySet()) {
      lockedMap.put(entry.getKey(), newRBMap(entry.getValue()));
    }
    return newRBMap(lockedMap);
  }

  /**
   * Create the map with all 'keys', with each key mapping to 'sharedValue'.
   */
  public static <K, V> RBMap<K, V> sharedItemMap(RBSet<K> keys, V sharedValue) {
    MutableRBMap<K, V> mutableMap = newMutableRBMapWithExpectedSize(keys.size());
    for (K key : keys) {
      mutableMap.putAssumingAbsent(key, sharedValue);
    }
    return newRBMap(mutableMap);
  }

  /**
   * If all maps are empty, returns an empty map.
   * If only one is non-empty, returns the non-empty one.
   * Otherwise returns Optional.empty().
   *
   * This is useful for set unions; if this returns a non-empty optional, it means it's a valid result of a set union.
   * It can speed up set union calculations in those special cases.
   */
  @SafeVarargs
  public static <K, V> Optional<RBMap<K, V>> getWhenUpToOneRBMapIsNonEmpty(
      RBMap<K, V> first, RBMap<K, V> second, RBMap<K, V>...rest) {
    RBMap<K, V> onlyNonEmptyMap = null;
    if (!first.isEmpty()) {
      onlyNonEmptyMap = first;
    }
    if (!second.isEmpty()) {
      if (onlyNonEmptyMap != null) { // i.e. if we already saw a non-empty map
        return Optional.empty();
      }
      onlyNonEmptyMap = second;
    }
    for (RBMap<K, V> restMap : rest) {
      if (!restMap.isEmpty()) {
        if (onlyNonEmptyMap != null) { // i.e. if we already saw a non-empty map
          return Optional.empty();
        }
        onlyNonEmptyMap = restMap;
      }
    }
    return Optional.of(onlyNonEmptyMap == null
        ? emptyRBMap()
        : onlyNonEmptyMap);
  }

  /**
   * Create a copy of this map, but only keep the entries where the key satisfies the supplied predicate.
   */
  public static <K, V> RBMap<K, V> filterMapKeys(RBMap<K, V> unfilteredMap, Predicate<K> keyPredicate) {
    MutableRBMap<K, V> filteredMap = newMutableRBMap();
    unfilteredMap.forEachEntry( (key, value) -> {
      if (keyPredicate.test(key)) {
        filteredMap.put(key, value);
      }
    });
    return newRBMap(filteredMap);
  }

  /**
   * Make a copy of this map, and either remove or modify the values in its entries.
   */
  public static <K, V, V1> RBMap<K, V1> filterForPresentValuesAndTransformValuesCopy(
      RBMap<K, V> map, Function<V, Optional<V1>> valueTransformer) {
    MutableRBMap<K, V1> mutableMap = newMutableRBMapWithExpectedSize(map.size());
    map.forEachEntry( (key, originalValue) -> {
      Optional<V1> transformedValue = valueTransformer.apply(originalValue);
      if (!transformedValue.isPresent()) {
        return;
      }
      mutableMap.putAssumingAbsent(key, transformedValue.get());
    });
    return newRBMap(mutableMap);
  }

  public static <K, V1, V2> Stream<V2> mapEntrySet(RBMap<K, V1> rbMap, BiFunction<K, V1, V2> converter) {
    return rbMap.entrySet()
        .stream()
        .map(entry -> converter.apply(entry.getKey(), entry.getValue()));
  }

  public static <K, V1> DoubleStream mapEntrySetToDouble(RBMap<K, V1> rbMap, ToDoubleBiFunction<K, V1> converter) {
    return rbMap.entrySet()
        .stream()
        .mapToDouble(entry -> converter.applyAsDouble(entry.getKey(), entry.getValue()));
  }

  /**
   * Compare two RBMaps whose values are PreciseValues to see if the keys match and the values
   * are equal to within epsilon.
   */
  public static <K, V extends PreciseValue<? super V>> boolean preciseValueMapsAlmostEqual(
      RBMap<K, V> map1, RBMap<K, V> map2, Epsilon epsilon) {
    if (!map1.keySet().equals(map2.keySet())) {
      return false;
    }
    return map1.entrySet()
        .stream()
        .allMatch( entry -> {
          K key1 = entry.getKey();
          V value1 = entry.getValue();
          final V orThrow = map2.getOrThrow(key1);
          return value1.almostEquals(orThrow, epsilon);
        });
  }

  /**
   * Compare two RBMaps whose values are ImpreciseValues to see if their keys match and the values
   * are equal within epsilon.
   */
  public static <K, V extends ImpreciseValue<? super V>> boolean impreciseValueMapsAlmostEqual(
      RBMap<K, V> map1, RBMap<K, V> map2, Epsilon epsilon) {
    if (!map1.keySet().equals(map2.keySet())) {
      return false;
    }
    return map1.entrySet()
        .stream()
        .allMatch( entry -> {
          K key1 = entry.getKey();
          V value1 = entry.getValue();
          final V orThrow = map2.getOrThrow(key1);
          return value1.almostEquals(orThrow, epsilon);
        });
  }

  public static <K, V> RBMap<K, V> filterPresentOptionalsInRBMapCopy(RBMap<K, Optional<V>> originalMap) {
    return originalMap
        .filterValues(v -> v.isPresent())
        .transformValuesCopy(v -> v.get());
  }

}
