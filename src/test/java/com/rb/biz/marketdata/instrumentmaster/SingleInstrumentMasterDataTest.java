package com.rb.biz.marketdata.instrumentmaster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.instrumentmaster.SingleInstrumentMasterData.singleInstrumentMasterData;
import static com.rb.biz.marketdata.instrumentmaster.SingleTimePeriodInstrumentMasterData.singleTimePeriodInstrumentMasterData;
import static com.rb.biz.marketdata.instrumentmaster.SingleTimePeriodInstrumentMasterDataTest.singleTimePeriodInstrumentMasterDataMatcher;
import static com.rb.biz.types.Symbol.symbol;
import static com.rb.nonbiz.collections.ContiguousDiscreteRangeMap.contiguousDiscreteRangeMap;
import static com.rb.nonbiz.collections.ContiguousDiscreteRangeMap.singletonContiguousDiscreteRangeMap;
import static com.rb.nonbiz.collections.ContiguousDiscreteRangeMapTest.contiguousDiscreteRangeMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_SYMBOL;

public class SingleInstrumentMasterDataTest extends RBTestMatcher<SingleInstrumentMasterData> {

  private final LocalDate dayA = LocalDate.of(1974, 4, 4);
  private final LocalDate dayB = LocalDate.of(1975, 5, 5);
  private final LocalDate dayC = LocalDate.of(1976, 6, 6);

  @Test
  public void idsMustMatch_otherwiseThrows() {
    assertIllegalArgumentException( () -> singleInstrumentMasterData(STOCK_A,
        singletonContiguousDiscreteRangeMap(
            Range.atLeast(dayA),
            singleTimePeriodInstrumentMasterData(STOCK_B, DUMMY_SYMBOL, Range.atLeast(dayA)))));
  }

  @Test
  public void rangesMustMatch_otherwiseThrows() {
    SingleInstrumentMasterData doesNotThrow = singleInstrumentMasterData(STOCK_A,
        singletonContiguousDiscreteRangeMap(Range.atLeast(dayA),
            singleTimePeriodInstrumentMasterData(STOCK_A, DUMMY_SYMBOL, Range.atLeast(dayA))));
    assertIllegalArgumentException( () -> singleInstrumentMasterData(STOCK_A,
        singletonContiguousDiscreteRangeMap(Range.atLeast(dayA),
            singleTimePeriodInstrumentMasterData(STOCK_A, DUMMY_SYMBOL, Range.closed(dayA, dayB)))));
    assertIllegalArgumentException( () -> singleInstrumentMasterData(STOCK_A,
        singletonContiguousDiscreteRangeMap(Range.atLeast(dayA),
            singleTimePeriodInstrumentMasterData(STOCK_A, DUMMY_SYMBOL, Range.atLeast(dayB)))));
  }

  @Override
  public SingleInstrumentMasterData makeTrivialObject() {
    return singleInstrumentMasterData(STOCK_A,
        singletonContiguousDiscreteRangeMap(Range.atLeast(dayA),
            singleTimePeriodInstrumentMasterData(STOCK_A, symbol("A"), Range.atLeast(dayA))));
  }

  @Override
  public SingleInstrumentMasterData makeNontrivialObject() {
    return singleInstrumentMasterData(STOCK_A,
        contiguousDiscreteRangeMap(
            ImmutableList.of(
                Range.closed(dayA, dayB.minusDays(1)),
                Range.closed(dayB, dayC.minusDays(1))),
            ImmutableList.of(
                singleTimePeriodInstrumentMasterData(STOCK_A, symbol("A"), Range.closed(dayA, dayB.minusDays(1))),
                singleTimePeriodInstrumentMasterData(STOCK_A, symbol("AX"), Range.closed(dayB, dayC.minusDays(1)))),
            date -> date.plusDays(1)));
  }

  @Override
  public SingleInstrumentMasterData makeMatchingNontrivialObject() {
    return singleInstrumentMasterData(STOCK_A,
        contiguousDiscreteRangeMap(
            ImmutableList.of(
                Range.closed(dayA, dayB.minusDays(1)),
                Range.closed(dayB, dayC.minusDays(1))),
            ImmutableList.of(
                singleTimePeriodInstrumentMasterData(STOCK_A, symbol("A"), Range.closed(dayA, dayB.minusDays(1))),
                singleTimePeriodInstrumentMasterData(STOCK_A, symbol("AX"), Range.closed(dayB, dayC.minusDays(1)))),
            date -> date.plusDays(1)));
  }

  @Override
  protected boolean willMatch(SingleInstrumentMasterData expected, SingleInstrumentMasterData actual) {
    return singleInstrumentMasterDataMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<SingleInstrumentMasterData> singleInstrumentMasterDataMatcher(SingleInstrumentMasterData expected) {
    return makeMatcher(expected, actual ->
        expected.getInstrumentId().equals(actual.getInstrumentId())
            && contiguousDiscreteRangeMapMatcher(expected.getDateToData(), stpimd -> singleTimePeriodInstrumentMasterDataMatcher(stpimd))
            .matches(actual.getDateToData()));
  }

}
