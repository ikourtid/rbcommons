package com.rb.biz.marketdata.instrumentmaster;

import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidBiMap;
import com.rb.nonbiz.collections.IidMap;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.DUMMY_INSTRUMENT_ID;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.instrumentmaster.CascadingInstrumentMaster.cascadingInstrumentMaster;
import static com.rb.biz.marketdata.instrumentmaster.CascadingInstrumentMasterTest.LocalHardCodedInstrumentMaster.localHardCodedInstrumentMaster;
import static com.rb.biz.marketdata.instrumentmaster.HardCodedInstrumentMaster.hardCodedInstrumentMaster;
import static com.rb.biz.types.Symbol.symbol;
import static com.rb.nonbiz.collections.IidBiMap.iidBiMap;
import static com.rb.nonbiz.collections.IidBiMaps.emptyIidBiMap;
import static com.rb.nonbiz.collections.IidBiMaps.iidBiMapOf;
import static com.rb.nonbiz.collections.IidBiMaps.singletonIidBiMap;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DATE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_SYMBOL;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import java.time.LocalDate;
import java.util.Optional;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class CascadingInstrumentMasterTest {


  // For this test, we can't use HardCodedInstrumentMaster, because it does some fail-fast logic in cases of
  // missing items. So we'll redefine it here but call getOptional in various places.
  static class LocalHardCodedInstrumentMaster implements InstrumentMaster {

    private final IidBiMap<Symbol> hardCodedSymbolBiMap;

    private LocalHardCodedInstrumentMaster(IidBiMap<Symbol> hardCodedSymbolBiMap) {
      this.hardCodedSymbolBiMap = hardCodedSymbolBiMap;
    }

    static LocalHardCodedInstrumentMaster localHardCodedInstrumentMaster(
        InstrumentId instrumentId1, String symbol1,
        InstrumentId instrumentId2, String symbol2) {
      return new LocalHardCodedInstrumentMaster(iidBiMapOf(
          instrumentId1, symbol(symbol1),
          instrumentId2, symbol(symbol2)));
    }

    @Override
    public Optional<InstrumentId> getInstrumentId(Symbol symbol, LocalDate ignoredDate) {
      return hardCodedSymbolBiMap.getInstrumentIdFromItem().getOptional(symbol);
    }

    @Override
    public Optional<Symbol> getSymbol(InstrumentId instrumentId, LocalDate ignoredEffectiveDate) {
      return hardCodedSymbolBiMap.getItemFromInstrumentId().getOptional(instrumentId);
    }

    @Override
    public Optional<Symbol> getLatestValidSymbol(InstrumentId instrumentId, LocalDate ignoredEffectiveDate) {
      return getSymbol(instrumentId, ignoredEffectiveDate);
    }

  }


  @Test
  public void generalCase() {
    InstrumentMaster instrumentMaster = cascadingInstrumentMaster(
        localHardCodedInstrumentMaster(
            STOCK_A, "A1",
            STOCK_B, "B1"),
        localHardCodedInstrumentMaster(
            STOCK_B, "A2",
            STOCK_C, "C2"));

    // For simplicity, this test doesn't use the date, because hardCodedInstrumentMaster doesn't either.
    assertOptionalEquals(STOCK_A, instrumentMaster.getInstrumentId(symbol("A1"), DUMMY_DATE));
    assertOptionalEquals(STOCK_B, instrumentMaster.getInstrumentId(symbol("B1"), DUMMY_DATE));
    assertOptionalEquals(STOCK_C, instrumentMaster.getInstrumentId(symbol("C2"), DUMMY_DATE));
    assertOptionalEmpty(instrumentMaster.getInstrumentId(DUMMY_SYMBOL, DUMMY_DATE));

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
