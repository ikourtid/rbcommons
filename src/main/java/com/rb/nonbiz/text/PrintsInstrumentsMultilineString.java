package com.rb.nonbiz.text;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;

import java.time.LocalDate;

/**
 * There are many data classes that print {@link InstrumentId} in their toString method.
 *
 * <p> It's clearer if we also print their {@link Symbol} as well. That requires more information
 * ({@link InstrumentMaster} and date). </p>
 *
 * <p> Sometimes an object prints so much information that it's easier to break it up
 * over multiple lines. </p>
 *
 * <p> This interface combines both features; it allows the user to both print {@link Symbol}s
 * and to print over multiple lines. </p>
 *
 * @see PrintsInstruments
 * @see PrintsMultilineString
 */
public interface PrintsInstrumentsMultilineString {

  String toMultilineString(InstrumentMaster instrumentMaster, LocalDate date);

}
