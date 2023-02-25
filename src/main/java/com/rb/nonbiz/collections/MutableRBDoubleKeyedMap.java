package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import static com.google.common.collect.Maps.newTreeMap;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

/**
 * A map from a double to a value.
 *
 * <p> We can't use {@link MutableRBMap} for this. In general, it's a bad idea to use doubles as keys, because double equality
 * can be approximate in case a number is the result of a calculation (e.g. 0.8 is not the same as 0.2 * 0.4),
 * so behavior can be unexpected. </p>
 *
 * <p> This special map lets you look up values an 'almost equal' keys, subject to an epsilon. </p>
 *
 * <p> Unlike {@link RBDoubleKeyedMap}, this is mutable. </p>
 */
public class MutableRBDoubleKeyedMap<V> {

  /**
   * If more than one key / value pair is within epsilon of the requested double key value, what should we do?
   *
   * <p> For example, if say a map has keys 71.0 and 73.0, and the epsilon is 1.2. Then, if we try to get the value
   * for key 72.0, both 71 and 73 are within 1.2 of 72, so there's more than one choice. </p>
   */
  public enum BehaviorWhenTwoDoubleKeysAreClose {

    /**
     * Throw an exception, to be safe.
     */
    THROW_EXCEPTION,

    /**
     * Use the 'floor key'; in the example, it would be the value keyed by 71.0.
     */
    USE_FLOOR,

    /**
     * Use the 'ceiling key'; in the example, it would be the value keyed by 73.0.
     */
    USE_CEILING,

    /**
     * Return the value using the key with the smallest distance to that specified: for 71.9, return the value
     * mapped by 71; for 72.1, return the value mapped by key 73.
     *
     * <p> If requesting the value for 72.0, which is exactly between 71 and 73 (example above), and if the double
     * equality ends up being exact, use the value mapped by the 'floor key' - in this case, 71. </p>
     */
    USE_NEAREST_OR_FLOOR,

    /**
     * Return the value using the key with the smallest distance to that specified: for 71.9, return the value
     * mapped by 71; for 72.1, return the value mapped by key 73.
     *
     * <p> If requesting the value for 72.0, which is exactly between 71 and 73 (example above), and if the double
     * equality ends up being exact, use the value mapped by the 'ceiling key' - in this case, 73. </p>
     */
    USE_NEAREST_OR_CEILING

  }

  private final TreeMap<Double, V> rawMap;

  private MutableRBDoubleKeyedMap(TreeMap<Double, V> rawMap) {
    this.rawMap = rawMap;
  }

  /**
   * Our static constructors don't normally have 'new' prepended, but this parallels newMutableRBMap, newMutableIidMap, etc.
   */
  public static <V> MutableRBDoubleKeyedMap<V> newMutableRBDoubleKeyedMap() {
    return new MutableRBDoubleKeyedMap<>(newTreeMap());
  }

  /**
   * If an exactly equal double key exists, then this replaces what's there. Otherwise, it creates a new entry.
   * Unlike {@link RBMap} etc., we don't do putAssumingAbsent etc., because checking whether a key already exists
   * is somewhat expensive, plus it's not needed in the current (Jan 2023) use case. We could add that later as needed.
   */
  public void put(double key, V value) {
    rawMap.put(key, value);
  }

  public V getOrThrow(
      double key,
      Epsilon epsilon,
      BehaviorWhenTwoDoubleKeysAreClose behaviorWhenTwoDoubleKeysAreClose) {
    V valueOrNull = getOrNull(key, epsilon, behaviorWhenTwoDoubleKeysAreClose);
    // Not using checkNotNull because it's too low level for the caller to know we're using nulls this deep in the code,
    // which we normally avoid anyway.
    RBPreconditions.checkArgument(
        valueOrNull != null,
        "Cannot find value using key= %s ; epsilon= %s ; behaviorOnAlmostEqualDoubleKeys= %s",
        key, epsilon, behaviorWhenTwoDoubleKeysAreClose);
    return valueOrNull;
  }

  public Optional<V> getOptional(
      double key,
      Epsilon epsilon,
      BehaviorWhenTwoDoubleKeysAreClose behaviorWhenTwoDoubleKeysAreClose) {
    return Optional.ofNullable(getOrNull(key, epsilon, behaviorWhenTwoDoubleKeysAreClose));
  }

