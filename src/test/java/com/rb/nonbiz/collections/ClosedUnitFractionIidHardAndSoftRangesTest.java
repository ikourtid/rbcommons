package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.ClosedUnitFractionRange;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.marketdata.FakeInstruments.DUMMY_INSTRUMENT_ID;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.nonbiz.collections.ClosedUnitFractionIidHardAndSoftRanges.closedUnitFractionIidHardAndSoftRanges;
import static com.rb.nonbiz.collections.ClosedUnitFractionIidHardAndSoftRanges.emptyClosedUnitFractionIidHardAndSoftRanges;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRange.closedUnitFractionHardAndSoftRange;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRangeTest.closedUnitFractionHardAndSoftRangeMatcher;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unrestrictedClosedUnitFractionRange;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class ClosedUnitFractionIidHardAndSoftRangesTest extends RBTestMatcher<ClosedUnitFractionIidHardAndSoftRanges> {

  @Test
  public void unrestrictedRangesAreDisallowed() {
    Function<ClosedUnitFractionRange, ClosedUnitFractionIidHardAndSoftRanges> maker =
        softRange -> closedUnitFractionIidHardAndSoftRanges(singletonIidMap(
        DUMMY_INSTRUMENT_ID, closedUnitFractionHardAndSoftRange(unrestrictedClosedUnitFractionRange(), softRange)));
    assertIllegalArgumentException( () -> maker.apply(
        unrestrictedClosedUnitFractionRange()));
    ClosedUnitFractionIidHardAndSoftRanges doesNotThrow = maker.apply(
        closedUnitFractionRange(unitFraction(0.98), unitFraction(0.99)));
  }

  @Override
  public ClosedUnitFractionIidHardAndSoftRanges makeTrivialObject() {
    return emptyClosedUnitFractionIidHardAndSoftRanges();
  }

  @Override
  public ClosedUnitFractionIidHardAndSoftRanges makeNontrivialObject() {
    return closedUnitFractionIidHardAndSoftRanges(
        iidMapOf(
            // the soft range for A is much narrower than the hard range, but that's valid.
            STOCK_A, closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(UNIT_FRACTION_0, UNIT_FRACTION_1),
                closedUnitFractionRange(unitFraction(0.98), unitFraction(0.99))),
            STOCK_B, closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(unitFraction(0.1), unitFraction(0.9)),
                closedUnitFractionRange(unitFraction(0.11), unitFraction(0.89))),
            STOCK_C, closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6)),
                closedUnitFractionRange(unitFraction(0.41), unitFraction(0.59)))));
  }

  @Override
  public ClosedUnitFractionIidHardAndSoftRanges makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return closedUnitFractionIidHardAndSoftRanges(
        iidMapOf(
            // the soft range for A is much narrower than the hard range, but that's valid.
            STOCK_A, closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(UNIT_FRACTION_0, UNIT_FRACTION_1),
                closedUnitFractionRange(unitFraction(0.98 + e), unitFraction(0.99 + e))),
            STOCK_B, closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(unitFraction(0.1 + e), unitFraction(0.9 + e)),
                closedUnitFractionRange(unitFraction(0.11 + e), unitFraction(0.89 + e))),
            STOCK_C, closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(unitFraction(0.4 + e), unitFraction(0.6 + e)),
                closedUnitFractionRange(unitFraction(0.41 + e), unitFraction(0.59 + e)))));
  }

  @Override
  protected boolean willMatch(ClosedUnitFractionIidHardAndSoftRanges expected,
                              ClosedUnitFractionIidHardAndSoftRanges actual) {
    return closedUnitFractionIidHardAndSoftRangesMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<ClosedUnitFractionIidHardAndSoftRanges> closedUnitFractionIidHardAndSoftRangesMatcher(
      ClosedUnitFractionIidHardAndSoftRanges expected) {
    return makeMatcher(expected,
        matchIidMap(v -> v.getRawMap(), f -> closedUnitFractionHardAndSoftRangeMatcher(f)));
  }

}
