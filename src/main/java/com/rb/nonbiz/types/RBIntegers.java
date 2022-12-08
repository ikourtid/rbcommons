package com.rb.nonbiz.types;

import java.util.OptionalInt;

import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptionalInt;

/**
 * Various utilities pertaining to {@link Integer}.
 */
public class RBIntegers {

  /**
   * If the {@link OptionalInt} argument is present, return the max of the two ints; otherwise return the only
   * present int.
   */
  public static int maxAllowingOptionalInt(OptionalInt optionalValue1, int value2) {
    return transformOptionalInt(optionalValue1, value1 -> Math.max(value1, value2))
        .orElse(value2);
  }

  /**
   * If the {@link OptionalInt} argument is present, return the max of the two ints; otherwise return the only
   * present int.
   */
  public static int maxAllowingOptionalInt(int value1, OptionalInt optionalValue2) {
    return transformOptionalInt(optionalValue2, value2 -> Math.max(value1, value2))
        .orElse(value1);
  }

  /**
   * If the {@link OptionalInt} argument is present, return the min of the two ints; otherwise return the only
   * present int.
   */
  public static int minAllowingOptionalInt(OptionalInt optionalValue1, int value2) {
    return transformOptionalInt(optionalValue1, value1 -> Math.min(value1, value2))
        .orElse(value2);
  }

  /**
   * If the {@link OptionalInt} argument is present, return the min of the two ints; otherwise return the only
   * present int.
   */
  public static int minAllowingOptionalInt(int value1, OptionalInt optionalValue2) {
    return transformOptionalInt(optionalValue2, value2 -> Math.min(value1, value2))
        .orElse(value1);
  }

}
