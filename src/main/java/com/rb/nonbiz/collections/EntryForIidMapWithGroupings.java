package com.rb.nonbiz.collections;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;

/**
 * Say we have an {@link IidMapWithGroupings} that has values for instruments {A1, A2, B1, B2},
 * and groups together { A1, A2 } and { B1, B2 }. If so, then this would contain an {@link IidMap} with entries
 * for A1 and A2, and also the {@link HasIidSet} object that describes the grouping { A1, A2 }.
 *
 * <p> The reason we need the grouping is that the {@link IidMap} is also allowed to contain fewer entries;
 * it doesn't have to have one entry per instrument in the {@link HasIidSet} grouping. </p>
 */
public class EntryForIidMapWithGroupings<V, S extends HasIidSet> {

  private final IidMap<V> iidMap;
  private final S iidGrouping;

  private EntryForIidMapWithGroupings(IidMap<V> iidMap, S iidGrouping) {
    this.iidMap = iidMap;
    this.iidGrouping = iidGrouping;
  }

  public static <V, S extends HasIidSet> EntryForIidMapWithGroupings<V, S> entryForIidMapWithGroupings(
      IidMap<V> iidMap, S iidGrouping) {
    iidMap.instrumentIdStream().forEach( instrumentId ->
        RBPreconditions.checkArgument(
            iidGrouping.contains(instrumentId),
            "%s is not in the grouping %s ; map was %s",
            instrumentId, iidGrouping, iidMap));
    return new EntryForIidMapWithGroupings<>(iidMap, iidGrouping);
  }

  public IidMap<V> getIidMap() {
    return iidMap;
  }

  public S getIidGrouping() {
    return iidGrouping;
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return "";
  }

}
