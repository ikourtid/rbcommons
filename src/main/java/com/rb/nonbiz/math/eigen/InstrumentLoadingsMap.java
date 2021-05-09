package com.rb.nonbiz.math.eigen;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBMapPreconditions;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;
import static com.rb.nonbiz.text.Strings.formatIidMapOfPrintsInstruments;

/**
 * A typesafe map of InstrumentId {@literal ->} FactorLoadings.
 */
public class InstrumentLoadingsMap implements PrintsInstruments {

  private final IidMap<InstrumentLoadings> rawMap;

  private InstrumentLoadingsMap(IidMap<InstrumentLoadings> rawMap) {
    this.rawMap = rawMap;
  }

  public static InstrumentLoadingsMap instrumentLoadingsMap(IidMap<InstrumentLoadings> rawMap) {
    RBPreconditions.checkArgument(
        !rawMap.isEmpty(),
        "We do not allow for empty InstrumentLoadingsMap");
    RBMapPreconditions.checkMatchingMapInstrumentIds(rawMap);
    return new InstrumentLoadingsMap(rawMap);
  }

  public IidMap<InstrumentLoadings> getRawMap() {
    return rawMap;
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[ILM %s ILM]", formatIidMapOfPrintsInstruments(rawMap, instrumentMaster, date));
  }

}
