package com.rb.nonbiz.math.beta;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Correlation;
import com.rb.nonbiz.util.RBPreconditions;

public class BetaBackground {

  private final int numMarketDaysInCalculation;
  private final Correlation correlation;

  private BetaBackground(int numMarketDaysInCalculation, Correlation correlation) {
    this.numMarketDaysInCalculation = numMarketDaysInCalculation;
    this.correlation = correlation;
  }

  public static BetaBackground betaBackground(int numMarketDaysInCalculation, Correlation correlation) {
    RBPreconditions.checkArgument(
        numMarketDaysInCalculation > 0,
        "For BetaBackground, numMarketDaysInCalculation was %s ; should be positive",
        numMarketDaysInCalculation);
    RBPreconditions.checkArgument(
        numMarketDaysInCalculation <= 10 * 250,
        "For BetaBackground, numMarketDaysInCalculation was %s ; should less than roughly 10 years",
        numMarketDaysInCalculation);
    return new BetaBackground(numMarketDaysInCalculation, correlation);
  }

  public int getNumMarketDaysInCalculation() {
    return numMarketDaysInCalculation;
  }

  public Correlation getCorrelation() {
    return correlation;
  }

  @Override
  public String toString() {
    return Strings.format("[BB %s %s BB]", numMarketDaysInCalculation, correlation);
  }

}
