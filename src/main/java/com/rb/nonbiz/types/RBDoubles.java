package com.rb.nonbiz.types;

import com.rb.nonbiz.types.PreciseValues.BigDecimalsEpsilonComparisonVisitor;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.OptionalDouble;

import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptionalDouble;

public class RBDoubles {

  /**
   * This is a handy abstraction that lets you sort out the 3 cases where two numbers relate to each other,
   * subject to an epsilon.
   * <p> It is clearer than {@code Comparable::compareTo}, which returns negative / 0 / positive,
   * and also lets you do a comparison subject to an epsilon. </p>
   *
   * @see BigDecimalsEpsilonComparisonVisitor
   */
  public interface EpsilonComparisonVisitor<T> {

    T visitRightIsGreater(double rightMinusLeft);
    T visitAlmostEqual();
    T visitLeftIsGreater(double rightMinusLeft);

  }

  public static <T> T epsilonCompareDoubles(double left, double right, Epsilon epsilon, EpsilonComparisonVisitor<T> visitor) {
    double rightMinusLeft = right - left;
    if (epsilon.isAlmostZero(rightMinusLeft)) {
      return visitor.visitAlmostEqual();
    }
    return rightMinusLeft > 0
        ? visitor.visitRightIsGreater(rightMinusLeft)
        : visitor.visitLeftIsGreater(rightMinusLeft);
  }

  /**
   * We need this variant of epsilonCompareDoubles usually for cases where the epsilon is read from a class (i.e.
   * not hardcoded into the code), in which case we may want to allow using an epsilon of 0.
   */
  public static <T> T epsilonCompareDoublesAllowingEpsilonOfZero(
      double left, double right, Epsilon epsilon, EpsilonComparisonVisitor<T> visitor) {
    double rightMinusLeft = right - left;
    if (epsilon.isAlmostZero(rightMinusLeft)) {
      return visitor.visitAlmostEqual();
    }
    return rightMinusLeft > 0
        ? visitor.visitRightIsGreater(rightMinusLeft)
        : visitor.visitLeftIsGreater(rightMinusLeft);
  }

  public static double average(double v1, double v2) {
    return 0.5 * (v1 + v2);
  }

  /**
   * We have a specific implementation for 2 decimal places, because if we use some variable n instead of 2,
   * we'll have to compute Math.pow which may be a tiny bit slow.
   */
  public static double roundToTwoDecimalPlaces(double value) {
    return Math.round(100 * value) / 100.0;
  }

  public static long getDoubleAsLongAssumingIsRound(double value, Epsilon epsilon) {
    long nearestRound = Math.round(value);
    RBPreconditions.checkArgument(
        epsilon.valuesAreWithin(value, nearestRound),
        "The closest long to value %s is %s which is not within an epsilon of %s",
        value, nearestRound, epsilon);
    return nearestRound;
  }

  /**
   * If the {@link OptionalDouble} argument is present, return the max of the two doubles; otherwise return the only
   * present double.
   */
  public static double maxAllowingOptionalDouble(OptionalDouble optionalValue1, double value2) {
    return transformOptionalDouble(optionalValue1, value1 -> Math.max(value1, value2))
        .orElse(value2);
  }

  /**
   * If the {@link OptionalDouble} argument is present, return the max of the two doubles; otherwise return the only
   * present double.
   */
  public static double maxAllowingOptionalDouble(double value1, OptionalDouble optionalValue2) {
    return transformOptionalDouble(optionalValue2, value2 -> Math.max(value1, value2))
        .orElse(value1);
  }

  /**
   * If the {@link OptionalDouble} argument is present, return the min of the two doubles; otherwise return the only
   * present double.
   */
  public static double minAllowingOptionalDouble(OptionalDouble optionalValue1, double value2) {
    return transformOptionalDouble(optionalValue1, value1 -> Math.min(value1, value2))
        .orElse(value2);
  }

  /**
   * If the {@link OptionalDouble} argument is present, return the min of the two doubles; otherwise return the only
   * present double.
   */
  public static double minAllowingOptionalDouble(double value1, OptionalDouble optionalValue2) {
    return transformOptionalDouble(optionalValue2, value2 -> Math.min(value1, value2))
        .orElse(value1);
  }

}
