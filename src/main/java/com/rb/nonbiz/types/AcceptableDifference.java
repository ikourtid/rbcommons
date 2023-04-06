package com.rb.nonbiz.types;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.collections.Either;
import com.rb.nonbiz.collections.Either.Visitor;

import static com.rb.nonbiz.types.Epsilon.ZERO_EPSILON;
import static com.rb.nonbiz.types.Epsilon.epsilon;

/**
 * Holds an epsilon (either relative or absolute) to use in deciding whether two quantities
 * are numerically the same.
 *
 * <p> This uses an {@code Either<UnitFraction, Double>} in order to hold either a relative
 * or absolute difference. </p>
 *
 * @see Either
 */
public class AcceptableDifference {

  private final Either<UnitFraction, Epsilon> epsilons;

  private AcceptableDifference(Either<UnitFraction, Epsilon> epsilons) {
    this.epsilons = epsilons;
  }

  /**
   * Use this to set an acceptable <i>relateive</i> difference.
   *
   * @param epsilonFraction the acceptable difference in the ratio of 2 values
   * @return AcceptableDifference
   */
  public static AcceptableDifference epsilonAsFraction(UnitFraction epsilonFraction) {
    return new AcceptableDifference(Either.left(epsilonFraction));
  }

  /**
   * Use this to set an acceptable <i>absolute</i> difference.
   *
   * @param epsilonDiff the acceptable absolute difference between the 2 values
   * @return AcceptableDifference
   */
  public static AcceptableDifference epsilonAsDiff(Epsilon epsilonDiff) {
    return new AcceptableDifference(Either.right(epsilonDiff));
  }

  /**
   * Use this to indicate that 2 numbers must be exactly the same by setting {@code epsilon = 0}.
   * @return AcceptableDifference
   */
  public static AcceptableDifference exactlySame() {
    return epsilonAsDiff(ZERO_EPSILON);
  }

  /**
   * Use this to indicate that 2 numbers are numerically the same by setting {@code epsilon = 1e-12}.
   * @return AcceptableDifference
   */
  public static AcceptableDifference numericallySame() {
    return epsilonAsDiff(epsilon(1e-12));
  }

  /**
   * Tells you if 2 values are close enough to each other, based on the criteria in
   * this {@link AcceptableDifference} class.
   *
   * <p> Typically data classes don't have logic but this is simple enough. </p>
   */
  public boolean withinEpsilon(double val1, double val2) {
    return epsilons.visit(new Visitor<UnitFraction, Epsilon, Boolean>() {
      @Override
      public Boolean visitLeft(UnitFraction epsilonFraction) {
        double average = 0.5 * (val1 + val2);
        // If denominator is ~= 0, we'll just cop out and use the fraction as a diff epsilon
        return Math.abs(average) < 1e-8
            ? visitRight(epsilon(epsilonFraction.doubleValue())):
            Math.abs((val1 - val2) / average) <= epsilonFraction.doubleValue();
      }

      @Override
      public Boolean visitRight(Epsilon epsilonDiff) {
        return epsilonDiff.valuesAreWithin(val1, val2);
      }
    });
  }

  // This is here to help the matchers
  @VisibleForTesting
  Either<UnitFraction, Epsilon> getEpsilons() {
    return epsilons;
  }

}
