package com.rb.biz.marketdata.instrumentmaster;

import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.marketdata.instrumentmaster.HardCodedInstrumentMaster.hardCodedInstrumentMaster;
import static com.rb.biz.types.Symbol.symbol;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.IidBiMap.iidBiMap;
import static com.rb.nonbiz.collections.IidBiMapTest.iidBiMapMatcher;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
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

    // Create instrument master with IDs 1, 2, 11, and 12, and check we get that back when we ask
    // for all instrument IDs.
    assertThat(
        hardCodedInstrumentMaster(
            iidBiMap(iidMapOf(
                instrumentId(1),   symbol("AAPL"),
                instrumentId(2),   symbol("BAC"),
                instrumentId(11),  symbol("C"),
                instrumentId(12),  symbol("DOW")))).getAllInstrumentIdsAsIidSet(),
        iidSetMatcher(iidSetOf(instrumentId(1), instrumentId(2), instrumentId(11), instrumentId(12))));
  }

  public static TypeSafeMatcher<HardCodedInstrumentMaster> hardCodedInstrumentMasterMatcher(
      HardCodedInstrumentMaster expected) {
    return makeMatcher(expected,
        match(v -> v.getHardCodedSymbolBiMapDoNotUse(), f -> iidBiMapMatcher(f)));
  }

}
