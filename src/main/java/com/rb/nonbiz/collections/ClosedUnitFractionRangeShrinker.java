package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.ClosedUnitFractionRange;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;

/**
 * Shrinks a range by scaling it by a non-zero UnitFraction multiplier (i.e. &le; 1).
 * E.g. the range [0.2, 0.6] scaled by 0.5 becomes [0.1, 0.3].
 *
 * Note that 'shrinking' here doesn't mean that the midpoint remains the same. Instead, each bound (lower &amp; upper)
 * gets multiplied by a number in (0, 1].
 */
public class ClosedUnitFractionRangeShrinker {

  public ClosedUnitFractionRange shrink(ClosedUnitFractionRange originalRange, UnitFraction fractionOfOriginal) {
    RBPreconditions.checkArgument(
        !fractionOfOriginal.isAlmostZero(1e-8),
        "You can only shrink a range using a UnitFraction multiplier in (0, 1], subject to epsilon; you used %s",
        fractionOfOriginal);
    return closedUnitFractionRange(
        originalRange.lowerEndpoint().multiply(fractionOfOriginal),
        originalRange.upperEndpoint().multiply(fractionOfOriginal));
  }

}
