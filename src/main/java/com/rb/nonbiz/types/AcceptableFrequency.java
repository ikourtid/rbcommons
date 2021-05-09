package com.rb.nonbiz.types;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

/**
 * A {@link Range} of {@link UnitFraction}s.
 *
 * <p> Represents that something must happen e.g. </p>
 * <ul>
 *   <li> between 80% of 90% of the time </li>
 *   <li> always </li>
 *   <li> never </li>
 *   <li> more than 70% of the time </li>
 *   <li> ... etc </li>
 * </ul>
 *
 * @see UnitFraction
 */
public class AcceptableFrequency {

  private final Range<UnitFraction> frequencyRange;

  private AcceptableFrequency(Range<UnitFraction> frequencyRange) {
    this.frequencyRange = frequencyRange;
  }

  public static AcceptableFrequency never() {
    return new AcceptableFrequency(Range.closed(UNIT_FRACTION_0, UNIT_FRACTION_0));
  }

  public static AcceptableFrequency always() {
    return new AcceptableFrequency(Range.closed(UNIT_FRACTION_1, UNIT_FRACTION_1));
  }

  public static AcceptableFrequency atLeastOnce() {
    return new AcceptableFrequency(Range.openClosed(UNIT_FRACTION_0, UNIT_FRACTION_1));
  }

  public static AcceptableFrequency notAlways() {
    return new AcceptableFrequency(Range.closedOpen(UNIT_FRACTION_0, UNIT_FRACTION_1));
  }

  /**
   * Strictly speaking, this is 'more often than - or just as often as',
   * but that's too long to fit in an identifier name.
   */
  public static AcceptableFrequency moreOftenThan(UnitFraction minFrequency) {
    return new AcceptableFrequency(Range.closed(minFrequency, UNIT_FRACTION_1));
  }

  /**
   * Strictly speaking, this is 'less often than - or just as often as',
   * but that's too long to fit in an identifier name.
   */
  public static AcceptableFrequency lessOftenThan(UnitFraction maxFrequency) {
    return new AcceptableFrequency(Range.closed(UNIT_FRACTION_0, maxFrequency));
  }

  // We add an epsilon because it's never good to compare doubles; the UnitFraction is not a double,
  // but its construction relies on double arithmetic.
  // Considering that we should never have n too big (e.g. hundreds of backtests), the epsilon will never
  // result in making the range too loose vs. a Range.singleton.
  public static AcceptableFrequency kTimesOutOfN(int k, int n) {
    RBPreconditions.checkArgument(
        0 < k && k < n,
        "We must have 0 < k < n but k= %s and n= %s",
        k, n);
    double ratio = (double) k / n;
    // This creates a tiny range around 'ratio'. The max/min is to prevent the result from going outside the range [0, 1].
    return new AcceptableFrequency(Range.open(
        unitFraction(Math.max(0, ratio - 1e-8)),
        unitFraction(Math.min(1, ratio + 1e-8))));
  }

  public static AcceptableFrequency atLeastKTimesOutOfN(int k, int n) {
    RBPreconditions.checkArgument(
        0 < k && k < n,
        "We must have 0 < k < n but k= %s and n= %s",
        k, n);
    double ratio = (double) k / n;
    // This creates a tiny range below 'ratio'. The max is to prevent the result from going below the range [0, 1].
    return new AcceptableFrequency(Range.openClosed(
        unitFraction(Math.max(0, ratio - 1e-8)),
        UNIT_FRACTION_1));
  }

  Range<UnitFraction> getFrequencyRange() {
    return frequencyRange;
  }

  public boolean isAcceptable(UnitFraction frequency) {
    return frequencyRange.contains(frequency);
  }

  @Override
  public String toString() {
    UnitFraction lower = frequencyRange.lowerEndpoint();
    UnitFraction upper = frequencyRange.upperEndpoint();
    if (lower.isAlmostOne(1e-8)) {
      return "always";
    }
    if (upper.isAlmostZero(1e-8)) {
      return "never";
    }
    if (upper.isAlmostOne(1e-8)) {
      return frequencyRange.lowerBoundType() == BoundType.OPEN
          ? Strings.format("more than %s of times", lower.toPercentString(0, true))
          : Strings.format("at least %s of times", lower.toPercentString(0, true));
    }
    if (lower.isAlmostZero(1e-8)) {
      return frequencyRange.upperBoundType() == BoundType.OPEN
          ? Strings.format("less than %s of times", upper.toPercentString(0, true))
          : Strings.format("at most %s of times", upper.toPercentString(0, true));
    }
    return Strings.format("In this range of times: %s", frequencyRange.toString());
  }

}
