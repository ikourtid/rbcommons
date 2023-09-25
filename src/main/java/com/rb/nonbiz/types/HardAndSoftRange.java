package com.rb.nonbiz.types;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import static com.rb.nonbiz.collections.RBRanges.rangeIsProperSubsetOnBothEnds;
import static com.rb.nonbiz.collections.RBRanges.rangeIsUnrestricted;
import static com.rb.nonbiz.collections.RBRanges.rbNumericRangeIsAlmostSinglePoint;
import static com.rb.nonbiz.text.Strings.formatRange;

/**
 * If we want to express a hard constraint such as 'US stocks &le; 20%' then we need to be smarter than just specifying
 * a &le; 20% constraint in the optimization. That's because if we trade and get to exactly 20%, then tomorrow (due to
 * price movement) that 20% could easily turn into 20.2%, which would force us to trade again.
 *
 * <p> Instead, we want the behavior to be similar to that of a thermostat: if we set the temperature to 71 degrees,
 * then the heat should turn on when the temperature drops to 70, and turn off when the temperature goes to 72.
 * Otherwise, if the heat were to go on and off at exactly the same temperature (and assuming that, in practice, the
 * sensor has perfect accuracy) then the heat would be turning itself on and off rapidly. </p>
 *
 * <p> This is similar to {@link ClosedUnitFractionHardAndSoftRange}, but it is more general, because: </p>
 *
 * <ul>
 *   <li>It does not only apply to UnitFraction</li>
 *   <li>It allows one of the two endpoints of the range (both hard and soft) to be unrestricted.</li>
 * </ul>
 *
 * <p> To clarify, the "hard" range is wider than the "soft" range and contains the "soft" range as a proper subset.
 * We can think of the "hard" range as meaning "REALLY don't exceed these bounds", while the soft range means
 * "it's not great to exceed these bounds; don't go any further." </p>
 */
public class HardAndSoftRange<T extends RBNumeric<? super T>> {

  private final Range<T> hardRange;
  private final Range<T> softRange;

  private HardAndSoftRange(Range<T> hardRange, Range<T> softRange) {
    this.hardRange = hardRange;
    this.softRange = softRange;
  }

  private static <T extends RBNumeric<? super T>> HardAndSoftRange<T> sharedHardAndSoftRange(
      Range<T> hardRange,
      Range<T> softRange) {
    RBPreconditions.checkArgument(
        !rangeIsUnrestricted(hardRange) && !rangeIsUnrestricted(softRange),
        "You cannot have an empty hard or soft range: %s %s",
        hardRange, softRange);
    RBPreconditions.checkArgument(
        !rbNumericRangeIsAlmostSinglePoint(hardRange) && !rbNumericRangeIsAlmostSinglePoint(softRange),
        "You cannot have a singleton hard or soft range: %s %s",
        hardRange, softRange);

    RBSimilarityPreconditions.checkBothSame(
        hardRange.hasLowerBound(),
        softRange.hasLowerBound(),
        "Either both or neither of the hard and soft range should have a lower bound: %s %s",
        hardRange, softRange);
    RBSimilarityPreconditions.checkBothSame(
        hardRange.hasUpperBound(),
        softRange.hasUpperBound(),
        "Either both or neither of the hard and soft range should have a upper bound: %s %s",
        hardRange, softRange);
    if (softRange.hasLowerBound()) {
      RBPreconditions.checkArgument(
          softRange.lowerBoundType() == BoundType.CLOSED,
          "Soft range may not have an open lower bound type: %s",
          softRange);
    }
    if (softRange.hasUpperBound()) {
      RBPreconditions.checkArgument(
          softRange.upperBoundType() == BoundType.CLOSED,
          "Soft range may not have an open upper bound type: %s %s",
          softRange);
    }
    return new HardAndSoftRange<>(hardRange, softRange);
  }


    // We use a builder when we have multiple values of the same type, but the name hardAndSoft should clarify that
  // hard comes before soft.
  public static <T extends RBNumeric<? super T>> HardAndSoftRange<T> hardAndSoftRange(
      Range<T> hardRange,
      Range<T> softRange) {
    RBPreconditions.checkArgument(
        // check that the soft range is a proper subset of the hard range
        rangeIsProperSubsetOnBothEnds(softRange, hardRange),
        "hard range must be a proper superset of soft range: %s %s",
        hardRange, softRange);
    return sharedHardAndSoftRange(hardRange, softRange);
  }

  // We use a builder when we have multiple values of the same type, but the name hardAndSoft should clarify that
  // hard comes before soft.
  public static <T extends RBNumeric<? super T>> HardAndSoftRange<T> hardAndPossiblySameSoftRange(
      Range<T> hardRange,
      Range<T> softRange) {
    RBPreconditions.checkArgument(
        // check that the soft range is a proper subset of the hard range
        rangeIsProperSubsetOnBothEnds(softRange, hardRange),
        "hard range must be a proper superset of soft range: %s %s",
        hardRange, softRange);
    return new HardAndSoftRange<>(hardRange, softRange);
  }

  public Range<T> getHardRange() {
    return hardRange;
  }

  public Range<T> getSoftRange() {
    return softRange;
  }

  @Override
  public String toString() {
    return Strings.format("[HASR hard= %s ; soft= %s HASR]", formatRange(hardRange), formatRange(softRange));
  }

}
