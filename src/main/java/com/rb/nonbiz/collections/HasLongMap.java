package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.types.HasLongRepresentation;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.LongStream;

/**
 * This is a special implementation of an RBMap (SORT OF) for the cases where the key class
 * implements HasLongRepresentation. In those cases, we can key off of the long, and can therefore use
 * specialized classes which are faster and more space-efficient (which can also result in speed increases
 * due to fewer cache misses, presumably).
 *
 * This is used in IidMap in particular, but can also be used for other cases
 * of HasLongRepresentation (AssetId, Investable, AssetClass, etc.)
 *
 * I originally wanted to make this look just like RBMap, so that the code can be agnostic
 * about whether it's using a special implementation for the cases
 * However, it's just not possible. The GNU Trove objects that we use for the raw underlying map
 * do not really support a java.util.Map-like interface - e.g. an entrySet().
 *
 * Therefore, any code that uses this will need to treat this specially (i.e. not like an RBMap),
 * and also cannot use the various handy transformation functions in RBMap
 * (although it is always possible to implement equivalents - but it can't be the same code).
 *
 * @see RBMap
 * @see HasLongSet
 */
public abstract class HasLongMap<K extends HasLongRepresentation, V> {

  private static final TLongObjectHashMap<?> EMPTY_INSTANCE = new TLongObjectHashMap(0);

  private final TLongObjectHashMap<V> rawMap;

  @SuppressWarnings("unchecked")
  protected HasLongMap(TLongObjectHashMap<V> rawMap) {
    // The following is useful in cases where we start out building a map but never end up adding anything to it.
    // In that case, the original TLongObjectHashMap will still have some size (e.g. 10-20 based on initial
    // default size and capacity). By doing the following, we allow the garbage collector to reclaim that
    // empty (with no items) but not really empty (memory-wise) TLongObjectHashMap.
    // We have the ability to do this here, because HasLongMap is not mutable, so EMPTY_INSTANCE
    // will only shared in a read-only context.
    this.rawMap = rawMap.isEmpty() ? (TLongObjectHashMap<V>) EMPTY_INSTANCE : rawMap;
  }

  @VisibleForTesting
  public TLongObjectHashMap<V> getRawMapUnsafe() {
    return rawMap;
  }

  public int size() {
    return rawMap.size();
  }

  public boolean isEmpty() {
    return rawMap.isEmpty();
  }

  public boolean containsKey(K key) {
    return rawMap.containsKey(key.asLong());
  }

  public boolean containsValue(V value) {
    return rawMap.containsValue(value);
  }

  /**
   * Returns Optional.empty() if there is no value for the key,
   * otherwise Optional.of(value under key).
   */
  public Optional<V> getOptional(K key) {
    if (key == null) {
      throw new IllegalArgumentException("An IidMap does not allow null keys");
    }
    return Optional.ofNullable(rawMap.get(key.asLong()));
  }

  /**
   * Returns the value under the key (if present), otherwise the defaultValue.
   */
  public V getOrDefault(K key, V defaultValue) {
    if (key == null) {
      throw new IllegalArgumentException("An IidMap does not allow null keys");
    }
    V value = rawMap.get(key.asLong());
    return value == null ? defaultValue : value;
  }

  /**
   * Returns the value under the key (if present), otherwise the defaultValue.
   */
  public V getOrDefault(K key, Supplier<V> defaultValue) {
    if (key == null) {
      throw new IllegalArgumentException("An IidMap does not allow null keys");
    }
    V value = rawMap.get(key.asLong());
    return value == null ? defaultValue.get() : value;
  }

  /**
   * Returns the value under the key (if present). Throws an exception otherwise.
   */
  public V getOrThrow(K key) {
    return getOrThrow(key, "Key %s does not exist in map", key);
  }

  /**
   * Returns the value under the key (if present). Throws an exception otherwise, with the specified message.
   */
  public V getOrThrow(K key, String template, Object...args) {
    Optional<V> value = getOptional(key);
    if (!value.isPresent()) {
      throw new IllegalArgumentException(smartFormat(template, args));
    }
    return value.get();
  }

  public Collection<V> values() {
    return rawMap.valueCollection();
  }

  public LongStream keysStream() {
    return LongStream.of(rawMap.keys());
  }

  public LongStream sortedKeysStream() {
    // this looks suspicious because it sorts the keys in place,
    // but #keys returns a copy of the underlying keys, so we're fine.
    // Since keys is a new array that we can modify anyway, we might as well use Arrays.sort;
    // it is probably faster than LongStream#sorted, since the latter probably has to allocate some space
    // to do the sorting and do some copying.
    long[] keys = rawMap.keys();
    Arrays.sort(keys);
    return LongStream.of(keys);
  }

  /**
   * We could presumably run through the map faster if we use the native TLongIterator,
   * but I don't want to expose that in order to keep the code somewhat general.
   */
  public Iterator<Long> keysIterator() {
    // We can't use Iterators.transform here because this is a specialized TLongIterator, not a plain iterator.
    return new Iterator<Long>() {
      TLongIterator rawIterator = rawMap.keySet().iterator();

      @Override
      public boolean hasNext() {
        return rawIterator.hasNext();
      }

      @Override
      public Long next() {
        return rawIterator.next();
      }
    };
  }

  public TLongObjectIterator<V> rawTroveIterator() {
    return rawMap.iterator();
  }

  public void forEach(BiConsumer<Long, V> biConsumer) {
    rawMap.forEachEntry( (keyLong, value) -> {
      biConsumer.accept(keyLong, value);
      return true;
    });
  }

  /**
   * Avoid this; when possible, use IidMap#forEachIidSortedEntry (if applicable)
   */
  public void forEachInKeyOrder(BiConsumer<Long, V> biConsumer) {
    sortedKeysStream().forEach(asLong ->
        biConsumer.accept(asLong, rawMap.get(asLong)));
  }

  @Override
  public String toString() {
    return rawMap.toString();
  }

}
