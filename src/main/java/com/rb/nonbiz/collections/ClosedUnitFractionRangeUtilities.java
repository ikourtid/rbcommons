package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.ClosedUnitFractionHardToSoftRangeTighteningInstructions;
import com.rb.nonbiz.types.ClosedUnitFractionRange;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.rb.nonbiz.collections.ClosedRange.optionalClosedRangeIntersection;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.InterpolationPreference.preferSuppliedValueBy;
import static com.rb.nonbiz.types.Interpolator.interpolateUsingPreference;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

/**
 * Various static functions pertaining to {@link ClosedUnitFractionRange} objects.
 *
 * We would normally have named this as the plural of ClosedUnitFractionRange, to follow the usual convention,
 * but {@link ClosedUnitFractionRanges} already an existing class.
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
        !fractionToTightenOnEachSide.isAlmostExtreme(DEFAULT_EPSILON_1e_8),
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
   * "Stretches" a {@link ClosedUnitFractionRange} to become bigger on each side by a
   * fixed UnitFraction, up to [0, 1].
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
   * E.g. [40%, 50%] tightened using a multiplier of 0.8 for upper and lower will give [ 41%, 49% ]
   * using a multiplier of 0 for upper and lower, this would become [ 45%, 45% ].
   *
   * <p> Using an upper multiplier of 0.5 and a lower multiplier of 0.8 would give [ 41%, 47.5% ]. </p>
   *
   * <p> There is no combination of inputs that should result in throwing an exception. </p>
   */
  public static ClosedUnitFractionRange tightenClosedUnitFractionRangeProportionally(
      ClosedUnitFractionRange initialRange,
      UnitFraction originalCenterOfRange,
      ClosedUnitFractionHardToSoftRangeTighteningInstructions closedUnitFractionHardToSoftRangeTighteningInstructions) {
    RBPreconditions.checkArgument(
        initialRange.contains(originalCenterOfRange),
    "FIXME IAK");

    UnitFraction rawLowerMultiplier = closedUnitFractionHardToSoftRangeTighteningInstructions.getRawMultiplierForLowerEndPoint();
    UnitFraction rawUpperMultiplier = closedUnitFractionHardToSoftRangeTighteningInstructions.getRawMultiplierForUpperEndPoint();
    if (rawLowerMultiplier.isAlmostOne(DEFAULT_EPSILON_1e_8) && rawUpperMultiplier.isAlmostOne(DEFAULT_EPSILON_1e_8)) {
      // There are some cases where we need to special-case this, to avoid numerical issues.
      // Unfortunately, the only way I'm able to test this for tiny epsilons is with DirectIndexingJapanBacktest.
      // There was a case where the double operations below would result in a soft range that was just a tiny
      // bit wider than the hard range, and that would cause an exception. Since there are cases where
      // we expressly want the soft range to be the same as the hard range (see static constructor
      // ClosedUnitFractionHardToSoftRangeTighteningInstructions#setClosedUnitFractionSoftRangeToSameAsHard)
      // it makes sense to special-case this.
      return initialRange;
    }
    double originalCenter = originalCenterOfRange.doubleValue();
    double lowerToCenter = originalCenter - initialRange.lowerEndpoint().doubleValue();
    double centerToUpper = initialRange.upperEndpoint().doubleValue() - originalCenter;

    return closedUnitFractionRange(
        unitFraction(originalCenter - lowerToCenter * rawLowerMultiplier.doubleValue() ),
        unitFraction(originalCenter + centerToUpper * rawUpperMultiplier.doubleValue()));
  }

  /**
   * If a ClosedUnitFractionRange does not already contain a point, we will 'loosen' it to contain it.
   *
   * <p> For instance, [0.3, 0.7] will be changed to [0.2, 0.7] using pointToContain = 0.2. </p>
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

  /**
   * Returns Optional.empty() if the two ranges have nothing in common; otherwise, returns the intersection.
   */
  public static Optional<ClosedUnitFractionRange> optionalClosedUnitFractionRangeIntersection(
      ClosedUnitFractionRange range1,
      ClosedUnitFractionRange range2) {
    return transformOptional(
        optionalClosedRangeIntersection(range1.asClosedRangeOfUnitFraction(), range2.asClosedRangeOfUnitFraction()),
        v -> closedUnitFractionRange(v));
  }

}
