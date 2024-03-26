package com.rb.nonbiz.types;

import com.rb.biz.types.Money;
import com.rb.biz.types.SignedMoney;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.Optional;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.SignedMoney.ZERO_SIGNED_MONEY;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;

/**
 * A ratio of {@link Money}.
 *
 * <p> Similar to {@link MoneyUnitFraction}, except that the numerator can also be negative - i.e.
 * it's a {@link SignedMoney}. Useful to represent stuff such as "out of $x held, gains / losses were $y", or
 * "out of $x held, tax due (or owed) is $y" </p>
 *
 * <p> It's also OK if it's $0 out of $0. </p>
 *
 * @see MoneyFraction
 * @see MoneyUnitFraction
 */
public class MoneySignedFraction extends PreciseValue<MoneySignedFraction> {

  private final SignedMoney numerator;
  private final Money denominator;

  private MoneySignedFraction(SignedMoney numerator, Money denominator) {
    super(denominator.isZero() ? BigDecimal.ZERO : numerator.asBigDecimal().divide(denominator.asBigDecimal(), DEFAULT_MATH_CONTEXT));
    this.numerator = numerator;
    this.denominator = denominator;
  }

  public static MoneySignedFraction moneySignedFraction(SignedMoney numerator, Money denominator) {
    RBPreconditions.checkArgument(
        !denominator.isZero() || numerator.isZero(),
        "MoneyUnitFraction cannot have numerator %s != 0 and denominator %s",
        numerator, denominator);
    return new MoneySignedFraction(numerator, denominator);
  }

  public static MoneySignedFraction emptyMoneySignedFraction() {
    return moneySignedFraction(ZERO_SIGNED_MONEY, ZERO_MONEY);
  }

  public SignedMoney getNumerator() {
    return numerator;
  }

  public Money getDenominator() {
    return denominator;
  }

  public Optional<SignedFraction> asSignedFraction() {
    return denominator.isZero()
        ? Optional.empty()
        : Optional.of(signedFraction(asBigDecimal()));
  }

  @Override
  public String toString() {
    return Strings.format("%s ( %s of %s )", asSignedFraction(), numerator, denominator);
  }

  public String toStringPercent(int maxPrecision, boolean includePercentSign) {
    // negative precisions could make sense (rounding 123.1 to 120.0 or 100.0), but we don't support them here
    RBPreconditions.checkArgument(
        0 <= maxPrecision,
        "Invalid maxPrecision %s",
        maxPrecision);
    String percentString = formatWithoutCommas(0, maxPrecision).format(doubleValue() * 100.0);
    return includePercentSign
           ? Strings.format("%s %", percentString)
           : percentString;
  }

}
