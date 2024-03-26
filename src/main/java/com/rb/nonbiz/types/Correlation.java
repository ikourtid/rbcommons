package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * A typesafe wrapper that denotes a correlation.
 *
 * <p> This also means that the valid values are between -1 and 1, inclusive. </p>
 */
public class Correlation extends ImpreciseValue<Correlation> {

  public static final Correlation PERFECT_CORRELATION = correlation(1.0);
  public static final Correlation NO_CORRELATION = correlation(0);
  public static final Correlation BIGGEST_ANTI_CORRELATION = correlation(-1);

  public Correlation(double correlationValue) {
    super(correlationValue);
  }

  /**
   * Constructs a {@link Correlation} in a 'forgiving' fashion, by allowing values that are outside [-1, 1]
   * by an epsilon amount of 1e-8, and forcing them to -1 or 1, respectively.
   *
   * <p> That's because correlations are calculated using double arithmetic, and it's not safe to rely on
   * double comparisons. </p>
   */
  public static Correlation correlation(double correlationValue) {
    // For numeric reasons, the correlation value calculated may be a tiny bit lower than -1
    // or a tiny bit higher than 1. Let's allow that.
    if (1 < correlationValue && correlationValue < 1 + 1e-8) {
      correlationValue = 1;
    } else if (-1 - 1e-8 < correlationValue && correlationValue < -1) {
      correlationValue = -1;
    }
    RBPreconditions.checkArgument(
        -1 <= correlationValue && correlationValue <= 1,
        "Correlation must be between -1 and 1 (inclusive) but was %s",
        correlationValue);
    return new Correlation(correlationValue);
  }

  @Override
  public String toString() {
    return Strings.format("[C %s C]", doubleValue());
  }

}
