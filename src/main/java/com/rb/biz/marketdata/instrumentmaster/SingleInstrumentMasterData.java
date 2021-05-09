package com.rb.biz.marketdata.instrumentmaster;

import com.google.common.collect.Range;
import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ContiguousDiscreteRangeMap;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;

public class SingleInstrumentMasterData implements HasInstrumentId {

  private final InstrumentId instrumentId;
  private final ContiguousDiscreteRangeMap<LocalDate, SingleTimePeriodInstrumentMasterData> dateToData;

  private SingleInstrumentMasterData(InstrumentId instrumentId,
                                     ContiguousDiscreteRangeMap<LocalDate, SingleTimePeriodInstrumentMasterData> dateToData) {
    this.instrumentId = instrumentId;
    this.dateToData = dateToData;
  }

  public static SingleInstrumentMasterData singleInstrumentMasterData(
      InstrumentId instrumentId,
      ContiguousDiscreteRangeMap<LocalDate, SingleTimePeriodInstrumentMasterData> dateToData) {
    RBPreconditions.checkArgument(
        dateToData.getUnderlyingMap().getRawRangeMap().asMapOfRanges().values()
            .stream()
            .allMatch(stpimd -> stpimd.getInstrumentId().equals(instrumentId)),
        "Not all instruments that show in the contiguous range map have instrument id %s",
        instrumentId);
    RBPreconditions.checkArgument(
        dateToData.getUnderlyingMap().getRawRangeMap().asMapOfRanges().entrySet()
            .stream()
            .allMatch(entry -> {
              Range<LocalDate> range = entry.getKey();
              SingleTimePeriodInstrumentMasterData singlePeriodData = entry.getValue();
              return singlePeriodData.getDateRange().equals(range);
            }),
        "All ranges in the keys and values must match");
    return new SingleInstrumentMasterData(instrumentId, dateToData);
  }

  @Override
  public InstrumentId getInstrumentId() {
    return instrumentId;
  }

  public ContiguousDiscreteRangeMap<LocalDate, SingleTimePeriodInstrumentMasterData> getDateToData() {
    return dateToData;
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[SIMD %s %s SIMD]",
        instrumentMaster.getLatestValidSymbolOrInstrumentIdAsSymbol(instrumentId, date), dateToData);
  }

}
