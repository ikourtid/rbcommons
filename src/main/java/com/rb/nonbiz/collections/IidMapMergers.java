package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidMapVisitors.TwoIidMapsVisitor;
import com.rb.nonbiz.functional.QuadriFunction;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.IidMapVisitors.visitInstrumentsOfTwoIidMaps;
import static com.rb.nonbiz.collections.IidSetOperations.unionOfIidSets;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBOptionals.filterPresentOptionalsInStream;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRest;

public class IidMapMergers {

  /**
   * Merges a bunch of maps into a single one.
   * Throws if a key appears in more than one map.
   */
  @SafeVarargs
  public static <V> IidMap<V> mergeIidMapsDisallowingOverlap(IidMap<V> first, IidMap<V> second, IidMap<V>...rest) {
    Optional<IidMap<V>> onlyNonEmptyIidMap = IidMaps.getWhenAtMostOneIidMapIsNonEmpty(first, second, rest);
    if (onlyNonEmptyIidMap.isPresent()) {
      return onlyNonEmptyIidMap.get(); // small performance optimization
    }
    return mergeIidMapsDisallowingOverlap(concatenateFirstSecondAndRest(first, second, rest).iterator());
  }

  /**
   * Merges a bunch of maps into a single one.
   * Throws if a key appears in more than one map.
   */
  public static <V> IidMap<V> mergeIidMapsDisallowingOverlap(Iterator<IidMap<V>> mapsIterator) {
    MutableIidMap<V> mutableMap = newMutableIidMap();
    mapsIterator
        .forEachRemaining(map -> map.forEachEntry(
            (instrumentId, value) -> mutableMap.putAssumingAbsent(instrumentId, value)));
    return newIidMap(mutableMap);
  }

  /**
   * Merges a bunch of maps into a single one.
   * Throws if a key appears in more than one map, unless the items we are trying to insert are 'similar'
   * (based on the predicate passed in).
   * Since this similarity is not the same as equality, this means that if e.g. the first IidMap maps
   * STOCK_A to money(100), the second one (in the iterator) maps STOCK_A to money(100 + 1e-9),
   * and the third one to money(100 - 1e-9),
   * and similarity is 'values similar to an epsilon of 1e-8', then the resulting IidMap will show money(100)
   * in the final value for STOCK_A, NOT money(100 + 1e-9) or money(100 - 1e-9)
   */
  public static <V> IidMap<V> mergeIidMapsAllowingOverlapOnSimilarItemsOnly(
      Iterator<IidMap<V>> mapsIterator, BiPredicate<V, V> itemsAreSimilar) {
    MutableIidMap<V> mutableMap = newMutableIidMap();
    mapsIterator
        .forEachRemaining(map -> map.forEachEntry(
            (instrumentId, newValue) -> {
              mutableMap.getOptional(instrumentId)
                  .ifPresent(existingValue -> RBPreconditions.checkArgument(
                      itemsAreSimilar.test(existingValue, newValue),
                      "We do not allow overlap in the IidMaps when items are dissimilar: %s has %s but trying to put %s",
                      instrumentId, existingValue, newValue));
              mutableMap.putIfAbsent(instrumentId, newValue);
            }));
    return newIidMap(mutableMap);
  }

  /**
   * Merges two IidMaps into one, assuming that the keys are the same in both maps.
   * @see IidMapMergers#mergeIidMapsByTransformedValue
   */
  public static <V, V1, V2> IidMap<V> mergeIidMapsByTransformedValueAssumingFullOverlap(
      IidMap<V1> leftMap, IidMap<V2> rightMap, BiFunction<V1, V2, V> mergeFunction) {
    return mergeIidMapsByTransformedValue(
        mergeFunction,
        inLeftOnly -> { throw new IllegalArgumentException(""); },
        inRightOnly -> { throw new IllegalArgumentException(""); },

        leftMap,
        rightMap);
  }

  @SafeVarargs
  public static <V> IidMap<V> mergeIidMapsByValue(
      BinaryOperator<V> mergeFunction, IidMap<V> first, IidMap<V> second, IidMap<V> ... rest) {
    Optional<IidMap<V>> onlyNonEmptyIidMap = IidMaps.getWhenAtMostOneIidMapIsNonEmpty(first, second, rest);
    if (onlyNonEmptyIidMap.isPresent()) {
      return onlyNonEmptyIidMap.get(); // small performance optimization
    }
    int finalSize = first.size() + second.size();
    for (IidMap<V> restMap : rest) {
      finalSize += restMap.size();
    }
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(finalSize);
    concatenateFirstSecondAndRest(first, second, rest)
        .forEach(iidMap -> iidMap
            .forEachEntry( (instrumentId, value) ->
                mutableMap.put(instrumentId, mutableMap.containsKey(instrumentId)
                    ? mergeFunction.apply(mutableMap.getOrThrow(instrumentId), value)
                    : value)));
    return newIidMap(mutableMap);
  }

