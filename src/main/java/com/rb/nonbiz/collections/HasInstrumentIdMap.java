package com.rb.nonbiz.collections;

import com.google.common.collect.Iterators;
import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.biz.types.asset.InstrumentId;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.HasInstrumentIdMaps.newHasInstrumentIdMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.Pair.pair;

/**
 * <p> A map of {@link HasInstrumentId} to some type V. </p>
 *
 * <p> Upside: it uses the instrument-id-specific optimized maps (using GNU Trove). </p>
 *
 * <p> Downside: you only index the map using an InstrumentId, not the full HasInstrumentId class.
 *    However, that's not bad, since we rarely implement #equals, and we'd need to if we actually wanted to use
 *    the HasInstrumentId implementer class as a key. </p>
 *
 * <p> This is useful for cases where we want to attach additional information to the {@link InstrumentId},
 * without generating 2 maps of iid {@code ->} {@link HasInstrumentId} and iid {@code ->} value. </p>
 *
 * @see HasLongMap
 */
public class HasInstrumentIdMap<T extends HasInstrumentId, V> extends HasLongMap<InstrumentId, Pair<T, V>> {

  /**
   * Avoid using this; use the static constructors in HasInstrumentIdMaps.java
   */
  protected HasInstrumentIdMap(TLongObjectHashMap<Pair<T, V>> rawMap) {
    super(rawMap);
  }

  public Iterator<InstrumentId> instrumentIdKeysIterator() {
    return Iterators.transform(keysIterator(), v -> instrumentId(v));
  }

  public Stream<V> valuesStream() {
    return values().stream().map(v -> v.getRight());
  }

  /**
   * <p> We could have exposed an {@code entrySet()} (to fit with map conventions)
   * but that would need to expose the {@code Pair<T, V>}, and it would be confusing since the key in the map
   * is still the instrument ID, not the entire {@link HasInstrumentId} itself. </p>
   *
   * <p> By only exposing a stream of the contents of this map after each entry is transformed,
   * we can avoid that. There's no mention of a pair below. </p>
   */
  public <T2> Stream<T2> transformedStream(BiFunction<T, V, T2> biFunction) {
    return keysStream().mapToObj(idAsLong -> {
      InstrumentId instrumentId = instrumentId(idAsLong);
      Pair<T, V> pair = getOrThrow(instrumentId);
      return biFunction.apply(pair.getLeft(), pair.getRight());
    });
  }

  public Stream<InstrumentId> sortedInstrumentIdStream() {
    return sortedKeysStream().mapToObj(v -> instrumentId(v));
  }

  public void forEachEntry(BiConsumer<T, V> biConsumer) {
    keysIterator().forEachRemaining(idAsLong -> {
      Pair<T, V> pair = getOrThrow(instrumentId(idAsLong));
      biConsumer.accept(pair.getLeft(), pair.getRight());
    });
  }

  public T getHasInstrumentIdOrThrow(InstrumentId instrumentId, String format, Object...args) {
    return getOrThrow(instrumentId, format, args).getLeft();
  }

  public T getHasInstrumentIdOrThrow(InstrumentId instrumentId) {
    return getOrThrow(instrumentId).getLeft();
  }

  public V getValueOrThrow(InstrumentId instrumentId, String format, Object...args) {
    return getOrThrow(instrumentId, format, args).getRight();
  }

  public V getValueOrThrow(InstrumentId instrumentId) {
    return getOrThrow(instrumentId).getRight();
  }

  public V getValueOrDefault(InstrumentId instrumentId, V defaultValue) {
    return containsKey(instrumentId)
        ? getOrThrow(instrumentId).getRight()
        : defaultValue;
  }

  public void forEachInInstrumentIdOrder(BiConsumer<T, V> biConsumer) {
    forEachInKeyOrder( (idAsLong, pair) -> biConsumer.accept(pair.getLeft(), pair.getRight()));
  }

  public <V2> HasInstrumentIdMap<T, V2> transformEntriesCopy(BiFunction<T, V, V2> transformer) {
    MutableIidMap<Pair<T, V2>> mutableMap = newMutableIidMapWithExpectedSize(size());
    forEachInKeyOrder( (keyLong, value) -> {
      InstrumentId instrumentId = instrumentId(keyLong);
      Pair<T, V> pair = getOrThrow(instrumentId);
      mutableMap.putAssumingAbsent(instrumentId, pair(pair.getLeft(), transformer.apply(pair.getLeft(), pair.getRight())));
    });
    return newHasInstrumentIdMap(mutableMap);
  }

}
