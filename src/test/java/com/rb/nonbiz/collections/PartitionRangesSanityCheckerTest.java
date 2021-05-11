package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.types.ClosedUnitFractionRange.unitFractionAtLeast;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unitFractionAtMost;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PartitionRangesSanityCheckerTest extends RBTest<PartitionRangesSanityChecker> {

  @Test
  public void sumOfUpperBoundsSignificantlyBelow1_rangesAreInvalid() {
    assertFalse(makeTestObject().rangesAreValid(
        ImmutableList.of(
            unitFractionAtMost(unitFraction(0.29)),
            unitFractionAtMost(unitFraction(0.7)))));
  }

  @Test
  public void sumOfUpperBoundsNotSignificantlyBelow1_doesNotThrow() {
    assertTrue(makeTestObject().rangesAreValid(ImmutableList.of( 
        unitFractionAtMost(unitFraction(0.3 - 1e-9)),
        unitFractionAtMost(unitFraction(0.7)))));
    assertTrue(makeTestObject().rangesAreValid(ImmutableList.of( 
        unitFractionAtMost(unitFraction(0.3)),
        unitFractionAtMost(unitFraction(0.7)))));
    assertTrue(makeTestObject().rangesAreValid(ImmutableList.of( 
        unitFractionAtMost(unitFraction(0.35)),
        unitFractionAtMost(unitFraction(0.75)))));
  }

  @Test
  public void sumOfLowerBoundsSignificantlyOver1_rangesAreInvalid() {
    assertFalse(makeTestObject().rangesAreValid(ImmutableList.of(
        unitFractionAtLeast(unitFraction(0.31)),
        unitFractionAtLeast(unitFraction(0.7)))));
  }

  @Test
  public void sumOfLowerBoundsNotSignificantlyOver1_rangesAreValid() {
    assertTrue(makeTestObject().rangesAreValid(ImmutableList.of(
        unitFractionAtLeast(unitFraction(0.3 + 1e-9)),
        unitFractionAtLeast(unitFraction(0.7)))));
    assertTrue(makeTestObject().rangesAreValid(ImmutableList.of(
        unitFractionAtLeast(unitFraction(0.3)),
        unitFractionAtLeast(unitFraction(0.7)))));
    assertTrue(makeTestObject().rangesAreValid(ImmutableList.of( 
        unitFractionAtLeast(unitFraction(0.25)),
        unitFractionAtLeast(unitFraction(0.65)))));
  }

  @Override
  protected PartitionRangesSanityChecker makeTestObject() {
    return new PartitionRangesSanityChecker();
  }

}
