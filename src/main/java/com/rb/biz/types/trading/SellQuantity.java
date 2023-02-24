package com.rb.biz.types.trading;

import com.rb.nonbiz.text.Strings;

import java.math.BigDecimal;
import java.util.Iterator;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;

/**
 * The share quantity to <i>sell</i> for a single instrument.
 *
 * <p> We use a separate class to represent a sell quantity
 * for better type safety throughout the code. </p>
 *
 * <p> Note that a SellQuantity <i>cannot be zero</i> (or negative). If we are
 * not selling an instrument, we cannot create a zero-share SellQuantity for it. </p>
 *
 * @see BuyQuantity
 */
public class SellQuantity extends PositiveQuantity {

  private SellQuantity(BigDecimal quantity) {
    super(quantity);
  }

  public static SellQuantity sellQuantity(double quantityAsDouble) {
    return sellQuantity(BigDecimal.valueOf(quantityAsDouble));
  }

  public static SellQuantity sellQuantity(BigDecimal quantity) {
    if (quantity.signum() < 1) {
      throw new IllegalArgumentException(smartFormat(
          "Attempt to construct a SellQuantity with quantity %s <= 0", quantity));
    }
    return new SellQuantity(quantity);
  }

  public static SellQuantity sellQuantity(PositiveQuantity quantity) {
    return sellQuantity(quantity.asBigDecimal());
  }

  /**
   * Add one SellQuantity to another.
   */
  public SellQuantity add(SellQuantity other) {
    return sellQuantity(asBigDecimal().add(other.asBigDecimal()));
  }

  /**
   * Subtract one SellQuantity from another.
   */
  public SellQuantity subtract(SellQuantity subtrahend) {
    return sellQuantity(asBigDecimal().subtract(subtrahend.asBigDecimal()));
  }

  @Override
  public SellQuantity multiply(BigDecimal multiplier) {
    return sellQuantity(asBigDecimal().multiply(multiplier));
  }

  public SellQuantity divide(BigDecimal divisor) {
    return sellQuantity(asBigDecimal().divide(divisor, DEFAULT_MATH_CONTEXT));
  }

  public static SellQuantity sumSellQuantities(Iterator<SellQuantity> items) {
    BigDecimal sum = BigDecimal.ZERO;
    while (items.hasNext()) {
      sum = sum.add(items.next().asBigDecimal());
    }
    return sellQuantity(sum);
  }

}
