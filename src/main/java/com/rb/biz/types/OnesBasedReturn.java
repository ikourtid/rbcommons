package com.rb.biz.types;

import com.rb.biz.investing.quality.AnnuallyCompoundedAnnualizedReturn;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.biz.investing.quality.AnnuallyCompoundedAnnualizedReturn.annuallyCompoundedAnnualizedReturn;
import static com.rb.nonbiz.math.RBBigDecimals.bigDecimalInvert;

/**
 * Returns expressed so that 1.0 means 'no increase or decrease', 1.02 means "+ 2%",
 * and 0.97 means "-3%".
 *
 * <p> Contrast this with zero-based returns (no such class exists as of Aug 2017),
 * where the respective values would have been 0, 0.02, -0.03, respectively. </p>
 *
 * <p> Ones-based returns have the property that they can be multiplied to produce a valid ones-based return.
 * This is not the case with zero-based returns; e.g. a $100 portfolio that goes up 10% and down 10% is
 * worth $99 ($100 {@code ->} $110 {@code ->} $99), but zero-based returns of +0.1 and -0.1 would give 0 (if added). </p>
 */
public class OnesBasedReturn extends PreciseValue<OnesBasedReturn> {

  public static final OnesBasedReturn FLAT_RETURN = new OnesBasedReturn(BigDecimal.ONE);
  private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000);

  private static final BigDecimal MIN_ALLOWABLE_RETURN_BIGDECIMAL = BigDecimal.valueOf(0.0001);
  private static final BigDecimal MAX_ALLOWABLE_RETURN_BIGDECIMAL = BigDecimal.valueOf(10_000);

  private OnesBasedReturn(BigDecimal onesBasedReturn) {
    super(onesBasedReturn);
  }

  /**
   * <p> Note that some very lossy returns may not be representable when using continuous compounding.
   * Take a portfolio that has dropped 20% over 2 months. </p>
   *
   * {@code final value = start value * e ^ (r * t)}
   * {@code ln(final / start) = r * t}
   * {@code ln(0.8) = r * .2}
   * {@code r = ln(0.8)/.2 = -1.11 (zero based)} - so that implies a -111% annual return, which is not possible for a single year.
   *
   * <p> Keep this in mind in case some mysterious exception takes you here! </p>
   */
  public static OnesBasedReturn onesBasedReturn(BigDecimal onesBasedReturn) {
    if (onesBasedReturn.compareTo(MIN_ALLOWABLE_RETURN_BIGDECIMAL) < 0) {
      throw new IllegalArgumentException(Strings.format(
          "OnesBasedReturn= %s < %s (safest min return allowed)",
          onesBasedReturn, MIN_ALLOWABLE_RETURN_BIGDECIMAL));
    }
    if (onesBasedReturn.compareTo(MAX_ALLOWABLE_RETURN_BIGDECIMAL) > 0) {
      throw new IllegalArgumentException(Strings.format(
          "OnesBasedReturn= %s > %s (safest max return allowed)",
          onesBasedReturn, MAX_ALLOWABLE_RETURN_BIGDECIMAL));
    }
    return new OnesBasedReturn(onesBasedReturn);
  }

  public static OnesBasedReturn onesBasedReturnFromTo(Price startingPrice, Price endingPrice) {
    return endingPrice.divide(startingPrice);
  }

  public static OnesBasedReturn onesBasedReturnFromTo(Money startingValue, Money endingValue) {
    return onesBasedReturn(endingValue.divide(startingValue));
  }

  public static OnesBasedReturn onesBasedReturn(double onesBasedReturn) {
    return onesBasedReturn(BigDecimal.valueOf(onesBasedReturn));
  }

  /**
   * A bit more explicit name, and includes error-checks
   */
  public static OnesBasedReturn onesBasedLoss(double onesBasedReturn) {
    return onesBasedLoss(BigDecimal.valueOf(onesBasedReturn));
  }

  public static OnesBasedReturn onesBasedLoss(BigDecimal onesBasedReturn) {
    RBPreconditions.checkArgument(
        onesBasedReturn.compareTo(BigDecimal.ONE) <= 0,
        "You are using the explicit loss constructor, but with a gain: %s",
        onesBasedReturn);
    return onesBasedReturn(onesBasedReturn);
  }

  public static OnesBasedReturn onesBasedGain(double onesBasedReturn) {
    return onesBasedGain(BigDecimal.valueOf(onesBasedReturn));
  }

  public static OnesBasedReturn onesBasedGain(BigDecimal onesBasedReturn) {
    RBPreconditions.checkArgument(
        onesBasedReturn.compareTo(BigDecimal.ONE) >= 0,
        "You are using the explicit gain constructor, but with a loss: %s",
        onesBasedReturn);
    return onesBasedReturn(onesBasedReturn);
  }

  /**
   * Note that this is not a straight subtraction, as the returns are centered on 1.
   *
   * There are 2 reasonable semantics (forgetting about the 1-centering for now)
   * ratio: thisReturn / benchmarkReturn
   * difference: thisReturn - benchmarkReturn
   *
   * The difference semantics are a bit easier to interpret. The disadvantages are:
   * a) at the limit (obviously not using reasonable values), if the benchmark was up 50%,
   *    and we were down 60%, is the return -110? (impossible, as that would result in a negative portfolio value)
   * b) there is no way to relate the individual over-the-benchmark returns with the whole-portfolio ones,
   *    whereas that is possible with the ratio ones.
   * Say the ideal returns are up 10% and 30%, which compound to 43% up (1.1 * 1.3)
   * and that our returns are up 15% and 50%, which compound to 72.5% up (1.15 * 1.5)
   * The diffs are +5% and +20%, which compound to 26% up (1.05 * 1.20). That number is not equal to
   * either 72.5% - 43% or 172.5 / 143
   *
   * However, using ratio calculations, the diff ratios are 1.15 / 1.10 and 1.50 / 1.30, which compound to 20.69% up
   * (1.15 / 1.10) * (1.50 / 1.30) = 1.20629371
   * which is the same as the ratio of the different performances over the 2-day history. You just need to rewrite the
   * above:
   * (actual return over 2 days) / (benchmark return over 2 days) = (1.15 * 1.50) / (1.10 * 1.30)
   *
   * So using the ratio semantics, product(tracking diff) = tracking diff for entire period.
   *
   * With small values, this doesn't make much of a difference. But it can't hurt to be precise here.
   */
  public OnesBasedReturn getReturnRatioOverBenchmark(OnesBasedReturn benchmarkReturn) {
    return onesBasedReturn(asBigDecimal().divide(benchmarkReturn.asBigDecimal(), DEFAULT_MATH_CONTEXT));
  }

  public BigDecimal asZeroBasedReturnBigDecimal() {
    return asBigDecimal().subtract(BigDecimal.ONE);
  }

  public double asZeroBasedReturnDouble() {
    return doubleValue() - 1;
  }

  public OnesBasedReturn toGain() {
    return this.compareTo(FLAT_RETURN) >= 0
        ? this
        : onesBasedReturn(bigDecimalInvert(asBigDecimal()));
  }

  public String toBasisPoints(int scale) {
    return toBasisPoints(scale, true);
  }

  public String toBasisPoints(int scale, boolean includeSuffix) {
    BigDecimal bps = asZeroBasedReturnBigDecimal().multiply(TEN_THOUSAND).setScale(scale, RoundingMode.HALF_EVEN);
    return includeSuffix ? String.format("%s bps", bps) : bps.toString();
  }

  public OnesBasedReturn compoundWith(OnesBasedReturn otherReturn) {
    return onesBasedReturn(this.asBigDecimal().multiply(otherReturn.asBigDecimal(), DEFAULT_MATH_CONTEXT));
  }

  public OnesBasedReturn residualReturnOver(OnesBasedReturn otherReturn) {
    return onesBasedReturn(this.asBigDecimal().divide(otherReturn.asBigDecimal(), DEFAULT_MATH_CONTEXT));
  }

  /**
   * This is a weird name, but I can't think of a better one.
   * This converts returns from one time scale to another.
   * Say this is a 1% daily return
   * (measured in pure trading days, NOT averaged over all days including holidays and weekends).
   * Then this.timeScaleAdjust(252) is a 1.01^252 ~= 1227.4% return in annual terms; it is not 252%.
   * Also, no square root of time is applicable here; this applies to tracking errors and measures of variability.
   * Likewise, we can convert an annualized return (annualized returns always include holidays and weekends)
   * to a 'daily-ized' one (the 'market days only' variety, again) by calling this.timeScaleAdjust(1 / 252).
   */
  public OnesBasedReturn timeScaleAdjust(double exp) {
    RBPreconditions.checkArgument(exp > 0);
    return onesBasedReturn(Math.pow(doubleValue(), exp));
  }

  // E.g. if this object represents returns up 1 basis point over 5 calendar days,
  // then this means *roughly* 73 bps per year
  public AnnuallyCompoundedAnnualizedReturn toAnnuallyCompoundedAnnualizedReturn(long numCalendarDays) {
    RBPreconditions.checkArgument(numCalendarDays > 0);
    return annuallyCompoundedAnnualizedReturn(timeScaleAdjust(365.0 / numCalendarDays));
  }

}
