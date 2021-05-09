package com.rb.biz.investing.quality;

import com.rb.biz.types.OnesBasedReturn;
import com.rb.nonbiz.text.Strings;

import static com.rb.biz.investing.quality.ContinuouslyCompoundedAnnualizedReturn.continuouslyCompoundedAnnualizedReturn;
import static com.rb.biz.types.OnesBasedReturn.onesBasedReturn;

public class AnnuallyCompoundedAnnualizedReturn implements Comparable<AnnuallyCompoundedAnnualizedReturn> {

  private final OnesBasedReturn rawReturn;

  private AnnuallyCompoundedAnnualizedReturn(OnesBasedReturn rawReturn) {
    this.rawReturn = rawReturn;
  }

  public static AnnuallyCompoundedAnnualizedReturn annuallyCompoundedAnnualizedReturn(OnesBasedReturn rawReturn) {
    return new AnnuallyCompoundedAnnualizedReturn(rawReturn);
  }

  public OnesBasedReturn getRawReturn() {
    return rawReturn;
  }

  public ContinuouslyCompoundedAnnualizedReturn toContinuouslyCompoundedAnnualizedReturn() {
    // end = start * e ^ rt
    // ln(end / start) = r * t
    return continuouslyCompoundedAnnualizedReturn(onesBasedReturn(Math.log(rawReturn.doubleValue()) + 1));
  }

  /**
   * This tells us what $1 on day 1 would turn into after numYears.
   */
  public OnesBasedReturn toNonAnnualized(double numYears) {
    return onesBasedReturn(Math.pow(rawReturn.doubleValue(), numYears));
  }

  @Override
  public int compareTo(AnnuallyCompoundedAnnualizedReturn other) {
    return rawReturn.compareTo(other.rawReturn);
  }

  @Override
  public String toString() {
    return Strings.format("[ACAR %s ACAR]", rawReturn);
  }

}
