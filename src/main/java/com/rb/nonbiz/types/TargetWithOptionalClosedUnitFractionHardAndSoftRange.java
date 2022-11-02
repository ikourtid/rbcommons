package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;

import static com.rb.nonbiz.text.Strings.formatOptional;

/**
 * A {@link UnitFraction} (whose semantics is that it is a target within that range that we want to get to)
 * together with an optional {@link ClosedUnitFractionHardAndSoftRange}. The value must be inside the ranges.
 *
 * <p> Despite the very basic nature of this data class, it is not used extensively currently (Nov 2022). </p>
 */
public class TargetWithOptionalClosedUnitFractionHardAndSoftRange {

  private final UnitFraction target;
  private final Optional<ClosedUnitFractionHardAndSoftRange> closedUnitFractionHardAndSoftRange;

  private TargetWithOptionalClosedUnitFractionHardAndSoftRange(
      UnitFraction target,
      Optional<ClosedUnitFractionHardAndSoftRange> closedUnitFractionHardAndSoftRange) {
    this.target = target;
    this.closedUnitFractionHardAndSoftRange = closedUnitFractionHardAndSoftRange;
  }

  public static TargetWithOptionalClosedUnitFractionHardAndSoftRange targetWithOptionalClosedUnitFractionHardAndSoftRange(
      UnitFraction target,
      Optional<ClosedUnitFractionHardAndSoftRange> closedUnitFractionHardAndSoftRange) {
    closedUnitFractionHardAndSoftRange.ifPresent(v ->
        RBPreconditions.checkArgument(
            v.getSoftRange().contains(target),
            "Target %s must be contained within range %s",
            target, v));
    return new TargetWithOptionalClosedUnitFractionHardAndSoftRange(target, closedUnitFractionHardAndSoftRange);
  }

  public static TargetWithOptionalClosedUnitFractionHardAndSoftRange targetWithClosedUnitFractionHardAndSoftRange(
      UnitFraction target,
      ClosedUnitFractionHardAndSoftRange closedUnitFractionHardAndSoftRange) {
    return targetWithOptionalClosedUnitFractionHardAndSoftRange(
        target, Optional.of(closedUnitFractionHardAndSoftRange));
  }

  public static TargetWithOptionalClosedUnitFractionHardAndSoftRange targetWithoutClosedUnitFractionHardAndSoftRange(
      UnitFraction target) {
    return targetWithOptionalClosedUnitFractionHardAndSoftRange(target, Optional.empty());
  }

  public UnitFraction getTarget() {
    return target;
  }

  public Optional<ClosedUnitFractionHardAndSoftRange> getClosedUnitFractionHardAndSoftRange() {
    return closedUnitFractionHardAndSoftRange;
  }

  @Override
  public String toString() {
    return Strings.format("[TWCUFHASR %s %s TWCUFHASR]",
        target, formatOptional(closedUnitFractionHardAndSoftRange));
  }

}
