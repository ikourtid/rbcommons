package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.biz.types.asset.InstrumentId;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;

/**
 * Various static utility methods for constructing an {@link IidMap}.
 */
public class IidMapConstructors {

  /**
   * When the key is an InstrumentId, we should always be using an IidMap.
   * However, there are cases where we need to use an RBMap keyed by InstrumentId.
   * One example is generic map classes that can be keyed by anything.
   * This method lets you convert an RBMap keyed by InstrumentId into an IidMap.
   */
  public static <V> IidMap<V> iidMapFromRBMap(RBMap<InstrumentId, V> asRbMap) {
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(asRbMap.size());
    asRbMap.forEachEntry( (instrumentId, v) -> mutableMap.put(instrumentId, v));
    return newIidMap(mutableMap);
  }

  public static <V> IidMap<V> iidMapFromSet(
      IidSet instrumentIds,
      Function<InstrumentId, V> transformer) {
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(instrumentIds.size());
    instrumentIds
        .sortedStream()
        .forEach(instrumentId -> mutableMap.putAssumingAbsent(instrumentId,
            transformer.apply(instrumentId)));
    return newIidMap(mutableMap);
  }

  /**
   * This will also throw if there is more than 1 item with the same InstrumentId.
   */
  public static <V extends HasInstrumentId> IidMap<V> iidMapFromCollection(Collection<V> collection) {
    MutableIidMap<V> mutableIidMap = newMutableIidMapWithExpectedSize(collection.size());
    collection.forEach(v -> mutableIidMap.putAssumingAbsent(v.getInstrumentId(), v));
    return newIidMap(mutableIidMap);
  }

  /**
   * This will also throw if there is more than 1 item with the same InstrumentId.
   */
  public static <V> IidMap<V> iidMapFromCollection(Collection<V> collection, Function<V, InstrumentId> keyExtractor) {
    MutableIidMap<V> mutableIidMap = newMutableIidMapWithExpectedSize(collection.size());
    collection.forEach(v -> mutableIidMap.putAssumingAbsent(keyExtractor.apply(v), v));
    return newIidMap(mutableIidMap);
  }

  /**
   * This will also throw if there is more than 1 item with the same InstrumentId.
   */
  public static <V extends HasInstrumentId> IidMap<V> iidMapFromStream(Stream<V> stream) {
    MutableIidMap<V> mutableIidMap = newMutableIidMap();
    stream.forEach(v -> mutableIidMap.putAssumingAbsent(v.getInstrumentId(), v));
    return newIidMap(mutableIidMap);
  }

  /**
   * Converts a stream into an IidMap by taking every item in the stream and creating both an InstrumentId
   * and an IidMap value for that entry.
   *
   * Use this when you have an idea of how big your stream is, so as to guide the code for better performance.
   *
   * This will also throw if there is more than 1 item with the same InstrumentId.
   */
  public static <V1, V2> IidMap<V2> iidMapFromStreamWithExpectedSize(
      int expectedSize,
      Stream<V1> stream,
      Function<V1, InstrumentId> instrumentIdExtractor,
      Function<V1, V2> valueExtractor) {
    return iidMapFromIteratorWithExpectedSize(expectedSize, stream.iterator(), instrumentIdExtractor, valueExtractor);
  }

  /**
   * Converts an Iterator into an IidMap by taking every item in the stream and creating both an InstrumentId
   * and an IidMap value for that entry.
   *
   * Use this when you have an idea of how big your stream is, so as to guide the code for better performance.
   *
   * This will also throw if there is more than 1 item with the same InstrumentId.
   */
  public static <V1, V2> IidMap<V2> iidMapFromIteratorWithExpectedSize(
      int expectedSize,
      Iterator<V1> iter,
      Function<V1, InstrumentId> instrumentIdExtractor,
      Function<V1, V2> valueExtractor) {
    MutableIidMap<V2> mutableIidMap = newMutableIidMapWithExpectedSize(expectedSize);
    iter.forEachRemaining(v -> mutableIidMap.putAssumingAbsent(instrumentIdExtractor.apply(v), valueExtractor.apply(v)));
    return newIidMap(mutableIidMap);
  }

