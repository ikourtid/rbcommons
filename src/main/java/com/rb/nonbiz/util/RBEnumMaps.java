package com.rb.nonbiz.util;

import com.rb.nonbiz.collections.MutableRBEnumMap;
import com.rb.nonbiz.collections.RBEnumMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.rb.nonbiz.collections.MutableRBEnumMap.newMutableRBEnumMap;
import static com.rb.nonbiz.collections.RBEnumMap.newRBEnumMap;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromStream;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.emptyRBEnumMap;
import static com.rb.nonbiz.util.RBSimilarityPreconditions.checkAllSame;

public class RBEnumMaps {

  /**
   * Transforms an {@link RBEnumMap}'s map values, without looking at the enum value itself.
   */
  public static <E extends Enum<E>, V1, V2> RBEnumMap<E, V2> transformRBEnumMap(
      RBEnumMap<E, V1> original, Function<V1, V2> transformer) {
    return transformRBEnumMap(original, (ignoredEnumValue, v) -> transformer.apply(v));
  }

  /**
   * Transforms an {@link RBEnumMap}'s map values, but the transformation can also look at the enum value as well.
   */
  public static <E extends Enum<E>, V1, V2> RBEnumMap<E, V2> transformRBEnumMap(
      RBEnumMap<E, V1> original, BiFunction<E, V1, V2> transformer) {
    MutableRBEnumMap<E, V2> mutableMap = newMutableRBEnumMap(original.getEnumClass());
    original.forEachEntryInKeyOrder( (enumValue, valueInMap) ->
        mutableMap.put(enumValue, transformer.apply(enumValue, valueInMap)));
    return newRBEnumMap(mutableMap);
  }

  /**
   * Creates an {@link RBEnumMap} using the convenient {@link RBMap} constructors,
   * while also asserting that every enum constant appears in the map.
   */
  public static <E extends Enum<E>, V> RBEnumMap<E, V> rbEnumMapCoveringAllEnumValues(
      Class<E> enumClass,
      RBMap<E, V> valuesMap) {
    RBPreconditions.checkArgument(enumClass.isEnum());
    RBSet<E> enumValues = newRBSet(enumClass.getEnumConstants());
    RBSimilarityPreconditions.checkBothSame(
        newRBSet(valuesMap.keySet()),
        enumValues,
        "All values for enum %s must appear in the map: enum has %s ; map is %s",
        enumClass, enumValues, valuesMap);
    return newRBEnumMap(enumClass, valuesMap);
  }

  /**
   * Creates an {@link RBEnumMap} such that every enum constant appears in the map.
   * Each value is a function of the key specified.
   */
  public static <E extends Enum<E>, V> RBEnumMap<E, V> rbEnumMapCoveringAllEnumValues(
      Class<E> enumClass,
      Function<E, V> valueGenerator) {
    RBPreconditions.checkArgument(enumClass.isEnum());
    return newRBEnumMap(enumClass, rbMapFromStream(
        Arrays.stream(enumClass.getEnumConstants()),
        key -> key,
        valueGenerator));
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
  public static <E extends Enum<E>, V> Optional<RBEnumMap<E, V>> getWhenUpToOneRBEnumMapIsNonEmpty(
      RBEnumMap<E, V> first, RBEnumMap<E, V> second, RBEnumMap<E, V>...rest) {
    // This will be the same for all maps (compiler-enforced), but let's play it extra safe.
    Class<E> sharedEnumClass = checkAllSame(
        concatenateFirstSecondAndRest(first, second, rest).iterator(),
        v -> v.getEnumClass());

    RBEnumMap<E, V> onlyNonEmptyMap = null;
    if (!first.isEmpty()) {
      onlyNonEmptyMap = first;
    }
    if (!second.isEmpty()) {
      if (onlyNonEmptyMap != null) { // i.e. if we already saw a non-empty map
        return Optional.empty();
      }
      onlyNonEmptyMap = second;
    }
    for (RBEnumMap<E, V> restMap : rest) {
      if (!restMap.isEmpty()) {
        if (onlyNonEmptyMap != null) { // i.e. if we already saw a non-empty map
          return Optional.empty();
        }
        onlyNonEmptyMap = restMap;
      }
    }
    return Optional.of(onlyNonEmptyMap == null
        ? emptyRBEnumMap(sharedEnumClass)
        : onlyNonEmptyMap);
  }

}
