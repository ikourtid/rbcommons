package com.rb.biz.marketdata.instrumentmaster;

import com.rb.nonbiz.testutils.RBTest;
import org.jmock.Expectations;
import org.junit.Test;

import java.time.LocalDate;

import static com.rb.biz.marketdata.instrumentmaster.AllIndexedInstrumentMasterData.allIndexedInstrumentMasterData;
import static com.rb.biz.marketdata.instrumentmaster.AllIndexedInstrumentMasterDataTest.allIndexedInstrumentMasterDataMatcher;
import static com.rb.biz.marketdata.instrumentmaster.AllUnindexedInstrumentMasterDataTest.allUnindexedInstrumentMasterDataMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

public class AllIndexedInstrumentMasterDataLoaderTest extends RBTest<AllIndexedInstrumentMasterDataLoader> {

  AllUnindexedInstrumentMasterDataLoader allUnindexedInstrumentMasterDataLoader =
      mockery.mock(AllUnindexedInstrumentMasterDataLoader.class);
  InstrumentMasterIndexerBySymbol instrumentMasterIndexerBySymbol =
      mockery.mock(InstrumentMasterIndexerBySymbol.class);

  @Test
  public void loadsData_createsIndex_returnsCombined() {
    AllUnindexedInstrumentMasterData unindexed = new AllUnindexedInstrumentMasterDataTest().makeTrivialObject();
    InstrumentMasterIndexBySymbol indexBySymbol = new InstrumentMasterIndexBySymbolTest().makeTrivialObject();
    mockery.checking(new Expectations() {{
      oneOf(allUnindexedInstrumentMasterDataLoader).load(LocalDate.of(1974, 4, 4));
      will(returnValue(unindexed));

      oneOf(instrumentMasterIndexerBySymbol).generateIndex(
          with(allUnindexedInstrumentMasterDataMatcher(unindexed)));
      will(returnValue(indexBySymbol));
    }});
    assertThat(
        makeTestObject().load(LocalDate.of(1974, 4, 4)),
        allIndexedInstrumentMasterDataMatcher(
            allIndexedInstrumentMasterData(unindexed, indexBySymbol)));
  }

  @Override
  protected AllIndexedInstrumentMasterDataLoader makeTestObject() {
    AllIndexedInstrumentMasterDataLoader testObject = new AllIndexedInstrumentMasterDataLoader();
    testObject.allUnindexedInstrumentMasterDataLoader = allUnindexedInstrumentMasterDataLoader;
    testObject.instrumentMasterIndexerBySymbol = instrumentMasterIndexerBySymbol;
    return testObject;
  }

}
