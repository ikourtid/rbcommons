package com.rb.nonbiz.collections;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;
import static java.util.Comparator.comparing;

/**
 * A set of {@link HasInstrumentId} that can be indexed by InstrumentId.
 *
 * <p> Note that whether a {@link HasInstrumentId} object is deemed to exist in the set or not
 * depends solely on its InstrumentId, and not the rest of the object. This helps performance.
 * The restriction is that the user of this must ensure that no two {@link HasInstrumentId} objects stored here
 * are the same. We generally do not implement equals/hashCode, but if we did, we could just use a regular
 * {@link RBSet}, which relies on checking the entire object for equality (equals/hashCode). </p>
 *
 * <p> It uses the instrument-id-specific optimized maps (using GNU Trove). </p>
 *
 * @see HasLongMap
 * @see HasInstrumentIdMap
 */
public class HasInstrumentIdSet<T extends HasInstrumentId> extends HasLongMap<InstrumentId, T>
    implements PrintsInstruments {

  /**
   * Avoid using this; use the static constructors in HasInstrumentIdSets.java
   */
  protected HasInstrumentIdSet(TLongObjectHashMap<T> rawMap) {
    super(rawMap);
  }

  public Iterator<InstrumentId> instrumentIdKeysIterator() {
    return Iterators.transform(keysIterator(), v -> instrumentId(v));
  }

  public Stream<T> stream() {
    return values().stream();
  }

  public Stream<T> sortedStream() {
    return values().stream().sorted(comparing(v -> v.getInstrumentId()));
  }

  public Stream<InstrumentId> sortedInstrumentIdStream() {
    return sortedStream().map(v -> v.getInstrumentId());
  }

  public IidSet toIidSet() {
    return newIidSet(instrumentIdKeysIterator(), size());
  }

  /**
   * Return a subset (not necessarily a proper subset) of the original map that only contains the
   * map entries where the value matches the predicate.
   */
  public HasInstrumentIdSet<T> filterValues(Predicate<T> predicate) {
    TLongObjectHashMap<T> hashMap = new TLongObjectHashMap<>(size());
    TLongObjectIterator<T> iterator = getRawMapUnsafe().iterator();
    while (iterator.hasNext()) {
      iterator.advance(); // this is not standard java iterators, hence the weird notation.
      long instrumentIdAsLong = iterator.key();
      T value = iterator.value();

      if (predicate.test(value)) {
        hashMap.putIfAbsent(instrumentIdAsLong, value);
      }
    }
    return new HasInstrumentIdSet<>(hashMap);
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[HIIS %s entries: %s HIIS]",
        this.size(),
        Joiner.on("; ").join(stream()
            .map(v -> v.toString(instrumentMaster, date))
            .sorted()
            .iterator()));
  }

}
