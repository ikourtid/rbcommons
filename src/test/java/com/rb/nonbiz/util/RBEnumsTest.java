package com.rb.nonbiz.util;

public class RBEnumsTest {

  /**
   * This is handy for tests to indicate a dummy value for a specific enum class.
   *
   * <p> Instead of having test code that says
   * {@code foo(ALLOW_WASH_SALES); // dummy }
   * you can use the (arguably) clearer
   * {@code foo(dummyEnumValue(WashSalePreventionStrategy.class)); }</p>
   */
  public static <E extends Enum<E>> E dummyEnumValue(Class<E> clazz) {
    return clazz.getEnumConstants()[0];
  }

}
