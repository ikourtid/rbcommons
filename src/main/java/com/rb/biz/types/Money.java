package com.rb.biz.types;

import com.rb.biz.types.trading.BuyQuantity;
import com.rb.biz.types.trading.NonNegativeQuantity;
import com.rb.biz.types.trading.PositiveQuantity;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.biz.types.Price.price;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.trading.BuyQuantity.buyQuantity;
import static com.rb.biz.types.trading.NonNegativeQuantity.nonNegativeQuantity;

/**
 * Represents a non-negative dollar amount.
 *
 * <p> Instead of calling this NonNegativeMoney, we just call it Money,
 * since in almost all cases, we want to enforce that the amounts are {@code >= 0}  $0.
 * For the cases where they can be negative, see {@link SignedMoney}. </p>
 */
public class Money extends PreciseValue<Money> {

  public static final Money ZERO_MONEY = new Money(BigDecimal.ZERO);
  public static final Boolean WITH_DOLLAR_SIGN_PREFIX = true;
  public static final Boolean WITHOUT_DOLLAR_SIGN_PREFIX = false;
  private static final BigDecimal ONE_MILLION = new BigDecimal(1_000_000);

  private Money(BigDecimal amount) {
    super(amount);
  }

  private Money(BigDecimal amount, double doubleValue) {
    super(amount, doubleValue);
  }

  public static Money money(double amount) {
    return money(BigDecimal.valueOf(amount), amount);
  }

  public static Money money(BigDecimal amount) {
    if (amount.signum() == -1) {
      throw new IllegalArgumentException(smartFormat(
          "Money cannot be negative at %s; use SignedMoney if you want that", amount));
    }
    return new Money(amount);
  }

  protected static Money money(BigDecimal amount, double doubleValue) {
    // This currently only gets called from SignedMoney#toMoney, for the cases where doubleValue is known already.
    // For speed reasons, we can just assume that the two numbers are the same (subject to some tiny epsilon),
    // since they came from a valid SignedMoney.
    // We do check for doubleValue < 0 though, since that's a cheap check .
    if (doubleValue < 0) {
      throw new IllegalArgumentException(smartFormat(
          "Money cannot be negative at %s; use SignedMoney if you want that", amount));
    }
    return new Money(amount, doubleValue);
  }

  public static Money sumMoney(Money first, Money second, Money...rest) {
    // optimization
    if (rest.length == 0) {
      return first.add(second);
    }
    BigDecimal sum = first.asBigDecimal().add(second.asBigDecimal());
    for (Money item : rest) {
      sum = sum.add(item.asBigDecimal());
    }
    return money(sum);
  }

  public static Money sumMoney(List<Money> items) {
    if (items.size() == 1) {
      return items.get(0);
    }
    return sumMoney(items.iterator());
  }

  public static Money sumMoney(Iterable<Money> items) {
    return sumMoney(items.iterator());
  }

  public static Money sumMoney(Stream<Money> items) {
    return sumMoney(items.iterator());
  }

  public static Money sumMoney(Iterator<Money> items) {
    // performance optimization for the case of 1 item
    BigDecimal sum = null;
    while (items.hasNext()) {
      BigDecimal thisItem = items.next().asBigDecimal();
      sum = sum == null
          ? thisItem
          : sum.add(thisItem);
    }
    return sum == null ? ZERO_MONEY : money(sum);
  }

  public Money add(Money otherMoney) {
    BigDecimal other = otherMoney.asBigDecimal();
    return other.signum() == 0
        ? this // Simple performance optimization when adding zero.
        : money(asBigDecimal().add(other));
  }

  public Money addOptional(Optional<Money> other) {
    return other.isPresent()
        ? this.add(other.get())
        : this;
  }

  /**
   * This will throw if the result is not &ge; 0.
   */
  public Money addSigned(SignedMoney other) {
    return money(asBigDecimal().add(other.asBigDecimal()));
  }

  public Money subtract(Money otherMoney) {
    BigDecimal other = otherMoney.asBigDecimal();
    if (other.signum() == 0) {
      return this; // Simple performance optimization when subtracting zero.
    }
    BigDecimal result = asBigDecimal().subtract(other);
    return BigDecimal.valueOf(-1e-8).compareTo(result) < 0 && result.signum() == -1
        ? ZERO_MONEY
        : money(result);
  }

