package com.rb.nonbiz.collections;

import com.google.common.collect.Maps;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

/**
 * This is one of the rare mutable data classes in the codebase.
 * Typically, we will build a {@link MutableRBMap} and then 'lock' it into an {@link RBMap} in the same method,
 * and then return it. We should (almost) never pass around a {@link MutableRBMap}.
 */
public class MutableRBMap<K, V> {

  private final Map<K, V> rawMap;

  private MutableRBMap(Map<K, V> rawMap) {
    this.rawMap = rawMap;
  }

  public static <K, V> MutableRBMap<K, V> newMutableRBMap() {
    return new MutableRBMap<>(Maps.newHashMap());
  }

  public static <K, V> MutableRBMap<K, V> newMutableRBMapWithExpectedSize(int expectedSize) {
    return new MutableRBMap<>(Maps.newHashMapWithExpectedSize(expectedSize));
  }

  public static <K, V> MutableRBMap<K, V> newMutableRBMap(RBSet<? extends K> keys, Supplier<V> value) {
    MutableRBMap<K, V> mutableRBMap = new MutableRBMap<>(Maps.newHashMapWithExpectedSize(keys.size()));
    keys.forEach(key -> mutableRBMap.put(key, value.get()));
    return mutableRBMap;
  }

  public static <K, V> MutableRBMap<K, V> newMutableRBMap(RBMap<K, V> initialValues) {
    MutableRBMap<K, V> mutableMap = newMutableRBMapWithExpectedSize(initialValues.size());
    mutableMap.addAllAssumingNoOverlap(initialValues);
    return mutableMap;
  }

  /**
   * Creates a copy of this map, but also adds the contents of the specified map.
   * Throws if any key appears in both maps.
   */
  public void addAllAssumingNoOverlap(RBMap<K, V> additionalValues) {
    additionalValues.forEachEntry( (key, value) -> putAssumingAbsent(key, value));
  }

  public Map<K, V> asMap() {
    return rawMap;
  }

  public int size() {
    return rawMap.size();
  }

  public boolean isEmpty() {
    return rawMap.isEmpty();
  }

  public boolean containsKey(K key) {
    return rawMap.containsKey(key);
  }

  public boolean containsValue(V value) {
    return rawMap.containsValue(value);
  }

  /**
   * #get on a regular Map returns null if the value is not there. We don't like nulls in the codebase,
   * plus that behavior is confusing to someone new to Java.
   * Instead, {@link MutableRBMap} has:
   * {@link #getOptional(Object)} (which will return an Optional.empty() if there is no value for the specified key),
   * {@link #getOrThrow(Object)}, which assumes the value is there (throws otherwise), and returns the value
   *
   * <p> Ideally, since we should never be passing a {@link MutableRBMap} around, you should use this sparingly
   * and only get values from the 'locked' {@link RBMap} that you'll convert the {@link MutableRBMap} to. </p>
   */
  public Optional<V> getOptional(K key) {
    checkKeyIsNotNull(key);
    return Optional.ofNullable(rawMap.get(key));
  }

  /**
   * #get on a regular Map returns null if the value is not there. We don't like nulls in the codebase,
   * plus that behavior is confusing to someone new to Java.
   * Instead, MutableRBMap has:
   * {@link #getOptional(Object)} (which will return an Optional.empty() if there is no value for the specified key),
   * {@link #getOrThrow(Object)}, which assumes the value is there (throws otherwise), and returns the value
   *
   * <p> Ideally, since we should never be passing a MutableRBMap around, you should use this sparingly
   * and only get values from the 'locked' RBMap that you'll convert the MutableRBMap to. </p>
   */
  public V getOrThrow(K key) {
    checkKeyIsNotNull(key);
    V value = rawMap.get(key);
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
    checkKeyIsNotNull(key);
    Optional<V> value = getOptional(key);
    RBPreconditions.checkArgument(value.isPresent(), template, args);
    return value.get();
  }

  public V remove(K key) {
    return rawMap.remove(key);
  }

  public void putAll(Map<? extends K, ? extends V> m) {
    rawMap.putAll(m);
  }

  public void clear() {
    rawMap.clear();
  }

  public Set<K> keySet() {
    return rawMap.keySet();
  }

  public Collection<V> values() {
    return rawMap.values();
  }

  public Set<Map.Entry<K, V>> entrySet() {
    return rawMap.entrySet();
  }

  /**
   * Get a value for a key.
   * If it doesn't exist, return defaultValue, but don't modify the map to add a key {@code ->} defaultValue mapping.
   */
  public V getOrDefault(K key, V defaultValue) {
    RBPreconditions.checkArgument(
        key != null,
        "A MutableRBMap does not allow null keys");
    return rawMap.getOrDefault(key, defaultValue);
  }

