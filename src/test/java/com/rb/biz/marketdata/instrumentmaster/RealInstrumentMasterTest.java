package com.rb.biz.marketdata.instrumentmaster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.biz.guice.RBClock;
import com.rb.nonbiz.testutils.RBTest;
import org.jmock.Expectations;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.instrumentmaster.AllIndexedInstrumentMasterData.allIndexedInstrumentMasterData;
import static com.rb.biz.marketdata.instrumentmaster.AllUnindexedInstrumentMasterData.allUnindexedInstrumentMasterData;
import static com.rb.biz.marketdata.instrumentmaster.InstrumentMasterIndexBySymbol.instrumentMasterIndexBySymbol;
import static com.rb.biz.marketdata.instrumentmaster.SingleInstrumentMasterData.singleInstrumentMasterData;
import static com.rb.biz.marketdata.instrumentmaster.SingleTimePeriodInstrumentMasterData.singleTimePeriodInstrumentMasterData;
import static com.rb.biz.types.Symbol.symbol;
import static com.rb.nonbiz.collections.ContiguousDiscreteRangeMap.contiguousDiscreteRangeMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.NonContiguousRangeMap.singletonNonContiguousRangeMapWithEnd;
import static com.rb.nonbiz.collections.NonContiguousRangeMap.singletonNonContiguousRangeMapWithNoEnd;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;

public class RealInstrumentMasterTest extends RBTest<RealInstrumentMaster> {

  AllInstrumentMasterDataSupplier allInstrumentMasterDataSupplier =
      mockery.mock(AllInstrumentMasterDataSupplier.class);

  private final LocalDateTime NOW = DUMMY_TIME;

