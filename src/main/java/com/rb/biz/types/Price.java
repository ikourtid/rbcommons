package com.rb.biz.types;

import com.rb.biz.types.trading.NonNegativeQuantity;
import com.rb.biz.types.trading.SignedQuantity;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.OnesBasedReturn.onesBasedReturn;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;
import static com.rb.nonbiz.math.RBBigDecimals.bigDecimalAverage;

/**
 * Holds a price.
 *
 * <p> Just a thin wrapper around {@link PreciseValue}. </p>
 */
public class Price extends PreciseValue<Price> {

  /**
   * You cannot construct a zero price yourself, as a safety check.
   * However, we do allow a zero price object - sometimes it's useful as a sentinel value.
   */
  public static Price ZERO_PRICE = new Price(BigDecimal.ZERO);

  private Price(BigDecimal price) {
    super(price);
  }

  private Price(BigDecimal price, double doubleValue) {
    super(price, doubleValue);
  }

  public static Price price(double priceAsDouble) {
    if (priceAsDouble <= 0) {
      throw new IllegalArgumentException(Strings.format("price must be >0 but was %s", priceAsDouble));
    }
    return new Price(BigDecimal.valueOf(priceAsDouble), priceAsDouble);
  }

  public static Price price(BigDecimal price) {
    if (price.signum() < 1) {
      throw new IllegalArgumentException(Strings.format("price must be >0 but was %s", price));
    }
    return new Price(price);
  }

  public OnesBasedReturn divide(Price endingPrice) {
    return onesBasedReturn(this.asBigDecimal().divide(endingPrice.asBigDecimal(), DEFAULT_MATH_CONTEXT));
  }

  public Price add(Price other) {
    return price(asBigDecimal().add(other.asBigDecimal()));
  }

  public Price subtract(Price other) {
    return price(asBigDecimal().subtract(other.asBigDecimal()));
  }

  public Price multiply(BigDecimal multiplier) {
    return price(asBigDecimal().multiply(multiplier, DEFAULT_MATH_CONTEXT));
  }

  public Price multiply(double multiplier) {

    return price(doubleValue() * multiplier);
  }

  public Price multiply(OnesBasedReturn onesBasedReturn) {
    return price(asBigDecimal().multiply(onesBasedReturn.asBigDecimal(), DEFAULT_MATH_CONTEXT));
  }

  public Money multiply(NonNegativeQuantity nonNegativeQuantity) {
    return money(asBigDecimal().multiply(nonNegativeQuantity.asBigDecimal()));
  }

  public SignedMoney multiply(SignedQuantity signedQuantity) {
    return signedMoney(asBigDecimal().multiply(signedQuantity.asBigDecimal()));
  }

  public Price divide(BigDecimal divisor) {
    return price(asBigDecimal().divide(divisor, DEFAULT_MATH_CONTEXT));
  }

  public Price divide(double divisor) {
    RBPreconditions.checkArgument(
        divisor > 0,
        "You can only divide a price by a positive divisor; got %s",
        divisor);
    return price(doubleValue() / divisor);
  }

  public static Price minPrice(Stream<Price> prices) {
    return getOrThrow(
        prices.min(Price::compareTo),
        "Cannot retrieve the min of 0 prices");
  }

  public static Price maxPrice(Stream<Price> prices) {
    return getOrThrow(
        prices.max(Price::compareTo),
        "Cannot retrieve the max of 0 prices");
  }

  public static Price averagePrice(Price price1, Price price2) {
    return price(bigDecimalAverage(price1.asBigDecimal(), price2.asBigDecimal()));
  }

  public String toString(int precision) {
    return String.format("%." + precision + "f", doubleValue());
  }

}
