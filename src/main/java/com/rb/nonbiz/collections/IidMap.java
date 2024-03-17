package com.rb.nonbiz.collections;

import com.google.common.collect.Iterators;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.types.LongCounter;
import com.rb.nonbiz.util.RBSimilarityPreconditions;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSetInOrder;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableIidSet.newMutableIidSetWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;
import static com.rb.nonbiz.text.Strings.formatIidMap;
import static com.rb.nonbiz.types.LongCounter.longCounter;
import static java.util.Comparator.comparing;

/**
 * A specialized map for the cases where the key is an {@link InstrumentId}.
 *
 * <p> Note that this is not exactly like an {@link RBMap}, and has somewhat unorthodox ways to iterate over it
 * (unlike RBMap, which sort of looks like a java.util.Map). </p>
 *
 * @see HasLongMap
 */
public class IidMap<V> extends HasLongMap<InstrumentId, V> implements PrintsInstruments {

  protected IidMap(TLongObjectHashMap<V> rawMap) {
    super(rawMap);
  }
  private transient IidSet keySet = null; // stored so we don't have to recompute it multiple times
  private transient Boolean sorted = false;

  public Iterator<InstrumentId> instrumentIdKeysIterator() {
    return Iterators.transform(this.keysIterator(), v -> instrumentId(v));
  }

  public IidSet keySet() {
    // We first check outside the synchronization, so for the most cases where keySet is NOT null,
    // we won't bother acquiring the lock. This is just a small performance optimization.
    if (keySet == null) {
      synchronized (this) {
        if (keySet == null) {
          MutableIidSet mutableKeySet = newMutableIidSetWithExpectedSize(size());
          instrumentIdKeysIterator().forEachRemaining(instrumentId -> mutableKeySet.add(instrumentId));
          keySet = newIidSet(mutableKeySet);
        }
      }
    }
    return keySet;
  }

  public Stream<InstrumentId> instrumentIdStream() {
    // It would be nice for this method to return keys in order (even though we don't guarantee it).
    // If keySet exists, we return that, because it is an IidSet, and IidSets cache any internal ordering
    // whenever it gets requested.

    // The following shouldn't be a problem with synchronization:
    return keySet != null
        // If we already have keySet available, we might as well return that (as a stream).
        // This shouldn't cause a problem with synchronization, because if keySet isn't null,
        // then it won't be modified again.
        // Note that "Reads and writes are atomic for reference variables":
        // https://docs.oracle.com/javase/tutorial/essential/concurrency/atomic.html
        // This means that we can't have some other thread give "half a pointer value" to keySet.
        ? keySet.stream()

        // ... and if keySet is null, no harm done; we'll just construct a stream on the fly using keysStream
        // which is a LongStream.
        : super.keysStream().mapToObj(v -> instrumentId(v));
  }

  public Stream<InstrumentId> sortedInstrumentIdStream() {
    // We first check outside the synchronization, so for the most cases where keySet is NOT null,
    // we won't bother acquiring the lock. This is just a small performance optimization.
    if (!sorted) {
      synchronized(this) {
        if (!sorted) {
          // We don't care if keySet is already cached as unsorted; we'll overwrite it
          // with a version (newIidSetInOrder) that already pre-caches sorted instrument IDs.
          keySet = newIidSetInOrder(sortedKeysStream()
              .mapToObj(v -> instrumentId(v))
              .collect(Collectors.toList()));
          sorted = true;
        }
      }
    }
    return keySet.stream(); // keySet is now sorted
  }

  public <V2> Stream<V2> toTransformedValuesStream(Function<V, V2> transformer) {
    return instrumentIdStream()
        .map(instrumentId -> transformer.apply(getOrThrow(instrumentId)));
  }

  public <V2> Stream<V2> toTransformedEntriesStream(BiFunction<InstrumentId, V, V2> transformer) {
    return instrumentIdStream()
        .map(instrumentId -> transformer.apply(instrumentId, getOrThrow(instrumentId)));
  }

  public void forEachEntry(BiConsumer<InstrumentId, V> biConsumer) {
    instrumentIdStream()
        .forEach(instrumentId -> biConsumer.accept(instrumentId, getOrThrow(instrumentId)));
  }

  /**
   * Sorts by increasing instrument ID. For general sorting, use the 2-arg overload.
   */
  public Stream<V> toIidSortedStream() {
    return sortedInstrumentIdStream()
        .map(instrumentId -> getOrThrow(instrumentId));
  }

  public <V2> Stream<V2> toIidSortedTransformedValuesStream(Function<V, V2> transformer) {
    return sortedInstrumentIdStream()
        .map(instrumentId -> transformer.apply(getOrThrow(instrumentId)));
  }

