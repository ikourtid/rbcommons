package com.rb.biz.marketdata.instrumentmaster;

import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.marketdata.instrumentmaster.HardCodedInstrumentMaster.hardCodedInstrumentMaster;
import static com.rb.nonbiz.collections.IidBiMap.iidBiMap;
import static com.rb.nonbiz.collections.IidBiMapTest.iidBiMapMatcher;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetTest.iidSetMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

public class HardCodedInstrumentMasterTest {

  @Test
  public void testGetAllInstrumentIdsAsIidSet() {

    // Test empty instrument master.
    assertThat(
        hardCodedInstrumentMaster(iidBiMap(emptyIidMap())).getAllInstrumentIdsAsIidSet(),
        iidSetMatcher(emptyIidSet()));

    // Create instrument master with IDs for STOCK_A, STOCK_B, STOCK_C, and STOCK_D, and check we get that same set when we ask
    // for all instrument IDs.
    assertThat(
        hardCodedInstrumentMaster(
            STOCK_A, "STOCK_A",
            STOCK_B, "STOCK_B",
            STOCK_C, "STOCK_C",
            STOCK_D, "STOCK_D").getAllInstrumentIdsAsIidSet(),
        iidSetMatcher(iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D)));
  }

  public static TypeSafeMatcher<HardCodedInstrumentMaster> hardCodedInstrumentMasterMatcher(
      HardCodedInstrumentMaster expected) {
    return makeMatcher(expected,
        match(v -> v.getHardCodedSymbolBiMapDoNotUse(), f -> iidBiMapMatcher(f)));
  }

}
