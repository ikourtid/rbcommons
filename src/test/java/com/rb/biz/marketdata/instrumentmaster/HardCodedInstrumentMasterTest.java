package com.rb.biz.marketdata.instrumentmaster;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.collections.IidBiMapTest.iidBiMapMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class HardCodedInstrumentMasterTest {

  public static TypeSafeMatcher<HardCodedInstrumentMaster> hardCodedInstrumentMasterMatcher(
      HardCodedInstrumentMaster expected) {
    return makeMatcher(expected,
        match(v -> v.getHardCodedSymbolBiMapDoNotUse(), f -> iidBiMapMatcher(f)));
  }

}
