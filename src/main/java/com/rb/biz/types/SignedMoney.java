package com.rb.biz.types;

import com.rb.biz.types.trading.SignedQuantity;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.stream.Stream;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.trading.SignedQuantity.signedQuantity;

/**
 * A typesafe representation of money that also allows for negative values.
 *
 * <p> The plain {@link Money} class only allows {@code >=} 0 values, so you'll need this for those rare cases
 * where you want to represent a negative amount. </p>
 *
 * @see Money
 */
public class SignedMoney extends PreciseValue<SignedMoney> {

  public static final SignedMoney ZERO_SIGNED_MONEY = signedMoney(BigDecimal.ZERO);

  protected SignedMoney(BigDecimal value) {
    super(value);
  }

  protected SignedMoney(BigDecimal value, double doubleValue) {
    super(value, doubleValue);
  }

  public static SignedMoney signedMoney(double value) {
    return new SignedMoney(BigDecimal.valueOf(value), value);
  }

  public static SignedMoney signedMoney(BigDecimal value) {
    return new SignedMoney(value);
  }

  public SignedMoney abs() {
    return signedMoney(asBigDecimal().abs(DEFAULT_MATH_CONTEXT));
  }

  public SignedMoney add(SignedMoney other) {
    return signedMoney(asBigDecimal().add(other.asBigDecimal()));
  }

  public SignedMoney add(Money money) {
    return signedMoney(asBigDecimal().add(money.asBigDecimal()));
  }

  public SignedMoney subtract(SignedMoney other) {
    return signedMoney(asBigDecimal().subtract(other.asBigDecimal()));
  }

  public SignedMoney subtract(Money money) {
    return signedMoney(asBigDecimal().subtract(money.asBigDecimal()));
  }

  public SignedMoney multiply(BigDecimal multiplier) {
    return signedMoney(this.asBigDecimal().multiply(multiplier));
  }

  public SignedMoney multiply(UnitFraction multiplier) {
    return signedMoney(this.asBigDecimal().multiply(multiplier.asBigDecimal()));
  }

  public SignedMoney multiply(double scalar) {
    return signedMoney(doubleValue() * scalar);
  }

  public SignedMoney divide(double scalar) {
    return divide(BigDecimal.valueOf(scalar));
  }

  public SignedMoney divide(BigDecimal divisor) {
    return signedMoney(asBigDecimal().divide(divisor, DEFAULT_MATH_CONTEXT));
  }

  public SignedQuantity divide(Price price) {
    return signedQuantity(asBigDecimal().divide(price.asBigDecimal(), DEFAULT_MATH_CONTEXT));
  }

  public SignedMoney negate() {
    return signedMoney(asBigDecimal().negate());
  }

  public Money toMoney() {
    RBPreconditions.checkArgument(
        !isNegative(),
        "Cannot convert negative SignedMoney of %s to a plain Money",
        this);
    return (doubleValue != doubleValue) // doubleValue has not been precomputed yet
        ? money(asBigDecimal())
        : money(asBigDecimal(), doubleValue);
  }

  /**
   * Converts this {@link SignedMoney} to a (non-negative) {@link Money} value, or to $0
   * if this SignedMoney is negative.
   */
  public Money toMoneyWithFloorOfZero() {
    return isNegative()
        ? ZERO_MONEY
        : toMoney();
  }

  public static SignedMoney sumSignedMoney(SignedMoney first, SignedMoney second, SignedMoney...rest) {
    BigDecimal sum = first.asBigDecimal().add(second.asBigDecimal());
    for (SignedMoney item : rest) {
      sum = sum.add(item.asBigDecimal());
    }
    return signedMoney(sum);
  }

  public static SignedMoney sumSignedMoney(Iterable<SignedMoney> items) {
    return sumSignedMoney(items.iterator());
  }

  public static SignedMoney sumSignedMoney(Stream<SignedMoney> stream) {
    return sumSignedMoney(stream.iterator());
  }

  public static SignedMoney sumSignedMoney(Iterator<SignedMoney> items) {
    BigDecimal sum = BigDecimal.ZERO;
    while (items.hasNext()) {
      sum = sum.add(items.next().asBigDecimal());
    }
    return signedMoney(sum);
  }

  @Override
  public String toString() {
    return toString(2);
  }

  public String toString(int maxPrecision) {
    // print at least 2 digits for money (pennies), unless the user asks for fewer
    int minPrecision = Math.min(2, maxPrecision);
    return toDollarsWithoutCommas(minPrecision, maxPrecision);
  }

  public String toDollarsWithCommas() {
    return Strings.format("$ %s", FORMAT_AS_INTEGER_WITH_COMMAS.format(doubleValue()));
  }

  public String toDollarsWithoutCommas(int minPrecision, int maxPrecision) {
    // It's annoying, but sometimes we see negative zero, and apparently there's no easy way to avoid it.
    return Strings.format("$ %s", formatWithoutCommas(minPrecision, maxPrecision).format(
        Math.abs(doubleValue()) < 1e-12 ? 0.0 : doubleValue()));
  }

}
