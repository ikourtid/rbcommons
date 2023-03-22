package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.rb.nonbiz.functional.QuadriFunction;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.types.Pointer;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

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

import static com.rb.nonbiz.collections.MutableRBEnumMap.newMutableRBEnumMap;
import static com.rb.nonbiz.collections.RBEnumMap.newRBEnumMap;
import static com.rb.nonbiz.collections.RBOptionals.filterPresentOptionalsInStream;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;
import static com.rb.nonbiz.types.Pointer.uninitializedPointer;
import static com.rb.nonbiz.util.RBEnumMaps.getWhenUpToOneRBEnumMapIsNonEmpty;
import static com.rb.nonbiz.util.RBSimilarityPreconditions.checkAllSame;
import static com.rb.nonbiz.util.RBSimilarityPreconditions.checkBothSame;

public class RBEnumMapMergers {

  /**
   * Creates a map of 'eithers' (see Either class), where items on the left map become 'left eithers',
   * and items on the right map become 'right eithers'.
   * Throws if a key appears in both maps.
   */
  public static <E extends Enum<E>, L, R> RBEnumMap<E, Either<L, R>> mergeIntoEithersEnumMap(
      RBEnumMap<E, L> leftMap, RBEnumMap<E, R> rightMap) {
    if (!RBSets.noSharedItems(leftMap.keySet(), rightMap.keySet())) {
      throw new IllegalArgumentException(smartFormat(
          "Shared keys exist between maps to be merged: %s and %s",
          leftMap, rightMap));
    }
    Class<E> sharedEnumClass = checkBothSame(
        leftMap.getEnumClass(),
        rightMap.getEnumClass(),
        "Internal error; enum classes must be the same; this should have been caught by the compiler: %s %s",
        leftMap, rightMap);
    MutableRBEnumMap<E, Either<L, R>> mutableMap = newMutableRBEnumMap(sharedEnumClass);
    leftMap.forEachEntryInKeyOrder(  (key, value) -> mutableMap.putAssumingAbsent(key, Either.left(value)));
    rightMap.forEachEntryInKeyOrder( (key, value) -> mutableMap.putAssumingAbsent(key, Either.right(value)));
    return newRBEnumMap(mutableMap);
  }

  /**
   * Merges a bunch of maps into a single one.
   * Throws if a key appears in more than one map.
   */
  @SafeVarargs
  public static <E extends Enum<E>, V> RBEnumMap<E, V> mergeRBEnumMapsDisallowingOverlap(
      RBEnumMap<E, V> first, RBEnumMap<E, V> second, RBEnumMap<E, V>...rest) {
    Optional<RBEnumMap<E, V>> onlyNonEmptyMap = getWhenUpToOneRBEnumMapIsNonEmpty(first, second, rest);
    if (onlyNonEmptyMap.isPresent()) {
      // The union of N items where only 1 is nonempty is equal to that 1 nonempty item.
      // This is a small performance optimization.
      return onlyNonEmptyMap.get();
    }
    // This will be the same for all maps (compiler-enforced), but let's play it extra safe.
    Class<E> sharedEnumClass = checkAllSame(
        concatenateFirstSecondAndRest(first, second, rest).iterator(),
        v -> v.getEnumClass());
    MutableRBEnumMap<E, V> mutableMap = newMutableRBEnumMap(sharedEnumClass);
    concatenateFirstSecondAndRest(first, second, rest)
        .flatMap(m -> m.entrySet().stream())
        .forEach(entry -> mutableMap.putAssumingAbsent(entry.getKey(), entry.getValue()));
    return newRBEnumMap(mutableMap);
  }

