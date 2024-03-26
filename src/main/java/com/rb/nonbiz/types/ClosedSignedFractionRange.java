package com.rb.nonbiz.types;

import com.google.common.collect.Range;
import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;

/**
 * This is effectively a {@code Range<Double>}, except that it is bounded on both ends.
 *
 * <p> It's similar to ClosedUnitFractionRange, except that the bounds don't have to be between 0 and 1. </p>
 */
public class ClosedSignedFractionRange {

  private final Range<SignedFraction> rawRange;

  private ClosedSignedFractionRange(Range<SignedFraction> rawRange) {
    this.rawRange = rawRange;
  }

  public static ClosedSignedFractionRange closedSignedFractionRange(SignedFraction lowerEndpoint, SignedFraction upperEndpoint) {
    return new ClosedSignedFractionRange(Range.closed(lowerEndpoint, upperEndpoint));
  }

  public static ClosedSignedFractionRange signedFractionFixedTo(SignedFraction fixedValue) {
    return closedSignedFractionRange(fixedValue, fixedValue);
  }

  public static ClosedSignedFractionRange signedFractionFixedToZero() {
    return signedFractionFixedTo(SIGNED_FRACTION_0);
  }

  public static ClosedSignedFractionRange signedFractionFixedToOne() {
    return signedFractionFixedTo(SIGNED_FRACTION_1);
  }

  public SignedFraction lowerEndpoint() {
    return rawRange.lowerEndpoint();
  }

  public SignedFraction upperEndpoint() {
    return rawRange.upperEndpoint();
  }

  public Range<SignedFraction> asSignedFractionRange() {
    return rawRange;
  }

  public Range<Double> asDoubleRange() {
    return Range.closed(
        rawRange.lowerEndpoint().doubleValue(),
        rawRange.upperEndpoint().doubleValue());
  }

  @Override
  public String toString() {
    return Strings.format("[CSFR %s CSFR]", rawRange);
  }

}