  // Note that we almost never return null, and we avoid doing that even in private methods such as this one,
  // but this makes it easy to centralize the logic so that both getOptional and getOrThrow can rely on this.
  private V getOrNull(double key, Epsilon epsilon, BehaviorWhenTwoDoubleKeysAreClose behaviorWhenTwoDoubleKeysAreClose) {
    Entry<Double, V>   floorEntry = rawMap.floorEntry(key);
    Entry<Double, V> ceilingEntry = rawMap.ceilingEntry(key);

    Entry<Double, V>   floorEntryWithinEpsilon =
        floorEntry != null && epsilon.valuesAreWithin(floorEntry.getKey(), key)
            ? floorEntry
            : null;
    Entry<Double, V> ceilingEntryWithinEpsilon =
        ceilingEntry != null && epsilon.valuesAreWithin(ceilingEntry.getKey(), key)
            ? ceilingEntry
            : null;

    if (floorEntryWithinEpsilon == null && ceilingEntryWithinEpsilon == null) {
      return null;
    }
    if (floorEntryWithinEpsilon == null) { // so ceilingEntry must be non-null
      return epsilon.valuesAreWithin(ceilingEntryWithinEpsilon.getKey(), key)
          ? ceilingEntryWithinEpsilon.getValue()
          : null;
    } else if (ceilingEntryWithinEpsilon == null) { // floorEntry is non-null
      return epsilon.valuesAreWithin(floorEntryWithinEpsilon.getKey(), key)
          ? floorEntryWithinEpsilon.getValue()
          : null;
    }

    // OK, at this point, we have two values to choose from - one below and one above.
    // First, check to see if it's the exact same double key (rare). If so, there's really no choice.
    // This is an intentional double comparison, so it will look like a warning in the IDE.
    if (floorEntryWithinEpsilon.getKey() == ceilingEntryWithinEpsilon.getKey()) {
      return RBSimilarityPreconditions.checkBothSame(
          floorEntryWithinEpsilon.getValue(),
          ceilingEntryWithinEpsilon.getValue(),
          "Internal error; same keys but different values in the MutableRBDoubleKeyedMap");
    }

    switch (behaviorWhenTwoDoubleKeysAreClose) {
      case THROW_EXCEPTION:
        throw new IllegalArgumentException(smartFormat(
            "Lookup key %s is within epsilon %s of consecutive keys %s and %s ; throwing exception, as requested",
            key, floorEntryWithinEpsilon.getKey(), ceilingEntryWithinEpsilon.getKey()));

      case USE_FLOOR:
        return floorEntryWithinEpsilon.getValue();

      case USE_CEILING:
        return ceilingEntryWithinEpsilon.getValue();

      case USE_NEAREST_OR_FLOOR:
        // In the event both keys are equally far from each other, it's a tie, so using the floor, as per this enum.
        return Math.abs(key - floorEntryWithinEpsilon.getKey()) <= Math.abs(key - ceilingEntryWithinEpsilon.getKey())
            ? floorEntryWithinEpsilon.getValue()
            : ceilingEntryWithinEpsilon.getValue();

      case USE_NEAREST_OR_CEILING:
        // In the event both keys are equally far from each other, it's a tie, so using the floor, as per this enum.
        return Math.abs(key - ceilingEntryWithinEpsilon.getKey()) <= Math.abs(key - floorEntryWithinEpsilon.getKey())
            ? ceilingEntryWithinEpsilon.getValue()
            : floorEntryWithinEpsilon.getValue();

      default:
        throw new IllegalArgumentException(smartFormat(
            "Internal error: not handled: %s", behaviorWhenTwoDoubleKeysAreClose));
    }
  }

  public int size() {
    return rawMap.size();
  }

  // Do not use this; it's here to help the test matcher
  @VisibleForTesting
  TreeMap<Double, V> getRawTreeMapUnsafe() {
    return rawMap;
  }

  @Override
  public String toString() {
    return Strings.format(
        "[MRBDKM %s : %s MRBKDM]",
        rawMap.size(), rawMap.toString());
  }

}