  /**
   * Merges two maps into a single one, but also transforms the values into a possibly different type.
   *
   * For the 3 separate cases of a key appearing in the left map / right map / both maps,
   * modify the values based on the functions passed in.
   *
   * This is similar to mergeIidMapsByValue, except that it applies:
   * - in cases where the maps have different types in their values
   * - for two maps only
   *
   * It is also similar to mergeRBMapsByTransformedValue, but it applies to IidMaps (not RBMaps),
   * and also it allows the two input maps to have values of different types.
   *
   * @see IidMapMergers#mergeIidMapsByTransformedValue
   * @see RBMapMergers#mergeRBMapsByTransformedValue
   */
  public static <V, V1, V2> IidMap<V> mergeIidMapsByTransformedEntry(
      TriFunction<InstrumentId, V1, V2, V> mergeFunction,
      BiFunction<InstrumentId, V1, V> onlyLeftPresent,
      BiFunction<InstrumentId, V2, V> onlyRightPresent,
      IidMap<V1> leftMap, IidMap<V2> rightMap) {
    // This is just an estimate; it doesn't have to be exact. Of course, if an instrument appears in both maps,
    // this will be an overestimate.
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(leftMap.size() + rightMap.size());
    visitInstrumentsOfTwoIidMaps(
        leftMap,
        rightMap,
        new TwoIidMapsVisitor<V1, V2>() {
          @Override
          public void visitInstrumentInLeftMapOnly(InstrumentId keyInLeftMapOnly, V1 valueInLeftMapOnly) {
            mutableMap.putAssumingAbsent(keyInLeftMapOnly, onlyLeftPresent.apply(keyInLeftMapOnly, valueInLeftMapOnly));
          }

          @Override
          public void visitInstrumentInRightMapOnly(InstrumentId keyInRightMapOnly, V2 valueInRightMapOnly) {
            mutableMap.putAssumingAbsent(keyInRightMapOnly, onlyRightPresent.apply(keyInRightMapOnly, valueInRightMapOnly));
          }

          @Override
          public void visitInstrumentInBothMaps(InstrumentId keyInBothMaps, V1 valueInLeftMap, V2 valueInRightMap) {
            mutableMap.putAssumingAbsent(keyInBothMaps, mergeFunction.apply(keyInBothMaps, valueInLeftMap, valueInRightMap));
          }
        });
    return newIidMap(mutableMap);
  }

  /**
   * Merges three maps into a single one, but also transforms the values into a possibly different type.
   *
   * Unlike mergeIidMapsByTransformedEntry, there are too many cases of keys having values or not (2 ^ 3 - 1)
   * to handle with separate lambdas. Therefore, we'll handle all of these with a lambda that takes optionals
   * based on whether values exist or not, for each respective map.
   *
   * @see IidMapMergers#mergeIidMapsByTransformedValue
   */
  public static <V, V1, V2, V3> IidMap<V> mergeThreeIidMapsByTransformedEntry(
      QuadriFunction<InstrumentId, Optional<V1>, Optional<V2>, Optional<V3>, V> mergeFunction,
      IidMap<V1> map1,
      IidMap<V2> map2,
      IidMap<V3> map3) {
    // There's probably a more efficient way to do this, but this is general and simple enough:
    IidSet allKeys = unionOfIidSets(map1.keySet(), map2.keySet(), map3.keySet());
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(allKeys.size());
    allKeys.forEach(instrumentId -> mutableMap.putAssumingAbsent(
        instrumentId,
        mergeFunction.apply(
            instrumentId,
            map1.getOptional(instrumentId),
            map2.getOptional(instrumentId),
            map3.getOptional(instrumentId))));
    return newIidMap(mutableMap);
  }

  /**
   * Just like mergeIidMapsByTransformedEntry, except that the lambdas you pass in
   * don't take the key in as a parameter. This is a simpler alternative for those cases where
   * your transformations do not rely on the map key.
   */
  public static <V, V1, V2> IidMap<V> mergeIidMapsByTransformedValue(
      BiFunction<V1, V2, V> mergeFunction,
      Function<V1, V> onlyLeftPresent,
      Function<V2, V> onlyRightPresent,
      IidMap<V1> leftMap,
      IidMap<V2> rightMap) {
    return mergeIidMapsByTransformedEntry(
        (instrumentId, v1, v2) -> mergeFunction.apply(v1, v2),
        (instrumentId, v1) -> onlyLeftPresent.apply(v1),
        (instrumentId, v2) -> onlyRightPresent.apply(v2),
        leftMap,
        rightMap);
  }