  /**
   * Merges a bunch of maps into a single one, but lets you put modified values in the merged map.
   * Throws if a key appears in more than one map.
   */
  public static <E extends Enum<E>, V> RBEnumMap<E, V> mergeRBEnumMapsDisallowingOverlap(
      UnaryOperator<V> left,
      UnaryOperator<V> right,
      RBEnumMap<E, V> leftMap, RBEnumMap<E, V> rightMap) {
    Class<E> sharedEnumClass = checkBothSame(
        leftMap.getEnumClass(),
        rightMap.getEnumClass(),
        "Internal error; enum classes must be the same; this should have been caught by the compiler: %s %s",
        leftMap, rightMap);
    MutableRBEnumMap<E, V> merged = newMutableRBEnumMap(sharedEnumClass);
    leftMap.forEachEntryInKeyOrder(  (key, value) -> merged.putAssumingAbsent(key, left.apply(value)));
    rightMap.forEachEntryInKeyOrder( (key, value) -> merged.putAssumingAbsent(key, right.apply(value)));
    return newRBEnumMap(merged);
  }

  public static <E extends Enum<E>, V1, V2, V> RBEnumMap<E, V> mergeRBEnumMapEntriesExpectingSameKeys(
      TriFunction<E, V1, V2, V> merger,
      RBEnumMap<E, V1> map1,
      RBEnumMap<E, V2> map2) {
    Class<E> sharedEnumClass = checkBothSame(
        map1.getEnumClass(),
        map2.getEnumClass(),
        "Internal error; enum classes must be the same; this should have been caught by the compiler: %s %s",
        map1, map2);
    MutableRBEnumMap<E, V> mutableMerged = newMutableRBEnumMap(sharedEnumClass);

    // We don't need to check for the *keys* to be the same. If we iterate over 'size' items of map1, and all of them
    // appear in map2 (hence the getOrThrow, which will throw otherwise), then there's no way map2 could have an entry
    // for a key that doesn't appear in map1, since their sizes are the same.
    checkBothSame(
        map1.size(),
        map2.size(),
        "Map sizes must be the same: %s %s",
        map1, map2);
    map1.forEachEntryInKeyOrder( (key, v1) ->
        mutableMerged.putAssumingAbsent(key, merger.apply(key, v1, map2.getOrThrow(key))));
    return newRBEnumMap(mutableMerged);
  }

  public static <E extends Enum<E>, V1, V2, V> RBEnumMap<E, V> mergeSortedRBEnumMapEntriesExpectingSameKeys(
      TriFunction<E, V1, V2, V> merger,
      RBEnumMap<E, V1> map1,
      RBEnumMap<E, V2> map2) {
    checkBothSame(
        map1.size(),
        map2.size(),
        "We can only merge maps with the same keys, which implies same # of entries: %s %s",
        map1, map2);
    Class<E> sharedEnumClass = checkBothSame(
        map1.getEnumClass(),
        map2.getEnumClass(),
        "Internal error; enum classes must be the same; this should have been caught by the compiler: %s %s",
        map1, map2);
    MutableRBEnumMap<E, V> mutableMerged = newMutableRBEnumMap(sharedEnumClass);

    // We don't need to check for the *keys* to be the same. If we iterate over 'size' items of map1, and all of them
    // appear in map2 (hence the getOrThrow, which will throw otherwise), then there's no way map2 could have an entry
    // for a key that doesn't appear in map1, since their sizes are the same.
    map1.forEachEntryInKeyOrder(
        (key, v1) -> mutableMerged.putAssumingAbsent(key, merger.apply(key, v1, map2.getOrThrow(key))));
    return newRBEnumMap(mutableMerged);
  }

  public static <E extends Enum<E>, V1, V2, V> RBEnumMap<E, V> mergeRBEnumMapValuesExpectingSameKeys(
      BiFunction<V1, V2, V> merger,
      RBEnumMap<E, V1> map1,
      RBEnumMap<E, V2> map2) {
    return mergeRBEnumMapEntriesExpectingSameKeys(
        (ignoredKey, v1, v2) -> merger.apply(v1, v2), map1, map2);
  }

