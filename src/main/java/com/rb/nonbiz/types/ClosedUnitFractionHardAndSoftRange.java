package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.text.SmartFormatter.smartFormat;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.HardAndSoftRange.hardAndPossiblySameSoftRange;

/**
 * A version of {@link HardAndSoftRange} that's specialized for {@link UnitFraction}.
 *
 * <p> See {@link HardAndSoftRange} for an explanation of the 'hard and soft range' concept. </p>
 */
public class ClosedUnitFractionHardAndSoftRange {

  private final ClosedUnitFractionRange hardRange;
  private final ClosedUnitFractionRange softRange;

  private ClosedUnitFractionHardAndSoftRange(ClosedUnitFractionRange hardRange, ClosedUnitFractionRange softRange) {
    this.hardRange = hardRange;
    this.softRange = softRange;
  }

  public static ClosedUnitFractionHardAndSoftRange closedUnitFractionHardAndSoftRange(
      ClosedUnitFractionRange hardRange, ClosedUnitFractionRange softRange) {
    RBPreconditions.checkArgument(
        hardRange.isStrictlyLooser(softRange),
        "Hard range %s must be a proper superset of soft range %s",
        hardRange, softRange);
    return new ClosedUnitFractionHardAndSoftRange(hardRange, softRange);
  }

  /**
   * This is for the rare cases where we want to allow hard = soft range (see usages of this method),
   * or, more generally, if only the upper or lower bound are the same (although admittedly this method name would
   * be a misnomer in the latter case).
   */
  public static ClosedUnitFractionHardAndSoftRange closedUnitFractionHardAndPossiblySameSoftRange(
      ClosedUnitFractionRange hardRange, ClosedUnitFractionRange softRange) {
    RBPreconditions.checkArgument(
        hardRange.isSameOrLooser(softRange),
        "Hard range %s cannot be a proper subset of soft range %s",
        hardRange, softRange);
    return new ClosedUnitFractionHardAndSoftRange(hardRange, softRange);
  }

  public ClosedUnitFractionRange getHardRange() {
    return hardRange;
  }

  public ClosedUnitFractionRange getSoftRange() {
    return softRange;
  }

  public boolean isUnrestricted() {
    return hardRange.isUnrestricted()
        && softRange.isUnrestricted();
  }

  /**
   * Returns a {@link ClosedUnitFractionRange} using the following rules:
   *
   * <p> If the point is outside the hard range, then return the soft range. </p>
   *
   * <p> If the point is within the soft range, then return the soft range. </p>
   *
   * <p>If the point is between the soft and hard range, return the soft range expanded to include the point. </p>
   *
   * <p> The business case is the following. Assume a soft range of [14%, 26%] and a hard range of [10%, 30%]. </p>
   *
   * <p> If the current position is e.g. 32%, we want to sell it all the way to 26%, not to 30%. </p>
   *
   * <ul>
   *  <li>
   *    If the current position is too high, and above the hard limit (e.g. 35%), then we want to sell it down to be
   *    within the soft range, so no more than 26%. The reason why we don't sell just to within the hard range
   *    (i.e. down to 30%) is that, if we were to do that, then the next time we consider trading, this position could go
   *    above 30% due to market drift, even if we don't trade, and we wouldn't want to sell yet again.
   *    In other words, we want to slightly over-shoot the hard limit,
   *    so we won't have to immediately trade again due to price fluctuations.
   *   </li>
   *   <li>
   *     If the current position is within the soft range (e.g. 22%), then we will return the soft range. We don't want
   *     it to go above 26%, so we'll use that as a max since the returned range will be [14%, 26%].
   *   </li>
   *   <li>
   *     If the current position is outside the soft range BUT within the hard range (e.g. 27%), then we don't want to
   *     buy more and increase it, but we don't necessarily want to sell.
   *   </li>
   * </ul>
   *
   * <p> The above discussion is for cases where pointToInclude is above the upper bounds of one or both of the ranges,
   * but the same applies (symmetrically) for the case where pointToInclude is below the lower bound. </p>
   *
   * <p> In that case, effectively the semantics are
   * "sell a lot if very misallocated; do not buy more if somewhat overweight; buy up to soft max otherwise". </p>
   *
   * <p> This may make more sense in the tests. </p>
   */
  public ClosedUnitFractionRange tightenToSoftOrCurrent(UnitFraction pointToInclude) {
    if (softRange.contains(pointToInclude)) {
      return softRange;
    }
    if (!hardRange.contains(pointToInclude)) {
      return softRange;
    }
    // By now, we know pointToInclude is inside the hard but outside the soft range.
    // Loosen the soft range (the tighter of the two) to contain it.
    if (pointToInclude.isGreaterThan(softRange.upperEndpoint())) {
      return closedUnitFractionRange(softRange.lowerEndpoint(), pointToInclude);
    } else if (pointToInclude.isLessThan(softRange.lowerEndpoint())) {
      return closedUnitFractionRange(pointToInclude, softRange.upperEndpoint());
    }
    throw new IllegalArgumentException(smartFormat(
        "Internal error: we should never be here per the constructor preconditions: pointToInclude= %s ; CUFHASR= %s",
        pointToInclude, this));
  }

  public HardAndSoftRange<UnitFraction> asGeneralHardAndSoftRange() {
    return hardAndPossiblySameSoftRange(
        getHardRange().asClosedRangeOfUnitFraction().asRange(),
        getSoftRange().asClosedRangeOfUnitFraction().asRange());
  }

  @Override
  public String toString() {
    return Strings.format("[CUFHASR hard= %s ; soft= %s CUFHASR]", hardRange, softRange);
  }

}
