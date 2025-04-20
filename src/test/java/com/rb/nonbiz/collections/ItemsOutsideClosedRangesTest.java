package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.AssetId;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.types.asset.CashId.CASH;
import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.ItemsOutsideClosedRanges.emptyItemsOutsideClosedRanges;
import static com.rb.nonbiz.collections.ItemsOutsideClosedRanges.itemsOutsideClosedRanges;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.ValueOutsideClosedRange.valueOutsideClosedRange;
import static com.rb.nonbiz.collections.ValueOutsideClosedRangeTest.valueOutsideClosedRangeMatcher;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// This test class is not generic, but the publicly exposed static matcher is.
public class ItemsOutsideClosedRangesTest extends RBTestMatcher<ItemsOutsideClosedRanges<AssetId, UnitFraction>> {

  @Test
  public void testIsEmpty() {
    assertTrue(emptyItemsOutsideClosedRanges().isEmpty());
    assertFalse(makeNontrivialObject().isEmpty());
  }

  @Override
  public ItemsOutsideClosedRanges<AssetId, UnitFraction> makeTrivialObject() {
    return emptyItemsOutsideClosedRanges();
  }

  @Override
  public ItemsOutsideClosedRanges<AssetId, UnitFraction> makeNontrivialObject() {
    return itemsOutsideClosedRanges(rbMapOf(
        STOCK_A1, valueOutsideClosedRange(unitFraction(0.10), closedRange(unitFraction(0.71), unitFraction(0.81))),
        CASH,     valueOutsideClosedRange(unitFraction(0.90), closedRange(unitFraction(0.91), UNIT_FRACTION_1))));
  }

  @Override
  public ItemsOutsideClosedRanges<AssetId, UnitFraction> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return itemsOutsideClosedRanges(rbMapOf(
        STOCK_A1, valueOutsideClosedRange(unitFraction(0.10 + e), closedRange(unitFraction(0.71 + e), unitFraction(0.81 + e))),
        CASH,     valueOutsideClosedRange(unitFraction(0.90 + e), closedRange(unitFraction(0.91 + e), UNIT_FRACTION_1))));
  }

  @Override
  protected boolean willMatch(ItemsOutsideClosedRanges<AssetId, UnitFraction> expected,
                              ItemsOutsideClosedRanges<AssetId, UnitFraction> actual) {
    return itemsOutsideClosedRangesMatcher(expected, f -> preciseValueMatcher(f, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <K, V extends Comparable<? super V>> TypeSafeMatcher<ItemsOutsideClosedRanges<K, V>>
  itemsOutsideClosedRangesMatcher(
      ItemsOutsideClosedRanges<K, V> expected, MatcherGenerator<V> matcherGenerator) {
    return makeMatcher(expected,
        matchRBMap(v -> v.getRawMap(), f -> valueOutsideClosedRangeMatcher(f, matcherGenerator)));
  }

}
