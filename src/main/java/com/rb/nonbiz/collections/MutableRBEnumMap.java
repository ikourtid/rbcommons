package com.rb.nonbiz.collections;

import com.rb.nonbiz.util.RBPreconditions;
import org.checkerframework.checker.units.qual.K;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

/**
 * This is one of the rare mutable data classes in the codebase.
 * Typically, we will build a {@link MutableRBEnumMap} and then 'lock' it into an {@link RBEnumMap} in the same method,
 * and then return it. We should (almost) never pass around a {@link MutableRBEnumMap}.
 */
public class MutableRBEnumMap<E extends Enum<E>, V> {

  private final Class<E> enumClass;
  private final EnumMap<E, V> rawMap;

  private MutableRBEnumMap(Class<E> enumClass, EnumMap<E, V> rawMap) {
    this.enumClass = enumClass;
    this.rawMap = rawMap;
  }

  public static <E extends Enum<E>, V> MutableRBEnumMap<E, V> newMutableRBEnumMap(Class<E> enumClass) {
    return new MutableRBEnumMap<>(enumClass, new EnumMap<>(enumClass));
  }

  public static <E extends Enum<E>, V> MutableRBEnumMap<E, V> newMutableRBEnumMap(
      Class<E> enumClass,
      RBSet<? extends E> keys,
      Supplier<V> value) {
    MutableRBEnumMap<E, V> mutableRBEnumMap = new MutableRBEnumMap<E, V>(enumClass, new EnumMap<E, V>(enumClass));
    keys.forEach(key -> mutableRBEnumMap.put(key, value.get()));
    return mutableRBEnumMap;
  }

  public static <E extends Enum<E>, V> MutableRBEnumMap<E, V> newMutableRBEnumMap(
      RBEnumMap<E, V> initialValues) {
    MutableRBEnumMap<E, V> mutableMap = newMutableRBEnumMap(initialValues.getEnumClass());
    mutableMap.addAllAssumingNoOverlap(initialValues);
    return mutableMap;
  }

  public static <E extends Enum<E>, V> MutableRBEnumMap<E, V> newMutableRBEnumMapFromPlainRBMap(
      Class<E> enumClass, RBMap<E, V> rbMap) {
    MutableRBEnumMap<E, V> mutableMap = newMutableRBEnumMap(enumClass);
    rbMap.forEachEntry( (enumKey, value) -> mutableMap.put(enumKey, value));
    return mutableMap;
  }

  /**
   * Creates a copy of this map, but also adds the contents of the specified map.
   * Throws if any key appears in both maps.
   */
  public void addAllAssumingNoOverlap(RBEnumMap<E, V> additionalValues) {
    additionalValues.forEachEntryInKeyOrder( (key, value) -> putAssumingAbsent(key, value));
  }

  public Class<E> getEnumClass() {
    return enumClass;
  }

  public Map<E, V> asMap() {
    return rawMap;
  }

  public int size() {
    return rawMap.size();
  }

  public boolean isEmpty() {
    return rawMap.isEmpty();
  }

  public boolean containsKey(E key) {
    return rawMap.containsKey(key);
  }

  public boolean containsValue(V value) {
    return rawMap.containsValue(value);
  }

  /**
   * #get on a regular Map returns null if the value is not there. We don't like nulls in the codebase,
   * plus that behavior is confusing to someone new to Java.
   * Instead, {@link MutableRBEnumMap} has:
   * #getOptional (which will return an Optional.empty() if there is no value for the specified key),
   * #getOrThrow, which assumes the value is there (throws otherwise), and returns the value
   *
   * <p> Ideally, since we should never be passing a {@link MutableRBEnumMap} around, you should use this sparingly
   * and only get values from the 'locked' {@link RBEnumMap} that you'll convert the MutableRBEnumMap to. </p>
   */
  public Optional<V> getOptional(E key) {
    checkKeyIsNotNull(key);
    return Optional.ofNullable(rawMap.get(key));
  }

