package com.rb.biz.marketdata.instrumentmaster;

import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.instrumentmaster.InstrumentMasterIndexBySymbol.instrumentMasterIndexBySymbol;
import static com.rb.biz.types.Symbol.symbol;
import static com.rb.nonbiz.collections.NonContiguousNonDiscreteRangeMapTest.nonContiguousRangeMapEqualityMatcher;
import static com.rb.nonbiz.collections.NonContiguousRangeMap.singletonNonContiguousRangeMapWithEnd;
import static com.rb.nonbiz.collections.NonContiguousRangeMap.singletonNonContiguousRangeMapWithNoEnd;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class InstrumentMasterIndexBySymbolTest extends RBTestMatcher<InstrumentMasterIndexBySymbol> {

  @Test
  public void emptyMap_throws() {
    assertIllegalArgumentException( () -> instrumentMasterIndexBySymbol(emptyRBMap()));
  }

  @Override
  public InstrumentMasterIndexBySymbol makeTrivialObject() {
    return instrumentMasterIndexBySymbol(singletonRBMap(
        symbol("A"), singletonNonContiguousRangeMapWithNoEnd(LocalDate.of(1974, 4, 4), STOCK_A)));
  }

  @Override
  public InstrumentMasterIndexBySymbol makeNontrivialObject() {
    return instrumentMasterIndexBySymbol(rbMapOf(
        symbol("A"),  singletonNonContiguousRangeMapWithEnd(Range.closed(LocalDate.of(1974, 4, 4), LocalDate.of(1975, 5, 4)), STOCK_A),
        symbol("AX"), singletonNonContiguousRangeMapWithEnd(Range.closed(LocalDate.of(1975, 5, 5), LocalDate.of(1976, 6, 7)), STOCK_A),
        symbol("B"),  singletonNonContiguousRangeMapWithEnd(Range.closed(LocalDate.of(1977, 7, 7), LocalDate.of(1978, 8, 7)), STOCK_B),
        symbol("BB"), singletonNonContiguousRangeMapWithNoEnd(LocalDate.of(1978, 8, 8), STOCK_B)));
  }

  @Override
  public InstrumentMasterIndexBySymbol makeMatchingNontrivialObject() {
    return instrumentMasterIndexBySymbol(rbMapOf(
        symbol("A"),  singletonNonContiguousRangeMapWithEnd(Range.closed(LocalDate.of(1974, 4, 4), LocalDate.of(1975, 5, 4)), STOCK_A),
        symbol("AX"), singletonNonContiguousRangeMapWithEnd(Range.closed(LocalDate.of(1975, 5, 5), LocalDate.of(1976, 6, 7)), STOCK_A),
        symbol("B"),  singletonNonContiguousRangeMapWithEnd(Range.closed(LocalDate.of(1977, 7, 7), LocalDate.of(1978, 8, 7)), STOCK_B),
        symbol("BB"), singletonNonContiguousRangeMapWithNoEnd(LocalDate.of(1978, 8, 8), STOCK_B)));
  }

  @Override
  protected boolean willMatch(InstrumentMasterIndexBySymbol expected, InstrumentMasterIndexBySymbol actual) {
    return instrumentMasterIndexForSymbolMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<InstrumentMasterIndexBySymbol> instrumentMasterIndexForSymbolMatcher(
      InstrumentMasterIndexBySymbol expected) {
    return makeMatcher(expected, actual ->
        rbMapMatcher(expected.getSymbolToInstrumentIdByDate(), ncrm -> nonContiguousRangeMapEqualityMatcher(ncrm))
            .matches(actual.getSymbolToInstrumentIdByDate()));
  }

}
