package com.rb.nonbiz.functional;

import com.google.common.collect.Range;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.SignedMoney.ZERO_SIGNED_MONEY;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.nonbiz.functional.RBPredicates.isAlmostEqualTo;
import static com.rb.nonbiz.functional.RBPredicates.isBetweenInclusive;
import static com.rb.nonbiz.functional.RBPredicates.isEqualTo;
import static com.rb.nonbiz.functional.RBPredicates.isGreaterThan;
import static com.rb.nonbiz.functional.RBPredicates.isGreaterThanOrEqualTo;
import static com.rb.nonbiz.functional.RBPredicates.isIn;
import static com.rb.nonbiz.functional.RBPredicates.isLessThan;
import static com.rb.nonbiz.functional.RBPredicates.isLessThanOrEqualTo;
import static com.rb.nonbiz.functional.RBPredicates.isWithin;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBPredicatesTest {

  @Test
  public void testFiveSimpleComparisons() { // i.e. < <= > >= ==
    assertFalse(isGreaterThan(8).test(7));
    assertFalse(isGreaterThanOrEqualTo(8).test(7));
    assertTrue(isLessThan(8).test(7));
    assertTrue(isLessThanOrEqualTo(8).test(7));
    assertFalse(isEqualTo(8).test(7));

    assertFalse(isGreaterThan(7).test(7));
    assertTrue(isGreaterThanOrEqualTo(7).test(7));
    assertFalse(isLessThan(7).test(7));
    assertTrue(isLessThanOrEqualTo(7).test(7));
    assertTrue(isEqualTo(7).test(7));

    assertTrue(isGreaterThan(6).test(7));
    assertTrue(isGreaterThanOrEqualTo(6).test(7));
    assertFalse(isLessThan(6).test(7));
    assertFalse(isLessThanOrEqualTo(6).test(7));
    assertFalse(isEqualTo(6).test(7));

    assertTrue(isGreaterThan(-8).test(-7));
    assertTrue(isGreaterThanOrEqualTo(-8).test(-7));
    assertFalse(isLessThan(-8).test(-7));
    assertFalse(isLessThanOrEqualTo(-8).test(-7));
    assertFalse(isEqualTo(-8).test(-7));

    assertFalse(isGreaterThan(-7).test(-7));
    assertTrue(isGreaterThanOrEqualTo(-7).test(-7));
    assertFalse(isLessThan(-7).test(-7));
    assertTrue(isLessThanOrEqualTo(-7).test(-7));
    assertTrue(isEqualTo(-7).test(-7));

    assertFalse(isGreaterThan(-6).test(-7));
    assertFalse(isGreaterThanOrEqualTo(-6).test(-7));
    assertTrue(isLessThan(-6).test(-7));
    assertTrue(isLessThanOrEqualTo(-6).test(-7));
    assertFalse(isEqualTo(-6).test(-7));
  }

  @Test
  public void testIsAlmostEqualTo() {
    assertFalse(isAlmostEqualTo(signedMoney(7.59), epsilon(0.1)).test(signedMoney(7.7)));
    assertTrue(isAlmostEqualTo(signedMoney(7.61), epsilon(0.1)).test(signedMoney(7.7)));
    assertTrue(isAlmostEqualTo(signedMoney(7.7), epsilon(0.1)).test(signedMoney(7.7)));
    assertTrue(isAlmostEqualTo(signedMoney(7.79), epsilon(0.1)).test(signedMoney(7.7)));
    assertFalse(isAlmostEqualTo(signedMoney(7.81), epsilon(0.1)).test(signedMoney(7.7)));

    assertFalse(isAlmostEqualTo(signedMoney(-7.59), epsilon(0.1)).test(signedMoney(-7.7)));
    assertTrue(isAlmostEqualTo(signedMoney(-7.61), epsilon(0.1)).test(signedMoney(-7.7)));
    assertTrue(isAlmostEqualTo(signedMoney(-7.7), epsilon(0.1)).test(signedMoney(-7.7)));
    assertTrue(isAlmostEqualTo(signedMoney(-7.79), epsilon(0.1)).test(signedMoney(-7.7)));
    assertFalse(isAlmostEqualTo(signedMoney(-7.81), epsilon(0.1)).test(signedMoney(-7.7)));
  }

  @Test
  public void testRangeIsIn() {
    double e = 1e-9;
    // closed ranges
    assertFalse(isIn(Range.closed(1.0, 2.0)).test( 0.0));
    assertFalse(isIn(Range.closed(1.0, 2.0)).test( 1.0 - e));

    assertTrue(isIn(Range.closed(1.0, 2.0)).test(1.0));
    assertTrue(isIn(Range.closed(1.0, 2.0)).test(1.5));
    assertTrue(isIn(Range.closed(1.0, 2.0)).test(2.0));

    assertFalse(isIn(Range.closed(1.0, 2.0)).test(2.0 + e));
    assertFalse(isIn(Range.closed(1.0, 2.0)).test(3.0));

    // open ranges
    assertFalse(isIn(Range.open(1.0, 2.0)).test(1.0));
    assertTrue( isIn(Range.open(1.0, 2.0)).test(1.5));
    assertFalse(isIn(Range.open(1.0, 2.0)).test(2.0));
  }

  @Test
  public void testRangeIsBetweenInclusive() {
    double e = 1e-9;

    assertFalse(isBetweenInclusive(1.0, 2.0).test( 0.0));
    assertFalse(isBetweenInclusive(1.0, 2.0).test( 1.0 - e));

    assertTrue(isBetweenInclusive(1.0, 2.0).test(1.0));
    assertTrue(isBetweenInclusive(1.0, 2.0).test(1.5));
    assertTrue(isBetweenInclusive(1.0, 2.0).test(2.0));

    assertFalse(isBetweenInclusive(1.0, 2.0).test(2.0 + e));
    assertFalse(isBetweenInclusive(1.0, 2.0).test(3.0));
  }

  @Test
  public void testRangeIsWithin() {
    double e = 1e-9;
    UnitFraction tenPercent = unitFraction(0.1);

    assertFalse(isWithin(money(1.0), tenPercent).test(ZERO_MONEY));
    assertFalse(isWithin(money(1.0), tenPercent).test(money(0.5)));
    assertFalse(isWithin(money(1.0), tenPercent).test(money(0.9 - e)));
    assertFalse(isWithin(money(1.0), tenPercent).test(money(0.9)));  // counterintuitive? abs((0.9 - 1.0) / 0.9) > 0.1
    assertTrue( isWithin(money(1.0), tenPercent).test(money(1.0)));
    assertTrue( isWithin(money(1.0), tenPercent).test(money(1.1)));  //                   abs((1.1 - 1.0) / 1.1) < 0.1
    assertTrue( isWithin(money(1.0), tenPercent).test(money(1.1 + e)));

    assertFalse(isWithin(money(100), tenPercent).test(money( 90))); //  90 is NOT within 10% of 100
    assertTrue( isWithin(money(100), tenPercent).test(money(110))); // 110 IS     within 10% of 100

    assertFalse(isWithin(money(123), UNIT_FRACTION_0).test(money(123 - e)));
    assertTrue( isWithin(money(123), UNIT_FRACTION_0).test(money(123)));
    assertFalse(isWithin(money(123), UNIT_FRACTION_0).test(money(123 + e)));

    assertFalse(isWithin(signedMoney(-1), tenPercent).test(ZERO_SIGNED_MONEY));
    assertFalse(isWithin(signedMoney(-1), tenPercent).test(signedMoney(-0.9)));  // NOTE ORDERING: abs(-0.9 - (-1)) / 0.9)    > 0.1
    assertTrue( isWithin(signedMoney(-1), tenPercent).test(signedMoney(-1)));
    assertTrue( isWithin(signedMoney(-1), tenPercent).test(signedMoney(-1.1)));  // NOTE ORDERING: abs(-1.1 - (-1)) /( -1.1)) < 0.1

    // zero is a special case; must match exactly
    assertFalse(isWithin(signedMoney(-0.1), UNIT_FRACTION_1).test(signedMoney(0.0)));
    assertFalse(isWithin(signedMoney(  -e), UNIT_FRACTION_1).test(signedMoney(0.0)));
    assertTrue( isWithin(signedMoney( 0.0), UNIT_FRACTION_1).test(signedMoney(0.0)));
    assertFalse(isWithin(signedMoney(   e), UNIT_FRACTION_1).test(signedMoney(0.0)));
    assertFalse(isWithin(signedMoney( 0.1), UNIT_FRACTION_1).test(signedMoney(0.0)));
  }

}
