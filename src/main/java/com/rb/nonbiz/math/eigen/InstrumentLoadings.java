package com.rb.nonbiz.math.eigen;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;

import java.time.LocalDate;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;

/**
 * This is just a typesafe wrapper for FactorLoadings, which makes the semantics explicit that this doesn't refer
 * to e.g. a portfolio's factor loadings, or the target allocation's factor loadings.
 */
public class InstrumentLoadings implements HasInstrumentId, PrintsInstruments {

  private final InstrumentId instrumentId;
  private final FactorLoadings loadings;

  private InstrumentLoadings(InstrumentId instrumentId, FactorLoadings loadings) {
    this.instrumentId = instrumentId;
    this.loadings = loadings;
  }

  public static InstrumentLoadings instrumentLoadings(InstrumentId instrumentId, FactorLoadings loadings) {
    return new InstrumentLoadings(instrumentId, loadings);
  }

  @Override
  public InstrumentId getInstrumentId() {
    return instrumentId;
  }

  public FactorLoadings getLoadings() {
    return loadings;
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[IL %s %s %s IL]",
        instrumentId,
        transformOptional(
            instrumentMaster.getSymbol(instrumentId, date),
            v -> v.toString())
            .orElse(instrumentId.toString()),
        loadings);
  }

}
