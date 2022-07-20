package com.rb.biz.types.trading;

import com.google.common.collect.Iterables;
import com.rb.nonbiz.text.Strings;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Holds a positive quantity. That is, holds a value x such that {@code x > 0}.
 *
 * @see NonNegativeQuantity
 * @see SignedQuantity
 */
public class PositiveQuantity extends NonNegativeQuantity {

  public static final PositiveQuantity POSITIVE_QUANTITY_1 = positiveQuantity(1);

  protected PositiveQuantity(BigDecimal quantity) {
    super(quantity);
  }

  public static PositiveQuantity positiveQuantity(double quantityAsDouble) {
    return positiveQuantity(BigDecimal.valueOf(quantityAsDouble));
  }

  public static PositiveQuantity positiveQuantity(BigDecimal quantity) {
    if (quantity.signum() < 1) {
      throw new IllegalArgumentException(Strings.format(
          "Attempt to construct a PositiveQuantity with %s <= 0", quantity));
    }
    return new PositiveQuantity(quantity);
  }

  public static PositiveQuantity sumPositiveQuantities(Collection<PositiveQuantity> positiveQuantities) {
    // optimization
    if (positiveQuantities.size() == 1) {
      return Iterables.getOnlyElement(positiveQuantities);
    }
    BigDecimal sum = BigDecimal.ZERO;
    for (PositiveQuantity positiveQuantity : positiveQuantities) {
      sum = sum.add(positiveQuantity.asBigDecimal());
    }
    return positiveQuantity(sum);
  }

  public static PositiveQuantity sumPositiveQuantities(Stream<PositiveQuantity> positiveQuantityStream) {
    return positiveQuantityStream
        .reduce(PositiveQuantity::add)
        .orElseThrow( () -> new IllegalArgumentException("You must be adding at least 1 PositiveQuantity"));
  }

  public static PositiveQuantity sumPositiveQuantities(PositiveQuantity...positiveQuantities) {
    // optimization
    if (positiveQuantities.length == 1) {
      return positiveQuantities[0];
    }
    BigDecimal sum = BigDecimal.ZERO;
    for (PositiveQuantity positiveQuantity : positiveQuantities) {
      sum = sum.add(positiveQuantity.asBigDecimal());
    }
    return positiveQuantity(sum);
  }

  public static PositiveQuantity max(PositiveQuantity q1, PositiveQuantity q2) {
    return q1.isGreaterThan(q2) ? q1 : q2;
  }

  public static PositiveQuantity min(PositiveQuantity q1, PositiveQuantity q2) {
    return q1.isLessThan(q2) ? q1 : q2;
  }

  public PositiveQuantity add(PositiveQuantity other) {
    return positiveQuantity(asBigDecimal().add(other.asBigDecimal()));
  }

  public PositiveQuantity subtract(PositiveQuantity subtrahend) {
    return positiveQuantity(asBigDecimal().subtract(subtrahend.asBigDecimal()));
  }

  @Override
  public PositiveQuantity multiply(BigDecimal multiplier) {
    return positiveQuantity(asBigDecimal().multiply(multiplier));
  }

  public SignedQuantity toSignedQuantity() {
    return signedQuantity(asBigDecimal());
  }

}
