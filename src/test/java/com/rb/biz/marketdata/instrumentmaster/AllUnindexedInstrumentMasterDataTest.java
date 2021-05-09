package com.rb.biz.marketdata.instrumentmaster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.instrumentmaster.AllUnindexedInstrumentMasterData.allUnindexedInstrumentMasterData;
import static com.rb.biz.marketdata.instrumentmaster.SingleInstrumentMasterData.singleInstrumentMasterData;
import static com.rb.biz.marketdata.instrumentmaster.SingleInstrumentMasterDataTest.singleInstrumentMasterDataMatcher;
import static com.rb.biz.marketdata.instrumentmaster.SingleTimePeriodInstrumentMasterData.singleTimePeriodInstrumentMasterData;
import static com.rb.biz.types.Symbol.symbol;
import static com.rb.nonbiz.collections.ContiguousDiscreteRangeMap.contiguousDiscreteRangeMap;
import static com.rb.nonbiz.collections.ContiguousDiscreteRangeMap.singletonContiguousDiscreteRangeMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class AllUnindexedInstrumentMasterDataTest extends RBTestMatcher<AllUnindexedInstrumentMasterData> {

  private final LocalDate dayA = LocalDate.of(1974, 4, 4);
  private final LocalDate dayB = LocalDate.of(1975, 5, 5);
  private final LocalDate dayC = LocalDate.of(1976, 6, 6);
  private final LocalDate dayD = LocalDate.of(1977, 7, 7);
  private final LocalDate dayE = LocalDate.of(1978, 8, 8);

  @Test
  public void instrumentIdsDoNotMatch_throws() {
    SingleInstrumentMasterData dataA = singleInstrumentMasterData(STOCK_A, singletonContiguousDiscreteRangeMap(
        Range.atLeast(dayA),
        singleTimePeriodInstrumentMasterData(STOCK_A, symbol("A"), Range.atLeast(dayA))));
    assertIllegalArgumentException( () -> allUnindexedInstrumentMasterData(
        singletonIidMap(STOCK_B, dataA)));
  }

  @Test
  public void emptyMapPassed_throws() {
    assertIllegalArgumentException( () -> allUnindexedInstrumentMasterData(emptyIidMap()));
  }

  @Override
  public AllUnindexedInstrumentMasterData makeTrivialObject() {
    return allUnindexedInstrumentMasterData(
        singletonIidMap(
            STOCK_A, singleInstrumentMasterData(STOCK_A, singletonContiguousDiscreteRangeMap(
                Range.atLeast(dayA),
                singleTimePeriodInstrumentMasterData(STOCK_A, symbol("A"), Range.atLeast(dayA))))));
  }

  @Override
  public AllUnindexedInstrumentMasterData makeNontrivialObject() {
    return allUnindexedInstrumentMasterData(
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
                    date -> date.plusDays(1)))));
  }

  @Override
  public AllUnindexedInstrumentMasterData makeMatchingNontrivialObject() {
    // same as nontrivial object; there's nothing we can epsilon-tweak here
    return allUnindexedInstrumentMasterData(
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
                    date -> date.plusDays(1)))));
  }

  @Override
  protected boolean willMatch(AllUnindexedInstrumentMasterData expected, AllUnindexedInstrumentMasterData actual) {
    return allUnindexedInstrumentMasterDataMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<AllUnindexedInstrumentMasterData> allUnindexedInstrumentMasterDataMatcher(
      AllUnindexedInstrumentMasterData expected) {
    return makeMatcher(expected,
        matchIidMap(v -> v.getMasterDataMap(), f -> singleInstrumentMasterDataMatcher(f)));
  }

}
