package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.rb.nonbiz.functional.QuadriFunction;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBMaps.getWhenUpToOneRBMapIsNonEmpty;
import static com.rb.nonbiz.collections.RBOptionals.filterPresentOptionalsInStream;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.util.RBSimilarityPreconditions.checkBothSame;

public class RBMapMergers {

  /**
   * Creates a map of 'eithers' (see Either class), where items on the left map become 'left eithers',
   * and items on the right map become 'right eithers'.
   * Throws if a key appears in both maps.
   */
  public static <K, L, R> RBMap<K, Either<L, R>> mergeIntoEithersMap(RBMap<K, L> leftMap, RBMap<K, R> rightMap) {
    if (!RBSets.noSharedItems(leftMap.keySet(), rightMap.keySet())) {
      throw new IllegalArgumentException(smartFormat(
          "Shared keys exist between maps to be merged: %s and %s",
          leftMap, rightMap));
    }
    MutableRBMap<K, Either<L, R>> mutableMap = newMutableRBMapWithExpectedSize(leftMap.size() + rightMap.size());
    leftMap.forEachEntry(  (key, value) -> mutableMap.putAssumingAbsent(key, Either.left(value)));
    rightMap.forEachEntry( (key, value) -> mutableMap.putAssumingAbsent(key, Either.right(value)));
    return newRBMap(mutableMap);
  }

  /**
   * Merges a bunch of maps into a single one.
   * Throws if a key appears in more than one map.
   */
  @SafeVarargs
  public static <K, V> RBMap<K, V> mergeRBMapsDisallowingOverlap(
      RBMap<K, V> first, RBMap<K, V> second, RBMap<K, V>...rest) {
    Optional<RBMap<K, V>> onlyNonEmptyMap = getWhenUpToOneRBMapIsNonEmpty(first, second, rest);
    if (onlyNonEmptyMap.isPresent()) {
      // The union of N items where only 1 is nonempty is equal to that 1 nonempty item.
      // This is a small performance optimization.
      return onlyNonEmptyMap.get();
    }
    int sumSizes = concatenateFirstSecondAndRest(first, second, rest)
        .map(rbMap -> rbMap.size())
        .reduce(0, Integer::sum);
    MutableRBMap<K, V> mutableMap = newMutableRBMapWithExpectedSize(sumSizes);
    concatenateFirstSecondAndRest(first, second, rest)
        .flatMap(m -> m.entrySet().stream())
        .forEach(entry -> mutableMap.putAssumingAbsent(entry.getKey(), entry.getValue()));
    return newRBMap(mutableMap);
  }

  /**
   * Merges a bunch of maps into a single one, but lets you put modified values in the merged map.
   * Throws if a key appears in more than one map.
   */
  public static <K, V> RBMap<K, V> mergeRBMapsDisallowingOverlap(UnaryOperator<V> left,
                                                                 UnaryOperator<V> right,
                                                                 RBMap<K, V> leftMap, RBMap<K, V> rightMap) {
    MutableRBMap<K, V> merged = newMutableRBMapWithExpectedSize(leftMap.size() + rightMap.size());
    leftMap.forEachEntry(  (key, value) -> merged.putAssumingAbsent(key, left.apply(value)));
    rightMap.forEachEntry( (key, value) -> merged.putAssumingAbsent(key, right.apply(value)));
    return newRBMap(merged);
  }

  public static <K, V1, V2, V> RBMap<K, V> mergeRBMapEntriesExpectingSameKeys(
      TriFunction<K, V1, V2, V> merger,
      RBMap<K, V1> map1,
      RBMap<K, V2> map2) {
    int sharedSize = checkBothSame(
        map1.size(),
        map2.size(),
        "We can only merge maps with the same keys, which implies same # of entries: %s %s",
        map1, map2);
    MutableRBMap<K, V> mutableMerged = newMutableRBMapWithExpectedSize(sharedSize);

    // We don't need to check for the *keys* to be the same. If we iterate over 'size' items of map1, and all of them
    // appear in map2 (hence the getOrThrow, which will throw otherwise), then there's no way map2 could have an entry
    // for a key that doesn't appear in map1, since their sizes are the same.
    map1.forEachEntry( (key, v1) -> mutableMerged.putAssumingAbsent(key, merger.apply(key, v1, map2.getOrThrow(key))));
    return newRBMap(mutableMerged);
  }

