package com.rb.nonbiz.util;

import com.rb.nonbiz.collections.MutableRBMap;

import java.util.EnumMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;

public class RBEnumMaps {

  /**
   * Transforms an EnumMap's map values, without looking at the enum value itself.
   */
  public static <E extends Enum<E>, V1, V2> EnumMap<E, V2> transformEnumMap(
      EnumMap<E, V1> original, Function<V1, V2> transformer) {
    return transformEnumMap(original, (ignoredEnumValue, v) -> transformer.apply(v));
  }

  /**
   * Transforms an EnumMap's map values, without looking at the enum value itself.
   */
  public static <E extends Enum<E>, V1, V2> EnumMap<E, V2> transformEnumMap(
      EnumMap<E, V1> original, BiFunction<E, V1, V2> transformer) {
    MutableRBMap<E, V2> mutableMap = newMutableRBMapWithExpectedSize(original.size());
    original.forEach( (enumValue, valueInMap) -> mutableMap.put(enumValue, transformer.apply(enumValue, valueInMap)));
    return new EnumMap<E, V2>(mutableMap.asMap());
  }

}