  /**
   * Converts a stream into an IidMap by taking every item in the stream and creating both an InstrumentId
   * and an IidMap value for that entry.
   *
   * Use this when you don't have an idea of how big your stream is; otherwise, the *WithExpectedSize overload
   * will give you better performance.
   *
   * This will also throw if there is more than 1 item with the same InstrumentId.
   */
  public static <V1, V2> IidMap<V2> iidMapFromStream(
      Stream<V1> stream,
      Function<V1, InstrumentId> instrumentIdExtractor,
      Function<V1, V2> valueExtractor) {
    return iidMapFromIterator(stream.iterator(), instrumentIdExtractor, valueExtractor);
  }

  /**
   * Converts an Iterator into an IidMap by taking every item in the iterator and creating both an InstrumentId
   * and an IidMap value for that entry.
   *
   * Use this when you don't have an idea of how big your iterator is; otherwise, the *WithExpectedSize overload
   * will give you better performance.
   *
   * This will also throw if there is more than 1 item with the same InstrumentId.
   */
  public static <V1, V2> IidMap<V2> iidMapFromIterator(
      Iterator<V1> iter,
      Function<V1, InstrumentId> instrumentIdExtractor,
      Function<V1, V2> valueExtractor) {
    MutableIidMap<V2> mutableIidMap = newMutableIidMap();
    iter.forEachRemaining(v -> mutableIidMap.putAssumingAbsent(instrumentIdExtractor.apply(v), valueExtractor.apply(v)));
    return newIidMap(mutableIidMap);
  }

  /**
   * Create an IidMap by only keeping the non-empty HasInstrumentId in a stream of optionals.
   *
   * This will also throw if there is more than 1 item with the same InstrumentId.
   */
  public static <V extends HasInstrumentId> IidMap<V> iidMapFromStreamOfOptionals(Stream<Optional<V>> stream) {
    MutableIidMap<V> mutableIidMap = newMutableIidMap();
    stream.forEach(v -> v.ifPresent(v1 -> mutableIidMap.putAssumingAbsent(v1.getInstrumentId(), v1)));
    return newIidMap(mutableIidMap);
  }

  /**
   * This will also throw if there is more than 1 item with the same InstrumentId.
   */
  public static <V extends HasInstrumentId> IidMap<V> iidMapFromStreamWithExpectedSize(int expectedSize, Stream<V> stream) {
    MutableIidMap<V> mutableIidMap = newMutableIidMapWithExpectedSize(expectedSize);
    stream.forEach(v -> mutableIidMap.putAssumingAbsent(v.getInstrumentId(), v));
    return newIidMap(mutableIidMap);
  }

  public static <V extends HasInstrumentId> IidMap<V> singletonIidMapOfHasInstrumentId(V onlyItem) {
    return singletonIidMap(onlyItem.getInstrumentId(), onlyItem);
  }

  @SafeVarargs
  public static <V extends HasInstrumentId> IidMap<V> iidMapOfHasInstrumentId(
      V first,
      V second,
      V ... rest) {
    MutableIidMap<V> mutableIidMap = newMutableIidMapWithExpectedSize(2 + rest.length);
    mutableIidMap.putAssumingAbsent(first.getInstrumentId(), first);
    mutableIidMap.putAssumingAbsent(second.getInstrumentId(), second);
    for (V value : rest) {
      mutableIidMap.putAssumingAbsent(value.getInstrumentId(), value);
    }
    return newIidMap(mutableIidMap);
  }

  /**
   * Just like iidFromSet, except that we skip elements for which the transformer returns Optional.empty(),
   * and when it returns Optional.of(X) the resulting map stores X.
   */
  public static <V> IidMap<V> iidMapFromFilteredSet(
      IidSet instrumentIds,
      Function<InstrumentId, Optional<V>> transformer) {
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(instrumentIds.size());
    instrumentIds
        .sortedStream()
        .forEach(instrumentId ->
            transformer.apply(instrumentId)
                .ifPresent(value -> mutableMap.putAssumingAbsent(instrumentId, value)));
    return newIidMap(mutableMap);
  }

}
