package com.rb.nonbiz.text;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;

import java.time.LocalDate;

/**
 * There are many data classes that print a bunch of instrument IDs in their toString method.
 * It's clearer if we also print their symbol as well. That requires more information
 * (instrument master and date).
 */
public interface PrintsInstruments {

  String toString(InstrumentMaster instrumentMaster, LocalDate date);

}
