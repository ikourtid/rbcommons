package com.rb.biz.types;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.collections.RBSet;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.rb.biz.types.IndexDivisor.indexDivisor;
import static com.rb.biz.types.IndexLevel.indexLevel;
import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.Money.sumMoney;
import static com.rb.biz.types.OnesBasedReturn.onesBasedReturn;
import static com.rb.biz.types.Price.price;
import static com.rb.biz.types.SignedMoney.ZERO_SIGNED_MONEY;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.trading.BuyQuantity.buyQuantity;
import static com.rb.biz.types.trading.NonNegativeQuantity.nonNegativeQuantity;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertThrows;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.testutils.RBPublicTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_ONE_HALF;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MoneyTest {

  @Test
  public void toMillions() {
    assertEquals("1000m", money(1_000_000_000).toMillions());
    assertEquals("999m", money(999_000_000).toMillions());
    assertEquals("1m", money(1_000_000).toMillions());
    assertEquals("1m", money(500_001).toMillions());
    assertEquals("0m", money(499_999).toMillions());
  }

  @Test
  public void negativeCash_throws() {
    assertThrows(IllegalArgumentException.class, () -> money(-0.0001));
    assertThrows(IllegalArgumentException.class, () -> money(new BigDecimal(-0.0001)));
  }

  @Test
  public void sums() {
    assertEquals(money(333.33), sumMoney(money(111.11), money(222.22)));
    assertEquals(money(777.77), sumMoney(money(111.11), money(222.22), money(444.44)));

    List<Money> moneyEmptyList = emptyList();
    List<Money> moneyList1 = singletonList(   money(111.11));
    List<Money> moneyList2 = ImmutableList.of(money(111.11), money(222.22));
    List<Money> moneyList3 = ImmutableList.of(money(111.11), money(222.22), money(444.44));

    assertEquals(ZERO_MONEY,    sumMoney(moneyEmptyList));
    assertEquals(money(111.11), sumMoney(moneyList1));
    assertEquals(money(333.33), sumMoney(moneyList2));
    assertEquals(money(777.77), sumMoney(moneyList3));

    assertEquals(ZERO_MONEY,    sumMoney(moneyEmptyList.iterator()));
    assertEquals(money(111.11), sumMoney(moneyList1.iterator()));
    assertEquals(money(333.33), sumMoney(moneyList2.iterator()));
    assertEquals(money(777.77), sumMoney(moneyList3.iterator()));

    assertEquals(ZERO_MONEY,    sumMoney(moneyEmptyList.stream()));
    assertEquals(money(111.11), sumMoney(moneyList1.stream()));
    assertEquals(money(333.33), sumMoney(moneyList2.stream()));
    assertEquals(money(777.77), sumMoney(moneyList3.stream()));

    RBSet<Money> moneyEmptyRBSet = emptyRBSet();
    RBSet<Money> moneyRBSet1 = singletonRBSet(money(111.11));
    RBSet<Money> moneyRBSet2 = newRBSet(      money(111.11), money(222.22));
    RBSet<Money> moneyRBSet3 = newRBSet(      money(111.11), money(222.22), money(444.44));

    assertEquals(ZERO_MONEY,    sumMoney(moneyEmptyRBSet));
    assertEquals(money(111.11), sumMoney(moneyRBSet1));
    assertEquals(money(333.33), sumMoney(moneyRBSet2));
    assertEquals(money(777.77), sumMoney(moneyRBSet3));
  }

  @Test
  public void add() {
    assertEquals(ZERO_MONEY,    ZERO_MONEY.add(ZERO_MONEY));
    assertEquals(money(123.45), ZERO_MONEY.add(money(123.45)));
    assertEquals(money(123.45), money(123.45).add(ZERO_MONEY));
    assertEquals(money(333.33), money(111.11).add(money(222.22)));
    assertEquals(money(333.33), money(222.22).add(money(111.11)));
  }

  @Test
  public void addOptional() {
    assertEquals(ZERO_MONEY,    ZERO_MONEY.addOptional(Optional.of(ZERO_MONEY)));
    assertEquals(money(123.45), ZERO_MONEY.addOptional(Optional.of(money(123.45))));
    assertEquals(money(123.45), money(123.45).addOptional(Optional.of(ZERO_MONEY)));
    assertEquals(money(333.33), money(111.11).addOptional(Optional.of(money(222.22))));
    assertEquals(money(333.33), money(222.22).addOptional(Optional.of(money(111.11))));

    assertEquals(ZERO_MONEY,    ZERO_MONEY.addOptional(Optional.empty()));
    assertEquals(money(123.45), money(123.45).addOptional(Optional.empty()));
  }

  @Test
  public void subtract() {
    assertEquals(ZERO_MONEY, ZERO_MONEY.subtract(ZERO_MONEY));
    assertIllegalArgumentException( () -> ZERO_MONEY.subtract(money(123.45)));
    assertEquals(money(123.45), money(123.45).subtract(ZERO_MONEY));
    assertEquals(money(111.11), money(333.33).subtract(money(222.22)));
  }

  @Test
  public void subtract_overloadWhereWeCanSpecifyErrorMessage() {
    String exceptionMessage = DUMMY_STRING;
    assertEquals(ZERO_MONEY,              ZERO_MONEY.subtract(ZERO_MONEY,    exceptionMessage));
    assertIllegalArgumentException( () -> ZERO_MONEY.subtract(money(123.45), exceptionMessage));
    assertEquals(money(123.45),        money(123.45).subtract(ZERO_MONEY,    exceptionMessage));
    assertEquals(money(111.11),        money(333.33).subtract(money(222.22), exceptionMessage));
  }

  @Test
  public void subtractSignedMoney() {
    assertEquals(money(555.55), money(444.44).subtract(signedMoney(-111.11)));
    assertEquals(money(444.44), money(444.44).subtract(ZERO_SIGNED_MONEY));
    assertEquals(money(333.33), money(444.44).subtract(signedMoney(111.11)));
    assertEquals(money(111.11), money(444.44).subtract(signedMoney(333.33)));

    assertIllegalArgumentException( () -> money(444.44).subtract(signedMoney(666.66)));
  }

  @Test
  public void subtractToSigned() {
    assertEquals(ZERO_SIGNED_MONEY,    ZERO_MONEY.subtractToSigned(   ZERO_MONEY));
    assertEquals(signedMoney(-123.45), ZERO_MONEY.subtractToSigned(   money(123.45)));
    assertEquals(signedMoney( 123.45), money(123.45).subtractToSigned(ZERO_MONEY));
    assertEquals(signedMoney( 111.11), money(333.33).subtractToSigned(money(222.22)));

    assertEquals(ZERO_SIGNED_MONEY, ZERO_MONEY.subtractToSigned(      ZERO_SIGNED_MONEY));
    assertEquals(signedMoney(-123.45), ZERO_MONEY.subtractToSigned(   signedMoney(123.45)));
    assertEquals(signedMoney( 123.45), money(123.45).subtractToSigned(ZERO_SIGNED_MONEY));
    assertEquals(signedMoney( 111.11), money(333.33).subtractToSigned(signedMoney(222.22)));

    assertEquals(signedMoney(intExplained(400, 100 - (-300))), money(100).subtractToSigned(signedMoney(-300)));
  }

  @Test
  public void testSubtractAndFloorToZero() {
    assertEquals(ZERO_MONEY,    ZERO_MONEY.subtractAndFloorToZero(money(1.2345)));
    assertEquals(ZERO_MONEY,    ZERO_MONEY.subtractAndFloorToZero(ZERO_MONEY));
    assertEquals(money(1.2345), money(1.2345).subtractAndFloorToZero(ZERO_MONEY));
    assertEquals(ZERO_MONEY,    money(1.2345).subtractAndFloorToZero(money(1.2345)));

    assertEquals(money(2.2), money(3.3).subtractAndFloorToZero(money(1.1)));
    assertEquals(ZERO_MONEY, money(1.1).subtractAndFloorToZero(money(3.3)));
  }

  @Test
  public void multiply() {
    assertAlmostEquals(money(222.22), money(444.44).multiply(0.5), 1e-8);
    assertAlmostEquals(money(444.44), money(444.44).multiply(1.0), 1e-8);
    assertAlmostEquals(money(888.88), money(444.44).multiply(2.0), 1e-8);

    assertAlmostEquals(money(222.22), money(444.44).multiply(BigDecimal.valueOf(0.5)), 1e-8);
    assertAlmostEquals(money(444.44), money(444.44).multiply(BigDecimal.valueOf(1.0)), 1e-8);
    assertAlmostEquals(money(888.88), money(444.44).multiply(BigDecimal.valueOf(2.0)), 1e-8);

    assertAlmostEquals(money(222.22), money(444.44).multiply(onesBasedReturn(0.5)), 1e-8);
    assertAlmostEquals(money(444.44), money(444.44).multiply(onesBasedReturn(1.0)), 1e-8);
    assertAlmostEquals(money(888.88), money(444.44).multiply(onesBasedReturn(2.0)), 1e-8);

    assertAlmostEquals(money(  0.00), money(444.44).multiply(UNIT_FRACTION_0),        1e-8);
    assertAlmostEquals(money(222.22), money(444.44).multiply(UNIT_FRACTION_ONE_HALF), 1e-8);
    assertAlmostEquals(money(444.44), money(444.44).multiply(UNIT_FRACTION_1),        1e-8);

    assertAlmostEquals(money(444.44), money(444.44).multiply(1L), 1e-8);
    assertAlmostEquals(money(888.88), money(444.44).multiply(2L), 1e-8);
  }

  @Test
  public void multiplyToSigned() {
    assertAlmostEquals(signedMoney(-888.88), money(444.44).multiplyToSigned(-2.0), 1e-8);
    assertAlmostEquals(signedMoney(-444.44), money(444.44).multiplyToSigned(-1.0), 1e-8);
    assertAlmostEquals(signedMoney(-222.22), money(444.44).multiplyToSigned(-0.5), 1e-8);
    assertAlmostEquals(signedMoney(    0.0), money(444.44).multiplyToSigned( 0.0), 1e-8);
    assertAlmostEquals(signedMoney( 222.22), money(444.44).multiplyToSigned( 0.5), 1e-8);
    assertAlmostEquals(signedMoney( 444.44), money(444.44).multiplyToSigned( 1.0), 1e-8);
    assertAlmostEquals(signedMoney( 888.88), money(444.44).multiplyToSigned( 2.0), 1e-8);
  }

  @Test
  public void divide() {
    assertTrue(BigDecimal.ZERO.compareTo(ZERO_MONEY.divide(money(123.0))) == 0);
    assertIllegalArgumentException( () -> ZERO_MONEY.divide(ZERO_MONEY));
    assertIllegalArgumentException( () -> money(123.0).divide(ZERO_MONEY));
    assertIllegalArgumentException( () -> money(123.0).divide(BigDecimal.ZERO));
    assertIllegalArgumentException( () -> money(123.0).divide(BigDecimal.valueOf(-1)));
    assertIllegalArgumentException( () -> money(123.0).divide( 0.0));
    assertIllegalArgumentException( () -> money(123.0).divide(-1.0));

    assertEquals(2.0, money(888.88).divide(money(444.44)).doubleValue(), 1e-8);
    assertEquals(1.0, money(888.88).divide(money(888.88)).doubleValue(), 1e-8);
    assertEquals(0.5, money(444.44).divide(money(888.88)).doubleValue(), 1e-8);

    assertAlmostEquals(nonNegativeQuantity(2.0), money(444.44).divide(price(222.22)), 1e-8);
    assertAlmostEquals(nonNegativeQuantity(1.0), money(444.44).divide(price(444.44)), 1e-8);
    assertAlmostEquals(nonNegativeQuantity(0.5), money(444.44).divide(price(888.88)), 1e-8);

    assertAlmostEquals(money(888.88), money(444.44).divide(BigDecimal.valueOf(0.5)), 1e-8);
    assertAlmostEquals(money(444.44), money(444.44).divide(BigDecimal.valueOf(1.0)), 1e-8);
    assertAlmostEquals(money(222.22), money(444.44).divide(BigDecimal.valueOf(2.0)), 1e-8);

    assertAlmostEquals(money(888.88), money(444.44).divide(0.5), 1e-8);
    assertAlmostEquals(money(444.44), money(444.44).divide(1.0), 1e-8);
    assertAlmostEquals(money(222.22), money(444.44).divide(2.0), 1e-8);
  }

  @Test
  public void testDivideByIndexDivisor() {
    assertAlmostEquals(indexLevel(100), money(1_000).divide(indexDivisor(10)), 1e-8);
    assertAlmostEquals(indexLevel(2_000), money(1_000).divide(indexDivisor(0.5)), 1e-8);
  }

  @Test
  public void isSuperSlightlyNegative_becomesZero() {
    assertIllegalArgumentException( () -> money(-1e-14));
    assertEquals(ZERO_MONEY, money(1.00).subtract(money(1.00 + 1e-14)));
  }

  @Test
  public void toSignedMoney() {
    assertIllegalArgumentException( () -> money(-444.44).toSignedMoney());

    assertEquals(ZERO_SIGNED_MONEY,   ZERO_MONEY.toSignedMoney());
    assertEquals(signedMoney(444.44), money(444.44).toSignedMoney());
  }

  @Test
  public void toBuyQuantity() {
    assertAlmostEquals(buyQuantity(888.88), money(444.44).calculateBuyQuantity(price(0.5)), 1e-8);
    assertAlmostEquals(buyQuantity(444.44), money(444.44).calculateBuyQuantity(price(1.0)), 1e-8);
    assertAlmostEquals(buyQuantity(222.22), money(444.44).calculateBuyQuantity(price(2.0)), 1e-8);
    assertAlmostEquals(buyQuantity(111.11), money(444.44).calculateBuyQuantity(price(4.0)), 1e-8);
  }

  @Test
  public void testToString() {
    assertEquals("$ 3.33",   money(10 / 3.0).toString());
    assertEquals("$ 3.3333", money(10 / 3.0).toString(4));
    assertEquals("$ 3.33", money(3.33).toString(4)); // does not print more precision than necessary
  }

  @Test
  public void testRoundToPennies() {
    assertEquals(ZERO_MONEY, ZERO_MONEY.roundToPennies());
    assertEquals(money(0.01),  money(0.009).roundToPennies());
    assertEquals(money(0.01),  money(0.010).roundToPennies());
    assertEquals(money(0.01),  money(0.011).roundToPennies());
    assertEquals(money(10),    money(9.996).roundToPennies());
    assertEquals(money(10),    money(10).roundToPennies());
    assertEquals(money(10),    money(10.004).roundToPennies());
    assertEquals(money(10.01), money(10.006).roundToPennies());
  }

  @Test
  public void testAddSigned() {
    assertEquals(ZERO_MONEY, ZERO_MONEY.addSigned(ZERO_SIGNED_MONEY));
    assertEquals(ZERO_MONEY, money(11.11).addSigned(signedMoney(-11.11)));
    assertEquals(money(10), money(11.11).addSigned(signedMoney(-1.11)));
    assertEquals(money(21.11), money(11.11).addSigned(signedMoney(10)));
    assertEquals(money(11.11), money(11.11).addSigned(ZERO_SIGNED_MONEY));
    assertIllegalArgumentException( () -> ZERO_MONEY.addSigned(signedMoney(-0.01)));
    assertIllegalArgumentException( () -> money(10).addSigned(signedMoney(-10.01)));
  }

  @Test
  public void testSubtractSigned() {
    assertEquals(ZERO_MONEY, ZERO_MONEY.subtractSigned(ZERO_SIGNED_MONEY));
    assertEquals(ZERO_MONEY, money(11.11).subtractSigned(signedMoney(11.11)));
    assertEquals(money(10), money(11.11).subtractSigned(signedMoney(1.11)));
    assertEquals(money(21.11), money(11.11).subtractSigned(signedMoney(-10)));
    assertEquals(money(11.11), money(11.11).subtractSigned(ZERO_SIGNED_MONEY));
    assertIllegalArgumentException( () -> ZERO_MONEY.subtractSigned(signedMoney(0.01)));
    assertIllegalArgumentException( () -> money(10).subtractSigned(signedMoney(10.01)));
  }

  @Test
  public void testToDollarsWithCommas() {
    assertEquals("$ 0", ZERO_MONEY.toDollarsWithCommas());
    assertEquals("$ 0", money(0.49).toDollarsWithCommas());
    assertEquals("$ 1", money(0.51).toDollarsWithCommas());
    assertEquals("$ 1,234,568", money(1_234_567.89).toDollarsWithCommas()); // note rounding
  }

}
