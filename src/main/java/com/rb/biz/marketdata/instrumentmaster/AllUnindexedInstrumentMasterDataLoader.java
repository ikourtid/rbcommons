package com.rb.biz.marketdata.instrumentmaster;

import java.time.LocalDate;

/**
 * Old comment: "We could load the instrument master data from a file (Jan 2017),
 * but this could be a database or some other mechanism later."
 *
 * As of Nov 2019, that's exactly what happened: we now also are able to load the instrument master
 * from cloud storage as well, not just a file. Therefore, we can't use @ImplementedBy here.
 */
public interface AllUnindexedInstrumentMasterDataLoader {

  /**
   * This is the data that was available on asOfDate.
   * In the case we are simulating the passage of time in backtests,
   * this would mean that we do not have any lookahead and load data that became available after that date.
   */
  AllUnindexedInstrumentMasterData load(LocalDate asOfDate);

}
