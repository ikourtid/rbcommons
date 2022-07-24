package com.rb.biz.types.trading;

import com.rb.nonbiz.text.Strings;

import java.math.BigDecimal;
import java.util.Iterator;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;


/**
 * <p> The share quantity to <i>buy</i> for a single instrument. </p>
 *
 * <p> We use a separate class to represent a {@code BuyQuantity}
 * for better type safety throughout the code. </p>
 *
 * <p> Note that a {@code BuyQuantity} <i>cannot be zero</i> (or negative). If we are
 * not buying an instrument, we cannot create a zero-share {@code BuyQuantity} for it. </p>
 *
 * @see SellQuantity
 */
public class BuyQuantity extends PositiveQuantity {

  private BuyQuantity(BigDecimal quantity) {
    super(quantity);
  }

  public static BuyQuantity buyQuantity(double quantityAsDouble) {
    return buyQuantity(BigDecimal.valueOf(quantityAsDouble));
  }

  public static BuyQuantity buyQuantity(BigDecimal quantity) {
    if (quantity.signum() < 1) {
      throw new IllegalArgumentException(Strings.format(
          "Attempt to construct a BuyQuantity with quantity %s <= 0", quantity));
    }
    return new BuyQuantity(quantity);
  }

  public static BuyQuantity buyQuantity(PositiveQuantity positiveQuantity) {
    return buyQuantity(positiveQuantity.asBigDecimal());
  }

  /**
   * Add one BuyQuantity to another.
   */
  public BuyQuantity add(BuyQuantity quantityToAdd) {
    return buyQuantity(asBigDecimal().add(quantityToAdd.asBigDecimal()));
  }

  /**
   * Sell one BuyQuantity from another.
   */
  public BuyQuantity subtract(BuyQuantity quantityToSubtract) {
    return buyQuantity(asBigDecimal().subtract(quantityToSubtract.asBigDecimal()));
  }

  @Override
  public BuyQuantity multiply(BigDecimal multiplier) {
    return buyQuantity(asBigDecimal().multiply(multiplier));
  }

  public BuyQuantity divide(BigDecimal divisor) {
    return buyQuantity(asBigDecimal().divide(divisor, DEFAULT_MATH_CONTEXT));
  }

  public static BuyQuantity sumBuyQuantities(Iterator<BuyQuantity> items) {
    BigDecimal sum = BigDecimal.ZERO;
    while (items.hasNext()) {
      sum = sum.add(items.next().asBigDecimal());
    }
    return buyQuantity(sum);
  }

}
