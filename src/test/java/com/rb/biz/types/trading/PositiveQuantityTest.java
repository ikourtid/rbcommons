package com.rb.biz.types.trading;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.rb.biz.types.trading.PositiveQuantity.positiveQuantity;
import static com.rb.biz.types.trading.PositiveQuantity.sumPositiveQuantities;
import static com.rb.biz.types.trading.SignedQuantity.signedQuantity;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

public class PositiveQuantityTest {

  @Test
  public void quantityMustBePositive() {
    assertIllegalArgumentException( () -> positiveQuantity(-0.01));
    assertIllegalArgumentException( () -> positiveQuantity(0));
    for (Double d : ImmutableList.of(0.01, 1.0, 100.0)) {
      PositiveQuantity willNotThrow = positiveQuantity(d);
    }
  }

  @Test
  public void testSum() {
    assertEquals(
        positiveQuantity(111),
        sumPositiveQuantities(ImmutableList.of(
            positiveQuantity(100),
            positiveQuantity(10),
            positiveQuantity(1))));
    assertEquals(
        positiveQuantity(100),
        sumPositiveQuantities(ImmutableList.of(
            positiveQuantity(100))));

    assertEquals(
        positiveQuantity(111),
        sumPositiveQuantities(
            positiveQuantity(100),
            positiveQuantity(10),
            positiveQuantity(1)));
    assertEquals(
        positiveQuantity(100),
        sumPositiveQuantities(
            positiveQuantity(100)));
  }

  @Test
  public void mustSumAtLeastOneNumber() {
    assertIllegalArgumentException( () -> sumPositiveQuantities(emptyList()));
  }

  @Test
  public void testSum_stream() {
    assertEquals(
        positiveQuantity(111),
        sumPositiveQuantities(Stream.of(
            positiveQuantity(100),
            positiveQuantity(10),
            positiveQuantity(1))));
    assertEquals(
        positiveQuantity(100),
        sumPositiveQuantities(Stream.of(
            positiveQuantity(100))));
  }

  @Test
  public void mustSumAtLeastOneNumber_stream() {
    assertIllegalArgumentException( () -> sumPositiveQuantities(Stream.empty()));
  }

  @Test
  public void subtract() {
    assertEquals(positiveQuantity(7), positiveQuantity(10).subtract(positiveQuantity(3)));
    assertIllegalArgumentException( () -> positiveQuantity(10).subtract(positiveQuantity(10)));
    assertIllegalArgumentException( () -> positiveQuantity(10).subtract(positiveQuantity(11)));
  }

  @Test
  public void noQuantitiesSpecified_sumCannotBePositive_throws() {
    assertIllegalArgumentException( () -> sumPositiveQuantities(emptyList()));
    assertIllegalArgumentException( () -> sumPositiveQuantities());
  }

  @Test
  public void testMinMax() {
    PositiveQuantity positiveQuantitySmall = positiveQuantity(1.23);
    PositiveQuantity positiveQuantityLarge = positiveQuantity(78.9);

    assertEquals(positiveQuantitySmall, PositiveQuantity.min(positiveQuantitySmall, positiveQuantitySmall));
    assertEquals(positiveQuantitySmall, PositiveQuantity.min(positiveQuantitySmall, positiveQuantityLarge));
    assertEquals(positiveQuantitySmall, PositiveQuantity.min(positiveQuantityLarge, positiveQuantitySmall));
    assertEquals(positiveQuantityLarge, PositiveQuantity.min(positiveQuantityLarge, positiveQuantityLarge));

    assertEquals(positiveQuantitySmall, PositiveQuantity.max(positiveQuantitySmall, positiveQuantitySmall));
    assertEquals(positiveQuantityLarge, PositiveQuantity.max(positiveQuantitySmall, positiveQuantityLarge));
    assertEquals(positiveQuantityLarge, PositiveQuantity.max(positiveQuantityLarge, positiveQuantitySmall));
    assertEquals(positiveQuantityLarge, PositiveQuantity.max(positiveQuantityLarge, positiveQuantityLarge));
  }

  @Test
  public void testMultiply() {
    // can't have positiveQuantity(0)
    assertIllegalArgumentException( () -> positiveQuantity(123.4).multiply(BigDecimal.ZERO));

    assertEquals(positiveQuantity(12.34), positiveQuantity(123.4).multiply(BigDecimal.valueOf(0.1)));
    assertEquals(positiveQuantity(123.4), positiveQuantity(123.4).multiply(BigDecimal.ONE));
    assertEquals(positiveQuantity(246.8), positiveQuantity(123.4).multiply(BigDecimal.valueOf(2)));
    assertEquals(positiveQuantity(1_234), positiveQuantity(123.4).multiply(BigDecimal.TEN));
  }

  @Test
  public void testToSignedQuantity() {
    assertEquals(signedQuantity(0.1234), positiveQuantity(0.1234).toSignedQuantity());
    assertEquals(signedQuantity(1),      positiveQuantity(1     ).toSignedQuantity());
    assertEquals(signedQuantity(123.4),  positiveQuantity(123.4 ).toSignedQuantity());
  }

}
