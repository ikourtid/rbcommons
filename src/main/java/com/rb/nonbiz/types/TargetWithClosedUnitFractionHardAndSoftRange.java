package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * A {@link ClosedUnitFractionHardAndSoftRange} together with a value (whose semantics is that it is a target within
 * that range that we want to get to). The value must be inside the ranges.
 *
 * <p> Despite the very basic nature of this data class, it is not used extensively currently (Nov 2022). </p>
 */
public class TargetWithClosedUnitFractionHardAndSoftRange {

  private final UnitFraction target;
  private final ClosedUnitFractionHardAndSoftRange closedUnitFractionHardAndSoftRange;

  private TargetWithClosedUnitFractionHardAndSoftRange(
      UnitFraction target,
      ClosedUnitFractionHardAndSoftRange closedUnitFractionHardAndSoftRange) {
    this.target = target;
    this.closedUnitFractionHardAndSoftRange = closedUnitFractionHardAndSoftRange;
  }

  public static TargetWithClosedUnitFractionHardAndSoftRange targetWithClosedUnitFractionHardAndSoftRange(
      UnitFraction target,
      ClosedUnitFractionHardAndSoftRange closedUnitFractionHardAndSoftRange) {
    RBPreconditions.checkArgument(
        closedUnitFractionHardAndSoftRange.getSoftRange().contains(target),
        "Target %s must be contained within range %s",
        target, closedUnitFractionHardAndSoftRange);
    return new TargetWithClosedUnitFractionHardAndSoftRange(target, closedUnitFractionHardAndSoftRange);
  }

  public UnitFraction getTarget() {
    return target;
  }

  public ClosedUnitFractionHardAndSoftRange getClosedUnitFractionHardAndSoftRange() {
    return closedUnitFractionHardAndSoftRange;
  }

  @Override
  public String toString() {
    return Strings.format("[TWCUFHASR %s %s TWCUFHASR]",
        target, closedUnitFractionHardAndSoftRange);
  }

}
