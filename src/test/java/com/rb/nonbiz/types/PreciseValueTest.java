package com.rb.nonbiz.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rb.biz.types.Money;
import com.rb.biz.types.SignedMoney;
import com.rb.biz.types.trading.SignedQuantity;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.SignedMoney.ZERO_SIGNED_MONEY;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.trading.BuyQuantity.buyQuantity;
import static com.rb.biz.types.trading.NonNegativeQuantity.nonNegativeQuantity;
import static com.rb.biz.types.trading.PositiveQuantity.positiveQuantity;
import static com.rb.biz.types.trading.RoundingScale.INTEGER_ROUNDING_SCALE;
import static com.rb.biz.types.trading.RoundingScale.roundingScale;
import static com.rb.biz.types.trading.SellQuantity.sellQuantity;
import static com.rb.biz.types.trading.SignedQuantity.signedQuantity;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.PreciseValue.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PreciseValueTest {

  @Test
  public void testConversions() {
    SignedMoney money15digits               = signedMoney(1.234_567_890_123_456);
    SignedMoney negativeMoney15digits       = signedMoney(-1.234_567_890_123_456);
    SignedMoney moneyWouldRoundUp           = signedMoney( 11.987_654_321);
    SignedMoney negativeMoneyWouldRoundDown = signedMoney(-11.987_654_321);

    assertEquals(  1, money15digits.byteValue());
    assertEquals( -1, negativeMoney15digits.byteValue());
    assertEquals( 11, moneyWouldRoundUp.byteValue());              // doesn't round
    assertEquals(-11, negativeMoneyWouldRoundDown.byteValue());    // doesn't round
    //assertIllegalArgumentException( () -> signedMoney(257).byteValue());
    assertEquals( 127, signedMoney( 127).byteValue());
    assertEquals(-128, signedMoney( 128).byteValue());             // wraps
    assertEquals(-127, signedMoney( 129).byteValue());
    assertEquals(  -1, signedMoney(  -1).byteValue());
    assertEquals(-127, signedMoney(-127).byteValue());
    assertEquals(-128, signedMoney(-128).byteValue());
    assertEquals( 127, signedMoney(-129).byteValue());

    assertEquals(  1, money15digits.shortValue());
    assertEquals( -1, negativeMoney15digits.shortValue());
    assertEquals( 11, moneyWouldRoundUp.shortValue());             // doesn't round
    assertEquals(-11, negativeMoneyWouldRoundDown.shortValue());   // doesn't round
    assertEquals(Short.MAX_VALUE, signedMoney(Short.MAX_VALUE    ).shortValue());   // wraps
    assertEquals(Short.MIN_VALUE, signedMoney(Short.MAX_VALUE + 1).shortValue());

    assertEquals(  1, money15digits.intValue());
    assertEquals( -1, negativeMoney15digits.intValue());
    assertEquals( 11, moneyWouldRoundUp.intValue());               // doesn't round
    assertEquals(-11, negativeMoneyWouldRoundDown.intValue());     // doesn't round

    assertEquals( 1L, money15digits.longValue());
    assertEquals(-1L, negativeMoney15digits.longValue());
    assertEquals( 11L, moneyWouldRoundUp.longValue());             // doesn't round
    assertEquals(-11L, negativeMoneyWouldRoundDown.longValue());   // doesn't round

    assertEquals(  1.234_567, money15digits.floatValue(), 1e-6);
    assertEquals( -1.234_567, negativeMoney15digits.floatValue(), 1e-6);
    assertEquals( 11.987_654, moneyWouldRoundUp.floatValue(), 1e-6);
    assertEquals(-11.987_654, negativeMoneyWouldRoundDown.floatValue(), 1e-6);
  }

  @Test
  public void testSumToBigDecimal() {
    List<Money> empty = Collections.<Money>emptyList();
    assertEquals(BigDecimal.ZERO, sumToBigDecimal(empty));
    assertEquals(BigDecimal.ZERO, sumToBigDecimal(empty.iterator()));

    List<Money> ten = singletonList(money(10));
    assertEquals(0, BigDecimal.valueOf(10).compareTo(sumToBigDecimal(ten)));
    assertEquals(0, BigDecimal.valueOf(10).compareTo(sumToBigDecimal(ten.iterator())));

    List<Money> fifty = ImmutableList.of(money(20), money(30));
    assertEquals(0, BigDecimal.valueOf(50).compareTo(sumToBigDecimal(fifty)));
    assertEquals(0, BigDecimal.valueOf(50).compareTo(sumToBigDecimal(fifty.iterator())));
  }

  @Test
  public void almostEqualsWorksForDifferentPreciseValueSubclasses() {
    double tiny = 1e-9;
    List<? extends SignedQuantity> quantities = ImmutableList.of(
        buyQuantity(10),
        sellQuantity(10 + 1 * tiny),
        signedQuantity(10 + 2 * tiny),
        nonNegativeQuantity(10 + 3 * tiny),
        positiveQuantity(10 + 4 * tiny));
    for (int i = 0; i < quantities.size(); i++) {
      SignedQuantity objI = quantities.get(i);
      for (int j = 0; j < quantities.size(); j++) {
        SignedQuantity objJ = quantities.get(j);
        assertTrue(objI.almostEquals(objJ, DEFAULT_EPSILON_1e_8));
        assertThat(
            objI,
            preciseValueMatcher(objJ, DEFAULT_EPSILON_1e_8));

      }
      // comparing to null causes an exception
      assertIllegalArgumentException( () -> objI.almostEquals(null, DEFAULT_EPSILON_1e_8));
    }
  }

  @Test
  public void testSignsAreOpposite() {
    // Using Money instead of PreciseValue, which cannot be instantiated
    assertTrue(signsAreOpposite(signedMoney(-10), signedMoney(20)));
    assertFalse(signsAreOpposite(signedMoney(-10), ZERO_SIGNED_MONEY));
    assertFalse(signsAreOpposite(signedMoney(-10), signedMoney(-20)));

    assertFalse(signsAreOpposite(ZERO_SIGNED_MONEY, signedMoney(20)));
    assertFalse(signsAreOpposite(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY));
    assertFalse(signsAreOpposite(ZERO_SIGNED_MONEY, signedMoney(-20)));

    assertFalse(signsAreOpposite(signedMoney(10), signedMoney(20)));
    assertFalse(signsAreOpposite(signedMoney(10), ZERO_SIGNED_MONEY));
    assertTrue(signsAreOpposite(signedMoney(10), signedMoney(-20)));
  }

  @Test
  public void testSignsAreSame() {
    // Using Money instead of PreciseValue, which cannot be instantiated
    assertFalse(signsAreSame(signedMoney(-10), signedMoney(20)));
    assertFalse(signsAreSame(signedMoney(-10), ZERO_SIGNED_MONEY));
    assertTrue(signsAreSame(signedMoney(-10), signedMoney(-20)));

    assertFalse(signsAreSame(ZERO_SIGNED_MONEY, signedMoney(20)));
    assertTrue(signsAreSame(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY));
    assertFalse(signsAreSame(ZERO_SIGNED_MONEY, signedMoney(-20)));

    assertTrue(signsAreSame(signedMoney(10), signedMoney(20)));
    assertFalse(signsAreSame(signedMoney(10), ZERO_SIGNED_MONEY));
    assertFalse(signsAreSame(signedMoney(10), signedMoney(-20)));
  }

  @Test
  public void testAsBigDecimalList() {
    assertEquals(
        emptyList(),
        asBigDecimalList(Collections.<SignedMoney> emptyList()));

    assertEquals(
        ImmutableList.of(BigDecimal.valueOf(1.0)),
        asBigDecimalList(ImmutableList.of(signedMoney(1))));

    assertEquals(
        ImmutableList.of(BigDecimal.valueOf(-1.234), BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.valueOf(10.0)),
        asBigDecimalList(ImmutableList.of(
            signedMoney(-1.234), ZERO_SIGNED_MONEY, signedMoney(1), signedMoney(10))));
  }

  @Test
  public void testSnapToRound() {
    for (int intValue : ImmutableSet.of(-10, -1, 0, 1, 10)) {
      for (double diff : rbSetOf(-1e-15, 0.0, 1e-15)) {
        assertTrue(signedMoney(intValue).almostEquals(
            signedMoney(snapToRound(BigDecimal.valueOf(intValue + diff))),
            epsilon(1e-14)));
        assertTrue(signedMoney(intValue).almostEquals(
            signedMoney(snapToRound(signedMoney(BigDecimal.valueOf(intValue + diff)))),
            epsilon(1e-14)));
      }
      for (double diff : rbSetOf(-0.5, -0.4, -1e-8, 0.4, 0.5)) {
        assertFalse(signedMoney(intValue).almostEquals(
            signedMoney(snapToRound(BigDecimal.valueOf(intValue + diff))),
            epsilon(1e-14)));
        assertFalse(signedMoney(intValue).almostEquals(
            signedMoney(snapToRound(signedMoney(BigDecimal.valueOf(intValue + diff)))),
            epsilon(1e-14)));
      }
    }
  }

  @Test
  public void testSnapToZero() {
    assertNotEquals(ZERO_SIGNED_MONEY, signedMoney(snapToZero(BigDecimal.valueOf(-1e-11))));
    assertEquals(ZERO_SIGNED_MONEY, signedMoney(snapToZero(BigDecimal.valueOf(-1e-13))));
    assertEquals(ZERO_SIGNED_MONEY, signedMoney(snapToZero(BigDecimal.valueOf(-0))));
    assertEquals(ZERO_SIGNED_MONEY, signedMoney(snapToZero(BigDecimal.valueOf(1e-13))));
    assertEquals(signedMoney(200 - 1e-11), signedMoney(snapToZero(BigDecimal.valueOf(200 - 1e-11))));
    assertEquals(signedMoney(200 - 1e-13), signedMoney(snapToZero(BigDecimal.valueOf(200 - 1e-13))));
    assertEquals(signedMoney(200 + 1e-13), signedMoney(snapToZero(BigDecimal.valueOf(200 + 1e-13))));
    assertEquals(signedMoney(200 + 1e-11), signedMoney(snapToZero(BigDecimal.valueOf(200 + 1e-11))));

    assertNotEquals(ZERO_SIGNED_MONEY, signedMoney(snapToZero(signedMoney(BigDecimal.valueOf(-1e-11)))));
    assertEquals(ZERO_SIGNED_MONEY, signedMoney(snapToZero(signedMoney(BigDecimal.valueOf(-1e-13)))));
    assertEquals(ZERO_SIGNED_MONEY, signedMoney(snapToZero(signedMoney(BigDecimal.valueOf(-0)))));
    assertEquals(ZERO_SIGNED_MONEY, signedMoney(snapToZero(signedMoney(BigDecimal.valueOf(1e-13)))));
    assertEquals(signedMoney(200 - 1e-11), signedMoney(snapToZero(signedMoney(BigDecimal.valueOf(200 - 1e-11)))));
    assertEquals(signedMoney(200 - 1e-13), signedMoney(snapToZero(signedMoney(BigDecimal.valueOf(200 - 1e-13)))));
    assertEquals(signedMoney(200 + 1e-13), signedMoney(snapToZero(signedMoney(BigDecimal.valueOf(200 + 1e-13)))));
    assertEquals(signedMoney(200 + 1e-11), signedMoney(snapToZero(signedMoney(BigDecimal.valueOf(200 + 1e-11)))));
  }


  @Test
  public void testPreciseValuesAlmostEqual() {
    assertTrue(preciseValuesAlmostEqual(signedMoney(10.0), signedMoney(10.0), DEFAULT_EPSILON_1e_8));

    assertTrue(preciseValuesAlmostEqual(signedMoney(10.0), signedMoney(10.0 + 1e-9), DEFAULT_EPSILON_1e_8));
    assertTrue(preciseValuesAlmostEqual(signedMoney(10.0), signedMoney(10.0 - 1e-9), DEFAULT_EPSILON_1e_8));
    assertTrue(preciseValuesAlmostEqual(signedMoney(10.0 + 1e-9), signedMoney(10.0), DEFAULT_EPSILON_1e_8));
    assertTrue(preciseValuesAlmostEqual(signedMoney(10.0 - 1e-9), signedMoney(10.0), DEFAULT_EPSILON_1e_8));

    assertFalse(preciseValuesAlmostEqual(signedMoney(10.0), signedMoney(10.0 + 1e-8), epsilon(1e-9)));
    assertFalse(preciseValuesAlmostEqual(signedMoney(10.0), signedMoney(10.0 - 1e-8), epsilon(1e-9)));
    assertFalse(preciseValuesAlmostEqual(signedMoney(10.0 + 1e-8), signedMoney(10.0), epsilon(1e-9)));
    assertFalse(preciseValuesAlmostEqual(signedMoney(10.0 - 1e-8), signedMoney(10.0), epsilon(1e-9)));
  }

  @Test
  public void testBigDecimalsAlmostEqual() {
    assertTrue(bigDecimalsAlmostEqual(BigDecimal.TEN, BigDecimal.TEN, DEFAULT_EPSILON_1e_8));
    assertTrue(bigDecimalsAlmostEqual(signedMoney(10.0).asBigDecimal(), signedMoney(10.0).asBigDecimal(), DEFAULT_EPSILON_1e_8));

    assertTrue(bigDecimalsAlmostEqual(signedMoney(10.0).asBigDecimal(), signedMoney(10.0 + 1e-9).asBigDecimal(), DEFAULT_EPSILON_1e_8));
    assertTrue(bigDecimalsAlmostEqual(signedMoney(10.0).asBigDecimal(), signedMoney(10.0 - 1e-9).asBigDecimal(), DEFAULT_EPSILON_1e_8));
    assertTrue(bigDecimalsAlmostEqual(signedMoney(10.0 + 1e-9).asBigDecimal(), signedMoney(10.0).asBigDecimal(), DEFAULT_EPSILON_1e_8));
    assertTrue(bigDecimalsAlmostEqual(signedMoney(10.0 - 1e-9).asBigDecimal(), signedMoney(10.0).asBigDecimal(), DEFAULT_EPSILON_1e_8));

    assertFalse(bigDecimalsAlmostEqual(signedMoney(10.0).asBigDecimal(), signedMoney(10.0 + 1e-8).asBigDecimal(), epsilon(1e-9)));
    assertFalse(bigDecimalsAlmostEqual(signedMoney(10.0).asBigDecimal(), signedMoney(10.0 - 1e-8).asBigDecimal(), epsilon(1e-9)));
    assertFalse(bigDecimalsAlmostEqual(signedMoney(10.0 + 1e-8).asBigDecimal(), signedMoney(10.0).asBigDecimal(), epsilon(1e-9)));
    assertFalse(bigDecimalsAlmostEqual(signedMoney(10.0 - 1e-8).asBigDecimal(), signedMoney(10.0).asBigDecimal(), epsilon(1e-9)));

    assertIllegalArgumentException( () -> bigDecimalsAlmostEqual(null, BigDecimal.TEN, DEFAULT_EPSILON_1e_8));
    assertIllegalArgumentException( () -> bigDecimalsAlmostEqual(BigDecimal.TEN, null, DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testIsAlmostZero() {
    assertFalse(signedMoney(-1e-7).isAlmostZero(DEFAULT_EPSILON_1e_8));
    assertTrue(signedMoney(-1e-9).isAlmostZero(DEFAULT_EPSILON_1e_8));
    assertTrue(ZERO_SIGNED_MONEY.isAlmostZero(DEFAULT_EPSILON_1e_8));
    assertTrue(signedMoney(1e-9).isAlmostZero(DEFAULT_EPSILON_1e_8));
    assertFalse(signedMoney(1e-7).isAlmostZero(DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testIsAlmostZeroButNotZero() {
    assertFalse(signedMoney(-1e-7).isAlmostZeroButNotZero(DEFAULT_EPSILON_1e_8));
    assertTrue(signedMoney( -1e-9).isAlmostZeroButNotZero(DEFAULT_EPSILON_1e_8));
    assertFalse(ZERO_SIGNED_MONEY .isAlmostZeroButNotZero(DEFAULT_EPSILON_1e_8));
    assertTrue(signedMoney(  1e-9).isAlmostZeroButNotZero(DEFAULT_EPSILON_1e_8));
    assertFalse(signedMoney( 1e-7).isAlmostZeroButNotZero(DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testIsLessThan() {
    assertTrue(signedMoney(1.0).isLessThan(signedMoney(2.0)));
    assertFalse(signedMoney(2.0).isLessThan(signedMoney(1.0)));
  }

  @Test
  public void testIsLessThanOrEqualTo() {
    assertTrue(signedMoney(1.0).isLessThanOrEqualTo(signedMoney(1.0)));
    assertTrue(signedMoney(1.0).isLessThanOrEqualTo(signedMoney(2.0)));
    assertFalse(signedMoney(2.0).isLessThanOrEqualTo(signedMoney(1.0)));
  }

  @Test
  public void testIsLessThanOrAlmostEqualTo() {
    assertFalse(signedMoney(1.0).isLessThanOrAlmostEqualTo(signedMoney(1.0 - 1e-7), DEFAULT_EPSILON_1e_8));
    assertTrue( signedMoney(1.0).isLessThanOrAlmostEqualTo(signedMoney(1.0 - 1e-9), DEFAULT_EPSILON_1e_8));
    assertTrue( signedMoney(1.0).isLessThanOrAlmostEqualTo(signedMoney(1.0       ), DEFAULT_EPSILON_1e_8));
    assertTrue( signedMoney(1.0).isLessThanOrAlmostEqualTo(signedMoney(1.0 + 1e-9), DEFAULT_EPSILON_1e_8));
    assertTrue( signedMoney(1.0).isLessThanOrAlmostEqualTo(signedMoney(2.0       ), DEFAULT_EPSILON_1e_8));

    assertFalse(signedMoney(2.0).isLessThanOrAlmostEqualTo(signedMoney(1.0       ), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testIsGreaterThan() {
    assertFalse(signedMoney(1.0).isGreaterThan(signedMoney(2.0)));
    assertTrue(signedMoney(2.0).isGreaterThan(signedMoney(1.0)));
  }

  @Test
  public void testIsGreaterThanOrEqualTo() {
    assertTrue(signedMoney(1.0).isGreaterThanOrEqualTo(signedMoney(1.0)));
    assertFalse(signedMoney(1.0).isGreaterThanOrEqualTo(signedMoney(2.0)));
    assertTrue(signedMoney(2.0).isGreaterThanOrEqualTo(signedMoney(1.0)));
  }

  @Test
  public void testIsGreaterThanOrAlmostEqualTo() {
    assertTrue( signedMoney(1.0).isGreaterThanOrAlmostEqualTo(signedMoney(1.0 - 1e-7), DEFAULT_EPSILON_1e_8));
    assertTrue( signedMoney(1.0).isGreaterThanOrAlmostEqualTo(signedMoney(1.0 - 1e-9), DEFAULT_EPSILON_1e_8));
    assertTrue( signedMoney(1.0).isGreaterThanOrAlmostEqualTo(signedMoney(1.0       ), DEFAULT_EPSILON_1e_8));
    assertTrue( signedMoney(1.0).isGreaterThanOrAlmostEqualTo(signedMoney(1.0 + 1e-9), DEFAULT_EPSILON_1e_8));
    assertFalse(signedMoney(1.0).isGreaterThanOrAlmostEqualTo(signedMoney(1.0 + 1e-7), DEFAULT_EPSILON_1e_8));

    assertTrue( signedMoney(2.0).isGreaterThanOrAlmostEqualTo(signedMoney(1.0       ), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testIsZero() {
    assertFalse(signedMoney(-1.0).isZero());
    assertFalse(signedMoney(-1e-12).isZero());
    assertTrue(signedMoney(0.0).isZero());
    assertTrue(ZERO_SIGNED_MONEY.isZero());
    assertFalse(signedMoney(1e-12).isZero());
    assertFalse(signedMoney(1.0).isZero());
  }

  @Test
  public void isNegative() {
    assertTrue(signedMoney(-0.01).isNegative());
    assertTrue(signedMoney(-1e-12).isNegative());
    assertFalse(signedMoney(0).isNegative());
    assertFalse(signedMoney( 1e-12).isNegative());
    assertFalse(ZERO_SIGNED_MONEY.isNegative());
    assertFalse(signedMoney(0.01).isNegative());
  }

  @Test
  public void isNegativeOrZero() {
    assertTrue(signedMoney(-0.01).isNegativeOrZero());
    assertTrue(signedMoney(-1e-12).isNegativeOrZero());
    assertTrue(signedMoney(0).isNegativeOrZero());
    assertTrue(ZERO_SIGNED_MONEY.isNegativeOrZero());
    assertFalse(signedMoney(1e-12).isNegativeOrZero());
    assertFalse(signedMoney(0.01).isNegativeOrZero());
  }

  @Test
  public void isPositive() {
    assertFalse(signedMoney(-0.01).isPositive());
    assertFalse(signedMoney(-1e-12).isPositive());
    assertFalse(signedMoney(0).isPositive());
    assertFalse(ZERO_SIGNED_MONEY.isPositive());
    assertTrue(signedMoney(1e-12).isPositive());
    assertTrue(signedMoney(0.01).isPositive());
  }

  @Test
  public void isPositiveOrZero() {
    assertFalse(signedMoney(-0.01).isPositiveOrZero());
    assertFalse(signedMoney(-1e-12).isPositiveOrZero());
    assertTrue(signedMoney(0).isPositiveOrZero());
    assertTrue(ZERO_SIGNED_MONEY.isPositiveOrZero());
    assertTrue(signedMoney(1e-12).isPositiveOrZero());
    assertTrue(signedMoney(0.01).isPositiveOrZero());
  }

  @Test
  public void testIsRoundToScale() {
    assertFalse(signedMoney(-1.23).isRoundToScale(0));
    assertFalse(signedMoney(-1.23).isRoundToScale(1));
    assertTrue( signedMoney(-1.23).isRoundToScale(2));
    assertTrue( signedMoney(-1.23).isRoundToScale(3));

    assertFalse(signedMoney(1.23).isRoundToScale(0));
    assertFalse(signedMoney(1.23).isRoundToScale(1));
    assertTrue( signedMoney(1.23).isRoundToScale(2));
    assertTrue( signedMoney(1.23).isRoundToScale(3));
  }

  @Test
  public void testIsRoundToScale_roundingScaleArgument() {
    assertFalse(signedMoney(-1.23).isRoundToScale(INTEGER_ROUNDING_SCALE));
    assertFalse(signedMoney(-1.23).isRoundToScale(roundingScale(1)));
    assertTrue( signedMoney(-1.23).isRoundToScale(roundingScale(2)));
    assertTrue( signedMoney(-1.23).isRoundToScale(roundingScale(3)));

    assertFalse(signedMoney(1.23).isRoundToScale(INTEGER_ROUNDING_SCALE));
    assertFalse(signedMoney(1.23).isRoundToScale(roundingScale(1)));
    assertTrue( signedMoney(1.23).isRoundToScale(roundingScale(2)));
    assertTrue( signedMoney(1.23).isRoundToScale(roundingScale(3)));
  }

  @Test
  public void testExactlyRound() {
    assertTrue(ZERO_SIGNED_MONEY.isExactlyRound());
    assertTrue(signedMoney(1).isExactlyRound());
    assertTrue(signedMoney(1.0).isExactlyRound());
    assertTrue(signedMoney(-1).isExactlyRound());
    assertTrue(signedMoney(-1.0).isExactlyRound());
    assertTrue(signedMoney(new BigDecimal("0.000000000000000")).isExactlyRound());
    assertTrue(signedMoney(new BigDecimal("-0.000000000000000")).isExactlyRound());
    assertTrue(signedMoney(new BigDecimal("1.000000000000000")).isExactlyRound());
    assertTrue(signedMoney(new BigDecimal("-1.000000000000000")).isExactlyRound());

    assertFalse(signedMoney(1e-9).isExactlyRound());
    assertFalse(signedMoney(1e-14).isExactlyRound());
    assertFalse(signedMoney(new BigDecimal("0.0000000000000001")).isExactlyRound());
    assertFalse(signedMoney(-1e-9).isExactlyRound());
    assertFalse(signedMoney(-1e-14).isExactlyRound());
    assertFalse(signedMoney(new BigDecimal("-0.0000000000000001")).isExactlyRound());

    assertFalse(signedMoney(1 + 1e-9).isExactlyRound());
    assertFalse(signedMoney(1 + 1e-14).isExactlyRound());
    assertFalse(signedMoney(new BigDecimal("1.0000000000000001")).isExactlyRound());
    assertFalse(signedMoney(1 - 1e-9).isExactlyRound());
    assertFalse(signedMoney(1 - 1e-14).isExactlyRound());
    assertFalse(signedMoney(new BigDecimal("0.9999999999999999")).isExactlyRound());


    assertFalse(signedMoney(-1 + 1e-9).isExactlyRound());
    assertFalse(signedMoney(-1 + 1e-14).isExactlyRound());
    assertFalse(signedMoney(new BigDecimal("-0.9999999999999999")).isExactlyRound());
    assertFalse(signedMoney(-1 - 1e-9).isExactlyRound());
    assertFalse(signedMoney(-1 - 1e-14).isExactlyRound());
    assertFalse(signedMoney(new BigDecimal("-1.0000000000000001")).isExactlyRound());
  }

  @Test
  public void testMin() {
    assertAlmostEquals(signedMoney(10.0), PreciseValue.min(signedMoney(10.0), signedMoney(20.0)), DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(signedMoney(10.0), PreciseValue.min(signedMoney(20.0), signedMoney(10.0)), DEFAULT_EPSILON_1e_8);
  }

  @Test
  public void testMax() {
    assertAlmostEquals(signedMoney(20.0), PreciseValue.max(signedMoney(10.0), signedMoney(20.0)), DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(signedMoney(20.0), PreciseValue.max(signedMoney(20.0), signedMoney(10.0)), DEFAULT_EPSILON_1e_8);
  }

  @Test
  public void testIsSafelyLessThan() {
    Epsilon e = DEFAULT_EPSILON_1e_8;
    assertTrue( ZERO_MONEY        .isSafelyLessThan(money(10), e));
    assertTrue( money(0.1)        .isSafelyLessThan(money(10), e));
    assertTrue( money(9.9)        .isSafelyLessThan(money(10), e));
    assertTrue( money(10.0)       .isSafelyLessThan(money(10), e));
    assertTrue( money(10.0 + 1e-9).isSafelyLessThan(money(10), e));
    assertFalse(money(10.0 + 1e-7).isSafelyLessThan(money(10), e));
    assertFalse(money(11)         .isSafelyLessThan(money(10), e));
    assertFalse(money(999)        .isSafelyLessThan(money(10), e));
  }

  @Test
  public void testIsSafelyGreaterThan() {
    Epsilon e = DEFAULT_EPSILON_1e_8;
    assertFalse(ZERO_MONEY        .isSafelyGreaterThan(money(10), e));
    assertFalse(money(0.1)        .isSafelyGreaterThan(money(10), e));
    assertFalse(money(9.9)        .isSafelyGreaterThan(money(10), e));
    assertFalse(money(10.0 - 1e-7).isSafelyGreaterThan(money(10), e));
    assertTrue( money(10.0 - 1e-9).isSafelyGreaterThan(money(10), e));
    assertTrue( money(10.0)       .isSafelyGreaterThan(money(10), e));
    assertTrue( money(10.0 + 1e-9).isSafelyGreaterThan(money(10), e));
    assertTrue( money(10.0 + 1e-7).isSafelyGreaterThan(money(10), e));
    assertTrue( money(11)         .isSafelyGreaterThan(money(10), e));
    assertTrue( money(999)        .isSafelyGreaterThan(money(10), e));
  }

  @Test
  public void testFormatWithCommas() {
    assertEquals("1,235",       formatWithCommas(0).format(1_234.567_89));
    assertEquals("1,234.6",     formatWithCommas(1).format(1_234.567_89));
    assertEquals("1,234.57",    formatWithCommas(2).format(1_234.567_89));
    // could include up to 9 digits, but only need 5 without trailing zeros
    assertEquals("1,234.56789", formatWithCommas(9).format(1_234.567_89));

    assertEquals("1,235",       formatWithCommas(0, 0).format(1_234.567_89));
    assertEquals("1,234.6",     formatWithCommas(0, 1).format(1_234.567_89));
    assertEquals("1,234.57",    formatWithCommas(0, 2).format(1_234.567_89));
    assertEquals("1,234.56789", formatWithCommas(0, 9).format(1_234.567_89));
  }

  @Test
  public void testFormatWithoutCommas() {
    assertEquals("1235",       formatWithoutCommas(0).format(1_234.567_89));
    assertEquals("1234.6",     formatWithoutCommas(1).format(1_234.567_89));
    assertEquals("1234.57",    formatWithoutCommas(2).format(1_234.567_89));
    // could include up to 9 digits, but only need 5 without trailing zeros
    assertEquals("1234.56789", formatWithoutCommas(9).format(1_234.567_89));

    // the 2-argument version
    assertEquals("1235",       formatWithoutCommas(0, 0).format(1_234.567_89));
    assertEquals("1234.6",     formatWithoutCommas(0, 1).format(1_234.567_89));
    assertEquals("1234.57",    formatWithoutCommas(0, 2).format(1_234.567_89));
    assertEquals("1234.56789", formatWithoutCommas(0, 9).format(1_234.567_89));
  }

  @Test
  public void testToStringMinMaxDigits() {
    Money money = money(1_234.567_89);
    assertEquals("1235",       money.toString(0, 0));
    assertEquals("1234.6",     money.toString(0, 1));
    assertEquals("1234.57",    money.toString(0, 2));
    assertEquals("1234.57",    money.toString(2, 2));
    assertEquals("1234.5679",  money.toString(2, 4));
    assertEquals("1234.56789", money.toString(2, 5));
    assertEquals("1234.56789", money.toString(2, 10));
  }

}
