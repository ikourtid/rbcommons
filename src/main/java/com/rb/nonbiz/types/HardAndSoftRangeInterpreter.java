package com.rb.nonbiz.types;

import com.google.common.collect.Range;
import com.rb.nonbiz.text.RBLog;

import static com.rb.nonbiz.collections.RBRanges.withNewDecreasedClosedLowerEndpoint;
import static com.rb.nonbiz.collections.RBRanges.withNewIncreasedClosedUpperEndpoint;
import static com.rb.nonbiz.text.RBLog.rbLog;
import static com.rb.nonbiz.text.Strings.formatRange;

/**
 * See {@link HardAndSoftRange} about why we need this class, and about the corresponding semantics.
 *
 * <p> Given a current point and a pair of hard and soft ranges (the soft being a subset of the hard),
 * we want to return the appropriate constraint range to apply to the current point.  </p>
 *
 * <p> For example, we may have a current stock position and a pair of hard and soft ranges of allowable positions,
 * and want to know what range of positions should be allowed for today's optimization. </p>
 *
 * <p> If we simply returned the soft range of allowable positions, but our current position were above the
 * upper bound of the hard range, we would be forced to sell down to the upper bound of the soft range.
 * This might not be possible; we might not be able to sell because of the wash-sale rule or other restrictions. </p>
 *
 * <p> In such a situation, we would prefer to <i>allow</i> the current position by including it in the "acceptable" range. </p>
 *
 * <p> In this case we would extend the upper bound of the hard range just enough to enclose the current position.
 * For the acceptable range lower bound, we would use the lower bound of the soft range. </p>
 *
 * @see HardAndSoftRange
 */
public class HardAndSoftRangeInterpreter {

  private final static RBLog log = rbLog(HardAndSoftRangeInterpreter.class);

  public <T extends RBNumeric<? super T>> Range<T> getRangeToUse(
      T currentPoint, HardAndSoftRange<T> hardAndSoftRange) {
    Range<T> hardRange = hardAndSoftRange.getHardRange();
    Range<T> softRange = hardAndSoftRange.getSoftRange();

    // Let's handle the lower bound first, if it exists.
    // If it does exist, it will exist for both the hard and the soft ranges, per the preconditions of HardAndSoftRange.
    // Same goes for the upper bound, by the way.
    if (hardRange.hasLowerBound() || softRange.hasLowerBound()) {
      T hardLower = hardRange.lowerEndpoint();
      T softLower = softRange.lowerEndpoint();

      // Can't use "if (currentPoint.compareTo(hardLower) < 0)" below; we have to check the bound types as well.
      // E.g. hard = (10.0, 20.0] does NOT contain 10.0; we would need to return the soft range.
      // Don't use hardRange.contains(currentPoint); that would check against the upper bound as well.
      if (!Range.downTo(hardLower, hardRange.lowerBoundType()).contains(currentPoint)) {
        log.debug("Point %s is below the hard range of %s ; forcing it back to the soft range of %s",
            currentPoint, formatRange(hardRange), formatRange(softRange));
        return softRange;
      } else if (currentPoint.compareTo(softLower) < 0) { // can do a simple bound comparison; soft bound is CLOSED
        // point is below soft lower, but not hard lower; allow currentPoint but don't let it get smaller
        Range<T> extendedRange = withNewDecreasedClosedLowerEndpoint(softRange, ignoredExistingLowerEndpoint -> currentPoint);
        log.debug("Point %s >= hard lower bound %s and < soft lower bound %s; returning extended range %s",
            currentPoint, hardLower, softLower, extendedRange);
        return extendedRange;
      }
    }

    if (hardRange.hasUpperBound() || softRange.hasUpperBound()) {
      T hardUpper = hardRange.upperEndpoint();
      T softUpper = softRange.upperEndpoint();

      // Can't use "if (currentPoint.compareTo(hardLower) > 0)" below; we have to check the bound types as well.
      // E.g. hard [10.0, 20.0) does NOT contain 20.0; we would need to return the soft range.
      // Don't use hardRange.contains(currentPoint); that would check against the lower bound as well.
      if (!Range.upTo(hardUpper, hardRange.upperBoundType()).contains(currentPoint)) {
        log.debug("Point %s is above the hard range of %s ; forcing it back to the soft range of %s",
            currentPoint, formatRange(hardRange), formatRange(softRange));
        return softRange;
      } else if (currentPoint.compareTo(softUpper) > 0) { // can do a simple bound comparison; soft bound is CLOSED
        // point is above soft upper, but not hard upper; allow currentPoint but don't let it get larger
        Range<T> extendedRange = withNewIncreasedClosedUpperEndpoint(softRange, ignoredExistingUpperEndpoint -> currentPoint);
        log.debug("Point %s > soft upper bound %s and <= hard upper bound %s; returning extended range %s",
            currentPoint, softUpper, hardUpper, extendedRange);
        return extendedRange;
      }
    }

    // If we are here, it means the point is within the soft range. So return that.
    return softRange;
  }

}
