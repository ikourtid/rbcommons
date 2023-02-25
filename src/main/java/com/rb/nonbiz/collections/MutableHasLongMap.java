package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.HasLongRepresentation;
import com.rb.nonbiz.util.RBPreconditions;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

/**
 * This is one of the rare mutable data classes in the codebase.
 * Typically, we will build a MutableHasLongMap and then 'lock' it into an RBMap in the same method, and then return it.
 * We should (almost) never pass around a MutableHasLongMap.
 */
public class MutableHasLongMap<K extends HasLongRepresentation, V> {

  // optimizes for speed vs space
  protected static final float DEFAULT_LOAD_FACTOR = 0.5f;

  // 10 is the default in many java non-Rowboat-Advisors classes
  protected static final int DEFAULT_INITIAL_SIZE = 10;

  private final TLongObjectHashMap<V> rawMap;

  protected MutableHasLongMap(TLongObjectHashMap<V> rawMap) {
    this.rawMap = rawMap;
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

  TLongObjectHashMap<V> getRawMap() {
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
   * #get on a regular Map returns null if the value is not there. We don't like nulls in the codebase,
   * plus that behavior is confusing to someone new to Java.
   * Instead, MutableHasLongMap has:
   * #getOptional (which will return an Optional.empty() if there is no value for the specified key),
   * #getOrThrow, which assumes the value is there, and returns Optional.of(...)
   *
   * Ideally, since we should never be passing a MutableHasLongMap around, you should use this sparingly
   * and only get values from the 'locked' HasLongMap that you'll convert the MutableHasLongMap to.
   */
  public Optional<V> getOptional(K key) {
    if (key == null) {
      throw new IllegalArgumentException("A MutableHasLongMap does not allow null keys");
    }
    return Optional.ofNullable(rawMap.get(key.asLong()));
  }

  /**
   * Like #getOptional, except that it returns the value if present (or null if absent).
   * We avoid nulls in the code and prefer optionals. However, in a few cases of code that's on the critical path,
   * it's better to use this for performance, so as to avoid instantiation of an Optional.
   */
  public V getOrNull(K key) {
    return rawMap.get(key.asLong());
  }

  /**
   * #get on a regular Map returns null if the value is not there. We don't like nulls in the codebase,
   * plus that behavior is confusing to someone new to Java.
   * Instead, MutableHasLongMap has:
   * #getOptional (which will return an Optional.empty() if there is no value for the specified key),
   * #getOrThrow, which assumes the value is there, and returns Optional.of(...)
   *
   * Ideally, since we should never be passing a MutableHasLongMap around, you should use this sparingly
   * and only get values from the 'locked' HasLongMap that you'll convert the MutableHasLongMap to.
   */
  public V getOrThrow(K key) {
    if (key == null) {
      throw new IllegalArgumentException("A MutableHasLongMap does not allow null keys");
    }
    V value = rawMap.get(key.asLong());
    if (value == null) {
      throw new IllegalArgumentException(smartFormat(
          "no value exists in the map for key %s ; map is %s",
          key, this));
    }
    return value;
  }

  /**
   * Same as getOrThrow above, but lets you specify the error message if a key is missing.
   */
  public V getOrThrow(K key, String template, Object...args) {
    if (key == null) {
      throw new IllegalArgumentException("A MutableHasLongMap does not allow null keys");
    }
    Optional<V> value = getOptional(key);
    RBPreconditions.checkArgument(value.isPresent(), template, args);
    return value.get();
  }

  public V remove(K key) {
    return rawMap.remove(key.asLong());
  }

  public void clear() {
    rawMap.clear();
  }

  /**
   * Get a value for a key.
   * If it doesn't exist, return defaultValue, but don't modify the map to add a key {@code ->} defaultValue mapping.
   */
  public V getOrDefault(K key, V defaultValue) {
    RBPreconditions.checkArgument(
        key != null,
        "A MutableHasLongMap does not allow null keys");
    V value = rawMap.get(key.asLong());
    return value == null ? defaultValue : value;
  }

  /**
   * Get a value for a key.
   * If it doesn't exist, return defaultValue, but don't modify the map to add a key {@code ->} defaultValue mapping.
   */
  public V getOrDefault(K key, Supplier<V> defaultValue) {
    RBPreconditions.checkArgument(
        key != null,
        "A MutableHasLongMap does not allow null keys");
    V value = rawMap.get(key.asLong());
    return value == null ? defaultValue.get() : value;
  }

  /**
   * Get a value for a key. If it doesn't exist, add the key {@code ->} defaultValue mapping, and return defaultValue.
   */
  public V getIfPresentElsePut(K key, Supplier<V> defaultValue) {
    RBPreconditions.checkArgument(
        key != null,
        "A MutableHasLongMap does not allow null keys");
    V value = rawMap.get(key.asLong());
    if (value != null) {
      return value;
    }
    V newValue = defaultValue.get();
    rawMap.put(key.asLong(), newValue);
    return newValue;
  }

  /**
   * There are very few cases where, while constructing a map, you actually want to put a value twice.
   * Use #put if you actually want that. For all other cases, use #putIfAbsent and #putAssumingAbsent.
   *
   * You can also use #put for performance-critical code, where you are OK with the diminished safety of
   * not checking if you are trying to overwrite an existing value.
   */
  public V put(K key, V value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException(smartFormat(
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableHasLongMap",
          key, value));
    }
    return rawMap.put(key.asLong(), value);
  }

  /**
   * Adds a key/value mapping ONLY if there's no value for this key yet.
   *
   * Use this instead of #put for extra safety, when applicable.
   */
  public void putIfAbsent(K key, V value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException(smartFormat(
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableHasLongMap",
          key, value));
    }
    rawMap.putIfAbsent(key.asLong(), value);
  }

