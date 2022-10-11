package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBSet.newRBSet;

public class IidMaps {

  /**
   * Use this when you have a mutable map where the values themselves are mutable maps,
   * and you want to 'lock' the values (i.e. go from {@link MutableHasLongMap} to {@link IidMap})
   * so that you end up with an {@link IidMap} where the values are immutable RBMaps.
   *
   * <p> We could also have a version that results in an {@code IidMap<IidMap<V>>} but the need hasn't arisen yet. </p>
   */
  public static <K, V> IidMap<RBMap<K, V>> lockValues(MutableIidMap<MutableRBMap<K, V>> sourceMap) {
    MutableIidMap<RBMap<K, V>> mutableMap = newMutableIidMapWithExpectedSize(sourceMap.size());
    sourceMap.keysIterator().forEachRemaining(asLong -> {
      InstrumentId key = instrumentId(asLong);
      mutableMap.put(key, newRBMap(sourceMap.getOrThrow(key)));
    });
    return newIidMap(mutableMap);
  }

  /**
   * Use this when you have a mutable map where the values themselves are mutable maps,
   * and you want to 'lock' the values (i.e. go from MutableRBMap to RBMap)
   * so that you end up with an RBMap where the values are RBMaps.
   */
  public static <K, V> RBMap<K, IidMap<V>> lockValues(MutableRBMap<K, MutableIidMap<V>> sourceMap) {
    MutableRBMap<K, IidMap<V>> mutableMap = newMutableRBMapWithExpectedSize(sourceMap.size());
    for (Entry<K, MutableIidMap<V>> entry : sourceMap.entrySet()) {
      mutableMap.put(entry.getKey(), newIidMap(entry.getValue()));
    }
    return newRBMap(mutableMap);
  }

  public static <V> void addAllAssumingNoOverlap(
      MutableIidMap<V> mutableMap,
      IidMap<V> additionalValues) {
    additionalValues.instrumentIdKeysIterator()
        .forEachRemaining(instrumentId -> mutableMap.putAssumingAbsent(
            instrumentId, additionalValues.getOrThrow(instrumentId)));
  }

  /**
   * Merge two {@link IidMap}s and return the result as a new {@link IidMap}.
   */
  public static <V> IidMap<V> mergeIidMapsAssumingNoOverlap(
      IidMap<V> iidMap1,
      IidMap<V> iidMap2) {
    MutableIidMap<V> mutableIidMap = newMutableIidMapWithExpectedSize(iidMap1.size() + iidMap2.size());

    // don't need 'AssumingNoOverlap' for iidMap1
    iidMap1.forEachEntry(
        (instrumentId, value) -> mutableIidMap.put(instrumentId, value));

    // merge iidMap2
    addAllAssumingNoOverlap(mutableIidMap, iidMap2);

    return newIidMap(mutableIidMap);
  }

  /**
   * Merge additional {@link IidMap}s and return the result as a new {@Link IidMap}.
   */
  @SafeVarargs
  public static <V> IidMap<V> mergeIidMapsAssumingNoOverlap(
      IidMap<V> first,
      IidMap<V> second,
      IidMap<V>...rest) {
    // allocate the total expected size
    int sizeRest = Arrays.stream(rest).map(additionalIidMap -> additionalIidMap.size()).reduce(Integer::sum).orElse(0);
    MutableIidMap<V> mutableIidMap = newMutableIidMapWithExpectedSize(first.size() + second.size() + sizeRest);

    // don't need 'AssumingNoOverlap' for the first IidMap
    first.forEachEntry( (instrumentId, value) -> mutableIidMap.put(instrumentId, value));

    // merge the second
    addAllAssumingNoOverlap(mutableIidMap, second);
    // merge the rest
    Arrays.stream(rest).forEach(
        additionalIidMap -> addAllAssumingNoOverlap(mutableIidMap, additionalIidMap));

    return newIidMap(mutableIidMap);
  }

  /**
   * If all maps are empty, returns an empty map.
   * If only one is non-empty, returns the non-empty one.
   * Otherwise, returns Optional.empty().
   *
   * <p> This is useful for set unions; if this returns a non-empty optional, it means it's a valid result of a set union.
   * It can speed up set union calculations in those special cases. </p>
   */
  @SafeVarargs
  public static <V> Optional<IidMap<V>> getWhenAtMostOneIidMapIsNonEmpty(
      IidMap<V> first, IidMap<V> second, IidMap<V>...rest) {
    IidMap<V> onlyNonEmptyMap = null;
    if (!first.isEmpty()) {
      onlyNonEmptyMap = first;
    }
    if (!second.isEmpty()) {
      if (onlyNonEmptyMap != null) { // i.e. if we already saw a non-empty map
        return Optional.empty();
      }
      onlyNonEmptyMap = second;
    }
    for (IidMap<V> restMap : rest) {
      if (!restMap.isEmpty()) {
        if (onlyNonEmptyMap != null) { // i.e. if we already saw a non-empty map
          return Optional.empty();
        }
        onlyNonEmptyMap = restMap;
      }
    }
    return Optional.of(onlyNonEmptyMap == null
        ? emptyIidMap()
        : onlyNonEmptyMap);
  }

  /**
   * Make a copy of this map, and either remove or modify the values in its entries.
   */
  public static <V, V1> IidMap<V1> filterForPresentValuesAndTransformValuesCopy(
      IidMap<V> map, Function<V, Optional<V1>> valueTransformer) {
    MutableIidMap<V1> mutableMap = newMutableIidMapWithExpectedSize(map.size());
    map.forEachEntry( (instrumentId, originalValue) -> {
      Optional<V1> transformedValue = valueTransformer.apply(originalValue);
      if (!transformedValue.isPresent()) {
        return;
      }
      mutableMap.putAssumingAbsent(instrumentId, transformedValue.get());
    });
    return newIidMap(mutableMap);
  }

  /**
   * Inverts a map's keys and values for some special cases.
   * E.g.
   * { "A" -> (iid1, iid2), "B" -> (iid1) }
   * will become
   * { iid1 -> ("A", "B"), iid2 -> ("C") }
   */
  public static <V> IidMap<RBSet<V>> invertMapOfDisjointIidSets(RBMap<V, IidSet> rbMap) {
    if (rbMap.isEmpty()) {
      return emptyIidMap(); // performance optimization
    }
    int sizeUpperBound = rbMap
        .values()
        .stream()
        .mapToInt(v -> v.size())
        .sum();
    MutableIidMap<MutableRBSet<V>> mutableMap = newMutableIidMapWithExpectedSize(sizeUpperBound);
    rbMap.forEachEntry( (rbMapKey, iidSet) ->
        iidSet.forEach( instrumentId -> mutableMap.possiblyInitializeAndThenUpdateInPlace(
            instrumentId,
            () -> MutableRBSet.<V>newMutableRBSetWithExpectedSize(rbMap.size()),
            mutableSet -> mutableSet.add(rbMapKey))));
    return newIidMap(mutableMap)
        .transformValuesCopy(mutableRBSet -> newRBSet(mutableRBSet));
  }

}
