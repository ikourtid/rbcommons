package com.rb.biz.marketdata.instrumentmaster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.time.LocalDate;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.instrumentmaster.AllUnindexedInstrumentMasterData.allUnindexedInstrumentMasterData;
import static com.rb.biz.marketdata.instrumentmaster.InstrumentMasterIndexBySymbol.instrumentMasterIndexBySymbol;
import static com.rb.biz.marketdata.instrumentmaster.InstrumentMasterIndexBySymbolTest.instrumentMasterIndexForSymbolMatcher;
import static com.rb.biz.marketdata.instrumentmaster.SingleInstrumentMasterData.singleInstrumentMasterData;
import static com.rb.biz.marketdata.instrumentmaster.SingleTimePeriodInstrumentMasterData.singleTimePeriodInstrumentMasterData;
import static com.rb.biz.types.Symbol.symbol;
import static com.rb.nonbiz.collections.ContiguousDiscreteRangeMap.contiguousDiscreteRangeMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.NonContiguousRangeMap.singletonNonContiguousRangeMapWithEnd;
import static com.rb.nonbiz.collections.NonContiguousRangeMap.singletonNonContiguousRangeMapWithNoEnd;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class InstrumentMasterIndexerBySymbolTest extends RBTest<InstrumentMasterIndexerBySymbol> {

  private final LocalDate dayA = LocalDate.of(1974, 4, 4);
  private final LocalDate dayB = LocalDate.of(1975, 5, 5);
  private final LocalDate dayC = LocalDate.of(1976, 6, 6);
  private final LocalDate dayD = LocalDate.of(1977, 7, 7);
  private final LocalDate dayE = LocalDate.of(1978, 8, 8);

  @Test
  public void happyPath() {
    assertThat(
        makeTestObject().generateIndex(
            allUnindexedInstrumentMasterData(
                iidMapOf(
                    STOCK_A, singleInstrumentMasterData(STOCK_A,
                        contiguousDiscreteRangeMap(
                            ImmutableList.of(
                                Range.closed(dayA, dayB.minusDays(1)),
                                Range.closed(dayB, dayC.minusDays(1))),
                            ImmutableList.of(
                                singleTimePeriodInstrumentMasterData(STOCK_A, symbol("A"), Range.closed(dayA, dayB.minusDays(1))),
                                singleTimePeriodInstrumentMasterData(STOCK_A, symbol("AX"), Range.closed(dayB, dayC.minusDays(1)))),
                            date -> date.plusDays(1))),
                    STOCK_B, singleInstrumentMasterData(STOCK_B,
                        contiguousDiscreteRangeMap(
                            ImmutableList.of(
                                Range.closed(dayD, dayE.minusDays(1)),
                                Range.atLeast(dayE)),
                            ImmutableList.of(
                                singleTimePeriodInstrumentMasterData(STOCK_B, symbol("B"), Range.closed(dayD, dayE.minusDays(1))),
                                singleTimePeriodInstrumentMasterData(STOCK_B, symbol("BB"), Range.atLeast(dayE))),
                            date -> date.plusDays(1)))))),
        instrumentMasterIndexForSymbolMatcher(instrumentMasterIndexBySymbol(rbMapOf(
            symbol("A"),  singletonNonContiguousRangeMapWithEnd(Range.closed(dayA, dayB.minusDays(1)), STOCK_A),
            symbol("AX"), singletonNonContiguousRangeMapWithEnd(Range.closed(dayB, dayC.minusDays(1)), STOCK_A),
            symbol("B"),  singletonNonContiguousRangeMapWithEnd(Range.closed(dayD, dayE.minusDays(1)), STOCK_B),
            symbol("BB"), singletonNonContiguousRangeMapWithNoEnd(dayE, STOCK_B)))));
  }

  @Override
  protected InstrumentMasterIndexerBySymbol makeTestObject() {
    return new InstrumentMasterIndexerBySymbol();
  }

}
