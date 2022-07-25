package com.rb.nonbiz.text;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;

import java.time.LocalDate;

/**
 * There are many data classes that print {@link InstrumentId} in their toString method.
 *
 * <p> It's clearer if we also print their symbol as well. That requires more information
 * (instrument master and date). </p>
 */
public interface PrintsInstruments {

  String toString(InstrumentMaster instrumentMaster, LocalDate date);

}