  public <V2> Stream<V2> toIidSortedTransformedEntriesStream(BiFunction<InstrumentId, V, V2> transformer) {
    return sortedInstrumentIdStream()
        .map(instrumentId -> transformer.apply(instrumentId, getOrThrow(instrumentId)));
  }

  /**
   * Sorts by an arbitrary comparator that only looks at value.
   * For simple sorting by instrument ID, use the 1-arg overload.
   */
  public <V2> Stream<V2> toSortedTransformedEntriesStream(
      BiFunction<InstrumentId, V, V2> transformer,
      Comparator<V> valuesComparator) {
    return instrumentIdStream()
        .map(instrumentId -> pair(instrumentId, getOrThrow(instrumentId)))
        .sorted( (entry1, entry2) -> valuesComparator.compare(entry1.getRight(), entry2.getRight()))
        .map(entry -> transformer.apply(entry.getLeft(), entry.getRight()));
  }

  /**
   * Sorts by an arbitrary entry comparator.
   * First transform a stream of V to a stream of pair(InstrumentId, V3).
   * Then sort the V3 values using a supplied comparator.
   * Finally, converts the sorted stream to a stream of V2 using a separate transformer.
   */
  public <V2, V3> Stream<V2> toSortedTransformedEntriesStream(
      BiFunction<InstrumentId, V, V2> transformer,
      BiFunction<InstrumentId, V, V3> comparatorTransformer,
      Comparator<V3> comparator) {
    return instrumentIdStream()
        .map(instrumentId -> pair(instrumentId, comparatorTransformer.apply(instrumentId, getOrThrow(instrumentId))))
        .sorted(comparing(pair -> pair.getRight(), comparator))
        .map(pair -> transformer.apply(pair.getLeft(), getOrThrow(pair.getLeft())));
  }

  /**
   * Goes in increasing instrument ID order. For general sorting, use the 2-arg overload.
   */
  public void forEachIidSortedEntry(BiConsumer<InstrumentId, V> biConsumer) {
    sortedInstrumentIdStream()
        .forEach(instrumentId -> biConsumer.accept(instrumentId, getOrThrow(instrumentId)));
  }

  /**
   * Processes each IidMap value in increasing instrument ID key order.
   */
  public void forEachIidSortedValue(Consumer<V> consumer) {
    sortedInstrumentIdStream()
        .forEach(instrumentId -> consumer.accept(getOrThrow(instrumentId)));
  }

  /**
   * Goes through the entries using a comparator that sorts based on both instrument ID and value.
   */
  public void forEachSortedEntry(
      BiConsumer<InstrumentId, V> biConsumer,
      Comparator<Pair<InstrumentId, V>> mapEntriesComparator) {
    instrumentIdStream()
        .map(instrumentId -> pair(instrumentId, getOrThrow(instrumentId)))
        .sorted(mapEntriesComparator)
        .forEach(entry -> biConsumer.accept(entry.getLeft(), entry.getRight()));
  }

  /**
   * Goes through the entries using a value sorting order.
   */
  public void forEachValueSortedEntry(BiConsumer<InstrumentId, V> biConsumer, Comparator<V> valuesComparator) {
    instrumentIdStream()
        .map(instrumentId -> pair(instrumentId, getOrThrow(instrumentId)))
        .sorted( (entry1, entry2) -> valuesComparator.compare(entry1.getRight(), entry2.getRight()))
        .forEach(entry -> biConsumer.accept(entry.getLeft(), entry.getRight()));
  }

  /**
   * Goes through the entries using a key sorting order.
   */
  public void forEachKeySortedEntry(BiConsumer<InstrumentId, V> biConsumer, Comparator<InstrumentId> keysComparator) {
    instrumentIdStream()
        .map(instrumentId -> pair(instrumentId, getOrThrow(instrumentId)))
        .sorted( (entry1, entry2) -> keysComparator.compare(entry1.getLeft(), entry2.getLeft()))
        .forEach(entry -> biConsumer.accept(entry.getLeft(), entry.getRight()));
  }