  public static <E extends Enum<E>, V1, V2, V> RBEnumMap<E, V> mergeSortedRBEnumMapValuesExpectingSameKeys(
      BiFunction<V1, V2, V> merger,
      RBEnumMap<E, V1> map1,
      RBEnumMap<E, V2> map2) {
    return mergeSortedRBEnumMapEntriesExpectingSameKeys(
        (ignoredKey, v1, v2) -> merger.apply(v1, v2), map1, map2);
  }

  public static <E extends Enum<E>, V1, V2, V3, V> RBEnumMap<E, V> mergeRBEnumMapEntriesExpectingSameKeys(
      RBEnumMap<E, V1> map1,
      RBEnumMap<E, V2> map2,
      RBEnumMap<E, V3> map3,
      QuadriFunction<E, V1, V2, V3, V> merger) {
    List<RBEnumMap<E, ?>> maps = ImmutableList.of(map1, map2, map3);
    checkAllSame(
        maps,
        map -> map.size(),
        "We can only merge maps with the same keys, which implies same # of entries: %s %s %s",
        map1, map2, map3);
    Class<E> sharedEnumClass = checkAllSame(
        maps,
        v -> v.getEnumClass());
    MutableRBEnumMap<E, V> mutableMerged = newMutableRBEnumMap(sharedEnumClass);

    // We don't need to check for the *keys* to be the same. If we iterate over 'size' items of map1, and all of them
    // appear in map2 (hence the getOrThrow, which will throw otherwise), then there's no way map2 could have an entry
    // for a key that doesn't appear in map1, since their sizes are the same. Same with map3.
    map1.forEachEntryInKeyOrder( (key, v1) -> mutableMerged.putAssumingAbsent(
        key, merger.apply(key, v1, map2.getOrThrow(key), map3.getOrThrow(key))));
    return newRBEnumMap(mutableMerged);
  }

