package com.rb.biz.investing.strategy.optbased.di;

import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * A simplistic estimate of the realized volatility of a portfolio, which ignores correlations
 * and only looks at a blended (weighted average) realized volatility of the constituents.
 *
 * We use 'blended' to mean it's a combination of the realized volatilities of the holdings for a single day,
 * vs. 'weighted average', which we tend to use to mean weighted across all backtest days by total portfolio value.
 */
public class BlendedRealizedVolatility extends ImpreciseValue<BlendedRealizedVolatility> {

  protected BlendedRealizedVolatility(double value) {
    super(value);
  }

  public static BlendedRealizedVolatility blendedRealizedVolatility(double value) {
    RBPreconditions.checkArgument(
        value >= 0,
        "Blended volatility cannot be negative at %s",
        value);
    return new BlendedRealizedVolatility(value);
  }

}
