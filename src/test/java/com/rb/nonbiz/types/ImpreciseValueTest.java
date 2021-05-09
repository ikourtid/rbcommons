package com.rb.nonbiz.types;

import com.google.common.collect.ImmutableList;
import com.rb.biz.investing.strategy.optbased.rebal.lp.RawObjectiveValue;
import org.junit.Test;

import java.util.List;

import static com.rb.biz.investing.strategy.optbased.rebal.lp.RawObjectiveValue.rawNaiveSubObjectiveValue;
import static com.rb.biz.investing.strategy.optbased.rebal.lp.RawObjectiveValue.rawObjectiveValue;
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
    List<RawObjectiveValue> empty = emptyList();
    assertEquals(0, sumToDouble(empty), 0);
    assertEquals(0, sumToDouble(empty.iterator()), 0);

    List<RawObjectiveValue> ten = singletonList(rawObjectiveValue(10));
    assertEquals(10, sumToDouble(ten), 1e-12);
    assertEquals(10, sumToDouble(ten.iterator()), 1e-12);

    List<RawObjectiveValue> fifty = ImmutableList.of(rawObjectiveValue(20), rawObjectiveValue(30));
    assertEquals(50, sumToDouble(fifty), 1e-8);
    assertEquals(50, sumToDouble(fifty.iterator()), 1e-8);
  }

  @Test
  public void testSignsAreOpposite() {
    // Using RawObjectiveValue instead of PreciseValue, which is not instantiable
    assertTrue(signsAreOpposite(rawObjectiveValue(-10), rawObjectiveValue(20)));
    assertFalse(signsAreOpposite(rawObjectiveValue(-10), rawObjectiveValue(0)));
    assertFalse(signsAreOpposite(rawObjectiveValue(-10), rawObjectiveValue(-20)));

    assertFalse(signsAreOpposite(rawObjectiveValue(0), rawObjectiveValue(20)));
    assertFalse(signsAreOpposite(rawObjectiveValue(0), rawObjectiveValue(0)));
    assertFalse(signsAreOpposite(rawObjectiveValue(0), rawObjectiveValue(-20)));

    assertFalse(signsAreOpposite(rawObjectiveValue(10), rawObjectiveValue(20)));
    assertFalse(signsAreOpposite(rawObjectiveValue(10), rawObjectiveValue(0)));
    assertTrue(signsAreOpposite(rawObjectiveValue(10), rawObjectiveValue(-20)));
  }

  @Test
  public void testPreciseValuesAlmostEqual() {
    assertTrue(rawObjectiveValue(10.0).almostEquals(rawObjectiveValue(10.0), 1e-8));

    assertTrue(rawObjectiveValue(10.0).almostEquals(rawObjectiveValue(10.0 + 1e-9), 1e-8));
    assertTrue(rawObjectiveValue(10.0).almostEquals(rawObjectiveValue(10.0 - 1e-9), 1e-8));
    assertTrue(rawObjectiveValue(10.0 + 1e-9).almostEquals(rawObjectiveValue(10.0), 1e-8));
    assertTrue(rawObjectiveValue(10.0 - 1e-9).almostEquals(rawObjectiveValue(10.0), 1e-8));

    assertFalse(rawObjectiveValue(10.0).almostEquals(rawObjectiveValue(10.0 + 1e-8), 1e-9));
    assertFalse(rawObjectiveValue(10.0).almostEquals(rawObjectiveValue(10.0 - 1e-8), 1e-9));
    assertFalse(rawObjectiveValue(10.0 + 1e-8).almostEquals(rawObjectiveValue(10.0), 1e-9));
    assertFalse(rawObjectiveValue(10.0 - 1e-8).almostEquals(rawObjectiveValue(10.0), 1e-9));
  }

  @Test
  public void testIsAlmostZero() {
    assertFalse(rawObjectiveValue(-1e-7).isAlmostZero(1e-8));
    assertTrue(rawObjectiveValue(-1e-9).isAlmostZero(1e-8));
    assertTrue(rawObjectiveValue(0).isAlmostZero(1e-8));
    assertTrue(rawObjectiveValue(1e-9).isAlmostZero(1e-8));
    assertFalse(rawObjectiveValue(1e-7).isAlmostZero(1e-8));
  }

  @Test
  public void testIsLessThan() {
    assertTrue(rawObjectiveValue(1.0).isLessThan(rawObjectiveValue(2.0)));
    assertFalse(rawObjectiveValue(2.0).isLessThan(rawObjectiveValue(1.0)));
  }

  @Test
  public void testIsLessThanOrEqualTo() {
    assertTrue(rawObjectiveValue(1.0).isLessThanOrEqualTo(rawObjectiveValue(1.0)));
    assertTrue(rawObjectiveValue(1.0).isLessThanOrEqualTo(rawObjectiveValue(2.0)));
    assertFalse(rawObjectiveValue(2.0).isLessThanOrEqualTo(rawObjectiveValue(1.0)));
  }

  @Test
  public void testIsGreaterThan() {
    assertFalse(rawObjectiveValue(1.0).isGreaterThan(rawObjectiveValue(2.0)));
    assertTrue(rawObjectiveValue(2.0).isGreaterThan(rawObjectiveValue(1.0)));
  }

  @Test
  public void testIsGreaterThanOrEqualTo() {
    assertTrue(rawObjectiveValue(1.0).isGreaterThanOrEqualTo(rawObjectiveValue(1.0)));
    assertFalse(rawObjectiveValue(1.0).isGreaterThanOrEqualTo(rawObjectiveValue(2.0)));
    assertTrue(rawObjectiveValue(2.0).isGreaterThanOrEqualTo(rawObjectiveValue(1.0)));
  }

  @Test
  public void testMin() {
    assertAlmostEquals(
        rawNaiveSubObjectiveValue(10.0), 
        ImpreciseValue.min(rawNaiveSubObjectiveValue(10.0), rawNaiveSubObjectiveValue(20.0)), 
        1e-8);
    assertAlmostEquals(
        rawNaiveSubObjectiveValue(10.0),
        ImpreciseValue.min(rawNaiveSubObjectiveValue(20.0), rawNaiveSubObjectiveValue(10.0)), 
        1e-8);
  }

  @Test
  public void testMax() {
    assertAlmostEquals(
        rawNaiveSubObjectiveValue(20.0),
        ImpreciseValue.max(rawNaiveSubObjectiveValue(10.0), rawNaiveSubObjectiveValue(20.0)),
        1e-8);
    assertAlmostEquals(
        rawNaiveSubObjectiveValue(20.0),
        ImpreciseValue.max(rawNaiveSubObjectiveValue(20.0), rawNaiveSubObjectiveValue(10.0)),
        1e-8);
  }

  @Test
  public void testToDoubleList() {
    assertEquals(
        ImmutableList.of(-123.456, 0.0, 1.0, 10.0, 123.456),
        asDoubleList(ImmutableList.of(
            rawNaiveSubObjectiveValue(-123.456),
            rawNaiveSubObjectiveValue(0),
            rawNaiveSubObjectiveValue(1),
            rawNaiveSubObjectiveValue(10),
            rawNaiveSubObjectiveValue(123.456))));
  }

}