  /**
   * Avoid using this, unless you're dealing with code that's not InstrumentId-specific and e.g. uses
   * {@code <K extends Investable>}. Example (Sep 2018): MultiItemQualityOfReturns
   */
  public RBMap<InstrumentId, V> toRBMap() {
    MutableRBMap<InstrumentId, V> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (instrumentId, value) -> mutableMap.put(instrumentId, value));
    return newRBMap(mutableMap);
  }

  public RBMap<String, V> toStringKeyedRBMap() {
    MutableRBMap<String, V> mutableMap = newMutableRBMapWithExpectedSize(size());
    forEachEntry( (instrumentId, value) -> mutableMap.put(Long.toString(instrumentId.asLong()), value));
    return newRBMap(mutableMap);
  }

  /**
   * Creates a new map whose keys are the same, and whose values are a transformation
   * of the values of the original map, when you don't care about they key when doing the transformation.
   * ADDITIONALLY, it may filter keys so that the final map may be smaller than the original.
   */
  public <V1> IidMap<V1> filterKeysAndTransformValuesCopy(
      Function<V, V1> valueTransformer,
      Predicate<InstrumentId> mustKeepKey) {
    return filterKeysAndTransformEntriesCopy(
        (ignoredInstrumentId, value) -> valueTransformer.apply(value),
        mustKeepKey);
  }

  /**
   * Creates a new map whose keys are the same, and whose values are a transformation
   * of the entries (key + value) of the original map.
   * ADDITIONALLY, it may filter keys so that the final map may be smaller than the original.
   */
  public <V1> IidMap<V1> filterKeysAndTransformEntriesCopy(
      BiFunction<InstrumentId, V, V1> valueTransformer,
      Predicate<InstrumentId> mustKeepKey) {
    MutableIidMap<V1> mutableMap = newMutableIidMapWithExpectedSize(size());
    forEachEntry( (instrumentId, originalValue) -> {
      if (!mustKeepKey.test(instrumentId)) {
        return;
      }
      V1 transformedValue = valueTransformer.apply(instrumentId, originalValue);
      mutableMap.putAssumingAbsent(instrumentId, transformedValue);
    });
    return newIidMap(mutableMap);
  }

  /**
   * Creates a new map whose keys are the same, and whose values are a transformation
   * of the values of the original map.
   * Additionally, it filters on the transformed values, so the final map may be smaller
   * than the original.
   */
  public <V1> IidMap<V1> transformAndFilterValuesCopy(
      Function<V, V1> valueTransformer,
      Predicate<V1> mustKeepTransformedValue) {
    MutableIidMap<V1> mutableMap = newMutableIidMapWithExpectedSize(size());
    forEachEntry( (instrumentId, originalValue) -> {
      V1 transformedValue = valueTransformer.apply(originalValue);
      if (!mustKeepTransformedValue.test(transformedValue)) {
        return;
      }
      mutableMap.putAssumingAbsent(instrumentId, transformedValue);
    });
    return newIidMap(mutableMap);
  }

  public IidMap<V> filterKeys(Predicate<InstrumentId> mustKeepKey) {
    // size() is passed as upper bound. If we do any filtering, it won't be the final size.
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(size());
    instrumentIdKeysIterator().forEachRemaining(instrumentId -> {
      if (mustKeepKey.test(instrumentId)) {
        mutableMap.putAssumingAbsent(instrumentId, getOrThrow(instrumentId));
      }
    });
    return newIidMap(mutableMap);
  }

  public IidMap<V> filterValues(Predicate<V> mustKeepValue) {
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(size());
    forEachEntry( (instrumentId, value) -> {
      if (mustKeepValue.test(value)) {
        mutableMap.putAssumingAbsent(instrumentId, value);
      }
    });
    return newIidMap(mutableMap);
  }

  public <V2> IidMap<V2> transformValuesCopy(Function<V, V2> transformer) {
    MutableIidMap<V2> mutableMap = newMutableIidMapWithExpectedSize(size());
    forEachEntry( (instrumentId, value) -> mutableMap.putAssumingAbsent(instrumentId, transformer.apply(value)));
    return newIidMap(mutableMap);
  }

  /**
   * Creates a new map whose keys AND values are a transformation of the original ones,
   * and the key transformation doesn't depend on the value in any particular entry, and also
   * the value transformation doesn't depend on the key in any particular entry.
   */
  public <K1, V1> RBMap<K1, V1> transformKeysAndValuesCopy(
      Function<InstrumentId, K1> keyTransformer,
      Function<V, V1> valuesTransformer) {
    MutableRBMap<K1, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    instrumentIdKeysIterator().forEachRemaining(instrumentId -> mutableMap.putAssumingAbsent(
        keyTransformer.apply(instrumentId), valuesTransformer.apply(getOrThrow(instrumentId))));
    return newRBMap(mutableMap);
  }

  /**
   * Creates a new map whose keys AND values are a transformation of the original ones,
   * and the key transformation doesn't depend on the value in any particular entry, and also
   * the value transformation doesn't depend on the key in any particular entry.
   */
  public <K1, V1> RBMap<K1, V1> orderedTransformKeysAndValuesCopy(
      Function<InstrumentId, K1> keyTransformer,
      Function<V, V1> valuesTransformer) {
    MutableRBMap<K1, V1> mutableMap = newMutableRBMapWithExpectedSize(size());
    sortedInstrumentIdStream().iterator().forEachRemaining(instrumentId -> mutableMap.putAssumingAbsent(
        keyTransformer.apply(instrumentId), valuesTransformer.apply(getOrThrow(instrumentId))));
    return newRBMap(mutableMap);
  }

  public <V2> IidMap<V2> transformEntriesCopy(BiFunction<InstrumentId, V, V2> transformer) {
    MutableIidMap<V2> mutableMap = newMutableIidMapWithExpectedSize(size());
    forEachEntry( (instrumentId, value) ->
        mutableMap.putAssumingAbsent(instrumentId, transformer.apply(instrumentId, value)));
    return newIidMap(mutableMap);
  }

  public <V2> IidMap<V2> transformEntriesAndFilterValuesCopy(
      BiFunction<InstrumentId, V, V2> transformer,
      Predicate<V2> mustKeepTransformedValue) {
    MutableIidMap<V2> mutableMap = newMutableIidMapWithExpectedSize(size());
    forEachEntry( (instrumentId, value) -> {
      V2 transformedValue = transformer.apply(instrumentId, value);
      if (mustKeepTransformedValue.test(transformedValue)) {
        mutableMap.putAssumingAbsent(instrumentId, transformer.apply(instrumentId, value));
      }
    });
    return newIidMap(mutableMap);
  }

  public <V2> IidMap<V2> transformEntriesToOptionalAndKeepIfPresentCopy(
      BiFunction<InstrumentId, V, Optional<V2>> transformer) {
    MutableIidMap<V2> mutableMap = newMutableIidMapWithExpectedSize(size());
    forEachEntry( (instrumentId, value) -> {
      Optional<V2> transformedValue = transformer.apply(instrumentId, value);
      transformedValue.ifPresent(v -> mutableMap.putAssumingAbsent(instrumentId, v));
    });
    return newIidMap(mutableMap);
  }

  /**
   * This does NOT modify this RBMap! It creates a copy, but with one key removed.
   */
  public IidMap<V> copyWithInstrumentIdRemoved(InstrumentId toRemove) {
    return copyWithPresentInstrumentIdsRemoved(singletonIidSet(toRemove));
  }

  /**
   * This does NOT modify this RBMap! It creates a copy, but with some keys removed.
   * However, if toRemove is empty, we return the same IidMap as a performance optimization.
   * Since IidMap is immutable, that's fine to do.
   *
   * Throws if one of the presentKeysToRemove is not in the map.
   * ('present' here means 'assume it's present').
   */
  public IidMap<V> copyWithPresentInstrumentIdsRemoved(IidSet presentKeysToRemove) {
    return copyWithInstrumentIdsRemovedHelper(presentKeysToRemove, true);
  }

  /**
   * This does NOT modify this RBMap! It creates a copy, but with some keys removed.
   * However, if toRemove is empty, we return the same IidMap as a performance optimization.
   * Since IidMap is immutable, that's fine to do.
   *
   * Does not throw if one of the keysToRemove is not in the map.
   */
  public IidMap<V> copyWithPossiblyAbsentInstrumentIdsRemoved(IidSet presentKeysToRemove) {
    return copyWithInstrumentIdsRemovedHelper(presentKeysToRemove, false);
  }

  private IidMap<V> copyWithInstrumentIdsRemovedHelper(IidSet toRemove, boolean throwOnMissingKeys) {
    if (toRemove.isEmpty()) {
      return this;
    }
    // The size hint is just a hint; we could either guess high with size() and possibly waste memory,
    // or low with size() - toRemove.size() and possibly take more time to build the map.
    // This is because we don't know the InstrumentIds in toRemove are all disjoint from this IidMap.
    int sizeHint = size();
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(sizeHint);
    LongCounter totalRemoved = longCounter();
    forEachEntry( (instrumentId, value) -> {
      if (toRemove.contains(instrumentId)) {
        totalRemoved.increment();
      } else {
        mutableMap.putAssumingAbsent(instrumentId, value);
      }
    });
    if (throwOnMissingKeys) {
      RBSimilarityPreconditions.checkBothSame(
          // We need both of the following to be of the same type for checkBothSame().
          // We could convert the following (a long) to an int, but that would incur the cost of checking for overflow.
          totalRemoved.get(),
          // Instead, we cast the following to long, which doesn't require an overflow checks.
          (long) toRemove.size(),
          "Not all keys requested to be removed are in the map");
    }
    return newIidMap(mutableMap);
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return formatIidMap(this, instrumentMaster, date);
  }

}
