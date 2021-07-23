package com.rb.nonbiz.types;

import com.rb.nonbiz.types.PreciseValues.BigDecimalsEpsilonComparisonVisitor;
import com.rb.nonbiz.util.RBPreconditions;

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

  public static <T> T epsilonCompareDoubles(double left, double right, double epsilon, EpsilonComparisonVisitor<T> visitor) {
    RBPreconditions.checkArgument(
        epsilon > 0,
        "epsilon must be positive; was %s",
        epsilon);
    RBPreconditions.checkArgument(
        epsilon < 1e4,
        "even though it could be reasonable, we disallow a huge (> 1e4) epsilon for safety, since it's probably a typo; was %s",
        epsilon);
    double rightMinusLeft = right - left;
    if (Math.abs(rightMinusLeft) <= epsilon) {
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
      double left, double right, double epsilon, EpsilonComparisonVisitor<T> visitor) {
    RBPreconditions.checkArgument(
        epsilon >= 0,
        "epsilon must be non-negative; was %s",
        epsilon);
    RBPreconditions.checkArgument(
        epsilon < 1e4,
        "even though it could be reasonable, we disallow a huge (> 1e4) epsilon for safety, since it's probably a typo; was %s",
        epsilon);
    double rightMinusLeft = right - left;
    if (Math.abs(rightMinusLeft) <= epsilon) {
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

  public static long getDoubleAsLongAssumingIsRound(double value, double epsilon) {
    RBPreconditions.checkArgument(
        epsilon >= 0,
        "epsilon to check % cannot be negative: %s",
        value, epsilon);
    long nearestRound = Math.round(value);
    RBPreconditions.checkArgument(
        Math.abs(value - nearestRound) <= epsilon,
        "The closest long to value %s is %s which is not within an epsilon of %s",
        value, nearestRound, epsilon);
    return nearestRound;
  }

}
