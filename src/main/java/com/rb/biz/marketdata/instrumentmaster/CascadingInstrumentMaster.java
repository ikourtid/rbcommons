package com.rb.biz.marketdata.instrumentmaster;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBOptionals.findFirstPresentOptional;

/**
 * This acts as a plain {@link InstrumentMaster}, but it looks at 2 or more supplied {@link InstrumentMaster}s
 * in order, and returns the first non-empty value it finds.
 *
 * So it's more like "if there's a value in the first instrument master, return that; then if there's a value in the
 * 2nd, return that; etc".
 *
 * We currently only have explicit static constructors for the case of exactly 2 and 3 instrument masters. If needed, we can
 * add 4+-arg (and more) versions later. We could also add a list argument, but it would be less succinct, because typically
 * when we use this, we will know exactly how many {@link InstrumentMaster}s we would need; it's not a programmatically
 * determined answer. Plus, we don't need to worry about a list of 0 or 1 items.
 */
public class CascadingInstrumentMaster implements InstrumentMaster {

  private final List<InstrumentMaster> instrumentMasterList;

  private CascadingInstrumentMaster(List<InstrumentMaster> instrumentMasterList) {
    this.instrumentMasterList = instrumentMasterList;
  }

  public static CascadingInstrumentMaster cascadingInstrumentMaster(
      InstrumentMaster instrumentMaster1,
      InstrumentMaster instrumentMaster2) {
    return new CascadingInstrumentMaster(ImmutableList.of(instrumentMaster1, instrumentMaster2));
  }

  public static CascadingInstrumentMaster cascadingInstrumentMaster(
      InstrumentMaster instrumentMaster1,
      InstrumentMaster instrumentMaster2,
      InstrumentMaster instrumentMaster3) {
    return new CascadingInstrumentMaster(ImmutableList.of(instrumentMaster1, instrumentMaster2, instrumentMaster3));
  }

  @Override
  public Optional<InstrumentId> getInstrumentId(Symbol symbol, LocalDate effectiveDate) {
    return findFirstPresentOptional(instrumentMasterList, v -> v.getInstrumentId(symbol, effectiveDate));
  }

  @Override
  public Optional<Symbol> getSymbol(InstrumentId instrumentId, LocalDate effectiveDate) {
    return findFirstPresentOptional(instrumentMasterList, v -> v.getSymbol(instrumentId, effectiveDate));
  }

  @Override
  public Optional<Symbol> getLatestValidSymbol(InstrumentId instrumentId, LocalDate effectiveDate) {
    return findFirstPresentOptional(instrumentMasterList, v -> v.getLatestValidSymbol(instrumentId, effectiveDate));
  }

}
