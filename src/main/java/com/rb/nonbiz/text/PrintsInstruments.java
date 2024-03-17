package com.rb.nonbiz.text;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.date.RBDates;

import java.time.LocalDate;

/**
 * There are many data classes that print {@link InstrumentId} in their toString method.
 *
 * <p> It's clearer if we also print their symbol as well. That requires more information
 * (instrument master and date). </p>
 *
 * <p> Typically, for a data class that implements {@link PrintsInstruments}, we implement a standard
 * (parameterless) toString method, which then calls {@link PrintsInstruments#toString(InstrumentMaster, LocalDate)}
 * using as parameters {@link NullInstrumentMaster} and {@link RBDates#UNUSED_DATE}. This is a very common pattern
 * and appears 300+ times (as of March 2024) in the entire Rowboat codebase. </p>
 */
public interface PrintsInstruments {

  String toString(InstrumentMaster instrumentMaster, LocalDate date);

}
