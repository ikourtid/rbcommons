package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;

/**
 * Typically, when we construct a {@link ClosedUnitFractionHardAndSoftRange}, instead of specifying both hard and
 * soft ranges independently, we only specify the hard range, and then further tighten it somewhat into a soft range.
 *
 * <p>
 * For instance, if AAPL has a target of 4% in the index, we may specify the hard range as [0.02, 0.06] (2 to 6%),
 * and if this multiplier is 0.9, then the soft range will be [0.022, 0.058].
 * </p>
 *
 * <p>
 *   The reason we're using a class and calling it 'instructions' instead of just subclassing from {@link ImpreciseValue}
 *   is that the concept is more general than a simple multiplier, even though that's all this represents right now.
 *   This is really a general function, which could operate as a*x+b or anything else.
 * </p>
 */
public class ClosedUnitFractionHardToSoftRangeTighteningInstructions {

  private final UnitFraction rawMultiplier;

  private ClosedUnitFractionHardToSoftRangeTighteningInstructions(UnitFraction rawMultiplier) {
    this.rawMultiplier = rawMultiplier;
  }

  public static ClosedUnitFractionHardToSoftRangeTighteningInstructions closedUnitFractionHardToSoftRangeTighteningInstructions(
      UnitFraction rawMultiplier) {
    RBPreconditions.checkArgument(
        !rawMultiplier.isAlmostZero(1e-8),
        "You can't have the soft range be a singleton (or almost singleton) range: %s",
        rawMultiplier);
    return new ClosedUnitFractionHardToSoftRangeTighteningInstructions(rawMultiplier);
  }

  /**
   * This is for the cases where you want the soft range to be the same as hard -
   * not tighter, like you might want in a more general case. It's particularly useful for situations where you
   * don't have to utilize the distinction between hard and soft ranges, but are using code that is more general
   * and takes a {@link ClosedUnitFractionHardAndSoftRange} as an input.
   */
  public static ClosedUnitFractionHardToSoftRangeTighteningInstructions setClosedUnitFractionSoftRangeToSameAsHard() {
    return closedUnitFractionHardToSoftRangeTighteningInstructions(UNIT_FRACTION_1);
  }

  public UnitFraction getRawMultiplier() {
    return rawMultiplier;
  }

  @Override
  public String toString() {
    return Strings.format("[CUFHTSRTI %s CUFHTSRTI]", rawMultiplier);
  }

}
