package com.rb.nonbiz.types;

import com.rb.biz.types.Money;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.Optional;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

/**
 * Useful to represent stuff such as "out of $x traded, $y was bought".
 * Basically, whenever the numerator is {@code <=} than the denominator.
 * It's also OK if it's $0 out of $0.
 */
public class MoneyUnitFraction extends PreciseValue<MoneyUnitFraction> {

  private final Money numerator;
  private final Money denominator;

  private MoneyUnitFraction(Money numerator, Money denominator) {
    super(denominator.isZero() ? BigDecimal.ZERO : numerator.divide(denominator));
    this.numerator = numerator;
    this.denominator = denominator;
  }

  public static MoneyUnitFraction moneyUnitFraction(Money numerator, Money denominator) {
    RBPreconditions.checkArgument(
        numerator.isLessThanOrEqualTo(denominator),
        "MoneyUnitFraction must cannot have numerator %s > denominator %s",
        numerator, denominator);
    RBPreconditions.checkArgument(
        !denominator.isZero() || numerator.isZero(),
        "MoneyUnitFraction cannot have numerator %s > 0 and denominator %s",
        numerator, denominator);
    return new MoneyUnitFraction(numerator, denominator);
  }

  public static MoneyUnitFraction emptyMoneyUnitFraction() {
    return moneyUnitFraction(ZERO_MONEY, ZERO_MONEY);
  }

  public Money getNumerator() {
    return numerator;
  }

  public Money getDenominator() {
    return denominator;
  }

  public Optional<UnitFraction> asFraction() {
    return denominator.isZero()
        ? Optional.empty()
        : Optional.of(unitFraction(asBigDecimal()));
  }

  @Override
  public String toString() {
    return Strings.format("%s ( $ %s of $ %s )", asFraction(), numerator, denominator);
  }

}
