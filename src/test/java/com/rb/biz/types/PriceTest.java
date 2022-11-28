package com.rb.biz.types;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.OnesBasedReturn.FLAT_RETURN;
import static com.rb.biz.types.OnesBasedReturn.onesBasedReturn;
import static com.rb.biz.types.Price.maxPrice;
import static com.rb.biz.types.Price.minPrice;
import static com.rb.biz.types.Price.price;
import static com.rb.biz.types.SignedMoney.ZERO_SIGNED_MONEY;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.trading.NonNegativeQuantity.nonNegativeQuantity;
import static com.rb.biz.types.trading.SignedQuantity.ZERO_SIGNED_QUANTITY;
import static com.rb.biz.types.trading.SignedQuantity.signedQuantity;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PriceTest {

  Price small  = price(6.99);
  Price medium = price(7.00);
  Price large  = price(7.01);

  Price twiceMedium = price(14.0);
  Price halfMedium  = price( 3.5);

  @Test
  public void testEquality() {
    assertEquals(price(6.99), price(6.99));
  }

  @Test
  public void zeroPrice_throws() {
    assertIllegalArgumentException( () -> price(0));
  }

  @Test
  public void negativePrice_throws() {
    assertIllegalArgumentException( () -> price(-0.0001));
  }

  @Test
  public void isLessThan() {
    assertTrue(!small.isLessThan(small));
    assertTrue(small.isLessThan(medium));
    assertTrue(small.isLessThan(large));

    assertTrue(!medium.isLessThan(small));
    assertTrue(!medium.isLessThan(medium));
    assertTrue(medium.isLessThan(large));

    assertTrue(!large.isLessThan(small));
    assertTrue(!large.isLessThan(medium));
    assertTrue(!large.isLessThan(large));
  }

  @Test
  public void isLessThanOrEqualTo() {
    assertTrue(small.isLessThanOrEqualTo(small));
    assertTrue(small.isLessThanOrEqualTo(medium));
    assertTrue(small.isLessThanOrEqualTo(large));

    assertTrue(!medium.isLessThanOrEqualTo(small));
    assertTrue(medium.isLessThanOrEqualTo(medium));
    assertTrue(medium.isLessThanOrEqualTo(large));

    assertTrue(!large.isLessThanOrEqualTo(small));
    assertTrue(!large.isLessThanOrEqualTo(medium));
    assertTrue(large.isLessThanOrEqualTo(large));
  }

  @Test
  public void isGreaterThan() {
    assertTrue(!small.isGreaterThan(small));
    assertTrue(!small.isGreaterThan(medium));
    assertTrue(!small.isGreaterThan(large));

    assertTrue(medium.isGreaterThan(small));
    assertTrue(!medium.isGreaterThan(medium));
    assertTrue(!medium.isGreaterThan(large));

    assertTrue(large.isGreaterThan(small));
    assertTrue(large.isGreaterThan(medium));
    assertTrue(!large.isGreaterThan(large));
  }

  @Test
  public void isGreaterThanOrEqualTo() {
    assertTrue(small.isGreaterThanOrEqualTo(small));
    assertTrue(!small.isGreaterThanOrEqualTo(medium));
    assertTrue(!small.isGreaterThanOrEqualTo(large));

    assertTrue(medium.isGreaterThanOrEqualTo(small));
    assertTrue(medium.isGreaterThanOrEqualTo(medium));
    assertTrue(!medium.isGreaterThanOrEqualTo(large));

    assertTrue(large.isGreaterThanOrEqualTo(small));
    assertTrue(large.isGreaterThanOrEqualTo(medium));
    assertTrue(large.isGreaterThanOrEqualTo(large));
  }

  @Test
  public void adding() {
    Price delta = price(medium.asBigDecimal().subtract(small.asBigDecimal()));

    assertTrue(small.add(delta).almostEquals(medium, 1e-8));
    assertTrue(delta.add(small).almostEquals(medium, 1e-8));
  }

  @Test
  public void subtracting() {
    Price delta = price(medium.asBigDecimal().subtract(small.asBigDecimal()));

    assertTrue(medium.subtract(delta).almostEquals(small, 1e-8));

    assertIllegalArgumentException( () -> small.subtract(small));
    assertIllegalArgumentException( () -> small.subtract(medium));
    assertIllegalArgumentException( () -> delta.subtract(small));
  }

  @Test
  public void multiplying() {

    assertIllegalArgumentException( () -> medium.multiply(BigDecimal.ZERO));

    assertTrue(medium.multiply(BigDecimal.valueOf(0.5)).almostEquals(halfMedium,  1e-8));
    assertTrue(medium.multiply(BigDecimal.ONE         ).almostEquals(medium,      1e-8));
    assertTrue(medium.multiply(BigDecimal.valueOf(2)  ).almostEquals(twiceMedium, 1e-8));

    assertTrue(medium.multiply(onesBasedReturn(0.5)).almostEquals(halfMedium,  1e-8));
    assertTrue(medium.multiply(FLAT_RETURN         ).almostEquals(medium,      1e-8));
    assertTrue(medium.multiply(onesBasedReturn(2)  ).almostEquals(twiceMedium, 1e-8));

    assertAlmostEquals(medium.multiply(nonNegativeQuantity(0)),   ZERO_MONEY, 1e-8);
    assertAlmostEquals(medium.multiply(nonNegativeQuantity(0.5)), money(3.5), 1e-8);
    assertAlmostEquals(medium.multiply(nonNegativeQuantity(1)),   money(7),   1e-8);
    assertAlmostEquals(medium.multiply(nonNegativeQuantity(2)),   money(14),  1e-8);

    assertAlmostEquals(medium.multiply(signedQuantity(-2)),   signedMoney(-14),  1e-8);
    assertAlmostEquals(medium.multiply(signedQuantity(-1)),   signedMoney(-7),   1e-8);
    assertAlmostEquals(medium.multiply(signedQuantity(-0.5)), signedMoney(-3.5), 1e-8);
    assertAlmostEquals(medium.multiply(ZERO_SIGNED_QUANTITY), ZERO_SIGNED_MONEY, 1e-8);
    assertAlmostEquals(medium.multiply(signedQuantity(0.5)),  signedMoney(3.5),  1e-8);
    assertAlmostEquals(medium.multiply(signedQuantity(1)),    signedMoney(7),    1e-8);
    assertAlmostEquals(medium.multiply(signedQuantity(2)),    signedMoney(14),   1e-8);

    // Test multiplying by a plain old double
    assertAlmostEquals(medium.multiply(2.0), price(14), 1e-8);
  }

  @Test
  public void dividing() {
    assertTrue(medium.divide(BigDecimal.valueOf(0.5)).almostEquals(twiceMedium, 1e-8));
    assertTrue(medium.divide(BigDecimal.ONE         ).almostEquals(medium,      1e-8));
    assertTrue(medium.divide(BigDecimal.valueOf(2)  ).almostEquals(halfMedium,  1e-8));

    assertAlmostEquals(medium.divide(price(0.5)),  onesBasedReturn(14),  1e-8);
    assertAlmostEquals(medium.divide(price(1.0)),  onesBasedReturn(7),   1e-8);
    assertAlmostEquals(medium.divide(halfMedium),  onesBasedReturn(2),   1e-8);
    assertAlmostEquals(medium.divide(medium),      FLAT_RETURN,          1e-8);
    assertAlmostEquals(medium.divide(twiceMedium), onesBasedReturn(0.5), 1e-8);

    assertAlmostEquals(medium.divide(2.0), price(3.5), 1e-8);
  }

  @Test
  public void testAveragePrice() {
    assertAlmostEquals(price(20.0), Price.averagePrice(price(10.0), price(30.0)), 1e-8);
    assertAlmostEquals(price(31.0), Price.averagePrice(price(1.0), price(61.0)), 1e-8);
  }

  @Test
  public void testToString(){
    assertEquals(price(20.0).toString(2), "20.00");
  }

  @Test
  public void testMinAndMax_throwsOnEmpty() {
    assertIllegalArgumentException( () -> minPrice(Stream.empty()));
    assertIllegalArgumentException( () -> maxPrice(Stream.empty()));
  }

  @Test
  public void testMinAndMax() {
    Collections2.permutations(ImmutableList.of(price(1.1), price(2.2), price(3.3)))
        .forEach(pricesList -> {
          assertAlmostEquals(price(1.1), minPrice(pricesList.stream()), 1e-8);
          assertAlmostEquals(price(3.3), maxPrice(pricesList.stream()), 1e-8);
        });
  }

}
