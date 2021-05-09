package com.rb.biz.types.asset;

import com.rb.nonbiz.text.PrintsInstruments;

/**
 * Useful for data classes that pertain to a single {@link InstrumentId}.
 *
 * <p> E.g. a buy order is for a single instrument; a tax lot has a single instrument; etc.
 *
 * @see InstrumentId
 */
public interface HasInstrumentId extends HasInvestable<InstrumentId>, PrintsInstruments {

  InstrumentId getInstrumentId();

  @Override
  default InstrumentId getInvestable() {
    return getInstrumentId();
  }

}
