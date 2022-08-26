package com.rb.biz.types.trading;

import com.rb.nonbiz.types.PreciseValue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.biz.types.trading.BuyQuantity.buyQuantity;
import static com.rb.biz.types.trading.NonNegativeQuantity.nonNegativeQuantity;
import static com.rb.biz.types.trading.PositiveQuantity.positiveQuantity;
import static com.rb.biz.types.trading.SellQuantity.sellQuantity;

/**
 * A quantity that can be positive, negative, or zero.
 *
 * <p> Instrument quantities are typically non-negative.
 * A {@code SignedQuantity} is useful because it makes it explicit that a quantity is allowed to be negative or zero,
 * since that's not a common case. </p>
 *
 * @see PreciseValue
 */
public class SignedQuantity extends PreciseValue<SignedQuantity> {

  public static final SignedQuantity ZERO_SIGNED_QUANTITY = new SignedQuantity(BigDecimal.ZERO);

  protected SignedQuantity(BigDecimal signedQuantity) {
    super(signedQuantity);
  }

  public static SignedQuantity signedQuantity(double signedQuantityAsDouble) {
    return new SignedQuantity(BigDecimal.valueOf(signedQuantityAsDouble));
  }

  public static SignedQuantity signedQuantity(BigDecimal signedQuantity) {
    return new SignedQuantity(signedQuantity);
  }

  public boolean isAlmostRound() {
    return
        asBigDecimal().setScale(8, BigDecimal.ROUND_HALF_EVEN).compareTo(
            asBigDecimal().setScale(0, BigDecimal.ROUND_HALF_EVEN)) == 0;
  }

  public BuyQuantity toBuyQuantity() {
    return buyQuantity(asBigDecimal());
  }

  public SellQuantity toSellQuantity() {
    return sellQuantity(negate().asBigDecimal());
  }

  public SignedQuantity negate() {
    return signedQuantity(asBigDecimal().negate(DEFAULT_MATH_CONTEXT));
  }

  public SignedQuantity multiply(BigDecimal multiplier) {
    return signedQuantity(asBigDecimal().multiply(multiplier));
  }

  public SignedQuantity abs() {
    return signedQuantity(asBigDecimal().abs());
  }

  public static SignedQuantity max(SignedQuantity q1, SignedQuantity q2) {
    return q1.isGreaterThan(q2) ? q1 : q2;
  }

  public static SignedQuantity min(SignedQuantity q1, SignedQuantity q2) {
    return q1.isLessThan(q2) ? q1 : q2;
  }

  public SignedQuantity round() {
    return round(0);
  }

  public SignedQuantity round(int scale) {
    return signedQuantity(asBigDecimal().setScale(scale, RoundingMode.HALF_UP));
  }

  @Override
  public String toString() {
    return isAlmostRound()
        ? asBigDecimal().setScale(0, RoundingMode.HALF_EVEN).toString()
        : asBigDecimal().toString();
  }

  public String toPercent(int precision) {
    return String.format("%." + precision + "f %%", doubleValue() * 100);
  }

  public SignedQuantity subtract(SignedQuantity subtrahend) {
    return signedQuantity(asBigDecimal().subtract(subtrahend.asBigDecimal()));
  }

  public SignedQuantity add(SignedQuantity remainingInCurrentLot) {
    return signedQuantity(asBigDecimal().add(remainingInCurrentLot.asBigDecimal()));
  }

  public PositiveQuantity toPositiveQuantity() {
    return positiveQuantity(asBigDecimal());
  }

  public NonNegativeQuantity toNonNegativeQuantity() {
    return nonNegativeQuantity(asBigDecimal());
  }

}
