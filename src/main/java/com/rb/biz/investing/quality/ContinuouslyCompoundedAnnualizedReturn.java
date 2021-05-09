package com.rb.biz.investing.quality;

import com.rb.biz.types.OnesBasedReturn;
import com.rb.nonbiz.text.Strings;

import static com.rb.biz.investing.quality.AnnuallyCompoundedAnnualizedReturn.annuallyCompoundedAnnualizedReturn;
import static com.rb.biz.types.OnesBasedReturn.onesBasedReturn;

public class ContinuouslyCompoundedAnnualizedReturn implements Comparable<ContinuouslyCompoundedAnnualizedReturn> {

  private final OnesBasedReturn rawReturn;

  private ContinuouslyCompoundedAnnualizedReturn(OnesBasedReturn rawReturn) {
    this.rawReturn = rawReturn;
  }

  public static ContinuouslyCompoundedAnnualizedReturn continuouslyCompoundedAnnualizedReturn(OnesBasedReturn rawReturn) {
    return new ContinuouslyCompoundedAnnualizedReturn(rawReturn);
  }

  public OnesBasedReturn getRawReturn() {
    return rawReturn;
  }

  public AnnuallyCompoundedAnnualizedReturn toAnnuallyCompoundedAnnualizedReturn() {
    // end = start * e ^ rt
    return annuallyCompoundedAnnualizedReturn(onesBasedReturn(Math.exp(rawReturn.doubleValue() - 1)));
  }

  /**
   * Here, compounding doesn't matter. All this does is tell us what $1 on day 1 would turn into after numYears.
   */
  public OnesBasedReturn toNonAnnualized(double numYears) {
    // end = start * e ^ rt
    return onesBasedReturn(Math.exp(rawReturn.asZeroBasedReturnDouble() * numYears));
  }

  @Override
  public int compareTo(ContinuouslyCompoundedAnnualizedReturn other) {
    return rawReturn.compareTo(other.rawReturn);
  }

  @Override
  public String toString() {
    return Strings.format("[CCAR %s CCAR]", rawReturn);
  }

}
