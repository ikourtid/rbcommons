package com.rb.nonbiz.types;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.math.stats.ZScore;
import org.junit.Test;

import java.util.List;

import static com.rb.nonbiz.math.stats.ZScore.zScore;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.types.ImpreciseValue.asDoubleList;
import static com.rb.nonbiz.types.ImpreciseValue.signsAreOpposite;
import static com.rb.nonbiz.types.ImpreciseValue.sumToDouble;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ImpreciseValueTest {

  @Test
  public void testSumToDouble() {
    List<ZScore> empty = emptyList();
    assertEquals(0, sumToDouble(empty), 0);
    assertEquals(0, sumToDouble(empty.iterator()), 0);

    List<ZScore> ten = singletonList(zScore(10));
    assertEquals(10, sumToDouble(ten), 1e-12);
    assertEquals(10, sumToDouble(ten.iterator()), 1e-12);

    // ZScores aren't additive logically, but other ImpreciseValue subclasses are -
    // it's just that there's no such example in rbcommons. This is here just to test sumToDouble.
    List<ZScore> fifty = ImmutableList.of(zScore(20), zScore(30));
    assertEquals(50, sumToDouble(fifty), 1e-8);
    assertEquals(50, sumToDouble(fifty.iterator()), 1e-8);
  }

  @Test
  public void testSignsAreOpposite() {
    // Using zScore instead of PreciseValue, which is not instantiable
    assertTrue(signsAreOpposite(zScore(-10), zScore(20)));
    assertFalse(signsAreOpposite(zScore(-10), zScore(0)));
    assertFalse(signsAreOpposite(zScore(-10), zScore(-20)));

    assertFalse(signsAreOpposite(zScore(0), zScore(20)));
    assertFalse(signsAreOpposite(zScore(0), zScore(0)));
    assertFalse(signsAreOpposite(zScore(0), zScore(-20)));

    assertFalse(signsAreOpposite(zScore(10), zScore(20)));
    assertFalse(signsAreOpposite(zScore(10), zScore(0)));
    assertTrue(signsAreOpposite(zScore(10), zScore(-20)));
  }

  @Test
  public void testPreciseValuesAlmostEqual() {
    assertTrue(zScore(10.0).almostEquals(zScore(10.0), 1e-8));

    assertTrue(zScore(10.0).almostEquals(zScore(10.0 + 1e-9), 1e-8));
    assertTrue(zScore(10.0).almostEquals(zScore(10.0 - 1e-9), 1e-8));
    assertTrue(zScore(10.0 + 1e-9).almostEquals(zScore(10.0), 1e-8));
    assertTrue(zScore(10.0 - 1e-9).almostEquals(zScore(10.0), 1e-8));

    assertFalse(zScore(10.0).almostEquals(zScore(10.0 + 1e-8), 1e-9));
    assertFalse(zScore(10.0).almostEquals(zScore(10.0 - 1e-8), 1e-9));
    assertFalse(zScore(10.0 + 1e-8).almostEquals(zScore(10.0), 1e-9));
    assertFalse(zScore(10.0 - 1e-8).almostEquals(zScore(10.0), 1e-9));
  }

  @Test
  public void testIsAlmostZero() {
    assertFalse(zScore(-1e-7).isAlmostZero(1e-8));
    assertTrue(zScore(-1e-9).isAlmostZero(1e-8));
    assertTrue(zScore(0).isAlmostZero(1e-8));
    assertTrue(zScore(1e-9).isAlmostZero(1e-8));
    assertFalse(zScore(1e-7).isAlmostZero(1e-8));
  }

  @Test
  public void testIsLessThan() {
    assertTrue(zScore(1.0).isLessThan(zScore(2.0)));
    assertFalse(zScore(2.0).isLessThan(zScore(1.0)));
  }

  @Test
  public void testIsLessThanOrEqualTo() {
    assertTrue(zScore(1.0).isLessThanOrEqualTo(zScore(1.0)));
    assertTrue(zScore(1.0).isLessThanOrEqualTo(zScore(2.0)));
    assertFalse(zScore(2.0).isLessThanOrEqualTo(zScore(1.0)));
  }

  @Test
  public void testIsGreaterThan() {
    assertFalse(zScore(1.0).isGreaterThan(zScore(2.0)));
    assertTrue(zScore(2.0).isGreaterThan(zScore(1.0)));
  }

  @Test
  public void testIsGreaterThanOrEqualTo() {
    assertTrue(zScore(1.0).isGreaterThanOrEqualTo(zScore(1.0)));
    assertFalse(zScore(1.0).isGreaterThanOrEqualTo(zScore(2.0)));
    assertTrue(zScore(2.0).isGreaterThanOrEqualTo(zScore(1.0)));
  }

  @Test
  public void testMin() {
    assertAlmostEquals(
        zScore(10.0), 
        ImpreciseValue.min(zScore(10.0), zScore(20.0)), 
        1e-8);
    assertAlmostEquals(
        zScore(10.0),
        ImpreciseValue.min(zScore(20.0), zScore(10.0)), 
        1e-8);
  }

  @Test
  public void testMax() {
    assertAlmostEquals(
        zScore(20.0),
        ImpreciseValue.max(zScore(10.0), zScore(20.0)),
        1e-8);
    assertAlmostEquals(
        zScore(20.0),
        ImpreciseValue.max(zScore(20.0), zScore(10.0)),
        1e-8);
  }

  @Test
  public void testToDoubleList() {
    assertEquals(
        ImmutableList.of(-123.456, 0.0, 1.0, 10.0, 123.456),
        asDoubleList(ImmutableList.of(
            zScore(-123.456),
            zScore(0),
            zScore(1),
            zScore(10),
            zScore(123.456))));
  }

}
