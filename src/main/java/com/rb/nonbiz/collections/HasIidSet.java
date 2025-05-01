package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;

public interface HasIidSet {

  IidSet getIidSet();

  default boolean contains(InstrumentId instrumentId) {
    return getIidSet().contains(instrumentId);
  }

}
