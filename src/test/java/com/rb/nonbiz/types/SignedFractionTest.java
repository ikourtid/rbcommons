package com.rb.nonbiz.types;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertThrows;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SignedFractionTest {

  SignedFraction smallPos  = signedFraction(0.6);
  SignedFraction mediumPos = signedFraction(0.7);
  SignedFraction largePos  = signedFraction(0.8);

  SignedFraction smallNeg  = signedFraction(-5.5);
  SignedFraction mediumNeg = signedFraction(-4.4);
  SignedFraction largeNeg  = signedFraction(-3.3);

  @Test
  public void testEquality() {
    assertEquals(signedFraction(-0.6), signedFraction(-0.6));
    assertEquals(signedFraction(0.6), signedFraction(0.6));
    assertEquals(signedFraction(0.0), SIGNED_FRACTION_0);
    assertEquals(signedFraction(0), SIGNED_FRACTION_0);
    assertEquals(signedFraction(1.0), SIGNED_FRACTION_1);
    assertEquals(signedFraction(1), SIGNED_FRACTION_1);
  }

  @Test
  public void isZeroOrAlmostZero() {
    assertTrue(signedFraction(0.0).isZero());
    assertTrue(signedFraction(0).isZero());
    assertTrue(SIGNED_FRACTION_0.isZero());
    assertFalse(signedFraction(0.12345).isZero());
    assertFalse(signedFraction(-0.12345).isZero());

    assertTrue(signedFraction(0.0).isAlmostZero(1e-8));
    assertTrue(signedFraction(0).isAlmostZero(1e-8));
    assertTrue(SIGNED_FRACTION_0.isAlmostZero(1e-8));
    assertFalse(signedFraction(0.12345).isAlmostZero(1e-8));
    assertFalse(signedFraction(-0.12345).isAlmostZero(1e-8));

    assertTrue(signedFraction(0.12345).isAlmostZero(0.2));
    assertTrue(signedFraction(-0.12345).isAlmostZero(0.2));
  }

  @Test
  public void isOneOrAlmostOne() {
    assertTrue(signedFraction(1.0).isOne());
    assertTrue(signedFraction(1).isOne());
    assertTrue(SIGNED_FRACTION_1.isOne());
    assertFalse(signedFraction(0.12345).isOne());
    assertFalse(signedFraction(-0.12345).isOne());

    assertTrue(signedFraction(1.0).isAlmostOne(1e-8));
    assertTrue(signedFraction(1).isAlmostOne(1e-8));
    assertTrue(SIGNED_FRACTION_1.isAlmostOne(1e-8));
    assertFalse(signedFraction(0.12345).isAlmostOne(1e-8));
    assertFalse(signedFraction(-0.12345).isAlmostOne(1e-8));

    assertTrue(signedFraction(0.999999).isAlmostOne(0.01));
    assertTrue(signedFraction(1.000001).isAlmostOne(0.01));
  }

  @Test
  public void complement() {
    assertEquals(signedFraction(-0.25), signedFraction(1.25).complement());
    assertEquals(SIGNED_FRACTION_0,     SIGNED_FRACTION_1.complement());
    assertEquals(signedFraction(0.25),  signedFraction(0.75).complement());
    assertEquals(signedFraction(0.50),  signedFraction(0.50).complement());
    assertEquals(signedFraction(0.75),  signedFraction(0.25).complement());
    assertEquals(SIGNED_FRACTION_1,     SIGNED_FRACTION_0.complement());
    assertEquals(signedFraction(1.25),  signedFraction(-0.25).complement());
  }

  @Test
  public void negate() {
    assertEquals(signedFraction(-0.75), signedFraction(0.75).negate());
    assertEquals(signedFraction(-0.5),  signedFraction(0.5).negate());
    assertEquals(signedFraction(-0.25), signedFraction(0.25).negate());
    assertEquals(SIGNED_FRACTION_0,     SIGNED_FRACTION_0.negate());
    assertEquals(signedFraction( 0.25), signedFraction(-0.25).negate());
    assertEquals(signedFraction( 0.50), signedFraction(-0.50).negate());
    assertEquals(signedFraction( 0.75), signedFraction(-0.75).negate());
    assertEquals(SIGNED_FRACTION_1,     signedFraction(-1).negate());
    assertEquals(signedFraction( 1.25), signedFraction(-1.25).negate());
  }

  @Test
  public void isLessThan_positive() {
    assertTrue(!smallPos.isLessThan(smallPos));
    assertTrue(smallPos.isLessThan(mediumPos));
    assertTrue(smallPos.isLessThan(largePos));

    assertTrue(!mediumPos.isLessThan(smallPos));
    assertTrue(!mediumPos.isLessThan(mediumPos));
    assertTrue(mediumPos.isLessThan(largePos));

    assertTrue(!largePos.isLessThan(smallPos));
    assertTrue(!largePos.isLessThan(mediumPos));
    assertTrue(!largePos.isLessThan(largePos));
  }

  @Test
  public void isLessThan_negative() {
    assertTrue(!smallNeg.isLessThan(smallNeg));
    assertTrue(smallNeg.isLessThan(mediumNeg));
    assertTrue(smallNeg.isLessThan(largeNeg));

    assertTrue(!mediumNeg.isLessThan(smallNeg));
    assertTrue(!mediumNeg.isLessThan(mediumNeg));
    assertTrue(mediumNeg.isLessThan(largeNeg));

    assertTrue(!largeNeg.isLessThan(smallNeg));
    assertTrue(!largeNeg.isLessThan(mediumNeg));
    assertTrue(!largeNeg.isLessThan(largeNeg));
  }

  @Test
  public void isLessThanOrEqualTo_positive() {
    assertTrue(smallPos.isLessThanOrEqualTo(smallPos));
    assertTrue(smallPos.isLessThanOrEqualTo(mediumPos));
    assertTrue(smallPos.isLessThanOrEqualTo(largePos));

    assertTrue(!mediumPos.isLessThanOrEqualTo(smallPos));
    assertTrue(mediumPos.isLessThanOrEqualTo(mediumPos));
    assertTrue(mediumPos.isLessThanOrEqualTo(largePos));

    assertTrue(!largePos.isLessThanOrEqualTo(smallPos));
    assertTrue(!largePos.isLessThanOrEqualTo(mediumPos));
    assertTrue(largePos.isLessThanOrEqualTo(largePos));
  }

  @Test
  public void isLessThanOrEqualTo_negative() {
    assertTrue(smallNeg.isLessThanOrEqualTo(smallNeg));
    assertTrue(smallNeg.isLessThanOrEqualTo(mediumNeg));
    assertTrue(smallNeg.isLessThanOrEqualTo(largeNeg));

    assertTrue(!mediumNeg.isLessThanOrEqualTo(smallNeg));
    assertTrue(mediumNeg.isLessThanOrEqualTo(mediumNeg));
    assertTrue(mediumNeg.isLessThanOrEqualTo(largeNeg));

    assertTrue(!largeNeg.isLessThanOrEqualTo(smallNeg));
    assertTrue(!largeNeg.isLessThanOrEqualTo(mediumNeg));
    assertTrue(largeNeg.isLessThanOrEqualTo(largeNeg));
  }

  @Test
  public void isGreaterThan_positive() {
    assertTrue(!smallPos.isGreaterThan(smallPos));
    assertTrue(!smallPos.isGreaterThan(mediumPos));
    assertTrue(!smallPos.isGreaterThan(largePos));

    assertTrue(mediumPos.isGreaterThan(smallPos));
    assertTrue(!mediumPos.isGreaterThan(mediumPos));
    assertTrue(!mediumPos.isGreaterThan(largePos));

    assertTrue(largePos.isGreaterThan(smallPos));
    assertTrue(largePos.isGreaterThan(mediumPos));
    assertTrue(!largePos.isGreaterThan(largePos));
  }

  @Test
  public void isGreaterThan_negative() {
    assertTrue(!smallNeg.isGreaterThan(smallNeg));
    assertTrue(!smallNeg.isGreaterThan(mediumNeg));
    assertTrue(!smallNeg.isGreaterThan(largeNeg));

    assertTrue(mediumNeg.isGreaterThan(smallNeg));
    assertTrue(!mediumNeg.isGreaterThan(mediumNeg));
    assertTrue(!mediumNeg.isGreaterThan(largeNeg));

    assertTrue(largeNeg.isGreaterThan(smallNeg));
    assertTrue(largeNeg.isGreaterThan(mediumNeg));
    assertTrue(!largeNeg.isGreaterThan(largeNeg));
  }

  @Test
  public void isGreaterThanOrEqualTo_positive() {
    assertTrue(smallPos.isGreaterThanOrEqualTo(smallPos));
    assertTrue(!smallPos.isGreaterThanOrEqualTo(mediumPos));
    assertTrue(!smallPos.isGreaterThanOrEqualTo(largePos));

    assertTrue(mediumPos.isGreaterThanOrEqualTo(smallPos));
    assertTrue(mediumPos.isGreaterThanOrEqualTo(mediumPos));
    assertTrue(!mediumPos.isGreaterThanOrEqualTo(largePos));

    assertTrue(largePos.isGreaterThanOrEqualTo(smallPos));
    assertTrue(largePos.isGreaterThanOrEqualTo(mediumPos));
    assertTrue(largePos.isGreaterThanOrEqualTo(largePos));
  }

  @Test
  public void isGreaterThanOrEqualTo_negative() {
    assertTrue(smallNeg.isGreaterThanOrEqualTo(smallNeg));
    assertTrue(!smallNeg.isGreaterThanOrEqualTo(mediumNeg));
    assertTrue(!smallNeg.isGreaterThanOrEqualTo(largeNeg));

    assertTrue(mediumNeg.isGreaterThanOrEqualTo(smallNeg));
    assertTrue(mediumNeg.isGreaterThanOrEqualTo(mediumNeg));
    assertTrue(!mediumNeg.isGreaterThanOrEqualTo(largeNeg));

    assertTrue(largeNeg.isGreaterThanOrEqualTo(smallNeg));
    assertTrue(largeNeg.isGreaterThanOrEqualTo(mediumNeg));
    assertTrue(largeNeg.isGreaterThanOrEqualTo(largeNeg));
  }

  @Test
  public void testCanBeConvertedToUnitFraction() {
    rbSetOf(-12.34, -1e-9, 1 + 1e-9, 12.34)
        .forEach(d -> assertFalse(signedFraction(d).canBeConvertedToUnitFraction()));
    rbSetOf(0.0, 1e-9, 0.5, 1 - 1e-9, 1.0)
        .forEach(d -> assertTrue(signedFraction(d).canBeConvertedToUnitFraction()));
  }

  @Test
  public void toPercent() {
    assertEquals("-12.34 %", signedFraction(-0.1234).toPercentString());
    assertEquals("12.34 %", signedFraction(0.1234).toPercentString());
    assertEquals("100.00 %", SIGNED_FRACTION_1.toPercentString());
    assertEquals("20.00 %", signedFraction(0.2).toPercentString());
    assertEquals("0.00 %", SIGNED_FRACTION_0.toPercentString());
    assertEquals("33.33 %", signedFraction(1, 3).toPercentString());
    assertEquals("33.33 %", signedFraction(1 / 3.0).toPercentString());
    assertEquals("-33.33 %", signedFraction(-1, 3).toPercentString());
    assertEquals("-33.33 %", signedFraction(-1 / 3.0).toPercentString());
  }

  @Test
  public void toBasisPoints() {


    assertEquals("-1234.0 bps", signedFraction(-0.1234).toBasisPoints(1));
    assertEquals("1234.0 bps",  signedFraction(0.1234).toBasisPoints(1));
    assertEquals("10000.0 bps", SIGNED_FRACTION_1.toBasisPoints(1));
    assertEquals("2000.0 bps",  signedFraction(0.2).toBasisPoints(1));
    assertEquals("-2000.0 bps", signedFraction(-0.2).toBasisPoints(1));
    assertEquals("0.0 bps",     SIGNED_FRACTION_0.toBasisPoints(1));
    assertEquals("3333.3 bps",  signedFraction(1, 3).toBasisPoints(1));
    assertEquals("3333.3 bps",  signedFraction(1 / 3.0).toBasisPoints(1));
    assertEquals("-3333.3 bps", signedFraction(-1, 3).toBasisPoints(1));
    assertEquals("-3333.3 bps", signedFraction(-1 / 3.0).toBasisPoints(1));

    // specify whether to add "bps" suffix explicitly
    assertEquals("-1234.0",     signedFraction(-0.1234).toBasisPoints(1, false));
    assertEquals("-1234.0 bps", signedFraction(-0.1234).toBasisPoints(1, true));
    assertEquals("-3333.333",     signedFraction(-1 / 3.0).toBasisPoints(3, false));
    assertEquals("-3333.333 bps", signedFraction(-1 / 3.0).toBasisPoints(3, true));
  }

  @Test
  public void toBasisPointsDefaultPrecision() {
    assertEquals("-1234.00 bps", signedFraction(-0.1234).toBasisPoints());
    assertEquals("1234.00 bps",  signedFraction(0.1234).toBasisPoints());
    assertEquals("10000.00 bps", SIGNED_FRACTION_1.toBasisPoints());
    assertEquals("2000.00 bps",  signedFraction(0.2).toBasisPoints());
    assertEquals("-2000.00 bps", signedFraction(-0.2).toBasisPoints());
    assertEquals("0.00 bps",     SIGNED_FRACTION_0.toBasisPoints());
    assertEquals("3333.33 bps",  signedFraction(1, 3).toBasisPoints());
    assertEquals("3333.33 bps",  signedFraction(1 / 3.0).toBasisPoints());
    assertEquals("-3333.33 bps", signedFraction(-1, 3).toBasisPoints());
    assertEquals("-3333.33 bps", signedFraction(-1 / 3.0).toBasisPoints());
  }

  @Test
  public void constructorWithNumeratorAndDenominator() {
    for (Runnable r : new Runnable[] {
        () -> signedFraction(0, 0),
        () -> signedFraction(1, 0),
        () -> signedFraction(1, -1),
        () -> signedFraction(-1, -1),
        () -> signedFraction(3, -2)
    }) {
      assertThrows(IllegalArgumentException.class, r);
    }
    assertEquals(signedFraction(-1), signedFraction(-1, 1));
    assertEquals(signedFraction(-1.5), signedFraction(-3, 2));

    assertEquals(SIGNED_FRACTION_0, signedFraction(0, 1));
    assertEquals(SIGNED_FRACTION_0, signedFraction(0, 123));
    assertEquals(SIGNED_FRACTION_1, signedFraction(1, 1));
    assertEquals(SIGNED_FRACTION_1, signedFraction(123, 123));
    assertEquals(signedFraction(0.6), signedFraction(3, 5));
    assertEquals(signedFraction(0.6), signedFraction(6, 10));
    assertEquals(signedFraction(0.6), signedFraction(60, 100));
    assertEquals(1 / 3.0, signedFraction(1, 3).doubleValue(), 1e-8);
  }

  @Test
  public void fromString() {
    assertEquals(signedFraction(-1),     signedFraction("-1"));
    assertEquals(signedFraction(-0.123), signedFraction("-0.123"));
    assertEquals(SIGNED_FRACTION_0,      signedFraction("0"));
    assertEquals(signedFraction(0.123),  signedFraction("0.123"));
    assertEquals(SIGNED_FRACTION_1,      signedFraction("1"));
  }

  @Test
  public void add() {
    assertEquals(SIGNED_FRACTION_0, SIGNED_FRACTION_0.add(SIGNED_FRACTION_0));
    assertEquals(SIGNED_FRACTION_1, SIGNED_FRACTION_0.add(SIGNED_FRACTION_1));
    assertEquals(signedFraction(0.12345), SIGNED_FRACTION_0.add(signedFraction(0.12345)));
    assertEquals(signedFraction(0.12345), signedFraction(0.12345).add(SIGNED_FRACTION_0));
    assertEquals(signedFraction(0.9), signedFraction(0.3).add(signedFraction(0.6)));
    assertEquals(signedFraction(0.9), signedFraction(0.6).add(signedFraction(0.3)));
    assertEquals(SIGNED_FRACTION_1, signedFraction(0.3).add(signedFraction(0.7)));
    assertEquals(SIGNED_FRACTION_1, signedFraction(0.7).add(signedFraction(0.3)));
    assertEquals(signedFraction(1.01), signedFraction(0.3).add(signedFraction(0.71)));
    assertEquals(signedFraction(1.01), SIGNED_FRACTION_1.add(signedFraction(0.01)));
    assertEquals(signedFraction(-1.01), signedFraction(-0.3).add(signedFraction(-0.71)));
  }

  @Test
  public void addDouble() {
    assertEquals(SIGNED_FRACTION_0, SIGNED_FRACTION_0.add(0.0));
    assertEquals(SIGNED_FRACTION_1, SIGNED_FRACTION_0.add(1.0));
    assertEquals(SIGNED_FRACTION_1, SIGNED_FRACTION_1.add(0.0));
    assertEquals(signedFraction(0.12345), SIGNED_FRACTION_0.add(0.12345));
    assertEquals(signedFraction(0.12345), signedFraction(0.12345).add(0.0));
    assertEquals(signedFraction(0.9), signedFraction(0.3).add(0.6));
    assertEquals(signedFraction(0.9), signedFraction(0.6).add(0.3));
    assertEquals(SIGNED_FRACTION_1, signedFraction(0.3).add(0.7));
    assertEquals(SIGNED_FRACTION_1, signedFraction(0.7).add(0.3));
    assertEquals(signedFraction(1.01), signedFraction(0.3).add(0.71));
    assertEquals(signedFraction(1.01), SIGNED_FRACTION_1.add(0.01));
    assertEquals(signedFraction(-1.01), signedFraction(-0.3).add(-0.71));
  }

  @Test
  public void sum() {
    assertEquals(SIGNED_FRACTION_0, SignedFraction.sum(emptyList()));
    assertEquals(SIGNED_FRACTION_0, SignedFraction.sum(ImmutableList.of(SIGNED_FRACTION_0)));
    assertEquals(SIGNED_FRACTION_0, SignedFraction.sum(ImmutableList.of(SIGNED_FRACTION_0, SIGNED_FRACTION_0)));
    assertEquals(SIGNED_FRACTION_0, SignedFraction.sum(ImmutableList.of(SIGNED_FRACTION_1, signedFraction(-1))));

    assertEquals(SIGNED_FRACTION_1, SignedFraction.sum(ImmutableList.of(SIGNED_FRACTION_1)));
    assertEquals(SIGNED_FRACTION_1, SignedFraction.sum(ImmutableList.of(SIGNED_FRACTION_0, SIGNED_FRACTION_1)));
    assertEquals(SIGNED_FRACTION_1, SignedFraction.sum(ImmutableList.of(SIGNED_FRACTION_1, SIGNED_FRACTION_0)));

    assertEquals(SIGNED_FRACTION_1, SignedFraction.sum(ImmutableList.of(signedFraction(0.2), signedFraction(0.3), signedFraction(0.5))));
    assertEquals(SIGNED_FRACTION_1, SignedFraction.sum(ImmutableList.of(signedFraction(0.5), signedFraction(0.3), signedFraction(0.2))));

    assertEquals(signedFraction(0.6), SignedFraction.sum(ImmutableList.of(signedFraction(0.1), signedFraction(0.5))));
    assertEquals(signedFraction(0.6), SignedFraction.sum(ImmutableList.of(signedFraction(0.5), signedFraction(0.1))));
    assertEquals(signedFraction(0.6), SignedFraction.sum(ImmutableList.of(signedFraction(0.1), signedFraction(0.2), signedFraction( 0.3))));
    assertEquals(signedFraction(0.6), SignedFraction.sum(ImmutableList.of(signedFraction(0.1), signedFraction(0.8), signedFraction(-0.3))));

    assertEquals(signedFraction(-0.6), SignedFraction.sum(ImmutableList.of(signedFraction(-0.1), signedFraction(-0.5))));
    assertEquals(signedFraction(-0.6), SignedFraction.sum(ImmutableList.of(signedFraction(-0.5), signedFraction(-0.1))));
    assertEquals(signedFraction(-0.6), SignedFraction.sum(ImmutableList.of(signedFraction(-0.1), signedFraction(-0.2), signedFraction(-0.3))));
    assertEquals(signedFraction(-0.6), SignedFraction.sum(ImmutableList.of(signedFraction(-0.1), signedFraction(-0.8), signedFraction( 0.3))));
  }

  @Test
  public void subtract() {
    assertEquals(SIGNED_FRACTION_0, SIGNED_FRACTION_0.subtract(SIGNED_FRACTION_0));
    assertEquals(signedFraction(0.12345), signedFraction(0.12345).subtract(SIGNED_FRACTION_0));
    assertEquals(SIGNED_FRACTION_1, SIGNED_FRACTION_1.subtract(SIGNED_FRACTION_0));
    assertEquals(signedFraction(0.7), signedFraction(0.9).subtract(signedFraction(0.2)));
    assertEquals(signedFraction(0.2), signedFraction(0.9).subtract(signedFraction(0.7)));
    assertEquals(signedFraction(0.3), SIGNED_FRACTION_1.subtract(signedFraction(0.7)));
    assertEquals(signedFraction(-0.01), signedFraction(0.3).subtract(signedFraction(0.31)));
    assertEquals(signedFraction(-0.01), SIGNED_FRACTION_0.subtract(signedFraction(0.01)));
    assertEquals(signedFraction(-0.01), signedFraction(0.99).subtract(SIGNED_FRACTION_1));
  }

  @Test
  public void multipyDouble() {
    assertEquals(SIGNED_FRACTION_0, SIGNED_FRACTION_0.multiply(0));
    assertEquals(SIGNED_FRACTION_0, SIGNED_FRACTION_0.multiply(0.12345));
    assertEquals(SIGNED_FRACTION_0, signedFraction(0.12345).multiply(0));
    assertEquals(SIGNED_FRACTION_0, SIGNED_FRACTION_0.multiply(1));
    assertEquals(SIGNED_FRACTION_0, SIGNED_FRACTION_1.multiply(0));
    assertEquals(signedFraction(0.05), signedFraction(0.1).multiply(0.5));
    assertEquals(signedFraction(0.05), signedFraction(0.5).multiply(0.1));
    assertEquals(signedFraction(0.3),  signedFraction(0.3).multiply(1));
    assertEquals(signedFraction(0.3),  SIGNED_FRACTION_1.multiply(0.3));
    assertEquals(SIGNED_FRACTION_1, SIGNED_FRACTION_1.multiply(SIGNED_FRACTION_1));
    assertEquals(signedFraction( 6), signedFraction( 2).multiply( 3));
    assertEquals(signedFraction(-6), signedFraction( 2).multiply(-3));
    assertEquals(signedFraction(-6), signedFraction(-2).multiply( 3));
    assertEquals(signedFraction( 6), signedFraction(-2).multiply(-3));
  }

  @Test
  public void multiply() {
    assertEquals(SIGNED_FRACTION_0, SIGNED_FRACTION_0.multiply(SIGNED_FRACTION_0));
    assertEquals(SIGNED_FRACTION_0, SIGNED_FRACTION_0.multiply(signedFraction(0.12345)));
    assertEquals(SIGNED_FRACTION_0, signedFraction(0.12345).multiply(SIGNED_FRACTION_0));
    assertEquals(SIGNED_FRACTION_0, SIGNED_FRACTION_0.multiply(SIGNED_FRACTION_1));
    assertEquals(SIGNED_FRACTION_0, SIGNED_FRACTION_1.multiply(SIGNED_FRACTION_0));
    assertEquals(signedFraction(0.05), signedFraction(0.1).multiply(signedFraction(0.5)));
    assertEquals(signedFraction(0.05), signedFraction(0.5).multiply(signedFraction(0.1)));
    assertEquals(signedFraction(0.3),  signedFraction(0.3).multiply(SIGNED_FRACTION_1));
    assertEquals(signedFraction(0.3),  SIGNED_FRACTION_1.multiply(signedFraction(0.3)));
    assertEquals(SIGNED_FRACTION_1, SIGNED_FRACTION_1.multiply(SIGNED_FRACTION_1));
    assertEquals(signedFraction( 6), signedFraction( 2).multiply(signedFraction( 3)));
    assertEquals(signedFraction(-6), signedFraction( 2).multiply(signedFraction(-3)));
    assertEquals(signedFraction(-6), signedFraction(-2).multiply(signedFraction( 3)));
    assertEquals(signedFraction( 6), signedFraction(-2).multiply(signedFraction(-3)));
  }

  @Test
  public void divide() {
    assertEquals(SIGNED_FRACTION_0, SIGNED_FRACTION_0.divide(signedFraction(0.12345)));
    assertEquals(SIGNED_FRACTION_0, SIGNED_FRACTION_0.divide(SIGNED_FRACTION_1));
    assertEquals(SIGNED_FRACTION_1, SIGNED_FRACTION_1.divide(SIGNED_FRACTION_1));
    assertEquals(SIGNED_FRACTION_1, signedFraction(0.12345).divide(signedFraction(0.12345)));
    assertEquals(signedFraction(0.4), signedFraction(0.2).divide(signedFraction(0.5)));
    assertEquals(signedFraction(2), signedFraction(0.8).divide(signedFraction(0.4)));
    assertEquals(signedFraction(-2), signedFraction(0.8).divide(signedFraction(-0.4)));
    assertEquals(signedFraction(-2), signedFraction(-0.8).divide(signedFraction(0.4)));
    assertEquals(signedFraction(2), signedFraction(-0.8).divide(signedFraction(-0.4)));
    assertIllegalArgumentException( () -> SIGNED_FRACTION_0.divide(SIGNED_FRACTION_0));
    assertIllegalArgumentException( () -> signedFraction(0.12345).divide(SIGNED_FRACTION_0));
    assertIllegalArgumentException( () -> signedFraction(-0.12345).divide(SIGNED_FRACTION_0));
    assertIllegalArgumentException( () -> SIGNED_FRACTION_1.divide(SIGNED_FRACTION_0));
  }

  @Test
  public void testMin() {
    assertEquals(SIGNED_FRACTION_0, SignedFraction.min(SIGNED_FRACTION_0, SIGNED_FRACTION_0));
    assertEquals(SIGNED_FRACTION_0, SignedFraction.min(SIGNED_FRACTION_0, SIGNED_FRACTION_1));
    assertEquals(SIGNED_FRACTION_0, SignedFraction.min(SIGNED_FRACTION_1, SIGNED_FRACTION_0));
    assertEquals(SIGNED_FRACTION_1, SignedFraction.min(SIGNED_FRACTION_1, SIGNED_FRACTION_1));

    assertEquals(mediumNeg, SignedFraction.min(mediumNeg, smallPos));
    assertEquals(mediumNeg, SignedFraction.min(mediumNeg, mediumNeg));
    assertEquals(mediumNeg, SignedFraction.min(mediumNeg, largeNeg));
  }

  @Test
  public void testMax() {
    assertEquals(SIGNED_FRACTION_0, SignedFraction.max(SIGNED_FRACTION_0, SIGNED_FRACTION_0));
    assertEquals(SIGNED_FRACTION_1, SignedFraction.max(SIGNED_FRACTION_0, SIGNED_FRACTION_1));
    assertEquals(SIGNED_FRACTION_1, SignedFraction.max(SIGNED_FRACTION_1, SIGNED_FRACTION_0));
    assertEquals(SIGNED_FRACTION_1, SignedFraction.max(SIGNED_FRACTION_1, SIGNED_FRACTION_1));

    assertEquals(mediumPos, SignedFraction.max(mediumPos, smallPos));
    assertEquals(mediumPos, SignedFraction.max(mediumPos, mediumPos));
    assertEquals(mediumPos, SignedFraction.max(mediumPos, largeNeg));
  }

}
