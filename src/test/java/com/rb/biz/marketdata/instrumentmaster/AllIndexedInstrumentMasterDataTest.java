package com.rb.biz.marketdata.instrumentmaster;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.biz.marketdata.instrumentmaster.AllIndexedInstrumentMasterData.allIndexedInstrumentMasterData;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class AllIndexedInstrumentMasterDataTest extends RBTestMatcher<AllIndexedInstrumentMasterData> {

  @Override
  public AllIndexedInstrumentMasterData makeTrivialObject() {
    return allIndexedInstrumentMasterData(
        new AllUnindexedInstrumentMasterDataTest().makeTrivialObject(),
        new InstrumentMasterIndexBySymbolTest().makeTrivialObject());
  }

  @Override
  public AllIndexedInstrumentMasterData makeNontrivialObject() {
    return allIndexedInstrumentMasterData(
        new AllUnindexedInstrumentMasterDataTest().makeNontrivialObject(),
        new InstrumentMasterIndexBySymbolTest().makeNontrivialObject());
  }

  @Override
  public AllIndexedInstrumentMasterData makeMatchingNontrivialObject() {
    return allIndexedInstrumentMasterData(
        new AllUnindexedInstrumentMasterDataTest().makeMatchingNontrivialObject(),
        new InstrumentMasterIndexBySymbolTest().makeMatchingNontrivialObject());
  }

  @Override
  protected boolean willMatch(AllIndexedInstrumentMasterData expected, AllIndexedInstrumentMasterData actual) {
    return allIndexedInstrumentMasterDataMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<AllIndexedInstrumentMasterData> allIndexedInstrumentMasterDataMatcher(
      AllIndexedInstrumentMasterData expected) {
    return makeMatcher(expected, actual ->
        AllUnindexedInstrumentMasterDataTest.allUnindexedInstrumentMasterDataMatcher(expected.getAllUnindexedInstrumentMasterData())
            .matches(actual.getAllUnindexedInstrumentMasterData())

            && InstrumentMasterIndexBySymbolTest.instrumentMasterIndexForSymbolMatcher(expected.getInstrumentMasterIndexBySymbol())
            .matches(actual.getInstrumentMasterIndexBySymbol()));
  }

}
