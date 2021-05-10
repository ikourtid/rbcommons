package com.rb.biz.types;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.Price.price;
import static com.rb.biz.types.SignedMoney.ZERO_SIGNED_MONEY;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.SignedMoney.sumSignedMoney;
import static com.rb.biz.types.trading.SignedQuantity.ZERO_SIGNED_QUANTITY;
import static com.rb.biz.types.trading.SignedQuantity.signedQuantity;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertThrowsAnyException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInPct;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;

public class SignedMoneyTest {

  @Test
  public void testAdd() {
    assertEquals(signedMoney(-123.45).add(ZERO_SIGNED_MONEY), signedMoney(-123.45));
    assertEquals(ZERO_SIGNED_MONEY   .add(ZERO_SIGNED_MONEY), ZERO_SIGNED_MONEY);
    assertEquals(signedMoney( 123.45).add(ZERO_SIGNED_MONEY), signedMoney( 123.45));

    assertEquals(ZERO_SIGNED_MONEY.add(signedMoney(-123.45)), signedMoney(-123.45));
    assertEquals(ZERO_SIGNED_MONEY.add(signedMoney( 123.45)), signedMoney( 123.45));

    assertEquals(signedMoney(-123.45).add(signedMoney( 123.45)), ZERO_SIGNED_MONEY);
    assertEquals(signedMoney( 123.45).add(signedMoney(-123.45)), ZERO_SIGNED_MONEY);

    assertEquals(signedMoney(-123.45).add(signedMoney(-123.45)), signedMoney(-246.90));
    assertEquals(signedMoney( 123.45).add(signedMoney( 123.45)), signedMoney( 246.90));
  }

  @Test
  public void testSubtract() {
    assertEquals(signedMoney(-123.45).subtract(ZERO_SIGNED_MONEY), signedMoney(-123.45));
    assertEquals(ZERO_SIGNED_MONEY   .subtract(ZERO_SIGNED_MONEY), ZERO_SIGNED_MONEY);
    assertEquals(signedMoney( 123.45).subtract(ZERO_SIGNED_MONEY), signedMoney( 123.45));

    assertEquals(ZERO_SIGNED_MONEY.subtract(signedMoney(-123.45)), signedMoney( 123.45));
    assertEquals(ZERO_SIGNED_MONEY.subtract(signedMoney( 123.45)), signedMoney(-123.45));

    assertEquals(signedMoney(-123.45).subtract(signedMoney( 123.45)), signedMoney(-246.90));
    assertEquals(signedMoney( 123.45).subtract(signedMoney(-123.45)), signedMoney( 246.90));

    assertEquals(signedMoney(-123.45).subtract(signedMoney(-123.45)), ZERO_SIGNED_MONEY);
    assertEquals(signedMoney( 123.45).subtract(signedMoney( 123.45)), ZERO_SIGNED_MONEY);
  }

  @Test
  public void testSubtractMoney() {
    assertEquals(signedMoney(-123.45).subtract(ZERO_MONEY), signedMoney(-123.45));
    assertEquals(ZERO_SIGNED_MONEY   .subtract(ZERO_MONEY), ZERO_SIGNED_MONEY);
    assertEquals(signedMoney( 123.45).subtract(ZERO_MONEY), signedMoney( 123.45));

    assertEquals(signedMoney(-123.45).subtract(money(123.45)), signedMoney(-246.90));
    assertEquals(ZERO_SIGNED_MONEY   .subtract(money(123.45)), signedMoney(-123.45));
    assertEquals(signedMoney( 123.45).subtract(money(123.45)), ZERO_SIGNED_MONEY);
  }

  @Test
  public void testToMoney() {
    assertIllegalArgumentException( () -> signedMoney(-123.45).toMoney());
    assertIllegalArgumentException( () -> signedMoney(  -0.01).toMoney());

    assertEquals(ZERO_MONEY,      ZERO_SIGNED_MONEY.toMoney());
    assertEquals(money(0.01),   signedMoney(  0.01).toMoney());
    assertEquals(money(123.45), signedMoney(123.45).toMoney());

    // check BigDecimal constructor
    assertIllegalArgumentException( () -> signedMoney(BigDecimal.valueOf(-123.45)).toMoney());
    assertEquals(money(123.45), signedMoney(BigDecimal.valueOf(123.45)).toMoney());
  }

