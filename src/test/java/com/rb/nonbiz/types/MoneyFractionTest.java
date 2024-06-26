package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_MONEY;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.MoneyFraction.moneyFraction;
import static org.junit.Assert.assertEquals;

public class MoneyFractionTest extends RBTestMatcher<MoneyFraction> {

  public static MoneyFraction testMoneyFractionWithSeed(double seed) {
    return moneyFraction(
        money(123.45 + seed),
        money(567.89 + seed));
  }

  @Test
  public void validArguments() {
    MoneyFraction doesNotThrow;
    doesNotThrow = moneyFraction(ZERO_MONEY, DUMMY_MONEY);
    doesNotThrow = moneyFraction(ZERO_MONEY, ZERO_MONEY);    // $0 / $0 is supported (fraction equals 0.0)
    doesNotThrow = moneyFraction(DUMMY_MONEY, DUMMY_MONEY);
    doesNotThrow = moneyFraction(money(123.45), money(678.90));
    doesNotThrow = moneyFraction(money(678.90), money(123.45));

    assertIllegalArgumentException( () -> moneyFraction(money(123.45), ZERO_MONEY));
  }

  @Test
  public void numeratorAndDenominator() {
    MoneyFraction moneyFraction = moneyFraction(money(123.45), money(678.90));
    assertEquals(money(123.45), moneyFraction.getNumerator());
    assertEquals(money(678.90), moneyFraction.getDenominator());
  }

  @Test
  public void toPercent() {
    assertEquals("0.00 %", moneyFraction(ZERO_MONEY, ZERO_MONEY).toPercentString(2));
    assertEquals("45.67", moneyFraction(money(45.67), money(100.0)).toPercentString(2, false));
    assertEquals("45.67", moneyFraction(money(2 * 45.67), money(2 * 100.00)).toPercentString(2, false));
    assertEquals("234.57", moneyFraction(money(2_345.67), money(1_000.0)).toPercentString(2, false));
  }

  @Test
  public void toBasisPoints() {
    assertEquals("0.00 bps", moneyFraction(ZERO_MONEY, ZERO_MONEY).toBasisPoints(2));
    assertEquals("4567.8", moneyFraction(money(45.678), money(100.0)).toBasisPoints(1, false));
    assertEquals("4567.8", moneyFraction(money(2 * 45.678), money(2 * 100.00)).toBasisPoints(1, false));
    assertEquals("23456.7", moneyFraction(money(2_345.67), money(1_000.0)).toBasisPoints(1, false));
    assertEquals("1.5", moneyFraction(money(15), money(100_000)).toBasisPoints(1, false));
  }

  @Test
  public void testToString() {
    MoneyFraction moneyFraction = moneyFraction(money(123.45), money(1_000));

    assertEquals("12.35 %",     moneyFraction.toString());
    assertEquals("12.345 %",    moneyFraction.toPercentString(3));
    assertEquals("12.345",      moneyFraction.toPercentString(3, false));
    assertEquals("12.345 %",    moneyFraction.toPercentString(3, true));
    assertEquals("1234.50",     moneyFraction.toBasisPoints(2, false));
    assertEquals("1234.50 bps", moneyFraction.toBasisPoints(2, true));
  }

  @Override
  public MoneyFraction makeTrivialObject() {
    return moneyFraction(
        ZERO_MONEY,
        ZERO_MONEY);
  }

  @Override
  public MoneyFraction makeNontrivialObject() {
    return testMoneyFractionWithSeed(ZERO_SEED);
  }

  @Override
  public MoneyFraction makeMatchingNontrivialObject() {
    return testMoneyFractionWithSeed(EPSILON_SEED);
  }

  @Override
  protected boolean willMatch(MoneyFraction expected, MoneyFraction actual) {
    return moneyFractionMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<MoneyFraction> moneyFractionMatcher(MoneyFraction expected) {
    return makeMatcher(expected,
        matchUsingAlmostEquals(v -> v.getNumerator(),   DEFAULT_EPSILON_1e_8),
        matchUsingAlmostEquals(v -> v.getDenominator(), DEFAULT_EPSILON_1e_8));
  }

}
