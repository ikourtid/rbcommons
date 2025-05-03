package com.rb.nonbiz.collections;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;

/**
 * Say we have an {@link IidMapWithGroupings} that has values for instruments {A1, A2, B1, B2},
 * and groups together { A1, A2 } and { B1, B2 }. If so, then this would contain an {@link IidMap} with entries
 * for A1 and A2, and also the {@link HasIidSet} object that describes the grouping { A1, A2 }.
 *
 * <p> The reason we need the grouping is that the {@link IidMap} is also allowed to contain fewer entries;
 * it doesn't have to have one entry per instrument in the {@link HasIidSet} grouping. </p>
 */
public class IidMapForSingleGrouping<V, S extends HasIidSet> implements PrintsInstruments {

  private final IidMap<V> iidMap;
  private final S iidGrouping;

  private IidMapForSingleGrouping(IidMap<V> iidMap, S iidGrouping) {
    this.iidMap = iidMap;
    this.iidGrouping = iidGrouping;
  }

  public static <V, S extends HasIidSet> IidMapForSingleGrouping<V, S> iidMapForSingleGrouping(
      IidMap<V> iidMap, S iidGrouping) {
    iidMap.instrumentIdStream().forEach( instrumentId ->
        RBPreconditions.checkArgument(
            iidGrouping.contains(instrumentId),
            "%s is not in the grouping %s ; map was %s",
            instrumentId, iidGrouping, iidMap));
    return new IidMapForSingleGrouping<>(iidMap, iidGrouping);
  }

  public IidMap<V> getIidMap() {
    return iidMap;
  }

  public S getIidGrouping() {
    return iidGrouping;
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[IMFSG %s %s IMFSG]",
        iidMap.toString(instrumentMaster, date),
        iidGrouping.toString(instrumentMaster, date));
  }

}