  public static <E extends Enum<E>, V1, V2, V3, V> RBEnumMap<E, V> mergeRBEnumMapValuesExpectingSameKeys(
      RBEnumMap<E, V1> map1,
      RBEnumMap<E, V2> map2,
      RBEnumMap<E, V3> map3,
      TriFunction<V1, V2, V3, V> merger) {
    return mergeRBEnumMapEntriesExpectingSameKeys(
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
  public static <E extends Enum<E>, V> RBEnumMap<E, V> mergeRBEnumMapsAllowingOverlapOnSimilarItemsOnly(
      Iterator<RBEnumMap<E, V>> mapsIterator, BiPredicate<V, V> itemsAreSimilar) {
    Pointer<MutableRBEnumMap<E, V>> mutableMapPointer = uninitializedPointer();
    mapsIterator
        .forEachRemaining(map -> {
          // This trickery with the side effect (which we rarely use) is required so that we won't have to pass in
          // the enum class, and will instead get it off of the first RBEnumMap object. This means that the stream
          // passed in must have at least one map in it.
          if (!mutableMapPointer.isInitialized()) {
            mutableMapPointer.setAssumingUninitialized(newMutableRBEnumMap(map.getEnumClass()));
          }
          map.forEachEntryInKeyOrder(
              (key, newValue) -> {
                MutableRBEnumMap<E, V> mutableMap = mutableMapPointer.getOrThrow();

                mutableMap.getOptional(key)
                    .ifPresent(existingValue -> RBPreconditions.checkArgument(
                        itemsAreSimilar.test(existingValue, newValue),
                        "We do not allow overlap in the IidMaps when items are dissimilar: %s has %s but trying to put %s",
                        key, existingValue, newValue));
                mutableMap.putIfAbsent(key, newValue);
              });
        });
    RBPreconditions.checkArgument(
        mutableMapPointer.isInitialized(),
        "mergeRBEnumMapsByValue must be called with a stream of at least one map");
    return newRBEnumMap(mutableMapPointer.getOrThrow());
  }

  /**
   * Merges a bunch of maps into a single one.
   *
   * In the event a key appears in more than one map, apply a binary operator (e.g. '+') to merge
   * the values. This doesn't *assume* that the operator is commutative, but it probably should be.
   * Otherwise, your result will depend on the order you pass the maps, which will be confusing.
   */
  @SafeVarargs
  public static <E extends Enum<E>, V> RBEnumMap<E, V> mergeRBEnumMapsByValue(
      BinaryOperator<V> mergeFunction, RBEnumMap<E, V> first, RBEnumMap<E, V> second, RBEnumMap<E, V>...rest) {
    Optional<RBEnumMap<E, V>> onlyNonEmptyMap = getWhenUpToOneRBEnumMapIsNonEmpty(first, second, rest);
    if (onlyNonEmptyMap.isPresent()) {
      // The union of N items where only 1 is nonempty is equal to that 1 nonempty item.
      return onlyNonEmptyMap.get();
    }
    return mergeRBEnumMapsByValue(mergeFunction, concatenateFirstSecondAndRest(first, second, rest));
  }

  /**
   * Merges two maps into a single one.
   *
   * For the 3 separate cases of a key appearing in the left map / right map / both maps,
   * modify the values based on the functions passed in.
   */
  public static <E extends Enum<E>, V> RBEnumMap<E, V> mergeRBEnumMapsByValue(
      BinaryOperator<V> mergeFunction,
      UnaryOperator<V> onlyLeftPresent,
      UnaryOperator<V> onlyRightPresent,
      RBEnumMap<E, V> leftMap, RBEnumMap<E, V> rightMap) {
    Class<E> sharedEnumClass = checkBothSame(
        leftMap.getEnumClass(),
        rightMap.getEnumClass(),
        "Internal error; enum classes must be the same; this should have been caught by the compiler: %s %s",
        leftMap, rightMap);
    return newRBEnumMap(
        sharedEnumClass,
        newRBSet(Sets.union(leftMap.keySet(), rightMap.keySet()))
            .toRBMap(key -> {
              Optional<V> leftItem = leftMap.getOptional(key);
              Optional<V> rightItem = rightMap.getOptional(key);
              return !leftItem.isPresent() ? onlyRightPresent.apply(rightItem.get()) :
                  !rightItem.isPresent() ? onlyLeftPresent.apply(leftItem.get()) :
                      mergeFunction.apply(leftItem.get(), rightItem.get());
            }));
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
  public static <E extends Enum<E>, V1, V2> RBEnumMap<E, V2> mergeRBEnumMapsByTransformedValue(
      BiFunction<E, List<V1>, V2> mergeFunction,
      List<RBEnumMap<E, V1>> mapsList) {
    RBPreconditions.checkArgument(
        !mapsList.isEmpty(),
        "There must be at least one RBEnumMap passed in");
    // This will be the same for all maps (compiler-enforced), but let's play it extra safe.
    Class<E> sharedEnumClass = checkAllSame(
        mapsList,
        v -> v.getEnumClass());
    MutableRBEnumMap<E, V2> mutableMap = newMutableRBEnumMap(sharedEnumClass);
    RBSet<E> allKeys = RBSets.union(
        mapsList.stream().map(rBEnumMap -> newRBSet(rBEnumMap.keySet())).iterator());

    allKeys.forEach(key ->
        mutableMap.putAssumingAbsent(key, mergeFunction.apply(
            key,
            filterPresentOptionalsInStream(mapsList
                .stream()
                .map(rBEnumMap -> rBEnumMap.getOptional(key)))
                .collect(Collectors.toList()))));
    return newRBEnumMap(mutableMap);
  }

  /**
   * Merges two maps into a single one, but also transforms the values into a possibly different type.
   *
   * For the 3 separate cases of a key appearing in the left map / right map / both maps,
   * modify the values based on the functions passed in.
   *
   * @see IidMapMergers#mergeIidMapsByTransformedEntry
   */
  public static <E extends Enum<E>, V1, V2, V3> RBEnumMap<E, V3> mergeRBEnumMapsByTransformedValue(
      BiFunction<V1, V2, V3> mergeFunction,
      Function<V1, V3> onlyLeftPresent,
      Function<V2, V3> onlyRightPresent,
      RBEnumMap<E, V1> leftMap, RBEnumMap<E, V2> rightMap) {
    Class<E> sharedEnumClass = checkBothSame(
        leftMap.getEnumClass(),
        rightMap.getEnumClass(),
        "Internal error; enum classes must be the same; this should have been caught by the compiler: %s %s",
        leftMap, rightMap);

    return newRBEnumMap(sharedEnumClass, newRBSet(Sets.union(leftMap.keySet(), rightMap.keySet()))
        .toRBMap(key -> {
          Optional<V1> leftItem = leftMap.getOptional(key);
          Optional<V2> rightItem = rightMap.getOptional(key);
          return !leftItem.isPresent() ? onlyRightPresent.apply(rightItem.get()) :
              !rightItem.isPresent() ? onlyLeftPresent.apply(leftItem.get()) :
                  mergeFunction.apply(leftItem.get(), rightItem.get());
        }));
  }

  /**
   * Merges a bunch of maps into a single one.
   *
   * <p> In the event a key appears in more than one map, apply a binary operator (e.g. '+') to merge
   * the values. This doesn't *assume* that the operator is commutative, but it probably should be.
   * Otherwise, your result will depend on the order you pass the maps in the stream, which will be confusing. </p>
   */
  public static <E extends Enum<E>, V> RBEnumMap<E, V> mergeRBEnumMapsByValue(
      BinaryOperator<V> mergeFunction, Stream<RBEnumMap<E, V>> mapsStream) {
    Pointer<MutableRBEnumMap<E, V>> mutableMapPointer = uninitializedPointer();
    mapsStream
        .flatMap(map -> {
          // This trickery with the side effect (which we rarely use) is required so that we won't have to pass in
          // the enum class, and will instead get it off of the first RBEnumMap object. This means that the stream
          // passed in must have at least one map in it.
          if (!mutableMapPointer.isInitialized()) {
            mutableMapPointer.setAssumingUninitialized(newMutableRBEnumMap(map.getEnumClass()));
          }
          return map.entrySet().stream();
        })
        .forEach(entry -> {
          E key = entry.getKey();
          V value = entry.getValue();
          MutableRBEnumMap<E, V> mutableMap = mutableMapPointer.getOrThrow();
          mutableMap.put(key, mutableMap.containsKey(key)
              ? mergeFunction.apply(mutableMap.getOrThrow(key), value)
              : value);
        });
    RBPreconditions.checkArgument(
        mutableMapPointer.isInitialized(),
        "mergeRBEnumMapsByValue must be called with a stream of at least one map");
    return newRBEnumMap(mutableMapPointer.getOrThrow());
  }

  /**
   * For two maps of RBNumeric, multiply each corresponding item, and return the sum of those terms,
   * kind of like a vector dot product.
   *
   * The keys are expected to be the same in each map.
   *
   */
  public static <E extends Enum<E>, V1 extends RBNumeric<? super V1>, V2 extends RBNumeric<? super V2>> double dotProductOfRBEnumMaps(
      RBEnumMap<E, V1> map1, RBEnumMap<E, V2> map2) {
    checkBothSame(
        map1.size(),
        map2.size(),
        "We can only merge maps with the same keys, which implies same # of entries: %s %s",
        map1, map2);

    double sum = 0;
    for(Map.Entry<E, V1> entry : map1.entrySet()) {
      E key = entry.getKey();
      V1 value1 = entry.getValue();
      // We don't need to check for the *keys* to be the same. If we iterate over 'size' items of map1, and all of them
      // appear in map2 (hence the getOrThrow, which will throw otherwise), then there's no way map2 could have an entry
      // for a key that doesn't appear in map1, since their sizes are the same.
      sum += value1.doubleValue() * map2.getOrThrow(key).doubleValue();
    }
    return sum;
  }

}
