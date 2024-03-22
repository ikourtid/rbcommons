package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.RBMapVisitors.PairOfRBSetAndRBMapVisitor;
import com.rb.nonbiz.collections.RBMapVisitors.TwoRBMapsVisitor;
import com.rb.nonbiz.functional.QuadriConsumer;
import com.rb.nonbiz.functional.TriConsumer;

import java.util.Optional;
import java.util.function.BiConsumer;

import static com.rb.nonbiz.collections.IidSetOperations.unionOfIidSets;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

/**
 * Support for visiting {@link IidMap} objects - possible multiple ones.
 *
 * <p> This it to help focus on what to do on each case (e.g. an object appears on only the 1st, or only the 2nd, or
 * both maps) by only having to supply a lambda for each case, without having to do if/then/else-type logic. </p>
 */
public class IidMapVisitors {

  /**
   * Useful when you are iterating over 2 maps, and want to cleanly separate out the behavior when an item
   * appears only on the left, only on the right, or on both maps.
   *
   * You could always just do this with a bunch of ifs, but this is cleaner and more functional-looking.
   *
   * @see TwoRBMapsVisitor
   */
  public interface TwoIidMapsVisitor<VL, VR> {

    void visitInstrumentInLeftMapOnly(InstrumentId keyInLeftMapOnly, VL valueInLeftMapOnly);
    void visitInstrumentInRightMapOnly(InstrumentId keyInRightMapOnly, VR valueInRightMapOnly);
    void visitInstrumentInBothMaps(InstrumentId keyInBothMaps, VL valueInLeftMap, VR valueInRightMap);

  }

  /**
   * Useful when you are iterating a set and a map, and want to cleanly separate out the behavior when an instrument
   * appears only on the left set, only on the right map, or in both places.
   *
   * You could always just do this with a bunch of ifs, but this is cleaner and more functional-looking.
   *
   * @see TwoIidMapsVisitor
   * @see PairOfRBSetAndRBMapVisitor
   */
  public interface PairOfIidSetAndIidMapVisitor<V> {

    void visitInstrumentInSetOnly(InstrumentId keyInSetOnly);
    void visitInstrumentInMapOnly(InstrumentId keyInMapOnly, V value);
    void visitInstrumentInBothSetAndMap(InstrumentId keyInBothSetAndMap, V value);

  }

  /**
   * When you have 2 IidMaps, this lets you perform 3 different actions for items that are only in the left map,
   * only in the right map, or in both maps.
   *
   * It lets you specify a bit more cleanly (via the TwoIidMapsVisitor) what to do in those 3 different cases.
   *
   * @see RBMapVisitors#visitItemsOfTwoRBMaps
   */
  public static <VL, VR> void visitInstrumentsOfTwoIidMaps(
      IidMap<VL> leftMap,
      IidMap<VR> rightMap,
      TwoIidMapsVisitor<VL, VR> visitor) {
    leftMap.forEachEntry( (leftKey, leftValue) -> {
      Optional<VR> maybeRightValue = rightMap.getOptional(leftKey);
      if (maybeRightValue.isPresent()) {
        visitor.visitInstrumentInBothMaps(leftKey, leftValue, maybeRightValue.get());
      } else {
        visitor.visitInstrumentInLeftMapOnly(leftKey, leftValue);
      }
    });
    rightMap.forEachEntry( (rightKey, rightValue) -> {
      if (!leftMap.containsKey(rightKey)) {
        visitor.visitInstrumentInRightMapOnly(rightKey, rightValue);
      }
    });
  }

  /**
   * When you have 3 IidMaps, this lets you perform 7 = 2 ^ 3 - 1 different actions for items based on whether items exist
   * in the {1st, 2nd, 3rd} map, respectively. It's not possible for an item to exist in NO map, by definition,
   * since we're looking at the set union of all InstrumentId keys from all 3 maps. Hence the minus 1 in the formula above.
   *
   * It lets you specify a bit more cleanly what to do in those 3 different cases.
   *
   * @see RBMapVisitors#visitItemsOfTwoRBMaps
   */
  public static <V1, V2, V3> void visitInstrumentsOfThreeIidMaps(
      IidMap<V1> map1,
      IidMap<V2> map2,
      IidMap<V3> map3,
      QuadriConsumer<InstrumentId, Optional<V1>, Optional<V2>, Optional<V3>> quadriConsumer) {
    // There's probably a more efficient way to do this, but this is general and simple enough:
    IidSet allKeys = unionOfIidSets(map1.keySet(), map2.keySet(), map3.keySet());

    allKeys.forEach(instrumentId -> quadriConsumer.accept(
        instrumentId,
        map1.getOptional(instrumentId),
        map2.getOptional(instrumentId),
        map3.getOptional(instrumentId)));
  }

