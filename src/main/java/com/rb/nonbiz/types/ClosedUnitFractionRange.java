package com.rb.nonbiz.types;

import com.google.common.collect.Range;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.RBRanges;
import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;

/**
 * This is effectively a {@code Range<Double>} except that:
 * <ul>
 *   <li> It is bounded on both ends. </li>
 *   <li> It is at most [0, 1], which is convenient because we use '{@link UnitFraction}
 *        of total portfolio' a lot in our LP formulations. </li>
 * </ul>
 */
public class ClosedUnitFractionRange {

  public static final ClosedUnitFractionRange WIDEST_POSSIBLE_CLOSED_UNIT_FRACTION_RANGE =
      new ClosedUnitFractionRange(closedRange(UNIT_FRACTION_0, UNIT_FRACTION_1));

  private final ClosedRange<UnitFraction> rawRange;

  private ClosedUnitFractionRange(ClosedRange<UnitFraction> rawRange) {
    this.rawRange = rawRange;
  }

  public boolean isFixedToZero() {
    // no need to check the lower bound, since 0 <= lower <= upper
    return rawRange.upperEndpoint().isZero();
  }

  public static ClosedUnitFractionRange closedUnitFractionRange(UnitFraction lowerEndpoint, UnitFraction upperEndpoint) {
    return new ClosedUnitFractionRange(closedRange(lowerEndpoint, upperEndpoint));
  }

  public static ClosedUnitFractionRange closedUnitFractionRange(ClosedRange<UnitFraction> closedRange) {
    return new ClosedUnitFractionRange(closedRange);
  }

  public static ClosedUnitFractionRange closedUnitFractionRange(Range<UnitFraction> rawRange) {
    if (rawRange.hasLowerBound()) {
      return rawRange.hasUpperBound()
          ? closedUnitFractionRange(rawRange.lowerEndpoint(), rawRange.upperEndpoint())
          : unitFractionAtLeast(rawRange.lowerEndpoint());
    } else {
      return rawRange.hasUpperBound()
          ? unitFractionAtMost(rawRange.upperEndpoint())
          : unrestrictedClosedUnitFractionRange();
    }
  }

  public static ClosedUnitFractionRange unrestrictedClosedUnitFractionRange() {
    return WIDEST_POSSIBLE_CLOSED_UNIT_FRACTION_RANGE;
  }

  public static ClosedUnitFractionRange unitFractionAtLeast(UnitFraction lowerEndpoint) {
    return closedUnitFractionRange(lowerEndpoint, UNIT_FRACTION_1);
  }

  public static ClosedUnitFractionRange unitFractionAtMost(UnitFraction upperEndpoint) {
    return closedUnitFractionRange(UNIT_FRACTION_0, upperEndpoint);
  }

  public static ClosedUnitFractionRange unitFractionFixedTo(UnitFraction fixedValue) {
    return closedUnitFractionRange(fixedValue, fixedValue);
  }

  public static ClosedUnitFractionRange unitFractionFixedToZero() {
    return unitFractionFixedTo(UNIT_FRACTION_0);
  }

  public static ClosedUnitFractionRange unitFractionFixedToOne() {
    return unitFractionFixedTo(UNIT_FRACTION_1);
  }

  public UnitFraction lowerEndpoint() {
    return rawRange.lowerEndpoint();
  }

  public UnitFraction upperEndpoint() {
    return rawRange.upperEndpoint();
  }

  public ClosedRange<UnitFraction> asClosedRangeOfUnitFraction() {
    return rawRange;
  }

  /**
   * The best way to think about what this should do is that it should fail if the
   * 'hard range' is somehow more restrictive than the 'soft range'.
   * If you think about the cases when that should be valid, it gives you an idea of what this returns.
   *
   * We want to allow hard = soft = [0%, 100%] (i.e. both unrestricted)
   * but not hard = soft = [40%, 100%]
   * or hard = soft = [0%, 70%].
   * That is, if we bothered to specify one endpoint of the range (0% and 100% don't count),
   * then that endpoint has to be strictly looser in the hard range vs. the soft range.
   */
  public boolean isStrictlyLooser(ClosedUnitFractionRange other) {
    // I am using compareTo once per bound, so as to avoid recomputation.
    int lowerComparison = rawRange.lowerEndpoint().compareTo(other.lowerEndpoint());
    int upperComparison = rawRange.upperEndpoint().compareTo(other.upperEndpoint());

    boolean otherMinsOut = other.lowerEndpoint().isZero();
    boolean otherMaxesOut = other.upperEndpoint().isOne();

    if (otherMinsOut && otherMaxesOut) {
      // 'other' is unrestricted on both ends, so return true only if 'this' is also unrestricted.
      // The following is a less intuitive, but faster way to say that,
      // since we compared 'this' and 'other' bounds already.
      return lowerComparison == 0 && upperComparison == 0;
    }

    if (otherMinsOut) {
      // 'other' is e.g. [0%, x%] where x != 100.
      return lowerComparison == 0 && upperComparison > 0;
    }
    if (otherMaxesOut) {
      // 'other' is e.g. [x%, 100%] where x != 0.
      return lowerComparison < 0 && upperComparison == 0;
    }
    return lowerComparison < 0 && upperComparison > 0;
  }

