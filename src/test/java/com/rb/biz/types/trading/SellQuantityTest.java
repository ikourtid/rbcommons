package com.rb.biz.types.trading;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.Price.ZERO_PRICE;
import static com.rb.biz.types.Price.price;
import static com.rb.biz.types.trading.BuyQuantity.buyQuantity;
import static com.rb.biz.types.trading.PositiveQuantity.positiveQuantity;
import static com.rb.biz.types.trading.SellQuantity.sellQuantity;
import static com.rb.biz.types.trading.SellQuantity.sumSellQuantities;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;

public class SellQuantityTest {

  @Test
  public void quantityMustBePositive_otherwiseThrows() {
    assertIllegalArgumentException( () -> sellQuantity(-1));
    assertIllegalArgumentException( () -> sellQuantity(0));
    SellQuantity doesNotThrow = sellQuantity(1);
  }

  @Test
  public void testPositiveQuantityConstructor() {
    assertEquals(sellQuantity(123), sellQuantity(positiveQuantity(123)));
  }

  @Test
  public void testSellQuantityArithmetic() {
    SellQuantity sellQuantity11 = sellQuantity(11);
    SellQuantity sellQuantity33 = sellQuantity(33);

    assertEquals(sellQuantity(44),  sellQuantity11.add(sellQuantity33));
    assertEquals(sellQuantity(22),  sellQuantity33.subtract(sellQuantity11));
    assertEquals(sellQuantity(66),  sellQuantity33.multiply(BigDecimal.valueOf(2)));
    assertEquals(money(22),         sellQuantity11.multiply(price(2)));
    assertEquals(money(22),         sellQuantity11.multiply(money(2)));
    assertEquals(sellQuantity(1.1), sellQuantity11.divide(BigDecimal.TEN));

    // adding and subtracting should be inverses
    assertEquals(sellQuantity33, sellQuantity33.add(sellQuantity11).subtract(sellQuantity11));
    assertEquals(sellQuantity33, sellQuantity33.subtract(sellQuantity11).add(sellQuantity11));

    // multiplying and dividing should be inverses
    assertAlmostEquals(
        sellQuantity33,
        sellQuantity33
            .multiply(BigDecimal.valueOf(123.45))
            .divide(  BigDecimal.valueOf(123.45)),
        1e-8);
    assertAlmostEquals(
        sellQuantity33,
        sellQuantity33
            .divide(  BigDecimal.valueOf(123.45))
            .multiply(BigDecimal.valueOf(123.45)),
        1e-8);

    // can't have sellQuantity(0)
    assertIllegalArgumentException( () -> sellQuantity11.subtract(sellQuantity11));
    assertIllegalArgumentException( () -> sellQuantity11.multiply(BigDecimal.ZERO));

    // but can have money(0)
    assertEquals(ZERO_MONEY, sellQuantity11.multiply(ZERO_MONEY));
    assertEquals(ZERO_MONEY, sellQuantity11.multiply(ZERO_PRICE));

    // Test sum
    ArrayList<SellQuantity> sellQuantities = newArrayList(sellQuantity11, sellQuantity33);
    assertEquals(sellQuantity(44), sumSellQuantities(sellQuantities.iterator()));
  }

}