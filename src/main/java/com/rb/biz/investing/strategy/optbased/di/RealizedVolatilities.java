package com.rb.biz.investing.strategy.optbased.di;

import com.rb.nonbiz.collections.ImmutableIndexableArray1D;
import com.rb.nonbiz.math.eigen.Investable;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * Realized volatilities per (usually) InstrumentId
 * for total returns (i.e. incorporating corporate actions, not just nominal prices).
 * We store these as 'daily-ized' (whatever the daily equivalent of 'annualized' is)
 * because that is the most natural unit that makes no assumptions about # of days in a year, etc.
 * We use that weird term to avoid confusion with 'daily' meaning 'for every day' which would imply that we have
 * a lot of them per Investable.
 *
 * Use Constants#getApproximateDailyToAnnualizedVolatilityMultiplier to convert to annualized, which is a more natural
 * unit, at least for those of us who worked in options trading before.
 */
public class RealizedVolatilities<K extends Investable> {

  private final ImmutableIndexableArray1D<K, Double> dailyizedStandardDeviations;

  private RealizedVolatilities(ImmutableIndexableArray1D<K, Double> dailyizedStandardDeviations) {
    this.dailyizedStandardDeviations = dailyizedStandardDeviations;
  }

  public static <K extends Investable> RealizedVolatilities<K> realizedVolatilities(
      ImmutableIndexableArray1D<K, Double> dailyizedStandardDeviations) {
    dailyizedStandardDeviations.valuesStream().forEach(standardDeviation -> {
      RBPreconditions.checkArgument(
          standardDeviation >= 0,
          "All standard deviations in realized volatilities must be >= 0 but I got %s : %s",
          standardDeviation, dailyizedStandardDeviations);
      RBPreconditions.checkArgument(
          standardDeviation < 1,
          // 1 = 100% daily vol is like 1600% annualized vol, which is extremely high. Real stocks are normally ~15% vol
          // under normal conditions, and maybe over 100 in high-vol markets (or some are high-vol stocks).
          "All standard deviations in realized volatilities can't be huge; got %s : %s",
          standardDeviation, dailyizedStandardDeviations);
    });
    return new RealizedVolatilities<>(dailyizedStandardDeviations);
  }

  public ImmutableIndexableArray1D<K, Double> getDailyizedStandardDeviations() {
    return dailyizedStandardDeviations;
  }

  /**
   * Use Constants#getApproximateDailyToAnnualizedVolatilityMultiplier to convert to annualized, which is a more natural
   * unit, at least for those of us who worked in options trading before.
   *
   * Throws if the key is not there.
   */
  public double getDailyizedStandardDeviation(K key) {
    return dailyizedStandardDeviations.get(key);
  }

  @Override
  public String toString() {
    return Strings.format("[RV %s items: %s RV]",
        dailyizedStandardDeviations.size(), dailyizedStandardDeviations);
  }

}
