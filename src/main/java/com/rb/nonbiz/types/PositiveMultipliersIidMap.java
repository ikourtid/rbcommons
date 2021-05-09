package com.rb.nonbiz.types;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.DoubleMap;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;

import java.time.LocalDate;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;

/**
 * A map of items {@code ->} {@link PositiveMultiplier}, specialized for {@link InstrumentId}
 * because it uses the more efficient IidMap.
 *
 * @see DoubleMap
 * @see PositiveMultipliersIidMap
 */
public class PositiveMultipliersIidMap implements PrintsInstruments {

  private final IidMap<PositiveMultiplier> rawMap;

  private PositiveMultipliersIidMap(IidMap<PositiveMultiplier> rawMap) {
    this.rawMap = rawMap;
  }

  public static PositiveMultipliersIidMap positiveMultipliersIidMap(IidMap<PositiveMultiplier> rawMap) {
    return new PositiveMultipliersIidMap(rawMap);
  }

  public static PositiveMultipliersIidMap singletonPositiveMultipliersIidMap(
      InstrumentId instrumentId, PositiveMultiplier positiveMultiplier) {
    return new PositiveMultipliersIidMap(singletonIidMap(instrumentId, positiveMultiplier));
  }

  public static PositiveMultipliersIidMap emptyPositiveMultipliersIidMap() {
    return positiveMultipliersIidMap(emptyIidMap());
  }

  public IidMap<PositiveMultiplier> getRawMap() {
    return rawMap;
  }

  public int size() {
    return rawMap.size();
  }

  public boolean isEmpty() {
    return rawMap.isEmpty();
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[PMIM %s PMIM]", rawMap.toString(instrumentMaster, date));
  }

}
