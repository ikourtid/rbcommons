package com.rb.nonbiz.util;

import com.rb.nonbiz.collections.MutableRBMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBMapConstructors;
import com.rb.nonbiz.collections.RBSet;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromStream;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;

public class RBEnumMaps {

  /**
   * Transforms an EnumMap's map values, without looking at the enum value itself.
   */
  public static <E extends Enum<E>, V1, V2> EnumMap<E, V2> transformEnumMap(
      EnumMap<E, V1> original, Function<V1, V2> transformer) {
    return transformEnumMap(original, (ignoredEnumValue, v) -> transformer.apply(v));
  }

  /**
   * Transforms an EnumMap's map values, but the transformation can also look at the enum value as well.
   */
  public static <E extends Enum<E>, V1, V2> EnumMap<E, V2> transformEnumMap(
      EnumMap<E, V1> original, BiFunction<E, V1, V2> transformer) {
    MutableRBMap<E, V2> mutableMap = newMutableRBMapWithExpectedSize(original.size());
    original.forEach( (enumValue, valueInMap) -> mutableMap.put(enumValue, transformer.apply(enumValue, valueInMap)));
    return new EnumMap<E, V2>(mutableMap.asMap());
  }

  /**
   * Creates an EnumMap using the convenient RBMap constructors, while also asserting that every enum constant
   * appears in the map.
   */
  public static <E extends Enum<E>, V> EnumMap<E, V> enumMapCoveringAllEnumValues(
      Class<E> enumClass,
      RBMap<E, V> valuesMap) {
    RBPreconditions.checkArgument(enumClass.isEnum());
    RBSet<E> enumValues = newRBSet(enumClass.getEnumConstants());
    RBSimilarityPreconditions.checkBothSame(
        newRBSet(valuesMap.keySet()),
        enumValues,
        "All values for enum %s must appear in the map: enum has %s ; map is %s",
        enumClass, enumValues, valuesMap);
    return new EnumMap<E, V>(valuesMap.asMap());
  }

  /**
   * Creates an EnumMap such that every enum constant appears in the map.
   * Each value is a function of the key specified.
   */
  public static <E extends Enum<E>, V> EnumMap<E, V> enumMapCoveringAllEnumValues(
      Class<E> enumClass,
      Function<E, V> valueGenerator) {
    RBPreconditions.checkArgument(enumClass.isEnum());
    return new EnumMap<E, V>(rbMapFromStream(
        Arrays.stream(enumClass.getEnumConstants()),
        key -> key,
        valueGenerator)
        .asMap());
  }

}