  /**
   * Just like mergeIidMapsByTransformedEntry, except that the lambdas you pass in
   * don't take the key in as a parameter. This is a simpler alternative for those cases where
   * your transformations do not rely on the map key.
   */
  public static <V, V1, V2, V3> IidMap<V> mergeThreeIidMapsByTransformedValue(
      TriFunction<Optional<V1>, Optional<V2>, Optional<V3>, V> mergeFunction,
      IidMap<V1> map1,
      IidMap<V2> map2,
      IidMap<V3> map3) {
    return mergeThreeIidMapsByTransformedEntry(
        (ignoredInstrumentId, v1, v2, v3) -> mergeFunction.apply(v1, v2, v3),
        map1, map2, map3);
  }

  /**
   * Merges a bunch of maps into a single one.
   *
   * In the event a key appears in more than one map, apply a binary operator (e.g. '+') to merge
   * the values. This doesn't *assume* that the operator is commutative, but it probably should be.
   * Otherwise, your result will depend in the order you pass the maps in the stream, which will be confusing.
   */
  public static <V> IidMap<V> mergeIidMapsByValue(BinaryOperator<V> mergeFunction, Stream<IidMap<V>> mapsStream) {
    MutableIidMap<V> mutableMap = newMutableIidMap();
    mapsStream
        .forEach(iidMap -> iidMap.forEachEntry( (instrumentId, value) ->
            mutableMap.put(instrumentId, mutableMap.containsKey(instrumentId)
                ? mergeFunction.apply(mutableMap.getOrThrow(instrumentId), value)
                : value)));
    return newIidMap(mutableMap);
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
  public static <V1, V2> IidMap<V2> mergeIidMapsByTransformedValue(
      BiFunction<InstrumentId, List<V1>, V2> mergeFunction,
      List<IidMap<V1>> mapsList) {
    IidSet allInstruments = unionOfIidSets(
        mapsList.stream().map(iidMap -> iidMap.keySet()).iterator(),
        // Note that if we use as a size hint the total # of instruments in each map, i.e.
        // mapsList.stream().mapToInt(v -> v.size()).sum();
        // then we'd be doublecounting some instruments. Let's just use a somewhat conservative size hint.
        mapsList.size() * 10);

    MutableIidMap<V2> mutableMap = newMutableIidMapWithExpectedSize(allInstruments.size());
    allInstruments.forEach(instrumentId ->
        mutableMap.putAssumingAbsent(instrumentId, mergeFunction.apply(
            instrumentId,
            filterPresentOptionalsInStream(mapsList
                .stream()
                .map(iidMap -> iidMap.getOptional(instrumentId)))
                .collect(Collectors.toList()))));
    return newIidMap(mutableMap);
  }

  /**
   * Merges two maps into a single one.
   *
   * For the 3 separate cases of a key appearing in the left map / right map / both maps,
   * modify the values based on the functions passed in.
   */
  public static <V> IidMap<V> mergeIidMapsByValue(
      BinaryOperator<V> mergeFunction,
      UnaryOperator<V> onlyLeftPresent,
      UnaryOperator<V> onlyRightPresent,
      IidMap<V> leftMap,
      IidMap<V> rightMap) {
    int sizeHint = leftMap.size() + rightMap.size();
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(sizeHint);
    visitInstrumentsOfTwoIidMaps(
        leftMap,
        rightMap,
        new TwoIidMapsVisitor<V, V>() {
          @Override
          public void visitInstrumentInLeftMapOnly(InstrumentId keyInLeftMapOnly, V valueInLeftMapOnly) {
            mutableMap.putAssumingAbsent(keyInLeftMapOnly, onlyLeftPresent.apply(valueInLeftMapOnly));
          }

          @Override
          public void visitInstrumentInRightMapOnly(InstrumentId keyInRightMapOnly, V valueInRightMapOnly) {
            mutableMap.putAssumingAbsent(keyInRightMapOnly, onlyRightPresent.apply(valueInRightMapOnly));
          }

          @Override
          public void visitInstrumentInBothMaps(InstrumentId keyInBothMaps, V valueInLeftMap, V valueInRightMap) {
            mutableMap.putAssumingAbsent(keyInBothMaps, mergeFunction.apply(valueInLeftMap, valueInRightMap));
          }
        });
    return newIidMap(mutableMap);
  }

}
