package com.rb.nonbiz.collections;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;

import java.time.LocalDate;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;

/**
 * A bidirectional map between InstrumentId and another type.
 *
 * Note: generic class V must implement equals/hashCode in order for this to have the semantics you want.
 * Otherwise, the RBMap member would just be doing pointer comparisons when looking up an item by value.
 */
public class IidBiMap<V> implements PrintsInstruments {

  private final IidMap<V> itemFromInstrumentId;
  private final RBMap<V, InstrumentId> instrumentIdFromItem;

  private IidBiMap(IidMap<V> itemFromInstrumentId, RBMap<V, InstrumentId> instrumentIdFromItem) {
    this.itemFromInstrumentId = itemFromInstrumentId;
    this.instrumentIdFromItem = instrumentIdFromItem;
  }

  public static <V> IidBiMap<V> iidBiMap(IidMap<V> itemFromInstrumentId) {
    MutableRBMap<V, InstrumentId> mutableMap = newMutableRBMapWithExpectedSize(itemFromInstrumentId.size());
    itemFromInstrumentId.forEachEntry( (instrumentId, item) ->
        mutableMap.putAssumingAbsent(item, instrumentId));
    return new IidBiMap<>(itemFromInstrumentId, newRBMap(mutableMap));
  }

  public IidMap<V> getItemFromInstrumentId() {
    return itemFromInstrumentId;
  }

  public RBMap<V, InstrumentId> getInstrumentIdFromItem() {
    return instrumentIdFromItem;
  }

  public boolean isEmpty() {
    // instrumentIdFromItem.isEmpty() would also return the same result.
    return itemFromInstrumentId.isEmpty();
  }

  public int size() {
    return instrumentIdFromItem.size(); // same as itemFromInstrumentId.size()
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    // No point in printing the reverse order, because there's no new information there.
    return Strings.format("[IBM %s IBM]", itemFromInstrumentId.toString(instrumentMaster, date));
  }

}
