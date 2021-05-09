package com.rb.nonbiz.types;

import com.rb.biz.types.Money;
import com.rb.biz.types.SignedMoney;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.SignedMoney.ZERO_SIGNED_MONEY;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.types.MoneySignedFraction.emptyMoneySignedFraction;
import static com.rb.nonbiz.types.MoneySignedFraction.moneySignedFraction;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static org.junit.Assert.assertEquals;

public class MoneySignedFractionTest {

  @Test
  public void testAsSignedFraction() {
    assertFractionComponents(signedMoney(-14),  money(7),  signedFraction(-2));
    assertFractionComponents(signedMoney(-7),   money(7),  signedFraction(-1));
    assertFractionComponents(signedMoney(-2.5), money(10), signedFraction(-0.25));
    assertFractionComponents(ZERO_SIGNED_MONEY, money(7),  SIGNED_FRACTION_0);
    assertFractionComponents(signedMoney(2.5),  money(10), signedFraction(0.25));
    assertFractionComponents(signedMoney(7),    money(7),  SIGNED_FRACTION_1);
    assertFractionComponents(signedMoney(14),   money(7),  signedFraction(2));
  }

  @Test
  public void zeroOverZero() {
    // the following are equivalent:
    assertOptionalEmpty(emptyMoneySignedFraction().asSignedFraction());
    assertOptionalEmpty(moneySignedFraction(ZERO_SIGNED_MONEY, ZERO_MONEY).asSignedFraction());

    assertEquals(ZERO_SIGNED_MONEY, moneySignedFraction(ZERO_SIGNED_MONEY, ZERO_MONEY).getNumerator());
    assertEquals(ZERO_SIGNED_MONEY, emptyMoneySignedFraction().getNumerator());
    assertEquals(ZERO_MONEY, moneySignedFraction(ZERO_SIGNED_MONEY, ZERO_MONEY).getDenominator());
  }

  @Test
  public void denominatorLessThanNumerator_valid() {
    MoneySignedFraction doesNotThrow;
    doesNotThrow = moneySignedFraction(signedMoney(100), money(99.99));
    doesNotThrow = moneySignedFraction(signedMoney(-100), money(99.99));
  }

  private void assertFractionComponents(SignedMoney numerator, Money denominator, SignedFraction signedFraction) {
    MoneySignedFraction moneySignedFraction = moneySignedFraction(numerator, denominator);
    assertEquals(numerator,   moneySignedFraction.getNumerator());
    assertEquals(denominator, moneySignedFraction.getDenominator());
    assertOptionalEquals(signedFraction, moneySignedFraction.asSignedFraction());
  }

  @Test
  public void positiveOverZero_throws() {
    assertIllegalArgumentException( () -> moneySignedFraction(signedMoney(100), ZERO_MONEY));
  }

  /**
   * MoneySignedFraction is a PreciseValue, so we can compare them using matchAlmostEquals etc. -
   * just like all PreciseValue subclasses. However, if our semantics is such that $10 / $40 is not the same as
   * $100 / $400 (even though, as a fraction, these are equal), then we want to use this matcher.
   */
  public static TypeSafeMatcher<MoneySignedFraction> moneySignedFractionNumeratorAndDenominatorMatcher(MoneySignedFraction expected) {
    return makeMatcher(expected,
        matchUsingAlmostEquals(v -> v.getNumerator(), 1e-8),
        matchUsingAlmostEquals(v -> v.getDenominator(), 1e-8));
  }

}
