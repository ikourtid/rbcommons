package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;

/**
 * A score difference that must be exceeded for something to happen.
 *
 * <p> E.g. trading must improve some 'portfolio quality score' by this much - otherwise don't bother trading. </p>
 */
public class ScoreThreshold<S extends Score> extends PreciseValue<ScoreThreshold<S>> {

  public static <S extends Score> ScoreThreshold<S> alwaysExceededThreshold() {
    return new ScoreThreshold<S>(BigDecimal.valueOf(-1.01));
  }

  /**
   * Each score is in the closed interval of [0, 1]. So all diffs are always {@code <=} 1.0
   * We use 1.01 so that even {@code <=} comparisons will result in 'score not exceeded'.
   */
  public static <S extends Score> ScoreThreshold<S> neverExceededThreshold() {
    return new ScoreThreshold<S>(BigDecimal.valueOf(1.01));
  }

  private ScoreThreshold(BigDecimal thresholdValue) {
    super(thresholdValue);
  }

  public static <S extends Score> ScoreThreshold<S> scoreThreshold(double thresholdValue) {
    return scoreThreshold(BigDecimal.valueOf(thresholdValue));
  }

  public static <S extends Score> ScoreThreshold<S> scoreThreshold(BigDecimal thresholdValue) {
    RBPreconditions.checkArgument(
        thresholdValue.signum() >= 0 && thresholdValue.compareTo(BigDecimal.ONE) <= 0,
        "A thresholdValue represents an abs diff of 2 scores in the interval [0, 1], so must also be in [0, 1] but was %s",
        thresholdValue);
    return new ScoreThreshold<S>(thresholdValue);
  }

  public boolean alwaysPasses() {
    return doubleValue() < -1;
  }

  public boolean isExceeded(S startingScore, S endingScore) {
    return endingScore.asBigDecimal().subtract(startingScore.asBigDecimal()).compareTo(asBigDecimal()) >= 0;
  }

  /**
   * ScoreThreshold intentionally does not extend PreciseValue, since we never really want to look at its value.
   * We really should just be using it as a launching point for comparing 2 scores.
   * So we are exposing its value at the package level only, to enable testing.
   *
   * Updated comment on Dec 2017: we need to look at its value when searching for backtests using the
   * SingleBacktestResultFinder. So now it does implement PreciseValue, which itself implements equals.
   * The SingleBacktestResultFinder assumes that #equals works for any knob that's searchable.
   */

  @Override
  public String toString() {
    return Strings.format("[threshold= %s ]", asBigDecimal());
  }

  public String toSimpleString() {
    // The precise formatting helps align the backtest labels vertically
    return String.format("%5.2f", doubleValue());
  }

}
