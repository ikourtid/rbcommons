package com.rb.biz.types.trading;

import com.rb.biz.types.Money;
import com.rb.biz.types.Price;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.stream.Stream;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

/**
 * Holds a non-negative quantity in shares of a security. That is, holds a value x such that {@code x >= 0}.
 *
 * @see SignedQuantity
 */
public class NonNegativeQuantity extends SignedQuantity {

  public static final NonNegativeQuantity ZERO_NON_NEGATIVE_QUANTITY = new NonNegativeQuantity(BigDecimal.ZERO);

  protected NonNegativeQuantity(BigDecimal quantity) {
    super(quantity);
  }

  public static NonNegativeQuantity nonNegativeQuantity(double quantityAsDouble) {
    return nonNegativeQuantity(BigDecimal.valueOf(quantityAsDouble));
  }

  /** Super-slightly negative numbers become 0 (useful if we subtract equal numbers that are 'numerically' off),
   * and super-slightly positive ones are 0 (since usually that's the intent).
   */
  public static NonNegativeQuantity nonNegativeQuantity(BigDecimal quantity) {
    BigDecimal snapped = snapToZero(quantity);
    if (snapped.signum() == 0) {
      return ZERO_NON_NEGATIVE_QUANTITY;
    }
    if (quantity.signum() == -1) {
      throw new IllegalArgumentException(smartFormat(
          "Attempt to construct a NonNegativeQuantity with %s < 0", quantity));
    }
    return new NonNegativeQuantity(quantity);
  }

  public Money multiply(Price price) {
    return money(asBigDecimal().multiply(price.asBigDecimal()));
  }

  public Money multiply(Money amount) {
    return money(asBigDecimal().multiply(amount.asBigDecimal()));
  }

  public NonNegativeQuantity add(NonNegativeQuantity other) {
    return nonNegativeQuantity(asBigDecimal().add(other.asBigDecimal()));
  }

  public NonNegativeQuantity subtract(NonNegativeQuantity other) {
    return nonNegativeQuantity(asBigDecimal().subtract(other.asBigDecimal()));
  }

  public SignedQuantity subtractToSigned(NonNegativeQuantity other) {
    return signedQuantity(asBigDecimal().subtract(other.asBigDecimal()));
  }

  public static NonNegativeQuantity max(NonNegativeQuantity q1, NonNegativeQuantity q2) {
    return q1.isGreaterThan(q2) ? q1 : q2;
  }

  public static NonNegativeQuantity min(NonNegativeQuantity q1, NonNegativeQuantity q2) {
    return q1.isLessThan(q2) ? q1 : q2;
  }

  public static NonNegativeQuantity sumNonNegativeQuantities(
      NonNegativeQuantity first,
      NonNegativeQuantity second,
      NonNegativeQuantity...rest) {
    // optimization
    if (rest.length == 0) {
      return first.add(second);
    }
    BigDecimal sum = first.asBigDecimal().add(second.asBigDecimal());
    for (NonNegativeQuantity item : rest) {
      sum = sum.add(item.asBigDecimal());
    }
    return nonNegativeQuantity(sum);
  }

  public static NonNegativeQuantity sumNonNegativeQuantities(Stream<NonNegativeQuantity> stream) {
    return sumNonNegativeQuantities(stream.iterator());
  }

  public static NonNegativeQuantity sumNonNegativeQuantities(Iterator<NonNegativeQuantity> items) {
    BigDecimal sum = null;
    while (items.hasNext()) {
      NonNegativeQuantity item = items.next();
      // small performance optimization, so in the case of 1 item we just return that
      if (!items.hasNext() && sum == null) {
        return item;
      }
      sum = sum == null
        ? item.asBigDecimal()
        : sum.add(item.asBigDecimal());
    }
    return sum == null ? ZERO_NON_NEGATIVE_QUANTITY : nonNegativeQuantity(sum);
  }

  public String toString(int precision) {
    return String.format("%." + precision + "f", doubleValue());
  }

}