  @Test
  public void testToMoneyWithFloorOfZero() {
    assertEquals(ZERO_MONEY, signedMoney(-123.45).toMoneyWithFloorOfZero());
    assertEquals(ZERO_MONEY, signedMoney(  -0.01).toMoneyWithFloorOfZero());

    assertEquals(ZERO_MONEY,      ZERO_SIGNED_MONEY.toMoneyWithFloorOfZero());
    assertEquals(money(0.01),   signedMoney(  0.01).toMoneyWithFloorOfZero());
    assertEquals(money(123.45), signedMoney(123.45).toMoneyWithFloorOfZero());
  }

  @Test
  public void sum() {
    assertEquals(
        ZERO_SIGNED_MONEY,
        sumSignedMoney(emptyList()));
    assertEquals(
        ZERO_SIGNED_MONEY,
        sumSignedMoney(emptySet()));
    assertEquals(
        ZERO_SIGNED_MONEY,
        sumSignedMoney(ImmutableList.of(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY)));
    assertEquals(
        signedMoney(-1),
        sumSignedMoney(ImmutableList.of(
            signedMoney(2),
            signedMoney(3),
            signedMoney(-6))));
  }

  @Test
  public void testSumSignedMoneyIteratorAndStream() {
    // empty iterator
    assertEquals(ZERO_SIGNED_MONEY, sumSignedMoney(Collections.<SignedMoney>emptyList().iterator()));
    // empty stream
    assertEquals(ZERO_SIGNED_MONEY, sumSignedMoney(Stream.empty()));

    List<SignedMoney> signedMoneyList = ImmutableList.of(signedMoney(-1), ZERO_SIGNED_MONEY, signedMoney(2), signedMoney(4));
    doubleExplained(5.0, -1.0 + 0.0 + 2.0 + 4.0);

    Collections2.permutations(signedMoneyList).forEach(permutation -> {
      assertEquals(signedMoney(5), sumSignedMoney(permutation.iterator()));
      assertEquals(signedMoney(5), sumSignedMoney(permutation.stream()));
    });
  }

  @Test
  public void testSumSignedMoneyVarArgs() {
    doubleExplained(5.0, -1.0 + 0.0 + 2.0 + 4.0);
    assertEquals(signedMoney(5), sumSignedMoney(signedMoney(-1), ZERO_SIGNED_MONEY, signedMoney(2), signedMoney(4)));
  }

  @Test
  public void testMultiplyBigDecimal() {
    assertEquals(signedMoney(-1.234), signedMoney(-1.234).multiply(BigDecimal.ONE));
    assertEquals(signedMoney(-1.0),   signedMoney(-1.0).multiply(  BigDecimal.ONE));
    assertEquals(ZERO_SIGNED_MONEY,   ZERO_SIGNED_MONEY.multiply(  BigDecimal.ONE));
    assertEquals(signedMoney( 1.0),   signedMoney( 1.0).multiply(  BigDecimal.ONE));
    assertEquals(signedMoney( 1.234), signedMoney( 1.234).multiply(BigDecimal.ONE));

    assertEquals(signedMoney(-2.468), signedMoney(-1.234).multiply(BigDecimal.valueOf(2)));
    assertEquals(signedMoney(-2.0),   signedMoney(-1.0).multiply(  BigDecimal.valueOf(2)));
    assertEquals(ZERO_SIGNED_MONEY,   ZERO_SIGNED_MONEY.multiply(  BigDecimal.valueOf(2)));
    assertEquals(signedMoney( 2.0),   signedMoney( 1.0).multiply(  BigDecimal.valueOf(2)));
    assertEquals(signedMoney( 2.468), signedMoney( 1.234).multiply(BigDecimal.valueOf(2)));

    assertEquals(signedMoney(-0.617), signedMoney(-1.234).multiply(BigDecimal.valueOf(0.5)));
    assertEquals(signedMoney(-0.5),   signedMoney(-1.0).multiply(  BigDecimal.valueOf(0.5)));
    assertEquals(ZERO_SIGNED_MONEY,   ZERO_SIGNED_MONEY.multiply(  BigDecimal.valueOf(0.5)));
    assertEquals(signedMoney( 0.5),   signedMoney( 1.0).multiply(  BigDecimal.valueOf(0.5)));
    assertEquals(signedMoney( 0.617), signedMoney( 1.234).multiply(BigDecimal.valueOf(0.5)));
  }