  public static <K, V1, V2, V> RBMap<K, V> mergeSortedRBMapEntriesExpectingSameKeys(
      TriFunction<K, V1, V2, V> merger,
      Comparator<K> keyComparator,
      RBMap<K, V1> map1,
      RBMap<K, V2> map2) {
    int sharedSize = checkBothSame(
        map1.size(),
        map2.size(),
        "We can only merge maps with the same keys, which implies same # of entries: %s %s",
        map1, map2);
    MutableRBMap<K, V> mutableMerged = newMutableRBMapWithExpectedSize(sharedSize);

    // We don't need to check for the *keys* to be the same. If we iterate over 'size' items of map1, and all of them
    // appear in map2 (hence the getOrThrow, which will throw otherwise), then there's no way map2 could have an entry
    // for a key that doesn't appear in map1, since their sizes are the same.
    map1.forEachSortedEntry(
        (entry1, entry2) -> keyComparator.compare(entry1.getKey(), entry2.getKey()),
        (key, v1) -> mutableMerged.putAssumingAbsent(key, merger.apply(key, v1, map2.getOrThrow(key))));
    return newRBMap(mutableMerged);
  }

  public static <K, V1, V2, V> RBMap<K, V> mergeRBMapValuesExpectingSameKeys(
      BiFunction<V1, V2, V> merger,
      RBMap<K, V1> map1,
      RBMap<K, V2> map2) {
    return mergeRBMapEntriesExpectingSameKeys(
        (ignoredKey, v1, v2) -> merger.apply(v1, v2), map1, map2);
  }

  public static <K, V1, V2, V> RBMap<K, V> mergeSortedRBMapValuesExpectingSameKeys(
      BiFunction<V1, V2, V> merger,
      Comparator<K> keyComparator,
      RBMap<K, V1> map1,
      RBMap<K, V2> map2) {
    return mergeSortedRBMapEntriesExpectingSameKeys(
        (ignoredKey, v1, v2) -> merger.apply(v1, v2), keyComparator, map1, map2);
  }

  public static <K, V1, V2, V3, V> RBMap<K, V> mergeRBMapEntriesExpectingSameKeys(
      RBMap<K, V1> map1,
      RBMap<K, V2> map2,
      RBMap<K, V3> map3,
      QuadriFunction<K, V1, V2, V3, V> merger) {
    int sharedSize = RBSimilarityPreconditions.checkAllSame(
        ImmutableList.of(map1, map2, map3),
        map -> map.size(),
        "We can only merge maps with the same keys, which implies same # of entries: %s %s %s",
        map1, map2, map3);
    MutableRBMap<K, V> mutableMerged = newMutableRBMapWithExpectedSize(sharedSize);

    // We don't need to check for the *keys* to be the same. If we iterate over 'size' items of map1, and all of them
    // appear in map2 (hence the getOrThrow, which will throw otherwise), then there's no way map2 could have an entry
    // for a key that doesn't appear in map1, since their sizes are the same. Same with map3.
    map1.forEachEntry( (key, v1) -> mutableMerged.putAssumingAbsent(
        key, merger.apply(key, v1, map2.getOrThrow(key), map3.getOrThrow(key))));
    return newRBMap(mutableMerged);
  }

  public static <K, V1, V2, V3, V> RBMap<K, V> mergeRBMapValuesExpectingSameKeys(
      RBMap<K, V1> map1,
      RBMap<K, V2> map2,
      RBMap<K, V3> map3,
      TriFunction<V1, V2, V3, V> merger) {
    return mergeRBMapEntriesExpectingSameKeys(
        map1,
        map2,
        map3,
        (ignoredKey, v1, v2, v3) -> merger.apply(v1, v2, v3));
  }

