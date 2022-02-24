package com.rb.biz.marketdata.instrumentmaster;

import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.DUMMY_INSTRUMENT_ID;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.instrumentmaster.CascadingInstrumentMaster.cascadingInstrumentMaster;
import static com.rb.biz.marketdata.instrumentmaster.HardCodedAllowingEmptyInstrumentMaster.hardCodedAllowingEmptyInstrumentMaster;
import static com.rb.biz.marketdata.instrumentmaster.HardCodedInstrumentMaster.hardCodedInstrumentMaster;
import static com.rb.biz.types.Symbol.symbol;
import static com.rb.nonbiz.collections.IidBiMaps.iidBiMapOf;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DATE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_SYMBOL;

import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class CascadingInstrumentMasterTest {

  @Test
  public void generalCase() {
    InstrumentMaster instrumentMaster = cascadingInstrumentMaster(
        hardCodedAllowingEmptyInstrumentMaster(
            STOCK_A, "A1",
            STOCK_B, "B1"),
        hardCodedAllowingEmptyInstrumentMaster(
            STOCK_B, "B2", // obscured by the first hardCodedAllowingEmptyInstrumentMaster, above
            STOCK_C, "C2"));

    // For simplicity, this test doesn't use the date, because hardCodedInstrumentMaster doesn't either.
    assertOptionalEquals(STOCK_A, instrumentMaster.getInstrumentId(symbol("A1"), DUMMY_DATE));
    assertOptionalEquals(STOCK_B, instrumentMaster.getInstrumentId(symbol("B1"), DUMMY_DATE));
    assertOptionalEquals(STOCK_C, instrumentMaster.getInstrumentId(symbol("C2"), DUMMY_DATE));
    assertOptionalEmpty(instrumentMaster.getInstrumentId(DUMMY_SYMBOL, DUMMY_DATE));

    // The semantics are a bit unexpected with CascadingInstrumentMaster when you go from symbol to instrument ID
    // (instead of vice versa), but they are simple: just fall through to the next IM if you can't find the answer.
    // Here, even though the first IM's STOCK_B ("B1") 'obscures' the second IM's entry ("B2"),
    // asking for B2's instrument ID will just fall through and return STOCK_B from the second IM.
    assertOptionalEquals(STOCK_B, instrumentMaster.getInstrumentId(symbol("B2"), DUMMY_DATE));

    assertOptionalEquals(symbol("A1"), instrumentMaster.getSymbol(STOCK_A, DUMMY_DATE));
    assertOptionalEquals(symbol("B1"), instrumentMaster.getSymbol(STOCK_B, DUMMY_DATE));
    assertOptionalEquals(symbol("C2"), instrumentMaster.getSymbol(STOCK_C, DUMMY_DATE));
    assertOptionalEmpty(instrumentMaster.getSymbol(DUMMY_INSTRUMENT_ID, DUMMY_DATE));

    assertOptionalEquals(symbol("A1"), instrumentMaster.getLatestValidSymbol(STOCK_A, DUMMY_DATE));
    assertOptionalEquals(symbol("B1"), instrumentMaster.getLatestValidSymbol(STOCK_B, DUMMY_DATE));
    assertOptionalEquals(symbol("C2"), instrumentMaster.getLatestValidSymbol(STOCK_C, DUMMY_DATE));
    assertOptionalEmpty(instrumentMaster.getLatestValidSymbol(DUMMY_INSTRUMENT_ID, DUMMY_DATE));
  }

}
