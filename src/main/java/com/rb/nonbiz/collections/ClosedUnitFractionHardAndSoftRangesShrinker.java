package com.rb.nonbiz.collections;

import com.google.inject.Inject;
import com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRange;
import com.rb.nonbiz.types.UnitFraction;

import static com.rb.nonbiz.collections.ClosedUnitFractionHardAndSoftRanges.closedUnitFractionHardAndSoftRanges;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRange.closedUnitFractionHardAndPossiblySameSoftRange;

/**
 * Shrinks all ranges in a {@link ClosedUnitFractionHardAndSoftRanges} object
 * by scaling them by a non-zero UnitFraction multiplier (i.e. &le; 1).
 * E.g. the range [0.2, 0.6] scaled by 0.5 becomes [0.1, 0.3].
 *
 * <p> Note that 'shrinking' here doesn't mean that the midpoint remains the same. Instead, each bound (lower &amp; upper)
 * gets multiplied by a number in (0, 1]. </p>
 */
public class ClosedUnitFractionHardAndSoftRangesShrinker {

  @Inject ClosedUnitFractionRangeShrinker closedUnitFractionRangeShrinker;

  public <K> ClosedUnitFractionHardAndSoftRanges<K> shrink(
      ClosedUnitFractionHardAndSoftRanges<K> unscaled, UnitFraction fractionOfOriginal) {
    return closedUnitFractionHardAndSoftRanges(
        unscaled.getRawMap().transformValuesCopy(v -> shrinkSingle(v, fractionOfOriginal)));
  }

  public ClosedUnitFractionHardAndSoftRange shrinkSingle(
      ClosedUnitFractionHardAndSoftRange unscaled, UnitFraction fractionOfOriginal) {
    return closedUnitFractionHardAndPossiblySameSoftRange(
        closedUnitFractionRangeShrinker.shrink(unscaled.getHardRange(), fractionOfOriginal),
        closedUnitFractionRangeShrinker.shrink(unscaled.getSoftRange(), fractionOfOriginal));
  }

}
