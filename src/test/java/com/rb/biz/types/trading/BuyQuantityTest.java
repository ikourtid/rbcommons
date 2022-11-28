package com.rb.biz.types.trading;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.Price.ZERO_PRICE;
import static com.rb.biz.types.Price.price;
import static com.rb.biz.types.trading.BuyQuantity.buyQuantity;
import static com.rb.biz.types.trading.PositiveQuantity.positiveQuantity;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;

public class BuyQuantityTest {

  @Test
  public void quantityMustBePositive_otherwiseThrows() {
    assertIllegalArgumentException( () -> buyQuantity(-1));
    assertIllegalArgumentException( () -> buyQuantity(0));
    BuyQuantity doesNotThrow = buyQuantity(1);
  }

  @Test
  public void testPositiveQuantityConstructor() {
    assertEquals(buyQuantity(123), buyQuantity(positiveQuantity(123)));
  }

  @Test
  public void testBuyQuantityArithmetic() {
    BuyQuantity buyQuantity11 = buyQuantity(11);
    BuyQuantity buyQuantity33 = buyQuantity(33);

    assertEquals(buyQuantity(44),  buyQuantity11.add(buyQuantity33));
    assertEquals(buyQuantity(22),  buyQuantity33.subtract(buyQuantity11));
    assertEquals(buyQuantity(66),  buyQuantity33.multiply(BigDecimal.valueOf(2)));
    assertEquals(money(22),        buyQuantity11.multiply(price(2)));
    assertEquals(money(22),        buyQuantity11.multiply(money(2)));
    assertEquals(buyQuantity(1.1), buyQuantity11.divide(BigDecimal.TEN));

    // adding and subtracting should be inverses
    assertEquals(buyQuantity33, buyQuantity33.add(buyQuantity11).subtract(buyQuantity11));
    assertEquals(buyQuantity33, buyQuantity33.subtract(buyQuantity11).add(buyQuantity11));

    // multiplying and dividing should be inverses
    assertAlmostEquals(
        buyQuantity33,
        buyQuantity33
            .multiply(BigDecimal.valueOf(123.45))
            .divide(  BigDecimal.valueOf(123.45)),
        1e-8);
    assertAlmostEquals(
        buyQuantity33,
        buyQuantity33
            .divide(  BigDecimal.valueOf(123.45))
            .multiply(BigDecimal.valueOf(123.45)),
        1e-8);

    // can't have buyQuantity(0)
    assertIllegalArgumentException( () -> buyQuantity11.subtract(buyQuantity11));
    assertIllegalArgumentException( () -> buyQuantity11.multiply(BigDecimal.ZERO));

    // but can have money(0)
    assertEquals(ZERO_MONEY, buyQuantity11.multiply(ZERO_MONEY));
    assertEquals(ZERO_MONEY, buyQuantity11.multiply(ZERO_PRICE));

    // Test sum
    ArrayList<BuyQuantity> buyQuantities = new ArrayList<BuyQuantity>();
    buyQuantities.add(buyQuantity11);
    buyQuantities.add(buyQuantity33);
    assertEquals(buyQuantity(44), BuyQuantity.sumBuyQuantities(buyQuantities.iterator()));
  }

}