  /**
   * Merges a bunch of maps into a single one.
   * Throws if a key appears in more than one map, unless the items we are trying to insert are 'similar'
   * (based on the predicate passed in).
   * Since this similarity is not the same as equality, this means that if e.g. the first map maps
   * "A" to money(100), the second one (in the iterator) maps "A" to money(100 + 1e-9),
   * and the third one to money(100 - 1e-9),
   * and similarity is 'values similar to an epsilon of 1e-8', then the resulting map will show money(100)
   * in the final value for "A", NOT money(100 + 1e-9) or money(100 - 1e-9)
   */
  public static <K, V> RBMap<K, V> mergeRBMapsAllowingOverlapOnSimilarItemsOnly(
      Iterator<RBMap<K, V>> mapsIterator, BiPredicate<V, V> itemsAreSimilar) {
    MutableRBMap<K, V> mutableMap = newMutableRBMap();
    mapsIterator
        .forEachRemaining(map -> map.forEachEntry(
            (key, newValue) -> {
              mutableMap.getOptional(key)
                  .ifPresent(existingValue -> RBPreconditions.checkArgument(
                      itemsAreSimilar.test(existingValue, newValue),
                      "We do not allow overlap in the IidMaps when items are dissimilar: %s has %s but trying to put %s",
                      key, existingValue, newValue));
              mutableMap.putIfAbsent(key, newValue);
            }));
    return newRBMap(mutableMap);
  }

  /**
   * Merges a bunch of maps into a single one.
   *
   * In the event a key appears in more than one map, apply a binary operator (e.g. '+') to merge
   * the values. This doesn't *assume* that the operator is commutative, but it probably should be.
   * Otherwise, your result will depend on the order you pass the maps, which will be confusing.
   */
  @SafeVarargs
  public static <K, V> RBMap<K, V> mergeRBMapsByValue(
      BinaryOperator<V> mergeFunction, RBMap<K, V> first, RBMap<K, V> second, RBMap<K, V>...rest) {
    Optional<RBMap<K, V>> onlyNonEmptyMap = getWhenUpToOneRBMapIsNonEmpty(first, second, rest);
    if (onlyNonEmptyMap.isPresent()) {
      // The union of N items where only 1 is nonempty is equal to that 1 nonempty item.
      return onlyNonEmptyMap.get();
    }
    return mergeRBMapsByValue(mergeFunction, concatenateFirstSecondAndRest(first, second, rest));
  }

  /**
   * Merges two maps into a single one.
   *
   * For the 3 separate cases of a key appearing in the left map / right map / both maps,
   * modify the values based on the functions passed in.
   */
  public static <K, V> RBMap<K, V> mergeRBMapsByValue(BinaryOperator<V> mergeFunction,
                                                      UnaryOperator<V> onlyLeftPresent,
                                                      UnaryOperator<V> onlyRightPresent,
                                                      RBMap<K, V> leftMap, RBMap<K, V> rightMap) {
    return newRBSet(Sets.union(leftMap.keySet(), rightMap.keySet()))
        .toRBMap(key -> {
          Optional<V> leftItem = leftMap.getOptional(key);
          Optional<V> rightItem = rightMap.getOptional(key);
          return !leftItem.isPresent() ? onlyRightPresent.apply(rightItem.get()) :
              !rightItem.isPresent() ? onlyLeftPresent.apply(leftItem.get()) :
                  mergeFunction.apply(leftItem.get(), rightItem.get());
        });
  }