  private final LocalDate dayA = LocalDate.of(1974, 4, 4);
  private final LocalDate dayB = LocalDate.of(1975, 5, 5);
  private final LocalDate dayC = LocalDate.of(1976, 6, 6);
  private final LocalDate dayD = LocalDate.of(1977, 7, 7);
  private final LocalDate dayE = LocalDate.of(1978, 8, 8);
  private final AllIndexedInstrumentMasterData INSTRUMENT_MASTER_DATA = allIndexedInstrumentMasterData(
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
                      date -> date.plusDays(1))))),
      instrumentMasterIndexBySymbol(rbMapOf(
          symbol("A"),  singletonNonContiguousRangeMapWithEnd(Range.closed(dayA, dayB.minusDays(1)), STOCK_A),
          symbol("AX"), singletonNonContiguousRangeMapWithEnd(Range.closed(dayB, dayC.minusDays(1)), STOCK_A),
          symbol("B"),  singletonNonContiguousRangeMapWithEnd(Range.closed(dayD, dayE.minusDays(1)), STOCK_B),
          symbol("BB"), singletonNonContiguousRangeMapWithNoEnd(dayE, STOCK_B))));

  @Test
  public void testGetSymbol() {
    expectInstrumentMasterData(INSTRUMENT_MASTER_DATA);

    assertOptionalEmpty(makeTestObject().getSymbol(STOCK_A, dayA.minusDays(1)));
    assertOptionalEquals(symbol("A"), makeTestObject().getSymbol(STOCK_A, dayA));
    assertOptionalEquals(symbol("A"), makeTestObject().getSymbol(STOCK_A, dayA.plusDays(1)));
    assertOptionalEquals(symbol("A"), makeTestObject().getSymbol(STOCK_A, dayB.minusDays(1)));
    assertOptionalEquals(symbol("AX"), makeTestObject().getSymbol(STOCK_A, dayB));
    assertOptionalEquals(symbol("AX"), makeTestObject().getSymbol(STOCK_A, dayB.plusDays(1)));
    assertOptionalEquals(symbol("AX"), makeTestObject().getSymbol(STOCK_A, dayC.minusDays(1)));
    assertOptionalEmpty(makeTestObject().getSymbol(STOCK_A, dayC));

    assertOptionalEmpty(makeTestObject().getSymbol(STOCK_B, dayD.minusDays(1)));
    assertOptionalEquals(symbol("B"), makeTestObject().getSymbol(STOCK_B, dayD));
    assertOptionalEquals(symbol("B"), makeTestObject().getSymbol(STOCK_B, dayD.plusDays(1)));
    assertOptionalEquals(symbol("B"), makeTestObject().getSymbol(STOCK_B, dayE.minusDays(1)));
    assertOptionalEquals(symbol("BB"), makeTestObject().getSymbol(STOCK_B, dayE));
    assertOptionalEquals(symbol("BB"), makeTestObject().getSymbol(STOCK_B, dayE.plusDays(1)));
    assertOptionalEquals(symbol("BB"), makeTestObject().getSymbol(STOCK_B, dayE.plusDays(10_000)));
  }
  
  @Test
  public void testGetLatestValidSymbol() {
    expectInstrumentMasterData(INSTRUMENT_MASTER_DATA);

    assertOptionalEmpty(makeTestObject().getLatestValidSymbol(STOCK_A, dayA.minusDays(1)));
    assertOptionalEquals(symbol("A"), makeTestObject().getLatestValidSymbol(STOCK_A, dayA));
    assertOptionalEquals(symbol("A"), makeTestObject().getLatestValidSymbol(STOCK_A, dayA.plusDays(1)));
    assertOptionalEquals(symbol("A"), makeTestObject().getLatestValidSymbol(STOCK_A, dayB.minusDays(1)));
    assertOptionalEquals(symbol("AX"), makeTestObject().getLatestValidSymbol(STOCK_A, dayB));
    assertOptionalEquals(symbol("AX"), makeTestObject().getLatestValidSymbol(STOCK_A, dayB.plusDays(1)));
    assertOptionalEquals(symbol("AX"), makeTestObject().getLatestValidSymbol(STOCK_A, dayC.minusDays(1)));
    // Note that dayC is the first date when this symbol stops being live
    assertOptionalEquals(symbol("AX"), makeTestObject().getLatestValidSymbol(STOCK_A, dayC));
    assertOptionalEquals(symbol("AX"), makeTestObject().getLatestValidSymbol(STOCK_A, dayC.plusDays(1)));
    assertOptionalEquals(symbol("AX"), makeTestObject().getLatestValidSymbol(STOCK_A, dayC.plusDays(10_000)));

    assertOptionalEmpty(makeTestObject().getLatestValidSymbol(STOCK_B, dayD.minusDays(1)));
    assertOptionalEquals(symbol("B"), makeTestObject().getLatestValidSymbol(STOCK_B, dayD));
    assertOptionalEquals(symbol("B"), makeTestObject().getLatestValidSymbol(STOCK_B, dayD.plusDays(1)));
    assertOptionalEquals(symbol("B"), makeTestObject().getLatestValidSymbol(STOCK_B, dayE.minusDays(1)));
    assertOptionalEquals(symbol("BB"), makeTestObject().getLatestValidSymbol(STOCK_B, dayE));
    assertOptionalEquals(symbol("BB"), makeTestObject().getLatestValidSymbol(STOCK_B, dayE.plusDays(1)));
    assertOptionalEquals(symbol("BB"), makeTestObject().getLatestValidSymbol(STOCK_B, dayE.plusDays(10_000)));
  }

  @Test
  public void testGetInstrumentId() {
    expectInstrumentMasterData(INSTRUMENT_MASTER_DATA);

    assertOptionalEmpty(makeTestObject().getInstrumentId(symbol("A"), dayA.minusDays(1)));
    assertOptionalEquals(STOCK_A, makeTestObject().getInstrumentId(symbol("A"), dayA));
    assertOptionalEquals(STOCK_A, makeTestObject().getInstrumentId(symbol("A"), dayA.plusDays(1)));
    assertOptionalEquals(STOCK_A, makeTestObject().getInstrumentId(symbol("A"), dayB.minusDays(1)));
    assertOptionalEmpty(makeTestObject().getInstrumentId(symbol("A"), dayB));

    assertOptionalEmpty(makeTestObject().getInstrumentId(symbol("AX"), dayB.minusDays(1)));
    assertOptionalEquals(STOCK_A, makeTestObject().getInstrumentId(symbol("AX"), dayB));
    assertOptionalEquals(STOCK_A, makeTestObject().getInstrumentId(symbol("AX"), dayB.plusDays(1)));
    assertOptionalEquals(STOCK_A, makeTestObject().getInstrumentId(symbol("AX"), dayC.minusDays(1)));
    assertOptionalEmpty(makeTestObject().getInstrumentId(symbol("AX"), dayC));

    assertOptionalEmpty(makeTestObject().getInstrumentId(symbol("B"), dayD.minusDays(1)));
    assertOptionalEquals(STOCK_B, makeTestObject().getInstrumentId(symbol("B"), dayD));
    assertOptionalEquals(STOCK_B, makeTestObject().getInstrumentId(symbol("B"), dayD.plusDays(1)));
    assertOptionalEquals(STOCK_B, makeTestObject().getInstrumentId(symbol("B"), dayE.minusDays(1)));
    assertOptionalEmpty(makeTestObject().getInstrumentId(symbol("B"), dayE));

    assertOptionalEmpty(makeTestObject().getInstrumentId(symbol("BB"), dayE.minusDays(1)));
    assertOptionalEquals(STOCK_B, makeTestObject().getInstrumentId(symbol("BB"), dayE));
    assertOptionalEquals(STOCK_B, makeTestObject().getInstrumentId(symbol("BB"), dayE.plusDays(1)));
    assertOptionalEquals(STOCK_B, makeTestObject().getInstrumentId(symbol("BB"), dayE.plusDays(10_000)));
  }

  private void expectInstrumentMasterData(AllIndexedInstrumentMasterData allIndexedInstrumentMasterData) {
    mockery.checking(new Expectations() {{
      atLeast(1).of(allInstrumentMasterDataSupplier).getAllMasterDataAsOf(NOW.toLocalDate());
      will(returnValue(allIndexedInstrumentMasterData));
    }});
  }

  @Override
  protected RealInstrumentMaster makeTestObject() {
    RealInstrumentMaster testObject = new RealInstrumentMaster();
    testObject.allInstrumentMasterDataSupplier = allInstrumentMasterDataSupplier;
    testObject.rbClock = new RBClock(NOW);
    return testObject;
  }

}