  /**
   * Like visitInstrumentsOfTwoIidMaps, except that it only cares about shared instruments.
   */
  public static <VL, VR> void visitSharedInstrumentsOfTwoIidMaps(
      IidMap<VL> leftMap,
      IidMap<VR> rightMap,
      BiConsumer<VL, VR> biConsumer) {
    leftMap.forEachEntry( (leftKey, leftValue) ->
        rightMap.getOptional(leftKey).ifPresent(rightValue ->
            biConsumer.accept(leftValue, rightValue)));
  }

  /**
   * Like visitInstrumentsOfTwoIidMaps, except that it only cares about shared instruments.
   */
  public static <VL, VR> void visitSharedInstrumentsOfTwoIidMaps(
      IidMap<VL> leftMap,
      IidMap<VR> rightMap,
      TriConsumer<InstrumentId, VL, VR> triConsumer) {
    leftMap.forEachEntry( (leftKey, leftValue) ->
        rightMap.getOptional(leftKey).ifPresent(rightValue ->
            triConsumer.accept(leftKey, leftValue, rightValue)));
  }

  /**
   * When you have 2 IidMaps with the same keys (throws otherwise),
   * this lets you perform an action for the corresponding values in the 2 maps.
   *
   * @see IidMapVisitors#visitInstrumentsOfTwoIidMaps
   */
  public static <VL, VR> void visitInstrumentsOfTwoIidMapsAssumingFullOverlap(
      IidMap<VL> leftMap,
      IidMap<VR> rightMap,
      TriConsumer<InstrumentId, VL, VR> triConsumer) {
    leftMap.forEachEntry( (leftKey, leftValue) -> {
      Optional<VR> maybeRightValue = rightMap.getOptional(leftKey);
      if (maybeRightValue.isPresent()) {
        triConsumer.accept(leftKey, leftValue, maybeRightValue.get());
      } else {
        throw new IllegalArgumentException(smartFormat("%s appears in left map only: %s vs %s",
            leftKey, leftMap, rightMap));
      }
    });
    rightMap.forEachEntry( (rightKey, rightValue) -> {
      if (!leftMap.containsKey(rightKey)) {
        throw new IllegalArgumentException(smartFormat("%s appears in right map only: %s vs %s",
            rightKey, leftMap, rightMap));
      }
    });
  }

  /**
   * When you have 2 IidMaps and the keys of the first are a subset of the keys
   * of the second, this lets you perform one action for the entries that appear in both,
   * or a different action for the entries that only appear in the second map.
   * Note: if the keys of the two maps match, then the first is still technically
   * a "subset" of the second, and this method will not throw an exception.
   */
  public static <VL, VR> void visitInstrumentsOfTwoIidMapsAssumingSubset(
      IidMap<VL> leftMap,
      IidMap<VR> rightMap,
      TriConsumer<InstrumentId, VL, VR> inBothConsumer,
      BiConsumer<InstrumentId, VR> rightOnlyConsumer) {
    leftMap.forEachEntry( (leftKey, leftValue) -> {
      VR rightValue = rightMap.getOrThrow(leftKey, "%s appears in left map only: %s vs %s", leftKey, leftMap, rightMap);
      inBothConsumer.accept(leftKey, leftValue, rightValue);
    });
    rightMap.forEachEntry( (rightKey, rightValue) -> {
      if (!leftMap.containsKey(rightKey)) {
        rightOnlyConsumer.accept(rightKey, rightValue);
      }
    });
  }

  /**
   * When you have a set and a map, this lets you perform 3 different actions for items that are only in the set,
   * only keys in the map, or both.
   *
   * It lets you specify a bit more cleanly (via the PairOfIidSetAndIidMapVisitor) what to do in those 3 different cases.
   *
   * @see IidMapVisitors#visitInstrumentsOfTwoIidMaps
   */
  public static <V> void visitItemsOfIidSetAndIidMap(IidSet set,
                                                     IidMap<V> map,
                                                     PairOfIidSetAndIidMapVisitor<V> visitor) {
    map.forEachEntry( (key, value) -> {
      if (set.contains(key)) {
        visitor.visitInstrumentInBothSetAndMap(key, value);
      } else {
        visitor.visitInstrumentInMapOnly(key, value);
      }
    });
    set.forEach( key -> {
      if (!map.containsKey(key)) {
        visitor.visitInstrumentInSetOnly(key);
      }
    });
  }

}
