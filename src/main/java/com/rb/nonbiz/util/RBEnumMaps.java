package com.rb.nonbiz.util;

import com.rb.nonbiz.collections.MutableRBEnumMap;
import com.rb.nonbiz.collections.RBEnumMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.rb.nonbiz.collections.MutableRBEnumMap.newMutableRBEnumMap;
import static com.rb.nonbiz.collections.RBEnumMap.newRBEnumMap;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromStream;
import static com.rb.nonbiz.collections.RBSet.newRBSet;

public class RBEnumMaps {

  /**
   * Transforms an RBEnumMap's map values, without looking at the enum value itself.
   */
  public static <E extends Enum<E>, V1, V2> RBEnumMap<E, V2> transformRBEnumMap(
      RBEnumMap<E, V1> original, Function<V1, V2> transformer) {
    return transformRBEnumMap(original, (ignoredEnumValue, v) -> transformer.apply(v));
  }

  /**
   * Transforms an RBEnumMap's map values, but the transformation can also look at the enum value as well.
   */
  public static <E extends Enum<E>, V1, V2> RBEnumMap<E, V2> transformRBEnumMap(
      RBEnumMap<E, V1> original, BiFunction<E, V1, V2> transformer) {
    MutableRBEnumMap<E, V2> mutableMap = newMutableRBEnumMap(original.getEnumClass());
    original.forEachEntryInKeyOrder( (enumValue, valueInMap) ->
        mutableMap.put(enumValue, transformer.apply(enumValue, valueInMap)));
    return newRBEnumMap(mutableMap);
  }

  /**
   * Creates an RBEnumMap using the convenient RBMap constructors, while also asserting that every enum constant
   * appears in the map.
   */
  public static <E extends Enum<E>, V> RBEnumMap<E, V> enumMapCoveringAllEnumValues(
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
   * Creates an RBEnumMap such that every enum constant appears in the map.
   * Each value is a function of the key specified.
   */
  public static <E extends Enum<E>, V> RBEnumMap<E, V> enumMapCoveringAllEnumValues(
      Class<E> enumClass,
      Function<E, V> valueGenerator) {
    RBPreconditions.checkArgument(enumClass.isEnum());
    return newRBEnumMap(enumClass, rbMapFromStream(
        Arrays.stream(enumClass.getEnumConstants()),
        key -> key,
        valueGenerator));
  }

}