  @Test
  public void testMultiplyUnitFraction() {
    assertEquals(ZERO_SIGNED_MONEY, signedMoney(-1.234).multiply(UNIT_FRACTION_0));
    assertEquals(ZERO_SIGNED_MONEY, signedMoney(-1.0).multiply(  UNIT_FRACTION_0));
    assertEquals(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY.multiply(  UNIT_FRACTION_0));
    assertEquals(ZERO_SIGNED_MONEY, signedMoney( 1.0).multiply(  UNIT_FRACTION_0));
    assertEquals(ZERO_SIGNED_MONEY, signedMoney( 1.234).multiply(UNIT_FRACTION_0));

    assertEquals(signedMoney(-0.1234), signedMoney(-1.234).multiply(unitFractionInPct(10)));
    assertEquals(signedMoney(-0.1),    signedMoney(-1.0).multiply(  unitFractionInPct(10)));
    assertEquals(ZERO_SIGNED_MONEY,    ZERO_SIGNED_MONEY.multiply(  unitFractionInPct(10)));
    assertEquals(signedMoney( 0.1),    signedMoney( 1.0).multiply(  unitFractionInPct(10)));
    assertEquals(signedMoney( 0.1234), signedMoney( 1.234).multiply(unitFractionInPct(10)));

    assertEquals(signedMoney(-0.617), signedMoney(-1.234).multiply(unitFractionInPct(50)));
    assertEquals(signedMoney(-0.5),   signedMoney(-1.0).multiply(  unitFractionInPct(50)));
    assertEquals(ZERO_SIGNED_MONEY,   ZERO_SIGNED_MONEY.multiply(  unitFractionInPct(50)));
    assertEquals(signedMoney( 0.5),   signedMoney( 1.0).multiply(  unitFractionInPct(50)));
    assertEquals(signedMoney( 0.617), signedMoney( 1.234).multiply(unitFractionInPct(50)));

    assertEquals(signedMoney(-1.234), signedMoney(-1.234).multiply(UNIT_FRACTION_1));
    assertEquals(signedMoney(-1),     signedMoney(-1.0).multiply(  UNIT_FRACTION_1));
    assertEquals(ZERO_SIGNED_MONEY,   ZERO_SIGNED_MONEY.multiply(  UNIT_FRACTION_1));
    assertEquals(signedMoney(1),      signedMoney( 1.0).multiply(  UNIT_FRACTION_1));
    assertEquals(signedMoney(1.234),  signedMoney( 1.234).multiply(UNIT_FRACTION_1));
  }

  @Test
  public void testMultiplyByDouble() {
    assertEquals(signedMoney(-246.90), signedMoney(123.45).multiply(-2.0));
    assertEquals(signedMoney(-123.45), signedMoney(123.45).multiply(-1.0));
    assertEquals(signedMoney(-12.345), signedMoney(123.45).multiply(-0.1));
    assertEquals(ZERO_SIGNED_MONEY,    signedMoney(123.45).multiply( 0.0));
    assertEquals(signedMoney( 12.345), signedMoney(123.45).multiply( 0.1));
    assertEquals(signedMoney( 123.45), signedMoney(123.45).multiply( 1.0));
    assertEquals(signedMoney( 246.90), signedMoney(123.45).multiply( 2.0));
  }

  @Test
  public void testDivideByDouble() {
    assertEquals(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY.divide(-567.89));
    assertEquals(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY.divide( 567.89));

    assertEquals(signedMoney( 1.0), signedMoney(-123.45).divide(-123.45));
    assertEquals(signedMoney(-1.0), signedMoney( 123.45).divide(-123.45));
    assertEquals(signedMoney(-1.0), signedMoney(-123.45).divide( 123.45));
    assertEquals(signedMoney( 1.0), signedMoney( 123.45).divide( 123.45));

    assertEquals(signedMoney( 0.50), signedMoney(-100).divide(-200));
    assertEquals(signedMoney(-0.50), signedMoney(-100).divide( 200));
    assertEquals(signedMoney(-0.50), signedMoney( 100).divide(-200));
    assertEquals(signedMoney( 0.50), signedMoney( 100).divide( 200));

    assertThrowsAnyException( () -> signedMoney(-123.45).divide(0));
    assertThrowsAnyException( () -> signedMoney( 123.45).divide(0));
  }