  /**
   * Merges a bunch of maps into a single one.
   *
   * This is similar to the previous overload, except that it allows you to merge multiple (and possibly 0) items
   * at a time, plus the value in the returned map doesn't have to be of the same type as the input maps.
   *
   * This is particularly useful for cases where it is more computationally expensive (or numerically unstable) to
   * merge N items 2 at a time (as with the previous overload) vs. merging them all together.
   */
  public static <K, V1, V2> RBMap<K, V2> mergeRBMapsByTransformedValue(
      BiFunction<K, List<V1>, V2> mergeFunction,
      List<RBMap<K, V1>> mapsList) {
    RBSet<K> allKeys = RBSets.union(
        mapsList.stream().map(rbMap -> newRBSet(rbMap.keySet())).iterator());

    MutableRBMap<K, V2> mutableMap = newMutableRBMapWithExpectedSize(allKeys.size());
    allKeys.forEach(key ->
        mutableMap.putAssumingAbsent(key, mergeFunction.apply(
            key,
            filterPresentOptionalsInStream(mapsList
                .stream()
                .map(rbMap -> rbMap.getOptional(key)))
                .collect(Collectors.toList()))));
    return newRBMap(mutableMap);
  }

  /**
   * Merges two maps into a single one, but also transforms the values into a possibly different type.
   *
   * For the 3 separate cases of a key appearing in the left map / right map / both maps,
   * modify the values based on the functions passed in.
   *
   * @see IidMapMergers#mergeIidMapsByTransformedEntry
   */
  public static <K, V1, V2, V3> RBMap<K, V3> mergeRBMapsByTransformedValue(
      BiFunction<V1, V2, V3> mergeFunction,
      Function<V1, V3> onlyLeftPresent,
      Function<V2, V3> onlyRightPresent,
      RBMap<K, V1> leftMap, RBMap<K, V2> rightMap) {
    return newRBSet(Sets.union(leftMap.keySet(), rightMap.keySet()))
        .toRBMap(key -> {
          Optional<V1> leftItem = leftMap.getOptional(key);
          Optional<V2> rightItem = rightMap.getOptional(key);
          return !leftItem.isPresent() ? onlyRightPresent.apply(rightItem.get()) :
              !rightItem.isPresent() ? onlyLeftPresent.apply(leftItem.get()) :
                  mergeFunction.apply(leftItem.get(), rightItem.get());
        });
  }

  /**
   * Merges a bunch of maps into a single one.
   *
   * In the event a key appears in more than one map, apply a binary operator (e.g. '+') to merge
   * the values. This doesn't *assume* that the operator is commutative, but it probably should be.
   * Otherwise, your result will depend on the order you pass the maps in the stream, which will be confusing.
   */
  public static <K, V> RBMap<K, V> mergeRBMapsByValue(BinaryOperator<V> mergeFunction, Stream<RBMap<K, V>> mapsStream) {
    MutableRBMap<K, V> mutableMap = newMutableRBMap();
    mapsStream
        .flatMap(map -> map.entrySet().stream())
        .forEach(entry -> {
          K key = entry.getKey();
          V value = entry.getValue();
          mutableMap.put(key, mutableMap.containsKey(key)
              ? mergeFunction.apply(mutableMap.getOrThrow(key), value)
              : value);
        });
    return newRBMap(mutableMap);
  }

  /**
   * For two maps of RBNumeric, multiply each corresponding item, and return the sum of those terms,
   * kind of like a vector dot product.
   *
   * The keys are expected to be the same in each map.
   *
   */
  public static <K, V1 extends RBNumeric<? super V1>, V2 extends RBNumeric<? super V2>> double dotProductOfRBMaps(
      RBMap<K, V1> map1, RBMap<K, V2> map2) {
    checkBothSame(
        map1.size(),
        map2.size(),
        "We can only merge maps with the same keys, which implies same # of entries: %s %s",
        map1, map2);

    double sum = 0;
    for(Map.Entry<K, V1> entry : map1.entrySet()) {
      K key = entry.getKey();
      V1 value1 = entry.getValue();
      // We don't need to check for the *keys* to be the same. If we iterate over 'size' items of map1, and all of them
      // appear in map2 (hence the getOrThrow, which will throw otherwise), then there's no way map2 could have an entry
      // for a key that doesn't appear in map1, since their sizes are the same.
      sum += value1.doubleValue() * map2.getOrThrow(key).doubleValue();
    }
    return sum;

  }

}
