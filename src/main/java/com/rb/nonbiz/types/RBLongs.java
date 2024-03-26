package com.rb.nonbiz.types;

import java.util.OptionalLong;

import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptionalLong;

/**
 * Various static utility methods pertaining to a plain Java @link Long}.
 */
public class RBLongs {

  /**
   * If the {@link OptionalLong} argument is present, return the max of the two longs; otherwise return the only
   * present long.
   */
  public static long maxAllowingOptionalLong(OptionalLong optionalValue1, long value2) {
    return transformOptionalLong(optionalValue1, value1 -> Math.max(value1, value2))
        .orElse(value2);
  }

  /**
   * If the {@link OptionalLong} argument is present, return the max of the two longs; otherwise return the only
   * present long.
   */
  public static long maxAllowingOptionalLong(long value1, OptionalLong optionalValue2) {
    return transformOptionalLong(optionalValue2, value2 -> Math.max(value1, value2))
        .orElse(value1);
  }

  /**
   * If the {@link OptionalLong} argument is present, return the min of the two longs; otherwise return the only
   * present long.
   */
  public static long minAllowingOptionalLong(OptionalLong optionalValue1, long value2) {
    return transformOptionalLong(optionalValue1, value1 -> Math.min(value1, value2))
        .orElse(value2);
  }

  /**
   * If the {@link OptionalLong} argument is present, return the min of the two longs; otherwise return the only
   * present long.
   */
  public static long minAllowingOptionalLong(long value1, OptionalLong optionalValue2) {
    return transformOptionalLong(optionalValue2, value2 -> Math.min(value1, value2))
        .orElse(value1);
  }

}
