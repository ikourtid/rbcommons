package com.rb.nonbiz.types;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.functional.TriConsumer;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertThrows;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_ONE_HALF;
import static com.rb.nonbiz.types.UnitFraction.forgivingUnitFraction;
import static com.rb.nonbiz.types.UnitFraction.sumToAlmostOne;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInBps;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInPct;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnitFractionTest {

  UnitFraction small  = unitFraction(0.6);
  UnitFraction medium = unitFraction(0.7);
  UnitFraction large  = unitFraction(0.8);

  @Test
  public void testEquality() {
    assertEquals(unitFraction(0.6), unitFraction(0.6));
    assertEquals(unitFraction(0.0), UNIT_FRACTION_0);
    assertEquals(unitFraction(0), UNIT_FRACTION_0);
    assertEquals(unitFraction(1.0), UNIT_FRACTION_1);
    assertEquals(unitFraction(1), UNIT_FRACTION_1);
    assertEquals(unitFraction(0.5), UNIT_FRACTION_ONE_HALF);
  }

  @Test
  public void isZeroOrAlmostZero() {
    assertTrue(unitFraction(0.0).isZero());
    assertTrue(unitFraction(0).isZero());
    assertTrue(UNIT_FRACTION_0.isZero());
    assertFalse(unitFraction(0.12345).isZero());

    assertTrue(unitFraction(0.0).isAlmostZero(DEFAULT_EPSILON_1e_8));
    assertTrue(unitFraction(0).isAlmostZero(DEFAULT_EPSILON_1e_8));
    assertTrue(UNIT_FRACTION_0.isAlmostZero(DEFAULT_EPSILON_1e_8));
    assertFalse(unitFraction(0.12345).isAlmostZero(DEFAULT_EPSILON_1e_8));

    assertTrue(unitFraction(0.12345).isAlmostZero(epsilon(0.2)));
  }

  @Test
  public void isOneOrAlmostOne() {
    assertTrue(unitFraction(1.0).isOne());
    assertTrue(unitFraction(1).isOne());
    assertTrue(UNIT_FRACTION_1.isOne());
    assertFalse(unitFraction(0.12345).isOne());

    assertTrue(unitFraction(1.0).isAlmostOne(DEFAULT_EPSILON_1e_8));
    assertTrue(unitFraction(1).isAlmostOne(DEFAULT_EPSILON_1e_8));
    assertTrue(UNIT_FRACTION_1.isAlmostOne(DEFAULT_EPSILON_1e_8));
    assertFalse(unitFraction(0.12345).isAlmostOne(DEFAULT_EPSILON_1e_8));

    assertTrue(unitFraction(0.999999).isAlmostOne(epsilon(0.01)));
  }

  @Test
  public void isAlmostExtreme() {
    for (UnitFraction almostExtreme : ImmutableList.of(
        unitFraction(0.0),
        unitFraction(0),
        UNIT_FRACTION_0,
        unitFraction(1e-9),

        unitFraction(1.0),
        unitFraction(1),
        UNIT_FRACTION_1,
        unitFraction(1 - 1e-9))) {
      assertTrue(almostExtreme.isAlmostExtreme(DEFAULT_EPSILON_1e_8));
    }
    for (UnitFraction notExtreme : ImmutableList.of(
        unitFraction(1e-7),
        unitFraction(0.1),
        unitFraction(0.5),
        unitFraction(0.9),
        unitFraction(1 - 1e-7))) {
      assertFalse(notExtreme.isAlmostExtreme(DEFAULT_EPSILON_1e_8));
    }
  }

  @Test
  public void complement() {
    assertEquals(UNIT_FRACTION_0, UNIT_FRACTION_1.complement());
    assertEquals(UNIT_FRACTION_1, UNIT_FRACTION_0.complement());
    assertEquals(UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_ONE_HALF.complement());
    assertEquals(unitFraction(0.1), unitFraction(0.9).complement());
  }

  @Test
  public void isLessThan() {
    assertTrue(!small.isLessThan(small));
    assertTrue(small.isLessThan(medium));
    assertTrue(small.isLessThan(large));

    assertTrue(!medium.isLessThan(small));
    assertTrue(!medium.isLessThan(medium));
    assertTrue(medium.isLessThan(large));

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
    assertTrue(medium.isLessThanOrEqualTo(medium));
    assertTrue(medium.isLessThanOrEqualTo(large));

    assertTrue(!large.isLessThanOrEqualTo(small));
    assertTrue(!large.isLessThanOrEqualTo(medium));
    assertTrue(large.isLessThanOrEqualTo(large));
  }

  @Test
  public void isGreaterThan() {
    assertTrue(!small.isGreaterThan(small));
    assertTrue(!small.isGreaterThan(medium));
    assertTrue(!small.isGreaterThan(large));

    assertTrue(medium.isGreaterThan(small));
    assertTrue(!medium.isGreaterThan(medium));
    assertTrue(!medium.isGreaterThan(large));

    assertTrue(large.isGreaterThan(small));
    assertTrue(large.isGreaterThan(medium));
    assertTrue(!large.isGreaterThan(large));
  }

  @Test
  public void isGreaterThanOrEqualTo() {
    assertTrue(small.isGreaterThanOrEqualTo(small));
    assertTrue(!small.isGreaterThanOrEqualTo(medium));
    assertTrue(!small.isGreaterThanOrEqualTo(large));

    assertTrue(medium.isGreaterThanOrEqualTo(small));
    assertTrue(medium.isGreaterThanOrEqualTo(medium));
    assertTrue(!medium.isGreaterThanOrEqualTo(large));

    assertTrue(large.isGreaterThanOrEqualTo(small));
    assertTrue(large.isGreaterThanOrEqualTo(medium));
    assertTrue(large.isGreaterThanOrEqualTo(large));
  }

  @Test
  public void testUnitFractionInPct() {
    assertEquals(UNIT_FRACTION_0,         unitFractionInPct(0));
    assertEquals(unitFraction(0.1234),    unitFractionInPct(12.34));
    assertEquals(unitFraction(0.9876543), unitFractionInPct(98.76543));
    assertEquals(UNIT_FRACTION_1,         unitFractionInPct(100));
  }

  @Test
  public void testUnitFractionInBps() {
    assertEquals(UNIT_FRACTION_0,         unitFractionInBps(0));
    assertEquals(unitFraction(0.1234),    unitFractionInBps(1_234.0));
    assertEquals(unitFraction(0.9876543), unitFractionInBps(9_876.543));
    assertEquals(UNIT_FRACTION_1,         unitFractionInBps(10_000));
  }

  @Test
  public void toPercent() {
    assertEquals("12.34 %", unitFraction(0.1234).toPercentString());
    assertEquals("100 %", UNIT_FRACTION_1.toPercentString());
    assertEquals("20 %", unitFraction(0.2).toPercentString());
    assertEquals("0 %", UNIT_FRACTION_0.toPercentString());
    assertEquals("33.33 %", unitFraction(1, 3).toPercentString());
    assertEquals("33.33 %", unitFraction(1 / 3.0).toPercentString());

    // test digits of precision and without the "%" suffix
    assertEquals("12.346", unitFraction(0.123456).toPercentString(3, false));
    assertEquals("100", UNIT_FRACTION_1.toPercentString(1, false));
    assertEquals("20", unitFraction(0.2).toPercentString(4, false));
    assertEquals("0", UNIT_FRACTION_0.toPercentString(2, false));
    assertEquals("33.3", unitFraction(1 / 3.0).toPercentString(1, false));
    assertEquals("66.666667", unitFraction(2, 3).toPercentString(6, false));
  }

  @Test
  public void testFromString() {
    assertEquals(UNIT_FRACTION_0,        unitFraction("0"));
    assertEquals(unitFraction(0.123),    unitFraction("0.123"));
    assertEquals(unitFraction(0.987654), unitFraction("0.987654"));
    assertEquals(UNIT_FRACTION_1,        unitFraction("1"));
  }

  @Test
  public void toBasisPoints() {
    // default: print up to 2 digits and "bps" suffix
    assertEquals("1234 bps",    unitFraction(0.1234).toBasisPoints());
    assertEquals("10000 bps",   UNIT_FRACTION_1.toBasisPoints());
    assertEquals("3333.33 bps", unitFraction(1, 3).toBasisPoints());
    assertEquals("6666.67 bps", unitFraction(2, 3).toBasisPoints());

    assertEquals("1234 bps",   unitFraction(0.1234).toBasisPoints(1));
    assertEquals("10000 bps",  UNIT_FRACTION_1.toBasisPoints(1));
    assertEquals("2000 bps",   unitFraction(0.2).toBasisPoints(1));
    assertEquals("0 bps",      UNIT_FRACTION_0.toBasisPoints(1));
    assertEquals("3333.3 bps", unitFraction(1, 3).toBasisPoints(1));
    assertEquals("6666.7 bps", unitFraction(2, 3).toBasisPoints(1));
    assertEquals("3333.3 bps", unitFraction(1 / 3.0).toBasisPoints(1));

    // vary number the max of digits to print; do not print "bps" suffix
    assertEquals("1234",     unitFraction(0.1234).toBasisPoints(2, false));
    assertEquals("10000",    UNIT_FRACTION_1.toBasisPoints(0, false));
    assertEquals("2000",     unitFraction(0.2).toBasisPoints(1, false));
    assertEquals("0",        UNIT_FRACTION_0.toBasisPoints(1, false));
    assertEquals("3333.3",   unitFraction(1, 3).toBasisPoints(1, false));
    assertEquals("6666.7",   unitFraction(2, 3).toBasisPoints(1, false));
    assertEquals("6666.667", unitFraction(2 / 3.0).toBasisPoints(3, false));
  }

  @Test
  public void constructorWithNumeratorAndDenominator() {
    for (Runnable r : new Runnable[] {
        () -> unitFraction(0, 0),
        () -> unitFraction(1, 0),
        () -> unitFraction(-1, 1),
        () -> unitFraction(-1, -1),
        () -> unitFraction(1, -1),
        () -> unitFraction(3, 2) }) {
      assertThrows(IllegalArgumentException.class, r);
    }
    assertEquals(UNIT_FRACTION_0, unitFraction(0, 1));
    assertEquals(UNIT_FRACTION_0, unitFraction(0, 123));
    assertEquals(UNIT_FRACTION_1, unitFraction(1, 1));
    assertEquals(UNIT_FRACTION_1, unitFraction(123, 123));
    assertEquals(UNIT_FRACTION_ONE_HALF, unitFraction(1, 2));
    assertEquals(UNIT_FRACTION_ONE_HALF, unitFraction(123, 246));
    assertEquals(unitFraction(0.6), unitFraction(3, 5));
    assertEquals(unitFraction(0.6), unitFraction(6, 10));
    assertEquals(unitFraction(0.6), unitFraction(60, 100));
    assertEquals(1 / 3.0, unitFraction(1, 3).doubleValue(), 1e-8);
  }

  @Test
  public void add() {
    assertEquals(UNIT_FRACTION_0, UNIT_FRACTION_0.add(UNIT_FRACTION_0));
    assertEquals(UNIT_FRACTION_1, UNIT_FRACTION_0.add(UNIT_FRACTION_1));
    assertEquals(UNIT_FRACTION_1, UNIT_FRACTION_ONE_HALF.add(UNIT_FRACTION_ONE_HALF));
    assertEquals(UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_0.add(UNIT_FRACTION_ONE_HALF));
    assertEquals(unitFraction(0.12345), UNIT_FRACTION_0.add(unitFraction(0.12345)));
    assertEquals(unitFraction(0.12345), unitFraction(0.12345).add(UNIT_FRACTION_0));
    assertEquals(unitFraction(0.9), unitFraction(0.3).add(unitFraction(0.6)));
    assertEquals(unitFraction(0.9), unitFraction(0.6).add(unitFraction(0.3)));
    assertEquals(UNIT_FRACTION_1, unitFraction(0.3).add(unitFraction(0.7)));
    assertEquals(UNIT_FRACTION_1, unitFraction(0.7).add(unitFraction(0.3)));
    assertEquals(UNIT_FRACTION_1,
        unitFraction(new BigDecimal("0.7")).add(
            unitFraction(new BigDecimal("0.300000000000000067"))));
    assertIllegalArgumentException( () -> unitFraction(0.3).add(unitFraction(0.71)));
    assertIllegalArgumentException( () -> UNIT_FRACTION_1.add(unitFraction(0.01)));
  }

  @Test
  public void addDouble() {
    assertEquals(UNIT_FRACTION_0, UNIT_FRACTION_0.add(0));
    assertEquals(UNIT_FRACTION_1, UNIT_FRACTION_0.add(1));
    assertEquals(UNIT_FRACTION_1, UNIT_FRACTION_ONE_HALF.add(0.5));
    assertEquals(UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_0.add(0.5));

    assertAlmostEquals(unitFraction(0.9), unitFraction(0.3).add(0.6), DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(unitFraction(0.9), unitFraction(0.6).add(0.3), DEFAULT_EPSILON_1e_8);
  }

  @Test
  public void sum() {
    assertEquals(UNIT_FRACTION_0, UnitFraction.sum(emptyList()));
    assertEquals(UNIT_FRACTION_0, UnitFraction.sum(Stream.empty()));
    assertAlmostEquals(
        unitFraction(0.7),
        UnitFraction.sum(ImmutableList.of(unitFraction(0.1), unitFraction(0.2), unitFraction(0.4))),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        unitFraction(0.7),
        UnitFraction.sum(Stream.of(unitFraction(0.1), unitFraction(0.2), unitFraction(0.4))),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        unitFraction(0.3),
        UnitFraction.sum(unitFraction(0.1), unitFraction(0.2)),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        unitFraction(0.7),
        UnitFraction.sum(unitFraction(0.1), unitFraction(0.2), unitFraction(0.4)),
        DEFAULT_EPSILON_1e_8);
    assertIllegalArgumentException( () -> UnitFraction.sum(Collections.nCopies(3, unitFraction(0.4))));
    assertIllegalArgumentException( () -> UnitFraction.sum(Collections.nCopies(3, unitFraction(0.4)).stream()));
    assertIllegalArgumentException( () -> UnitFraction.sum(unitFraction(0.4), unitFraction(0.4), unitFraction(0.4)));
  }

  @Test
  public void testSumWithCeilingOf1() {
    assertEquals(UNIT_FRACTION_0, UnitFraction.sumWithCeilingOf1(emptyList()));
    assertEquals(UNIT_FRACTION_0, UnitFraction.sumWithCeilingOf1(Stream.empty()));

    // Normal sum behavior here...
    assertAlmostEquals(
        unitFraction(0.7),
        UnitFraction.sumWithCeilingOf1(ImmutableList.of(unitFraction(0.1), unitFraction(0.2), unitFraction(0.4))),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        unitFraction(0.7),
        UnitFraction.sumWithCeilingOf1(Stream.of(unitFraction(0.1), unitFraction(0.2), unitFraction(0.4))),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        unitFraction(0.3),
        UnitFraction.sumWithCeilingOf1(unitFraction(0.1), unitFraction(0.2)),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        unitFraction(0.7),
        UnitFraction.sumWithCeilingOf1(unitFraction(0.1), unitFraction(0.2), unitFraction(0.4)),
        DEFAULT_EPSILON_1e_8);

    // ... but here the sum exceeds 1, so we just use a ceiling of 1,
    // instead of an exception which we would get if we just used UnitFraction#sum.
    rbSetOf(
        UnitFraction.sumWithCeilingOf1(ImmutableList.of(unitFraction(0.1), unitFraction(0.2), unitFraction(0.7))), // sum = 1
        UnitFraction.sumWithCeilingOf1(ImmutableList.of(unitFraction(0.1), unitFraction(0.2), unitFraction(0.8))), // sum = 1.1
        UnitFraction.sumWithCeilingOf1(Stream.of(unitFraction(0.1), unitFraction(0.2), unitFraction(0.7))), // sum = 1
        UnitFraction.sumWithCeilingOf1(Stream.of(unitFraction(0.1), unitFraction(0.2), unitFraction(0.8))), // sum = 1.1
        UnitFraction.sumWithCeilingOf1(unitFraction(0.1), unitFraction(0.9)), // sum = 1
        UnitFraction.sumWithCeilingOf1(unitFraction(0.2), unitFraction(0.9)), // sum = 1.1
        UnitFraction.sumWithCeilingOf1(unitFraction(0.1), unitFraction(0.2), unitFraction(0.7)), // sum = 1
        UnitFraction.sumWithCeilingOf1(unitFraction(0.1), unitFraction(0.2), unitFraction(0.8))) // sum = 1
    .forEach(sum -> assertEquals(UNIT_FRACTION_1, sum));
  }

  @Test
  public void sumForgiving() {
    assertEquals(UNIT_FRACTION_0, UnitFraction.forgivingSum(emptyList(), DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_0, UnitFraction.forgivingSum(
        ImmutableList.of(UNIT_FRACTION_0),
        DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_1, UnitFraction.forgivingSum(
        ImmutableList.of(UNIT_FRACTION_1),
        DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_1, UnitFraction.forgivingSum(
        ImmutableList.of(UNIT_FRACTION_1, unitFraction(1e-9)),
        DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_1, UnitFraction.forgivingSum(
        ImmutableList.of(unitFraction(0.400), unitFraction(0.601)),
        epsilon(1e-3)));

    assertEquals(UNIT_FRACTION_0, UnitFraction.forgivingSum(Stream.empty(), DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_0, UnitFraction.forgivingSum(Stream.of(UNIT_FRACTION_0), DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_1, UnitFraction.forgivingSum(Stream.of(UNIT_FRACTION_1), DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_1, UnitFraction.forgivingSum(
        Stream.of(UNIT_FRACTION_1, unitFraction(1e-9)),
        DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void subtract() {
    assertEquals(UNIT_FRACTION_0, UNIT_FRACTION_0.subtract(UNIT_FRACTION_0));
    assertEquals(UNIT_FRACTION_0, UNIT_FRACTION_1.subtract(UNIT_FRACTION_1));
    assertEquals(unitFraction(0.12345), unitFraction(0.12345).subtract(UNIT_FRACTION_0));
    assertEquals(UNIT_FRACTION_1, UNIT_FRACTION_1.subtract(UNIT_FRACTION_0));
    assertEquals(UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_1.subtract(UNIT_FRACTION_ONE_HALF));
    assertEquals(UNIT_FRACTION_0, UNIT_FRACTION_ONE_HALF.subtract(UNIT_FRACTION_ONE_HALF));
    assertEquals(unitFraction(0.7), unitFraction(0.9).subtract(unitFraction(0.2)));
    assertEquals(unitFraction(0.2), unitFraction(0.9).subtract(unitFraction(0.7)));
    assertEquals(unitFraction(0.3), UNIT_FRACTION_1.subtract(unitFraction(0.7)));
    assertIllegalArgumentException( () -> unitFraction(0.3).subtract(unitFraction(0.31)));
    assertIllegalArgumentException( () -> UNIT_FRACTION_0.subtract(unitFraction(0.01)));
    assertIllegalArgumentException( () -> unitFraction(0.99).subtract(UNIT_FRACTION_1));
  }

  @Test
  public void multiply() {
    assertEquals(UNIT_FRACTION_0, UNIT_FRACTION_0.multiply(UNIT_FRACTION_0));
    assertEquals(UNIT_FRACTION_0, UNIT_FRACTION_0.multiply(unitFraction(0.12345)));
    assertEquals(UNIT_FRACTION_0, unitFraction(0.12345).multiply(UNIT_FRACTION_0));
    assertEquals(UNIT_FRACTION_0, UNIT_FRACTION_0.multiply(UNIT_FRACTION_1));
    assertEquals(UNIT_FRACTION_0, UNIT_FRACTION_1.multiply(UNIT_FRACTION_0));
    assertEquals(UNIT_FRACTION_0, UNIT_FRACTION_ONE_HALF.multiply(UNIT_FRACTION_0));
    assertEquals(unitFraction(0.05), unitFraction(0.1).multiply(unitFraction(0.5)));
    assertEquals(unitFraction(0.05), unitFraction(0.5).multiply(unitFraction(0.1)));
    assertEquals(unitFraction(0.3), unitFraction(0.3).multiply(UNIT_FRACTION_1));
    assertEquals(unitFraction(0.3), UNIT_FRACTION_1.multiply(unitFraction(0.3)));
    assertEquals(UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_1.multiply(UNIT_FRACTION_ONE_HALF));
    assertEquals(UNIT_FRACTION_1, UNIT_FRACTION_1.multiply(UNIT_FRACTION_1));
  }

  @Test
  public void divide() {
    assertEquals(UNIT_FRACTION_0, UNIT_FRACTION_0.divide(unitFraction(0.12345)));
    assertEquals(UNIT_FRACTION_0, UNIT_FRACTION_0.divide(UNIT_FRACTION_1));
    assertEquals(UNIT_FRACTION_1, UNIT_FRACTION_1.divide(UNIT_FRACTION_1));
    assertEquals(UNIT_FRACTION_1, UNIT_FRACTION_ONE_HALF.divide(UNIT_FRACTION_ONE_HALF));
    assertEquals(UNIT_FRACTION_1, unitFraction(0.12345).divide(unitFraction(0.12345)));
    assertEquals(unitFraction(0.4), unitFraction(0.2).divide(unitFraction(0.5)));
    assertIllegalArgumentException( () -> unitFraction(0.9).divide(unitFraction(0.8)));
    assertIllegalArgumentException( () -> UNIT_FRACTION_0.divide(UNIT_FRACTION_0));
    assertIllegalArgumentException( () -> unitFraction(0.12345).divide(UNIT_FRACTION_0));
    assertIllegalArgumentException( () -> UNIT_FRACTION_1.divide(UNIT_FRACTION_0));
    assertIllegalArgumentException( () -> UNIT_FRACTION_ONE_HALF.divide(UNIT_FRACTION_0));
  }

  @Test
  public void minMax() {
    assertEquals(UNIT_FRACTION_0, UnitFraction.max(UNIT_FRACTION_0, UNIT_FRACTION_0));
    assertEquals(UNIT_FRACTION_1, UnitFraction.max(UNIT_FRACTION_0, UNIT_FRACTION_1));
    assertEquals(UNIT_FRACTION_1, UnitFraction.max(UNIT_FRACTION_1, UNIT_FRACTION_0));
    assertEquals(UNIT_FRACTION_1, UnitFraction.max(UNIT_FRACTION_1, UNIT_FRACTION_1));
    assertEquals(UNIT_FRACTION_ONE_HALF, UnitFraction.max(UNIT_FRACTION_0,        UNIT_FRACTION_ONE_HALF));
    assertEquals(UNIT_FRACTION_ONE_HALF, UnitFraction.max(UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_0));
    assertEquals(UNIT_FRACTION_ONE_HALF, UnitFraction.max(UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_ONE_HALF));
    assertEquals(UNIT_FRACTION_1,        UnitFraction.max(UNIT_FRACTION_1,        UNIT_FRACTION_ONE_HALF));
    assertEquals(UNIT_FRACTION_1,        UnitFraction.max(UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_1));

    assertEquals(UNIT_FRACTION_0, UnitFraction.min(UNIT_FRACTION_0, UNIT_FRACTION_0));
    assertEquals(UNIT_FRACTION_0, UnitFraction.min(UNIT_FRACTION_0, UNIT_FRACTION_1));
    assertEquals(UNIT_FRACTION_0, UnitFraction.min(UNIT_FRACTION_1, UNIT_FRACTION_0));
    assertEquals(UNIT_FRACTION_1, UnitFraction.min(UNIT_FRACTION_1, UNIT_FRACTION_1));
    assertEquals(UNIT_FRACTION_0,        UnitFraction.min(UNIT_FRACTION_0,        UNIT_FRACTION_ONE_HALF));
    assertEquals(UNIT_FRACTION_0,        UnitFraction.min(UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_0));
    assertEquals(UNIT_FRACTION_ONE_HALF, UnitFraction.min(UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_ONE_HALF));
    assertEquals(UNIT_FRACTION_ONE_HALF, UnitFraction.min(UNIT_FRACTION_1,        UNIT_FRACTION_ONE_HALF));
    assertEquals(UNIT_FRACTION_ONE_HALF, UnitFraction.min(UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_1));
  }

  @Test
  public void testForgivingUnitFraction() {
    assertIllegalArgumentException( () -> forgivingUnitFraction(new BigDecimal(-1e-7), DEFAULT_EPSILON_1e_8));
    assertIllegalArgumentException( () -> forgivingUnitFraction(-1e-7, DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_0, forgivingUnitFraction(new BigDecimal(-1e-10), DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_0, forgivingUnitFraction(-1e-10, DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_0, forgivingUnitFraction(BigDecimal.ZERO, DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_0, forgivingUnitFraction(0, DEFAULT_EPSILON_1e_8));
    assertAlmostEquals(unitFraction(1e-10), forgivingUnitFraction(new BigDecimal(1e-10), epsilon(1e-8)), epsilon(1e-12));
    assertEquals(unitFraction(1e-10), forgivingUnitFraction(1e-10, DEFAULT_EPSILON_1e_8));
    assertAlmostEquals(unitFraction(1 - 1e-10), forgivingUnitFraction(new BigDecimal(1 - 1e-10), epsilon(1e-8)), epsilon(1e-12));
    assertEquals(unitFraction(1 - 1e-10), forgivingUnitFraction(1 - 1e-10, DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_1, forgivingUnitFraction(BigDecimal.ONE, DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_1, forgivingUnitFraction(1, DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_1, forgivingUnitFraction(new BigDecimal(1 + 1e-10), DEFAULT_EPSILON_1e_8));
    assertEquals(UNIT_FRACTION_1, forgivingUnitFraction(1 + 1e-10, DEFAULT_EPSILON_1e_8));
    assertIllegalArgumentException( () -> forgivingUnitFraction(new BigDecimal(1 + 1e-7), DEFAULT_EPSILON_1e_8));
    assertIllegalArgumentException( () -> forgivingUnitFraction(1 + 1e-7, DEFAULT_EPSILON_1e_8));

    assertEquals(UNIT_FRACTION_1, forgivingUnitFraction(new BigDecimal("1.000000000000000067"),  epsilon(1e-15)));
    assertEquals(UNIT_FRACTION_0, forgivingUnitFraction(new BigDecimal("-0.000000000000000067"), epsilon(1e-15)));
  }

  @Test
  public void testAdd_mustBeALittleForgiving() {
    UnitFraction a = unitFraction(new BigDecimal("0.4000000000000003")); // 0.4 + 3 * 1e-16
    UnitFraction b = unitFraction(new BigDecimal("0.6000000000000003")); // 0.6 + 3 * 1e-16
    assertEquals(UNIT_FRACTION_1, a.add(b)); // equals, not almost equals, since the sum caps at 1
    assertEquals(UNIT_FRACTION_1, a.addForgiving(b, epsilon(1e-15)));
  }

  @Test
  public void testAdd_overloadThatControlsForgivingness() {
    UnitFraction a = unitFraction(new BigDecimal("0.4002"));
    UnitFraction b = unitFraction(new BigDecimal("0.6002"));
    assertEquals(UNIT_FRACTION_1, a.addForgiving(b, epsilon(1e-3)));
    assertIllegalArgumentException( () -> a.addForgiving(b, epsilon(1e-4)));
  }

  @Test
  public void testSumToAlmostOne() {
    assertTrue(sumToAlmostOne(epsilon(0.00), UNIT_FRACTION_1));
    assertTrue(sumToAlmostOne(epsilon(0.02), UNIT_FRACTION_1));
    assertTrue(sumToAlmostOne(epsilon(0.02), unitFraction(0.99)));
    assertFalse(sumToAlmostOne(epsilon(0.02), unitFraction(0.97)));

    assertTrue(sumToAlmostOne(epsilon(0.00), UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_ONE_HALF));
    assertTrue(sumToAlmostOne(epsilon(0.00), UNIT_FRACTION_1, UNIT_FRACTION_0));
    assertTrue(sumToAlmostOne(epsilon(0.00), unitFraction(0.1), unitFraction(0.9)));
    assertTrue(sumToAlmostOne(epsilon(0.02), UNIT_FRACTION_ONE_HALF, unitFraction(0.51)));
    assertTrue(sumToAlmostOne(epsilon(0.02), unitFraction(0.1), unitFraction(0.9)));
    assertTrue(sumToAlmostOne(epsilon(0.02), unitFraction(0.1), unitFraction(0.89)));
    assertFalse(sumToAlmostOne(epsilon(0.02), unitFraction(0.1), unitFraction(0.87)));

    assertTrue(sumToAlmostOne(epsilon(0.00), UNIT_FRACTION_1, UNIT_FRACTION_0, UNIT_FRACTION_0));
    assertTrue(sumToAlmostOne(epsilon(0.00), UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_ONE_HALF, UNIT_FRACTION_0));
    assertTrue(sumToAlmostOne(epsilon(0.00), unitFraction(0.1), unitFraction(0.4), unitFraction(0.5)));
    assertTrue(sumToAlmostOne(epsilon(0.02), unitFraction(0.1), unitFraction(0.4), unitFraction(0.5)));
    assertTrue(sumToAlmostOne(epsilon(0.02), unitFraction(0.1), unitFraction(0.4), unitFraction(0.49)));
    assertFalse(sumToAlmostOne(epsilon(0.02), unitFraction(0.1), unitFraction(0.4), unitFraction(0.47)));

    // This is not supposed to throw, even though these sum to > 1
    assertFalse(sumToAlmostOne(epsilon(0.00), unitFraction(0.1), unitFraction(0.4), unitFraction(0.51)));
    assertTrue(sumToAlmostOne(epsilon(0.02), unitFraction(0.1), unitFraction(0.4), unitFraction(0.51)));
    assertFalse(sumToAlmostOne(epsilon(0.02), unitFraction(0.1), unitFraction(0.4), unitFraction(0.53)));
  }

  @Test
  public void testToSignedFraction() {
    assertEquals(SIGNED_FRACTION_0,    UNIT_FRACTION_0.toSignedFraction());
    assertEquals(signedFraction(0.25), unitFraction(0.25).toSignedFraction());
    assertEquals(signedFraction(0.99), unitFraction(0.99).toSignedFraction());
    assertEquals(SIGNED_FRACTION_1,    UNIT_FRACTION_1.toSignedFraction());
  }

  @Test
  public void testValueOrSnapToZero() {
    assertEquals(UNIT_FRACTION_0,    UNIT_FRACTION_0.valueOrSnapToZero(epsilon(0.021)));
    assertEquals(UNIT_FRACTION_0,    UNIT_FRACTION_0.valueOrSnapToZero(epsilon(0.019)));

    assertEquals(UNIT_FRACTION_0,    unitFraction(0.02).valueOrSnapToZero(epsilon(0.021)));
    assertEquals(unitFraction(0.02), unitFraction(0.02).valueOrSnapToZero(epsilon(0.019)));

    assertEquals(UNIT_FRACTION_1,    UNIT_FRACTION_1.valueOrSnapToZero(epsilon(0.021)));
    assertEquals(UNIT_FRACTION_1,    UNIT_FRACTION_1.valueOrSnapToZero(epsilon(0.019)));
  }

  @Test
  public void testValueOrSnapToOne() {
    assertEquals(UNIT_FRACTION_1,    UNIT_FRACTION_1.valueOrSnapToOne(epsilon(0.021)));
    assertEquals(UNIT_FRACTION_1,    UNIT_FRACTION_1.valueOrSnapToOne(epsilon(0.019)));

    assertEquals(UNIT_FRACTION_1,    unitFraction(0.98).valueOrSnapToOne(epsilon(0.021)));
    assertEquals(unitFraction(0.98), unitFraction(0.98).valueOrSnapToOne(epsilon(0.019)));

    assertEquals(UNIT_FRACTION_0,    UNIT_FRACTION_0.valueOrSnapToOne(epsilon(0.021)));
    assertEquals(UNIT_FRACTION_0,    UNIT_FRACTION_0.valueOrSnapToOne(epsilon(0.019)));
  }

  @Test
  public void testToPercentString_maxDigits() {
    UnitFraction integerPercent = unitFractionInPct(12.0);
    // zero trailing digits are not printed
    // if the boolean 'includeSign' is omitted, 'true' is used
    assertEquals("12 %", integerPercent.toPercentString(0));
    assertEquals("12 %", integerPercent.toPercentString(1));
    assertEquals("12 %", integerPercent.toPercentString(4));
    assertEquals("12 %", integerPercent.toPercentString(8));

    UnitFraction unitFraction = unitFraction(0.123456789);

    assertEquals("12 %",         unitFraction.toPercentString(0));
    assertEquals("12.3 %",       unitFraction.toPercentString(1));
    assertEquals("12.346 %",     unitFraction.toPercentString(3));
    assertEquals("12.34568 %",   unitFraction.toPercentString(5));
    assertEquals("12.3456789 %", unitFraction.toPercentString(7));
    // asking for more digits does nothing; no more digits available to print
    assertEquals("12.3456789 %", unitFraction.toPercentString(19));

    assertEquals("12",         unitFraction.toPercentString(0, false));
    assertEquals("12.3",       unitFraction.toPercentString(1, false));
    assertEquals("12.346",     unitFraction.toPercentString(3, false));
    assertEquals("12.34568",   unitFraction.toPercentString(5, false));
    assertEquals("12.3456789", unitFraction.toPercentString(7, false));
    // asking for more digits does nothing; no more digits available to print
    assertEquals("12.3456789", unitFraction.toPercentString(19, false));

    // negative precisions are not supported
    assertIllegalArgumentException( () -> unitFraction.toPercentString(-1));
    assertIllegalArgumentException( () -> unitFraction.toPercentString(-1, true));
    assertIllegalArgumentException( () -> unitFraction.toPercentString(-1, false));
  }

  @Test
  public void testToPercentString_minDigits_maxDigits() {
    UnitFraction integerPercent = unitFractionInPct(12.0);
    // if "includeSign" boolean is omitted, "true" is used
    // zero trailing digits are not printed
    assertEquals("12 %",      integerPercent.toPercentString(0, 0));
    assertEquals("12 %",      integerPercent.toPercentString(0, 1));
    assertEquals("12.0 %",    integerPercent.toPercentString(1, 1));
    assertEquals("12.0 %",    integerPercent.toPercentString(1, 8));
    assertEquals("12.0000 %", integerPercent.toPercentString(4, 8));

    UnitFraction nonIntegerPercent = unitFraction(0.123456789);

    assertEquals("12 %",         nonIntegerPercent.toPercentString(0, 0, true));
    assertEquals("12.3 %",       nonIntegerPercent.toPercentString(1, 1, true));
    assertEquals("12.346 %",     nonIntegerPercent.toPercentString(1, 3, true));
    assertEquals("12.34568 %",   nonIntegerPercent.toPercentString(1, 5, true));
    assertEquals("12.3456789 %", nonIntegerPercent.toPercentString(1, 7, true));
    // asking for more digits does nothing; no more digits available to print
    assertEquals("12.3456789 %", nonIntegerPercent.toPercentString(1, 19, true));

    assertEquals("12",         nonIntegerPercent.toPercentString(0, 0, false));
    assertEquals("12.3",       nonIntegerPercent.toPercentString(1, 1, false));
    assertEquals("12.346",     nonIntegerPercent.toPercentString(1, 3, false));
    assertEquals("12.34568",   nonIntegerPercent.toPercentString(1, 5, false));
    assertEquals("12.3456789", nonIntegerPercent.toPercentString(1, 7, false));
    // asking for more digits does nothing; no more digits available to print
    assertEquals("12.3456789", nonIntegerPercent.toPercentString(1, 19, false));

    // invalid minPrecision; can't be negative
    assertIllegalArgumentException( () -> nonIntegerPercent.toPercentString(-1, 0));
    assertIllegalArgumentException( () -> nonIntegerPercent.toPercentString(-1, 0, true));
    assertIllegalArgumentException( () -> nonIntegerPercent.toPercentString(-1, 0, false));
    // minPrecision can't be greater than maxPrecision
    assertIllegalArgumentException( () -> nonIntegerPercent.toPercentString(1, -1));
    assertIllegalArgumentException( () -> nonIntegerPercent.toPercentString(1, -1, true));
    assertIllegalArgumentException( () -> nonIntegerPercent.toPercentString(1,  0, true));
    assertIllegalArgumentException( () -> nonIntegerPercent.toPercentString(2,  1, true));
    assertIllegalArgumentException( () -> nonIntegerPercent.toPercentString(2,  1, false));
  }

  @Test
  public void testSubtractWithFloorOfZero() {
    TriConsumer<UnitFraction, UnitFraction, UnitFraction> asserter = (left, right, result) -> assertAlmostEquals(
        left.subtractWithFloorOfZero(right),
        result,
        DEFAULT_EPSILON_1e_8);

    asserter.accept(unitFractionInPct(10), UNIT_FRACTION_1,          UNIT_FRACTION_0);
    asserter.accept(unitFractionInPct(10), unitFractionInPct(11),    UNIT_FRACTION_0);
    asserter.accept(unitFractionInPct(10), unitFractionInPct(10),    UNIT_FRACTION_0);
    asserter.accept(unitFractionInPct(10), unitFractionInPct(9),     unitFractionInPct(1));

    asserter.accept(UNIT_FRACTION_0,       unitFractionInPct(12.34), UNIT_FRACTION_0);
    asserter.accept(UNIT_FRACTION_0,       UNIT_FRACTION_0,          UNIT_FRACTION_0);

    asserter.accept(UNIT_FRACTION_1,       UNIT_FRACTION_1,          UNIT_FRACTION_0);
    asserter.accept(UNIT_FRACTION_1,       unitFractionInPct(10),    unitFractionInPct(90));
    asserter.accept(UNIT_FRACTION_1,       UNIT_FRACTION_0,          UNIT_FRACTION_1);
  }

}
