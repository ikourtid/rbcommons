package com.rb.biz.marketdata.instrumentmaster;

import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;

import static com.rb.biz.marketdata.FakeInstruments.DUMMY_INSTRUMENT_ID;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.instrumentmaster.SingleTimePeriodInstrumentMasterData.singleTimePeriodInstrumentMasterData;
import static com.rb.biz.types.Symbol.symbol;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.rangeEqualityMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_SYMBOL;

public class SingleTimePeriodInstrumentMasterDataTest extends RBTestMatcher<SingleTimePeriodInstrumentMasterData>  {

  LocalDate dayA = LocalDate.of(1974, 4, 4);
  LocalDate dayB = LocalDate.of(1975, 5, 5);
  LocalDate dayC = LocalDate.of(1976, 6, 6);
  LocalDate dayD = LocalDate.of(1977, 7, 7);

  @Test
  public void dateRangeMustBeClosedOrAtLeast() {
    for (Range<LocalDate> invalidRange : rbSetOf(
        Range.closedOpen(dayC, dayD),
        Range.openClosed(dayC, dayD),
        Range.open(dayC, dayD),
        Range.greaterThan(dayC),
        Range.lessThan(dayD),
        Range.atMost(dayD),
        Range.<LocalDate>all())) {
      assertIllegalArgumentException( () -> singleTimePeriodInstrumentMasterData(DUMMY_INSTRUMENT_ID, DUMMY_SYMBOL, invalidRange));
    }
    SingleTimePeriodInstrumentMasterData doesNotThrow;
    doesNotThrow = singleTimePeriodInstrumentMasterData(DUMMY_INSTRUMENT_ID, DUMMY_SYMBOL, Range.closed(dayA, dayA));
    doesNotThrow = singleTimePeriodInstrumentMasterData(DUMMY_INSTRUMENT_ID, DUMMY_SYMBOL, Range.closed(dayA, dayB));
    doesNotThrow = singleTimePeriodInstrumentMasterData(DUMMY_INSTRUMENT_ID, DUMMY_SYMBOL, Range.atLeast(dayA));
  }

  @Override
  public SingleTimePeriodInstrumentMasterData makeTrivialObject() {
    return singleTimePeriodInstrumentMasterData(instrumentId(1), symbol("X"), Range.atLeast(dayA));
  }

  @Override
  public SingleTimePeriodInstrumentMasterData makeNontrivialObject() {
    return singleTimePeriodInstrumentMasterData(STOCK_A, symbol("A"), Range.closed(dayB, dayC));
  }

  @Override
  public SingleTimePeriodInstrumentMasterData makeMatchingNontrivialObject() {
    return singleTimePeriodInstrumentMasterData(STOCK_A, symbol("A"), Range.closed(dayB, dayC));
  }

  @Override
  protected boolean willMatch(SingleTimePeriodInstrumentMasterData expected, SingleTimePeriodInstrumentMasterData actual) {
    return singleTimePeriodInstrumentMasterDataMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<SingleTimePeriodInstrumentMasterData> singleTimePeriodInstrumentMasterDataMatcher(
      SingleTimePeriodInstrumentMasterData expected) {
    return makeMatcher(expected, actual ->
        expected.getInstrumentId().equals(actual.getInstrumentId())

            && expected.getSymbol().equals(actual.getSymbol())

            && rangeEqualityMatcher(expected.getDateRange()).matches(actual.getDateRange()));
  }

}