  /**
   * As in isStrictlyLooser() above,
   * but allow the range to match either (or both) endpoint of 'other'.
   * E.g. we want to allow all of
   * hard = soft = [ 0%, 100%]
   * hard = soft = [ 0%,  60%]
   * hard = soft = [40%, 100%]
   * hard = soft = [40%,  60%]
   */
  public boolean isSameOrLooser(ClosedUnitFractionRange other) {
    int lowerComparison = rawRange.lowerEndpoint().compareTo(other.lowerEndpoint());
    int upperComparison = rawRange.upperEndpoint().compareTo(other.upperEndpoint());

    boolean otherMinsOut  = other.lowerEndpoint().isZero();
    boolean otherMaxesOut = other.upperEndpoint().isOne();

    if (otherMinsOut && otherMaxesOut) {
      // 'other' is unrestricted on both ends, so return true only if 'this' is also unrestricted.
      // The following is a less intuitive, but faster way to say that,
      // since we compared 'this' and 'other' bounds already.
      return lowerComparison == 0 && upperComparison == 0;
    }

    if (otherMinsOut) {
      // 'other' is e.g. [0%, x%] where x != 100.
      // note upperComparison is >= 0, as opposed to > 0 in isStrictlyLooser()
      return lowerComparison == 0 && upperComparison >= 0;
    }
    if (otherMaxesOut) {
      // 'other' is e.g. [x%, 100%] where x != 0.
      // note lowerComparison is <= 0, as opposed to < 0 in isStrictlyLooser()
      return lowerComparison <= 0 && upperComparison == 0;
    }
    // note comparisons include "= 0", as opposed to strictly greater than or less than in isStrictlyLooser()
    return lowerComparison <= 0 && upperComparison >= 0;
  }

  public boolean contains(UnitFraction point) {
    return rawRange.lowerEndpoint().isLessThanOrEqualTo(point)
        && rawRange.upperEndpoint().isGreaterThanOrEqualTo(point);
  }

  /**
   * Like 'contains', but returns false if the point that we're passing in
   * is the same as either the lower or upper endpoint of the range.
   */
  public boolean containsInteriorPoint(UnitFraction point) {
    return rawRange.lowerEndpoint().isLessThan(point)
        && rawRange.upperEndpoint().isGreaterThan(point);
  }

  public boolean contains(SignedFraction current) {
    return current.canBeConvertedToUnitFraction() && contains(current.toUnitFraction());
  }

  public Range<Double> asDoubleRange() {
    return Range.closed(
        rawRange.lowerEndpoint().doubleValue(),
        rawRange.upperEndpoint().doubleValue());
  }

  public UnitFraction getNearestValueInRange(UnitFraction startingValue) {
    return RBRanges.getNearestValueInRange(rawRange.asRange(), startingValue);
  }

  /**
   * Possibly tighten the lower bound to the tighter / more restrictive / higher
   * of the existing lower bound and the new lower bound supplied.
   *
   * <p> Throws (indirectly, via the closedUnitFractionRange constructor) if the new tighter
   * lower bound is even higher than the existing upper bound, which would result in an inverted range. </p>
   */
  public ClosedUnitFractionRange withPossiblyTightenedLowerBound(UnitFraction possiblyTighterLowerBound) {
    return closedUnitFractionRange(
        UnitFraction.max(rawRange.lowerEndpoint(), possiblyTighterLowerBound),
        rawRange.upperEndpoint());
  }

  /**
   * Possibly tighten the upper bound to the tighter / more restrictive / lower
   * of the existing upper bound and the new upper bound supplied.
   *
   * <p> Throws (indirectly, via the closedUnitFractionRange constructor) if the new tighter upper bound
   * is even lower than the existing lower bound, which would result in an inverted range. </p>
   */
  public ClosedUnitFractionRange withPossiblyTightenedUpperBound(UnitFraction possiblyTighterUpperBound) {
    return closedUnitFractionRange(
        rawRange.lowerEndpoint(),
        UnitFraction.min(rawRange.upperEndpoint(), possiblyTighterUpperBound));
  }

  public boolean isUnrestricted() {
    return rawRange.lowerEndpoint().isZero()
        && rawRange.upperEndpoint().isOne();
  }

  @Override
  public String toString() {
    return Strings.format("[CUFR [%s..%s] CUFR]",
        rawRange.lowerEndpoint(), rawRange.upperEndpoint());
  }

  public String toString(int maxPrecision) {
    return Strings.format("[CUFR [%s..%s] CUFR]",
        rawRange.lowerEndpoint().toPercentString(maxPrecision), rawRange.upperEndpoint().toPercentString(maxPrecision));
  }

}
