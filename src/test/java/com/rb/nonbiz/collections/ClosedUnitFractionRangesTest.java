package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.ClosedUnitFractionRange;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.ClosedUnitFractionRanges.closedUnitFractionRanges;
import static com.rb.nonbiz.collections.ClosedUnitFractionRanges.emptyClosedUnitFractionRanges;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRangeTest.closedUnitFractionRangeMatcher;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

// This test class is not generic, but the publicly exposed static matcher is
public class ClosedUnitFractionRangesTest extends RBTestMatcher<ClosedUnitFractionRanges<String>> {

  public static <T> ClosedUnitFractionRanges<T> singletonClosedUnitFractionRanges(
      T onlyKey, ClosedUnitFractionRange onlyValue) {
    return closedUnitFractionRanges(singletonRBMap(onlyKey, onlyValue));
  }

  @Test
  public void getNearestValueInRange() {
    ClosedUnitFractionRanges<String> closedUnitFractionRanges = closedUnitFractionRanges(rbMapOf(
        "a", closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6)),
        "b", closedUnitFractionRange(unitFraction(0.5), unitFraction(0.5))));
    assertOptionalEquals(unitFraction(0.4),  closedUnitFractionRanges.getNearestValueInRange("a", unitFraction(0.39)));
    assertOptionalEquals(unitFraction(0.4),  closedUnitFractionRanges.getNearestValueInRange("a", unitFraction(0.4)));
    assertOptionalEquals(unitFraction(0.41), closedUnitFractionRanges.getNearestValueInRange("a", unitFraction(0.41)));
    assertOptionalEquals(unitFraction(0.59), closedUnitFractionRanges.getNearestValueInRange("a", unitFraction(0.59)));
    assertOptionalEquals(unitFraction(0.6),  closedUnitFractionRanges.getNearestValueInRange("a", unitFraction(0.6)));
    assertOptionalEquals(unitFraction(0.6),  closedUnitFractionRanges.getNearestValueInRange("a", unitFraction(0.61)));

    assertOptionalEquals(unitFraction(0.5), closedUnitFractionRanges.getNearestValueInRange("b", unitFraction(0.49)));
    assertOptionalEquals(unitFraction(0.5), closedUnitFractionRanges.getNearestValueInRange("b", unitFraction(0.50)));
    assertOptionalEquals(unitFraction(0.5), closedUnitFractionRanges.getNearestValueInRange("b", unitFraction(0.51)));
  }

  @Override
  public ClosedUnitFractionRanges<String> makeTrivialObject() {
    return emptyClosedUnitFractionRanges();
  }

  @Override
  public ClosedUnitFractionRanges<String> makeNontrivialObject() {
    return closedUnitFractionRanges(rbMapOf(
        "a", closedUnitFractionRange(UNIT_FRACTION_0, UNIT_FRACTION_1),
        "b", closedUnitFractionRange(unitFraction(0.1), unitFraction(0.9)),
        "c", closedUnitFractionRange(unitFraction(0.5), unitFraction(0.5))));
  }

  @Override
  public ClosedUnitFractionRanges<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return closedUnitFractionRanges(rbMapOf(
        "c", closedUnitFractionRange(unitFraction(0.5 - e), unitFraction(0.5 + e)),
        "b", closedUnitFractionRange(unitFraction(0.1 - e), unitFraction(0.9 + e)),
        "a", closedUnitFractionRange(unitFraction(e), unitFraction(1 - e))));
  }


  @Override
  protected boolean willMatch(ClosedUnitFractionRanges<String> expected, ClosedUnitFractionRanges<String> actual) {
    return closedUnitFractionRangesMatcher(expected).matches(actual);
  }

  public static <K> TypeSafeMatcher<ClosedUnitFractionRanges<K>> closedUnitFractionRangesMatcher(
      ClosedUnitFractionRanges<K> expected) {
    return makeMatcher(expected,
        matchRBMap(v -> v.getRawMap(), f -> closedUnitFractionRangeMatcher(f)));
  }

}
