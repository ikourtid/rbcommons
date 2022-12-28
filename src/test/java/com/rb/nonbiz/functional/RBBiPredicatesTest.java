package com.rb.nonbiz.functional;

import com.rb.biz.types.Money;
import com.rb.biz.types.SignedMoney;
import com.rb.biz.types.trading.BuyQuantity;
import com.rb.biz.types.trading.NonNegativeQuantity;
import com.rb.biz.types.trading.PositiveQuantity;
import org.junit.Test;

import java.util.function.BiFunction;

import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.trading.BuyQuantity.buyQuantity;
import static com.rb.biz.types.trading.NonNegativeQuantity.nonNegativeQuantity;
import static com.rb.biz.types.trading.PositiveQuantity.positiveQuantity;
import static com.rb.nonbiz.functional.RBBiPredicates.doubleIsAlmostEqualTo;
import static com.rb.nonbiz.functional.RBBiPredicates.doubleIsWithin;
import static com.rb.nonbiz.functional.RBBiPredicates.doubleMustIncreaseByAtLeast;
import static com.rb.nonbiz.functional.RBBiPredicates.doubleMustNotDecreaseByMoreThan;
import static com.rb.nonbiz.functional.RBBiPredicates.doubleMustNotIncreaseByMoreThan;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBBiPredicatesTest {

  @Test
  public void testFiveSimpleComparisons() { // i.e. < <= > >= ==
    assertTrue(RBBiPredicates.<Integer>isGreaterThan().test(8, 7));
    assertTrue(RBBiPredicates.<Integer>isGreaterThanOrEqualTo().test(8, 7));
    assertFalse(RBBiPredicates.<Integer>isLessThan().test(8, 7));
    assertFalse(RBBiPredicates.<Integer>isLessThanOrEqualTo().test(8, 7));
    assertFalse(RBBiPredicates.<Integer>isEqualTo().test(8, 7));

    assertFalse(RBBiPredicates.<Integer>isGreaterThan().test(7, 7));
    assertTrue(RBBiPredicates.<Integer>isGreaterThanOrEqualTo().test(7, 7));
    assertFalse(RBBiPredicates.<Integer>isLessThan().test(7, 7));
    assertTrue(RBBiPredicates.<Integer>isLessThanOrEqualTo().test(7, 7));
    assertTrue(RBBiPredicates.<Integer>isEqualTo().test(7, 7));

    assertFalse(RBBiPredicates.<Integer>isGreaterThan().test(6, 7));
    assertFalse(RBBiPredicates.<Integer>isGreaterThanOrEqualTo().test(6, 7));
    assertTrue(RBBiPredicates.<Integer>isLessThan().test(6, 7));
    assertTrue(RBBiPredicates.<Integer>isLessThanOrEqualTo().test(6, 7));
    assertFalse(RBBiPredicates.<Integer>isEqualTo().test(6, 7));

    assertFalse(RBBiPredicates.<Integer>isGreaterThan().test(-8, -7));
    assertFalse(RBBiPredicates.<Integer>isGreaterThanOrEqualTo().test(-8, -7));
    assertTrue(RBBiPredicates.<Integer>isLessThan().test(-8, -7));
    assertTrue(RBBiPredicates.<Integer>isLessThanOrEqualTo().test(-8, -7));
    assertFalse(RBBiPredicates.<Integer>isEqualTo().test(-8, -7));

    assertFalse(RBBiPredicates.<Integer>isGreaterThan().test(-7, -7));
    assertTrue(RBBiPredicates.<Integer>isGreaterThanOrEqualTo().test(-7, -7));
    assertFalse(RBBiPredicates.<Integer>isLessThan().test(-7, -7));
    assertTrue(RBBiPredicates.<Integer>isLessThanOrEqualTo().test(-7, -7));
    assertTrue(RBBiPredicates.<Integer>isEqualTo().test(-7, -7));

    assertTrue(RBBiPredicates.<Integer>isGreaterThan().test(-6, -7));
    assertTrue(RBBiPredicates.<Integer>isGreaterThanOrEqualTo().test(-6, -7));
    assertFalse(RBBiPredicates.<Integer>isLessThan().test(-6, -7));
    assertFalse(RBBiPredicates.<Integer>isLessThanOrEqualTo().test(-6, -7));
    assertFalse(RBBiPredicates.<Integer>isEqualTo().test(-6, -7));
  }

  @Test
  public void testDoubleIsAlmostEqualTo() {
    assertFalse(doubleIsAlmostEqualTo(epsilon(0.1)).test(7.59, 7.7));
    assertTrue(doubleIsAlmostEqualTo(epsilon(0.1)).test(7.61, 7.7));
    assertTrue(doubleIsAlmostEqualTo(epsilon(0.1)).test(7.7, 7.7));
    assertTrue(doubleIsAlmostEqualTo(epsilon(0.1)).test(7.79, 7.7));
    assertFalse(doubleIsAlmostEqualTo(epsilon(0.1)).test(7.81, 7.7));

    assertFalse(doubleIsAlmostEqualTo(epsilon(0.1)).test(-7.59, -7.7));
    assertTrue(doubleIsAlmostEqualTo(epsilon(0.1)).test(-7.61, -7.7));
    assertTrue(doubleIsAlmostEqualTo(epsilon(0.1)).test(-7.7, -7.7));
    assertTrue(doubleIsAlmostEqualTo(epsilon(0.1)).test(-7.79, -7.7));
    assertFalse(doubleIsAlmostEqualTo(epsilon(0.1)).test(-7.81, -7.7));
  }

  @Test
  public void testIsAlmostEqualTo() {
    assertFalse(RBBiPredicates.<SignedMoney>isAlmostEqualTo(epsilon(0.1)).test(signedMoney(7.59), signedMoney(7.7)));
    assertTrue(RBBiPredicates.<SignedMoney>isAlmostEqualTo(epsilon(0.1)).test(signedMoney(7.61), signedMoney(7.7)));
    assertTrue(RBBiPredicates.<SignedMoney>isAlmostEqualTo(epsilon(0.1)).test(signedMoney(7.7), signedMoney(7.7)));
    assertTrue(RBBiPredicates.<SignedMoney>isAlmostEqualTo(epsilon(0.1)).test(signedMoney(7.79), signedMoney(7.7)));
    assertFalse(RBBiPredicates.<SignedMoney>isAlmostEqualTo(epsilon(0.1)).test(signedMoney(7.81), signedMoney(7.7)));

    assertFalse(RBBiPredicates.<SignedMoney>isAlmostEqualTo(epsilon(0.1)).test(signedMoney(-7.59), signedMoney(-7.7)));
    assertTrue(RBBiPredicates.<SignedMoney>isAlmostEqualTo(epsilon(0.1)).test(signedMoney(-7.61), signedMoney(-7.7)));
    assertTrue(RBBiPredicates.<SignedMoney>isAlmostEqualTo(epsilon(0.1)).test(signedMoney(-7.7), signedMoney(-7.7)));
    assertTrue(RBBiPredicates.<SignedMoney>isAlmostEqualTo(epsilon(0.1)).test(signedMoney(-7.79), signedMoney(-7.7)));
    assertFalse(RBBiPredicates.<SignedMoney>isAlmostEqualTo(epsilon(0.1)).test(signedMoney(-7.81), signedMoney(-7.7)));
  }

  @Test
  public void testDoubleIsWithin_positiveValues() {
    assertFalse(doubleIsWithin(unitFraction(0.2)).test(30_000.0, 23_999.0));
    assertTrue(doubleIsWithin(unitFraction(0.2)).test(30_000.0, 24_001.0));
    assertTrue(doubleIsWithin(unitFraction(0.2)).test(30_000.0, 29_999.0));
    assertTrue(doubleIsWithin(unitFraction(0.2)).test(30_000.0, 30_000.0));
    assertTrue(doubleIsWithin(unitFraction(0.2)).test(30_000.0, 30_001.0));
    assertTrue(doubleIsWithin(unitFraction(0.2)).test(30_000.0, 35_999.0));
    assertFalse(doubleIsWithin(unitFraction(0.2)).test(30_000.0, 36_001.0));
  }

  @Test
  public void testDoubleIsWithin_negativeValues() {
    assertFalse(doubleIsWithin(unitFraction(0.2)).test(-30_000.0, -23_999.0));
    assertTrue(doubleIsWithin(unitFraction(0.2)).test(-30_000.0, -24_001.0));
    assertTrue(doubleIsWithin(unitFraction(0.2)).test(-30_000.0, -29_999.0));
    assertTrue(doubleIsWithin(unitFraction(0.2)).test(-30_000.0, -30_000.0));
    assertTrue(doubleIsWithin(unitFraction(0.2)).test(-30_000.0, -30_001.0));
    assertTrue(doubleIsWithin(unitFraction(0.2)).test(-30_000.0, -35_999.0));
    assertFalse(doubleIsWithin(unitFraction(0.2)).test(-30_000.0, -36_001.0));
  }

  @Test
  public void testDoubleIsWithin_epsilonIsZero() {
    assertTrue(doubleIsWithin(UNIT_FRACTION_0).test(-1.0, -1.0));
    assertTrue(doubleIsWithin(UNIT_FRACTION_0).test(-0.5, -0.5));
    assertTrue(doubleIsWithin(UNIT_FRACTION_0).test(0.0, 0.0));
    assertTrue(doubleIsWithin(UNIT_FRACTION_0).test(0.5, 0.5));
    assertTrue(doubleIsWithin(UNIT_FRACTION_0).test(1.0, 1.0));
  }

  @Test
  public void testIsWithin_positiveValues() {
    assertFalse(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(23_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(24_001.0)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(29_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(30_000)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(30_001.0)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(35_999.0)));
    assertFalse(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(36_001.0)));
  }

  @Test
  public void testIsWithin_negativeValues() {
    assertFalse(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-23_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-24_001.0)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-29_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-30_000)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-30_001.0)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-35_999.0)));
    assertFalse(RBBiPredicates.<SignedMoney>isWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-36_001.0)));
  }

  @Test
  public void testIsNotWithin_positiveValues() {
    assertTrue(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(23_999.0)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(24_001.0)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(29_999.0)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(30_000)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(30_001.0)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(35_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(30_000), signedMoney(36_001.0)));
  }

  @Test
  public void testIsNotWithin_negativeValues() {
    assertTrue(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-23_999.0)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-24_001.0)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-29_999.0)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-30_000)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-30_001.0)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-35_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.2)).test(signedMoney(-30_000), signedMoney(-36_001.0)));
  }

  @Test
  public void testIsWithin_epsilonIsZero() {
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(UNIT_FRACTION_0).test(signedMoney(-1.0), signedMoney(-1.0)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(UNIT_FRACTION_0).test(signedMoney(-0.5), signedMoney(-0.5)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(UNIT_FRACTION_0).test(signedMoney(0.0), signedMoney(0.0)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(UNIT_FRACTION_0).test(signedMoney(0.5), signedMoney(0.5)));
    assertTrue(RBBiPredicates.<SignedMoney>isWithin(UNIT_FRACTION_0).test(signedMoney(1.0), signedMoney(1.0)));
  }

  @Test
  public void testIsNotWithin_epsilonIsZero() {
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(UNIT_FRACTION_0).test(signedMoney(-1.0), signedMoney(-1.0)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(UNIT_FRACTION_0).test(signedMoney(-0.5), signedMoney(-0.5)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(UNIT_FRACTION_0).test(signedMoney(0.0), signedMoney(0.0)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(UNIT_FRACTION_0).test(signedMoney(0.5), signedMoney(0.5)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(UNIT_FRACTION_0).test(signedMoney(1.0), signedMoney(1.0)));
  }

  @Test
  public void testDoubleMustNotIncreaseByMoreThan_positiveValues() {
    assertTrue(doubleMustNotIncreaseByMoreThan(unitFraction(0.2)).test(30_000.0, -1.0));
    assertTrue(doubleMustNotIncreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 0.0));
    assertTrue(doubleMustNotIncreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 23_999.0));
    assertTrue(doubleMustNotIncreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 24_001.0));
    assertTrue(doubleMustNotIncreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 29_999.0));
    assertTrue(doubleMustNotIncreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 30_000.0));
    assertTrue(doubleMustNotIncreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 30_001.0));
    assertTrue(doubleMustNotIncreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 35_999.0));
    assertTrue(doubleMustNotIncreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 36_000.0));
    assertFalse(doubleMustNotIncreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 36_001.0));
  }

  @Test
  public void testDoubleMustIncreaseByAtLeast_positiveValues() {
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(30_000.0, -1.0));
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(30_000.0, 0.0));
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(30_000.0, 23_999.0));
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(30_000.0, 24_001.0));
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(30_000.0, 29_999.0));
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(30_000.0, 30_000.0));
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(30_000.0, 30_001.0));
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(30_000.0, 35_999.0));
    assertTrue(doubleMustIncreaseByAtLeast( unitFraction(0.2)).test(30_000.0, 36_000.0));
    assertTrue(doubleMustIncreaseByAtLeast( unitFraction(0.2)).test(30_000.0, 36_001.0));
  }

  @Test
  public void testDoubleMustIncreaseByAtLeast_negativeValues() {
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(-30_000.0, -36_001.0));
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(-30_000.0, -35_999.0));
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(-30_000.0, -30_001.0));
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(-30_000.0, -30_000.0));
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(-30_000.0, -29_999.0));
    assertFalse(doubleMustIncreaseByAtLeast(unitFraction(0.2)).test(-30_000.0, -24_001.0));
    assertTrue(doubleMustIncreaseByAtLeast( unitFraction(0.2)).test(-30_000.0, -24_000.0));
    assertTrue(doubleMustIncreaseByAtLeast( unitFraction(0.2)).test(-30_000.0, -23_999.0));
    assertTrue(doubleMustIncreaseByAtLeast( unitFraction(0.2)).test(-30_000.0, -1.0));
    assertTrue(doubleMustIncreaseByAtLeast( unitFraction(0.2)).test(-30_000.0, 0.0));
    assertTrue(doubleMustIncreaseByAtLeast( unitFraction(0.2)).test(-30_000.0, 1.0));
  }

  @Test
  public void testDoubleMustNotDecreaseByMoreThan_positiveValues() {
    assertFalse(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(30_000.0, -1.0));
    assertFalse(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 0.0));
    assertFalse(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 23_999.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 24_001.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 29_999.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 30_000.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 30_001.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 35_999.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 36_000.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(30_000.0, 36_001.0));
  }

  @Test
  public void testDoubleMustNotDecreaseByMoreThan_negativeValues() {
    assertFalse(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(-30_000.0, -36_001.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(-30_000.0, -35_999.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(-30_000.0, -30_001.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(-30_000.0, -30_000.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(-30_000.0, -29_999.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(-30_000.0, -24_001.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(-30_000.0, -24_000.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(-30_000.0, -23_999.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(-30_000.0, -1.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(-30_000.0, 0.0));
    assertTrue(doubleMustNotDecreaseByMoreThan(unitFraction(0.2)).test(-30_000.0, 1.0));
  }

  @Test
  public void testMustNotIncreaseByMoreThan_positiveValues() {
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(-1.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(0.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(23_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(24_001.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(29_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(30_000.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(30_001.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(35_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(36_000.0)));
    assertFalse(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(36_001.0)));
  }

  @Test
  public void testMustNotIncreaseByMoreThan_negativeValues() {
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-36_001.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-35_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-30_001.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-30_000.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-29_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-24_001.0)));
    assertFalse(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-23_999.0)));
    assertFalse(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-1.0)));
    assertFalse(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(0.0)));
    assertFalse(RBBiPredicates.<SignedMoney>mustNotIncreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(1.0)));
  }

  @Test
  public void testMustIncreaseByAtLeast_positiveValues() {
    BiFunction<Double, Double, Boolean> maker = (starting, ending) ->
        RBBiPredicates.<SignedMoney>mustIncreaseByAtLeast(unitFraction(0.2))
            .test(signedMoney(starting), signedMoney(ending));

    assertFalse(maker.apply(30_000.0, -1.0));
    assertFalse(maker.apply(30_000.0, 0.0));
    assertFalse(maker.apply(30_000.0, 23_999.0));
    assertFalse(maker.apply(30_000.0, 24_001.0));
    assertFalse(maker.apply(30_000.0, 29_999.0));
    assertFalse(maker.apply(30_000.0, 30_000.0));
    assertFalse(maker.apply(30_000.0, 30_001.0));
    assertFalse(maker.apply(30_000.0, 35_999.0));
    assertTrue( maker.apply(30_000.0, 36_000.0));
    assertTrue( maker.apply(30_000.0, 36_001.0));
  }

  @Test
  public void testMustIncreaseByAtLeast_negativeValues() {
    BiFunction<Double, Double, Boolean> maker = (starting, ending) ->
        RBBiPredicates.<SignedMoney>mustIncreaseByAtLeast(unitFraction(0.2))
            .test(signedMoney(starting), signedMoney(ending));

    assertFalse(maker.apply(-30_000.0, -36_001.0));
    assertFalse(maker.apply(-30_000.0, -35_999.0));
    assertFalse(maker.apply(-30_000.0, -30_001.0));
    assertFalse(maker.apply(-30_000.0, -30_000.0));
    assertFalse(maker.apply(-30_000.0, -29_999.0));
    assertFalse(maker.apply(-30_000.0, -24_001.0));
    assertTrue(maker.apply(-30_000.0, -24_000.0));
    assertTrue(maker.apply(-30_000.0, -23_999.0));
    assertTrue(maker.apply(-30_000.0, -1.0));
    assertTrue(maker.apply(-30_000.0, 0.0));
    assertTrue(maker.apply(-30_000.0, 1.0));
  }

  @Test
  public void testMustDecreaseByAtLeast_positiveValues() {
    BiFunction<Double, Double, Boolean> maker = (starting, ending) ->
        RBBiPredicates.<SignedMoney>mustDecreaseByAtLeast(unitFraction(0.2))
            .test(signedMoney(starting), signedMoney(ending));

    assertTrue( maker.apply(30_000.0, -1.0));
    assertTrue( maker.apply(30_000.0, 0.0));
    assertTrue( maker.apply(30_000.0, 23_999.0));
    assertFalse(maker.apply(30_000.0, 24_001.0));
    assertFalse(maker.apply(30_000.0, 29_999.0));
    assertFalse(maker.apply(30_000.0, 30_000.0));
    assertFalse(maker.apply(30_000.0, 30_001.0));
    assertFalse(maker.apply(30_000.0, 35_999.0));
    assertFalse(maker.apply(30_000.0, 36_000.0));
    assertFalse(maker.apply(30_000.0, 36_001.0));
  }

  @Test
  public void testMustDecreaseByAtLeast_negativeValues() {
    BiFunction<Double, Double, Boolean> maker = (starting, ending) ->
        RBBiPredicates.<SignedMoney>mustDecreaseByAtLeast(unitFraction(0.2))
            .test(signedMoney(starting), signedMoney(ending));

    assertTrue(maker.apply(-30_000.0, -36_001.0));
    assertFalse(maker.apply(-30_000.0, -35_999.0));
    assertFalse(maker.apply(-30_000.0, -30_001.0));
    assertFalse(maker.apply(-30_000.0, -30_000.0));
    assertFalse(maker.apply(-30_000.0, -29_999.0));
    assertFalse(maker.apply(-30_000.0, -24_001.0));
    assertFalse(maker.apply(-30_000.0, -24_000.0));
    assertFalse(maker.apply(-30_000.0, -23_999.0));
    assertFalse(maker.apply(-30_000.0, -1.0));
    assertFalse(maker.apply(-30_000.0, 0.0));
    assertFalse(maker.apply(-30_000.0, 1.0));
  }

  @Test
  public void testMustNotDecreaseByMoreThan_positiveValues() {
    assertFalse(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(-1.0)));
    assertFalse(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(0.0)));
    assertFalse(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(23_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(24_001.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(29_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(30_000.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(30_001.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(35_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(30_000.0), signedMoney(36_001.0)));
  }

  @Test
  public void testMustNotDecreaseByMoreThan_negativeValues() {
    assertFalse(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-36_001.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-35_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-30_001.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-30_000.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-29_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-24_001.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-23_999.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(-1.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(0.0)));
    assertTrue(RBBiPredicates.<SignedMoney>mustNotDecreaseByMoreThan(unitFraction(0.2)).test(signedMoney(-30_000.0), signedMoney(1.0)));
  }

  @Test
  public void testPreciseValueExtendersComparisons() {
    assertTrue(RBBiPredicates.<NonNegativeQuantity>mustNotIncreaseByMoreThan(unitFraction(0.1)).test(nonNegativeQuantity(100.0), nonNegativeQuantity(105.0)));
    assertTrue(RBBiPredicates.<NonNegativeQuantity>mustNotIncreaseByMoreThan(unitFraction(0.1)).test(nonNegativeQuantity(100.0), nonNegativeQuantity(110.0)));
    assertFalse(RBBiPredicates.<NonNegativeQuantity>mustNotIncreaseByMoreThan(unitFraction(0.1)).test(nonNegativeQuantity(100.0), nonNegativeQuantity(115.0)));
    assertTrue(RBBiPredicates.<NonNegativeQuantity>mustNotIncreaseByMoreThan(unitFraction(0.1)).test(nonNegativeQuantity(100.0), nonNegativeQuantity(95.0)));
    assertTrue(RBBiPredicates.<NonNegativeQuantity>mustNotIncreaseByMoreThan(unitFraction(0.1)).test(nonNegativeQuantity(100.0), nonNegativeQuantity(90.0)));
    assertTrue(RBBiPredicates.<NonNegativeQuantity>mustNotIncreaseByMoreThan(unitFraction(0.1)).test(nonNegativeQuantity(100.0), nonNegativeQuantity(85.0)));

    assertTrue(RBBiPredicates.<PositiveQuantity>isAlmostEqualTo(epsilon(0.1)).test(positiveQuantity(10), positiveQuantity(10.01)));
    assertTrue(RBBiPredicates.<PositiveQuantity>isAlmostEqualTo(epsilon(0.1)).test(positiveQuantity(10), positiveQuantity(10.10)));
    assertFalse(RBBiPredicates.<PositiveQuantity>isAlmostEqualTo(epsilon(0.1)).test(positiveQuantity(10), positiveQuantity(10.20)));
    assertTrue(RBBiPredicates.<PositiveQuantity>isAlmostEqualTo(epsilon(0.1)).test(positiveQuantity(10), positiveQuantity(9.99)));
    assertTrue(RBBiPredicates.<PositiveQuantity>isAlmostEqualTo(epsilon(0.1)).test(positiveQuantity(10), positiveQuantity(9.90)));
    assertFalse(RBBiPredicates.<PositiveQuantity>isAlmostEqualTo(epsilon(0.1)).test(positiveQuantity(10), positiveQuantity(9.80)));

    assertTrue(RBBiPredicates.<BuyQuantity>isWithin(unitFraction(0.05)).test(buyQuantity(100), buyQuantity(104)));
    assertTrue(RBBiPredicates.<BuyQuantity>isWithin(unitFraction(0.05)).test(buyQuantity(100), buyQuantity(105)));
    assertFalse(RBBiPredicates.<BuyQuantity>isWithin(unitFraction(0.05)).test(buyQuantity(100), buyQuantity(106)));
    assertTrue(RBBiPredicates.<BuyQuantity>isWithin(unitFraction(0.05)).test(buyQuantity(100), buyQuantity(96)));
    assertTrue(RBBiPredicates.<BuyQuantity>isWithin(unitFraction(0.05)).test(buyQuantity(100), buyQuantity(95)));
    assertFalse(RBBiPredicates.<BuyQuantity>isWithin(unitFraction(0.05)).test(buyQuantity(100), buyQuantity(94)));

    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.05)).test(signedMoney(100), signedMoney(104)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.05)).test(signedMoney(100), signedMoney(105)));
    assertTrue(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.05)).test(signedMoney(100), signedMoney(106)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.05)).test(signedMoney(100), signedMoney(96)));
    assertFalse(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.05)).test(signedMoney(100), signedMoney(95)));
    assertTrue(RBBiPredicates.<SignedMoney>isNotWithin(unitFraction(0.05)).test(signedMoney(100), signedMoney(94)));
  }

  @Test
  public void testIsAlmostMultipliedBy() {
    assertFalse(RBBiPredicates.<Money>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(money(100), money(117)));
    assertTrue( RBBiPredicates.<Money>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(money(100), money(119)));
    assertTrue( RBBiPredicates.<Money>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(money(100), money(120)));
    assertTrue( RBBiPredicates.<Money>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(money(100), money(121)));
    assertFalse(RBBiPredicates.<Money>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(money(100), money(123)));

    // Cases where the denominator is 0 or almost 0
    // In these 4 cases, 1.2 * 0 = 0 (or thereabouts), so this evaluates to true
    assertTrue( RBBiPredicates.<Money>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(money(0),    money(0)));
    assertTrue( RBBiPredicates.<Money>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(money(1e-9), money(0)));
    assertTrue( RBBiPredicates.<Money>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(money(0),    money(1e-9)));
    assertTrue( RBBiPredicates.<Money>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(money(1e-9), money(1e-9)));

    // However, 1.2 * 0 != 789. But at least there's no divide-by-zero problem here.
    assertFalse(RBBiPredicates.<Money>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(money(0),    money(789)));
    assertFalse(RBBiPredicates.<Money>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(money(1e-9), money(789)));
  }

  @Test
  public void testIsAlmostMultipliedBy_negativeValues() {
    assertFalse(RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(-1.2, epsilon(0.02)).test(signedMoney(-100), signedMoney(117)));
    assertTrue( RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(-1.2, epsilon(0.02)).test(signedMoney(-100), signedMoney(119)));
    assertTrue( RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(-1.2, epsilon(0.02)).test(signedMoney(-100), signedMoney(120)));
    assertTrue( RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(-1.2, epsilon(0.02)).test(signedMoney(-100), signedMoney(121)));
    assertFalse(RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(-1.2, epsilon(0.02)).test(signedMoney(-100), signedMoney(123)));

    assertFalse(RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(-1.2, epsilon(0.02)).test(signedMoney(100), signedMoney(-117)));
    assertTrue( RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(-1.2, epsilon(0.02)).test(signedMoney(100), signedMoney(-119)));
    assertTrue( RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(-1.2, epsilon(0.02)).test(signedMoney(100), signedMoney(-120)));
    assertTrue( RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(-1.2, epsilon(0.02)).test(signedMoney(100), signedMoney(-121)));
    assertFalse(RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(-1.2, epsilon(0.02)).test(signedMoney(100), signedMoney(-123)));

    assertFalse(RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(signedMoney(-100), signedMoney(-117)));
    assertTrue( RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(signedMoney(-100), signedMoney(-119)));
    assertTrue( RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(signedMoney(-100), signedMoney(-120)));
    assertTrue( RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(signedMoney(-100), signedMoney(-121)));
    assertFalse(RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(signedMoney(-100), signedMoney(-123)));

    assertFalse(RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(signedMoney(100), signedMoney(117)));
    assertTrue( RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(signedMoney(100), signedMoney(119)));
    assertTrue( RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(signedMoney(100), signedMoney(120)));
    assertTrue( RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(signedMoney(100), signedMoney(121)));
    assertFalse(RBBiPredicates.<SignedMoney>isAlmostMultipliedBy(1.2, epsilon(0.02)).test(signedMoney(100), signedMoney(123)));
  }

}
