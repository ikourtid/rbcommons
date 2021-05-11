package com.rb.nonbiz.collections;

import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.Optional;
import java.util.function.BiConsumer;

public class RBMapVisitors {

  /**
   * Useful when you are iterating over 2 maps, and want to cleanly separate out the behavior when an item
   * appears only on the left, only on the right, or on both maps.
   *
   * You could always just do this with a bunch of ifs, but this is cleaner and more functional-looking.
   *
   * @see PairOfRBSetAndRBMapVisitor
   */
  public interface TwoRBMapsVisitor<K, VL, VR> {

    void visitItemInLeftMapOnly(K keyInLeftMapOnly, VL valueInLeftMapOnly);
    void visitItemInRightMapOnly(K keyInRightMapOnly, VR valueInRightMapOnly);
    void visitItemInBothMaps(K keyInBothMaps, VL valueInLeftMap, VR valueInRightMap);

  }

  /**
   * Useful when you are iterating a set and a map, and want to cleanly separate out the behavior when an item
   * appears only on the left set, only on the right map, or in both places.
   *
   * You could always just do this with a bunch of ifs, but this is cleaner and more functional-looking.
   *
   * @see TwoRBMapsVisitor
   */
  public interface PairOfRBSetAndRBMapVisitor<K, V> {

    void visitItemInSetOnly(K keyInSetOnly);
    void visitItemInMapOnly(K keyInMapOnly, V value);
    void visitItemInBothSetAndMap(K keyInBothSetAndMap, V value);

  }

  /**
   * Like visitItemsOfTwoRBMaps, except that it only cares about shared instruments.
   */
  public static <K, VL, VR> void visitSharedItemsOfTwoRBMaps(
      RBMap<K, VL> leftMap,
      RBMap<K, VR> rightMap,
      BiConsumer<VL, VR> biConsumer) {
    leftMap.forEachEntry( (leftKey, leftValue) ->
        rightMap.getOptional(leftKey).ifPresent(rightValue ->
            biConsumer.accept(leftValue, rightValue)));
  }

  /**
   * Like visitItemsOfTwoRBMaps, except that it only cares about shared instruments.
   */
  public static <K, VL, VR> void visitSharedItemsOfTwoRBMaps(
      RBMap<K, VL> leftMap,
      RBMap<K, VR> rightMap,
      TriConsumer<K, VL, VR> triConsumer) {
    leftMap.forEachEntry( (leftKey, leftValue) ->
        rightMap.getOptional(leftKey).ifPresent(rightValue ->
            triConsumer.accept(leftKey, leftValue, rightValue)));
  }

  /**
   * When you have 2 maps, this lets you perform 3 different actions for items that are only in the left map,
   * only in the right map, or in both maps.
   *
   * It lets you specify a bit more cleanly (via the TwoRBMapsVisitor) what to do in those 3 different cases.
   *
   * @see RBMapVisitors#visitItemsOfRBSetAndRBMap
   */
  public static <K, VL, VR> void visitItemsOfTwoRBMaps(RBMap<K, VL> leftMap,
                                                       RBMap<K, VR> rightMap,
                                                       TwoRBMapsVisitor<K, VL, VR> visitor) {
    leftMap.forEachEntry( (leftKey, leftValue) -> {
      Optional<VR> maybeRightValue = rightMap.getOptional(leftKey);
      if (maybeRightValue.isPresent()) {
        visitor.visitItemInBothMaps(leftKey, leftValue, maybeRightValue.get());
      } else {
        visitor.visitItemInLeftMapOnly(leftKey, leftValue);
      }
    });
    rightMap.forEachEntry( (rightKey, rightValue) -> {
      if (!leftMap.containsKey(rightKey)) {
        visitor.visitItemInRightMapOnly(rightKey, rightValue);
      }
    });
  }

  /**
   * When you have a set and a map, this lets you perform 3 different actions for items that are only in the set,
   * only keys in the map, or both.
   *
   * It lets you specify a bit more cleanly (via the PairOfRBSetAndRBMapVisitor) what to do in those 3 different cases.
   */
  public static <K, V> void visitItemsOfRBSetAndRBMap(RBSet<K> set,
                                                      RBMap<K, V> map,
                                                      PairOfRBSetAndRBMapVisitor<K, V> visitor) {
    map.forEachEntry( (key, value) -> {
      if (set.contains(key)) {
        visitor.visitItemInBothSetAndMap(key, value);
      } else {
        visitor.visitItemInMapOnly(key, value);
      }
    });
    set.forEach( key -> {
      if (!map.containsKey(key)) {
        visitor.visitItemInSetOnly(key);
      }
    });
  }

  public static <K, V1, V2> void visitRBMapsExpectingSameKeys(
      RBMap<K, V1> map1,
      RBMap<K, V2> map2,
      TriConsumer<K, V1, V2> consumer) {
    RBSimilarityPreconditions.checkBothSame(
        map1.size(),
        map2.size(),
        "The two maps must have the same keys, but the map sizes are different: %s %s",
        map1, map2);
    map1.forEachEntry( (key, value1) -> {
      V2 value2 = map2.getOrThrow(key, "Key %s appers in left map but not right one: left= %s ; right= %s",
          key, map1, map2);
      consumer.accept(key, value1, value2);
    });
  }

}
