package com.rb.nonbiz.types;

import com.google.common.collect.Range;
import com.rb.biz.types.trading.RoundingScale;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * A {@link Range} of {@link PreciseValue}s in which the endpoints are rounded to a specified
 * {@link RoundingScale}.
 *
 * @see Range
 * @see RoundingScale
 */
public class RoundedPreciseValueRange<P extends PreciseValue<? super P>> {

  private final Range<P> rawRange;
  private final RoundingScale roundingScale;

  private RoundedPreciseValueRange(Range<P> rawRange, RoundingScale roundingScale) {
    this.rawRange = rawRange;
    this.roundingScale = roundingScale;
  }

  public static <P extends PreciseValue<? super P>> RoundedPreciseValueRange<P> roundedPreciseValueRange(
      Range<P> rawRange, RoundingScale roundingScale) {

    if (rawRange.hasLowerBound()) {
      RBPreconditions.checkArgument(
          rawRange.lowerEndpoint().isRoundToScale(roundingScale),
          "lower bound of range %s is not round to scale %s",
          rawRange, roundingScale);
    }

    if (rawRange.hasUpperBound()) {
      RBPreconditions.checkArgument(
          rawRange.upperEndpoint().isRoundToScale(roundingScale),
          "upper bound of range %s is not round to scale %s",
          rawRange, roundingScale);
    }

    return new RoundedPreciseValueRange<>(rawRange, roundingScale);
  }

  public Range<P> getRawRange() {
    return rawRange;
  }

  public RoundingScale getRoundingScale() {
    return roundingScale;
  }

  @Override
  public String toString() {
    return Strings.format("[RPVR %s %s RPVR]", rawRange, roundingScale);
  }

}