  @Test
  public void testDivideByBigDecimal() {
    assertEquals(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY.divide(BigDecimal.valueOf(-567.89)));
    assertEquals(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY.divide(BigDecimal.valueOf( 567.89)));

    assertEquals(signedMoney( 12_345.00), signedMoney(-123.45).divide(BigDecimal.valueOf((-0.01))));
    assertEquals(signedMoney(-12_345.00), signedMoney(-123.45).divide(BigDecimal.valueOf(( 0.01))));
    assertEquals(signedMoney(-12_345.00), signedMoney( 123.45).divide(BigDecimal.valueOf((-0.01))));
    assertEquals(signedMoney( 12_345.00), signedMoney( 123.45).divide(BigDecimal.valueOf(( 0.01))));

    assertEquals(signedMoney( 1.0), signedMoney(-123.45).divide(BigDecimal.valueOf((-123.45))));
    assertEquals(signedMoney(-1.0), signedMoney(-123.45).divide(BigDecimal.valueOf(( 123.45))));
    assertEquals(signedMoney(-1.0), signedMoney( 123.45).divide(BigDecimal.valueOf((-123.45))));
    assertEquals(signedMoney( 1.0), signedMoney( 123.45).divide(BigDecimal.valueOf(( 123.45))));

    assertEquals(signedMoney( 0.50), signedMoney(-100).divide(BigDecimal.valueOf((-200))));
    assertEquals(signedMoney(-0.50), signedMoney(-100).divide(BigDecimal.valueOf(( 200))));
    assertEquals(signedMoney(-0.50), signedMoney( 100).divide(BigDecimal.valueOf((-200))));
    assertEquals(signedMoney( 0.50), signedMoney( 100).divide(BigDecimal.valueOf(( 200))));

    assertThrowsAnyException( () -> signedMoney(-123.45).divide(BigDecimal.valueOf((0))));
    assertThrowsAnyException( () -> signedMoney( 123.45).divide(BigDecimal.valueOf((0))));
  }

  @Test
  public void testDivideByPrice() {
    assertEquals(ZERO_SIGNED_QUANTITY, ZERO_SIGNED_MONEY.divide(price(567.89)));

    assertEquals(signedQuantity(-12_345.00), signedMoney(-123.45).divide(price(0.01)));
    assertEquals(signedQuantity( 12_345.00), signedMoney( 123.45).divide(price(0.01)));

    assertEquals(signedQuantity(-1.0), signedMoney(-123.45).divide(price(123.45)));
    assertEquals(signedQuantity( 1.0), signedMoney( 123.45).divide(price(123.45)));

    assertEquals(signedQuantity(-0.50), signedMoney(-100).divide(price((200))));
    assertEquals(signedQuantity( 0.50), signedMoney( 100).divide(price((200))));
  }

  @Test
  public void abs() {
    assertEquals(signedMoney(1.23), signedMoney(-1.23).abs());
    assertEquals(ZERO_SIGNED_MONEY, ZERO_SIGNED_MONEY.abs());
    assertEquals(signedMoney(1.23), signedMoney(1.23).abs());
  }

  @Test
  public void doesNotPrintNegativeZero() {
    assertEquals("$ 0.00", signedMoney(-1e-14).toString());
    assertEquals("$ 0.00", ZERO_SIGNED_MONEY.toString());
    assertEquals("$ 0.00", signedMoney(BigDecimal.ZERO.negate()).toString());
    assertEquals("$ 0.00", signedMoney(BigDecimal.ZERO).negate().toString());
    assertEquals("$ 0.00", signedMoney(new BigDecimal("0.0000000000000000001").negate()).toString());
    assertEquals("$ 0.00", signedMoney(new BigDecimal("0.0000000000000000001")).negate().toString());
    assertEquals("$ 0.00", signedMoney(new BigDecimal("-0.0000000000000000001")).toString());
  }

  @Test
  public void printWithCommas_doesNotPrintCents() {
    assertEquals("$ 0",    ZERO_SIGNED_MONEY.toDollarsWithCommas());

    assertEquals("$ -0", signedMoney(-0.49).toDollarsWithCommas());
    assertEquals("$ 0",  signedMoney( 0.49).toDollarsWithCommas());

    assertEquals("$ -123", signedMoney(-123.45).toDollarsWithCommas());
    assertEquals("$ 123",  signedMoney( 123.45).toDollarsWithCommas());

    assertEquals("$ -123,457",  signedMoney(-123_456.78).toDollarsWithCommas());
    assertEquals("$ 123,457",   signedMoney( 123_456.78).toDollarsWithCommas());
  }

}
