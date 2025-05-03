package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;

public interface HasIidSet extends PrintsInstruments {

  IidSet getIidSet();

  default boolean contains(InstrumentId instrumentId) {
    return getIidSet().contains(instrumentId);
  }

}
