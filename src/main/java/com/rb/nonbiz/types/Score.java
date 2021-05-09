package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;

import java.math.BigDecimal;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;

/**
 * A {@code Score} is similar to a {@link UnitFraction} in that its range is [0, 1],
 * but it has specific semantics of "0=worst, 1=best, 0.5=in between", etc.
 *
 * <p> In contrast, a general {@code UnitFraction} represents a ratio (e.g unitFraction of portfolio that's in cash). </p>
 *
 * <p> '{@code Score}s' are mostly applicable in the simple, older, mostly deprecated rules-based investing logic. </p>
 *
 * <p> Although still relevant, they are not as applicable to optimization-based logic, because optimization optimizes
 * for multiple things. In particular, the {@code NaiveAllocationAccuracyDiffScore} only makes sense if the only thing that
 * we are optimizing for is the 'naive asset class misallocation', i.e. excluding tracking based on the
 * eigendecomposition-based risk model, and excluding other subobjectives (tax, transaction cost, holding cost)
 * altogether. </p>
 */
public class Score extends PreciseValue<Score> {

  public static final Score WORST_SCORE = new Score(BigDecimal.ZERO);
  public static final Score BEST_SCORE = new Score(BigDecimal.ONE);

  protected Score(BigDecimal value) {
    super(value);
  }

  public static Score score(double score) {
    return score(BigDecimal.valueOf(score));
  }

  public static Score score(BigDecimal score) {
    if (score.signum() == -1 || score.compareTo(BigDecimal.ONE) > 0) {
      throw new IllegalArgumentException(Strings.format(
          "Score must be between 0 and 1 (inclusive) but was %s", score));
    }
    return new Score(score);
  }

  public boolean isWorst() {
    return asBigDecimal().signum() == 0;
  }

  public boolean isBest() {
    return asBigDecimal().compareTo(BigDecimal.ONE) == 0;
  }

  /**
   * As in 'antimatter'. This is a convenience for making scores easier to read.
   * Many scores are close to 1, and it's hard to for the human eye to tell between 0.9855 and 0.9789 (say).
   * Using 1 minus the score makes it easier to compare values: e.g. 145 and 211.
   * We also multiply by 10^6 so it's even easier to make comparisons; scores that are considerably higher
   * will have more digits, and it's easy for the human eye to pick that up.
   */
  public BigDecimal toAntiScore() {
    return BigDecimal.ONE.subtract(asBigDecimal()).multiply(BigDecimal.valueOf(1_000_000), DEFAULT_MATH_CONTEXT);
  }

}
