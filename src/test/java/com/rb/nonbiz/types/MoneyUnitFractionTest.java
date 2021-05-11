package com.rb.nonbiz.types;

import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.types.MoneyUnitFraction.emptyMoneyUnitFraction;
import static com.rb.nonbiz.types.MoneyUnitFraction.moneyUnitFraction;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.junit.Assert.assertEquals;

public class MoneyUnitFractionTest {

  @Test
  public void fraction0() {
    assertEquals(ZERO_MONEY, moneyUnitFraction(ZERO_MONEY, money(7)).getNumerator());
    assertEquals(money(7), moneyUnitFraction(ZERO_MONEY, money(7)).getDenominator());
    assertOptionalEquals(UNIT_FRACTION_0, moneyUnitFraction(ZERO_MONEY, money(7)).asFraction());
  }

  @Test
  public void fraction1() {
    assertEquals(money(7), moneyUnitFraction(money(7), money(7)).getNumerator());
    assertEquals(money(7), moneyUnitFraction(money(7), money(7)).getDenominator());
    assertOptionalEquals(UNIT_FRACTION_1, moneyUnitFraction(money(7), money(7)).asFraction());
  }

  @Test
  public void oneQuarter() {
    assertEquals(money(2.5), moneyUnitFraction(money(2.5), money(10)).getNumerator());
    assertEquals(money(10), moneyUnitFraction(money(2.5), money(10)).getDenominator());
    assertOptionalEquals(unitFraction(1, 4), moneyUnitFraction(money(2.5), money(10)).asFraction());
  }

  @Test
  public void zeroOverZero() {
    // the following are equivalent:
    assertOptionalEmpty(emptyMoneyUnitFraction().asFraction());
    assertOptionalEmpty(moneyUnitFraction(ZERO_MONEY, ZERO_MONEY).asFraction());

    assertEquals(ZERO_MONEY, moneyUnitFraction(ZERO_MONEY, ZERO_MONEY).getNumerator());
    assertEquals(ZERO_MONEY, moneyUnitFraction(ZERO_MONEY, ZERO_MONEY).getDenominator());
  }

  @Test
  public void denominatorLessThanNumerator() {
    assertIllegalArgumentException( () -> moneyUnitFraction(money(100), money(99.99)));
    assertIllegalArgumentException( () -> moneyUnitFraction(money(100), ZERO_MONEY));
  }

  @Test
  public void positiveNumerator() {
    assertIllegalArgumentException( () -> moneyUnitFraction(money(-10), money(100)));
  }

  @Test
  public void positiveDenominator() {
    assertIllegalArgumentException( () -> moneyUnitFraction(money(100), money(-100)));
  }

  @Test
  public void positiveOverZero_throws() {
    assertIllegalArgumentException( () -> moneyUnitFraction(money(100), ZERO_MONEY));
  }

  /**
   * MoneyUnitFraction is a PreciseValue, so we can compare them using matchAlmostEquals etc. -
   * just like all PreciseValue subclasses. However, if our semantics is such that $10 / $40 is not the same as
   * $100 / $400 (even though, as a fraction, these are equal), then we want to use this matcher.
   */
  public static TypeSafeMatcher<MoneyUnitFraction> moneyUnitFractionNumeratorAndDenominatorMatcher(MoneyUnitFraction expected) {
    return makeMatcher(expected,
        matchUsingAlmostEquals(v -> v.getNumerator(), 1e-8),
        matchUsingAlmostEquals(v -> v.getDenominator(), 1e-8));
  }

}
