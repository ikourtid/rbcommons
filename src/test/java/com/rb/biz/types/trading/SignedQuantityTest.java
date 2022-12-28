package com.rb.biz.types.trading;

import com.rb.nonbiz.functional.TriConsumer;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.function.BiConsumer;

import static com.rb.biz.types.trading.BuyQuantity.buyQuantity;
import static com.rb.biz.types.trading.NonNegativeQuantity.ZERO_NON_NEGATIVE_QUANTITY;
import static com.rb.biz.types.trading.NonNegativeQuantity.nonNegativeQuantity;
import static com.rb.biz.types.trading.PositiveQuantity.positiveQuantity;
import static com.rb.biz.types.trading.SellQuantity.sellQuantity;
import static com.rb.biz.types.trading.SignedQuantity.ZERO_SIGNED_QUANTITY;
import static com.rb.biz.types.trading.SignedQuantity.signedQuantity;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SignedQuantityTest {

  SignedQuantity small  = signedQuantity(6.99);
  SignedQuantity medium = signedQuantity(7.00);
  SignedQuantity large  = signedQuantity(7.01);

  @Test
  public void testEquality() {
    assertEquals(signedQuantity(6.99), signedQuantity(6.99));
  }

  @Test
  public void isLessThan() {
    assertTrue(!small.isLessThan(small));
    assertTrue( small.isLessThan(medium));
    assertTrue( small.isLessThan(large));

    assertTrue(!medium.isLessThan(small));
    assertTrue(!medium.isLessThan(medium));
    assertTrue( medium.isLessThan(large));

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
    assertTrue( medium.isLessThanOrEqualTo(medium));
    assertTrue( medium.isLessThanOrEqualTo(large));

    assertTrue(!large.isLessThanOrEqualTo(small));
    assertTrue(!large.isLessThanOrEqualTo(medium));
    assertTrue( large.isLessThanOrEqualTo(large));
  }

  @Test
  public void isGreaterThan() {
    assertTrue(!small.isGreaterThan(small));
    assertTrue(!small.isGreaterThan(medium));
    assertTrue(!small.isGreaterThan(large));

    assertTrue( medium.isGreaterThan(small));
    assertTrue(!medium.isGreaterThan(medium));
    assertTrue(!medium.isGreaterThan(large));

    assertTrue( large.isGreaterThan(small));
    assertTrue( large.isGreaterThan(medium));
    assertTrue(!large.isGreaterThan(large));
  }

  @Test
  public void isGreaterThanOrEqualTo() {
    assertTrue( small.isGreaterThanOrEqualTo(small));
    assertTrue(!small.isGreaterThanOrEqualTo(medium));
    assertTrue(!small.isGreaterThanOrEqualTo(large));

    assertTrue( medium.isGreaterThanOrEqualTo(small));
    assertTrue( medium.isGreaterThanOrEqualTo(medium));
    assertTrue(!medium.isGreaterThanOrEqualTo(large));

    assertTrue(large.isGreaterThanOrEqualTo(small));
    assertTrue(large.isGreaterThanOrEqualTo(medium));
    assertTrue(large.isGreaterThanOrEqualTo(large));
  }

  @Test
  public void testMinMax() {
    assertEquals(small, SignedQuantity.min(small,  small));
    assertEquals(small, SignedQuantity.min(small,  medium));
    assertEquals(small, SignedQuantity.min(medium, small));
    assertEquals(small, SignedQuantity.min(small,  large));
    assertEquals(small, SignedQuantity.min(large,  small));

    assertEquals(large, SignedQuantity.max(small,  large));
    assertEquals(large, SignedQuantity.max(large,  small));
    assertEquals(large, SignedQuantity.max(medium, large));
    assertEquals(large, SignedQuantity.max(large,  medium));
    assertEquals(large, SignedQuantity.max(small,  large));
    assertEquals(large, SignedQuantity.max(large,  large));
  }

  @Test
  public void testAbsAndNegate() {
    SignedQuantity largeNegative = signedQuantity(large.asBigDecimal().negate());

    assertEquals(largeNegative, large.negate());

    // negate should be its own inverse
    assertEquals(large, large.negate().negate());

    assertEquals(large, large.abs());
    assertEquals(large, largeNegative.abs());

    assertEquals(ZERO_SIGNED_QUANTITY, ZERO_SIGNED_QUANTITY.negate());
    assertEquals(ZERO_SIGNED_QUANTITY, ZERO_SIGNED_QUANTITY.abs());
  }

  @Test
  public void testMultiply() {
    assertEquals(signedQuantity(-44.4), signedQuantity(22.2).multiply(BigDecimal.valueOf(-2)));
    assertEquals(signedQuantity(-22.2), signedQuantity(22.2).multiply(BigDecimal.valueOf(-1)));
    assertEquals(signedQuantity(-11.1), signedQuantity(22.2).multiply(BigDecimal.valueOf(-0.5)));
    assertEquals(ZERO_SIGNED_QUANTITY,  signedQuantity(22.2).multiply(BigDecimal.ZERO));
    assertEquals(signedQuantity( 11.1), signedQuantity(22.2).multiply(BigDecimal.valueOf(0.5)));
    assertEquals(signedQuantity( 22.2), signedQuantity(22.2).multiply(BigDecimal.ONE));
    assertEquals(signedQuantity( 44.4), signedQuantity(22.2).multiply(BigDecimal.valueOf(2)));
  }

  @Test
  public void testAdditionSubtraction(){
    SignedQuantity signedQuantity11 = signedQuantity(11);
    SignedQuantity signedQuantity44 = signedQuantity(44);

    assertEquals(signedQuantity(55), signedQuantity11.add(signedQuantity44));
    assertEquals(signedQuantity(55), signedQuantity44.add(signedQuantity11));

    assertEquals(ZERO_SIGNED_QUANTITY, ZERO_SIGNED_QUANTITY.add(ZERO_SIGNED_QUANTITY));
    assertEquals(signedQuantity11, signedQuantity11.add(ZERO_SIGNED_QUANTITY));
    assertEquals(signedQuantity11, ZERO_SIGNED_QUANTITY.add(signedQuantity11));

    assertEquals(ZERO_SIGNED_QUANTITY, ZERO_SIGNED_QUANTITY.subtract(ZERO_SIGNED_QUANTITY));
    assertEquals(ZERO_SIGNED_QUANTITY, signedQuantity11.subtract(signedQuantity11));
    assertEquals(signedQuantity(-33),  signedQuantity11.subtract(signedQuantity44));
    assertEquals(signedQuantity( 33),  signedQuantity44.subtract(signedQuantity11));
  }

  @Test
  public void testBuySellQuantityConversions() {
    assertEquals(buyQuantity(123.4), signedQuantity(123.4).toBuyQuantity());
    assertIllegalArgumentException( () -> signedQuantity(-123.4).toBuyQuantity());

    // converting to a SellQuantity reverses the sign
    assertEquals(sellQuantity(123.4), signedQuantity(-123.4).toSellQuantity());
    // can't convert a positive SignedQuantity to a SellQuantity
    assertIllegalArgumentException( () -> signedQuantity(123.4).toSellQuantity());

    assertIllegalArgumentException( () -> ZERO_SIGNED_QUANTITY.toBuyQuantity());
    assertIllegalArgumentException( () -> ZERO_SIGNED_QUANTITY.toSellQuantity());
  }

  @Test
  public void isAlmostRound() {
    for (Double v : new double[]{
        -2.0000000001,
        -2,
        -1.9999999999,
        -1.0000000001,
        -1,
        -0.9999999999,
        -0.0000000001,
        0,
        0.0000000001,
        0.9999999999,
        1,
        1.0000000001,
        1.9999999999,
        2,
        2.0000000001}) {
      assertTrue(
          String.format("%.15f ( %s ) should count as round, but is not", v, signedQuantity(v).toString()),
          signedQuantity(v).isAlmostRound());
    }
    for (Double v : new double[]{
        -2.0001,
        -1.9999,
        -1.5,
        -1.0001,
        -0.9999,
        -0.5,
        -0.0001,
        0.0001,
        0.5,
        0.9999,
        1.0001,
        1.5,
        1.9999,
        2.0001}) {
      assertFalse(signedQuantity(v).isAlmostRound());
    }
  }

  @Test
  public void isRoundToScaleToNDecimals() {
    // check non-integers
    for (Double v : new double[]{
        -2.01,
        -1.99,
        -1.01,
        -0.99,
        -0.01,
        0.01,
        0.99,
        1.01,
        1.99,
        2.01}) {
      assertFalse(
          String.format("%f is not a power of 10", v),
          signedQuantity(v).isRoundToScale(-1));
      assertFalse(
          String.format("%f is not a round integer", v),
          signedQuantity(v).isRoundToScale(0));
      assertFalse(
          String.format("%f is not round to 1 decimal place", v),
          signedQuantity(v).isRoundToScale(1));
      assertTrue(
          String.format("%f is round to 2 decimal places", v),
          signedQuantity(v).isRoundToScale(2));
      assertTrue(
          String.format("%f is round to 3 decimal places", v),
          signedQuantity(v).isRoundToScale(3));
      assertTrue(
          String.format("%f is round to 8 decimal places", v),
          signedQuantity(v).isRoundToScale(8));
    }

    // check small integers
    for (Double v : new double[]{-2, -1, 0, 1, 2}) {
      if (v != 0) {
        assertFalse(
            String.format("%f is a not a multiple of 10", v),
            signedQuantity(v).isRoundToScale(-1));
      }
      assertTrue(
          String.format("%f is a round integer", v),
          signedQuantity(v).isRoundToScale(0));
      assertTrue(
          String.format("%f is round to 1 decimal place", v),
          signedQuantity(v).isRoundToScale(1));
      assertTrue(
          String.format("%f is round to 8 decimal places", v),
          signedQuantity(v).isRoundToScale(8));
    }

    // check multiples of 100
    for (Double v : new double[]{-200, -100, 0, 100, 200}) {
      if (v != 0) {
        assertFalse(
            String.format("%f is a not a multiple of 1_000", v),
            signedQuantity(v).isRoundToScale(-3));
      }
      assertTrue(
          String.format("%f is a multiple of 100", v),
          signedQuantity(v).isRoundToScale(-2));
      assertTrue(
          String.format("%f is a multiple of 10", v),
          signedQuantity(v).isRoundToScale(-1));
      assertTrue(
          String.format("%f is a round integer", v),
          signedQuantity(v).isRoundToScale(0));
      assertTrue(
          String.format("%f is round to 1 decimal place", v),
          signedQuantity(v).isRoundToScale(1));
      assertTrue(
          String.format("%f is round to 8 decimal places", v),
          signedQuantity(v).isRoundToScale(8));
    }
  }

  @Test
  public void testToString_roundQuantitiesShowNoNeedlessPrecision() {
    assertEquals("-2", signedQuantity(-2).toString());
    assertEquals("-1", signedQuantity(-1).toString());
    assertEquals("0", signedQuantity(0).toString());
    assertEquals("1", signedQuantity(1).toString());
    assertEquals("2", signedQuantity(2).toString());

    assertEquals("-2.01", signedQuantity(-2.01).toString());
    assertEquals("-1.99", signedQuantity(-1.99).toString());
    assertEquals("-1.01", signedQuantity(-1.01).toString());
    assertEquals("-0.99", signedQuantity(-0.99).toString());
    assertEquals("-0.01", signedQuantity(-0.01).toString());
    assertEquals( "0.01", signedQuantity( 0.01).toString());
    assertEquals( "0.99", signedQuantity( 0.99).toString());
    assertEquals( "1.01", signedQuantity( 1.01).toString());
    assertEquals( "1.99", signedQuantity( 1.99).toString());
    assertEquals( "2.01", signedQuantity( 2.01).toString());
  }

  @Test
  public void testRound() {
    BiConsumer<Integer, Double> asserter = (rounded, unrounded) ->
        assertAlmostEquals(signedQuantity(rounded), signedQuantity(unrounded).round(), DEFAULT_EPSILON_1e_8);
    asserter.accept(-1, -1.4);
    asserter.accept(-1, -1.0);
    asserter.accept(-1, -0.6);
    asserter.accept( 0, -0.4);
    asserter.accept( 0,  0.0);
    asserter.accept( 0,  0.4);
    asserter.accept( 1,  0.6);
    asserter.accept( 1,  1.0);
    asserter.accept( 1,  1.4);
  }

  @Test
  public void testRoundNDigits() {
    TriConsumer<Integer, Double, Double> asserter = (numDecimalsRounding, rounded, unrounded) ->
        assertAlmostEquals(signedQuantity(rounded), signedQuantity(unrounded).round(numDecimalsRounding), DEFAULT_EPSILON_1e_8);

    // round to integers
    asserter.accept(0, -2.0, -1.6);
    asserter.accept(0, -1.0, -1.4);
    asserter.accept(0, -1.0, -1.0);
    asserter.accept(0, -1.0, -0.6);
    asserter.accept(0,  0.0, -0.4);
    asserter.accept(0,  0.0,  0.0);
    asserter.accept(0,  0.0,  0.4);
    asserter.accept(0,  1.0,  0.6);
    asserter.accept(0,  1.0,  1.0);
    asserter.accept(0,  1.0,  1.4);
    asserter.accept(0,  2.0,  1.6);

    // round to 1 digit
    asserter.accept(1, -2.0, -2.01);
    asserter.accept(1, -2.0, -1.99);
    asserter.accept(1, -1.5, -1.456);
    asserter.accept(1, -1.0, -1.0);
    asserter.accept(1, -0.4, -0.432);
    asserter.accept(1,  0.0,  0.0);
    asserter.accept(1,  0.4,  0.432);
    asserter.accept(1,  1.0,  1.0);
    asserter.accept(1,  1.5,  1.456);
    asserter.accept(1,  2.0,  1.99);
    asserter.accept(1,  2.0,  2.01);

    // scale = -1 means round to 10's
    asserter.accept(-1, -100.0, -103.0);
    asserter.accept(-1,  -90.0,  -93.456);
    asserter.accept(-1,  -10.0,  -14.56);
    asserter.accept(-1,  -10.0,   -6.0);
    asserter.accept(-1,    0.0,   -1.0);
    asserter.accept(-1,    0.0,    0.0);
    asserter.accept(-1,    0.0,    1.0);
    asserter.accept(-1,   10.0,    6.0);
    asserter.accept(-1,   10.0,   14.56);
    asserter.accept(-1,   90.0,   93.456);
    asserter.accept(-1,  100.0,  103.0);
  }

  @Test
  public void testAbs() {
    assertEquals(signedQuantity(1.5),  signedQuantity(-1.5).abs());
    assertEquals(signedQuantity(1),    signedQuantity(-1).abs());
    assertEquals(signedQuantity(1e-6), signedQuantity(-1e-6).abs());
    assertEquals(ZERO_SIGNED_QUANTITY, ZERO_SIGNED_QUANTITY.abs());
    assertEquals(signedQuantity(1e-6), signedQuantity(1e-6).abs());
    assertEquals(signedQuantity(1),    signedQuantity(1).abs());
    assertEquals(signedQuantity(1.5),  signedQuantity(1.5).abs());
  }

  @Test
  public void testPositiveAndNonNegativeConversions() {
    assertEquals(ZERO_NON_NEGATIVE_QUANTITY, ZERO_SIGNED_QUANTITY.toNonNegativeQuantity());

    assertEquals(nonNegativeQuantity(123.4), signedQuantity(123.4).toNonNegativeQuantity());
    assertEquals(positiveQuantity(   123.4), signedQuantity(123.4).toPositiveQuantity());

    assertIllegalArgumentException( () -> ZERO_SIGNED_QUANTITY.toPositiveQuantity());
    assertIllegalArgumentException( () -> signedQuantity(-123.4).toNonNegativeQuantity());
    assertIllegalArgumentException( () -> signedQuantity(-123.4).toPositiveQuantity());
  }

  @Test
  public void testPercentString() {
    assertEquals("0 %",      ZERO_SIGNED_QUANTITY .toPercent(0));
    assertEquals("0.0 %",    ZERO_SIGNED_QUANTITY .toPercent(1));

    assertEquals("123 %",    signedQuantity(1.234).toPercent(0));
    assertEquals("123.4 %",  signedQuantity(1.234).toPercent(1));
    assertEquals("123.40 %", signedQuantity(1.234).toPercent(2));
  }

}
