package com.rb.nonbiz.types;

import com.rb.nonbiz.types.RBDoubles.EpsilonComparisonVisitor;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;

import static com.rb.nonbiz.types.RBDoubles.epsilonCompareDoubles;

/**
 * A set of utilities for working with {@link PreciseValue}s.
 *
 * @see PreciseValue
 */
public class PreciseValues {

  /**
   * This is a handy abstraction that lets you sort out the 3 cases where two numbers relate to each other,
   * subject to an epsilon.
   *
   * <p> It is clearer than {@code Comparable::compareTo}, which returns negative / 0 / positive,
   * and also lets you do a comparison subject to an epsilon. </p>
   *
   * @see EpsilonComparisonVisitor
   */
  public interface BigDecimalsEpsilonComparisonVisitor<T> {

    T visitAlmostEqual();
    T visitLeftIsGreater(BigDecimal rightMinusLeft);
    T visitRightIsGreater(BigDecimal rightMinusLeft);

  }

  /**
   * This method gives an abstraction over the {@code Comparable::compareTo}, which returns negative / 0 / positive,
   * by letting you do the comparison subject to an epsilon.
   *
   * @see #epsilonComparePreciseValuesAsDoubles
   */
  public static <T, P1 extends PreciseValue<? extends P1>, P2 extends PreciseValue<? extends P2>> T epsilonComparePreciseValues(
      P1 left, P2 right, double epsilon, BigDecimalsEpsilonComparisonVisitor<T> visitor) {
    RBPreconditions.checkArgument(
        epsilon > 0,
        "epsilon must be positive; was %s",
        epsilon);
    RBPreconditions.checkArgument(
        epsilon < 1e4,
        "even though it could be reasonable, we disallow a huge (> 1e4) epsilon for safety, since it's probably a typo; was %s",
        epsilon);
    BigDecimal rightMinusLeft = right.asBigDecimal().subtract(left.asBigDecimal());
    int signum = rightMinusLeft.signum();
    if (Math.abs(rightMinusLeft.doubleValue()) <= epsilon) {
      return visitor.visitAlmostEqual();
    }
    return signum > 0
        ? visitor.visitRightIsGreater(rightMinusLeft)
        : visitor.visitLeftIsGreater(rightMinusLeft);
  }

  /**
   * This method gives an abstraction over the {@code Comparable::compareTo}, which returns negative / 0 / positive,
   * by letting you do the comparison subject to an epsilon.
   *
   * It is specifically for those cases where we don't care about the BigDecimal precision, hence the name "...AsDoubles"
   *
   * @see #epsilonComparePreciseValues
   */
  public static <T1 extends PreciseValue<T1>, T2> T2 epsilonComparePreciseValuesAsDoubles(
      T1 left, T1 right, double epsilon, EpsilonComparisonVisitor<T2> visitor) {
    return epsilonCompareDoubles(left.doubleValue(), right.doubleValue(), epsilon, visitor);
  }

}
