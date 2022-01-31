package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.ClosedUnitFractionHardToSoftRangeTighteningInstructions;
import com.rb.nonbiz.types.ClosedUnitFractionRange;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.function.UnaryOperator;

import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.InterpolationPreference.preferSuppliedValueBy;
import static com.rb.nonbiz.types.Interpolator.interpolateUsingPreference;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

/**
 * Various static functions pertaining to ClosedUnitFractionRanges objects.
 *
 * We would normally have named it as the plural of ClosedUnitFractionRange, to follow the usual convention,
 * but ClosedUnitFractionRanges already an existing class.
 */
public class ClosedUnitFractionRangeUtilities {

  /**
   * E.g. tightening the range [0.2, 0.6] with a center of 0.5 by 0.1 (10%) gives [0.23, 0.59]
   */
  public static ClosedUnitFractionRange tightenClosedUnitFractionRangeAround(
      ClosedUnitFractionRange initialRange,
      UnitFraction centerOfRange,
      UnitFraction fractionToTightenOnEachSide) {
    RBPreconditions.checkArgument(
        initialRange.containsInteriorPoint(centerOfRange),
        "Center of range %s must be an interior point for range %s",
        centerOfRange, initialRange);
    RBPreconditions.checkArgument(
        !fractionToTightenOnEachSide.isAlmostExtreme(1e-8),
        "Fraction to tighten range can be neither 0 nor 1");
    UnaryOperator<UnitFraction> interpolator = initial -> unitFraction(
        interpolateUsingPreference(preferSuppliedValueBy(fractionToTightenOnEachSide))
            .betweenSuppliedValue(centerOfRange.doubleValue())
            .andDefaultValue(initial.doubleValue()));
    return closedUnitFractionRange(
        interpolator.apply(initialRange.lowerEndpoint()),
        interpolator.apply(initialRange.upperEndpoint()));
  }

  /**
   * "Stretches" a ClosedUnitFractionRange to become bigger on each side by a fixed UnitFraction, up to [0, 1]
   */
  public static ClosedUnitFractionRange loosenClosedUnitFractionRangeByFixedAmount(
      ClosedUnitFractionRange initialRange,
      UnitFraction fixedCushionBetweenHardAndSoftRange) {
    return closedUnitFractionRange(
        unitFraction(Math.max(0, initialRange.lowerEndpoint().doubleValue() - fixedCushionBetweenHardAndSoftRange.doubleValue())),
        unitFraction(Math.min(1, initialRange.upperEndpoint().doubleValue() + fixedCushionBetweenHardAndSoftRange.doubleValue())));
  }

  // We could also have a tightenClosedUnitFractionRangeByFixedAmount and a
  // loosenClosedUnitFractionRangeProportionally, but there has been no need so far (April 2019)

  /**
   * E.g. [40%, 50%] tightened using a multiplier of 0.8 will give [ 41%, 49% ]
   * using a multiplier of 0, this would be come [ 45%, 45% ]
   * There is no combination of inputs that should result in throwing an exception.
   */
  public static ClosedUnitFractionRange tightenClosedUnitFractionRangeProportionally(
      ClosedUnitFractionRange initialRange,
      ClosedUnitFractionHardToSoftRangeTighteningInstructions closedUnitFractionHardToSoftRangeTighteningInstructions) {
    UnitFraction rawMultiplier = closedUnitFractionHardToSoftRangeTighteningInstructions.getRawMultiplier();
    if (rawMultiplier.isAlmostOne(1e-8)) {
      // There are some cases where we need to special-case this, to avoid numerical issues.
      // Unfortunately, the only way I'm able to test this for tiny epsilons is with DirectIndexingJapanBacktest.
      // There was a case where where the double operations below would result in a soft range that was just a tiny
      // bit wider than the hard range, and that would cause an exception. Since there are cases where
      // we expressly want the soft range to be the same as the hard range (see static constructor
      // ClosedUnitFractionHardToSoftRangeTighteningInstructions#setClosedUnitFractionSoftRangeToSameAsHard)
      // it makes sense to special-case this.
      return initialRange;
    }

    double oldLower = initialRange.lowerEndpoint().doubleValue();
    double oldUpper = initialRange.upperEndpoint().doubleValue();
    double middle = 0.5 * (oldLower + oldUpper);
    double oldWidth = oldUpper - oldLower;
    double newHalfWidth = 0.5 * oldWidth * rawMultiplier.doubleValue();

    return closedUnitFractionRange(
        unitFraction(middle - newHalfWidth),
        unitFraction(middle + newHalfWidth));
  }

  /**
   * If a ClosedUnitFractionRange does not already contain a point, we will 'loosen' it to contain it.
   *
   * For instance, [0.3, 0.7] will be changed to [0.2, 0.7] using pointToContain = 0.2.
   */
  public static ClosedUnitFractionRange possiblyLoosenToContainPoint(
      ClosedUnitFractionRange initialRange,
      UnitFraction pointToContain) {
    // There are many ways to implement this (e.g. with UnitFraction.min) but this should be faster.
    if (pointToContain.isLessThan(initialRange.lowerEndpoint())) {
      return closedUnitFractionRange(pointToContain, initialRange.upperEndpoint());
    }
    if (pointToContain.isGreaterThan(initialRange.upperEndpoint())) {
      return closedUnitFractionRange(initialRange.lowerEndpoint(), pointToContain);
    }
    return initialRange;
  }

}