  public Money subtract(Money otherMoney, String formatForErrorWhenMoneyIsNegative, Object ... args) {
    BigDecimal other = otherMoney.asBigDecimal();
    if (other.signum() == 0) {
      return this; // simple performance optimization when subtracting zero.
    }
    BigDecimal result = asBigDecimal().subtract(other);
    if (BigDecimal.valueOf(-1e-8).compareTo(result) < 0 && result.signum() == -1) {
      return ZERO_MONEY;
    }
    RBPreconditions.checkArgument(
        result.signum() >= 0,
        formatForErrorWhenMoneyIsNegative,
        args);
    return money(result);
  }

  public SignedMoney toSignedMoney() {
    return signedMoney(asBigDecimal());
  }

  public SignedMoney subtractToSigned(Money other) {
    return signedMoney(asBigDecimal().subtract(other.asBigDecimal()));
  }

  public SignedMoney subtractToSigned(SignedMoney other) {
    return signedMoney(asBigDecimal().subtract(other.asBigDecimal()));
  }

  public Money subtractAndFloorToZero(Money other) {
    BigDecimal difference = asBigDecimal().subtract(other.asBigDecimal());
    return difference.signum() < 0
        ? ZERO_MONEY
        : money(difference);
  }

  /**
   * This will throw if the result is not &ge; 0.
   */
  public Money subtractSigned(SignedMoney other) {
    return money(asBigDecimal().subtract(other.asBigDecimal()));
  }

  public Money multiply(double multiplier) {
    return multiply(BigDecimal.valueOf(multiplier));
  }

  public SignedMoney multiplyToSigned(double multiplier) {
    return signedMoney(asBigDecimal().multiply(BigDecimal.valueOf(multiplier)));
  }

  public Money multiply(long multiplier) {
    return multiply(BigDecimal.valueOf(multiplier));
  }

  public Money multiply(BigDecimal multiplier) {
    return money(this.asBigDecimal().multiply(multiplier));
  }

  public Money multiply(UnitFraction unitFraction) {
    return multiply(unitFraction.asBigDecimal());
  }

  public Money multiply(OnesBasedReturn onesBasedReturn) {
    return multiply(onesBasedReturn.asBigDecimal());
  }

  public BuyQuantity calculateBuyQuantity(Price price) {
    return buyQuantity(asBigDecimal().divide(price.asBigDecimal(), DEFAULT_MATH_CONTEXT));
  }

  public String toMillions() {
    return String.format("%.0fm", asBigDecimal().divide(ONE_MILLION, DEFAULT_MATH_CONTEXT).doubleValue());
  }

  @Override
  public String toString() {
    return String.format("$ %.2f", doubleValue());
  }

  public String toString(int maxPrecision) {
    return toString(maxPrecision, WITH_DOLLAR_SIGN_PREFIX);
  }

  public String toString(int maxPrecision, boolean dollarSignPrefix) {
    // print Money with at least 2 digits (pennies), unless the user asks for fewer
    int minPrecision = Math.min(2, maxPrecision);
    return toString(minPrecision, maxPrecision, dollarSignPrefix);
  }

  public String toString(int minPrecision, int maxPrecision, boolean dollarSignPrefix) {
    String formatStr = dollarSignPrefix ? "$ %s" : "%s";
    return Strings.format(formatStr, formatWithoutCommas(minPrecision, maxPrecision).format(doubleValue()));
  }

  public String toDollarsWithCommas() {
    return Strings.format("$ %s", FORMAT_AS_INTEGER_WITH_COMMAS.format(doubleValue()));
  }

  public BigDecimal divide(Money denominator) {
    if (denominator.isZero()) {
      throw new IllegalArgumentException(smartFormat("Cannot divide %s by $0", this));
    }
    return asBigDecimal().divide(denominator.asBigDecimal(), DEFAULT_MATH_CONTEXT);
  }

  public NonNegativeQuantity divide(Price price) {
    return nonNegativeQuantity(asBigDecimal().divide(price.asBigDecimal(), DEFAULT_MATH_CONTEXT));
  }

  public Price divide(PositiveQuantity positiveQuantity) {
    return price(asBigDecimal().divide(positiveQuantity.asBigDecimal(), DEFAULT_MATH_CONTEXT));
  }

  public Money divide(double denominator) {
    return divide(BigDecimal.valueOf(denominator));
  }

  public Money divide(BigDecimal denominator) {
    RBPreconditions.checkArgument(denominator.signum() > 0);
    return money(this.asBigDecimal().divide(denominator, DEFAULT_MATH_CONTEXT));
  }

  /** the static constructor called will throw if result is negative */
  public Money subtract(SignedMoney signedMoney) {
    return money(asBigDecimal().subtract(signedMoney.asBigDecimal()));
  }

  public Money roundToPennies() {
    return money(asBigDecimal().setScale(2, RoundingMode.HALF_EVEN));
  }

}