  /**
   * Adds a key/value mapping ONLY if there's no value for this key yet.
   *
   * Use this instead of #put for extra safety, when applicable.
   */
  public void putIfAbsent(K key, Supplier<V> valueSupplier) {
    if (key == null) {
      throw new IllegalArgumentException(smartFormat(
          "Key cannot be null in a MutableHasLongMap; value was ( %s ) ",
          valueSupplier.get()));
    }
    if (!rawMap.containsKey(key.asLong())) {
      V value = valueSupplier.get();
      RBPreconditions.checkNotNull(value);
      rawMap.put(key.asLong(), value);
    }
  }

  /**
   * Adds a key/value mapping.
   * Throws if there already is a value for this key.
   *
   * Use this instead of #put for extra safety, when applicable - which is pretty much all the time.
   */
  public void putAssumingAbsent(K key, V value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException(smartFormat(
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableHasLongMap",
          key, value));
    }
    V previousValue = put(key, value);
    if (previousValue != null) {
      throw new IllegalArgumentException(smartFormat(
          "Trying to add value %s to key %s which already maps to %s",
          value, key, previousValue));
    }
  }

  /**
   * Adds a key/value mapping.
   * Throws if there already is a value for this key AND the existing value is not equal
   * (subject to the {@link BiPredicate)} to the one supplied.
   *
   * <p> Use this instead of #put for extra safety, when applicable - which is pretty much all the time. </p>
   */
  public void putAssumingAbsentOrEqual(K key, V value, BiPredicate<V, V> equalityPredicate) {
    if (key == null || value == null) {
      throw new IllegalArgumentException(smartFormat(
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableHasLongMap",
          key, value));
    }
    V previousValue = put(key, value);
    if (previousValue != null && !equalityPredicate.test(previousValue, value)) {
      throw new IllegalArgumentException(smartFormat(
          "Trying to add value %s to key %s which already maps to the (unequal) value of %s",
          value, key, previousValue));
    }
  }

  /**
   * Replace a value under a key.
   * Throws if the key is not already there.
   *
   * Use this instead of #put for extra safety, when applicable.
   */
  public void putAssumingPresent(K key, V value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException(smartFormat(
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableHasLongMap",
          key, value));
    }
    V previousValue = put(key, value);
    if (previousValue == null) {
      throw new IllegalArgumentException(smartFormat(
          "Trying to add value %s to key %s which is not there yet",
          value, key));
    }
  }

  /**
   * This helps you avoid the 'if empty then modify object, else add object' type of logic,
   * e.g. if you have a map whose values are e.g. numbers that need to be added to,
   * and where the first time around there's nothing in the map.
   */
  public void putOrModifyExisting(K key, V value, BinaryOperator<V> whenPresent) {
    Optional<V> existingValue = getOptional(key);
    put(key, existingValue.isPresent() ? whenPresent.apply(existingValue.get(), value) : value);
  }

  /**
   * Similar to putOrModifyExisting, except that this is most useful for situations where you always want to initialize
   * with a non-trivial object (e.g. an empty array list), and then perform operations on it.
   * E.g. {@code MutableHasLongMap<AssetClass, List<?>>}
   * We use a {@code Supplier<V>} instead of just V, so that we won't always need to initialize e.g. a newArrayList()
   * even for those cases where there's already a value under the key.
   *
   * Unlike eitherInitializeOrUpdate, if we need to initialize the value for this key, we will still call the update
   * operator.
   */
  public void possiblyInitializeAndThenUpdate(K key, Supplier<V> initialValueIfNoneExists, UnaryOperator<V> modifierIfValueExists) {
    Optional<V> maybeExisting = getOptional(key);
    put(key, modifierIfValueExists.apply(maybeExisting.isPresent()
        ? maybeExisting.get()
        : initialValueIfNoneExists.get()));
  }

  /**
   * Similar to the previous, except that the 'modifier' function does not return anything.
   * This is handy in cases where the modification e.g. appends to a list.
   * For example, in a mutable map of string to a mutable collection (e.g. list or another mutable map),
   * you don't really need to call put(key, collection) to insert a new collection; instead, all you need to do is
   * modify the collection.
   *
   * Here is an example where this overload is easier to read than the previous one:
   * <pre>
   * {@code
   * mutableLotsMap.possiblyInitializeAndThenUpdateInPlace(
   *                     taxLot.getInstrumentId(),
   *                     () -> newArrayList(),
   *                     list -> list.add(taxLot))
   *                     }
   * </pre>
   *
   * previously, the last line would have been the following, which is harder to read, even when properly indented:
   * <pre>
   *              {@code list -> { list.add(taxLot); return list; })}
   * </pre>
   */
  public void possiblyInitializeAndThenUpdateInPlace(K key, Supplier<V> initialEmptyValueIfNoneExists, Consumer<V> modifierInPlace) {
    Optional<V> maybeExisting = getOptional(key);
    if (maybeExisting.isPresent()) {
      modifierInPlace.accept(maybeExisting.get());
    } else {
      V initial = initialEmptyValueIfNoneExists.get();
      modifierInPlace.accept(initial);
      put(key, initial);
    }
  }

  /**
   * Similar to possiblyInitializeAndThenUpdate, except that we EITHER initialize if there is no existing value,
   * OR modify the existing value. If we have to initialize, we will NOT additionally modify the initialized value afterwards.
   */
  public void eitherInitializeOrUpdate(K key, Supplier<V> initialValueIfNoneExists, UnaryOperator<V> modifierIfValueExists) {
    Optional<V> maybeExisting = getOptional(key);
    put(key, maybeExisting.isPresent()
        ? modifierIfValueExists.apply(maybeExisting.get())
        : initialValueIfNoneExists.get());
  }

  @Override
  public String toString() {
    return rawMap.toString();
  }

}