  /**
   * #get on a regular Map returns null if the value is not there. We don't like nulls in the codebase,
   * plus that behavior is confusing to someone new to Java.
   * Instead, {@link MutableRBEnumMap} has:
   * #getOptional (which will return an Optional.empty() if there is no value for the specified key),
   * #getOrThrow, which assumes the value is there (throws otherwise), and returns the value
   *
   * <p> Ideally, since we should never be passing a {@link MutableRBEnumMap} around, you should use this sparingly
   * and only get values from the 'locked' {@link RBEnumMap} that you'll convert the {@link MutableRBEnumMap} to. </p>
   */
  public V getOrThrow(E key) {
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
  public V getOrThrow(E key, String template, Object...args) {
    checkKeyIsNotNull(key);
    Optional<V> value = getOptional(key);
    RBPreconditions.checkArgument(value.isPresent(), template, args);
    return value.get();
  }

  public V remove(E key) {
    return rawMap.remove(key);
  }

  public void putAll(Map<? extends E, ? extends V> m) {
    rawMap.putAll(m);
  }

  public void clear() {
    rawMap.clear();
  }

  public Set<E> keySet() {
    return rawMap.keySet();
  }

  public Collection<V> values() {
    return rawMap.values();
  }

  public Set<Map.Entry<E, V>> entrySet() {
    return rawMap.entrySet();
  }

  /**
   * Get a value for a key.
   * If it doesn't exist, return defaultValue, but don't modify the map to add a key {@code ->} defaultValue mapping.
   */
  public V getOrDefault(E key, V defaultValue) {
    RBPreconditions.checkArgument(
        key != null,
        "A MutableRBEnumMap does not allow null keys");
    return rawMap.getOrDefault(key, defaultValue);
  }

  /**
   * Get a value for a key.
   * If it doesn't exist, return defaultValue, but don't modify the map to add a key {@code ->} defaultValue mapping.
   */
  public V getOrDefault(E key, Supplier<V> defaultValue) {
    RBPreconditions.checkArgument(
        key != null,
        "A MutableRBEnumMap does not allow null keys");
    V value = rawMap.get(key);
    return value == null
        ? defaultValue.get()
        : value;
  }

  /**
   * Get a value for a key. If it doesn't exist, add the key {@code ->} defaultValue mapping, and return defaultValue.
   */
  public V getIfPresentElsePut(E key, Supplier<V> defaultValue) {
    RBPreconditions.checkArgument(
        key != null,
        "A MutableRBEnumMap does not allow null keys");
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
  public V put(E key, V value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException(smartFormat(
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableRBEnumMap",
          key, value));
    }
    return rawMap.put(key, value);
  }

  /**
   * Adds a key/value mapping ONLY if there's no value for this key yet.
   *
   * Use this instead of #put for extra safety, when applicable.
   */
  public void putIfAbsent(E key, V value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException(smartFormat(
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableRBEnumMap",
          key, value));
    }
    rawMap.putIfAbsent(key, value);
  }

  /**
   * Adds a key/value mapping ONLY if there's no value for this key yet.
   *
   * Use this instead of #put for extra safety, when applicable.
   */
  public void putIfAbsent(E key, Supplier<V> valueSupplier) {
    if (key == null) {
      throw new IllegalArgumentException(smartFormat(
          "Key cannot be null in a MutableRBEnumMap; value was ( %s ) ",
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
  public void putAssumingAbsent(E key, V value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException(smartFormat(
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableRBEnumMap",
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
  public void putAssumingAbsentAllowingNullValue(E key, V value) {
    if (key == null) {
      throw new IllegalArgumentException(smartFormat(
          "Key ( %s ) cannot be null in a MutableRBEnumMap; value was %s",
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
  public void putAssumingPresent(E key, V value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException(smartFormat(
          "Neither key ( %s ) nor value ( %s ) can be null in a MutableRBEnumMap",
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
  public void putOrModifyExisting(E key, V value, BinaryOperator<V> whenPresent) {
    Optional<V> existingValue = getOptional(key);
    put(key, existingValue.isPresent() ? whenPresent.apply(existingValue.get(), value) : value);
  }

  /**
   * Similar to putOrModifyExisting, except that this is most useful for situations where you always want to initialize
   * with a non-trivial object (e.g. an empty array list), and then perform operations on it.
   * E.g. {@code MutableRBEnumMap<AssetClass, List<?>>}
   * We use a {@code Supplier<V>} instead of just V, so that we won't always need to initialize e.g. a newArrayList()
   * even for those cases where there's already a value under the key.
   *
   * Unlike eitherInitializeOrUpdate, if we need to initialize the value for this key, we will still call the update
   * operator.
   */
  public void possiblyInitializeAndThenUpdate(E key, Supplier<V> initialValueIfNoneExists, UnaryOperator<V> modifierIfValueExists) {
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
  public void possiblyInitializeAndThenUpdateInPlace(E key, Supplier<V> initialEmptyValueIfNoneExists, Consumer<V> modifierInPlace) {
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
  public void eitherInitializeOrUpdate(E key, Supplier<V> initialValueIfNoneExists, UnaryOperator<V> modifierIfValueExists) {
    Optional<V> maybeExisting = getOptional(key);
    put(key, maybeExisting.isPresent()
        ? modifierIfValueExists.apply(maybeExisting.get())
        : initialValueIfNoneExists.get());
  }

  public boolean remove(E key, V value) {
    return rawMap.remove(key, value);
  }

  public boolean replace(E key, V oldValue, V newValue) {
    return rawMap.replace(key, oldValue, newValue);
  }

  public V replace(E key, V value) {
    return rawMap.replace(key, value);
  }

  @Override
  public String toString() {
    return rawMap.toString();
  }

  // We normally do not implement equals/hashCode, but we do for our more basic collection classes.
  // We rarely (if ever) need to compare two mutable maps for equality (we only need to do that for immutable maps),
  // but RBEnumMap includes a MutableRBEnumMap, and it relies on this equals/hashCode in order to work.
  // IDE-generated
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MutableRBEnumMap<?, ?> that = (MutableRBEnumMap<?, ?>) o;
    return enumClass.equals(that.enumClass) && rawMap.equals(that.rawMap);
  }

  // We normally do not implement equals/hashCode, but we do for our more basic collection classes.
  // We rarely (if ever) need to compare two mutable maps for equality (we only need to do that for immutable maps),
  // but RBEnumMap includes a MutableRBEnumMap, and it relies on this equals/hashCode in order to work.
  // IDE-generated
  @Override
  public int hashCode() {
    return Objects.hash(enumClass, rawMap);
  }

  private void checkKeyIsNotNull(E key) {
    if (key == null) {
      throw new IllegalArgumentException("A MutableRBEnumMap does not allow null keys");
    }
  }

}