  /**
   * Get a value for a key.
   * If it doesn't exist, return defaultValue, but don't modify the map to add a key {@code ->} defaultValue mapping.
   */
  public V getOrDefault(K key, Supplier<V> defaultValue) {
    RBPreconditions.checkArgument(
        key != null,
        "A MutableRBMap does not allow null keys");
    V value = rawMap.get(key);
    return value == null
        ? defaultValue.get()
        : value;
  }

  /**
   * Get a value for a key. If it doesn't exist, add the key {@code ->} defaultValue mapping, and return defaultValue.
   */
  public V getIfPresentElsePut(K key, Supplier<V> defaultValue) {
    RBPreconditions.checkArgument(
        key != null,
        "A MutableRBMap does not allow null keys");
    V value = rawMap.get(key);
    if (value != null) {
      return value;
    }
    V newValue = defaultValue.get();
    rawMap.put(key, newValue);
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
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableRBMap",
          key, value));
    }
    return rawMap.put(key, value);
  }

  /**
   * Adds a key/value mapping ONLY if there's no value for this key yet.
   *
   * Use this instead of #put for extra safety, when applicable.
   */
  public void putIfAbsent(K key, V value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException(smartFormat(
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableRBMap",
          key, value));
    }
    rawMap.putIfAbsent(key, value);
  }

  /**
   * Adds a key/value mapping ONLY if there's no value for this key yet.
   *
   * Use this instead of #put for extra safety, when applicable.
   */
  public void putIfAbsent(K key, Supplier<V> valueSupplier) {
    if (key == null) {
      throw new IllegalArgumentException(smartFormat(
          "Key cannot be null in a MutableRBMap; value was ( %s ) ",
          valueSupplier.get()));
    }
    if (!rawMap.containsKey(key)) {
      V value = valueSupplier.get();
      RBPreconditions.checkNotNull(value);
      rawMap.putIfAbsent(key, value);
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
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableRBMap",
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
   * Throws if there already is a value for this key.
   *
   * Use this instead of #put for extra safety, when applicable - which is pretty much all the time.
   */
  public void putAssumingAbsentAllowingNullValue(K key, V value) {
    if (key == null) {
      throw new IllegalArgumentException(smartFormat(
          "Key ( %s ) cannot be null in a MutableRBMap; value was %s",
          key, value));
    }
    V previousValue = rawMap.put(key, value);
    if (previousValue != null) {
      throw new IllegalArgumentException(smartFormat(
          "Trying to add value %s to key %s which already maps to %s",
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
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableRBMap",
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
   * Adds a key/value mapping.
   *
   * <p> If a value already exists for the supplied key, it will not get replaced, but this will throw an
   * exception if the new value is different than the existing value per the predicate supplied.
   * This is useful for objects that do not implement equals/hashCode, and for which we need to pass in a lambda
   * to define what equality means. It is also useful for objects that do implement equals/hashCode, but for which
   * we want to use different criteria for equality than the default equals/hashCode. </p>
   *
   * <p> For the case where the new value is similar to the old value (per the {@link BiPredicate}), we could
   * also have chosen the semantics of 'always replace existing value'. This will make a difference in cases where the
   * similarity predicate is inexact, such as if it uses an epsilon. In that case, the actual value under the key
   * would be different between the two semantics. At any rate, we will choose the 'don't replace if similar'
   * semantics. </p>
   *
   * <p> Returns true if a new value was added, and false if we were trying to replace a 'similar' value
   * per the {@link BiPredicate} supplied. </p>
   */
  public boolean putAssumingNoChange(K key, V value, BiPredicate<V, V> valuesAreSimilar) {
    if (key == null || value == null) {
      throw new IllegalArgumentException(smartFormat(
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableRBMap",
          key, value));
    }
    V previousValue = rawMap.get(key);
    if (previousValue == null) {
      put(key, value);
      return true;
    }

    RBPreconditions.checkArgument(
        valuesAreSimilar.test(previousValue, value),
        "In putAssumingNoChange: key %s has value %s which is different than new value %s",
        key, previousValue, value);
    return false;
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
   * E.g. {@code MutableRBMap<AssetClass, List<?>>}
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
   *   {@code mutableLotsMap.possiblyInitializeAndThenUpdateInPlace(
   *                     taxLot.getInstrumentId(),
   *                     () -> newArrayList(),
   *                     list -> list.add(taxLot))}
   * </pre>
   *
   * previously, the last line would have been the following, which is harder to read, even when properly indented:
   * <pre>
   *                     {@code list -> { list.add(taxLot); return list; })}
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

  public boolean remove(K key, V value) {
    return rawMap.remove(key, value);
  }

  public boolean replace(K key, V oldValue, V newValue) {
    return rawMap.replace(key, oldValue, newValue);
  }

  public V replace(K key, V value) {
    return rawMap.replace(key, value);
  }

  @Override
  public String toString() {
    return rawMap.toString();
  }

  private void checkKeyIsNotNull(K key) {
    if (key == null) {
      throw new IllegalArgumentException("A MutableRBMap does not allow null keys");
    }
  }

}
