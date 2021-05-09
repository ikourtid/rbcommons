package com.rb.biz.marketdata.instrumentmaster;

import com.google.common.collect.Range;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.collections.RBRanges.rangeIsAtLeast;
import static com.rb.nonbiz.collections.RBRanges.rangeIsClosed;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;

/**
 * E.g. if instrument ID 999 had ticker "A" from 2000 to 2005, and then "B" from 2006 onwards,
 * this will represent the symbol (and any other information) during a single period during which it was the same.
 */
public class SingleTimePeriodInstrumentMasterData implements HasInstrumentId {

  private final InstrumentId instrumentId;
  private final Symbol symbol;
  private final Range<LocalDate> dateRange;

  private SingleTimePeriodInstrumentMasterData(InstrumentId instrumentId, Symbol symbol, Range<LocalDate> dateRange) {
    this.instrumentId = instrumentId;
    this.symbol = symbol;
    this.dateRange = dateRange;
  }

  public static SingleTimePeriodInstrumentMasterData singleTimePeriodInstrumentMasterData(
      InstrumentId instrumentId, Symbol symbol, Range<LocalDate> dateRange) {
    RBPreconditions.checkArgument(
        rangeIsClosed(dateRange) || rangeIsAtLeast(dateRange),
        "date range for %s ( %s ) is %s which is neither closed on both ends nor 'at least'",
        symbol, instrumentId, dateRange);
    return new SingleTimePeriodInstrumentMasterData(instrumentId, symbol, dateRange);
  }

  @Override
  public InstrumentId getInstrumentId() {
    return instrumentId;
  }

  public Symbol getSymbol() {
    return symbol;
  }

  public Range<LocalDate> getDateRange() {
    return dateRange;
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[STPIMD %s %s %s STPIMD]", instrumentId, symbol, dateRange);
  }

}
