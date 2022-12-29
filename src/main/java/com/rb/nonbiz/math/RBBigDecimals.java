package com.rb.nonbiz.math;

import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.RBDoubles.EpsilonComparisonVisitor;

import java.math.BigDecimal;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.types.RBDoubles.epsilonCompareDoubles;

public class RBBigDecimals {

  private static final BigDecimal ONE_HALF = new BigDecimal("0.5");

  public static BigDecimal bigDecimalMax(BigDecimal bd1, BigDecimal bd2) {
    return bd1.compareTo(bd2) >= 0 ? bd1 : bd2;
  }

  public static BigDecimal bigDecimalMin(BigDecimal bd1, BigDecimal bd2) {
    return bd1.compareTo(bd2) <= 0 ? bd1 : bd2;
  }

  public static BigDecimal bigDecimalInvert(BigDecimal bd) {
    return BigDecimal.ONE.divide(bd, DEFAULT_MATH_CONTEXT);
  }

  public static BigDecimal bigDecimalAverage(BigDecimal bd1, BigDecimal bd2) {
    // The parentheses are unnecessary, but they make things clearer.
    return (bd1.add(bd2)).multiply(ONE_HALF);
  }

  /**
   * This method gives an abstraction over the Comparable::compareTo, which returns negative / 0 / positive,
   * and also lets you do a comparison subject to an epsilon.
   */
  public static <T> T epsilonCompareBigDecimals(
      BigDecimal left, BigDecimal right, Epsilon epsilon, EpsilonComparisonVisitor<T> visitor) {
    // Since these comparisons are subject to an epsilon, we don't really need to worry about the loss of precision
    // in going from a BigDecimal to a double.
    return epsilonCompareDoubles(left.doubleValue(), right.doubleValue(), epsilon, visitor);
  }

}
