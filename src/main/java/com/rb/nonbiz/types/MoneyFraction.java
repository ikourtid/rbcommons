package com.rb.nonbiz.types;

import com.rb.biz.types.Money;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;

/**
 * Similar to {@link MoneyUnitFraction}.
 *
 * <p> {@code MoneyFraction} includes the possibility of the numerator being greater than the denominator.
 * The use case was for {@code PortfolioTurnover} &gt; 100%. </p>
 *
 * @see MoneyUnitFraction
 */
public class MoneyFraction extends PreciseValue<MoneyFraction> {

  public static final MoneyFraction ZERO_MONEY_FRACTION = moneyFraction(ZERO_MONEY, money(1));
  private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000);

  private final Money numerator;
  private final Money denominator;

  private MoneyFraction(Money numerator, Money denominator) {
    super(denominator.isZero() ? BigDecimal.ZERO : numerator.divide(denominator));
    this.numerator = numerator;
    this.denominator = denominator;
  }

  public static MoneyFraction moneyFraction(Money numerator, Money denominator) {
    RBPreconditions.checkArgument(
        denominator.isGreaterThan(ZERO_MONEY) || numerator.isZero(),
        "MoneyUnitFraction cannot have denominator = 0 but numerator %s > 0",
        numerator);
    return new MoneyFraction(numerator, denominator);
  }

  public Money getNumerator() {
    return numerator;
  }

  public Money getDenominator() {
    return denominator;
  }

  @Override
  public String toString() {
    return toPercentString(2);
  }

  public String toPercentString(int scale) {
    return toPercentString(scale, true);
  }

  public String toPercentString(int scale, boolean includePercentSign) {
    String pct = String.format("%." + scale + "f", 100 * doubleValue());
    return includePercentSign ? Strings.format("%s %", pct) : pct;
  }

  public String toBasisPoints(int scale) {
    return toBasisPoints(scale, true);
  }

  public String toBasisPoints(int scale, boolean includeSuffix) {
    BigDecimal bps = asBigDecimal().multiply(TEN_THOUSAND).setScale(scale, RoundingMode.HALF_EVEN);
    return includeSuffix ? String.format("%s bps", bps) : bps.toString();
  }

}
