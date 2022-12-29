package com.rb.nonbiz.types;

import com.google.common.collect.Range;
import com.rb.biz.types.trading.PositiveQuantity;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;

import static com.google.common.collect.Range.closed;
import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.biz.types.trading.PositiveQuantity.POSITIVE_QUANTITY_1;
import static com.rb.biz.types.trading.PositiveQuantity.positiveQuantity;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

/**
 * A class to hold a stock split ratio, as well as the numerator and denominator
 * of the split.
 *
 * <p> E.g. for a "3 for 2" split, store the ratio 1.5 as well
 * as the numerator (3) and the denominator (2). </p>
 *
 * <p> This would be used in calculating stock splits and stock dividends (but not CASH dividends). </p>
 *
 * <p> Some terminology: a 2:1 split is called a "forward split"; every 1 old share will be replaced
 * by 2 new shares.  </p>
 *
 * <p> Conversely, a 1:2 split is called a "reverse split"; 2 old shares will
 * be replaced by 1 new share. Most splits are "forward splits". </p>
 *
 * <p> Note that both the numerator and denominator are stored as {@code BigDecimal}s,
 * so we can represent non-integer splits, e.g. "2.75:1"</p>
 */
public class SplitMultiplier extends PreciseValue<SplitMultiplier> {

  public static final BigDecimal MIN_SPLIT_MULTIPLIER = BigDecimal.valueOf(0.0001);
  public static final BigDecimal MAX_SPLIT_MULTIPLIER = BigDecimal.valueOf(10_000.0);
  public static final Range<BigDecimal> ALLOWABLE_SPLIT_RANGE = closed(
      MIN_SPLIT_MULTIPLIER,
      MAX_SPLIT_MULTIPLIER);

  private final PositiveQuantity numerator;
  private final PositiveQuantity denominator;

  private SplitMultiplier(PositiveQuantity numerator, PositiveQuantity denominator, BigDecimal ratio) {
    super(ratio);
    this.numerator = numerator;
    this.denominator = denominator;
  }

  public static SplitMultiplier splitMultiplier(PositiveQuantity numerator, PositiveQuantity denominator) {
    BigDecimal ratio = numerator.asBigDecimal().divide(denominator.asBigDecimal(), DEFAULT_MATH_CONTEXT);
    sanityChecks(numerator, denominator, ratio);
    return new SplitMultiplier(numerator, denominator, ratio);
  }

  // supply only the ratio, not the numerator and denominator separately
  public static SplitMultiplier splitMultiplier(BigDecimal multiplier) {
    // since we don't know the numerator and denominator separately, use:
    //   effectiveNumerator = multiplier
    //   denominator        = 1
    PositiveQuantity effectiveNumerator = positiveQuantity(multiplier);
    sanityChecks(effectiveNumerator, POSITIVE_QUANTITY_1, multiplier);

    return new SplitMultiplier(effectiveNumerator, POSITIVE_QUANTITY_1, multiplier);
  }

  private static void sanityChecks(
      PositiveQuantity numerator, PositiveQuantity denominator, BigDecimal multiplier) {
    RBPreconditions.checkArgument(
        multiplier.doubleValue() > 0,
        "Encountered non-positive split multiplier %s",
        multiplier);

    RBPreconditions.checkInRange(
        multiplier,
        ALLOWABLE_SPLIT_RANGE,
        "split multiplier %s not in allowable range %s",
        multiplier, ALLOWABLE_SPLIT_RANGE);

    // a 1:1 split wouldn't make sense (neither would an N:N split)
    RBPreconditions.checkArgument(!numerator.almostEquals(denominator, DEFAULT_EPSILON_1e_8),
        "A 1:1 split (or N:N) is invalid: %s : %s",
        numerator, denominator);
  }

  PositiveQuantity getNumerator() {
    return numerator;
  }

  PositiveQuantity getDenominator() {
    return denominator;
  }

  @Override
  public String toString() {
    return Strings.format("[SM mult %s : %s for %s SM]",
        asBigDecimal(), numerator, denominator);
  }

}
